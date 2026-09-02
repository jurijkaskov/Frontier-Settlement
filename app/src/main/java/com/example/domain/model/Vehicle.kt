package com.example.domain.model

/**
 * Categories of transportation available to the settlement.
 */
enum class VehicleType(
    val titleRu: String,
    val defaultSpeedKmH: Int,
    val defaultCapacityKg: Int,
    val defaultFuelPerKm: Float,
    val defaultMaxPassengers: Int,
    val isPhysicalVehicle: Boolean = true
) {
    FOOT(
        titleRu = "Пеший переход",
        defaultSpeedKmH = 5,
        defaultCapacityKg = 25,
        defaultFuelPerKm = 0f,
        defaultMaxPassengers = 4,
        isPhysicalVehicle = false
    ),
    BICYCLE(
        titleRu = "Велосипед",
        defaultSpeedKmH = 14,
        defaultCapacityKg = 45,
        defaultFuelPerKm = 0f,
        defaultMaxPassengers = 1
    ),
    MOTORCYCLE(
        titleRu = "Мотоцикл",
        defaultSpeedKmH = 45,
        defaultCapacityKg = 60,
        defaultFuelPerKm = 0.35f,
        defaultMaxPassengers = 2
    ),
    LIGHT_BUGGY(
        titleRu = "Лёгкий багги",
        defaultSpeedKmH = 35,
        defaultCapacityKg = 190,
        defaultFuelPerKm = 0.65f,
        defaultMaxPassengers = 2
    ),
    OFFROAD(
        titleRu = "Внедорожник",
        defaultSpeedKmH = 30,
        defaultCapacityKg = 320,
        defaultFuelPerKm = 0.95f,
        defaultMaxPassengers = 4
    ),
    ARMORED_TRUCK(
        titleRu = "Бронегрузовик",
        defaultSpeedKmH = 22,
        defaultCapacityKg = 650,
        defaultFuelPerKm = 1.6f,
        defaultMaxPassengers = 5
    )
}

/**
 * Operational state of a vehicle in the settlement fleet.
 */
enum class VehicleStatus(val titleRu: String, val isAvailableForMissions: Boolean) {
    AVAILABLE("Готов к рейду", true),
    IN_USE("В экспедиции", false),
    MAINTENANCE("На обслуживании", false),
    DAMAGED("Требует ремонта", false),
    UNAVAILABLE("Недоступен", false)
}

/**
 * Individual physical vehicle entity owned by the settlement.
 */
data class Vehicle(
    val id: String,
    val name: String,
    val type: VehicleType,
    val capacityKg: Int = type.defaultCapacityKg,
    val fuelConsumptionPerKm: Float = type.defaultFuelPerKm,
    val speedKmH: Int = type.defaultSpeedKmH,
    val maxPassengers: Int = type.defaultMaxPassengers,
    val status: VehicleStatus = VehicleStatus.AVAILABLE,
    val isAvailable: Boolean = true,
    val isUnlocked: Boolean = true,
    val durabilityPercent: Int = 100,
    val description: String = "",
    val visualAssetId: String = "veh_default",
    val currentLocationId: String = "loc_base",
    val tripsCompleted: Int = 0,
    val totalDistanceTraveledKm: Int = 0
) {
    val isReadyForTrip: Boolean
        get() = isUnlocked && isAvailable && status == VehicleStatus.AVAILABLE

    val isMotorized: Boolean
        get() = fuelConsumptionPerKm > 0f

    val speedRatingRu: String
        get() = when {
            speedKmH >= 40 -> "Очень высокая"
            speedKmH >= 25 -> "Высокая"
            speedKmH >= 12 -> "Средняя"
            else -> "Низкая"
        }

    val capacityRatingRu: String
        get() = when {
            capacityKg >= 400 -> "Огромная ($capacityKg кг)"
            capacityKg >= 150 -> "Большая ($capacityKg кг)"
            capacityKg >= 50 -> "Средняя ($capacityKg кг)"
            else -> "Малая ($capacityKg кг)"
        }

    val fuelRatingRu: String
        get() = if (isMotorized) {
            "${String.format("%.1f", fuelConsumptionPerKm)} л/км"
        } else {
            "Не требуется"
        }
}
