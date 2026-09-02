package com.example.data

import com.example.domain.model.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Centralized calculation and validation engine for global map travel and expeditions.
 * Provides pure, deterministic functions for resource cost, duration, route validation,
 * terrain penalties, and technological bonuses.
 */
object TravelCalculator {

    /**
     * Calculates the full travel cost and estimated duration between two locations.
     */
    fun calculateTravelCost(
        destination: Location,
        transportMode: TravelTransportMode,
        participantCount: Int = 2,
        technologies: List<ResearchTech> = emptyList(),
        origin: Location? = null,
        vehicle: Vehicle? = null
    ): TravelCost {
        val rawDistance = if (origin != null && origin.id != "loc_base") {
            destination.distanceKm.coerceAtLeast(origin.distanceKm / 2).coerceAtLeast(4)
        } else {
            destination.distanceKm.coerceAtLeast(1)
        }

        val count = max(1, participantCount)
        val squadMultiplier = 1.0f + (count - 1) * 0.35f

        // Terrain difficulty modifiers
        val terrainTimeMult = when (destination.terrainType) {
            TerrainType.WASTELAND -> 1.0f
            TerrainType.FOREST -> 1.15f
            TerrainType.HILLS -> 1.30f
            TerrainType.WATER -> 1.20f
            TerrainType.RUINS -> 1.25f
        }

        val terrainRationMult = when (destination.terrainType) {
            TerrainType.WASTELAND -> 1.0f
            TerrainType.FOREST -> 1.10f
            TerrainType.HILLS -> 1.20f
            TerrainType.WATER -> 1.05f
            TerrainType.RUINS -> 1.15f
        }

        val terrainFuelMult = when (destination.terrainType) {
            TerrainType.HILLS -> 1.20f
            TerrainType.RUINS -> 1.15f
            else -> 1.0f
        }

        // Active research tech reductions
        val fuelEfficiencyPercent = technologies
            .filter { it.isResearched }
            .flatMap { it.effects }
            .filterIsInstance<TechEffect.FuelEfficiency>()
            .sumOf { it.reductionPercent }
            .coerceIn(0, 70)

        val fuelTechMultiplier = (100f - fuelEfficiencyPercent) / 100f

        // Effective vehicle parameters or default mode parameters
        val speedKmH = vehicle?.speedKmH?.toFloat() ?: transportMode.baseSpeedKmH
        val fuelConsumptionPerKm = vehicle?.fuelConsumptionPerKm ?: transportMode.fuelCostPerKm

        // Calculate food, water & fuel cost
        val rawFood = rawDistance * transportMode.foodCostPerKm * squadMultiplier * terrainRationMult
        val rawWater = rawDistance * transportMode.waterCostPerKm * squadMultiplier * terrainRationMult
        val rawFuel = rawDistance * fuelConsumptionPerKm * terrainFuelMult * fuelTechMultiplier

        val foodCost = if (rawFood > 0f) ceil(rawFood).toInt().coerceAtLeast(1) else 0
        val waterCost = if (rawWater > 0f) ceil(rawWater).toInt().coerceAtLeast(1) else 0
        val fuelCost = if (rawFuel > 0f) ceil(rawFuel).toInt().coerceAtLeast(1) else 0

        // Duration in hours
        val effectiveSpeed = (speedKmH / terrainTimeMult).coerceAtLeast(2f)
        val estimatedHours = (rawDistance.toFloat() / effectiveSpeed).coerceAtLeast(0.2f)

        return TravelCost(
            food = foodCost,
            water = waterCost,
            fuel = fuelCost,
            money = 0,
            estimatedDurationHours = estimatedHours,
            distanceKm = rawDistance,
            rawSpeedKmH = effectiveSpeed,
            vehicleUsed = vehicle
        )
    }

    /**
     * Finds the most appropriate vehicle for the selected mode from the gameState.
     */
    fun resolveVehicle(
        transportMode: TravelTransportMode,
        gameState: GameState,
        preferredVehicleId: String? = null
    ): Vehicle? {
        if (!transportMode.requiresVehicle) return null

        if (preferredVehicleId != null) {
            val pref = gameState.vehicles.find { it.id == preferredVehicleId }
            if (pref != null) return pref
        }

        // Try to find available vehicle matching the transport mode type
        val matchingAvailable = gameState.vehicles.find {
            it.type == transportMode.vehicleType && it.isReadyForTrip
        }
        if (matchingAvailable != null) return matchingAvailable

        // Fallback to default vehicle ID or matching type
        return gameState.vehicles.find { it.id == transportMode.defaultVehicleId }
            ?: gameState.vehicles.find { it.type == transportMode.vehicleType }
    }

    /**
     * Validates whether travel can safely be initiated.
     */
    fun validateTravel(
        destination: Location,
        transportMode: TravelTransportMode,
        gameState: GameState,
        participantIds: List<String> = emptyList(),
        vehicle: Vehicle? = null,
        originLocationId: String = gameState.currentLocationId
    ): TravelValidationResult {
        // 1. Destination must be unlocked and known
        if (destination.isHiddenOrUnknown || !destination.isUnlocked) {
            return TravelValidationResult.Invalid(
                reason = TravelFailureReason.LOCATION_LOCKED,
                message = "Локация «${destination.displayName}» заблокирована или скрыта радиоактивным туманом."
            )
        }

        // 2. Cannot travel to current location
        if (destination.id == originLocationId) {
            return TravelValidationResult.Invalid(
                reason = TravelFailureReason.LOCATION_IS_CURRENT,
                message = "Группа уже находится в локации «${destination.name}»."
            )
        }

        // 3. Cannot start another travel if one is currently active
        val activeTravel = gameState.activeTravel
        if (activeTravel != null && activeTravel.isActiveTravel) {
            return TravelValidationResult.Invalid(
                reason = TravelFailureReason.ALREADY_TRAVELING,
                message = "Другой отряд уже находится в пути (${activeTravel.progressPercent}%)."
            )
        }

        // 4. Vehicle availability check
        val targetVehicle = vehicle ?: resolveVehicle(transportMode, gameState)
        if (transportMode.requiresVehicle) {
            if (targetVehicle == null || !targetVehicle.isUnlocked) {
                return TravelValidationResult.Invalid(
                    reason = TravelFailureReason.VEHICLE_UNAVAILABLE,
                    message = "Транспорт для режима «${transportMode.titleRu}» отсутствует в гараже."
                )
            }
            if (targetVehicle.status == VehicleStatus.IN_USE) {
                return TravelValidationResult.Invalid(
                    reason = TravelFailureReason.VEHICLE_UNAVAILABLE,
                    message = "Транспорт «${targetVehicle.name}» уже находится в пути."
                )
            }
            if (targetVehicle.status == VehicleStatus.DAMAGED || targetVehicle.status == VehicleStatus.MAINTENANCE) {
                return TravelValidationResult.Invalid(
                    reason = TravelFailureReason.VEHICLE_DAMAGED,
                    message = "Транспорт «${targetVehicle.name}» требует ремонта или техобслуживания."
                )
            }
            if (!targetVehicle.isAvailable) {
                return TravelValidationResult.Invalid(
                    reason = TravelFailureReason.VEHICLE_UNAVAILABLE,
                    message = "Транспорт «${targetVehicle.name}» временно недоступен."
                )
            }
        }

        // 5. Squad member count and vehicle passenger capacity
        val squadCount = if (participantIds.isNotEmpty()) {
            participantIds.size
        } else {
            gameState.squad.memberIds.size
        }

        if (squadCount == 0) {
            return TravelValidationResult.Invalid(
                reason = TravelFailureReason.NO_SQUAD_MEMBERS,
                message = "Для путешествия необходимо сформировать экспедиционный отряд (минимум 1 боец)."
            )
        }

        if (targetVehicle != null && squadCount > targetVehicle.maxPassengers) {
            return TravelValidationResult.Invalid(
                reason = TravelFailureReason.SQUAD_TOO_LARGE_FOR_VEHICLE,
                message = "Отряд ($squadCount чел.) превышает вместимость транспорта «${targetVehicle.name}» (макс. ${targetVehicle.maxPassengers} чел.)."
            )
        }

        // 6. Calculate required resources
        val originLoc = gameState.locations.find { it.id == originLocationId }
        val cost = calculateTravelCost(
            destination = destination,
            transportMode = transportMode,
            participantCount = squadCount,
            technologies = gameState.technologies,
            origin = originLoc,
            vehicle = targetVehicle
        )

        // 7. Check resource sufficiency
        if (gameState.resources.water < cost.water) {
            return TravelValidationResult.Invalid(
                reason = TravelFailureReason.INSUFFICIENT_WATER,
                message = "Недостаточно воды (требуется: ${cost.water}, на складе: ${gameState.resources.water})."
            )
        }

        if (gameState.resources.food < cost.food) {
            return TravelValidationResult.Invalid(
                reason = TravelFailureReason.INSUFFICIENT_FOOD,
                message = "Недостаточно еды (требуется: ${cost.food}, на складе: ${gameState.resources.food})."
            )
        }

        if (cost.fuel > 0 && gameState.resources.fuel < cost.fuel) {
            return TravelValidationResult.Invalid(
                reason = TravelFailureReason.INSUFFICIENT_FUEL,
                message = "Недостаточно топлива (требуется: ${cost.fuel}, в цистернах: ${gameState.resources.fuel})."
            )
        }

        return TravelValidationResult.Valid
    }

    /**
     * Executes atomic deduction of resources and generates initial TravelState,
     * transitioning the vehicle to IN_USE.
     */
    fun startTravelTransaction(
        destination: Location,
        transportMode: TravelTransportMode,
        gameState: GameState,
        participantIds: List<String> = emptyList(),
        vehicle: Vehicle? = null,
        originLocationId: String = gameState.currentLocationId
    ): TravelTransactionResult {
        val targetVehicle = vehicle ?: resolveVehicle(transportMode, gameState)

        val validation = validateTravel(
            destination = destination,
            transportMode = transportMode,
            gameState = gameState,
            participantIds = participantIds,
            vehicle = targetVehicle,
            originLocationId = originLocationId
        )

        if (validation is TravelValidationResult.Invalid) {
            return TravelTransactionResult.Failure(
                reason = validation.reason,
                message = validation.message
            )
        }

        val effectiveSquad = if (participantIds.isNotEmpty()) {
            participantIds
        } else {
            gameState.selectedSquadIds.toList().ifEmpty { listOf("char_1") }
        }

        val originLoc = gameState.locations.find { it.id == originLocationId }
        val cost = calculateTravelCost(
            destination = destination,
            transportMode = transportMode,
            participantCount = effectiveSquad.size,
            technologies = gameState.technologies,
            origin = originLoc,
            vehicle = targetVehicle
        )

        // Safe atomic resource deduction
        val updatedResources = gameState.resources.copy(
            water = (gameState.resources.water - cost.water).coerceAtLeast(0),
            food = (gameState.resources.food - cost.food).coerceAtLeast(0),
            fuel = (gameState.resources.fuel - cost.fuel).coerceAtLeast(0)
        )

        // Update vehicle status to IN_USE if a physical vehicle was assigned
        val updatedVehicles = if (targetVehicle != null && transportMode.requiresVehicle) {
            gameState.vehicles.map { veh ->
                if (veh.id == targetVehicle.id) {
                    veh.copy(status = VehicleStatus.IN_USE)
                } else veh
            }
        } else {
            gameState.vehicles
        }

        val isReturning = destination.isPlayerBase
        val travelId = "travel_${System.currentTimeMillis()}"

        val vehicleName = targetVehicle?.name ?: transportMode.titleRu
        val cargoCap = targetVehicle?.capacityKg ?: 25

        val travelState = TravelState(
            id = travelId,
            fromLocationId = originLocationId,
            toLocationId = destination.id,
            transportMode = transportMode,
            vehicleId = targetVehicle?.id,
            vehicleName = vehicleName,
            participantIds = effectiveSquad,
            leaderId = effectiveSquad.firstOrNull(),
            cargoCapacityKg = cargoCap,
            distanceKm = cost.distanceKm,
            traveledKm = 0f,
            progressFraction = 0f,
            status = TravelStatus.TRAVELING,
            isReturning = isReturning,
            startTimestamp = System.currentTimeMillis(),
            startDateTime = gameState.gameDateTime,
            estimatedHours = cost.estimatedDurationHours,
            costPaid = cost,
            statusMessage = if (isReturning) {
                "Отряд начал возвращение на аванпост «Фронтир» ($vehicleName)."
            } else {
                "Отряд выдвинулся к точке «${destination.name}» на $vehicleName (${cost.distanceKm} км)."
            },
            currentSectorName = destination.sectorCode,
            travelLogs = listOf(
                "Снаряжение укомплектовано. Списано: Вода ${cost.water}, Еда ${cost.food}, Топливо ${cost.fuel}.",
                "Группа покинула исходную точку [${originLoc?.name ?: "База"}] на «$vehicleName»."
            )
        )

        return TravelTransactionResult.Success(
            travelState = travelState,
            cost = cost,
            updatedResources = updatedResources,
            updatedVehicles = updatedVehicles
        )
    }

    /**
     * Advances travel by a step (e.g. 25% or given delta), returning updated state.
     */
    fun advanceStep(current: TravelState, locations: List<Location>, stepFraction: Float = 0.25f): TravelState {
        if (current.status != TravelStatus.TRAVELING && current.status != TravelStatus.RETURNING) {
            return current
        }

        val newFraction = (current.progressFraction + stepFraction).coerceIn(0f, 1f)
        val newTraveledKm = (current.distanceKm * newFraction)
        val newStep = current.stepCount + 1

        val destLoc = locations.find { it.id == current.toLocationId }
        val destName = destLoc?.name ?: "Пункт назначения"

        if (newFraction >= 1.0f) {
            val arrivedLog = "Отряд успешно прибыл в «$destName»! Маршрут завершён."
            return current.copy(
                traveledKm = current.distanceKm.toFloat(),
                progressFraction = 1.0f,
                stepCount = newStep,
                status = TravelStatus.ARRIVED,
                statusMessage = "Прибыли в «$destName». Сектор готов к осмотру.",
                travelLogs = current.travelLogs + arrivedLog
            )
        }

        val percent = (newFraction * 100).roundToInt()
        val stepLog = when (percent) {
            in 20..35 -> "Пройдена первая треть пути (${String.format("%.1f", newTraveledKm)} км). Дорожные условия стабильные."
            in 45..65 -> "Экватор маршрута. Следы старого асфальта сменились песчаными барханами."
            in 70..90 -> "Ориентиры цели «$destName» видны на горизонте. До точки осталось ${String.format("%.1f", current.distanceKm - newTraveledKm)} км."
            else -> "Пройдено ${String.format("%.1f", newTraveledKm)} из ${current.distanceKm} км."
        }

        return current.copy(
            traveledKm = newTraveledKm,
            progressFraction = newFraction,
            stepCount = newStep,
            statusMessage = "В пути к «$destName»: $percent%",
            travelLogs = current.travelLogs + stepLog
        )
    }
}
