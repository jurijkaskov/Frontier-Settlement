package com.example.data.save

/**
 * Result of saving, deleting, or manipulating a save slot.
 */
sealed interface SaveOperationResult {
    data class Success(
        val metadata: SaveMetadata,
        val message: String = "Игра успешно сохранена"
    ) : SaveOperationResult

    data class Failure(
        val slotId: String,
        val message: String,
        val cause: Throwable? = null
    ) : SaveOperationResult
}
