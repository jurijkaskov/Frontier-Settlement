package com.example.domain.model

enum class ExpeditionStatus(val titleRu: String) {
    PREPARING("Подготовка"),
    TRAVELING("В пути"),
    EXPLORING("Исследование локации"),
    COMBAT("Боевое столкновение"),
    RETURNING("Возвращение"),
    COMPLETED("Завершена"),
    FAILED("Провалена")
}

/**
 * Detailed expedition life-cycle phases as defined in System 16, 17 & 18.
 */
enum class ExpeditionPhase(val titleRu: String) {
    PREPARING("Подготовка снаряжения"),
    TRAVELING_TO_LOCATION("Переход к цели"),
    AT_LOCATION("Прибытие на точку"),
    EXPLORING("Осмотр и зачистка сектора"),
    COMBAT("Тактический бой"),
    RETURNING("Возвращение на базу"),
    COMPLETED("Экспедиция завершена")
}

data class Expedition(
    val id: String,
    val location: Location,
    val squad: List<Character>,
    val vehicle: Vehicle,
    val status: ExpeditionStatus = ExpeditionStatus.PREPARING,
    val phase: ExpeditionPhase = ExpeditionPhase.PREPARING,
    val currentStep: Int = 0,
    val maxSteps: Int = 4,
    val currentEvent: ExpeditionEvent? = null,
    val activeEventState: ActiveEventState? = null,
    val gatheredLoot: GameResources = GameResources(money = 0, food = 0, water = 0, fuel = 0, materials = 0),
    val lootItemIds: List<String> = emptyList(),
    val xpReward: Int = 35,
    val logs: List<String> = listOf("Отряд сформирован и готов выдвигаться."),
    // Extended properties for Point 16, 17 & 18
    val leaderId: String? = null,
    val travelMode: TravelTransportMode = TravelTransportMode.FOOT,
    val travelId: String? = null,
    val supplies: Map<ResourceType, Int> = emptyMap(),
    val carriedItemIds: List<String> = emptyList(),
    val cargoCapacityKg: Int = 25,
    val cargoWeightKg: Float = 0f,
    val startTimestamp: Long = System.currentTimeMillis(),
    val startDateTime: GameDateTime = GameDateTime.START_TIME,
    val explorationProgress: Int = 0,
    val currentAreaId: String? = null,
    val visitedAreaIds: Set<String> = emptySet(),
    val seed: Long = System.currentTimeMillis()
) {
    val progressFraction: Float
        get() = if (maxSteps > 0) (currentStep.toFloat() / maxSteps.toFloat()).coerceIn(0f, 1f) else 0f

    val freeLootCapacityKg: Float
        get() = (cargoCapacityKg - cargoWeightKg).coerceAtLeast(0f)

    val leader: Character?
        get() = squad.find { it.id == leaderId } ?: squad.firstOrNull()

    val currentEventOrActive: ExpeditionEvent?
        get() = activeEventState?.event ?: currentEvent
}
