package com.example.domain.model

/**
 * Status of an expedition party or traveler navigating the global wasteland map.
 */
enum class TravelStatus(val titleRu: String) {
    IN_SETTLEMENT("В поселении"),
    PREPARING("Подготовка к путешествию"),
    TRAVELING("В пути"),
    ARRIVED("Прибыл в пункт назначения"),
    RETURNING("Возвращение на базу"),
    COMPLETED("Путешествие завершено"),
    CANCELLED("Отменено")
}

/**
 * Transportation modes available for global map travel.
 */
enum class TravelTransportMode(
    val titleRu: String,
    val baseSpeedKmH: Float,
    val foodCostPerKm: Float,
    val waterCostPerKm: Float,
    val fuelCostPerKm: Float,
    val requiresVehicle: Boolean,
    val defaultVehicleId: String?,
    val description: String,
    val vehicleType: VehicleType
) {
    FOOT(
        titleRu = "Пешком",
        baseSpeedKmH = 5f,
        foodCostPerKm = 0.6f,
        waterCostPerKm = 0.8f,
        fuelCostPerKm = 0f,
        requiresVehicle = false,
        defaultVehicleId = null,
        description = "Базовый способ передвижения. Не требует топлива, но расходует больше воды и провианта.",
        vehicleType = VehicleType.FOOT
    ),
    BICYCLE(
        titleRu = "Велосипед",
        baseSpeedKmH = 14f,
        foodCostPerKm = 0.35f,
        waterCostPerKm = 0.5f,
        fuelCostPerKm = 0f,
        requiresVehicle = true,
        defaultVehicleId = "veh_bike_1",
        description = "Втрое быстрее пешего шага. Умеренный расход сил и воды, не требует топлива.",
        vehicleType = VehicleType.BICYCLE
    ),
    MOTORCYCLE(
        titleRu = "Мотоцикл",
        baseSpeedKmH = 45f,
        foodCostPerKm = 0.12f,
        waterCostPerKm = 0.15f,
        fuelCostPerKm = 0.35f,
        requiresVehicle = true,
        defaultVehicleId = "veh_moto_1",
        description = "Быстрый транспорт для оперативных разведок с малым расходом топлива.",
        vehicleType = VehicleType.MOTORCYCLE
    ),
    BUGGY(
        titleRu = "Багги «Бархан»",
        baseSpeedKmH = 35f,
        foodCostPerKm = 0.15f,
        waterCostPerKm = 0.2f,
        fuelCostPerKm = 0.65f,
        requiresVehicle = true,
        defaultVehicleId = "veh_buggy_1",
        description = "Скоростной внедорожник. Быстро преодолевает расстояния с минимальным расходом еды, требует топлива.",
        vehicleType = VehicleType.LIGHT_BUGGY
    ),
    OFFROAD(
        titleRu = "Внедорожник",
        baseSpeedKmH = 30f,
        foodCostPerKm = 0.15f,
        waterCostPerKm = 0.2f,
        fuelCostPerKm = 0.95f,
        requiresVehicle = true,
        defaultVehicleId = "veh_offroad_1",
        description = "Универсальный полноприводный автомобиль с хорошей вместимостью.",
        vehicleType = VehicleType.OFFROAD
    ),
    ARMORED_TRUCK(
        titleRu = "Бронегрузовик «Утёс»",
        baseSpeedKmH = 22f,
        foodCostPerKm = 0.15f,
        waterCostPerKm = 0.2f,
        fuelCostPerKm = 1.6f,
        requiresVehicle = true,
        defaultVehicleId = "veh_truck_1",
        description = "Тяжёлая защищённая машина для опасных дальних рейдов с высоким расходом топлива и огромным кузовом.",
        vehicleType = VehicleType.ARMORED_TRUCK
    );

    companion object {
        fun fromVehicle(vehicle: Vehicle?): TravelTransportMode {
            if (vehicle == null) return FOOT
            return values().find { it.vehicleType == vehicle.type } ?: BUGGY
        }
    }
}

/**
 * Detailed breakdown of resources and duration required for a specific journey.
 */
data class TravelCost(
    val food: Int = 0,
    val water: Int = 0,
    val fuel: Int = 0,
    val money: Int = 0,
    val estimatedDurationHours: Float = 1.0f,
    val distanceKm: Int = 10,
    val rawSpeedKmH: Float = 5f,
    val vehicleUsed: Vehicle? = null
) {
    val duration: GameDuration
        get() = GameDuration.fromHoursFloat(estimatedDurationHours)

    val formattedDuration: String
        get() = duration.formatted

    val totalResourceUnits: Int
        get() = food + water + fuel
}

/**
 * Immutable state representing an active or completed journey across the wasteland map.
 */
data class TravelState(
    val id: String,
    val fromLocationId: String = "loc_base",
    val toLocationId: String,
    val transportMode: TravelTransportMode = TravelTransportMode.FOOT,
    val vehicleId: String? = null,
    val vehicleName: String? = null,
    val participantIds: List<String> = emptyList(),
    val leaderId: String? = null,
    val carriedItemIds: List<String> = emptyList(),
    val cargoCapacityKg: Int = 25,
    val distanceKm: Int = 10,
    val traveledKm: Float = 0f,
    val progressFraction: Float = 0f,
    val status: TravelStatus = TravelStatus.TRAVELING,
    val isReturning: Boolean = false,
    val startTimestamp: Long = 0L,
    val startDateTime: GameDateTime = GameDateTime.START_TIME,
    val estimatedHours: Float = 1.0f,
    val costPaid: TravelCost = TravelCost(),
    val statusMessage: String = "Отряд выдвинулся по указанному маршруту.",
    val currentSectorName: String = "Песчаный тракт",
    val stepCount: Int = 0,
    val maxSteps: Int = 4,
    val travelLogs: List<String> = listOf("Экспедиционная группа покинула базу.")
) {
    val totalDuration: GameDuration
        get() = GameDuration.fromHoursFloat(estimatedHours)
    val remainingKm: Float
        get() = (distanceKm - traveledKm).coerceAtLeast(0f)

    val progressPercent: Int
        get() = (progressFraction * 100).toInt().coerceIn(0, 100)

    val isCompletedOrArrived: Boolean
        get() = status == TravelStatus.ARRIVED || status == TravelStatus.COMPLETED

    val isActiveTravel: Boolean
        get() = status == TravelStatus.TRAVELING || status == TravelStatus.RETURNING
}

/**
 * Reasons why a journey might be blocked or invalidated.
 */
enum class TravelFailureReason(val titleRu: String) {
    INSUFFICIENT_WATER("Недостаточно чистой воды на складе"),
    INSUFFICIENT_FOOD("Недостаточно провизии на складе"),
    INSUFFICIENT_FUEL("Недостаточно топлива в цистернах аванпоста"),
    LOCATION_LOCKED("Сектор заблокирован или скрыт туманом"),
    LOCATION_IS_CURRENT("Группа уже находится в данной локации"),
    ALREADY_TRAVELING("Другой отряд уже находится в пути"),
    VEHICLE_UNAVAILABLE("Выбранный транспорт занят в пути или недоступен"),
    VEHICLE_DAMAGED("Транспортное средство требует ремонта"),
    NO_SQUAD_MEMBERS("В отряде должен быть хотя бы один участник"),
    SQUAD_TOO_LARGE_FOR_VEHICLE("Размер отряда превышает вместимость транспорта"),
    TECH_REQUIRED("Необходима довоенная технология для прохода")
}

/**
 * Result of validating travel conditions prior to departure.
 */
sealed class TravelValidationResult {
    data object Valid : TravelValidationResult()
    data class Invalid(val reason: TravelFailureReason, val message: String) : TravelValidationResult()

    val isValid: Boolean get() = this is Valid
}

/**
 * Atomic transaction outcome of dispatching a travel expedition.
 */
sealed class TravelTransactionResult {
    data class Success(
        val travelState: TravelState,
        val cost: TravelCost,
        val updatedResources: GameResources,
        val updatedVehicles: List<Vehicle> = emptyList()
    ) : TravelTransactionResult()

    data class Failure(
        val reason: TravelFailureReason,
        val message: String
    ) : TravelTransactionResult()

    val isSuccess: Boolean get() = this is Success
}
