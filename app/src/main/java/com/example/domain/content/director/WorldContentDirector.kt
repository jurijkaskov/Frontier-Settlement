package com.example.domain.content.director

import com.example.domain.content.core.ContentGenerationContext
import com.example.domain.content.core.ContentGenerationHistory
import com.example.domain.content.location.LocationGenerator
import com.example.domain.content.quest.RepeatableQuestGenerator
import com.example.domain.content.registry.GameContentRegistry
import com.example.domain.model.GameState
import com.example.domain.model.Location
import com.example.domain.model.QuestStatus
import com.example.domain.model.quest.QuestDefinition
import com.example.domain.model.quest.QuestState

/**
 * High-level director coordinating content budgets, repeatable quest generation,
 * map exploration replenishment, and unique content lifecycle across game days.
 */
object WorldContentDirector {

    const val MAX_UNDISCOVERED_LOCATIONS = 8
    const val MAX_ACTIVE_REPEATABLE_CONTRACTS = 3

    /**
     * Evaluates whether new POI locations should be spawned on the world map.
     */
    fun evaluateLocationSpawns(
        gameState: GameState,
        context: ContentGenerationContext,
        registry: GameContentRegistry = GameContentRegistry
    ): List<Location> {
        val nonBaseLocations = gameState.locations.filter { !it.isPlayerBase }
        val currentCount = nonBaseLocations.size

        if (currentCount >= MAX_UNDISCOVERED_LOCATIONS) {
            return emptyList()
        }

        val locationsToSpawn = mutableListOf<Location>()
        val needed = (MAX_UNDISCOVERED_LOCATIONS - currentCount).coerceAtMost(2)

        var currentGenHistory = gameState.contentGenerationHistory

        for (i in 0 until needed) {
            val genContext = context.copy(generationIndex = currentCount + i + 1)
            val genResult = LocationGenerator.generateRandomLocation(
                context = genContext,
                existingLocations = gameState.locations + locationsToSpawn,
                registry = registry
            )
            val loc = genResult.getOrNull()
            if (loc != null) {
                locationsToSpawn.add(loc)
            }
        }

        return locationsToSpawn
    }

    /**
     * Evaluates whether new repeatable contracts should be generated in the settlement.
     */
    fun evaluateRepeatableContracts(
        gameState: GameState,
        context: ContentGenerationContext,
        registry: GameContentRegistry = GameContentRegistry
    ): List<Pair<QuestDefinition, QuestState>> {
        val activeContractCount = gameState.questStates.values.count { state ->
            (state.status == QuestStatus.ACTIVE || state.status == QuestStatus.IN_PROGRESS) &&
                    (gameState.quests.any { it.id == state.questId } || state.questId.startsWith("quest_gen_"))
        }

        if (activeContractCount >= MAX_ACTIVE_REPEATABLE_CONTRACTS) {
            return emptyList()
        }

        val needed = MAX_ACTIVE_REPEATABLE_CONTRACTS - activeContractCount
        val generatedQuests = mutableListOf<Pair<QuestDefinition, QuestState>>()

        for (i in 0 until needed) {
            val genContext = context.copy(generationIndex = gameState.day * 10 + i)
            val result = RepeatableQuestGenerator.generateRandomContract(
                context = genContext,
                availableLocations = gameState.locations,
                registry = registry
            )
            val pair = result.getOrNull()
            if (pair != null) {
                // Ensure ID is not already active
                if (!gameState.questStates.containsKey(pair.first.id)) {
                    generatedQuests.add(pair)
                }
            }
        }

        return generatedQuests
    }
}
