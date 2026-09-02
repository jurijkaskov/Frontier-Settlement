package com.example

import com.example.core.result.GameError
import com.example.core.result.GameResult
import com.example.data.InitialGameData
import com.example.data.repository.DefaultGameStateRepository
import com.example.data.repository.GameStateRepository
import com.example.domain.model.*
import com.example.domain.usecase.economy.CraftItemUseCase
import com.example.domain.usecase.expedition.CompleteExpeditionReturnUseCase
import com.example.domain.usecase.expedition.StartExpeditionUseCase
import com.example.domain.usecase.settlement.BuildBuildingUseCase
import com.example.domain.validator.GameStateValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Architectural, Invariant, and Concurrency Tests for Frontier Settlement.
 */
class ArchitectureAndInvariantsTest {

    private lateinit var repository: GameStateRepository

    @Before
    fun setUp() {
        repository = DefaultGameStateRepository(initialState = InitialGameData.createInitialGameState())
    }

    @Test
    fun `test GameStateRepository provides reactive StateFlow and atomic updates`() {
        val initial = repository.currentGameState
        assertEquals(1, initial.day)

        val updated = repository.updateGameStateSync { state ->
            state.copy(gameDateTime = state.gameDateTime.copy(day = state.day + 1))
        }

        assertEquals(2, updated.day)
        assertEquals(2, repository.gameState.value.day)
        assertEquals(2, repository.currentGameState.day)
    }

    @Test
    fun `test GameStateRepository concurrency safety under parallel coroutines`() = runBlocking {
        val iterations = 50
        val deferreds = (1..iterations).map {
            async(Dispatchers.Default) {
                repository.updateGameState { state ->
                    val curMat = state.resources.materials
                    state.copy(resources = state.resources.copy(materials = curMat + 10))
                }
            }
        }
        deferreds.awaitAll()

        val expectedMaterials = InitialGameData.createInitialGameState().resources.materials + (iterations * 10)
        assertEquals(expectedMaterials, repository.currentGameState.resources.materials)
    }

    @Test
    fun `test GameStateValidator detects valid initial state`() {
        val validation = GameStateValidator.validate(repository.currentGameState)
        assertTrue(
            "Initial state should be completely valid, but got errors: ${validation.errors}",
            validation.isValid
        )
        assertTrue(validation.errors.isEmpty())
    }

    @Test
    fun `test GameStateValidator catches duplicate resident IDs and dangling squad members`() {
        val baseState = repository.currentGameState
        val resident = baseState.characters.first()
        
        // Introduce duplicate resident
        val invalidResidentsState = baseState.copy(
            characters = baseState.characters + resident
        )
        val residentValidation = GameStateValidator.validate(invalidResidentsState)
        assertFalse("Validator should fail on duplicate residents", residentValidation.isValid)
        assertTrue(residentValidation.errors.any { it.contains("повторяющиеся ID жителей") })

        // Introduce dangling squad member ID
        val invalidSquadState = baseState.copy(
            squad = baseState.squad.copy(
                memberIds = baseState.squad.memberIds + "char_ghost_999"
            )
        )
        val squadValidation = GameStateValidator.validate(invalidSquadState)
        assertFalse("Validator should fail on dangling squad member not in settlement", squadValidation.isValid)
        assertTrue(squadValidation.errors.any { it.contains("не найден в списке жителей") })
    }

    @Test
    fun `test BuildBuildingUseCase validates resources and updates state atomically`() {
        val useCase = BuildBuildingUseCase(repository)
        
        // Find an unconstructed building
        val unbuilt = repository.currentGameState.settlement.buildings.find { !it.isConstructed }
        assertNotNull(unbuilt)
        val buildingId = unbuilt!!.id

        // Attempt build with insufficient materials
        repository.updateGameStateSync { state ->
            state.copy(resources = state.resources.copy(materials = 0))
        }
        val failResult = useCase(buildingId)
        assertTrue("Build should fail with 0 materials", failResult.isFailure)
        assertEquals(GameError.InsufficientResources, (failResult as GameResult.Failure).error)

        // Give ample materials and build successfully
        repository.updateGameStateSync { state ->
            state.copy(resources = state.resources.copy(materials = 500))
        }
        val successResult = useCase(buildingId)
        assertTrue("Build should succeed with materials", successResult.isSuccess)
        val builtBuilding = repository.currentGameState.settlement.buildings.find { it.id == buildingId }
        assertTrue(builtBuilding?.isConstructed == true)
        assertEquals(BuildingStatus.OPERATIONAL, builtBuilding?.status)
    }

    @Test
    fun `test StartExpeditionUseCase and CompleteExpeditionReturnUseCase lifecycle`() {
        val startUseCase = StartExpeditionUseCase(repository)
        val returnUseCase = CompleteExpeditionReturnUseCase(repository)

        val targetLoc = repository.currentGameState.locations.first { it.id != "loc_base" }.id
        val startResult = startUseCase(targetLoc)
        assertTrue("Starting expedition should succeed", startResult.isSuccess)
        assertNotNull(repository.currentGameState.activeExpedition)
        assertEquals(targetLoc, repository.currentGameState.activeExpedition?.location?.id)

        // Cannot start another expedition while one is active
        val secondStart = startUseCase("loc_2")
        assertTrue("Cannot start second expedition while one is active", secondStart.isFailure)

        // Conclude expedition
        val returnResult = returnUseCase()
        assertTrue("Return expedition should succeed", returnResult.isSuccess)
        assertNull("Expedition should be inactive after return", repository.currentGameState.activeExpedition)
    }

    @Test
    fun `test CraftItemUseCase checks recipes and produces items atomically`() {
        val craftUseCase = CraftItemUseCase(repository)
        
        val testItem = WarehouseItem(
            id = "item_test_medkit",
            name = "Армейская аптечка",
            description = "Восстанавливает здоровье",
            category = ItemCategory.MEDICINE_AND_AID,
            rarity = ItemRarity.UNCOMMON,
            unitSize = 1,
            quantity = 1
        )
        val recipe = CraftRecipe(
            id = "recipe_test_medkit",
            nameRu = "Сборка аптечки",
            descriptionRu = "Создание аптечки из трав и реагентов",
            outputItem = testItem,
            outputQuantity = 1,
            requiredResources = mapOf(
                ResourceType.MEDICINE to 5,
                ResourceType.COMPONENTS to 2
            ),
            category = CraftRecipeCategory.MEDICINE
        )

        // Without resources -> fails
        repository.updateGameStateSync { state ->
            state.copy(resources = state.resources.withResource(ResourceType.MEDICINE, 0).withResource(ResourceType.COMPONENTS, 0))
        }
        val failCraft = craftUseCase(recipe, craftCount = 1)
        assertTrue("Craft should fail without medicine", failCraft.isFailure)

        // With resources -> succeeds and places item in warehouse
        repository.updateGameStateSync { state ->
            state.copy(resources = state.resources.withResource(ResourceType.MEDICINE, 20).withResource(ResourceType.COMPONENTS, 10))
        }
        val successCraft = craftUseCase(recipe, craftCount = 2)
        assertTrue("Craft should succeed with enough materials", successCraft.isSuccess)
        val producedCount = repository.currentGameState.inventoryItems.find { it.id == testItem.id }?.quantity ?: 0
        assertEquals(2, producedCount)
        assertEquals(10, repository.currentGameState.resources[ResourceType.MEDICINE]) // 20 - (5*2) = 10
        assertEquals(6, repository.currentGameState.resources[ResourceType.COMPONENTS]) // 10 - (2*2) = 6
    }
}
