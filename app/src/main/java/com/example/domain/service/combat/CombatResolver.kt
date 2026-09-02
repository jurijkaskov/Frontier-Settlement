package com.example.domain.service.combat

import com.example.domain.model.*

/**
 * Validates and executes tactical actions performed by player-controlled or ally combatants.
 * Delegates directly to the centralized CombatActionExecutor pipeline.
 */
object CombatResolver {

    data class ActionResolutionResult(
        val updatedCombatState: CombatState,
        val updatedExpedition: Expedition? = null,
        val updatedGameState: GameState? = null,
        val isActionSuccess: Boolean = true,
        val validationErrorMessage: String? = null
    )

    /**
     * Executes an action on the currently active combatant.
     */
    fun resolveAction(
        state: CombatState,
        action: CombatAction,
        targetId: String? = state.selectedTargetId,
        expedition: Expedition? = null,
        gameState: GameState? = null,
        seed: Long = state.instanceSeed + (state.roundNumber * 31L) + state.currentTurnIndex
    ): ActionResolutionResult {
        val execResult = CombatActionExecutor.executeAction(
            state = state,
            action = action,
            targetId = targetId,
            gameState = gameState,
            seed = seed
        )

        return ActionResolutionResult(
            updatedCombatState = execResult.updatedCombatState,
            updatedExpedition = expedition,
            updatedGameState = execResult.updatedGameState ?: gameState,
            isActionSuccess = execResult.actionResult.success,
            validationErrorMessage = execResult.actionResult.errorMessage
        )
    }

    /**
     * Resolves using a consumable item in combat on a target ally.
     */
    fun resolveItemUsage(
        state: CombatState,
        itemId: String,
        targetId: String? = state.selectedTargetId,
        expedition: Expedition? = null,
        gameState: GameState? = null
    ): ActionResolutionResult {
        val execResult = CombatActionExecutor.executeItem(
            state = state,
            itemId = itemId,
            targetId = targetId,
            gameState = gameState
        )

        return ActionResolutionResult(
            updatedCombatState = execResult.updatedCombatState,
            updatedExpedition = expedition,
            updatedGameState = execResult.updatedGameState ?: gameState,
            isActionSuccess = execResult.actionResult.success,
            validationErrorMessage = execResult.actionResult.errorMessage
        )
    }
}

