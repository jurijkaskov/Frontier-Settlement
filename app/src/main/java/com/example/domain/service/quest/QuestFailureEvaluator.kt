package com.example.domain.service.quest

import com.example.domain.model.GameState
import com.example.domain.model.quest.QuestDefinition
import com.example.domain.model.quest.QuestFailureCondition
import com.example.domain.model.quest.QuestState

/**
 * Result of evaluating quest failure conditions.
 */
data class QuestFailureResult(
    val isFailed: Boolean,
    val failureReasonRu: String? = null
)

/**
 * Centralized evaluator to check if an active quest has been failed.
 */
object QuestFailureEvaluator {

    fun evaluate(
        definition: QuestDefinition,
        questState: QuestState,
        gameState: GameState
    ): QuestFailureResult {
        // 1. Time Limit / Deadline check
        if (questState.deadlineGameDateTime != null && gameState.gameDateTime > questState.deadlineGameDateTime) {
            return QuestFailureResult(
                isFailed = true,
                failureReasonRu = "Истёк установленный срок выполнения контракта."
            )
        }

        // 2. Failure conditions from definition
        definition.failureConditions.forEach { condition ->
            when (condition) {
                is QuestFailureCondition.IncompatibleWorldFlag -> {
                    val flagVal = gameState.worldFlags[condition.flag] ?: false
                    if (flagVal == condition.failureValue) {
                        return QuestFailureResult(
                            isFailed = true,
                            failureReasonRu = condition.reasonRu
                        )
                    }
                }
                is QuestFailureCondition.FactionRelationBelow -> {
                    val points = gameState.factionRelations[condition.factionId]?.points ?: 0
                    if (points < condition.thresholdPoints) {
                        return QuestFailureResult(
                            isFailed = true,
                            failureReasonRu = condition.reasonRu
                        )
                    }
                }
                is QuestFailureCondition.TimeLimitExpired -> {
                    if (questState.deadlineGameDateTime != null && gameState.gameDateTime > questState.deadlineGameDateTime) {
                        return QuestFailureResult(
                            isFailed = true,
                            failureReasonRu = condition.reasonRu
                        )
                    }
                }
                is QuestFailureCondition.TargetDestroyed -> {
                    val isDestroyed = gameState.worldFlags["destroyed_${condition.targetId}"] ?: false
                    if (isDestroyed) {
                        return QuestFailureResult(
                            isFailed = true,
                            failureReasonRu = condition.reasonRu
                        )
                    }
                }
                is QuestFailureCondition.CustomCondition -> {
                    val customTriggered = questState.customFlags[condition.conditionId] == "failed"
                    if (customTriggered) {
                        return QuestFailureResult(
                            isFailed = true,
                            failureReasonRu = condition.reasonRu
                        )
                    }
                }
            }
        }

        return QuestFailureResult(isFailed = false)
    }
}
