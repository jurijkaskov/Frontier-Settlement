package com.example.domain.service.events

import com.example.domain.model.*
import kotlin.math.roundToInt

/**
 * Result bundle after resolving an event outcome.
 */
data class EventResolutionResult(
    val updatedGameState: GameState,
    val updatedExpedition: Expedition,
    val activeEventState: ActiveEventState,
    val summaryLogs: List<String>,
    val requiresCombat: Boolean = false,
    val chainEventId: String? = null
)

/**
 * Centralized service to execute and apply event consequences safely, deterministically,
 * and with strict inventory / cargo capacity enforcement.
 */
object EventOutcomeResolver {

    /**
     * Estimates physical weight of a bundle of resources in kilograms.
     */
    fun calculateResourceWeightKg(resources: Map<ResourceType, Int>): Float {
        var totalKg = 0f
        resources.forEach { (type, amount) ->
            val unitKg = when (type) {
                ResourceType.MONEY -> 0.0f
                ResourceType.FOOD -> 0.5f
                ResourceType.WATER -> 1.0f
                ResourceType.FUEL -> 0.8f
                ResourceType.MATERIALS -> 1.0f
                ResourceType.MEDICINE -> 0.2f
                ResourceType.AMMO -> 0.1f
                ResourceType.COMPONENTS -> 0.5f
                ResourceType.RARE_ALLOY -> 2.0f
            }
            totalKg += (amount * unitKg)
        }
        return totalKg
    }

    /**
     * Resolves an event choice and generates the updated GameState and Expedition state.
     */
    fun resolve(
        event: ExpeditionEvent,
        choice: EventChoice,
        gameState: GameState,
        expedition: Expedition,
        actorId: String? = null,
        seed: Long = System.currentTimeMillis()
    ): Pair<GameState, EventResolutionResult> {
        val activeState = expedition.activeEventState ?: ActiveEventState(
            eventId = event.id,
            event = event,
            instanceSeed = seed
        )
        val result = resolve(
            gameState = gameState,
            expedition = expedition,
            activeState = activeState,
            choiceId = choice.id,
            selectedActorId = actorId,
            seed = seed
        )
        return Pair(result.updatedGameState, result)
    }

    /**
     * Resolves an event choice and generates the updated GameState and Expedition state.
     */
    fun resolve(
        gameState: GameState,
        expedition: Expedition,
        activeState: ActiveEventState,
        choiceId: String,
        selectedActorId: String? = null,
        seed: Long = activeState.instanceSeed
    ): EventResolutionResult {
        val choice = activeState.event.choices.find { it.id == choiceId }
            ?: return EventResolutionResult(
                updatedGameState = gameState,
                updatedExpedition = expedition,
                activeEventState = activeState,
                summaryLogs = listOf("Ошибка: Вариант действия не найден.")
            )

        // 1. Resolve Skill Check if required
        val actor = if (selectedActorId != null) {
            expedition.squad.find { it.id == selectedActorId } ?: expedition.leader ?: expedition.squad.first()
        } else {
            expedition.leader ?: expedition.squad.first()
        }

        val skillCheckResult: SkillCheckResult? = choice.skillCheck?.let { req ->
            SkillCheckResolver.resolveCheck(
                actor = actor,
                requirement = req,
                inventoryItems = gameState.inventoryItems,
                seed = seed
            )
        }

        val outcome = if (skillCheckResult != null) {
            if (skillCheckResult.isSuccess) choice.successOutcome else (choice.failureOutcome ?: choice.successOutcome)
        } else {
            choice.successOutcome
        }

        // 2. Consume Resources / Supplies from Expedition
        val updatedSupplies = expedition.supplies.toMutableMap()
        choice.costResources.forEach { (resType, cost) ->
            val current = updatedSupplies[resType] ?: 0
            updatedSupplies[resType] = (current - cost).coerceAtLeast(0)
        }

        // 3. Consume Items
        val updatedCarriedItems = expedition.carriedItemIds.toMutableList()
        choice.consumedItemIds.forEach { itemId ->
            updatedCarriedItems.remove(itemId)
        }

        // 4. Calculate Loot & Cargo Capacity
        val freeCapacityKg = (expedition.cargoCapacityKg - expedition.cargoWeightKg).coerceAtLeast(0f)
        val awardedResources = mutableMapOf<ResourceType, Int>()
        val droppedResources = mutableMapOf<ResourceType, Int>()
        var addedWeightKg = 0f

        outcome.resourceRewards.forEach { (type, amount) ->
            if (amount > 0) {
                val unitKg = when (type) {
                    ResourceType.MONEY -> 0.0f
                    ResourceType.FOOD -> 0.5f
                    ResourceType.WATER -> 1.0f
                    ResourceType.FUEL -> 0.8f
                    ResourceType.MATERIALS -> 1.0f
                    ResourceType.MEDICINE -> 0.2f
                    ResourceType.AMMO -> 0.1f
                    ResourceType.COMPONENTS -> 0.5f
                    ResourceType.RARE_ALLOY -> 2.0f
                }
                val totalItemKg = amount * unitKg

                if (unitKg == 0.0f || (addedWeightKg + totalItemKg) <= freeCapacityKg) {
                    awardedResources[type] = amount
                    addedWeightKg += totalItemKg
                } else {
                    // Partial capacity calculation
                    val spaceLeft = (freeCapacityKg - addedWeightKg).coerceAtLeast(0f)
                    val fittingAmount = (spaceLeft / unitKg).toInt()
                    if (fittingAmount > 0) {
                        awardedResources[type] = fittingAmount
                        addedWeightKg += fittingAmount * unitKg
                    }
                    val dropped = amount - fittingAmount
                    if (dropped > 0) {
                        droppedResources[type] = dropped
                    }
                }
            }
        }

        val updatedGatheredLoot = expedition.gatheredLoot.copy(
            money = expedition.gatheredLoot.money + (awardedResources[ResourceType.MONEY] ?: 0),
            food = expedition.gatheredLoot.food + (awardedResources[ResourceType.FOOD] ?: 0),
            water = expedition.gatheredLoot.water + (awardedResources[ResourceType.WATER] ?: 0),
            fuel = expedition.gatheredLoot.fuel + (awardedResources[ResourceType.FUEL] ?: 0),
            materials = expedition.gatheredLoot.materials + (awardedResources[ResourceType.MATERIALS] ?: 0),
            extraResources = (expedition.gatheredLoot.extraResources.toMutableMap()).apply {
                awardedResources.forEach { (k, v) ->
                    if (k !in listOf(ResourceType.MONEY, ResourceType.FOOD, ResourceType.WATER, ResourceType.FUEL, ResourceType.MATERIALS)) {
                        this[k] = (this[k] ?: 0) + v
                    }
                }
            }
        )

        val updatedLootItemIds = expedition.lootItemIds + outcome.itemRewards
        val newCargoWeightKg = (expedition.cargoWeightKg + addedWeightKg).coerceAtMost(expedition.cargoCapacityKg.toFloat())

        // 5. Apply XP and Health to Squad Participants
        val xpGain = outcome.xpReward
        val healthDelta = outcome.healthDelta
        val updatedSquad = expedition.squad.map { member ->
            val isActor = member.id == actor.id
            val memberXp = if (isActor) xpGain else (xpGain / 2)
            val newTotalXp = member.experience + memberXp
            val isLevelUp = newTotalXp >= member.maxExperience
            val finalLevel = if (isLevelUp) member.level + 1 else member.level
            val finalMaxXp = if (isLevelUp) (member.maxExperience * 1.5).roundToInt() else member.maxExperience
            val finalExp = if (isLevelUp) newTotalXp - member.maxExperience else newTotalXp
            val finalHp = (member.health + healthDelta).coerceIn(1, member.maxHealth)
            val finalMorale = (member.morale + outcome.moraleDelta).coerceIn(0, 100)

            member.copy(
                level = finalLevel,
                experience = finalExp,
                maxExperience = finalMaxXp,
                health = finalHp,
                morale = finalMorale,
                unspentSkillPoints = if (isLevelUp) member.unspentSkillPoints + 1 else member.unspentSkillPoints
            )
        }

        // 6. Update Exploration Progress & Discovered Areas
        val updatedExplorationProgress = (expedition.explorationProgress + outcome.explorationProgressGain).coerceIn(0, 100)
        val updatedVisitedAreas = if (outcome.discoveredAreaId != null) {
            expedition.visitedAreaIds + outcome.discoveredAreaId
        } else {
            expedition.visitedAreaIds
        }

        // 7. Update World Flags & History
        val updatedWorldFlags = gameState.worldFlags + outcome.setWorldFlags
        val historyEntry = EventHistoryEntry(
            eventId = activeState.eventId,
            locationId = expedition.location.id,
            expeditionId = expedition.id,
            choiceId = choiceId,
            wasSuccess = skillCheckResult?.isSuccess ?: true,
            day = gameState.day,
            gameDateTime = gameState.gameDateTime
        )
        val updatedHistory = gameState.eventHistory + historyEntry

        // 8. Update Location Exploration Status
        val updatedLocations = gameState.locations.map { loc ->
            if (loc.id == expedition.location.id) {
                val currentAreas = loc.localAreas.map { area ->
                    if (area.id == outcome.discoveredAreaId) area.copy(isDiscovered = true, isExplored = true) else area
                }
                loc.copy(
                    explorationProgressPercent = (loc.explorationProgressPercent + outcome.explorationProgressGain).coerceAtMost(100),
                    localAreas = currentAreas
                )
            } else loc
        }

        // 9. Process Time Cost
        val totalTimeCost = choice.timeCost + outcome.timeCost
        val timeAdvanceResult = if (!totalTimeCost.isZero) {
            com.example.domain.service.time.GameClock.advance(gameState.gameDateTime, totalTimeCost)
        } else null

        // 10. Build Updated Expedition State
        val newLogs = mutableListOf<String>()
        newLogs.add("📌 Событие «${activeState.event.title}»: выбран вариант «${choice.text}».")
        if (skillCheckResult != null) {
            newLogs.add("🎲 ${skillCheckResult.explanation}")
        }
        newLogs.add(outcome.narrativeText)
        if (totalTimeCost > GameDuration.ZERO && timeAdvanceResult != null) {
            newLogs.add("⏱️ Затрачено времени: ${totalTimeCost.formatted} (теперь ${timeAdvanceResult.newTime.formattedFull}).")
        }
        if (awardedResources.isNotEmpty()) {
            val lootStr = awardedResources.entries.joinToString { "+${it.value} ${it.key.nameRu}" }
            newLogs.add("📦 Добыча в груз: $lootStr (+${String.format("%.1f", addedWeightKg)} кг)")
        }
        if (droppedResources.isNotEmpty()) {
            val dropStr = droppedResources.entries.joinToString { "${it.value} ${it.key.nameRu}" }
            newLogs.add("⚠️ Внимание! Не поместилось в багажный отсек: $dropStr (груз заполнен)")
        }
        if (xpGain > 0) {
            newLogs.add("⭐ Начислено +$xpGain XP участникам операции.")
        }
        if (outcome.customLog != null) {
            newLogs.add(outcome.customLog)
        }

        val finalizedActiveState = activeState.copy(
            selectedActorId = actor.id,
            selectedChoiceId = choiceId,
            resolvedSkillCheckResult = skillCheckResult,
            resolvedOutcome = outcome,
            isResolved = true,
            awardedResources = awardedResources,
            awardedItems = outcome.itemRewards,
            droppedResourcesDueToCapacity = droppedResources,
            overflowPending = droppedResources.isNotEmpty(),
            consumedSupplies = choice.costResources,
            consumedItems = choice.consumedItemIds
        )

        val updatedExpedition = expedition.copy(
            squad = updatedSquad,
            supplies = updatedSupplies,
            carriedItemIds = updatedCarriedItems,
            gatheredLoot = updatedGatheredLoot,
            lootItemIds = updatedLootItemIds,
            cargoWeightKg = newCargoWeightKg,
            activeEventState = finalizedActiveState,
            explorationProgress = updatedExplorationProgress,
            visitedAreaIds = updatedVisitedAreas,
            logs = expedition.logs + newLogs
        )

        // 11. Update Global GameState & Handle any crossed days and Reputation
        val stateWithReputation = com.example.domain.service.reputation.ReputationManager.applyEventReputationConsequences(
            initialState = gameState,
            reputationDelta = outcome.reputationDelta,
            factionDeltas = outcome.factionRelationDeltas,
            sourceTitle = activeState.event.title,
            narrativeReason = choice.text,
            type = com.example.domain.model.reputation.ReputationChangeType.EVENT_CHOICE
        )

        var intermediateGameState = stateWithReputation.copy(
            gameDateTime = timeAdvanceResult?.newTime ?: stateWithReputation.gameDateTime,
            activeExpedition = updatedExpedition,
            locations = updatedLocations,
            worldFlags = updatedWorldFlags,
            eventHistory = updatedHistory,
            dayLogs = newLogs.take(2) + stateWithReputation.dayLogs.take(18)
        )

        if (timeAdvanceResult != null && timeAdvanceResult.isNewDayCrossed) {
            val (dailyState, _) = com.example.domain.service.time.DailyTickProcessor.processMultipleDays(
                intermediateGameState,
                timeAdvanceResult.crossedDays
            )
            intermediateGameState = dailyState
        }

        return EventResolutionResult(
            updatedGameState = intermediateGameState,
            updatedExpedition = updatedExpedition,
            activeEventState = finalizedActiveState,
            summaryLogs = newLogs,
            requiresCombat = outcome.requiresCombat,
            chainEventId = outcome.chainEventId
        )
    }
}
