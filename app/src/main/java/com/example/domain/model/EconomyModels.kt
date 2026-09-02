package com.example.domain.model

/**
 * Data-driven rule defining daily resource production for a specific building type.
 */
data class ProductionRule(
    val buildingType: BuildingType,
    val resourceType: ResourceType,
    val baseAmountPerDay: Int,
    val scalingPerLevel: Int = baseAmountPerDay,
    val requiredStatus: BuildingStatus = BuildingStatus.OPERATIONAL,
    val description: String = ""
) {
    /**
     * Calculates total output based on building level and active technology multipliers.
     */
    fun calculateProduction(buildingLevel: Int, multiplier: Float = 1.0f): Int {
        if (buildingLevel <= 0) return 0
        val rawAmount = (baseAmountPerDay + (buildingLevel - 1) * scalingPerLevel) * multiplier
        return rawAmount.toInt().coerceAtLeast(0)
    }
}

/**
 * Data-driven rule defining daily upkeep costs (money and/or resources) for maintaining a building.
 */
data class BuildingUpkeepRule(
    val buildingType: BuildingType,
    val baseMoneyUpkeepPerDay: Int = 0,
    val moneyScalingPerLevel: Int = 0,
    val resourceUpkeepPerDay: Map<ResourceType, Int> = emptyMap(),
    val requiredStatus: BuildingStatus = BuildingStatus.OPERATIONAL
) {
    /**
     * Calculates financial maintenance cost for the building at given level.
     */
    fun calculateMoneyUpkeep(buildingLevel: Int, discountMultiplier: Float = 1.0f): Int {
        if (buildingLevel <= 0) return 0
        val raw = (baseMoneyUpkeepPerDay + (buildingLevel - 1) * moneyScalingPerLevel) * discountMultiplier
        return raw.toInt().coerceAtLeast(0)
    }

    /**
     * Calculates physical resource maintenance costs (e.g. generator fuel consumption).
     */
    fun calculateResourceUpkeep(buildingLevel: Int, efficiencyReductionPercent: Int = 0): Map<ResourceType, Int> {
        if (buildingLevel <= 0) return emptyMap()
        val efficiencyFactor = (1.0f - (efficiencyReductionPercent / 100f)).coerceIn(0.1f, 1.0f)
        return resourceUpkeepPerDay.mapValues { (_, baseAmount) ->
            val totalForLevel = baseAmount + ((buildingLevel - 1) * (baseAmount / 2).coerceAtLeast(1))
            (totalForLevel * efficiencyFactor).toInt().coerceAtLeast(1)
        }
    }
}

/**
 * Severity level of economic deficits or resource shortages.
 */
enum class ShortageSeverity(val titleRu: String) {
    INFO("Информация"),
    WARNING("Предупреждение"),
    CRITICAL("Критическая угроза")
}

/**
 * Types of economic resource shortages registered during daily settlement ticks.
 */
enum class EconomicShortageType(val titleRu: String, val severity: ShortageSeverity) {
    FOOD_SHORTAGE("Дефицит провизии", ShortageSeverity.CRITICAL),
    WATER_SHORTAGE("Дефицит питьевой воды", ShortageSeverity.CRITICAL),
    FUEL_SHORTAGE("Нехватка генераторного топлива", ShortageSeverity.WARNING),
    MONEY_SHORTAGE("Нехватка средств на содержание", ShortageSeverity.WARNING),
    MATERIALS_SHORTAGE("Нехватка стройматериалов", ShortageSeverity.INFO),
    MEDICINE_SHORTAGE("Нехватка медикаментов", ShortageSeverity.WARNING)
}

/**
 * Record of an unresolved or partially resolved resource shortage.
 */
data class EconomicDeficit(
    val type: EconomicShortageType,
    val requestedAmount: Int,
    val availableAmount: Int,
    val deficitAmount: Int,
    val sourceRu: String,
    val gameDay: Int
)

/**
 * Health status classification for a resource supply trend.
 */
enum class ResourceEconomicStatus(val titleRu: String) {
    SURPLUS("Профицит"),
    STABLE("Стабильно"),
    DEFICIT("Дефицит"),
    CRITICAL("Критично")
}

/**
 * Immutable report generated at the conclusion of a daily economic cycle.
 */
data class DailyEconomyReport(
    val day: Int,
    val previousDay: Int = day - 1,
    val producedResources: Map<ResourceType, Int> = emptyMap(),
    val consumedResources: Map<ResourceType, Int> = emptyMap(),
    val overflowLost: Map<ResourceType, Int> = emptyMap(),
    val incomeCredits: Int = 0,
    val expenseCredits: Int = 0,
    val netMoneyChange: Int = 0,
    val incomeBreakdown: Map<String, Int> = emptyMap(),
    val expenseBreakdown: Map<String, Int> = emptyMap(),
    val shortages: List<EconomicDeficit> = emptyList(),
    val storageOccupancyAfter: Int = 0,
    val storageCapacity: Int = 0,
    val activeModifiersSummary: List<String> = emptyList(),
    val residentsInSettlementCount: Int = 0,
    val residentsOnExpeditionCount: Int = 0,
    val summaryLogs: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
) {
    val hasCriticalShortage: Boolean
        get() = shortages.any { it.type.severity == ShortageSeverity.CRITICAL }

    val totalProducedUnits: Int
        get() = producedResources.values.sum()

    val totalConsumedUnits: Int
        get() = consumedResources.values.sum()

    val totalOverflowLostUnits: Int
        get() = overflowLost.values.sum()
}

/**
 * Detailed breakdown per resource used for daily forecast projection.
 */
data class ResourceForecastDetail(
    val resourceType: ResourceType,
    val currentStock: Int,
    val dailyProduction: Int,
    val dailyConsumption: Int,
    val netDailyDelta: Int = dailyProduction - dailyConsumption,
    val daysOfSupply: Float,
    val status: ResourceEconomicStatus,
    val productionSources: List<Pair<String, Int>> = emptyList(),
    val consumptionSources: List<Pair<String, Int>> = emptyList()
)

/**
 * Pure projection of the upcoming day's economy (no state mutation).
 */
data class EconomyForecast(
    val day: Int,
    val resourceDetails: Map<ResourceType, ResourceForecastDetail>,
    val expectedIncome: Int,
    val incomeSources: List<Pair<String, Int>>,
    val expectedExpenses: Int,
    val expenseSources: List<Pair<String, Int>>,
    val netMoneyForecast: Int,
    val criticalWarnings: List<String>,
    val residentsInSettlement: Int,
    val residentsOnExpedition: Int,
    val projectedStorageOccupancy: Int,
    val storageCapacity: Int
) {
    val isAnyCritical: Boolean
        get() = criticalWarnings.isNotEmpty() || resourceDetails.values.any { it.status == ResourceEconomicStatus.CRITICAL }
}
