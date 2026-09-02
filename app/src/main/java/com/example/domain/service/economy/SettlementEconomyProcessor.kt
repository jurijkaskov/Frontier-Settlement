package com.example.domain.service.economy

import com.example.data.EconomyBalanceConfig
import com.example.data.ResearchConfig
import com.example.domain.model.*

/**
 * Centralized domain processor for settlement economy, resource production/consumption,
 * financial accounting, shortage detection, and storage capacity constraints.
 */
object SettlementEconomyProcessor {

    /**
     * Pure calculation of upcoming daily economy projection.
     * Guaranteed side-effect free: does NOT mutate or modify GameState.
     */
    fun calculateDailyEconomyForecast(state: GameState): EconomyForecast {
        val res = state.resources
        val sett = state.settlement
        val techs = state.technologies

        val expeditionMemberIds = if (state.activeExpedition != null &&
            state.activeExpedition.status != ExpeditionStatus.COMPLETED &&
            state.activeExpedition.status != ExpeditionStatus.FAILED) {
            state.activeExpedition.squad.map { it.id }.toSet()
        } else emptySet()

        val residentsOnExpedition = state.characters.count {
            it.status == CharacterStatus.ON_EXPEDITION || expeditionMemberIds.contains(it.id)
        }
        val residentsInBase = (state.characters.size - residentsOnExpedition).coerceAtLeast(0)

        // 1. Calculate Production Projections
        val prodMap = mutableMapOf<ResourceType, Int>()
        val prodSources = mutableMapOf<ResourceType, MutableList<Pair<String, Int>>>()

        sett.buildings.filter { it.status == BuildingStatus.OPERATIONAL && it.isConstructed }.forEach { bld ->
            val rules = EconomyBalanceConfig.productionRules.filter { it.buildingType == bld.type }
            rules.forEach { rule ->
                val multiplier = ResearchConfig.getProductionMultiplier(techs, rule.resourceType)
                val amount = rule.calculateProduction(bld.level, multiplier)
                if (amount > 0) {
                    prodMap[rule.resourceType] = (prodMap[rule.resourceType] ?: 0) + amount
                    val sourceList = prodSources.getOrPut(rule.resourceType) { mutableListOf() }
                    val label = "${bld.name} (Ур. ${bld.level})"
                    sourceList.add(label to amount)
                }
            }
        }

        // 2. Calculate Consumption Projections
        val consMap = mutableMapOf<ResourceType, Int>()
        val consSources = mutableMapOf<ResourceType, MutableList<Pair<String, Int>>>()

        // Food & Water consumption based on actual residents at base
        val foodNeeded = (residentsInBase * EconomyBalanceConfig.BASE_FOOD_PER_RESIDENT_DAY).toInt()
        val waterNeeded = (residentsInBase * EconomyBalanceConfig.BASE_WATER_PER_RESIDENT_DAY).toInt()

        if (foodNeeded > 0) {
            consMap[ResourceType.FOOD] = foodNeeded
            consSources.getOrPut(ResourceType.FOOD) { mutableListOf() }
                .add("Жители базы ($residentsInBase чел.)" to foodNeeded)
        }

        if (waterNeeded > 0) {
            consMap[ResourceType.WATER] = waterNeeded
            consSources.getOrPut(ResourceType.WATER) { mutableListOf() }
                .add("Жители базы ($residentsInBase чел.)" to waterNeeded)
        }

        // Fuel & Building Resource Upkeep
        val fuelEfficiency = ResearchConfig.getFuelEfficiencyPercent(techs)
        sett.buildings.filter { it.status == BuildingStatus.OPERATIONAL && it.isConstructed }.forEach { bld ->
            val upkeepRule = EconomyBalanceConfig.upkeepRules.find { it.buildingType == bld.type }
            if (upkeepRule != null) {
                val resUpkeep = upkeepRule.calculateResourceUpkeep(bld.level, fuelEfficiency)
                resUpkeep.forEach { (resType, amount) ->
                    if (amount > 0) {
                        consMap[resType] = (consMap[resType] ?: 0) + amount
                        consSources.getOrPut(resType) { mutableListOf() }
                            .add("${bld.name} (Ур. ${bld.level})" to amount)
                    }
                }
            }
        }

        // 3. Calculate Financial Incomes
        var totalIncome = EconomyBalanceConfig.BASE_SETTLEMENT_COMMERCE_CREDITS
        val incomeList = mutableListOf<Pair<String, Int>>(
            "Торговый оборот базы" to EconomyBalanceConfig.BASE_SETTLEMENT_COMMERCE_CREDITS
        )

        sett.buildings.filter { it.status == BuildingStatus.OPERATIONAL && it.isConstructed }.forEach { bld ->
            when (bld.type) {
                BuildingType.HQ_COMMAND -> {
                    val gain = EconomyBalanceConfig.HQ_BASE_INCOME_CREDITS * bld.level
                    totalIncome += gain
                    incomeList.add("${bld.name} (Ур. ${bld.level})" to gain)
                }
                BuildingType.TRADING_POST -> {
                    val gain = EconomyBalanceConfig.TRADING_POST_BASE_INCOME_CREDITS * bld.level
                    totalIncome += gain
                    incomeList.add("${bld.name} (Ур. ${bld.level})" to gain)
                }
                else -> {}
            }
        }

        // 4. Calculate Financial Expenses (Building Maintenance)
        var totalExpenses = 0
        val expenseList = mutableListOf<Pair<String, Int>>()

        sett.buildings.filter { it.status == BuildingStatus.OPERATIONAL && it.isConstructed }.forEach { bld ->
            val upkeepRule = EconomyBalanceConfig.upkeepRules.find { it.buildingType == bld.type }
            if (upkeepRule != null) {
                val cost = upkeepRule.calculateMoneyUpkeep(bld.level)
                if (cost > 0) {
                    totalExpenses += cost
                    expenseList.add("Содержание: ${bld.name}" to cost)
                }
            }
        }

        // 5. Evaluate Individual Resource Forecast Details
        val criticalWarnings = mutableListOf<String>()
        val resourceDetails = mutableMapOf<ResourceType, ResourceForecastDetail>()

        val trackedTypes = listOf(
            ResourceType.FOOD,
            ResourceType.WATER,
            ResourceType.FUEL,
            ResourceType.MATERIALS,
            ResourceType.MEDICINE
        )

        trackedTypes.forEach { type ->
            val stock = res[type]
            val prod = prodMap[type] ?: 0
            val cons = consMap[type] ?: 0
            val (days, status) = EconomyBalanceConfig.evaluateSupplyStatus(stock, cons, prod)

            if (status == ResourceEconomicStatus.CRITICAL) {
                if (type == ResourceType.FOOD) {
                    criticalWarnings.add("Критический запас провизии: осталось на ${String.format("%.1f", days)} дн.!")
                } else if (type == ResourceType.WATER) {
                    criticalWarnings.add("Критический запас воды: осталось на ${String.format("%.1f", days)} дн.!")
                } else if (type == ResourceType.FUEL) {
                    criticalWarnings.add("Топливо генератора на исходе: осталось на ${String.format("%.1f", days)} дн.!")
                }
            }

            resourceDetails[type] = ResourceForecastDetail(
                resourceType = type,
                currentStock = stock,
                dailyProduction = prod,
                dailyConsumption = cons,
                daysOfSupply = days,
                status = status,
                productionSources = prodSources[type] ?: emptyList(),
                consumptionSources = consSources[type] ?: emptyList()
            )
        }

        val netMoney = totalIncome - totalExpenses
        if (res.money + netMoney < 0) {
            criticalWarnings.add("Дефицит бюджета: расходов больше, чем средств в казне!")
        }

        val currentStored = state.totalWarehouseOccupiedVolume
        val projectedPhysicalGain = prodMap.filter { it.key.isPhysical }.values.sum()
        val projectedPhysicalLoss = consMap.filter { it.key.isPhysical }.values.sum()
        val projectedStorage = (currentStored + projectedPhysicalGain - projectedPhysicalLoss).coerceAtLeast(0)

        return EconomyForecast(
            day = state.day,
            resourceDetails = resourceDetails,
            expectedIncome = totalIncome,
            incomeSources = incomeList,
            expectedExpenses = totalExpenses,
            expenseSources = expenseList,
            netMoneyForecast = netMoney,
            criticalWarnings = criticalWarnings,
            residentsInSettlement = residentsInBase,
            residentsOnExpedition = residentsOnExpedition,
            projectedStorageOccupancy = projectedStorage,
            storageCapacity = res.warehouseMaxCapacity
        )
    }

    /**
     * Executes the atomic daily economic processing step for [dayNumber].
     * Computes production, verifies storage limits (overflows), deducts consumption,
     * settles building upkeeps and commerce incomes, and produces a structured [DailyEconomyReport].
     */
    fun processDailyEconomy(
        state: GameState,
        dayNumber: Int
    ): Pair<GameState, DailyEconomyReport> {
        val res = state.resources
        val sett = state.settlement
        val techs = state.technologies

        val expeditionMemberIds = if (state.activeExpedition != null &&
            state.activeExpedition.status != ExpeditionStatus.COMPLETED &&
            state.activeExpedition.status != ExpeditionStatus.FAILED) {
            state.activeExpedition.squad.map { it.id }.toSet()
        } else emptySet()

        val residentsOnExpedition = state.characters.count {
            it.status == CharacterStatus.ON_EXPEDITION || expeditionMemberIds.contains(it.id)
        }
        val residentsInBase = (state.characters.size - residentsOnExpedition).coerceAtLeast(0)

        val activeModifiersSummary = mutableListOf<String>()

        // 1. Calculate Daily Production
        val produced = mutableMapOf<ResourceType, Int>()
        val overflowLost = mutableMapOf<ResourceType, Int>()

        sett.buildings.filter { it.status == BuildingStatus.OPERATIONAL && it.isConstructed }.forEach { bld ->
            val rules = EconomyBalanceConfig.productionRules.filter { it.buildingType == bld.type }
            rules.forEach { rule ->
                val multiplier = ResearchConfig.getProductionMultiplier(techs, rule.resourceType)
                if (multiplier > 1.0f) {
                    val bonusPercent = ((multiplier - 1.0f) * 100).toInt()
                    val bonusDesc = "${rule.resourceType.titleRu}: +$bonusPercent% от технологий"
                    if (!activeModifiersSummary.contains(bonusDesc)) {
                        activeModifiersSummary.add(bonusDesc)
                    }
                }
                val amount = rule.calculateProduction(bld.level, multiplier)
                if (amount > 0) {
                    produced[rule.resourceType] = (produced[rule.resourceType] ?: 0) + amount
                }
            }
        }

        // 2. Calculate Daily Consumption & Building Resource Upkeep
        val consumed = mutableMapOf<ResourceType, Int>()

        // Food & Water consumption (Only residents currently in settlement consume home stocks)
        val foodNeeded = (residentsInBase * EconomyBalanceConfig.BASE_FOOD_PER_RESIDENT_DAY).toInt()
        val waterNeeded = (residentsInBase * EconomyBalanceConfig.BASE_WATER_PER_RESIDENT_DAY).toInt()

        if (foodNeeded > 0) consumed[ResourceType.FOOD] = foodNeeded
        if (waterNeeded > 0) consumed[ResourceType.WATER] = waterNeeded

        val fuelEfficiency = ResearchConfig.getFuelEfficiencyPercent(techs)
        if (fuelEfficiency > 0) {
            activeModifiersSummary.add("Топливо генератора: -$fuelEfficiency% к расходу")
        }

        sett.buildings.filter { it.status == BuildingStatus.OPERATIONAL && it.isConstructed }.forEach { bld ->
            val upkeepRule = EconomyBalanceConfig.upkeepRules.find { it.buildingType == bld.type }
            if (upkeepRule != null) {
                val resUpkeep = upkeepRule.calculateResourceUpkeep(bld.level, fuelEfficiency)
                resUpkeep.forEach { (resType, amount) ->
                    if (amount > 0) {
                        consumed[resType] = (consumed[resType] ?: 0) + amount
                    }
                }
            }
        }

        // 3. Shortage Detection & Consumption Deduction
        val shortages = mutableListOf<EconomicDeficit>()

        var currentFood = res.food
        var currentWater = res.water
        var currentFuel = res.fuel
        var currentMaterials = res.materials
        val extraRes = res.extraResources.toMutableMap()

        // Deduct Food
        val foodDemand = consumed[ResourceType.FOOD] ?: 0
        if (currentFood < foodDemand) {
            val shortage = foodDemand - currentFood
            shortages.add(
                EconomicDeficit(
                    type = EconomicShortageType.FOOD_SHORTAGE,
                    requestedAmount = foodDemand,
                    availableAmount = currentFood,
                    deficitAmount = shortage,
                    sourceRu = "Жители поселения",
                    gameDay = dayNumber
                )
            )
            currentFood = 0
        } else {
            currentFood -= foodDemand
        }

        // Deduct Water
        val waterDemand = consumed[ResourceType.WATER] ?: 0
        if (currentWater < waterDemand) {
            val shortage = waterDemand - currentWater
            shortages.add(
                EconomicDeficit(
                    type = EconomicShortageType.WATER_SHORTAGE,
                    requestedAmount = waterDemand,
                    availableAmount = currentWater,
                    deficitAmount = shortage,
                    sourceRu = "Жители поселения",
                    gameDay = dayNumber
                )
            )
            currentWater = 0
        } else {
            currentWater -= waterDemand
        }

        // Deduct Fuel
        val fuelDemand = consumed[ResourceType.FUEL] ?: 0
        if (currentFuel < fuelDemand) {
            val shortage = fuelDemand - currentFuel
            shortages.add(
                EconomicDeficit(
                    type = EconomicShortageType.FUEL_SHORTAGE,
                    requestedAmount = fuelDemand,
                    availableAmount = currentFuel,
                    deficitAmount = shortage,
                    sourceRu = "Дизель-генератор",
                    gameDay = dayNumber
                )
            )
            currentFuel = 0
        } else {
            currentFuel -= fuelDemand
        }

        // 4. Apply Production with Storage Capacity Constraints
        // Current physical volume after consumption
        var runningStoredVolume = currentFood + currentWater + currentFuel + currentMaterials +
                extraRes.filter { it.key.isPhysical }.values.sum() + state.totalInventoryVolume
        val maxCapacity = res.warehouseMaxCapacity

        // Add produced food
        val foodProduced = produced[ResourceType.FOOD] ?: 0
        if (foodProduced > 0) {
            val canFit = (maxCapacity - runningStoredVolume).coerceAtLeast(0)
            val added = foodProduced.coerceAtMost(canFit)
            val lost = foodProduced - added
            currentFood += added
            runningStoredVolume += added
            if (lost > 0) overflowLost[ResourceType.FOOD] = lost
        }

        // Add produced water
        val waterProduced = produced[ResourceType.WATER] ?: 0
        if (waterProduced > 0) {
            val canFit = (maxCapacity - runningStoredVolume).coerceAtLeast(0)
            val added = waterProduced.coerceAtMost(canFit)
            val lost = waterProduced - added
            currentWater += added
            runningStoredVolume += added
            if (lost > 0) overflowLost[ResourceType.WATER] = lost
        }

        // Add produced materials
        val matProduced = produced[ResourceType.MATERIALS] ?: 0
        if (matProduced > 0) {
            val canFit = (maxCapacity - runningStoredVolume).coerceAtLeast(0)
            val added = matProduced.coerceAtMost(canFit)
            val lost = matProduced - added
            currentMaterials += added
            runningStoredVolume += added
            if (lost > 0) overflowLost[ResourceType.MATERIALS] = lost
        }

        // Add produced extra resources (e.g. Medicine from bio-dome)
        val medProduced = produced[ResourceType.MEDICINE] ?: 0
        if (medProduced > 0) {
            val canFit = (maxCapacity - runningStoredVolume).coerceAtLeast(0)
            val added = medProduced.coerceAtMost(canFit)
            val lost = medProduced - added
            extraRes[ResourceType.MEDICINE] = (extraRes[ResourceType.MEDICINE] ?: 0) + added
            runningStoredVolume += added
            if (lost > 0) overflowLost[ResourceType.MEDICINE] = lost
        }

        // 5. Calculate Financial Settlement (Incomes vs Building Upkeeps)
        var totalIncome = EconomyBalanceConfig.BASE_SETTLEMENT_COMMERCE_CREDITS
        val incomeBreakdown = mutableMapOf(
            "Торговый оборот базы" to EconomyBalanceConfig.BASE_SETTLEMENT_COMMERCE_CREDITS
        )

        sett.buildings.filter { it.status == BuildingStatus.OPERATIONAL && it.isConstructed }.forEach { bld ->
            when (bld.type) {
                BuildingType.HQ_COMMAND -> {
                    val gain = EconomyBalanceConfig.HQ_BASE_INCOME_CREDITS * bld.level
                    totalIncome += gain
                    incomeBreakdown[bld.name] = gain
                }
                BuildingType.TRADING_POST -> {
                    val gain = EconomyBalanceConfig.TRADING_POST_BASE_INCOME_CREDITS * bld.level
                    totalIncome += gain
                    incomeBreakdown[bld.name] = gain
                }
                else -> {}
            }
        }

        var totalExpenses = 0
        val expenseBreakdown = mutableMapOf<String, Int>()

        sett.buildings.filter { it.status == BuildingStatus.OPERATIONAL && it.isConstructed }.forEach { bld ->
            val upkeepRule = EconomyBalanceConfig.upkeepRules.find { it.buildingType == bld.type }
            if (upkeepRule != null) {
                val cost = upkeepRule.calculateMoneyUpkeep(bld.level)
                if (cost > 0) {
                    totalExpenses += cost
                    expenseBreakdown[bld.name] = cost
                }
            }
        }

        // Deduct expenses & apply income
        var currentMoney = res.money + totalIncome
        if (currentMoney < totalExpenses) {
            val deficit = totalExpenses - currentMoney
            shortages.add(
                EconomicDeficit(
                    type = EconomicShortageType.MONEY_SHORTAGE,
                    requestedAmount = totalExpenses,
                    availableAmount = currentMoney,
                    deficitAmount = deficit,
                    sourceRu = "Содержание инфраструктуры",
                    gameDay = dayNumber
                )
            )
            currentMoney = 0
        } else {
            currentMoney -= totalExpenses
        }

        val netMoneyChange = totalIncome - totalExpenses

        // 6. Build Daily Economy Report & Summary Logs
        val summaryLogs = mutableListOf<String>()
        val prodSummary = buildString {
            append("Производство: ")
            val parts = mutableListOf<String>()
            produced.forEach { (type, amount) ->
                parts.add("+${amount} ${type.titleRu}")
            }
            append(if (parts.isNotEmpty()) parts.joinToString(", ") else "Нет")
        }
        summaryLogs.add(prodSummary)

        val consSummary = buildString {
            append("Потребление: ")
            val parts = mutableListOf<String>()
            consumed.forEach { (type, amount) ->
                parts.add("-${amount} ${type.titleRu}")
            }
            append(if (parts.isNotEmpty()) parts.joinToString(", ") else "Нет")
        }
        summaryLogs.add(consSummary)

        summaryLogs.add("Финансы: +$totalIncome Кр. доход, -$totalExpenses Кр. расходы (Итог: ${if (netMoneyChange >= 0) "+$netMoneyChange" else "$netMoneyChange"} Кр.)")

        if (overflowLost.isNotEmpty()) {
            val lostDesc = overflowLost.entries.joinToString(", ") { "-${it.value} ${it.key.titleRu}" }
            summaryLogs.add("Склад переполнен! Потеряно из-за нехватки места: $lostDesc")
        }

        if (shortages.isNotEmpty()) {
            shortages.forEach { def ->
                summaryLogs.add("⚠️ ${def.type.titleRu}: не хватило ${def.deficitAmount} ед. (${def.sourceRu})")
            }
        }

        val report = DailyEconomyReport(
            day = dayNumber,
            previousDay = dayNumber - 1,
            producedResources = produced,
            consumedResources = consumed,
            overflowLost = overflowLost,
            incomeCredits = totalIncome,
            expenseCredits = totalExpenses,
            netMoneyChange = netMoneyChange,
            incomeBreakdown = incomeBreakdown,
            expenseBreakdown = expenseBreakdown,
            shortages = shortages,
            storageOccupancyAfter = runningStoredVolume,
            storageCapacity = maxCapacity,
            activeModifiersSummary = activeModifiersSummary,
            residentsInSettlementCount = residentsInBase,
            residentsOnExpeditionCount = residentsOnExpedition,
            summaryLogs = summaryLogs
        )

        // 7. Update GameResources & Return Immutable Pair
        val updatedResources = res.copy(
            money = currentMoney,
            food = currentFood,
            water = currentWater,
            fuel = currentFuel,
            materials = currentMaterials,
            extraResources = extraRes
        )

        val updatedEconomyReports = (listOf(report) + state.economyReports).take(EconomyBalanceConfig.MAX_ECONOMY_REPORTS_HISTORY)

        val nextState = state.copy(
            resources = updatedResources,
            economyReports = updatedEconomyReports,
            lastEconomyReport = report,
            unpaidDeficits = (state.unpaidDeficits + shortages).takeLast(20)
        )

        return Pair(nextState, report)
    }
}
