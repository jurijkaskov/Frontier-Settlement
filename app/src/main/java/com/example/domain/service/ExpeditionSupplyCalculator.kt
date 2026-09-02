package com.example.domain.service

import com.example.data.TravelCalculator
import com.example.domain.model.*
import kotlin.math.ceil
import kotlin.math.max

/**
 * Pure calculation service for expedition supply requirements, recommended rations,
 * cargo capacity, and weight distribution.
 */
object ExpeditionSupplyCalculator {

    /**
     * Gets unit weight in kg for a given ResourceType.
     */
    fun getResourceUnitWeightKg(resourceType: ResourceType): Float {
        return when (resourceType) {
            ResourceType.WATER -> 1.0f
            ResourceType.FOOD -> 0.5f
            ResourceType.FUEL -> 0.8f
            ResourceType.MEDICINE -> 0.2f
            ResourceType.AMMO -> 0.4f
            ResourceType.MATERIALS -> 1.5f
            ResourceType.COMPONENTS -> 0.6f
            ResourceType.RARE_ALLOY -> 2.0f
            ResourceType.MONEY -> 0.0f
        }
    }

    /**
     * Calculates the bare minimum supplies strictly required to make the journey.
     */
    fun calculateMinimumSupplies(
        destination: Location,
        transportMode: TravelTransportMode,
        participantCount: Int,
        technologies: List<ResearchTech> = emptyList(),
        origin: Location? = null,
        vehicle: Vehicle? = null
    ): Map<ResourceType, Int> {
        val cost = TravelCalculator.calculateTravelCost(
            destination = destination,
            transportMode = transportMode,
            participantCount = participantCount,
            technologies = technologies,
            origin = origin,
            vehicle = vehicle
        )

        val result = mutableMapOf<ResourceType, Int>()
        if (cost.water > 0) result[ResourceType.WATER] = cost.water
        if (cost.food > 0) result[ResourceType.FOOD] = cost.food
        if (cost.fuel > 0) result[ResourceType.FUEL] = cost.fuel

        return result
    }

    /**
     * Calculates recommended supplies with safety buffer for wilderness exploration,
     * capped at available warehouse stock.
     */
    fun calculateRecommendedSupplies(
        destination: Location,
        transportMode: TravelTransportMode,
        participantCount: Int,
        technologies: List<ResearchTech> = emptyList(),
        origin: Location? = null,
        vehicle: Vehicle? = null,
        availableResources: GameResources
    ): Map<ResourceType, Int> {
        val minSupplies = calculateMinimumSupplies(
            destination = destination,
            transportMode = transportMode,
            participantCount = participantCount,
            technologies = technologies,
            origin = origin,
            vehicle = vehicle
        )

        val squadCount = max(1, participantCount)
        val dangerMultiplier = when (destination.dangerLevel) {
            DangerLevel.SAFE -> 1.20f
            DangerLevel.LOW -> 1.30f
            DangerLevel.MODERATE -> 1.45f
            DangerLevel.HIGH -> 1.60f
            DangerLevel.EXTREME -> 1.80f
            DangerLevel.UNKNOWN -> 1.35f
        }

        val baseWater = minSupplies[ResourceType.WATER] ?: 2
        val baseFood = minSupplies[ResourceType.FOOD] ?: 2
        val baseFuel = minSupplies[ResourceType.FUEL] ?: 0

        val recWater = ceil(baseWater * dangerMultiplier).toInt().coerceAtMost(availableResources[ResourceType.WATER])
        val recFood = ceil(baseFood * dangerMultiplier).toInt().coerceAtMost(availableResources[ResourceType.FOOD])
        val recFuel = if (baseFuel > 0) {
            ceil(baseFuel * 1.25f).toInt().coerceAtMost(availableResources[ResourceType.FUEL])
        } else 0

        val recMeds = when (destination.dangerLevel) {
            DangerLevel.SAFE -> 0
            DangerLevel.LOW -> 1
            DangerLevel.MODERATE -> (squadCount / 2).coerceAtLeast(1)
            DangerLevel.HIGH, DangerLevel.EXTREME, DangerLevel.UNKNOWN -> squadCount.coerceAtLeast(2)
        }.coerceAtMost(availableResources[ResourceType.MEDICINE])

        val recAmmo = when (destination.dangerLevel) {
            DangerLevel.SAFE, DangerLevel.LOW -> 0
            DangerLevel.MODERATE -> 2
            DangerLevel.HIGH -> 5
            DangerLevel.EXTREME, DangerLevel.UNKNOWN -> 10
        }.coerceAtMost(availableResources[ResourceType.AMMO])

        val map = mutableMapOf<ResourceType, Int>()
        if (recWater > 0) map[ResourceType.WATER] = recWater
        if (recFood > 0) map[ResourceType.FOOD] = recFood
        if (recFuel > 0) map[ResourceType.FUEL] = recFuel
        if (recMeds > 0) map[ResourceType.MEDICINE] = recMeds
        if (recAmmo > 0) map[ResourceType.AMMO] = recAmmo

        return map
    }

    /**
     * Calculates total weight of a supply inventory in kilograms.
     */
    fun calculateSuppliesWeightKg(supplies: Map<ResourceType, Int>): Float {
        return supplies.entries.sumOf { (type, count) ->
            (getResourceUnitWeightKg(type) * count).toDouble()
        }.toFloat()
    }

    /**
     * Calculates total expedition cargo capacity and weight breakdown.
     */
    fun calculateCargoSummary(
        destination: Location,
        prepState: ExpeditionPreparationState,
        gameState: GameState
    ): ExpeditionCargoSummary {
        val selectedParticipants = gameState.characters.filter { prepState.participantIds.contains(it.id) }
        val vehicle = if (prepState.travelMode.requiresVehicle) {
            gameState.vehicles.find { it.id == prepState.selectedVehicleId }
                ?: TravelCalculator.resolveVehicle(prepState.travelMode, gameState)
        } else null

        // 1. Participant carrying capacity
        val participantsCapacity = selectedParticipants.sumOf {
            it.getEffectiveCarryCapacityKg(gameState.inventoryItems)
        }

        // 2. Vehicle cargo capacity
        val vehicleCapacity = vehicle?.capacityKg ?: 0

        // 3. Tech bonus capacity
        val techCargoBonus = gameState.technologies
            .filter { it.isResearched }
            .flatMap { it.effects }
            .filterIsInstance<TechEffect.StorageCapacityBoost>()
            .sumOf { (it.additionalCapacity / 10).coerceAtLeast(5) }

        val totalCapacity = max(10, participantsCapacity + vehicleCapacity + techCargoBonus)

        // Weight of taken supplies
        val suppliesWeight = calculateSuppliesWeightKg(prepState.supplies)

        // Weight of discrete carried warehouse items
        val itemMap = gameState.inventoryItems.associateBy { it.id }
        val carriedItemsWeight = prepState.carriedItemIds.mapNotNull { itemMap[it] }.sumOf { it.weightKg.toDouble() }.toFloat()

        // Gear worn by characters is tracked for stats
        val gearWeight = selectedParticipants.sumOf { it.getTotalEquippedWeightKg(gameState.inventoryItems).toDouble() }.toFloat()

        val totalCargoWeight = suppliesWeight + carriedItemsWeight
        val freeLootSpace = (totalCapacity - totalCargoWeight).coerceAtLeast(0f)
        val isOverloaded = totalCargoWeight > totalCapacity
        val percent = ((totalCargoWeight / totalCapacity) * 100).toInt().coerceIn(0, 150)

        return ExpeditionCargoSummary(
            totalCapacityKg = totalCapacity,
            suppliesWeightKg = suppliesWeight,
            gearWeightKg = gearWeight,
            totalCurrentWeightKg = totalCargoWeight,
            freeLootCapacityKg = freeLootSpace,
            isOverloaded = isOverloaded,
            capacityPercent = percent
        )
    }
}
