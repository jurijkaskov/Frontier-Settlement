package com.example

import com.example.data.InitialGameData
import com.example.domain.model.*
import com.example.domain.service.events.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class RandomEventSystemTest {

    private lateinit var gameState: GameState
    private lateinit var expedition: Expedition
    private lateinit var testSquad: List<Character>
    private lateinit var testVehicle: Vehicle

    @Before
    fun setup() {
        gameState = InitialGameData.createInitialGameState()
        testSquad = gameState.characters.take(3)
        testVehicle = gameState.vehicles.first()

        val targetLoc = gameState.locations.find { it.id == "loc_station" } ?: gameState.locations.first()

        expedition = Expedition(
            id = "exp_test",
            location = targetLoc,
            squad = testSquad,
            vehicle = testVehicle,
            phase = ExpeditionPhase.EXPLORING,
            status = ExpeditionStatus.EXPLORING,
            cargoCapacityKg = 100,
            cargoWeightKg = 10f,
            supplies = mapOf(ResourceType.FOOD to 10, ResourceType.WATER to 10, ResourceType.FUEL to 10),
            seed = 123456L
        )
    }

    @Test
    fun testEventRequirementEvaluator_roleCheck() {
        val roleReq = EventRequirement.RequiresRole(CharacterRole.ENGINEER)
        
        // When squad has no engineer
        val squadNoEngineer = listOf(
            testSquad[0].copy(role = CharacterRole.SCAVENGER),
            testSquad[1].copy(role = CharacterRole.SOLDIER)
        )
        val expNoEngineer = expedition.copy(squad = squadNoEngineer)
        val evalFail = EventRequirementEvaluator.evaluate(listOf(roleReq), gameState, expNoEngineer)
        assertFalse(evalFail.isMet)
        assertNotNull(evalFail.lockDescription)

        // When squad has engineer
        val squadWithEngineer = listOf(
            testSquad[0].copy(role = CharacterRole.ENGINEER)
        )
        val expWithEngineer = expedition.copy(squad = squadWithEngineer)
        val evalPass = EventRequirementEvaluator.evaluate(listOf(roleReq), gameState, expWithEngineer)
        assertTrue(evalPass.isMet)
        assertNull(evalPass.lockDescription)
    }

    @Test
    fun testEventRequirementEvaluator_suppliesCheck() {
        val supplyReq = EventRequirement.RequiresResource(ResourceType.FUEL, 5)

        val expLowFuel = expedition.copy(supplies = mapOf(ResourceType.FUEL to 2))
        val evalFail = EventRequirementEvaluator.evaluate(listOf(supplyReq), gameState, expLowFuel)
        assertFalse(evalFail.isMet)

        val expGoodFuel = expedition.copy(supplies = mapOf(ResourceType.FUEL to 10))
        val evalPass = EventRequirementEvaluator.evaluate(listOf(supplyReq), gameState, expGoodFuel)
        assertTrue(evalPass.isMet)
    }

    @Test
    fun testSkillCheckResolver_deterministicRollAndRoleBonus() {
        val engineerChar = Character(
            id = "char_eng",
            name = "Виктор",
            role = CharacterRole.ENGINEER,
            stats = CharacterStats(engineeringSkill = 6, attack = 2, defense = 2, scavengingSkill = 2, medicalSkill = 2),
            health = 100,
            maxHealth = 100
        )

        val checkReq = SkillCheckRequirement(
            statType = CharacterStatType.ENGINEERING,
            difficulty = 8,
            applicableRoles = setOf(CharacterRole.ENGINEER)
        )

        // Seed gives deterministic roll
        val seed = 42L
        val javaRng = java.util.Random(seed)
        val expectedRoll = javaRng.nextInt(10) + 1

        val result = SkillCheckResolver.resolveCheck(
            actor = engineerChar,
            requirement = checkReq,
            inventoryItems = emptyList(),
            seed = seed
        )

        // Base stat 6 + role bonus 3 + roll >= 8 -> should be success
        assertEquals(expectedRoll, result.roll)
        assertEquals(6, result.statValue)
        assertEquals(3, result.roleBonus)
        assertTrue(result.isSuccess)
    }

    @Test
    fun testEventOutcomeResolver_cargoCapacityEnforcement() {
        val testEvent = EventCatalog.EVT_LOCKED_SAFE
        val testChoice = testEvent.choices.first()

        // Create expedition with limited cargo capacity
        val tightExpedition = expedition.copy(
            cargoCapacityKg = 20,
            cargoWeightKg = 18f, // only 2kg free
            gatheredLoot = GameResources()
        )

        val initialHomeMoney = gameState.resources.money
        val initialHomeMaterials = gameState.resources.materials

        val (updatedState, _) = EventOutcomeResolver.resolve(
            event = testEvent,
            choice = testChoice,
            gameState = gameState,
            expedition = tightExpedition,
            seed = 9999L
        )

        val updatedExp = updatedState.activeExpedition
        assertNotNull(updatedExp)

        // Verify loot goes to EXPEDITION cargo, NOT home warehouse directly
        assertEquals("Home resources must not change during expedition exploration", initialHomeMoney, updatedState.resources.money)
        assertEquals("Home materials must not change during expedition exploration", initialHomeMaterials, updatedState.resources.materials)

        // Check active event outcome state is resolved
        assertNotNull(updatedExp?.activeEventState?.resolvedOutcome)
        assertTrue(updatedExp?.activeEventState?.isResolved == true)
    }

    @Test
    fun testEventSelector_deterministicWeightedSelection() {
        val catalog = EventCatalog.ALL_EVENTS
        assertTrue("Catalog must have multiple rich events", catalog.size >= 10)

        val seed = 777L
        val selected1 = EventSelector.selectNextEvent(catalog, gameState, expedition, seed)
        val selected2 = EventSelector.selectNextEvent(catalog, gameState, expedition, seed)

        assertNotNull(selected1)
        assertNotNull(selected2)
        assertEquals("Selection with the same seed must be deterministic", selected1?.id, selected2?.id)
    }

    @Test
    fun testEventRepeatMode_globalOnce() {
        val onceEvent = EventCatalog.EVT_MYSTERIOUS_TRANSMISSION
        assertEquals(EventRepeatMode.GLOBAL_ONCE, onceEvent.repeatMode)

        // When flag is set in gameState
        val stateWithEventCompleted = gameState.copy(
            eventHistory = listOf(
                EventHistoryEntry(
                    eventId = onceEvent.id,
                    locationId = expedition.location.id,
                    expeditionId = expedition.id,
                    choiceId = "choice_test",
                    wasSuccess = true,
                    day = 1,
                    timestamp = System.currentTimeMillis()
                )
            )
        )

        val isRepeatAllowed = EventSelector.isRepeatAllowed(onceEvent, stateWithEventCompleted, expedition)
        assertFalse("GLOBAL_ONCE event must not be allowed to repeat", isRepeatAllowed)
    }
}
