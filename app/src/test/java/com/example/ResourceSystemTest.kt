package com.example

import com.example.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class ResourceSystemTest {

    @Test
    fun testStorageVolumeCalculation_MoneyOccupiesZeroStorage() {
        val resources = GameResources(
            money = 50000,
            food = 100,
            water = 100,
            fuel = 50,
            materials = 50,
            warehouseMaxCapacity = 500
        )

        // Stored volume should only count physical resources: 100 + 100 + 50 + 50 = 300
        assertEquals(300, resources.totalStoredVolume)
        assertEquals(200, resources.availableCapacity)
        assertFalse(resources.isStorageFull)
    }

    @Test
    fun testExtensibleResourcesStorageVolume() {
        val resources = GameResources(
            food = 50,
            water = 50,
            fuel = 20,
            materials = 30,
            warehouseMaxCapacity = 400,
            extraResources = mapOf(
                ResourceType.MEDICINE to 10,    // 10 * 1 = 10
                ResourceType.RARE_ALLOY to 20   // 20 * 2 = 40
            )
        )

        // Primary: 50 + 50 + 20 + 30 = 150
        // Extra: 10 + 40 = 50
        // Total: 200
        assertEquals(200, resources.totalStoredVolume)
        assertEquals(200, resources.availableCapacity)
    }

    @Test
    fun testSafeConsumption_PreventsNegativeValues() {
        val initial = GameResources(food = 30, water = 10)

        // Attempting to consume 50 food when only 30 available
        val (resultRes, opResult) = initial.consumeResourceSafe(ResourceType.FOOD, 50)

        assertFalse(opResult.isSuccess)
        assertTrue(opResult is ResourceOperationResult.Failure)
        assertEquals(ResourceOperationResult.FailureReason.INSUFFICIENT_RESOURCE, (opResult as ResourceOperationResult.Failure).reason)
        assertEquals(30, resultRes.food) // State remained unchanged
    }

    @Test
    fun testSafeConsumption_Successful() {
        val initial = GameResources(food = 100)
        val (resultRes, opResult) = initial.consumeResourceSafe(ResourceType.FOOD, 40)

        assertTrue(opResult.isSuccess)
        assertEquals(60, resultRes.food)
    }

    @Test
    fun testSafeAddition_FullFit() {
        val initial = GameResources(
            food = 100,
            water = 100,
            fuel = 50,
            materials = 50,
            warehouseMaxCapacity = 500
        ) // Currently using 300 / 500 -> 200 free

        val (resultRes, opResult) = initial.addResourceSafe(ResourceType.FOOD, 100)

        assertTrue(opResult.isSuccess)
        assertTrue(opResult is ResourceOperationResult.Success)
        assertEquals(200, resultRes.food)
        assertEquals(400, resultRes.totalStoredVolume)
    }

    @Test
    fun testSafeAddition_PartialOverflowWhenWarehouseFull() {
        val initial = GameResources(
            food = 100,
            water = 100,
            fuel = 50,
            materials = 50,
            warehouseMaxCapacity = 350
        ) // Currently using 300 / 350 -> only 50 free space

        // Attempt to add 150 food
        val (resultRes, opResult) = initial.addResourceSafe(ResourceType.FOOD, 150, allowPartial = true)

        assertTrue(opResult.isSuccess)
        assertTrue(opResult is ResourceOperationResult.PartialSuccess)

        val partial = opResult as ResourceOperationResult.PartialSuccess
        assertEquals(150, partial.requestedAmount)
        assertEquals(50, partial.actualAmountAdded)
        assertEquals(100, partial.rejectedAmount)

        assertEquals(150, resultRes.food) // 100 + 50
        assertEquals(350, resultRes.totalStoredVolume)
        assertTrue(resultRes.isStorageFull)
    }

    @Test
    fun testSafeAddition_StrictRejectionWhenAllowPartialFalse() {
        val initial = GameResources(
            food = 100,
            water = 100,
            fuel = 50,
            materials = 50,
            warehouseMaxCapacity = 350
        ) // 50 free space

        val (resultRes, opResult) = initial.addResourceSafe(ResourceType.FOOD, 150, allowPartial = false)

        assertFalse(opResult.isSuccess)
        assertTrue(opResult is ResourceOperationResult.Failure)
        assertEquals(ResourceOperationResult.FailureReason.INSUFFICIENT_STORAGE, (opResult as ResourceOperationResult.Failure).reason)
        assertEquals(100, resultRes.food) // Unchanged
    }

    @Test
    fun testAtomicBundleConsumption() {
        val initial = GameResources(
            money = 500,
            food = 100,
            materials = 150
        )

        // Case 1: Can afford bundle
        val costBundle = mapOf(
            ResourceType.MONEY to 200,
            ResourceType.FOOD to 40,
            ResourceType.MATERIALS to 50
        )
        val (res1, op1) = initial.consumeBundleSafe(costBundle)
        assertTrue(op1.isSuccess)
        assertEquals(300, res1.money)
        assertEquals(60, res1.food)
        assertEquals(100, res1.materials)

        // Case 2: Insufficient of one resource -> entire transaction aborted
        val expensiveBundle = mapOf(
            ResourceType.MONEY to 100,
            ResourceType.FOOD to 999
        )
        val (res2, op2) = initial.consumeBundleSafe(expensiveBundle)
        assertFalse(op2.isSuccess)
        assertEquals(500, res2.money) // Not subtracted
        assertEquals(100, res2.food)
    }

    @Test
    fun testDepletionLevels() {
        val res = GameResources(food = 15, water = 60)

        // With daily food consumption of 18, 15 food is CRITICAL
        val foodLevel = res.getDepletionLevel(ResourceType.FOOD, dailyConsumption = 18)
        assertEquals(ResourceStateLevel.CRITICAL, foodLevel)

        // 60 water with daily consumption of 20 is NORMAL (> 40)
        val waterLevel = res.getDepletionLevel(ResourceType.WATER, dailyConsumption = 20)
        assertEquals(ResourceStateLevel.NORMAL, waterLevel)
    }
}
