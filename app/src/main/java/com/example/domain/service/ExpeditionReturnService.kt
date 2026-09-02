package com.example.domain.service

import com.example.data.TravelCalculator
import com.example.domain.model.*
import kotlin.math.max

/**
 * Service orchestrating the complete expedition return cycle:
 * 1. Return readiness validation (checks for combat/event blockers).
 * 2. Return journey initiation using existing TravelState.
 * 3. Atomic, idempotent completion and safe cargo unloading with storage overflow protection.
 * 4. Staging area unload of pending cargo once warehouse space is made available.
 */
object ExpeditionReturnService {

    /**
     * Validates whether the active expedition can safely initiate return back to the settlement.
     */
    fun validateReturnReadiness(gameState: GameState): ReturnValidationResult {
        val exp = gameState.activeExpedition
        if (exp == null) {
            // Also check if we have an active travel returning to base
            val activeTravel = gameState.activeTravel
            if (activeTravel != null && activeTravel.isReturning) {
                return ReturnValidationResult(isReady = true)
            }
            return ReturnValidationResult(
                isReady = false,
                failureReason = ReturnFailureReason.NO_ACTIVE_EXPEDITION,
                message = "Нет активной экспедиции для возвращения."
            )
        }

        // 1. Check for active combat
        if (gameState.activeCombat != null && !gameState.activeCombat.isEnded) {
            return ReturnValidationResult(
                isReady = false,
                failureReason = ReturnFailureReason.COMBAT_IN_PROGRESS,
                message = "Невозможно начать возвращение во время активного боя! Завершите бой или отступите."
            )
        }

        // 2. Check if already in settlement
        val currentLoc = gameState.locations.find { it.id == gameState.currentLocationId }
        if (currentLoc?.isPlayerBase == true && (gameState.activeTravel == null || gameState.activeTravel.status == TravelStatus.ARRIVED)) {
            if (exp.phase == ExpeditionPhase.COMPLETED || exp.phase == ExpeditionPhase.PREPARING) {
                return ReturnValidationResult(
                    isReady = false,
                    failureReason = ReturnFailureReason.ALREADY_IN_SETTLEMENT,
                    message = "Отряд уже находится в поселении."
                )
            }
        }

        val warnings = mutableListOf<String>()
        if (exp.gatheringSuppliesRemaining(gameState) > 0) {
            warnings.add("В секторе ещё остались неисследованные зоны.")
        }
        if (exp.cargoCapacityKg - exp.cargoWeightKg < 2f) {
            warnings.add("Трюм экспедиции почти заполнен.")
        }

        return ReturnValidationResult(
            isReady = true,
            warnings = warnings
        )
    }

    private fun Expedition.gatheringSuppliesRemaining(gameState: GameState): Int {
        val loc = gameState.locations.find { it.id == location.id } ?: return 0
        return loc.localAreas.count { !it.isExplored }
    }

    /**
     * Initiates the return journey using existing TravelState and TravelCalculator.
     */
    fun startReturnJourney(
        gameState: GameState,
        mode: TravelTransportMode? = null
    ): Pair<GameState, TravelTransactionResult> {
        val validation = validateReturnReadiness(gameState)
        if (!validation.isReady) {
            return Pair(
                gameState,
                TravelTransactionResult.Failure(
                    reason = TravelFailureReason.ALREADY_TRAVELING,
                    message = validation.message
                )
            )
        }

        val baseLoc = gameState.locations.find { it.isPlayerBase }
            ?: return Pair(
                gameState,
                TravelTransactionResult.Failure(
                    reason = TravelFailureReason.LOCATION_LOCKED,
                    message = "Базовый аванпост не найден в списке локаций."
                )
            )

        val exp = gameState.activeExpedition
        val currentLoc = gameState.locations.find { it.id == gameState.currentLocationId } ?: exp?.location ?: baseLoc
        val effectiveMode = mode ?: exp?.travelMode ?: gameState.activeTravel?.transportMode ?: TravelTransportMode.FOOT
        val participantIds = exp?.squad?.map { it.id }
            ?: gameState.activeTravel?.participantIds
            ?: gameState.selectedSquadIds.toList()
        val vehicleId = exp?.vehicle?.id ?: gameState.activeTravel?.vehicleId

        val targetVehicle = if (vehicleId != null) {
            gameState.vehicles.find { it.id == vehicleId }
        } else null

        val cost = TravelCalculator.calculateTravelCost(
            destination = baseLoc,
            transportMode = effectiveMode,
            participantCount = participantIds.size,
            technologies = gameState.technologies,
            origin = currentLoc,
            vehicle = targetVehicle
        )

        val travelId = "return_${System.currentTimeMillis()}"
        val vehicleName = targetVehicle?.name ?: effectiveMode.titleRu

        val returnTravelState = TravelState(
            id = travelId,
            fromLocationId = currentLoc.id,
            toLocationId = baseLoc.id,
            transportMode = effectiveMode,
            vehicleId = targetVehicle?.id,
            vehicleName = vehicleName,
            participantIds = participantIds,
            leaderId = exp?.leaderId ?: participantIds.firstOrNull(),
            cargoCapacityKg = exp?.cargoCapacityKg ?: targetVehicle?.capacityKg ?: 25,
            distanceKm = cost.distanceKm,
            traveledKm = 0f,
            progressFraction = 0f,
            status = TravelStatus.RETURNING,
            isReturning = true,
            startTimestamp = System.currentTimeMillis(),
            startDateTime = gameState.gameDateTime,
            estimatedHours = cost.estimatedDurationHours,
            costPaid = cost,
            statusMessage = "Отряд возвращается на базу «Фронтир» ($vehicleName).",
            currentSectorName = baseLoc.sectorCode,
            travelLogs = listOf("Полевой лагерь свёрнут. Отряд начал обратный переход на базу.")
        )

        val updatedExp = exp?.copy(
            phase = ExpeditionPhase.RETURNING,
            status = ExpeditionStatus.RETURNING,
            travelId = travelId,
            logs = exp.logs + "Приказ командира: Возвращение на аванпост. Дистанция: ${cost.distanceKm} км."
        )

        val updatedVehicles = if (targetVehicle != null) {
            gameState.vehicles.map { veh ->
                if (veh.id == targetVehicle.id) veh.copy(status = VehicleStatus.IN_USE) else veh
            }
        } else gameState.vehicles

        val newLog = "🗺️ Экспедиция: Отряд покинул «${currentLoc.name}» и выдвинулся обратно на базу."
        val updatedState = gameState.copy(
            activeTravel = returnTravelState,
            activeExpedition = updatedExp,
            vehicles = updatedVehicles,
            dayLogs = listOf(newLog) + gameState.dayLogs.take(19)
        )

        val successResult = TravelTransactionResult.Success(
            travelState = returnTravelState,
            cost = cost,
            updatedResources = gameState.resources,
            updatedVehicles = updatedVehicles
        )

        return Pair(updatedState, successResult)
    }

    /**
     * Atomically and idempotently concludes the expedition, safely transfers loot into the settlement warehouse,
     * routes storage overflows to PendingSettlementUnload, releases squad members and vehicles, awards XP,
     * updates reputation and quests, and generates a rich ExpeditionReturnSummary.
     */
    fun completeExpeditionReturn(gameState: GameState): Pair<GameState, ExpeditionReturnSummary?> {
        val exp = gameState.activeExpedition
        if (exp == null) {
            // Check if travel was returning and arrived
            if (gameState.activeTravel != null && gameState.activeTravel.isReturning) {
                // Return travel without expedition draft: reset travel cleanly
                val usedVehId = gameState.activeTravel.vehicleId
                val updatedChars = gameState.characters.map { c ->
                    if (gameState.activeTravel.participantIds.contains(c.id)) {
                        c.copy(status = CharacterStatus.READY)
                    } else c
                }
                val updatedVehicles = gameState.vehicles.map { v ->
                    if (v.id == usedVehId) {
                        v.copy(
                            status = VehicleStatus.AVAILABLE,
                            tripsCompleted = v.tripsCompleted + 1,
                            totalDistanceTraveledKm = v.totalDistanceTraveledKm + gameState.activeTravel.distanceKm
                        )
                    } else v
                }
                val cleanedState = gameState.copy(
                    activeTravel = null,
                    currentLocationId = "loc_base",
                    characters = updatedChars,
                    vehicles = updatedVehicles,
                    dayLogs = listOf("Группа благополучно вернулась в поселение.") + gameState.dayLogs.take(19)
                )
                return Pair(cleanedState, null)
            }
            return Pair(gameState, null)
        }

        // 1. Calculate Warehouse capacity and Safe Cargo Unload
        val warehouseCap = gameState.resources.warehouseMaxCapacity
        val currentOccupied = gameState.totalWarehouseOccupiedVolume
        var availableVolume = (warehouseCap - currentOccupied).coerceAtLeast(0)

        val gathered = exp.gatheredLoot
        val moneyLoot = gathered.money // Money has 0 volume

        // Bulk resources to unload
        val resTypes = listOf(
            ResourceType.MATERIALS to gathered.materials,
            ResourceType.FOOD to gathered.food,
            ResourceType.WATER to gathered.water,
            ResourceType.FUEL to gathered.fuel,
            ResourceType.MEDICINE to (gathered.extraResources[ResourceType.MEDICINE] ?: 0),
            ResourceType.AMMO to (gathered.extraResources[ResourceType.AMMO] ?: 0),
            ResourceType.COMPONENTS to (gathered.extraResources[ResourceType.COMPONENTS] ?: 0),
            ResourceType.RARE_ALLOY to (gathered.extraResources[ResourceType.RARE_ALLOY] ?: 0)
        )

        val unloadedResMap = mutableMapOf<ResourceType, Int>()
        val overflowResMap = mutableMapOf<ResourceType, Int>()

        for ((type, amount) in resTypes) {
            if (amount <= 0) continue
            val unitVol = type.unitSize
            val neededVol = amount * unitVol
            if (neededVol <= availableVolume) {
                unloadedResMap[type] = amount
                availableVolume -= neededVol
            } else {
                val fittingUnits = (availableVolume / unitVol)
                if (fittingUnits > 0) {
                    unloadedResMap[type] = fittingUnits
                    availableVolume -= fittingUnits * unitVol
                }
                val overflow = amount - fittingUnits
                if (overflow > 0) {
                    overflowResMap[type] = overflow
                }
            }
        }

        val unloadedResources = GameResources(
            money = moneyLoot,
            materials = unloadedResMap[ResourceType.MATERIALS] ?: 0,
            food = unloadedResMap[ResourceType.FOOD] ?: 0,
            water = unloadedResMap[ResourceType.WATER] ?: 0,
            fuel = unloadedResMap[ResourceType.FUEL] ?: 0,
            warehouseMaxCapacity = warehouseCap,
            extraResources = unloadedResMap.filterKeys {
                it !in listOf(ResourceType.MATERIALS, ResourceType.FOOD, ResourceType.WATER, ResourceType.FUEL)
            }
        )

        val overflowResources = GameResources(
            money = 0,
            materials = overflowResMap[ResourceType.MATERIALS] ?: 0,
            food = overflowResMap[ResourceType.FOOD] ?: 0,
            water = overflowResMap[ResourceType.WATER] ?: 0,
            fuel = overflowResMap[ResourceType.FUEL] ?: 0,
            warehouseMaxCapacity = warehouseCap,
            extraResources = overflowResMap.filterKeys {
                it !in listOf(ResourceType.MATERIALS, ResourceType.FOOD, ResourceType.WATER, ResourceType.FUEL)
            }
        )

        // 2. Process Discrete Item Rewards
        val gatheredItems = resolveItemsFromIds(exp.lootItemIds, gameState.inventoryItems)
        val unloadedItems = mutableListOf<WarehouseItem>()
        val overflowItems = mutableListOf<WarehouseItem>()

        for (item in gatheredItems) {
            val itemVol = item.quantity * item.unitSize
            if (itemVol <= availableVolume) {
                unloadedItems.add(item)
                availableVolume -= itemVol
            } else {
                overflowItems.add(item)
            }
        }

        // Apply unloaded resources to settlement
        val newResources = gameState.resources.add(unloadedResources)

        // Merge unloaded items with existing inventoryItems
        val updatedInventory = mergeInventoryItems(gameState.inventoryItems, unloadedItems)

        // Update pending settlement unload (merge with existing pending if any)
        val existingPending = gameState.pendingSettlementUnload
        val updatedPending = PendingSettlementUnload(
            resources = existingPending.resources.add(overflowResources),
            items = mergeInventoryItems(existingPending.items, overflowItems),
            sourceLocationName = exp.location.name,
            dayArrived = gameState.day
        )

        // 3. Squad member lifecycle, XP, Morale & Health resolution
        val totalXp = exp.xpReward + (exp.location.dangerLevel.rating * 15)
        val squadOutcomes = mutableListOf<SquadMemberReturnOutcome>()
        val squadIds = exp.squad.map { it.id }.toSet()

        val updatedCharacters = gameState.characters.map { char ->
            if (squadIds.contains(char.id)) {
                val expFighter = exp.squad.find { it.id == char.id } ?: char
                val baseHp = expFighter.health
                val isInjured = baseHp < (char.maxHealth * 0.25f).toInt() || baseHp <= 15
                val finalStatus = if (isInjured) CharacterStatus.INJURED else CharacterStatus.READY

                val progResult = CharacterProgressionService.addExperience(char, totalXp)
                val leveledChar = progResult.updatedCharacter

                val moraleDelta = if (isInjured) -15 else 10
                val finalMorale = (leveledChar.morale + moraleDelta).coerceIn(0, 100)

                val finalChar = leveledChar.copy(
                    health = baseHp.coerceIn(1, char.maxHealth),
                    status = finalStatus,
                    morale = finalMorale,
                    expeditionsCount = char.expeditionsCount + 1
                )

                squadOutcomes.add(
                    SquadMemberReturnOutcome(
                        characterId = char.id,
                        characterName = char.name,
                        role = char.role,
                        avatarTag = char.avatarTag,
                        xpGained = totalXp,
                        oldLevel = char.level,
                        newLevel = finalChar.level,
                        leveledUp = progResult.leveledUp,
                        skillPointsGained = progResult.gainedSkillPoints,
                        oldHealth = char.health,
                        finalHealth = finalChar.health,
                        maxHealth = finalChar.maxHealth,
                        isInjured = isInjured,
                        moraleDelta = moraleDelta,
                        finalMorale = finalMorale
                    )
                )
                finalChar
            } else char
        }

        // 4. Vehicle lifecycle
        val vehId = exp.vehicle.id
        val updatedVehicles = gameState.vehicles.map { v ->
            if (v.id == vehId) {
                val wear = if (v.isMotorized) 5 else 2
                val newDurability = (v.durabilityPercent - wear).coerceIn(0, 100)
                v.copy(
                    status = if (newDurability <= 20) VehicleStatus.DAMAGED else VehicleStatus.AVAILABLE,
                    durabilityPercent = newDurability,
                    tripsCompleted = v.tripsCompleted + 1,
                    totalDistanceTraveledKm = v.totalDistanceTraveledKm + (exp.location.distanceKm * 2)
                )
            } else v
        }

        // 5. Quests and Settlement Reputation
        val reputationGained = 15 + (exp.location.dangerLevel.rating * 10)
        val completedQuests = mutableListOf<String>()

        val updatedQuests = gameState.quests.map { q ->
            if (q.id == "quest_1" && q.status == QuestStatus.IN_PROGRESS) {
                completedQuests.add(q.title)
                q.copy(progress = 1, status = QuestStatus.READY_TO_CLAIM)
            } else q
        }

        val (stateAfterReputation, repEntry) = com.example.domain.service.reputation.ReputationManager.changeSettlementReputation(
            state = gameState,
            delta = reputationGained,
            sourceTitle = "Экспедиция в «${exp.location.name}»",
            reason = "Успешное возвращение отряда и доставка ценных припасов",
            type = com.example.domain.model.reputation.ReputationChangeType.EXPEDITION_SUCCESS
        )
        val updatedSettlement = stateAfterReputation.settlement

        // 6. Update Location stats
        val updatedLocations = gameState.locations.map { loc ->
            if (loc.id == exp.location.id) {
                loc.copy(
                    status = LocationStatus.EXPLORED,
                    timesExplored = loc.timesExplored + 1,
                    visitCount = loc.visitCount + 1,
                    lastVisitedDay = gameState.day
                )
            } else loc
        }

        // 7. Summary Logs & Summary Object
        val summaryLogs = mutableListOf<String>()
        summaryLogs.add("Отряд успешно завершил экспедицию в «${exp.location.name}».")
        summaryLogs.add("Выгружено на склад: +${unloadedResources.money} Кр, +${unloadedResources.materials} Мат, +${unloadedResources.food} Еды, +${unloadedResources.water} Воды, +${unloadedResources.fuel} Топл.")
        if (overflowResources.totalStoredVolume > 0 || overflowItems.isNotEmpty()) {
            summaryLogs.add("⚠️ Внимание: Склад переполнен! Часть добычи временно оставлена в зоне разгрузки у ворот.")
        }
        if (squadOutcomes.any { it.leveledUp }) {
            val leveledNames = squadOutcomes.filter { it.leveledUp }.joinToString { "${it.characterName} (Ур. ${it.newLevel})" }
            summaryLogs.add("🌟 Повышение уровня бойцов: $leveledNames!")
        }
        if (squadOutcomes.any { it.isInjured }) {
            val injuredNames = squadOutcomes.filter { it.isInjured }.joinToString { it.characterName }
            summaryLogs.add("🏥 Ранены и направлены в лазарет: $injuredNames.")
        }

        val totalDuration = exp.startDateTime.durationUntil(gameState.gameDateTime)
        val elapsedDays = max(1, (gameState.gameDateTime.day - exp.startDateTime.day + 1))

        val summary = ExpeditionReturnSummary(
            expeditionId = exp.id,
            locationName = exp.location.name,
            locationDistanceKm = exp.location.distanceKm,
            vehicleName = exp.vehicle.name,
            travelMode = exp.travelMode,
            daysElapsed = elapsedDays,
            startDateTime = exp.startDateTime,
            endDateTime = gameState.gameDateTime,
            travelDuration = if (!totalDuration.isZero) totalDuration else GameDuration.ofHours(1),
            gatheredResources = gathered,
            unloadedResources = unloadedResources,
            overflowResources = overflowResources,
            gatheredItems = gatheredItems,
            unloadedItems = unloadedItems,
            overflowItems = overflowItems,
            squadOutcomes = squadOutcomes,
            totalXpAwarded = totalXp,
            reputationGained = reputationGained,
            completedQuests = completedQuests,
            discoveredFlags = emptyList(),
            summaryLogs = summaryLogs,
            timestamp = System.currentTimeMillis()
        )

        val mainReturnLog = "🚩 ВОЗВРАЩЕНИЕ: Экспедиция из «${exp.location.name}» вернулась на базу! Получено +$reputationGained Репутации, +$totalXp XP."

        val stateBeforeQuests = stateAfterReputation.copy(
            resources = newResources,
            inventoryItems = updatedInventory,
            pendingSettlementUnload = updatedPending,
            characters = updatedCharacters,
            vehicles = updatedVehicles,
            locations = updatedLocations,
            settlement = updatedSettlement,
            quests = updatedQuests,
            activeExpedition = null,
            activeTravel = null,
            activeCombat = null,
            currentLocationId = "loc_base",
            lastReturnSummary = summary,
            dayLogs = listOf(mainReturnLog) + summaryLogs + stateAfterReputation.dayLogs.take(15)
        )

        // Process domain quest events
        var stateWithQuests = com.example.domain.service.quest.QuestProgressProcessor.process(
            com.example.domain.model.quest.GameEvent.LocationVisited(exp.location.id),
            stateBeforeQuests
        )
        stateWithQuests = com.example.domain.service.quest.QuestProgressProcessor.process(
            com.example.domain.model.quest.GameEvent.LocationExplored(exp.location.id, exp.location.explorationProgressPercent),
            stateWithQuests
        )
        stateWithQuests = com.example.domain.service.quest.QuestProgressProcessor.process(
            com.example.domain.model.quest.GameEvent.ExpeditionReturned(exp.location.id, gathered, gatheredItems.map { it.id }),
            stateWithQuests
        )
        val finalizedState = com.example.domain.service.quest.QuestManager.syncQuestsOnStateChange(stateWithQuests)

        return Pair(finalizedState, summary)
    }

    /**
     * Unloads pending excess cargo from the staging area into the warehouse
     * after the warehouse has been expanded or supplies consumed.
     */
    fun unloadPendingSettlementCargo(gameState: GameState): Pair<GameState, ResourceOperationResult> {
        val pending = gameState.pendingSettlementUnload
        if (!pending.hasPendingCargo) {
            return Pair(
                gameState,
                ResourceOperationResult.Failure(
                    reason = ResourceOperationResult.FailureReason.INVALID_AMOUNT,
                    message = "Временная зона разгрузки пуста."
                )
            )
        }

        val warehouseCap = gameState.resources.warehouseMaxCapacity
        val currentOccupied = gameState.totalWarehouseOccupiedVolume
        var availableVolume = (warehouseCap - currentOccupied).coerceAtLeast(0)

        if (availableVolume <= 0) {
            return Pair(
                gameState,
                ResourceOperationResult.Failure(
                    reason = ResourceOperationResult.FailureReason.STORAGE_FULL,
                    message = "На складе нет свободного места (${currentOccupied}/$warehouseCap). Расширьте склад!"
                )
            )
        }

        val gathered = pending.resources
        val resTypes = listOf(
            ResourceType.MATERIALS to gathered.materials,
            ResourceType.FOOD to gathered.food,
            ResourceType.WATER to gathered.water,
            ResourceType.FUEL to gathered.fuel,
            ResourceType.MEDICINE to (gathered.extraResources[ResourceType.MEDICINE] ?: 0),
            ResourceType.AMMO to (gathered.extraResources[ResourceType.AMMO] ?: 0),
            ResourceType.COMPONENTS to (gathered.extraResources[ResourceType.COMPONENTS] ?: 0),
            ResourceType.RARE_ALLOY to (gathered.extraResources[ResourceType.RARE_ALLOY] ?: 0)
        )

        val unloadedResMap = mutableMapOf<ResourceType, Int>()
        val remainingResMap = mutableMapOf<ResourceType, Int>()

        for ((type, amount) in resTypes) {
            if (amount <= 0) continue
            val unitVol = type.unitSize
            val neededVol = amount * unitVol
            if (neededVol <= availableVolume) {
                unloadedResMap[type] = amount
                availableVolume -= neededVol
            } else {
                val fittingUnits = availableVolume / unitVol
                if (fittingUnits > 0) {
                    unloadedResMap[type] = fittingUnits
                    availableVolume -= fittingUnits * unitVol
                }
                val overflow = amount - fittingUnits
                if (overflow > 0) {
                    remainingResMap[type] = overflow
                }
            }
        }

        val unloadedResources = GameResources(
            money = gathered.money,
            materials = unloadedResMap[ResourceType.MATERIALS] ?: 0,
            food = unloadedResMap[ResourceType.FOOD] ?: 0,
            water = unloadedResMap[ResourceType.WATER] ?: 0,
            fuel = unloadedResMap[ResourceType.FUEL] ?: 0,
            warehouseMaxCapacity = warehouseCap,
            extraResources = unloadedResMap.filterKeys {
                it !in listOf(ResourceType.MATERIALS, ResourceType.FOOD, ResourceType.WATER, ResourceType.FUEL)
            }
        )

        val remainingResources = GameResources(
            money = 0,
            materials = remainingResMap[ResourceType.MATERIALS] ?: 0,
            food = remainingResMap[ResourceType.FOOD] ?: 0,
            water = remainingResMap[ResourceType.WATER] ?: 0,
            fuel = remainingResMap[ResourceType.FUEL] ?: 0,
            warehouseMaxCapacity = warehouseCap,
            extraResources = remainingResMap.filterKeys {
                it !in listOf(ResourceType.MATERIALS, ResourceType.FOOD, ResourceType.WATER, ResourceType.FUEL)
            }
        )

        val unloadedItems = mutableListOf<WarehouseItem>()
        val remainingItems = mutableListOf<WarehouseItem>()

        for (item in pending.items) {
            val itemVol = item.quantity * item.unitSize
            if (itemVol <= availableVolume) {
                unloadedItems.add(item)
                availableVolume -= itemVol
            } else {
                remainingItems.add(item)
            }
        }

        val newResources = gameState.resources.add(unloadedResources)
        val newInventory = mergeInventoryItems(gameState.inventoryItems, unloadedItems)

        val updatedPending = PendingSettlementUnload(
            resources = remainingResources,
            items = remainingItems,
            sourceLocationName = pending.sourceLocationName,
            dayArrived = pending.dayArrived
        )

        val logMsg = "📦 Разгрузка: Трофеи перемещены на склад (+${unloadedResources.totalStoredVolume} ед. припасов, +${unloadedItems.size} предметов)."
        val nextState = gameState.copy(
            resources = newResources,
            inventoryItems = newInventory,
            pendingSettlementUnload = updatedPending,
            dayLogs = listOf(logMsg) + gameState.dayLogs.take(19)
        )

        return Pair(
            nextState,
            ResourceOperationResult.Success(message = logMsg)
        )
    }

    private fun resolveItemsFromIds(itemIds: List<String>, existingInventory: List<WarehouseItem>): List<WarehouseItem> {
        if (itemIds.isEmpty()) return emptyList()
        val inventoryMap = existingInventory.associateBy { it.id }
        return itemIds.mapNotNull { id ->
            inventoryMap[id] ?: createFallbackItem(id)
        }
    }

    private fun createFallbackItem(id: String): WarehouseItem {
        return WarehouseItem(
            id = id,
            name = when {
                id.contains("med") -> "Армейский медпакет"
                id.contains("ammo") -> "Патроны 7.62"
                id.contains("part") || id.contains("scrap") -> "Электронные платы"
                else -> "Трофейный контейнер"
            },
            category = ItemCategory.ELECTRONICS_AND_PARTS,
            quantity = 1,
            unitSize = 1,
            rarity = ItemRarity.UNCOMMON,
            description = "Ценные довоенные компоненты, обнаруженные в экспедиции.",
            baseValueCredits = 25
        )
    }

    private fun mergeInventoryItems(base: List<WarehouseItem>, added: List<WarehouseItem>): List<WarehouseItem> {
        if (added.isEmpty()) return base
        val map = base.associateBy { it.id }.toMutableMap()
        for (item in added) {
            val existing = map[item.id]
            if (existing != null) {
                map[item.id] = existing.copy(quantity = existing.quantity + item.quantity)
            } else {
                map[item.id] = item
            }
        }
        return map.values.toList()
    }
}
