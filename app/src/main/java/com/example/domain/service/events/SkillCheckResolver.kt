package com.example.domain.service.events

import com.example.domain.model.*
import java.util.Random

/**
 * Service to deterministically evaluate character skill and attribute checks during expedition events.
 * Accounts for base stats, equipped items, traits, role affinities, and repeatable pseudo-random dice rolls.
 */
object SkillCheckResolver {

    /**
     * Resolves a skill check for a specific character undertaking a challenge.
     *
     * @param actor The squad member performing the action.
     * @param requirement The difficulty and stat requirements of the check.
     * @param inventoryItems Global inventory to resolve equipped gear bonuses.
     * @param seed Deterministic seed ensuring reproducible results across recompositions and app restarts.
     */
    fun resolveCheck(
        actor: Character,
        requirement: SkillCheckRequirement,
        inventoryItems: List<WarehouseItem> = emptyList(),
        seed: Long = System.currentTimeMillis()
    ): SkillCheckResult {
        val effectiveStats = actor.getEffectiveStats(inventoryItems)
        val statValue = when (requirement.statType) {
            CharacterStatType.ATTACK -> effectiveStats.attack
            CharacterStatType.DEFENSE -> effectiveStats.defense
            CharacterStatType.SCAVENGING -> effectiveStats.scavengingSkill
            CharacterStatType.ENGINEERING -> effectiveStats.engineeringSkill
            CharacterStatType.MEDICAL -> effectiveStats.medicalSkill
            CharacterStatType.MAX_HEALTH -> (actor.getEffectiveMaxHealth(inventoryItems) / 10).coerceAtLeast(1)
        }

        // Equipment bonus breakdown
        val equippedItems = actor.getEquippedItems(inventoryItems).values
        val equipmentBonus = when (requirement.statType) {
            CharacterStatType.ATTACK -> equippedItems.sumOf { it.equipmentBonus.bonusAttack }
            CharacterStatType.DEFENSE -> equippedItems.sumOf { it.equipmentBonus.bonusDefense }
            CharacterStatType.SCAVENGING -> equippedItems.sumOf { it.equipmentBonus.bonusScavenging }
            CharacterStatType.ENGINEERING -> equippedItems.sumOf { it.equipmentBonus.bonusEngineering }
            CharacterStatType.MEDICAL -> equippedItems.sumOf { it.equipmentBonus.bonusMedical }
            CharacterStatType.MAX_HEALTH -> equippedItems.sumOf { it.equipmentBonus.bonusMaxHealth } / 10
        }

        // Trait bonus breakdown
        val traitBonus = when (requirement.statType) {
            CharacterStatType.ATTACK -> actor.traits.sumOf { it.bonusAttack }
            CharacterStatType.DEFENSE -> actor.traits.sumOf { it.bonusDefense }
            CharacterStatType.SCAVENGING -> actor.traits.sumOf { it.bonusScavenging }
            CharacterStatType.ENGINEERING -> actor.traits.sumOf { it.bonusEngineering }
            CharacterStatType.MEDICAL -> actor.traits.sumOf { it.bonusMedical }
            CharacterStatType.MAX_HEALTH -> actor.traits.sumOf { it.bonusMaxHealth } / 10
        }

        // Role synergy bonus (+3 if the character's role aligns with the task)
        val roleBonus = if (requirement.applicableRoles.isNotEmpty() && requirement.applicableRoles.contains(actor.role)) {
            3
        } else {
            0
        }

        // Deterministic D10 roll (1 to 10)
        val rng = Random(seed)
        val roll = rng.nextInt(10) + 1

        val totalScore = statValue + roleBonus + roll
        val isCriticalSuccess = (roll == 10) || (totalScore >= requirement.difficulty + 5)
        val isCriticalFailure = (roll == 1) && (totalScore < requirement.difficulty)
        val isSuccess = totalScore >= requirement.difficulty

        val explanation = buildString {
            append("${actor.name}: ${requirement.statType.titleRu} $statValue")
            if (roleBonus > 0) append(" + Роль ${actor.role.titleRu} (+$roleBonus)")
            append(" + Бросок [$roll] = $totalScore (Сложность: ${requirement.difficulty})")
            when {
                isCriticalSuccess -> append(" — КРИТИЧЕСКИЙ УСПЕХ!")
                isCriticalFailure -> append(" — КРИТИЧЕСКИЙ ПРОВАЛ!")
                isSuccess -> append(" — УСПЕХ")
                else -> append(" — НЕУДАЧА")
            }
        }

        return SkillCheckResult(
            isSuccess = isSuccess,
            isCriticalSuccess = isCriticalSuccess,
            isCriticalFailure = isCriticalFailure,
            roll = roll,
            statValue = statValue,
            traitBonus = traitBonus,
            equipmentBonus = equipmentBonus,
            roleBonus = roleBonus,
            totalScore = totalScore,
            difficulty = requirement.difficulty,
            actorName = actor.name,
            actorId = actor.id,
            explanation = explanation
        )
    }

    /**
     * Estimates qualitatively the chance of success for the UI before committing the action.
     * Returns a qualitative label ("Высокий шанс", "Средний шанс", "Низкий шанс", "Экстремальный риск").
     */
    fun estimateSuccessOdds(
        actor: Character,
        requirement: SkillCheckRequirement,
        inventoryItems: List<WarehouseItem> = emptyList()
    ): String {
        val effectiveStats = actor.getEffectiveStats(inventoryItems)
        val statValue = when (requirement.statType) {
            CharacterStatType.ATTACK -> effectiveStats.attack
            CharacterStatType.DEFENSE -> effectiveStats.defense
            CharacterStatType.SCAVENGING -> effectiveStats.scavengingSkill
            CharacterStatType.ENGINEERING -> effectiveStats.engineeringSkill
            CharacterStatType.MEDICAL -> effectiveStats.medicalSkill
            CharacterStatType.MAX_HEALTH -> (actor.getEffectiveMaxHealth(inventoryItems) / 10).coerceAtLeast(1)
        }
        val roleBonus = if (requirement.applicableRoles.contains(actor.role)) 3 else 0
        val baseScore = statValue + roleBonus
        val diff = requirement.difficulty - baseScore

        return when {
            diff <= 1 -> "Очень высокий шанс"
            diff <= 4 -> "Высокий шанс"
            diff <= 7 -> "Средний шанс"
            diff <= 9 -> "Низкий шанс"
            else -> "Крайне опасный риск"
        }
    }
}
