package com.example.domain.service.combat.ai

import com.example.domain.model.*
import com.example.domain.service.combat.CombatActionCatalog
import com.example.domain.service.combat.CombatActionExecutor
import com.example.domain.service.combat.CombatTurnManager
import java.util.Random

/**
 * Orchestrates full AI decision-making turn loops with utility evaluation, candidate selection,
 * loop safety guards, and debug telemetry.
 */
object EnemyAIController {

    private const val MAX_AI_ACTIONS_PER_TURN = 5

    data class EnemyTurnResult(
        val updatedCombatState: CombatState,
        val decisionLogs: List<AIDecisionLog>,
        val actionsExecuted: Int
    )

    /**
     * Resolves the entire turn of an AI combatant.
     */
    fun resolveEnemyTurn(
        state: CombatState,
        difficulty: AIDifficulty = AIDifficulty.NORMAL,
        seed: Long = state.instanceSeed + (state.roundNumber * 100L) + state.currentTurnIndex
    ): EnemyTurnResult {
        if (state.isEnded) {
            return EnemyTurnResult(state, emptyList(), 0)
        }

        val actor = state.currentActiveCombatant
        if (actor == null || actor.team != CombatantTeam.ENEMY) {
            return EnemyTurnResult(state, emptyList(), 0)
        }

        // If actor cannot act (defeated or stunned), advance turn immediately
        if (!actor.canAct) {
            val advanced = CombatTurnManager.advanceTurn(state)
            return EnemyTurnResult(advanced, emptyList(), 0)
        }

        val profile = EnemyAIProfileCatalog.getProfile(actor.aiProfileId)
        val decisionLogs = mutableListOf<AIDecisionLog>()
        var currentState = state
        var actionsExecuted = 0
        val random = Random(seed)

        while (
            actionsExecuted < MAX_AI_ACTIONS_PER_TURN &&
            !currentState.isEnded &&
            currentState.currentActiveCombatant?.id == actor.id &&
            currentState.currentActiveCombatant?.canAct == true &&
            (currentState.currentActiveCombatant?.actionPoints ?: 0) > 0
        ) {
            val currentActor = currentState.currentActiveCombatant ?: break

            // 1. Evaluate candidate actions
            val candidates = EnemyActionEvaluator.evaluateCandidates(
                actor = currentActor,
                state = currentState,
                profile = profile,
                seed = seed,
                stepIndex = actionsExecuted,
                difficulty = difficulty
            )

            // Filter viable non-pass actions
            val viableCandidates = candidates.filter { 
                it.action.actionType != CombatActionType.PASS && it.finalScore >= profile.minScoreThreshold 
            }

            if (viableCandidates.isEmpty()) {
                // No viable tactical action, pass turn gracefully
                val decisionLog = AIDecisionLog(
                    actorId = currentActor.id,
                    actorName = currentActor.displayName,
                    round = currentState.roundNumber,
                    turnIndex = currentState.currentTurnIndex,
                    actionStep = actionsExecuted + 1,
                    profileId = profile.profileId,
                    candidates = candidates.take(5),
                    chosenCandidate = null,
                    reason = "Нет доступных действий с достаточным приоритетом (ОД: ${currentActor.actionPoints}). Завершение хода."
                )
                decisionLogs.add(decisionLog)

                currentState = currentState.copy(
                    aiDecisionLogs = (currentState.aiDecisionLogs + decisionLog).takeLast(20)
                )

                currentState = CombatTurnManager.advanceTurn(currentState)
                break
            }

            // 2. Select chosen candidate based on difficulty and score weights
            val chosenCandidate = selectCandidate(viableCandidates, difficulty, random)

            // 3. Log decision
            val decisionLog = AIDecisionLog(
                actorId = currentActor.id,
                actorName = currentActor.displayName,
                round = currentState.roundNumber,
                turnIndex = currentState.currentTurnIndex,
                actionStep = actionsExecuted + 1,
                profileId = profile.profileId,
                candidates = candidates.take(5),
                chosenCandidate = chosenCandidate,
                reason = chosenCandidate.explanation
            )
            decisionLogs.add(decisionLog)

            currentState = currentState.copy(
                aiDecisionLogs = (currentState.aiDecisionLogs + decisionLog).takeLast(20)
            )

            // 4. Execute chosen action
            val execResult = CombatActionExecutor.executeAction(
                state = currentState,
                action = chosenCandidate.action,
                targetId = chosenCandidate.targetId,
                seed = seed + (actionsExecuted * 37L)
            )

            if (!execResult.actionResult.success) {
                // Execution failed, break to avoid infinite loop
                currentState = if (currentState.currentActiveCombatant?.id == actor.id) {
                    CombatTurnManager.advanceTurn(currentState)
                } else currentState
                break
            }

            currentState = execResult.updatedCombatState
            actionsExecuted++

            // If battle ended from this action, stop immediately
            if (currentState.isEnded) {
                break
            }
        }

        // Safety fallback: if turn did not advance and current actor is still this enemy, advance turn
        if (!currentState.isEnded && currentState.currentActiveCombatant?.id == actor.id) {
            currentState = CombatTurnManager.advanceTurn(currentState)
        }

        return EnemyTurnResult(
            updatedCombatState = currentState,
            decisionLogs = decisionLogs,
            actionsExecuted = actionsExecuted
        )
    }

    /**
     * Selects candidate with slight probabilistic distribution based on difficulty.
     */
    private fun selectCandidate(
        candidates: List<AIActionCandidate>,
        difficulty: AIDifficulty,
        random: Random
    ): AIActionCandidate {
        if (candidates.size == 1 || difficulty == AIDifficulty.HARD) {
            return candidates.first()
        }

        // Check score delta: if top candidate is significantly better, always pick it
        val topScore = candidates[0].finalScore
        val secondScore = candidates.getOrNull(1)?.finalScore ?: 0f

        if (topScore - secondScore > 20.0f) {
            return candidates.first()
        }

        // Softmax / Weighted top-3 selection on Normal/Easy
        val topCandidates = candidates.take(3)
        val weights = when (difficulty) {
            AIDifficulty.EASY -> listOf(0.60f, 0.30f, 0.10f)
            AIDifficulty.NORMAL -> listOf(0.80f, 0.15f, 0.05f)
            AIDifficulty.HARD -> listOf(1.0f, 0.0f, 0.0f)
        }

        val roll = random.nextFloat()
        var accumulated = 0f
        for (i in topCandidates.indices) {
            accumulated += weights.getOrElse(i) { 0f }
            if (roll <= accumulated) {
                return topCandidates[i]
            }
        }

        return topCandidates.first()
    }
}
