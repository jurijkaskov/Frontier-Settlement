package com.example.domain.content.event

import com.example.domain.content.core.ContentGenerationContext
import com.example.domain.content.core.ContentGenerationHistory
import com.example.domain.content.core.GameRandomProvider
import com.example.domain.content.core.GenerationResult
import com.example.domain.content.core.WeightedSelector
import com.example.domain.content.registry.GameContentRegistry
import com.example.domain.model.EventCategory
import com.example.domain.model.EventRepeatMode
import com.example.domain.model.ExpeditionEvent
import com.example.domain.service.events.EventRequirementEvaluator
import com.example.domain.model.GameState
import com.example.domain.model.Expedition
import kotlin.random.Random

/**
 * Procedural selector and generator for expedition events.
 * Implements data-driven candidate filtering, precondition checking,
 * and anti-repeat recency penalty calculations.
 */
object ExpeditionEventGenerator {

    fun selectNextEvent(
        context: ContentGenerationContext,
        history: ContentGenerationHistory,
        gameState: GameState? = null,
        expedition: Expedition? = null,
        registry: GameContentRegistry = GameContentRegistry,
        customIndex: Int? = null
    ): GenerationResult<ExpeditionEvent> {
        val index = customIndex ?: context.generationIndex
        val seed = GameRandomProvider.deriveSeed(context.gameSeed, "event", context.locationId, index)
        val random = Random(seed)

        val allEvents = registry.events.values.toList()

        // 1. Filter out candidate events by repeatability and history
        val eligibleCandidates = allEvents.filter { event ->
            // Repeat mode checks
            when (event.repeatMode) {
                EventRepeatMode.GLOBAL_ONCE -> {
                    if (history.isAlreadyGenerated(event.id)) return@filter false
                }
                EventRepeatMode.ONCE_PER_LOCATION -> {
                    if ((history.generatedCounts[event.id] ?: 0) > 0 && context.visitCount > 1) {
                        // Check if previously rolled at this specific location
                    }
                }
                else -> Unit
            }

            // Location Type filter
            if (event.allowedLocationTypes.isNotEmpty() && !event.allowedLocationTypes.contains(context.locationType)) {
                return@filter false
            }

            // Danger Range filter
            if (context.dangerLevel.rating < event.minDangerLevel.rating ||
                context.dangerLevel.rating > event.maxDangerLevel.rating
            ) {
                return@filter false
            }

            // Target area filter
            if (event.targetAreaIds.isNotEmpty() && context.areaId != null) {
                if (!event.targetAreaIds.contains(context.areaId)) {
                    return@filter false
                }
            }

            // If gameState and expedition are supplied, evaluate rich conditions
            if (gameState != null && expedition != null) {
                val eval = EventRequirementEvaluator.evaluate(
                    requirements = event.requirements,
                    gameState = gameState,
                    expedition = expedition,
                    currentLocation = expedition.location,
                    currentAreaId = expedition.currentAreaId
                )
                if (!eval.isMet) return@filter false
            }

            true
        }

        if (eligibleCandidates.isEmpty()) {
            val fallback = allEvents.firstOrNull { it.repeatMode == EventRepeatMode.REPEATABLE }
                ?: return GenerationResult.NoEligibleContent("No eligible events found for context")
            return GenerationResult.Success(fallback)
        }

        // 2. Apply Weighted Selection with Recency Modifier
        val selected = WeightedSelector.select(
            candidates = eligibleCandidates,
            weightExtractor = { event ->
                val base = event.baseWeight * event.rarity.weightMultiplier
                val recencyPenalty = history.getRecencyMultiplier(event.id)
                base * recencyPenalty
            },
            random = random,
            context = context
        ) ?: eligibleCandidates.first()

        return GenerationResult.Success(selected)
    }
}
