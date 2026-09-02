package com.example.domain.service.events

import com.example.domain.model.*
import java.util.Random

/**
 * Service responsible for deterministic, weighted filtering and selection of random events
 * for expedition exploration.
 */
object EventSelector {

    /**
     * Checks if an event can be repeated according to its repeatMode and event history.
     */
    fun isRepeatAllowed(
        event: ExpeditionEvent,
        gameState: GameState,
        expedition: Expedition
    ): Boolean {
        val currentLoc = expedition.location
        val history = gameState.eventHistory
        return when (event.repeatMode) {
            EventRepeatMode.REPEATABLE -> true
            EventRepeatMode.ONCE_PER_EXPEDITION -> {
                history.none { it.eventId == event.id && it.expeditionId == expedition.id }
            }
            EventRepeatMode.ONCE_PER_LOCATION -> {
                history.none { it.eventId == event.id && it.locationId == currentLoc.id }
            }
            EventRepeatMode.GLOBAL_ONCE -> {
                history.none { it.eventId == event.id }
            }
        }
    }

    /**
     * Selects the next appropriate event from the catalog for the current exploration step.
     */
    fun selectNextEvent(
        catalog: List<ExpeditionEvent>,
        gameState: GameState,
        expedition: Expedition,
        seed: Long = System.currentTimeMillis()
    ): ExpeditionEvent? {
        val currentLoc = expedition.location
        val history = gameState.eventHistory

        // 1. Filter out candidate events violating repeat rules
        val repeatFiltered = catalog.filter { event ->
            isRepeatAllowed(event, gameState, expedition)
        }

        // 2. Filter by location type, danger levels, and general event requirements
        val candidateEvents = repeatFiltered.filter { event ->
            // Location type match
            if (event.allowedLocationTypes.isNotEmpty() && !event.allowedLocationTypes.contains(currentLoc.type)) {
                return@filter false
            }

            // Danger level range match
            if (currentLoc.dangerLevel.rating < event.minDangerLevel.rating ||
                currentLoc.dangerLevel.rating > event.maxDangerLevel.rating
            ) {
                return@filter false
            }

            // Target area match (if event is tied to specific area)
            if (event.targetAreaIds.isNotEmpty() && expedition.currentAreaId != null) {
                if (!event.targetAreaIds.contains(expedition.currentAreaId)) {
                    return@filter false
                }
            }

            // Evaluate preconditions
            val reqEval = EventRequirementEvaluator.evaluate(
                requirements = event.requirements,
                gameState = gameState,
                expedition = expedition,
                currentLocation = currentLoc,
                currentAreaId = expedition.currentAreaId
            )
            reqEval.isMet
        }

        if (candidateEvents.isEmpty()) {
            // Fallback to basic safe event
            return catalog.firstOrNull { it.repeatMode == EventRepeatMode.REPEATABLE }
        }

        // 3. Calculate weighted probabilities
        val weightedList = candidateEvents.map { event ->
            var weight = event.baseWeight.toFloat() * event.rarity.weightMultiplier

            // Danger level affinity modifiers
            when (currentLoc.dangerLevel) {
                DangerLevel.SAFE, DangerLevel.LOW -> {
                    if (event.category == EventCategory.DISCOVERY || event.category == EventCategory.RESOURCE) {
                        weight *= 1.4f
                    }
                }
                DangerLevel.HIGH, DangerLevel.EXTREME -> {
                    if (event.category == EventCategory.ENVIRONMENT || event.category == EventCategory.ENCOUNTER) {
                        weight *= 1.5f
                    }
                }
                else -> {}
            }

            // Squad traits / roles modifiers
            if (expedition.squad.any { it.role == CharacterRole.SCOUT } && event.category == EventCategory.DISCOVERY) {
                weight *= 1.25f
            }
            if (expedition.squad.any { it.role == CharacterRole.ENGINEER } && event.category == EventCategory.TECHNICAL) {
                weight *= 1.25f
            }

            Pair(event, weight.coerceAtLeast(1.0f))
        }

        val totalWeight = weightedList.sumOf { it.second.toDouble() }
        if (totalWeight <= 0) return candidateEvents.firstOrNull()

        // 4. Deterministic weighted random roll
        val rng = Random(seed)
        val target = rng.nextDouble() * totalWeight

        var cumulative = 0.0
        for ((event, weight) in weightedList) {
            cumulative += weight
            if (target <= cumulative) {
                return event
            }
        }

        return weightedList.lastOrNull()?.first ?: candidateEvents.firstOrNull()
    }
}
