package com.example.domain.content.core

/**
 * Historical generation tracking model stored in GameState to support anti-repeat mechanics,
 * recency penalties, unique content exclusion, and cooldown enforcement across days.
 */
data class ContentGenerationHistory(
    val generatedCounts: Map<String, Int> = emptyMap(),
    val lastGeneratedDay: Map<String, Int> = emptyMap(),
    val recentGeneratedIds: List<String> = emptyList(),
    val uniqueIdsGenerated: Set<String> = emptySet(),
    val spawnedLocationIds: Set<String> = emptySet(),
    val activeRepeatableQuestIds: Set<String> = emptySet(),
    val maxRecentMemorySize: Int = 20
) {
    /**
     * Records the generation of a specific content definition ID.
     */
    fun recordGeneration(id: String, day: Int, isUnique: Boolean = false): ContentGenerationHistory {
        val count = (generatedCounts[id] ?: 0) + 1
        val updatedCounts = generatedCounts + (id to count)
        val updatedLastDay = lastGeneratedDay + (id to day)
        val updatedRecent = (listOf(id) + recentGeneratedIds.filter { it != id }).take(maxRecentMemorySize)
        val updatedUnique = if (isUnique) uniqueIdsGenerated + id else uniqueIdsGenerated

        return copy(
            generatedCounts = updatedCounts,
            lastGeneratedDay = updatedLastDay,
            recentGeneratedIds = updatedRecent,
            uniqueIdsGenerated = updatedUnique
        )
    }

    /**
     * Checks if a unique content definition was already generated previously in the game world.
     */
    fun isAlreadyGenerated(id: String): Boolean {
        return uniqueIdsGenerated.contains(id)
    }

    /**
     * Gets how many game days have passed since this definition was last generated.
     * Returns Int.MAX_VALUE if never generated.
     */
    fun daysSinceLastGeneration(id: String, currentDay: Int): Int {
        val lastDay = lastGeneratedDay[id] ?: return Int.MAX_VALUE
        return (currentDay - lastDay).coerceAtLeast(0)
    }

    /**
     * Calculates the recency penalty multiplier [0.1f .. 1.0f] for anti-repeat.
     */
    fun getRecencyMultiplier(id: String): Float {
        val index = recentGeneratedIds.indexOf(id)
        return when {
            index == -1 -> 1.0f
            index == 0 -> 0.1f  // Generated most recently
            index == 1 -> 0.25f
            index == 2 -> 0.45f
            index < 5 -> 0.7f
            else -> 0.9f
        }
    }
}
