package com.example.domain.service.quest

import com.example.domain.model.*
import com.example.domain.model.quest.QuestDefinition
import com.example.domain.model.quest.QuestRewardDefinition
import com.example.domain.model.quest.QuestState
import com.example.domain.model.reputation.ReputationChangeType
import com.example.domain.service.reputation.ReputationManager

/**
 * Result bundle after completing a quest and applying its rewards idempotently.
 */
data class QuestRewardResult(
    val updatedGameState: GameState,
    val updatedQuestState: QuestState,
    val rewardSummaryLogs: List<String>,
    val wasAlreadyApplied: Boolean = false
)

/**
 * Safely and idempotently applies quest rewards to GameState using unified domain processors.
 */
object QuestRewardProcessor {

    fun applyRewards(
        gameState: GameState,
        questState: QuestState,
        definition: QuestDefinition
    ): QuestRewardResult {
        val rewardId = "reward_${questState.questId}_${questState.instanceId ?: "main"}"

        // Idempotency check: if reward was already applied, do not duplicate!
        if (questState.appliedRewardIds.contains(rewardId)) {
            return QuestRewardResult(
                updatedGameState = gameState,
                updatedQuestState = questState,
                rewardSummaryLogs = listOf("Награда за задание «${definition.titleRu}» уже была начислена ранее."),
                wasAlreadyApplied = true
            )
        }

        val rewards: QuestRewardDefinition = definition.rewards
        var currentState = gameState
        val summaryLogs = mutableListOf<String>()
        summaryLogs.add("🏆 Задание выполнено: «${definition.titleRu}»!")

        // 1. Credits and Bulk Resources
        var updatedResources = currentState.resources
        rewards.resources.forEach { (type, amount) ->
            if (amount > 0) {
                val currentAmt = updatedResources[type]
                updatedResources = updatedResources.withResource(type, currentAmt + amount)
            }
        }
        if (rewards.credits > 0) {
            updatedResources = updatedResources.copy(money = updatedResources.money + rewards.credits)
        }
        currentState = currentState.copy(resources = updatedResources)

        if (rewards.credits > 0) summaryLogs.add("💰 Получено: +${rewards.credits} Кредитов")
        val resStrings = rewards.resources.filter { it.value > 0 }.map { "+${it.value} ${it.key.nameRu}" }
        if (resStrings.isNotEmpty()) {
            summaryLogs.add("📦 Припасы на склад: ${resStrings.joinToString(", ")}")
        }

        // 2. Inventory Items
        if (rewards.itemIds.isNotEmpty()) {
            val updatedInventory = currentState.inventoryItems.toMutableList()
            rewards.itemIds.forEach { itemId ->
                val existingIdx = updatedInventory.indexOfFirst { it.id == itemId }
                if (existingIdx >= 0) {
                    val existing = updatedInventory[existingIdx]
                    updatedInventory[existingIdx] = existing.copy(quantity = existing.quantity + 1)
                } else {
                    updatedInventory.add(
                        WarehouseItem(
                            id = itemId,
                            name = "Предмет $itemId",
                            category = ItemCategory.EQUIPMENT_AND_TOOLS,
                            quantity = 1,
                            unitSize = 1,
                            description = "Награда за задание",
                            baseValueCredits = 100
                        )
                    )
                }
            }
            currentState = currentState.copy(inventoryItems = updatedInventory)
            summaryLogs.add("🎁 Получены предметы: ${rewards.itemIds.joinToString()}")
        }

        // 3. XP & Settlement Experience
        if (rewards.xp > 0) {
            val updatedCharacters = currentState.characters.map { member ->
                val newExp = member.experience + rewards.xp
                val isLevelUp = newExp >= member.maxExperience
                member.copy(
                    level = if (isLevelUp) member.level + 1 else member.level,
                    experience = if (isLevelUp) newExp - member.maxExperience else newExp,
                    maxExperience = if (isLevelUp) (member.maxExperience * 1.5).toInt() else member.maxExperience,
                    unspentSkillPoints = if (isLevelUp) member.unspentSkillPoints + 1 else member.unspentSkillPoints
                )
            }
            val newSettlementExp = currentState.settlement.xp + rewards.xp
            val isBaseLevelUp = newSettlementExp >= currentState.settlement.xpToNextLevel
            val updatedSettlement = currentState.settlement.copy(
                xp = if (isBaseLevelUp) newSettlementExp - currentState.settlement.xpToNextLevel else newSettlementExp,
                level = if (isBaseLevelUp) currentState.settlement.level + 1 else currentState.settlement.level
            )
            currentState = currentState.copy(
                characters = updatedCharacters,
                settlement = updatedSettlement
            )
            summaryLogs.add("⭐ Начислено +${rewards.xp} XP жителям и базе.")
        }

        // 4. Settlement Reputation
        if (rewards.reputationDelta != 0) {
            val (repState, _) = ReputationManager.changeSettlementReputation(
                state = currentState,
                delta = rewards.reputationDelta,
                sourceTitle = "Задание «${definition.titleRu}»",
                reason = "Успешное выполнение поручения",
                type = ReputationChangeType.QUEST_COMPLETED
            )
            currentState = repState
            summaryLogs.add("🎖️ Репутация базы: ${if (rewards.reputationDelta > 0) "+" else ""}${rewards.reputationDelta}")
        }

        // 5. Faction Relations
        rewards.factionRelationDeltas.forEach { (fId, delta) ->
            if (delta != 0) {
                val (factionState, _) = ReputationManager.changeFactionRelation(
                    state = currentState,
                    factionId = fId,
                    delta = delta,
                    sourceTitle = "Задание «${definition.titleRu}»",
                    reason = "Выполнение задания «${definition.titleRu}»"
                )
                currentState = factionState
                val fName = com.example.data.ReputationBalanceConfig.getFaction(fId)?.nameRu ?: fId
                summaryLogs.add("🤝 Отношения с «$fName»: ${if (delta > 0) "+" else ""}$delta")
            }
        }

        // 6. World Flags
        if (rewards.worldFlags.isNotEmpty()) {
            currentState = currentState.copy(
                worldFlags = currentState.worldFlags + rewards.worldFlags
            )
        }

        // 7. Unlock Locations
        if (rewards.unlockLocationIds.isNotEmpty()) {
            val updatedLocations = currentState.locations.map { loc ->
                if (loc.id in rewards.unlockLocationIds) loc.copy(isUnlocked = true, status = LocationStatus.AVAILABLE) else loc
            }
            currentState = currentState.copy(locations = updatedLocations)
            summaryLogs.add("🗺️ Открыты новые территории на карте!")
        }

        // 8. Unlock Technologies
        if (rewards.unlockTechIds.isNotEmpty()) {
            val updatedTechs = currentState.technologies.map { tech ->
                if (tech.id in rewards.unlockTechIds) tech.copy(isResearched = true) else tech
            }
            currentState = currentState.copy(technologies = updatedTechs)
            summaryLogs.add("💡 Открыты новые технологии!")
        }

        // 9. Update QuestState
        val finalizedQuestState = questState.copy(
            status = QuestStatus.COMPLETED,
            completedGameDateTime = currentState.gameDateTime,
            lastCompletedDay = currentState.day,
            appliedRewardIds = questState.appliedRewardIds + rewardId
        )

        // 10. Update QuestStates map in GameState and unlock next quests
        val nextQuestIdsToUnlock = definition.nextQuestIds + rewards.unlockNextQuestIds
        val currentQuestStates = currentState.questStates.toMutableMap()
        currentQuestStates[questState.questId] = finalizedQuestState

        nextQuestIdsToUnlock.forEach { nextId ->
            val existing = currentQuestStates[nextId]
            if (existing == null || existing.status == QuestStatus.LOCKED) {
                val nextDef = QuestCatalog.get(nextId)
                if (nextDef != null) {
                    val reqResult = QuestRequirementEvaluator.evaluate(nextDef, currentState)
                    if (reqResult.isMet) {
                        currentQuestStates[nextId] = QuestState(
                            questId = nextId,
                            status = if (nextDef.autoAccept) QuestStatus.ACTIVE else QuestStatus.AVAILABLE,
                            acceptedGameDateTime = if (nextDef.autoAccept) currentState.gameDateTime else null,
                            objectiveProgress = nextDef.objectives.associate {
                                it.id to com.example.domain.model.quest.QuestObjectiveProgress(
                                    objectiveId = it.id,
                                    targetAmount = it.requiredAmount,
                                    status = com.example.domain.model.quest.ObjectiveStatus.IN_PROGRESS
                                )
                            }
                        )
                        summaryLogs.add("📜 Доступно новое задание: «${nextDef.titleRu}»!")
                    }
                }
            }
        }

        currentState = currentState.copy(
            questStates = currentQuestStates,
            dayLogs = summaryLogs.take(2) + currentState.dayLogs.take(18)
        )

        return QuestRewardResult(
            updatedGameState = currentState,
            updatedQuestState = finalizedQuestState,
            rewardSummaryLogs = summaryLogs,
            wasAlreadyApplied = false
        )
    }
}
