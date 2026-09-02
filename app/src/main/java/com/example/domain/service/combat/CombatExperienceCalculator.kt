package com.example.domain.service.combat

import com.example.domain.model.Character
import com.example.domain.model.CharacterStatus
import com.example.domain.model.CombatState
import com.example.domain.model.CombatantTeam

/**
 * Calculates experience rewards from tactical combat encounters and applies them idempotently.
 */
object CombatExperienceCalculator {

    /**
     * Calculates total XP reward for a combat encounter.
     */
    fun calculateCombatExperience(
        state: CombatState,
        dangerRating: Int = 1
    ): Int {
        val defeatedEnemies = state.combatants.count { it.team == CombatantTeam.ENEMY && it.isDefeated }
        val enemyXp = defeatedEnemies * CombatBalanceConfig.BASE_XP_PER_COMBATANT
        val dangerBonus = dangerRating * CombatBalanceConfig.XP_PER_DANGER_RATING
        val victoryBonus = if (state.isVictory) CombatBalanceConfig.BASE_VICTORY_BONUS_XP else 0

        return (enemyXp + dangerBonus + victoryBonus).coerceAtLeast(40)
    }

    /**
     * Applies experience to characters in squad, handling level ups and stat improvements cleanly.
     */
    fun applyExperienceToSquad(
        squadCharacters: List<Character>,
        xpAmount: Int
    ): List<Character> {
        if (xpAmount <= 0) return squadCharacters

        return squadCharacters.map { character ->
            var currentXp = character.experience + xpAmount
            var currentMaxXp = character.maxExperience
            var currentLevel = character.level
            var currentStats = character.stats

            while (currentXp >= currentMaxXp) {
                currentXp -= currentMaxXp
                currentLevel += 1
                currentMaxXp = (currentMaxXp * 1.5f).toInt()
                currentStats = currentStats.copy(
                    attack = currentStats.attack + 2,
                    defense = currentStats.defense + 1,
                    scavengingSkill = currentStats.scavengingSkill + 2
                )
            }

            character.copy(
                level = currentLevel,
                experience = currentXp,
                maxExperience = currentMaxXp,
                stats = currentStats
            )
        }
    }
}
