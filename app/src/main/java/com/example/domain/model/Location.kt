package com.example.domain.model

enum class LocationType(val titleRu: String, val iconKey: String) {
    SETTLEMENT("Базовый аванпост", "base"),
    TRADING_POST("Торговый форпост", "trading"),
    FARM("Заброшенная ферма", "farm"),
    FOREST("Хвойная чаща", "forest"),
    VILLAGE("Опустевшая деревня", "village"),
    ABANDONED_STATION("Заброшенная станция", "station"),
    INDUSTRIAL_PLANT("Промышленный завод", "industrial"),
    WAREHOUSE_COMPLEX("Логистический склад", "warehouse"),
    CITY_RUINS("Руины мегаполиса", "ruins"),
    MILITARY_BUNKER("Военный бункер", "bunker"),
    ANOMALY_ZONE("Аномальный сектор", "anomaly")
}

enum class DangerLevel(val titleRu: String, val rating: Int) {
    SAFE("Безопасно", 0),
    LOW("Низкая угроза", 1),
    MODERATE("Умеренная угроза", 2),
    HIGH("Высокая опасность", 3),
    EXTREME("Смертельная зона", 4),
    UNKNOWN("Неизвестно", -1)
}

enum class LocationStatus(val titleRu: String) {
    AVAILABLE("Доступно для вылазки"),
    VISITED("Посещено"),
    EXPLORED("Исследовано"),
    LOCKED("Заблокировано"),
    UNKNOWN("Неизведанная территория")
}

data class LocalArea(
    val id: String,
    val name: String,
    val typeRu: String,
    val isDiscovered: Boolean = true,
    val isExplored: Boolean = false,
    val dangerRating: Int = 1,
    val description: String = ""
)

enum class TerrainType(val titleRu: String) {
    WASTELAND("Пустошь"),
    FOREST("Лесной массив"),
    HILLS("Холмы и скалы"),
    WATER("Речная долина"),
    RUINS("Руины застройки")
}

data class Location(
    val id: String,
    val name: String,
    val type: LocationType,
    val dangerLevel: DangerLevel,
    val isUnlocked: Boolean = true,
    val status: LocationStatus = if (isUnlocked) LocationStatus.AVAILABLE else LocationStatus.LOCKED,
    val distanceKm: Int = 12,
    val potentialLoot: List<String> = listOf("Материалы", "Топливо", "Медикаменты"),
    val description: String = "",
    val estimatedLootMaterials: Int = 40,
    val estimatedLootCredits: Int = 80,
    val estimatedLootFood: Int = 30,
    val estimatedLootFuel: Int = 20,
    val coordinateX: Float = 0.5f,
    val coordinateY: Float = 0.5f,
    val requiredSettlementLevel: Int = 1,
    val requiredTechId: String? = null,
    val recommendedSquadSize: Int = 2,
    val terrainType: TerrainType = TerrainType.WASTELAND,
    val isPlayerBase: Boolean = false,
    val timesExplored: Int = 0,
    val sectorCode: String = "S-1",
    val visualAssetId: String? = null,
    val observations: List<String> = emptyList(),
    val threats: List<String> = emptyList(),
    val localAreas: List<LocalArea> = emptyList(),
    val explorationProgressPercent: Int = 0,
    val visitCount: Int = 0,
    val firstVisitedDay: Int? = null,
    val lastVisitedDay: Int? = null
) {
    val isAvailable: Boolean
        get() = (status == LocationStatus.AVAILABLE || status == LocationStatus.VISITED || status == LocationStatus.EXPLORED || isUnlocked) && !isPlayerBase

    val isHiddenOrUnknown: Boolean
        get() = status == LocationStatus.UNKNOWN

    val displayName: String
        get() = if (isHiddenOrUnknown) "Неизвестный сектор ($sectorCode)" else name

    val displayDescription: String
        get() = if (isHiddenOrUnknown) "Плотный туман радиоактивных осадков и помех скрывает этот сектор. Необходима разведка." else description
}

