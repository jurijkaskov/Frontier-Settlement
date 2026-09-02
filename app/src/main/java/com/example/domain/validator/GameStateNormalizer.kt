package com.example.domain.validator

import com.example.core.log.GameLogger
import com.example.domain.model.GameState
import com.example.domain.model.ResourceType

/**
 * Result of GameState normalization containing corrected state and record of fixes.
 */
data class GameStateNormalizationResult(
    val normalizedState: GameState,
    val appliedFixes: List<String>
)

/**
 * Normalizes recoverable inconsistencies in [GameState] to guarantee robust runtime invariants.
 */
object GameStateNormalizer {

    fun normalize(gameState: GameState): GameStateNormalizationResult {
        val fixes = mutableListOf<String>()
        var state = gameState

        // 1. Character health and skill point clamps
        val existingCharIds = state.characters.map { it.id }.toSet()
        val normalizedCharacters = state.characters.map { char ->
            var updated = char
            if (char.health < 0 || char.health > char.maxHealth) {
                val clampedHp = char.health.coerceIn(0, char.maxHealth)
                fixes.add("Исправлено здоровье жителя ${char.name}: ${char.health} -> $clampedHp")
                updated = updated.copy(health = clampedHp)
            }
            if (char.unspentSkillPoints < 0) {
                fixes.add("Сброшены отрицательные очки навыков жителя ${char.name}")
                updated = updated.copy(unspentSkillPoints = 0)
            }
            updated
        }
        state = state.copy(characters = normalizedCharacters)

        // 2. Squad reference cleanup
        val validSquadMembers = state.squad.memberIds.filter { existingCharIds.contains(it) }
        if (validSquadMembers.size != state.squad.memberIds.size) {
            fixes.add("Удалены отсутствующие жители из состава отряда")
            state = state.copy(squad = state.squad.copy(memberIds = validSquadMembers))
        }

        if (state.squad.leaderId != null && !existingCharIds.contains(state.squad.leaderId)) {
            fixes.add("Сброшен отсутствующий лидер отряда")
            state = state.copy(squad = state.squad.copy(leaderId = validSquadMembers.firstOrNull()))
        }

        // 3. Resource non-negative clamp
        val res = state.resources
        val clampedRes = res.copy(
            food = res.food.coerceAtLeast(0),
            water = res.water.coerceAtLeast(0),
            fuel = res.fuel.coerceAtLeast(0),
            materials = res.materials.coerceAtLeast(0),
            money = res.money.coerceAtLeast(0),
            extraResources = res.extraResources.mapValues { it.value.coerceAtLeast(0) }
        )
        if (clampedRes != res) {
            fixes.add("Исправлены отрицательные остатки ресурсов на складе")
            state = state.copy(resources = clampedRes)
        }

        // 4. Tracked quest integrity
        if (state.trackedQuestId != null) {
            val questExists = state.quests.any { it.id == state.trackedQuestId } ||
                    state.questStates.containsKey(state.trackedQuestId)
            if (!questExists) {
                fixes.add("Сброшено отслеживание несуществующего задания: ${state.trackedQuestId}")
                state = state.copy(trackedQuestId = null)
            }
        }

        // 5. Active combat HP & AP clamp
        if (state.activeCombat != null) {
            val normalizedCombatants = state.activeCombat.combatants.map { combatant ->
                val clampedHp = combatant.currentHealth.coerceIn(0, combatant.maxHealth)
                val clampedAp = combatant.actionPoints.coerceIn(0, combatant.maxActionPoints)
                combatant.copy(currentHealth = clampedHp, actionPoints = clampedAp)
            }
            state = state.copy(activeCombat = state.activeCombat.copy(combatants = normalizedCombatants))
        }

        if (fixes.isNotEmpty()) {
            GameLogger.i("GameStateNormalizer", "Applied ${fixes.size} state normalization fixes: $fixes")
        }

        return GameStateNormalizationResult(
            normalizedState = state,
            appliedFixes = fixes
        )
    }
}
