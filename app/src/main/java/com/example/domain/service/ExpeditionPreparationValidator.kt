package com.example.domain.service

import com.example.data.TravelCalculator
import com.example.domain.model.*

/**
 * Comprehensive, pure validation engine for expedition preparations.
 * Evaluates draft configuration against current GameState rules and returns
 * structured blocking issues, advisory warnings, and checklist verifications.
 */
object ExpeditionPreparationValidator {

    fun validate(
        prepState: ExpeditionPreparationState,
        gameState: GameState
    ): ExpeditionValidationResult {
        val blockingIssues = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val checks = mutableListOf<ExpeditionCheckItem>()

        val destLoc = gameState.locations.find { it.id == prepState.destinationLocationId }
        val originLoc = gameState.locations.find { it.id == prepState.originLocationId }

        // 1. Destination Check
        val isDestValid = destLoc != null && destLoc.isUnlocked && destLoc.id != gameState.currentLocationId
        checks.add(
            ExpeditionCheckItem(
                id = "check_dest",
                title = "Место назначения",
                detail = if (destLoc == null) {
                    "Локация не найдена"
                } else if (!destLoc.isUnlocked) {
                    "Локация заблокирована"
                } else if (destLoc.id == gameState.currentLocationId) {
                    "Отряд уже находится в этой локации"
                } else {
                    "${destLoc.name} (${destLoc.distanceKm} км)"
                },
                isPassed = isDestValid,
                isCritical = true
            )
        )
        if (destLoc == null) {
            blockingIssues.add("Целевая локация не найдена на карте.")
        } else if (!destLoc.isUnlocked) {
            blockingIssues.add("Сектор «${destLoc.displayName}» заблокирован или скрыт туманом.")
        } else if (destLoc.id == gameState.currentLocationId) {
            blockingIssues.add("Отряд уже находится в локации «${destLoc.name}».")
        }

        // 2. Active Travel Conflict
        val noActiveTravelConflict = gameState.activeTravel == null || !gameState.activeTravel.isActiveTravel
        if (!noActiveTravelConflict) {
            blockingIssues.add("Другой отряд уже находится в пути (${gameState.activeTravel?.progressPercent}%).")
        }

        // 3. Squad Participants
        val participantChars = gameState.characters.filter { prepState.participantIds.contains(it.id) }
        val hasParticipants = participantChars.isNotEmpty()
        checks.add(
            ExpeditionCheckItem(
                id = "check_squad",
                title = "Состав отряда",
                detail = if (hasParticipants) "${participantChars.size} бойцов готовы к маршу" else "В отряде нет участников",
                isPassed = hasParticipants,
                isCritical = true
            )
        )
        if (!hasParticipants) {
            blockingIssues.add("Выберите хотя бы одного жителя для экспедиции.")
        }

        // Check health and availability of participants
        val injuredChars = participantChars.filter { it.status == CharacterStatus.INJURED || it.health <= 20 }
        if (injuredChars.isNotEmpty()) {
            blockingIssues.add("В отряде есть тяжелораненые бойцы: ${injuredChars.joinToString { it.name }}. Вылечите их в медпункте.")
        }
        val awayChars = participantChars.filter { it.status == CharacterStatus.ON_EXPEDITION }
        if (awayChars.isNotEmpty()) {
            blockingIssues.add("Некоторые бойцы уже на задании: ${awayChars.joinToString { it.name }}.")
        }

        // 4. Leader Selection
        val hasLeader = !prepState.leaderId.isNullOrBlank() && prepState.participantIds.contains(prepState.leaderId)
        val leaderChar = participantChars.find { it.id == prepState.leaderId }
        checks.add(
            ExpeditionCheckItem(
                id = "check_leader",
                title = "Командир группы",
                detail = if (hasLeader) "Командир: ${leaderChar?.name} (Ур. ${leaderChar?.level})" else "Командир не назначен",
                isPassed = hasLeader,
                isCritical = true
            )
        )
        if (!hasLeader && hasParticipants) {
            blockingIssues.add("Назначьте командира экспедиции перед выходом.")
        }

        // 5. Transport & Passenger Capacity
        val targetVehicle = if (prepState.travelMode.requiresVehicle) {
            gameState.vehicles.find { it.id == prepState.selectedVehicleId }
                ?: TravelCalculator.resolveVehicle(prepState.travelMode, gameState)
        } else null

        var vehicleValid = true
        var vehicleDetail = prepState.travelMode.titleRu

        if (prepState.travelMode.requiresVehicle) {
            if (targetVehicle == null || !targetVehicle.isUnlocked) {
                vehicleValid = false
                vehicleDetail = "Транспорт отсутствует в гараже"
                blockingIssues.add("Для режима «${prepState.travelMode.titleRu}» требуется исправный транспорт.")
            } else if (targetVehicle.status == VehicleStatus.IN_USE) {
                vehicleValid = false
                vehicleDetail = "«${targetVehicle.name}» уже на задании"
                blockingIssues.add("Транспорт «${targetVehicle.name}» уже используется в другом рейде.")
            } else if (targetVehicle.status == VehicleStatus.DAMAGED || targetVehicle.durabilityPercent <= 0) {
                vehicleValid = false
                vehicleDetail = "«${targetVehicle.name}» сломан (требуется ремонт)"
                blockingIssues.add("Транспорт «${targetVehicle.name}» повреждён и требует ремонта в гараже.")
            } else if (participantChars.size > targetVehicle.maxPassengers) {
                vehicleValid = false
                vehicleDetail = "Перегруз экипажа: ${participantChars.size} / ${targetVehicle.maxPassengers} мест"
                blockingIssues.add("Отряд (${participantChars.size} чел.) превышает вместимость «${targetVehicle.name}» (${targetVehicle.maxPassengers} мест).")
            } else {
                vehicleDetail = "«${targetVehicle.name}» (${participantChars.size}/${targetVehicle.maxPassengers} мест)"
            }
        }

        checks.add(
            ExpeditionCheckItem(
                id = "check_transport",
                title = "Транспорт и логистика",
                detail = vehicleDetail,
                isPassed = vehicleValid,
                isCritical = true
            )
        )

        // 6. Minimum Travel Cost Calculation
        val destination = destLoc ?: Location(id = "dummy", name = "Unknown", type = LocationType.CITY_RUINS, distanceKm = 10, dangerLevel = DangerLevel.LOW)
        val travelCost = TravelCalculator.calculateTravelCost(
            destination = destination,
            transportMode = prepState.travelMode,
            participantCount = participantChars.size.coerceAtLeast(1),
            technologies = gameState.technologies,
            origin = originLoc,
            vehicle = targetVehicle
        )

        // 7. Supplies Checks (Minimum Required vs Taken)
        val takenWater = prepState.getSupplyAmount(ResourceType.WATER)
        val takenFood = prepState.getSupplyAmount(ResourceType.FOOD)
        val takenFuel = prepState.getSupplyAmount(ResourceType.FUEL)

        val hasEnoughWaterTaken = takenWater >= travelCost.water
        val hasEnoughFoodTaken = takenFood >= travelCost.food
        val hasEnoughFuelTaken = if (travelCost.fuel > 0) takenFuel >= travelCost.fuel else true

        val suppliesDetail = "Вода: $takenWater/${travelCost.water}, Еда: $takenFood/${travelCost.food}" +
                if (travelCost.fuel > 0) ", Топливо: $takenFuel/${travelCost.fuel}" else ""

        val suppliesPassed = hasEnoughWaterTaken && hasEnoughFoodTaken && hasEnoughFuelTaken
        checks.add(
            ExpeditionCheckItem(
                id = "check_supplies",
                title = "Провизия и припасы",
                detail = suppliesDetail,
                isPassed = suppliesPassed,
                isCritical = true
            )
        )

        if (!hasEnoughWaterTaken) {
            blockingIssues.add("Недостаточно воды в экспедиционном запасе (взято $takenWater, минимум требуется ${travelCost.water}).")
        }
        if (!hasEnoughFoodTaken) {
            blockingIssues.add("Недостаточно еды в экспедиционном запасе (взято $takenFood, минимум требуется ${travelCost.food}).")
        }
        if (!hasEnoughFuelTaken) {
            blockingIssues.add("Недостаточно топлива для поездки (взято $takenFuel, минимум требуется ${travelCost.fuel}).")
        }

        // Check if warehouse has enough to supply the taken amounts
        prepState.supplies.forEach { (type, amount) ->
            val availableInWarehouse = gameState.resources[type]
            if (amount > availableInWarehouse) {
                blockingIssues.add("На складе недостаточно ресурса «${type.titleRu}» (выбрано $amount, в наличии $availableInWarehouse).")
            }
        }

        // 8. Cargo Capacity & Weight
        val cargoSummary = ExpeditionSupplyCalculator.calculateCargoSummary(
            destination = destination,
            prepState = prepState,
            gameState = gameState
        )

        checks.add(
            ExpeditionCheckItem(
                id = "check_weight",
                title = "Вес и грузоподъёмность",
                detail = "${String.format("%.1f", cargoSummary.totalCurrentWeightKg)} / ${cargoSummary.totalCapacityKg} кг (Свободно: ${String.format("%.1f", cargoSummary.freeLootCapacityKg)} кг)",
                isPassed = !cargoSummary.isOverloaded,
                isCritical = true
            )
        )
        if (cargoSummary.isOverloaded) {
            blockingIssues.add("Перегруз снаряжения (${String.format("%.1f", cargoSummary.totalCurrentWeightKg)} из ${cargoSummary.totalCapacityKg} кг). Уменьшите объём припасов.")
        }

        // -------------------------------------------------------------
        // Advisory Warnings (Non-Blocking Hints)
        // -------------------------------------------------------------
        val roles = participantChars.map { it.role }.toSet()

        if (destination.dangerLevel >= DangerLevel.MODERATE && !roles.contains(CharacterRole.MEDIC)) {
            warnings.add("В опасном секторе нет Медика: ранения бойцов не смогут быть перевязаны на месте.")
        }
        if (destination.dangerLevel >= DangerLevel.HIGH && !roles.contains(CharacterRole.SOLDIER)) {
            warnings.add("Высокий уровень опасности: в отряде нет Штурмовика для сдерживания атак рейдеров.")
        }
        if (!roles.contains(CharacterRole.SCOUT)) {
            warnings.add("Без Разведчика возрастает риск попасть в засаду и пропустить ценные тайники.")
        }
        if (prepState.travelMode.requiresVehicle && !roles.contains(CharacterRole.ENGINEER)) {
            warnings.add("Поездка на технике без Инженера: в случае поломки транспорт нельзя будет починить в поле.")
        }
        if (cargoSummary.freeLootCapacityKg < 10f && !cargoSummary.isOverloaded) {
            warnings.add("Мало свободного места для трофеев (${String.format("%.1f", cargoSummary.freeLootCapacityKg)} кг). Отряд не сможет унести много ценного лута.")
        }
        if (takenWater == travelCost.water || takenFood == travelCost.food) {
            warnings.add("Запас провизии взят строго впритык. Рекомендуется взять дополнительный паёк на случай задержки.")
        }

        val emptyBackpacks = participantChars.count { !it.equipment.hasBackpack }
        if (emptyBackpacks > 0) {
            warnings.add("$emptyBackpacks бойцов без рюкзака. Экипируйте рюкзаки в мастерской для увеличения вместимости.")
        }

        val canDepart = blockingIssues.isEmpty()

        return ExpeditionValidationResult(
            canDepart = canDepart,
            blockingIssues = blockingIssues,
            warnings = warnings,
            checkItems = checks,
            travelCost = travelCost,
            cargoSummary = cargoSummary
        )
    }
}
