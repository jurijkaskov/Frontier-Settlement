package com.example

import com.example.data.EconomyBalanceConfig
import com.example.data.InitialGameData
import com.example.domain.model.*
import com.example.domain.service.economy.SettlementEconomyProcessor
import com.example.domain.service.time.DailyTickProcessor
import org.junit.Assert.*
import org.junit.Test

/**
 * Verification test suite for Point 27: Settlement Economy & Accounting System.
 */
class SettlementEconomySystemTest {

    @Test
    fun testForecastCalculation_isPureAndAccurate() {
        val initialGameState = InitialGameData.createInitialGameState()
        val forecast = SettlementEconomyProcessor.calculateDailyEconomyForecast(initialGameState)

        assertNotNull(forecast)
        assertTrue("Base commerce should provide positive income", forecast.expectedIncome > 0)
        assertEquals("Total residents should match initial characters", initialGameState.characters.size, forecast.residentsInSettlement + forecast.residentsOnExpedition)
        assertEquals(initialGameState.characters.size, forecast.residentsInSettlement)
        assertEquals(0, forecast.residentsOnExpedition)

        // Verify food, water, and fuel forecast details exist
        assertTrue(forecast.resourceDetails.containsKey(ResourceType.FOOD))
        assertTrue(forecast.resourceDetails.containsKey(ResourceType.WATER))
        assertTrue(forecast.resourceDetails.containsKey(ResourceType.FUEL))
    }

    @Test
    fun testExpeditionExclusionFromSettlementConsumption() {
        val initialGameState = InitialGameData.createInitialGameState()
        val char1 = initialGameState.characters.first()
        val char2 = initialGameState.characters.drop(1).first()

        // Create an active expedition with 2 members
        val activeExpedition = Expedition(
            id = "exp_test",
            location = initialGameState.locations.first(),
            squad = listOf(char1, char2),
            vehicle = initialGameState.vehicles.first(),
            leaderId = char1.id,
            status = ExpeditionStatus.TRAVELING,
            phase = ExpeditionPhase.TRAVELING_TO_LOCATION,
            supplies = mapOf(ResourceType.FOOD to 10, ResourceType.WATER to 10)
        )

        val stateWithExp = initialGameState.copy(activeExpedition = activeExpedition)
        val forecast = SettlementEconomyProcessor.calculateDailyEconomyForecast(stateWithExp)

        assertEquals(2, forecast.residentsOnExpedition)
        assertEquals(initialGameState.characters.size - 2, forecast.residentsInSettlement)

        val expectedFoodConsumption = (initialGameState.characters.size - 2) * EconomyBalanceConfig.BASE_FOOD_PER_RESIDENT_DAY.toInt()
        val foodDetail = forecast.resourceDetails[ResourceType.FOOD]
        assertNotNull(foodDetail)
        assertEquals(expectedFoodConsumption, foodDetail?.dailyConsumption)
    }

    @Test
    fun testProcessDailyEconomy_productionAndConsumptionAppliedCorrectly() {
        val baseState = InitialGameData.createInitialGameState()
        val state = baseState.copy(
            gameDateTime = GameDateTime(day = 1, hour = 0, minute = 0),
            resources = GameResources(
                food = 50,
                water = 50,
                fuel = 30,
                materials = 20,
                money = 100,
                warehouseMaxCapacity = 500
            ),
            inventoryItems = emptyList()
        )

        val (nextState, report) = SettlementEconomyProcessor.processDailyEconomy(state, dayNumber = 2)

        assertEquals(2, report.day)
        assertTrue("Report must record produced units", report.totalProducedUnits >= 0)
        assertTrue("Report must record consumed units", report.totalConsumedUnits > 0)
        assertNotNull(nextState.lastEconomyReport)
        assertEquals(report, nextState.lastEconomyReport)
        assertEquals(1, nextState.economyReports.size)
    }

    @Test
    fun testProcessDailyEconomy_warehouseOverflowLostTracked() {
        val baseState = InitialGameData.createInitialGameState()
        // Create state where warehouse starts with empty items and small cap
        val state = baseState.copy(
            gameDateTime = GameDateTime(day = 1, hour = 0, minute = 0),
            inventoryItems = emptyList(),
            resources = GameResources(
                food = 10,
                water = 10,
                fuel = 10,
                materials = 10,
                money = 100,
                warehouseMaxCapacity = 45 // Small cap (currently holding 40 physical)
            ),
            settlement = baseState.settlement.copy(
                buildings = listOf(
                    Building(
                        id = "bld_farm",
                        name = "Гидропонная ферма",
                        type = BuildingType.HYDROPONICS_FARM,
                        category = BuildingCategory.PRODUCTION,
                        level = 5,
                        maxLevel = 5,
                        description = "Ферма",
                        status = BuildingStatus.OPERATIONAL,
                        requiredSettlementLevel = 1,
                        buildCostMaterials = 100,
                        buildCostMoney = 100,
                        upgradeCostMaterials = 100,
                        upgradeCostMoney = 100,
                        xpRewardOnBuild = 50,
                        xpRewardOnUpgrade = 50,
                        dailyProductionDescription = "+25 Еды / день"
                    )
                )
            )
        )

        val (nextState, report) = SettlementEconomyProcessor.processDailyEconomy(state, dayNumber = 2)

        assertTrue(
            "Warehouse should enforce maximum capacity cap",
            nextState.totalWarehouseOccupiedVolume <= nextState.resources.warehouseMaxCapacity
        )
        assertTrue("Overflow of food must be recorded", report.overflowLost.containsKey(ResourceType.FOOD))
        assertTrue("Overflow lost amount must be greater than zero", report.overflowLost.getValue(ResourceType.FOOD) > 0)
    }

    @Test
    fun testProcessDailyEconomy_shortageDoesNotGoNegative() {
        val baseState = InitialGameData.createInitialGameState()
        val state = baseState.copy(
            gameDateTime = GameDateTime(day = 1, hour = 0, minute = 0),
            inventoryItems = emptyList(),
            resources = GameResources(
                food = 0,
                water = 0,
                fuel = 0,
                materials = 0,
                money = 0,
                warehouseMaxCapacity = 100
            )
        )

        val (nextState, report) = SettlementEconomyProcessor.processDailyEconomy(state, dayNumber = 2)

        // Resources must never fall below 0
        assertTrue(nextState.resources.food >= 0)
        assertTrue(nextState.resources.water >= 0)
        assertTrue(nextState.resources.fuel >= 0)
        assertTrue(nextState.resources.money >= 0)

        // Shortages must be recorded
        assertTrue(report.shortages.isNotEmpty())
        val foodDeficit = report.shortages.find { it.type == EconomicShortageType.FOOD_SHORTAGE }
        assertNotNull(foodDeficit)
        assertTrue((foodDeficit?.deficitAmount ?: 0) > 0)
    }

    @Test
    fun testDailyTickProcessor_integrationWithEconomy() {
        val initialState = InitialGameData.createInitialGameState().copy(
            gameDateTime = GameDateTime(day = 1, hour = 0, minute = 0),
            processedDays = emptySet()
        )
        val (nextDayState, summary) = DailyTickProcessor.processSingleDay(initialState, dayNumber = 2)

        assertNotNull(nextDayState.lastEconomyReport)
        assertEquals(2, nextDayState.lastEconomyReport?.day)
        assertEquals(1, nextDayState.economyReports.size)
        assertNotNull(summary.economyReport)
        assertEquals(2, summary.economyReport?.day)
    }
}
