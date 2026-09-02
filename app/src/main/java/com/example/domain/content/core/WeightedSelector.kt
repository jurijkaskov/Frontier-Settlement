package com.example.domain.content.core

import kotlin.random.Random

/**
 * Functional modifier applied to an item's candidate selection weight.
 */
fun interface WeightModifier<T> {
    fun modifyWeight(item: T, baseWeight: Float, context: ContentGenerationContext): Float
}

/**
 * Robust, deterministic weighted selection utility.
 */
object WeightedSelector {

    /**
     * Selects a single item from the candidate list using weighted probabilities.
     */
    fun <T> select(
        candidates: List<T>,
        weightExtractor: (T) -> Float,
        random: Random,
        context: ContentGenerationContext? = null,
        modifiers: List<WeightModifier<T>> = emptyList()
    ): T? {
        if (candidates.isEmpty()) return null
        if (candidates.size == 1) return candidates.first()

        val weightedCandidates = candidates.map { item ->
            var w = weightExtractor(item).coerceAtLeast(0f)
            if (context != null) {
                for (mod in modifiers) {
                    w = mod.modifyWeight(item, w, context).coerceAtLeast(0f)
                }
            }
            Pair(item, w)
        }

        val totalWeight = weightedCandidates.sumOf { it.second.toDouble() }
        if (totalWeight <= 0.0001) {
            // Fallback: pick uniformly
            return candidates[random.nextInt(candidates.size)]
        }

        val roll = random.nextDouble() * totalWeight
        var accumulated = 0.0

        for ((item, weight) in weightedCandidates) {
            accumulated += weight
            if (roll <= accumulated) {
                return item
            }
        }

        return weightedCandidates.last().first
    }

    /**
     * Selects up to [count] distinct items from the candidate list without replacement.
     */
    fun <T> selectMultipleWithoutReplacement(
        candidates: List<T>,
        count: Int,
        weightExtractor: (T) -> Float,
        random: Random,
        context: ContentGenerationContext? = null,
        modifiers: List<WeightModifier<T>> = emptyList()
    ): List<T> {
        if (candidates.isEmpty() || count <= 0) return emptyList()
        if (candidates.size <= count) return candidates.shuffled(random)

        val remaining = candidates.toMutableList()
        val result = mutableListOf<T>()

        for (i in 0 until count) {
            if (remaining.isEmpty()) break
            val selected = select(remaining, weightExtractor, random, context, modifiers) ?: break
            result.add(selected)
            remaining.remove(selected)
        }

        return result
    }
}
