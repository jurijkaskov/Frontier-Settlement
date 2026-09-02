package com.example.domain.service.combat

import com.example.domain.model.*
import java.util.Random

/**
 * Initiates combat encounters from events or exploration with deterministic rolls
 * based on the expedition's seed, location danger, and character stats.
 */
object CombatInitiator {

    /**
     * Initializes a new CombatState using actual characters from the active expedition squad.
     */
    fun createCombatEncounter(
        expedition: Expedition,
        gameState: GameState,
        sourceEventId: String? = null,
        sourceChoiceId: String? = null,
        seed: Long = expedition.seed + (expedition.currentStep * 17L)
    ): CombatState {
        val random = Random(seed)

        // 1. Build Player Combatants using live Character stats
        val playerCombatants = expedition.squad.mapIndexed { index, char ->
            val effectiveStats = char.getEffectiveStats(gameState.inventoryItems)
            val baseInitiative = 10 + (char.stats.scavengingSkill / 3) + (effectiveStats.defense / 4)
            val initiativeRoll = random.nextInt(6) + 1
            val totalInitiative = baseInitiative + initiativeRoll

            Combatant(
                id = char.id,
                team = CombatantTeam.PLAYER,
                displayName = char.name,
                characterId = char.id,
                currentHealth = char.health,
                maxHealth = char.maxHealth,
                actionPoints = 4,
                maxActionPoints = 4,
                initiative = totalInitiative,
                attack = effectiveStats.attack,
                defense = effectiveStats.defense,
                status = if (char.health <= 0) CombatantStatus.DEFEATED else CombatantStatus.ACTIVE,
                role = char.role,
                avatarTag = char.avatarTag
            )
        }

        // 2. Build Thematic Enemies based on location danger level & terrain
        val dangerRating = expedition.location.dangerLevel.rating
        val enemyCount = when (expedition.location.dangerLevel) {
            DangerLevel.SAFE -> 1
            DangerLevel.LOW -> 2
            DangerLevel.MODERATE -> 2
            DangerLevel.HIGH -> 3
            DangerLevel.EXTREME -> 3
            DangerLevel.UNKNOWN -> 2
        }.coerceIn(1, 4)

        val enemyCombatants = (1..enemyCount).map { i ->
            generateEnemy(
                index = i,
                dangerRating = dangerRating,
                terrainType = expedition.location.terrainType,
                random = random
            )
        }

        val allCombatants = playerCombatants + enemyCombatants

        // 3. Compute Turn Order: sorted descending by initiative (players break ties)
        val sortedTurnOrder = allCombatants
            .filter { !it.isDefeated }
            .sortedWith(
                compareByDescending<Combatant> { it.initiative }
                    .thenBy { if (it.team == CombatantTeam.PLAYER) 0 else 1 }
            )
            .map { it.id }

        val firstCombatantId = sortedTurnOrder.firstOrNull()
        val firstCombatant = allCombatants.find { it.id == firstCombatantId }
        val initialPhase = if (firstCombatant?.team == CombatantTeam.PLAYER) {
            CombatPhase.PLAYER_TURN
        } else {
            CombatPhase.ENEMY_TURN
        }

        // Default target is the first living enemy
        val defaultTargetId = enemyCombatants.firstOrNull { !it.isDefeated }?.id

        val startLog = CombatLogEntry(
            turn = 1,
            text = "⚔️ Вражеский отряд обнаружен в секторе «${expedition.location.name}»! Очередь хода определена по инициативе.",
            isPlayerAction = true
        )

        val bonusMoney = 80 + (dangerRating * 45) + random.nextInt(30)
        val bonusMaterials = 30 + (dangerRating * 25) + random.nextInt(20)
        val bonusFuel = 10 + (dangerRating * 8)
        val bonusFood = 15 + (dangerRating * 10)

        return CombatState(
            id = "combat_${seed}_${System.currentTimeMillis()}",
            encounterTitle = "Схватка: ${expedition.location.name}",
            locationId = expedition.location.id,
            sourceEventId = sourceEventId,
            sourceChoiceId = sourceChoiceId,
            combatants = allCombatants,
            turnOrder = sortedTurnOrder,
            currentTurnIndex = 0,
            roundNumber = 1,
            selectedTargetId = defaultTargetId,
            currentPhase = initialPhase,
            logs = listOf(startLog),
            instanceSeed = seed,
            bonusLoot = GameResources(
                money = bonusMoney,
                materials = bonusMaterials,
                fuel = bonusFuel,
                food = bonusFood,
                water = 10
            ),
            xpReward = 80 + (dangerRating * 40)
        )
    }

    private fun generateEnemy(
        index: Int,
        dangerRating: Int,
        terrainType: TerrainType,
        random: Random
    ): Combatant {
        val (name, hp, atk, def, init, icon, role, aiProfileId) = when (index) {
            1 -> {
                val baseHp = 40 + (dangerRating * 15)
                val baseAtk = 11 + (dangerRating * 3)
                val baseDef = 4 + (dangerRating * 2)
                CombatantData(
                    name = "Мародёр-застрельщик",
                    hp = baseHp,
                    attack = baseAtk,
                    defense = baseDef,
                    initiative = 13 + random.nextInt(4),
                    icon = "enemy_raider",
                    role = CharacterRole.SCOUT,
                    aiProfileId = "ai_opportunist"
                )
            }
            2 -> {
                val baseHp = 60 + (dangerRating * 22)
                val baseAtk = 14 + (dangerRating * 4)
                val baseDef = 8 + (dangerRating * 2)
                CombatantData(
                    name = "Главарь банды пустоши",
                    hp = baseHp,
                    attack = baseAtk,
                    defense = baseDef,
                    initiative = 9 + random.nextInt(3),
                    icon = "enemy_boss",
                    role = CharacterRole.SOLDIER,
                    aiProfileId = "ai_aggressive"
                )
            }
            3 -> {
                val baseHp = 42 + (dangerRating * 14)
                val baseAtk = 9 + (dangerRating * 2)
                val baseDef = 5 + (dangerRating * 2)
                CombatantData(
                    name = "Пустошный знахарь",
                    hp = baseHp,
                    attack = baseAtk,
                    defense = baseDef,
                    initiative = 11 + random.nextInt(3),
                    icon = "enemy_medic",
                    role = CharacterRole.MEDIC,
                    aiProfileId = "ai_support"
                )
            }
            4 -> {
                val baseHp = 65 + (dangerRating * 20)
                val baseAtk = 10 + (dangerRating * 3)
                val baseDef = 10 + (dangerRating * 2)
                CombatantData(
                    name = "Бронированный щитовик",
                    hp = baseHp,
                    attack = baseAtk,
                    defense = baseDef,
                    initiative = 8 + random.nextInt(3),
                    icon = "enemy_guard",
                    role = CharacterRole.ENGINEER,
                    aiProfileId = "ai_cautious"
                )
            }
            else -> {
                val baseHp = 50 + (dangerRating * 18)
                val baseAtk = 12 + (dangerRating * 3)
                val baseDef = 6 + (dangerRating * 2)
                CombatantData(
                    name = "Одичалый дезертир",
                    hp = baseHp,
                    attack = baseAtk,
                    defense = baseDef,
                    initiative = 10 + random.nextInt(3),
                    icon = "enemy_deserter",
                    role = CharacterRole.SOLDIER,
                    aiProfileId = "ai_balanced"
                )
            }
        }

        return Combatant(
            id = "enemy_$index",
            team = CombatantTeam.ENEMY,
            displayName = name,
            enemyTemplateId = icon,
            currentHealth = hp,
            maxHealth = hp,
            actionPoints = 4,
            maxActionPoints = 4,
            initiative = init,
            attack = atk,
            defense = def,
            status = CombatantStatus.ACTIVE,
            role = role,
            avatarTag = icon,
            aiProfileId = aiProfileId
        )
    }

    private data class CombatantData(
        val name: String,
        val hp: Int,
        val attack: Int,
        val defense: Int,
        val initiative: Int,
        val icon: String,
        val role: CharacterRole?,
        val aiProfileId: String
    )
}
