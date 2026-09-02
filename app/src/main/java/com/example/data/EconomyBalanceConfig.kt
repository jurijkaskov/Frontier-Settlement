package com.example.data

import com.example.domain.model.*

/**
 * Centralized configuration for Settlement Economy balance, consumption rates,
 * building production definitions, and upkeep rules.
 */
object EconomyBalanceConfig {

    // --- Base Resident Consumption Rates ---
    const val BASE_FOOD_PER_RESIDENT_DAY: Float = 1.0f
    const val BASE_WATER_PER_RESIDENT_DAY: Float = 1.0f

    // --- Base Baseline Outpost Incomes & Operations ---
    const val BASE_SETTLEMENT_COMMERCE_CREDITS: Int = 20
    const val HQ_BASE_INCOME_CREDITS: Int = 25
    const val TRADING_POST_BASE_INCOME_CREDITS: Int = 35

    // --- Critical Supply Thresholds (in Days) ---
    const val DAYS_SUPPLY_CRITICAL: Float = 1.5f
    const val DAYS_SUPPLY_DEFICIT: Float = 3.5f
    const val DAYS_SUPPLY_SURPLUS: Float = 7.0f

    // Maximum daily economy reports history retained in memory/save state
    const val MAX_ECONOMY_REPORTS_HISTORY: Int = 30

    /**
     * Data-driven registry of production rules by building type.
     */
    val productionRules: List<ProductionRule> = listOf(
        // Hydroponics Farm: Primary Food source
        ProductionRule(
            buildingType = BuildingType.HYDROPONICS_FARM,
            resourceType = ResourceType.FOOD,
            baseAmountPerDay = 25,
            scalingPerLevel = 25,
            description = "Гидропонные кассеты питательной зелени и злаков"
        ),
        // Greenhouse Complex: Advanced Food + Herbal Medicine
        ProductionRule(
            buildingType = BuildingType.GREENHOUSE_COMPLEX,
            resourceType = ResourceType.FOOD,
            baseAmountPerDay = 40,
            scalingPerLevel = 40,
            description = "Биокупольное агропроизводство"
        ),
        ProductionRule(
            buildingType = BuildingType.GREENHOUSE_COMPLEX,
            resourceType = ResourceType.MEDICINE,
            baseAmountPerDay = 5,
            scalingPerLevel = 5,
            description = "Выращивание целебных культур и сырья для стимуляторов"
        ),
        // Water Extractor: Deep aquifer artesian filter
        ProductionRule(
            buildingType = BuildingType.WATER_EXTRACTOR,
            resourceType = ResourceType.WATER,
            baseAmountPerDay = 30,
            scalingPerLevel = 30,
            description = "Артезианская фильтровальная колонна"
        ),
        // Workshop: Materials smelting and scrap salvage
        ProductionRule(
            buildingType = BuildingType.WORKSHOP,
            resourceType = ResourceType.MATERIALS,
            baseAmountPerDay = 15,
            scalingPerLevel = 15,
            description = "Переплавка лома в стандартизированные стройматериалы"
        )
    )

    /**
     * Data-driven registry of building maintenance and upkeep rules.
     */
    val upkeepRules: List<BuildingUpkeepRule> = listOf(
        // HQ Command: Administrative & communication upkeep
        BuildingUpkeepRule(
            buildingType = BuildingType.HQ_COMMAND,
            baseMoneyUpkeepPerDay = 10,
            moneyScalingPerLevel = 5
        ),
        // Research Lab: High-tech instrumentation calibration & power
        BuildingUpkeepRule(
            buildingType = BuildingType.RESEARCH_LAB,
            baseMoneyUpkeepPerDay = 15,
            moneyScalingPerLevel = 5
        ),
        // Medical Clinic: Sanitation, power, sterile supplies
        BuildingUpkeepRule(
            buildingType = BuildingType.MEDICAL_CLINIC,
            baseMoneyUpkeepPerDay = 10,
            moneyScalingPerLevel = 5
        ),
        // Defense Perimeter: Turret sensors, spotlights, barricade repairs
        BuildingUpkeepRule(
            buildingType = BuildingType.DEFENSE_PERIMETER,
            baseMoneyUpkeepPerDay = 8,
            moneyScalingPerLevel = 4
        ),
        // Radio Tower: High-frequency transmitter power & parts
        BuildingUpkeepRule(
            buildingType = BuildingType.RADIO_TOWER,
            baseMoneyUpkeepPerDay = 10,
            moneyScalingPerLevel = 5
        ),
        // Armory Lab: Precision lathes and forge upkeep
        BuildingUpkeepRule(
            buildingType = BuildingType.ARMORY_LAB,
            baseMoneyUpkeepPerDay = 12,
            moneyScalingPerLevel = 6
        ),
        // Generator Station: Daily fuel consumption (5 base fuel/day)
        BuildingUpkeepRule(
            buildingType = BuildingType.GENERATOR_STATION,
            baseMoneyUpkeepPerDay = 5,
            moneyScalingPerLevel = 3,
            resourceUpkeepPerDay = mapOf(ResourceType.FUEL to 5)
        )
    )

    /**
     * Evaluates supply health status based on days remaining and daily delta.
     */
    fun evaluateSupplyStatus(
        currentStock: Int,
        dailyConsumption: Int,
        dailyProduction: Int
    ): Pair<Float, ResourceEconomicStatus> {
        if (dailyConsumption <= 0) {
            val days = if (currentStock > 0) 99f else 0f
            val status = if (currentStock > 0) ResourceEconomicStatus.SURPLUS else ResourceEconomicStatus.STABLE
            return Pair(days, status)
        }

        val daysOfSupply = currentStock.toFloat() / dailyConsumption.toFloat()
        val netDelta = dailyProduction - dailyConsumption

        val status = when {
            daysOfSupply <= DAYS_SUPPLY_CRITICAL -> ResourceEconomicStatus.CRITICAL
            daysOfSupply <= DAYS_SUPPLY_DEFICIT -> ResourceEconomicStatus.DEFICIT
            netDelta >= 0 && daysOfSupply >= DAYS_SUPPLY_SURPLUS -> ResourceEconomicStatus.SURPLUS
            else -> ResourceEconomicStatus.STABLE
        }

        return Pair(daysOfSupply, status)
    }
}
