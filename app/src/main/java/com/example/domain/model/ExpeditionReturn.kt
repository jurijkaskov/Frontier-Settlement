package com.example.domain.model

/**
 * Reasons why an expedition cannot initiate or complete return.
 */
enum class ReturnFailureReason(val titleRu: String) {
    COMBAT_IN_PROGRESS("Невозможно вернуться: отряд находится в активном бою!"),
    EVENT_UNRESOLVED("Необходимо завершить текущее событие или сделать выбор."),
    NO_ACTIVE_EXPEDITION("Активная экспедиция не найдена."),
    ALREADY_IN_SETTLEMENT("Отряд уже находится на территории базового аванпоста."),
    ALREADY_RETURNING("Отряд уже находится на обратном пути на базу.")
}

/**
 * Outcome of validating return readiness.
 */
data class ReturnValidationResult(
    val isReady: Boolean,
    val failureReason: ReturnFailureReason? = null,
    val message: String = "",
    val warnings: List<String> = emptyList()
) {
    val canReturn: Boolean get() = isReady
}

/**
 * Detailed outcome for an individual squad participant upon returning to settlement.
 */
data class SquadMemberReturnOutcome(
    val characterId: String,
    val characterName: String,
    val role: CharacterRole,
    val avatarTag: String,
    val xpGained: Int,
    val oldLevel: Int,
    val newLevel: Int,
    val leveledUp: Boolean,
    val skillPointsGained: Int,
    val oldHealth: Int,
    val finalHealth: Int,
    val maxHealth: Int,
    val isInjured: Boolean,
    val moraleDelta: Int = 0,
    val finalMorale: Int = 100
)

/**
 * Holds excess resources and items that could not fit into the warehouse upon arrival,
 * stored safely in a temporary staging area at the outpost gates.
 */
data class PendingSettlementUnload(
    val resources: GameResources = GameResources(money = 0, food = 0, water = 0, fuel = 0, materials = 0),
    val items: List<WarehouseItem> = emptyList(),
    val sourceLocationName: String = "",
    val dayArrived: Int = 1
) {
    val hasPendingCargo: Boolean
        get() = resources.totalStoredVolume > 0 || items.isNotEmpty()

    val totalVolume: Int
        get() = resources.totalStoredVolume + items.sumOf { it.quantity * it.unitSize }
}

/**
 * Comprehensive summary model generated upon expedition completion,
 * used for UI display, logs, and statistics.
 */
data class ExpeditionReturnSummary(
    val expeditionId: String,
    val locationName: String,
    val locationDistanceKm: Int,
    val vehicleName: String,
    val travelMode: TravelTransportMode,
    val daysElapsed: Int = 1,
    val startDateTime: GameDateTime = GameDateTime.START_TIME,
    val endDateTime: GameDateTime = GameDateTime.START_TIME,
    val travelDuration: GameDuration = GameDuration.ofHours(1),
    val gatheredResources: GameResources,
    val unloadedResources: GameResources,
    val overflowResources: GameResources,
    val gatheredItems: List<WarehouseItem> = emptyList(),
    val unloadedItems: List<WarehouseItem> = emptyList(),
    val overflowItems: List<WarehouseItem> = emptyList(),
    val squadOutcomes: List<SquadMemberReturnOutcome>,
    val totalXpAwarded: Int,
    val reputationGained: Int = 15,
    val completedQuests: List<String> = emptyList(),
    val discoveredFlags: List<String> = emptyList(),
    val summaryLogs: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
) {
    val hasOverflow: Boolean
        get() = overflowResources.totalStoredVolume > 0 || overflowItems.isNotEmpty()

    val totalLootValueCredits: Int
        get() = gatheredResources.money +
                (gatheredResources.materials * 3) +
                (gatheredResources.food * 2) +
                (gatheredResources.water * 2) +
                (gatheredResources.fuel * 4) +
                gatheredItems.sumOf { it.baseValueCredits * it.quantity }
}
