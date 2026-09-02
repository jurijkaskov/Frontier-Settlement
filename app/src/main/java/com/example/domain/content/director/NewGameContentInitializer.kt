package com.example.domain.content.director

import com.example.data.InitialGameData
import com.example.domain.content.core.ContentGenerationContext
import com.example.domain.content.core.ContentGenerationHistory
import com.example.domain.content.location.LocationGenerator
import com.example.domain.content.registry.GameContentRegistry
import com.example.domain.model.DangerLevel
import com.example.domain.model.GameState
import com.example.domain.model.Location
import com.example.domain.model.QuestStatus
import com.example.domain.model.quest.QuestState

/**
 * Deterministic Initializer for new game worlds.
 * Combines handcrafted starter POIs with procedural location and contract generation.
 */
object NewGameContentInitializer {

    /**
     * Initializes a fresh GameState using the procedural content pipeline.
     */
    fun initializeNewGame(
        gameSeed: Long = 133742L,
        registry: GameContentRegistry = GameContentRegistry
    ): GameState {
        val baseState = InitialGameData.createInitialGameState()

        val context = ContentGenerationContext(
            gameSeed = gameSeed,
            currentGameDay = 1,
            dangerLevel = DangerLevel.LOW,
            settlementLevel = 1,
            reputation = baseState.settlement.reputation
        )

        // 1. Generate additional procedural locations to complement starter locations
        val generatedLocations = mutableListOf<Location>()
        for (i in 1..2) {
            val genContext = context.copy(generationIndex = i)
            val result = LocationGenerator.generateRandomLocation(
                context = genContext,
                existingLocations = baseState.locations + generatedLocations,
                registry = registry
            )
            val loc = result.getOrNull()
            if (loc != null) {
                generatedLocations.add(loc)
            }
        }

        // 2. Generate initial repeatable contracts
        val initialContracts = WorldContentDirector.evaluateRepeatableContracts(
            gameState = baseState,
            context = context,
            registry = registry
        )

        val updatedQuestStates = baseState.questStates.toMutableMap()
        for ((def, state) in initialContracts) {
            updatedQuestStates[def.id] = state
        }

        val allQuests = (baseState.quests + initialContracts.map { (def, state) ->
            com.example.domain.model.Quest(
                id = def.id,
                title = def.titleRu,
                description = def.descriptionRu,
                requirementDescription = def.objectives.firstOrNull()?.descriptionRu ?: "",
                progress = 0,
                target = def.objectives.firstOrNull()?.requiredAmount ?: 1,
                rewardCredits = def.rewards.credits,
                rewardReputation = def.rewards.reputationDelta,
                rewardMaterials = def.rewards.resources[com.example.domain.model.ResourceType.MATERIALS] ?: 0,
                status = com.example.domain.model.QuestStatus.IN_PROGRESS
            )
        }).distinctBy { it.id }

        return baseState.copy(
            gameSeed = gameSeed,
            locations = (baseState.locations + generatedLocations).distinctBy { it.id },
            quests = allQuests,
            questStates = updatedQuestStates,
            contentGenerationHistory = ContentGenerationHistory()
        )
    }
}
