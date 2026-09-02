package com.example.domain.model

import com.example.domain.model.quest.QuestState
import com.example.domain.model.reputation.*
import com.example.domain.service.reputation.ReputationLevelResolver

/**
 * Top-level immutable GameState containing all player progress, settlement status,
 * resources, characters, vehicles, expedition status, combat, and world data.
 * Designed to be easily serializable for persistence or cloud saves in future milestones.
 */
data class GameState(
    val playthroughId: String = "playthrough_${System.currentTimeMillis()}",
    val gameDateTime: GameDateTime = GameDateTime.START_TIME,
    val processedDays: Set<Int> = setOf(1),
    val settlement: Settlement = Settlement(),
    val resources: GameResources = GameResources(),
    val characters: List<Character> = emptyList(),
    val vehicles: List<Vehicle> = emptyList(),
    val locations: List<Location> = emptyList(),
    val technologies: List<ResearchTech> = emptyList(),
    val quests: List<Quest> = emptyList(),
    val questStates: Map<String, QuestState> = emptyMap(),
    val trackedQuestId: String? = null,
    val activeExpedition: Expedition? = null,
    val activeCombat: CombatState? = null,
    val squad: Squad = Squad(),
    val selectedSquadIds: Set<String> = setOf("char_1", "char_2"),
    val selectedVehicleId: String = "veh_foot",
    val dayLogs: List<String> = listOf("День 1: Аванпост-7 основан. Разведчики готовы к выходу на карту пустошей."),
    val merchantState: MerchantState = MerchantState(),
    val activeTabRoute: String = "settlement",
    val inventoryItems: List<WarehouseItem> = emptyList(),
    val currentLocationId: String = "loc_base",
    val activeTravel: TravelState? = null,
    val worldFlags: Map<String, Boolean> = emptyMap(),
    val eventHistory: List<EventHistoryEntry> = emptyList(),
    val lastReturnSummary: ExpeditionReturnSummary? = null,
    val pendingSettlementUnload: PendingSettlementUnload = PendingSettlementUnload(),
    val lastDailySummary: DailySummary? = null,
    val economyReports: List<DailyEconomyReport> = emptyList(),
    val lastEconomyReport: DailyEconomyReport? = null,
    val unpaidDeficits: List<EconomicDeficit> = emptyList(),
    val factionRelations: Map<String, FactionRelation> = emptyMap(),
    val reputationHistory: List<ReputationHistoryEntry> = emptyList(),
    val gameSeed: Long = 133742L,
    val contentGenerationHistory: com.example.domain.content.core.ContentGenerationHistory = com.example.domain.content.core.ContentGenerationHistory()
) {
    /**
     * Primary calendar day number (1-indexed), derived from the authoritative GameDateTime.
     */
    val day: Int
        get() = gameDateTime.day

    /**
     * Current global settlement reputation tier (computed from single source of truth settlement.reputation).
     */
    val reputationTier: ReputationTier
        get() = ReputationLevelResolver.resolveSettlementTier(settlement.reputation)
    /**
     * Whether an expedition or travel party is actively en route.
     */
    val isCurrentlyTraveling: Boolean
        get() = activeTravel != null && activeTravel.isActiveTravel

    /**
     * Whether the travel party has completed the journey and arrived at the target location.
     */
    val isArrivedAtDestination: Boolean
        get() = activeTravel != null && activeTravel.status == TravelStatus.ARRIVED

    /**
     * Current travel progress fraction (0.0f to 1.0f).
     */
    val travelProgress: Float
        get() = activeTravel?.progressFraction ?: 0f

    /**
     * Current location object representing where the player group/scout party is currently stationed.
     */
    val currentLocation: Location?
        get() = locations.find { it.id == currentLocationId } ?: locations.find { it.isPlayerBase }
    /**
     * Total physical storage volume occupied by discrete manufactured items on the warehouse shelves.
     */
    val totalInventoryVolume: Int
        get() = inventoryItems.sumOf { it.quantity * it.unitSize }

    /**
     * Complete combined warehouse storage occupied (bulk resources + crafted items).
     */
    val totalWarehouseOccupiedVolume: Int
        get() = resources.totalStoredVolume + totalInventoryVolume

    /**
     * Available free storage capacity remaining across the settlement warehouse.
     */
    val freeWarehouseCapacity: Int
        get() = (resources.warehouseMaxCapacity - totalWarehouseOccupiedVolume).coerceAtLeast(0)

    /**
     * Storage occupancy fraction between 0.0f and 1.0f.
     */
    val warehouseOccupancyFraction: Float
        get() {
            if (resources.warehouseMaxCapacity <= 0) return 0f
            return (totalWarehouseOccupiedVolume.toFloat() / resources.warehouseMaxCapacity.toFloat()).coerceIn(0f, 1f)
        }

    /**
     * Whether the warehouse is currently at full capacity.
     */
    val isWarehouseFull: Boolean
        get() = freeWarehouseCapacity <= 0

    /**
     * Total living residents in the settlement (single source of truth = characters list size).
     */
    val currentPopulation: Int
        get() = characters.size

    /**
     * Maximum population capacity supported by current settlement infrastructure.
     */
    val maxPopulation: Int
        get() = settlement.maxPopulation

    /**
     * Available free housing beds remaining in the settlement.
     */
    val freeHousingSlots: Int
        get() = (settlement.maxPopulation - characters.size).coerceAtLeast(0)

    /**
     * Whether the settlement housing is completely full.
     */
    val isPopulationAtMax: Boolean
        get() = characters.size >= settlement.maxPopulation

    /**
     * Population occupancy ratio between 0.0f and 1.0f.
     */
    val populationOccupancyFraction: Float
        get() = if (settlement.maxPopulation > 0) {
            (characters.size.toFloat() / settlement.maxPopulation.toFloat()).coerceIn(0f, 1f)
        } else 0f

    /**
     * List of physical vehicles currently ready and available at the settlement base.
     */
    val availableVehicles: List<Vehicle>
        get() = vehicles.filter { it.isReadyForTrip }

    /**
     * List of vehicles currently deployed in active expeditions or travels.
     */
    val inUseVehicles: List<Vehicle>
        get() = vehicles.filter { it.status == VehicleStatus.IN_USE }

    /**
     * Total cargo capacity across the entire settlement fleet.
     */
    val totalFleetCapacityKg: Int
        get() = vehicles.filter { it.isUnlocked }.sumOf { it.capacityKg }

    /**
     * Currently selected vehicle object.
     */
    val selectedVehicle: Vehicle?
        get() = vehicles.find { it.id == selectedVehicleId }

    /**
     * Real resolved character objects belonging to the current expedition squad.
     */
    val squadMembers: List<Character>
        get() = SquadCalculator.resolveMembers(squad, characters)

    /**
     * The designated squad leader character object, if chosen and in squad.
     */
    val squadLeader: Character?
        get() = SquadCalculator.resolveLeader(squad, characters)

    /**
     * Calculated tactical aggregated summary of the expedition squad.
     */
    val squadSummary: SquadAggregatedStats
        get() = SquadCalculator.calculateSummary(squad, characters, selectedVehicle)

    /**
     * Field readiness checklist and status of the expedition squad.
     */
    val squadReadiness: SquadReadinessSummary
        get() = SquadCalculator.calculateReadiness(squad, characters, selectedVehicle)
}
