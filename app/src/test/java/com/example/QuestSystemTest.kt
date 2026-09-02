package com.example

import com.example.data.InitialGameData
import com.example.domain.model.*
import com.example.domain.model.quest.*
import com.example.domain.model.reputation.FactionRelation
import com.example.domain.model.reputation.ReputationTier
import com.example.domain.service.quest.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class QuestSystemTest {

    private lateinit var initialGameState: GameState

    @Before
    fun setUp() {
        initialGameState = InitialGameData.createInitialGameState()
    }

    @Test
    fun `test catalog integrity and uniqueness`() {
        val validationErrors = QuestCatalog.validate()
        assertTrue(
            "Quest catalog validation should have 0 errors, but got: $validationErrors",
            validationErrors.isEmpty()
        )
    }

    @Test
    fun `test quest requirement evaluator`() {
        val startingQuest = QuestCatalog.get(QuestCatalog.QUEST_FIRST_SUPPLIES.id)
        assertNotNull(startingQuest)

        val evalStarting = QuestRequirementEvaluator.evaluate(startingQuest!!, initialGameState)
        assertTrue("Initial quest requirements should be satisfied", evalStarting.isMet)

        val advancedQuest = QuestCatalog.get(QuestCatalog.QUEST_HIDDEN_THREAT.id)
        assertNotNull(advancedQuest)
        val evalAdvanced = QuestRequirementEvaluator.evaluate(advancedQuest!!, initialGameState)
        assertFalse("Hidden threat requires quest_scout_north completed first", evalAdvanced.isMet)
    }

    @Test
    fun `test quest accept and decline flow`() {
        val nomadQuestId = QuestCatalog.QUEST_NOMAD_AID.id
        // Set relation requirement met
        val stateWithRep = initialGameState.copy(
            factionRelations = initialGameState.factionRelations + ("nomads" to FactionRelation(
                factionId = "nomads",
                points = 20,
                isDiscovered = true
            )),
            questStates = initialGameState.questStates + (nomadQuestId to QuestState(
                questId = nomadQuestId,
                status = QuestStatus.AVAILABLE
            ))
        )

        val acceptResult = QuestManager.acceptQuest(stateWithRep, nomadQuestId)
        assertTrue("Accepting available nomad quest should succeed: ${acceptResult.messageRu}", acceptResult.isSuccess)

        val stateAfterAccept = acceptResult.updatedGameState
        val qState = stateAfterAccept.questStates[nomadQuestId]
        assertNotNull(qState)
        assertEquals(QuestStatus.ACTIVE, qState?.status)
        assertTrue(qState?.isActive == true)

        // Decline declinable quest
        val caravanQuestId = QuestCatalog.QUEST_CARAVAN_SUPPLY.id
        val stateWithCaravan = stateAfterAccept.copy(
            questStates = stateAfterAccept.questStates + (caravanQuestId to QuestState(
                questId = caravanQuestId,
                status = QuestStatus.AVAILABLE
            ))
        )
        val declineResult = QuestManager.declineQuest(stateWithCaravan, caravanQuestId)
        assertTrue("Declining declinable caravan contract should succeed", declineResult.isSuccess)
        assertEquals(QuestStatus.DECLINED, declineResult.updatedGameState.questStates[caravanQuestId]?.status)
    }

    @Test
    fun `test quest progress via game events`() {
        val questDef = QuestCatalog.QUEST_SCOUT_NORTH
        val questId = questDef.id
        val stateWithQuest = QuestManager.acceptQuest(initialGameState, questId).updatedGameState

        // Visit north post location
        val stateAfterVisit = QuestProgressProcessor.process(
            GameEvent.LocationVisited("loc_north_post"),
            stateWithQuest
        )
        val progVisit = stateAfterVisit.questStates[questId]?.objectiveProgress?.get("obj_visit_north")
        assertEquals(1, progVisit?.currentAmount)
        assertEquals(ObjectiveStatus.COMPLETED, progVisit?.status)

        // Return to base objective unlocks and progresses
        val stateAfterReturn = QuestProgressProcessor.process(
            GameEvent.LocationVisited("loc_base"),
            stateAfterVisit
        )
        val progReturn = stateAfterReturn.questStates[questId]?.objectiveProgress?.get("obj_return_base")
        assertEquals(1, progReturn?.currentAmount)
        assertEquals(ObjectiveStatus.COMPLETED, progReturn?.status)

        val qStateFinal = stateAfterReturn.questStates[questId]
        assertEquals(QuestStatus.COMPLETED, qStateFinal?.status)
    }

    @Test
    fun `test quest turn in rewards idempotency`() {
        val questId = QuestCatalog.QUEST_FIRST_SUPPLIES.id
        val questDef = QuestCatalog.get(questId)!!

        // Force quest to ready to claim
        val readyState = initialGameState.copy(
            questStates = initialGameState.questStates + (questId to QuestState(
                questId = questId,
                status = QuestStatus.READY_TO_CLAIM,
                objectiveProgress = mapOf(
                    "obj_collect_mat" to QuestObjectiveProgress("obj_collect_mat", currentAmount = 50, targetAmount = 50, status = ObjectiveStatus.COMPLETED)
                )
            ))
        )

        val creditsBefore = readyState.resources.money
        val repBefore = readyState.settlement.reputation

        val turnInResult = QuestManager.turnInQuest(readyState, questId)
        assertTrue("Turn in should succeed: ${turnInResult.messageRu}", turnInResult.isSuccess)

        val finalState = turnInResult.updatedGameState
        assertEquals(creditsBefore + questDef.rewards.credits, finalState.resources.money)
        assertEquals(repBefore + questDef.rewards.reputationDelta, finalState.settlement.reputation)
        assertEquals(QuestStatus.COMPLETED, finalState.questStates[questId]?.status)

        // Attempt second turn in -> must be rejected and not grant double reward
        val secondTurnIn = QuestManager.turnInQuest(finalState, questId)
        assertFalse("Second turn in should fail", secondTurnIn.isSuccess)
        assertEquals(finalState.resources.money, secondTurnIn.updatedGameState.resources.money)
    }

    @Test
    fun `test resource delivery objective`() {
        val questId = QuestCatalog.QUEST_CARAVAN_SUPPLY.id
        val stateWithCaravan = initialGameState.copy(
            resources = initialGameState.resources.withResource(ResourceType.COMPONENTS, 50),
            questStates = initialGameState.questStates + (questId to QuestState(
                questId = questId,
                status = QuestStatus.ACTIVE,
                objectiveProgress = mapOf(
                    "obj_deliver_components" to QuestObjectiveProgress("obj_deliver_components", currentAmount = 0, targetAmount = 20, status = ObjectiveStatus.IN_PROGRESS)
                )
            ))
        )

        val deliverResult = QuestManager.deliverResource(stateWithCaravan, questId, "obj_deliver_components", 20)
        assertTrue("Delivering components should succeed: ${deliverResult.messageRu}", deliverResult.isSuccess)

        val stateAfterDelivery = deliverResult.updatedGameState
        assertEquals(30, stateAfterDelivery.resources[ResourceType.COMPONENTS])
        val objProg = stateAfterDelivery.questStates[questId]?.objectiveProgress?.get("obj_deliver_components")
        assertEquals(20, objProg?.currentAmount)
        assertEquals(ObjectiveStatus.COMPLETED, objProg?.status)
        assertEquals(QuestStatus.READY_TO_CLAIM, stateAfterDelivery.questStates[questId]?.status)
    }

    @Test
    fun `test quest deadline failure on daily tick`() {
        val contractId = QuestCatalog.QUEST_CARAVAN_SUPPLY.id
        val stateWithQuest = initialGameState.copy(
            questStates = initialGameState.questStates + (contractId to QuestState(
                questId = contractId,
                status = QuestStatus.ACTIVE,
                deadlineGameDateTime = GameDateTime(day = 5, hour = 0, minute = 0)
            ))
        )

        // Fast forward past deadline day
        val futureState = stateWithQuest.copy(
            gameDateTime = GameDateTime(day = 6, hour = 12, minute = 0)
        )

        val stateAfterTick = QuestManager.onDailyTick(futureState)
        val qState = stateAfterTick.questStates[contractId]
        assertEquals(QuestStatus.EXPIRED, qState?.status)
    }

    @Test
    fun `test quest marker helper`() {
        val state = initialGameState.copy(
            questStates = initialGameState.questStates + (QuestCatalog.QUEST_SCOUT_NORTH.id to QuestState(
                questId = QuestCatalog.QUEST_SCOUT_NORTH.id,
                status = QuestStatus.ACTIVE,
                objectiveProgress = mapOf(
                    "obj_visit_north" to QuestObjectiveProgress("obj_visit_north", 0, 1, ObjectiveStatus.IN_PROGRESS)
                )
            ))
        )
        val locationMarkers = QuestMarkerHelper.getMarkersForLocation("loc_north_post", state)
        assertTrue("Location north post should have active quest marker", locationMarkers.isNotEmpty())
        assertEquals(QuestMarkerType.ACTIVE_OBJECTIVE, locationMarkers.first().markerType)
    }
}
