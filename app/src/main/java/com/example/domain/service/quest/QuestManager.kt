package com.example.domain.service.quest

import com.example.domain.model.*
import com.example.domain.model.quest.*

/**
 * Result bundle for transactional Quest operations.
 */
data class QuestOperationResult(
    val updatedGameState: GameState,
    val isSuccess: Boolean,
    val messageRu: String,
    val rewardResult: QuestRewardResult? = null
)

/**
 * High-level domain manager orchestrating all Quest lifecycle actions (accept, decline, deliver, turn in, track).
 */
object QuestManager {

    /**
     * Accepts an AVAILABLE quest and activates its tracking and objective progression.
     */
    fun acceptQuest(state: GameState, questId: String): QuestOperationResult {
        val def = QuestCatalog.get(questId)
            ?: return QuestOperationResult(state, false, "Задание $questId не найдено в каталоге.")

        val reqResult = QuestRequirementEvaluator.evaluate(def, state)
        if (!reqResult.isMet) {
            return QuestOperationResult(
                state,
                false,
                "Условия не выполнены: ${reqResult.unmetRequirements.joinToString(", ")}"
            )
        }

        val existing = state.questStates[questId]
        if (existing != null && (existing.status == QuestStatus.ACTIVE || existing.status == QuestStatus.COMPLETED)) {
            return QuestOperationResult(state, false, "Задание уже активно или завершено.")
        }

        val initialProgress = def.objectives.associate { objDef ->
            val depsMet = objDef.dependsOnObjectiveIds.isEmpty()
            objDef.id to QuestObjectiveProgress(
                objectiveId = objDef.id,
                targetAmount = objDef.requiredAmount,
                status = if (depsMet) ObjectiveStatus.IN_PROGRESS else ObjectiveStatus.NOT_STARTED
            )
        }

        val deadline = def.timeLimitDays?.let { days ->
            state.gameDateTime.plusDuration(GameDuration.ofDays(days))
        }

        val newQuestState = (existing ?: QuestState(questId = questId)).copy(
            status = QuestStatus.ACTIVE,
            acceptedGameDateTime = state.gameDateTime,
            deadlineGameDateTime = deadline,
            objectiveProgress = initialProgress
        )

        val updatedMap = state.questStates + (questId to newQuestState)
        val newTracked = state.trackedQuestId ?: questId

        val stateAfterAccept = state.copy(
            questStates = updatedMap,
            trackedQuestId = newTracked,
            dayLogs = listOf("📋 Принято задание: «${def.titleRu}».") + state.dayLogs.take(19)
        )

        // Run sync to immediately evaluate any pre-existing conditions (e.g. current materials in warehouse)
        val syncedState = syncQuestsOnStateChange(stateAfterAccept)

        return QuestOperationResult(syncedState, true, "Задание «${def.titleRu}» принято.")
    }

    /**
     * Declines an AVAILABLE quest if allowed.
     */
    fun declineQuest(state: GameState, questId: String): QuestOperationResult {
        val def = QuestCatalog.get(questId)
            ?: return QuestOperationResult(state, false, "Задание $questId не найдено.")

        if (!def.canDecline) {
            return QuestOperationResult(state, false, "Это ключевое задание нельзя отклонить.")
        }

        val existing = state.questStates[questId]
        val updatedQuest = (existing ?: QuestState(questId = questId)).copy(
            status = QuestStatus.DECLINED
        )
        val updatedMap = state.questStates + (questId to updatedQuest)
        val newTracked = if (state.trackedQuestId == questId) null else state.trackedQuestId

        val nextState = state.copy(
            questStates = updatedMap,
            trackedQuestId = newTracked,
            dayLogs = listOf("Задание «${def.titleRu}» отклонено.") + state.dayLogs.take(19)
        )

        return QuestOperationResult(nextState, true, "Задание «${def.titleRu}» отклонено.")
    }

    /**
     * Turns in a ready-to-claim quest, completing it and awarding rewards idempotently.
     */
    fun turnInQuest(state: GameState, questId: String): QuestOperationResult {
        val def = QuestCatalog.get(questId)
            ?: return QuestOperationResult(state, false, "Задание $questId не найдено.")

        val questState = state.questStates[questId]
            ?: return QuestOperationResult(state, false, "Состояние задания отсутствует.")

        if (questState.status == QuestStatus.COMPLETED) {
            return QuestOperationResult(state, false, "Задание уже было завершено.")
        }

        val isReady = QuestCompletionEvaluator.isReadyForCompletion(def, questState) || questState.status == QuestStatus.READY_TO_CLAIM
        if (!isReady) {
            return QuestOperationResult(state, false, "Не все обязательные цели задания выполнены.")
        }

        val rewardResult = QuestRewardProcessor.applyRewards(state, questState, def)
        val newTracked = if (state.trackedQuestId == questId) {
            // Find another active quest to track if available
            rewardResult.updatedGameState.questStates.values.find { it.isActive }?.questId
        } else state.trackedQuestId

        val finalState = rewardResult.updatedGameState.copy(trackedQuestId = newTracked)

        return QuestOperationResult(
            updatedGameState = finalState,
            isSuccess = true,
            messageRu = "Задание «${def.titleRu}» успешно выполнено!",
            rewardResult = rewardResult
        )
    }

    /**
     * Delivers bulk resources to an active delivery objective.
     */
    fun deliverResource(
        state: GameState,
        questId: String,
        objectiveId: String,
        amountToDeliver: Int
    ): QuestOperationResult {
        val def = QuestCatalog.get(questId)
            ?: return QuestOperationResult(state, false, "Задание не найдено.")
        val questState = state.questStates[questId]
            ?: return QuestOperationResult(state, false, "Задание не активно.")
        val objDef = def.objectives.find { it.id == objectiveId }
            ?: return QuestOperationResult(state, false, "Цель не найдена.")

        val resType = ResourceType.entries.find { it.name.equals(objDef.targetId, ignoreCase = true) }
            ?: ResourceType.MATERIALS

        val available = state.resources[resType]

        val objProg = questState.objectiveProgress[objectiveId]
        val currentProg = objProg?.currentAmount ?: 0
        val remainingNeeded = (objDef.requiredAmount - currentProg).coerceAtLeast(0)
        val actualDeliver = minOf(amountToDeliver, available, remainingNeeded)

        if (actualDeliver <= 0) {
            return QuestOperationResult(state, false, "Недостаточно ресурсов на складе для передачи.")
        }

        // Deduct resources
        val updatedRes = state.resources.withResource(resType, available - actualDeliver)

        val stateAfterDeduct = state.copy(resources = updatedRes)
        val event = GameEvent.ResourceDelivered(questId, objectiveId, resType, actualDeliver)
        val nextState = QuestProgressProcessor.process(event, stateAfterDeduct)

        return QuestOperationResult(
            nextState,
            true,
            "Передано $actualDeliver ${resType.nameRu} по заданию «${def.titleRu}»."
        )
    }

    /**
     * Delivers an inventory quest item.
     */
    fun deliverItem(
        state: GameState,
        questId: String,
        objectiveId: String,
        itemId: String
    ): QuestOperationResult {
        val def = QuestCatalog.get(questId)
            ?: return QuestOperationResult(state, false, "Задание не найдено.")
        val hasItem = state.inventoryItems.any { it.id == itemId && it.quantity > 0 }
        if (!hasItem) {
            return QuestOperationResult(state, false, "Предмет $itemId отсутствует на складе.")
        }

        // Consume item
        val updatedInv = state.inventoryItems.mapNotNull { item ->
            if (item.id == itemId) {
                if (item.quantity > 1) item.copy(quantity = item.quantity - 1) else null
            } else item
        }

        val stateAfterItem = state.copy(inventoryItems = updatedInv)
        val event = GameEvent.ItemDelivered(questId, objectiveId, itemId)
        val nextState = QuestProgressProcessor.process(event, stateAfterItem)

        return QuestOperationResult(
            nextState,
            true,
            "Предмет передан по заданию «${def.titleRu}»."
        )
    }

    /**
     * Sets or toggles the actively tracked quest in the HUD.
     */
    fun setTrackedQuest(state: GameState, questId: String?): GameState {
        return state.copy(trackedQuestId = questId)
    }

    /**
     * Syncs all world state values into active quest objective progress.
     */
    fun syncQuestsOnStateChange(state: GameState): GameState {
        var s = state

        // Resource stock sync
        ResourceType.entries.forEach { type ->
            val amt = s.resources[type]
            s = QuestProgressProcessor.process(GameEvent.ResourceStockUpdated(type, amt), s)
        }

        // Reputation sync
        s = QuestProgressProcessor.process(
            GameEvent.ReputationChanged(s.settlement.reputation, 0),
            s
        )

        // Faction relations sync
        s.factionRelations.forEach { (fId, rel) ->
            s = QuestProgressProcessor.process(
                GameEvent.FactionRelationChanged(fId, rel.points, 0),
                s
            )
        }

        // Building upgrade sync
        s.settlement.buildings.forEach { bld ->
            if (bld.isConstructed) {
                s = QuestProgressProcessor.process(
                    GameEvent.BuildingUpgraded(bld.type, bld.level),
                    s
                )
            }
        }

        return s
    }

    /**
     * Checks deadlines, repeatability cooldowns, and failures on daily tick.
     */
    fun onDailyTick(state: GameState): GameState {
        var s = state
        val updatedStates = s.questStates.toMutableMap()

        // 1. Check time limits and expiration
        QuestCatalog.ALL_QUESTS.forEach { def ->
            val qState = updatedStates[def.id]
            if (qState != null && qState.isActive) {
                val failure = QuestFailureEvaluator.evaluate(def, qState, s)
                if (failure.isFailed) {
                    updatedStates[def.id] = qState.copy(
                        status = QuestStatus.EXPIRED,
                        failedGameDateTime = s.gameDateTime,
                        failureReasonRu = failure.failureReasonRu ?: "Срок контракта истёк."
                    )
                }
            }
        }

        // 2. Check repeatability cooldowns
        QuestCatalog.ALL_QUESTS.forEach { def ->
            if (def.repeatability == QuestRepeatability.REPEATABLE_WITH_COOLDOWN) {
                val qState = updatedStates[def.id]
                if (qState != null && qState.status == QuestStatus.COMPLETED && qState.lastCompletedDay != null) {
                    val daysPassed = s.day - qState.lastCompletedDay
                    if (daysPassed >= def.cooldownDays) {
                        val reqResult = QuestRequirementEvaluator.evaluate(def, s)
                        if (reqResult.isMet) {
                            val initialProgress = def.objectives.associate { objDef ->
                                objDef.id to QuestObjectiveProgress(
                                    objectiveId = objDef.id,
                                    targetAmount = objDef.requiredAmount,
                                    status = ObjectiveStatus.IN_PROGRESS
                                )
                            }
                            updatedStates[def.id] = qState.copy(
                                status = QuestStatus.AVAILABLE,
                                objectiveProgress = initialProgress,
                                instanceId = "inst_${s.day}"
                            )
                        }
                    }
                }
            }
        }

        s = s.copy(questStates = updatedStates)
        return QuestProgressProcessor.process(GameEvent.DailyTick(s.day, s.gameDateTime), s)
    }
}
