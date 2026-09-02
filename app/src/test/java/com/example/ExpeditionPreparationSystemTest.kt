package com.example

import com.example.domain.model.*
import com.example.domain.service.ExpeditionPreparationValidator
import com.example.domain.service.ExpeditionSupplyCalculator
import com.example.viewmodel.GameViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExpeditionPreparationSystemTest {

    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        viewModel = GameViewModel()
    }

    @Test
    fun testInitExpeditionDraft() {
        val state = viewModel.gameState.value
        val destination = state.locations.first { it.id != state.currentLocationId }
        viewModel.initExpeditionDraft(destination.id)

        val draft = viewModel.expeditionDraft.value
        assertNotNull("Expedition draft should not be null after init", draft)
        assertEquals(destination.id, draft?.destinationLocationId)
        assertTrue("Draft should include at least one participant", draft?.participantIds?.isNotEmpty() == true)
    }

    @Test
    fun testSupplyAndWeightCalculations() {
        val state = viewModel.gameState.value
        val destination = state.locations.first { it.id != state.currentLocationId }

        val supplies = mapOf(
            ResourceType.WATER to 10,
            ResourceType.FOOD to 8,
            ResourceType.FUEL to 15,
            ResourceType.MEDICINE to 3
        )

        val draft = ExpeditionPreparationState(
            destinationLocationId = destination.id,
            originLocationId = state.currentLocationId,
            participantIds = state.characters.take(3).map { it.id },
            leaderId = state.characters.first().id,
            travelMode = TravelTransportMode.BUGGY,
            selectedVehicleId = "veh_buggy_1",
            supplies = supplies
        )

        val cargo = ExpeditionSupplyCalculator.calculateCargoSummary(
            destination = destination,
            prepState = draft,
            gameState = state
        )

        assertTrue("Cargo total capacity should be positive", cargo.totalCapacityKg > 0)
        assertTrue("Supplies weight should be positive", cargo.suppliesWeightKg > 0)
        assertFalse("With a buggy, moderate supplies should not be overloaded", cargo.isOverloaded)
    }

    @Test
    fun testValidationBlocksWhenNoParticipants() {
        val state = viewModel.gameState.value
        val destination = state.locations.first { it.id != state.currentLocationId }

        val emptySquadDraft = ExpeditionPreparationState(
            destinationLocationId = destination.id,
            originLocationId = state.currentLocationId,
            participantIds = emptyList(),
            supplies = mapOf(ResourceType.WATER to 5, ResourceType.FOOD to 5)
        )

        val validation = ExpeditionPreparationValidator.validate(emptySquadDraft, state)
        assertFalse("Validation must fail when no participants selected", validation.canDepart)
        assertTrue(
            "Issues should mention squad/inhabitant requirement",
            validation.blockingIssues.any { issue ->
                issue.contains("отряд", ignoreCase = true) ||
                issue.contains("жител", ignoreCase = true) ||
                issue.contains("человек", ignoreCase = true) ||
                issue.contains("участник", ignoreCase = true)
            }
        )
    }

    @Test
    fun testAtomicStartPreparedExpeditionExecution() {
        val state = viewModel.gameState.value
        val destination = state.locations.first { it.id != state.currentLocationId && it.isUnlocked }

        viewModel.initExpeditionDraft(destination.id)
        viewModel.setDraftTravelMode(TravelTransportMode.FOOT)
        viewModel.applyRecommendedSupplies()

        val initialWater = viewModel.gameState.value.resources.water
        val initialFood = viewModel.gameState.value.resources.food
        val draftWater = viewModel.expeditionDraft.value?.getSupplyAmount(ResourceType.WATER) ?: 0
        val draftFood = viewModel.expeditionDraft.value?.getSupplyAmount(ResourceType.FOOD) ?: 0

        val result = viewModel.startPreparedExpedition(destination.id)
        assertTrue("Expedition launch should succeed: ${result.message}", result.isSuccess)

        val updatedState = viewModel.gameState.value
        assertEquals("Water should be deducted atomically", initialWater - draftWater, updatedState.resources.water)
        assertEquals("Food should be deducted atomically", initialFood - draftFood, updatedState.resources.food)

        val participants = updatedState.characters.filter { updatedState.selectedSquadIds.contains(it.id) }
        assertTrue("Participants must have ON_EXPEDITION status", participants.all { it.status == CharacterStatus.ON_EXPEDITION })
        assertNotNull("Active travel must be instantiated", updatedState.activeTravel)
    }
}
