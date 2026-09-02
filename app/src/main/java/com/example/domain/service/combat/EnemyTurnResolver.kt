package com.example.domain.service.combat

import com.example.domain.model.*
import com.example.domain.service.combat.ai.EnemyAIController

/**
 * Tactical AI solver for enemy combatants.
 * Delegates to [EnemyAIController] for full utility-based AI evaluation and execution.
 */
object EnemyTurnResolver {

    /**
     * Executes the turn for the currently active enemy combatant using full utility AI.
     */
    fun resolveEnemyTurn(
        state: CombatState,
        seed: Long = state.instanceSeed + (state.roundNumber * 47L) + state.currentTurnIndex
    ): CombatState {
        val result = EnemyAIController.resolveEnemyTurn(state = state, seed = seed)
        return result.updatedCombatState
    }
}


