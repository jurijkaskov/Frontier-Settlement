package com.example.domain.content.encounter

import com.example.domain.content.core.ContentGenerationContext
import com.example.domain.content.core.GameRandomProvider
import com.example.domain.content.core.GenerationResult
import com.example.domain.content.core.WeightedSelector
import com.example.domain.content.registry.GameContentRegistry
import com.example.domain.model.*
import kotlin.random.Random

/**
 * Procedural generator for tactical combat encounters and enemy combatants.
 */
object EncounterGenerator {

    /**
     * Generates a concrete [CombatState] based on encounter template, active squad, and context.
     */
    fun generateEncounter(
        encounterTemplate: EncounterTemplate,
        squad: List<Character>,
        inventoryItems: List<WarehouseItem>,
        context: ContentGenerationContext,
        registry: GameContentRegistry = GameContentRegistry,
        customIndex: Int? = null
    ): GenerationResult<CombatState> {
        val index = customIndex ?: context.generationIndex
        val seed = GameRandomProvider.deriveSeed(context.gameSeed, "encounter", encounterTemplate.id, index)
        val random = Random(seed)

        // 1. Build Player Combatants
        val playerCombatants = squad.map { char ->
            val effectiveStats = char.getEffectiveStats(inventoryItems)
            val baseInitiative = 10 + (char.stats.scavengingSkill / 3) + (effectiveStats.defense / 4)
            val initiativeRoll = random.nextInt(1, 7)

            Combatant(
                id = char.id,
                team = CombatantTeam.PLAYER,
                displayName = char.name,
                characterId = char.id,
                currentHealth = char.health,
                maxHealth = char.maxHealth,
                actionPoints = 4,
                maxActionPoints = 4,
                initiative = baseInitiative + initiativeRoll,
                attack = effectiveStats.attack,
                defense = effectiveStats.defense,
                status = if (char.health <= 0) CombatantStatus.DEFEATED else CombatantStatus.ACTIVE,
                role = char.role,
                avatarTag = char.avatarTag
            )
        }

        // 2. Determine Enemy Count
        val enemyCount = if (encounterTemplate.minEnemies >= encounterTemplate.maxEnemies) {
            encounterTemplate.minEnemies
        } else {
            random.nextInt(encounterTemplate.minEnemies, encounterTemplate.maxEnemies + 1)
        }

        // 3. Select Enemies from Pool
        val selectedEnemyTemplates = mutableListOf<EnemyTemplate>()
        val pool = encounterTemplate.enemyPool

        for (i in 0 until enemyCount) {
            val pickedEntry = WeightedSelector.select(
                candidates = pool,
                weightExtractor = { it.weight },
                random = random
            )
            val enemyTmpl = pickedEntry?.let { registry.getEnemyTemplate(it.enemyTemplateId) }
                ?: registry.enemyTemplates.values.firstOrNull()
            if (enemyTmpl != null) {
                selectedEnemyTemplates.add(enemyTmpl)
            }
        }

        // 4. Instantiate Enemy Combatants with bounded stat variance
        val dangerRating = context.dangerLevel.rating
        val enemyCombatants = selectedEnemyTemplates.mapIndexed { idx, tmpl ->
            val variance = tmpl.statVariancePercent
            val hpVariance = 1.0f + ((random.nextFloat() * 2f - 1f) * variance)
            val atkVariance = 1.0f + ((random.nextFloat() * 2f - 1f) * variance)
            val defVariance = 1.0f + ((random.nextFloat() * 2f - 1f) * variance)

            val finalHp = ((tmpl.baseHp + (dangerRating * 12)) * hpVariance).toInt().coerceAtLeast(20)
            val finalAtk = ((tmpl.baseAttack + (dangerRating * 3)) * atkVariance).toInt().coerceAtLeast(5)
            val finalDef = ((tmpl.baseDefense + (dangerRating * 2)) * defVariance).toInt().coerceAtLeast(2)
            val finalInit = tmpl.baseInitiative + random.nextInt(1, 5)

            Combatant(
                id = "enemy_${idx + 1}_${tmpl.id}",
                team = CombatantTeam.ENEMY,
                displayName = "${tmpl.nameRu} #${idx + 1}",
                enemyTemplateId = tmpl.id,
                currentHealth = finalHp,
                maxHealth = finalHp,
                actionPoints = 4,
                maxActionPoints = 4,
                initiative = finalInit,
                attack = finalAtk,
                defense = finalDef,
                status = CombatantStatus.ACTIVE,
                role = tmpl.role,
                avatarTag = tmpl.avatarTag,
                aiProfileId = tmpl.aiProfileId
            )
        }

        val allCombatants = playerCombatants + enemyCombatants

        // 5. Calculate Turn Order
        val turnOrder = allCombatants
            .filter { !it.isDefeated }
            .sortedWith(
                compareByDescending<Combatant> { it.initiative }
                    .thenBy { if (it.team == CombatantTeam.PLAYER) 0 else 1 }
            )
            .map { it.id }

        val firstCombatant = allCombatants.find { it.id == turnOrder.firstOrNull() }
        val initialPhase = if (firstCombatant?.team == CombatantTeam.PLAYER) {
            CombatPhase.PLAYER_TURN
        } else {
            CombatPhase.ENEMY_TURN
        }

        val startLog = CombatLogEntry(
            turn = 1,
            text = "⚔️ ${encounterTemplate.titleRu}! Отряд вступил в бой.",
            isPlayerAction = true,
            logType = CombatLogType.ROUND_STARTED
        )

        val combatState = CombatState(
            id = "combat_${encounterTemplate.id}_$seed",
            encounterTitle = encounterTemplate.titleRu,
            locationId = context.locationId,
            combatants = allCombatants,
            turnOrder = turnOrder,
            currentTurnIndex = 0,
            roundNumber = 1,
            selectedTargetId = enemyCombatants.firstOrNull()?.id,
            currentPhase = initialPhase,
            logs = listOf(startLog),
            instanceSeed = seed,
            bonusLoot = GameResources(
                money = 50 + (dangerRating * 30) + random.nextInt(20),
                materials = 20 + (dangerRating * 15) + random.nextInt(15),
                fuel = 5 + (dangerRating * 5)
            ),
            xpReward = encounterTemplate.baseRewardXp + (dangerRating * 25)
        )

        return GenerationResult.Success(combatState)
    }

    /**
     * Selects an appropriate encounter template for the location and danger level, then generates.
     */
    fun generateRandomEncounter(
        squad: List<Character>,
        inventoryItems: List<WarehouseItem>,
        context: ContentGenerationContext,
        registry: GameContentRegistry = GameContentRegistry
    ): GenerationResult<CombatState> {
        val eligibleTemplates = registry.encounterTemplates.values.filter { tmpl ->
            (tmpl.allowedLocationTypes.isEmpty() || tmpl.allowedLocationTypes.contains(context.locationType)) &&
                    tmpl.minDangerLevel.rating <= context.dangerLevel.rating &&
                    tmpl.maxDangerLevel.rating >= context.dangerLevel.rating
        }

        if (eligibleTemplates.isEmpty()) {
            val fallback = registry.encounterTemplates.values.firstOrNull()
                ?: return GenerationResult.NoEligibleContent("No encounter templates registered")
            return generateEncounter(fallback, squad, inventoryItems, context, registry)
        }

        val random = GameRandomProvider.createRandom(context.gameSeed, "encounter_select", context.generationIndex)
        val selected = WeightedSelector.select(
            candidates = eligibleTemplates,
            weightExtractor = { it.baseWeight },
            random = random,
            context = context
        ) ?: eligibleTemplates.first()

        return generateEncounter(selected, squad, inventoryItems, context, registry)
    }
}
