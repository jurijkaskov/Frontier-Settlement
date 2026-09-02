package com.example

import com.example.data.InitialGameData
import com.example.data.TravelCalculator
import com.example.domain.model.*
import com.example.viewmodel.GameViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TravelSystemTest {

    private lateinit var viewModel: GameViewModel
    private lateinit var initialState: GameState

    @Before
    fun setup() {
        viewModel = GameViewModel()
        initialState = InitialGameData.createInitialGameState()
    }

    @Test
    fun testTravelCostCalculationForDifferentModes() {
        val dest = initialState.locations.first { it.id == "loc_7" } // Старая ферма, 12 km

        // 1. Foot
        val footCost = TravelCalculator.calculateTravelCost(
            destination = dest,
            transportMode = TravelTransportMode.FOOT,
            participantCount = 2
        )
        assertTrue("Foot travel should require food", footCost.food > 0)
        assertTrue("Foot travel should require water", footCost.water > 0)
        assertEquals("Foot travel should not consume fuel", 0, footCost.fuel)
        assertEquals("Distance must match", dest.distanceKm, footCost.distanceKm)
        assertTrue("Duration should be greater than 1 hour", footCost.estimatedDurationHours >= 1.0f)

        // 2. Bicycle
        val bikeCost = TravelCalculator.calculateTravelCost(
            destination = dest,
            transportMode = TravelTransportMode.BICYCLE,
            participantCount = 2
        )
        assertEquals("Bicycle does not use fuel", 0, bikeCost.fuel)
        assertTrue("Bicycle should be faster than foot", bikeCost.estimatedDurationHours < footCost.estimatedDurationHours)
        assertTrue("Bicycle should consume less food than foot", bikeCost.food <= footCost.food)

        // 3. Buggy
        val buggyCost = TravelCalculator.calculateTravelCost(
            destination = dest,
            transportMode = TravelTransportMode.BUGGY,
            participantCount = 2
        )
        assertTrue("Buggy must consume fuel", buggyCost.fuel > 0)
        assertTrue("Buggy should be significantly faster than foot", buggyCost.estimatedDurationHours < bikeCost.estimatedDurationHours)

        // 4. Armored Truck
        val truckCost = TravelCalculator.calculateTravelCost(
            destination = dest,
            transportMode = TravelTransportMode.ARMORED_TRUCK,
            participantCount = 2
        )
        assertTrue("Armored truck consumes more fuel than buggy", truckCost.fuel > buggyCost.fuel)
    }

    @Test
    fun testTerrainMultipliersAffectCostAndDuration() {
        val wastelandLoc = Location(
            id = "loc_test_waste",
            name = "Равнина",
            type = LocationType.FARM,
            terrainType = TerrainType.WASTELAND,
            dangerLevel = DangerLevel.SAFE,
            distanceKm = 10,
            isUnlocked = true
        )

        val hillsLoc = Location(
            id = "loc_test_hills",
            name = "Хребет",
            type = LocationType.MILITARY_BUNKER,
            terrainType = TerrainType.HILLS,
            dangerLevel = DangerLevel.HIGH,
            distanceKm = 10,
            isUnlocked = true
        )

        val wasteCost = TravelCalculator.calculateTravelCost(
            destination = wastelandLoc,
            transportMode = TravelTransportMode.BUGGY
        )

        val hillsCost = TravelCalculator.calculateTravelCost(
            destination = hillsLoc,
            transportMode = TravelTransportMode.BUGGY
        )

        assertTrue("Hills should require more travel time than flat wasteland", hillsCost.estimatedDurationHours > wasteCost.estimatedDurationHours)
        assertTrue("Hills should consume more fuel", hillsCost.fuel >= wasteCost.fuel)
    }

    @Test
    fun testTravelValidationResourceChecks() {
        val dest = initialState.locations.first { it.id == "loc_7" }

        // 1. Success with rich state
        val validState = initialState.copy(
            resources = initialState.resources.copy(water = 100, food = 100, fuel = 100)
        )
        val validResult = TravelCalculator.validateTravel(
            destination = dest,
            transportMode = TravelTransportMode.BUGGY,
            gameState = validState
        )
        assertTrue("Travel should be valid with sufficient resources", validResult is TravelValidationResult.Valid)

        // 2. Insufficient Water
        val dryState = validState.copy(resources = validState.resources.copy(water = 0))
        val waterFail = TravelCalculator.validateTravel(
            destination = dest,
            transportMode = TravelTransportMode.FOOT,
            gameState = dryState
        )
        assertTrue("Validation must fail with insufficient water", waterFail is TravelValidationResult.Invalid)
        assertEquals(TravelFailureReason.INSUFFICIENT_WATER, (waterFail as TravelValidationResult.Invalid).reason)

        // 3. Insufficient Fuel for motorized
        val noFuelState = validState.copy(resources = validState.resources.copy(fuel = 0))
        val fuelFail = TravelCalculator.validateTravel(
            destination = dest,
            transportMode = TravelTransportMode.BUGGY,
            gameState = noFuelState
        )
        assertTrue("Validation must fail with insufficient fuel for buggy", fuelFail is TravelValidationResult.Invalid)
        assertEquals(TravelFailureReason.INSUFFICIENT_FUEL, (fuelFail as TravelValidationResult.Invalid).reason)
    }

    @Test
    fun testCannotTravelToCurrentLocationOrLockedLocation() {
        val baseLoc = initialState.locations.first { it.isPlayerBase }
        val lockedLoc = initialState.locations.first { !it.isUnlocked }

        val richState = initialState.copy(
            resources = initialState.resources.copy(water = 100, food = 100, fuel = 100)
        )

        // 1. Current Location
        val currentLocFail = TravelCalculator.validateTravel(
            destination = baseLoc,
            transportMode = TravelTransportMode.FOOT,
            gameState = richState,
            originLocationId = baseLoc.id
        )
        assertTrue("Cannot travel to current location", currentLocFail is TravelValidationResult.Invalid)
        assertEquals(TravelFailureReason.LOCATION_IS_CURRENT, (currentLocFail as TravelValidationResult.Invalid).reason)

        // 2. Locked Location
        val lockedFail = TravelCalculator.validateTravel(
            destination = lockedLoc,
            transportMode = TravelTransportMode.FOOT,
            gameState = richState
        )
        assertTrue("Cannot travel to locked location", lockedFail is TravelValidationResult.Invalid)
        assertEquals(TravelFailureReason.LOCATION_LOCKED, (lockedFail as TravelValidationResult.Invalid).reason)
    }

    @Test
    fun testAtomicResourceDeductionOnTravelStart() {
        val dest = initialState.locations.first { it.id == "loc_1" } // Станция, 9 km
        val initialWater = 80
        val initialFood = 70
        val initialFuel = 60

        val stateWithSupplies = initialState.copy(
            resources = initialState.resources.copy(
                water = initialWater,
                food = initialFood,
                fuel = initialFuel
            )
        )

        val tx = TravelCalculator.startTravelTransaction(
            destination = dest,
            transportMode = TravelTransportMode.BUGGY,
            gameState = stateWithSupplies
        )

        assertTrue("Transaction must succeed", tx is TravelTransactionResult.Success)
        val success = tx as TravelTransactionResult.Success

        assertEquals("Water must be deducted correctly", initialWater - success.cost.water, success.updatedResources.water)
        assertEquals("Food must be deducted correctly", initialFood - success.cost.food, success.updatedResources.food)
        assertEquals("Fuel must be deducted correctly", initialFuel - success.cost.fuel, success.updatedResources.fuel)
        assertEquals(TravelStatus.TRAVELING, success.travelState.status)
        assertEquals(0f, success.travelState.progressFraction, 0.001f)
    }

    @Test
    fun testRouteProgressionAndArrival() {
        val dest = initialState.locations.first { it.id == "loc_1" }
        val startState = TravelState(
            id = "travel_test",
            toLocationId = dest.id,
            distanceKm = dest.distanceKm,
            traveledKm = 0f,
            progressFraction = 0f,
            status = TravelStatus.TRAVELING
        )

        // Step 1: Advance by 25%
        val step1 = TravelCalculator.advanceStep(startState, initialState.locations, stepFraction = 0.25f)
        assertEquals(0.25f, step1.progressFraction, 0.01f)
        assertEquals(dest.distanceKm * 0.25f, step1.traveledKm, 0.1f)
        assertEquals(TravelStatus.TRAVELING, step1.status)

        // Step 2: Advance to 100%
        val stepArrived = TravelCalculator.advanceStep(step1, initialState.locations, stepFraction = 0.80f)
        assertEquals(1.0f, stepArrived.progressFraction, 0.01f)
        assertEquals(dest.distanceKm.toFloat(), stepArrived.traveledKm, 0.1f)
        assertEquals(TravelStatus.ARRIVED, stepArrived.status)
        assertTrue("Should have arrival log", stepArrived.travelLogs.any { it.contains("успешно прибыл") })
    }

    @Test
    fun testViewModelFullTravelCycle() {
        val dest = initialState.locations.first { it.id == "loc_7" }

        // Start travel via ViewModel
        val result = viewModel.startTravel(
            destinationId = dest.id,
            mode = TravelTransportMode.FOOT
        )
        assertTrue("ViewModel should start travel", result is TravelTransactionResult.Success)

        val stateAfterStart = viewModel.gameState.value
        assertNotNull("activeTravel must not be null", stateAfterStart.activeTravel)
        assertTrue("isCurrentlyTraveling must be true", stateAfterStart.isCurrentlyTraveling)
        assertEquals(TravelStatus.TRAVELING, stateAfterStart.activeTravel?.status)

        // Cannot start another travel while one is active
        val secondAttempt = viewModel.startTravel(
            destinationId = "loc_1",
            mode = TravelTransportMode.FOOT
        )
        assertTrue("Cannot start second travel while in progress", secondAttempt is TravelTransactionResult.Failure)

        // Advance steps to destination
        viewModel.instantArriveTravel()

        val stateArrived = viewModel.gameState.value
        assertEquals(TravelStatus.ARRIVED, stateArrived.activeTravel?.status)
        assertTrue("isArrivedAtDestination must be true", stateArrived.isArrivedAtDestination)
        assertEquals(dest.id, stateArrived.currentLocationId)

        // Explore location
        viewModel.exploreArrivedLocation()
        val stateExploring = viewModel.gameState.value
        assertNotNull("Expedition should be active after exploring", stateExploring.activeExpedition)

        // Cancel / return to settlement safely
        viewModel.cancelOrReturnToSettlement()
        val stateBack = viewModel.gameState.value
        assertNull("activeTravel should be cleared", stateBack.activeTravel)
        assertEquals("loc_base", stateBack.currentLocationId)
    }
}
