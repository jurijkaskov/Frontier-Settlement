package com.example.data.save

/**
 * Result of loading a game save slot.
 */
sealed interface SaveLoadResult {
    data class Success(
        val saveFile: GameSaveFile,
        val loadedFromBackup: Boolean = false
    ) : SaveLoadResult

    data class NotFound(
        val slotId: String
    ) : SaveLoadResult

    data class MigrationRequired(
        val slotId: String,
        val sourceVersion: Int,
        val targetVersion: Int
    ) : SaveLoadResult

    data class Corrupted(
        val slotId: String,
        val error: String,
        val canTryBackup: Boolean = false
    ) : SaveLoadResult

    data class UnsupportedNewerVersion(
        val slotId: String,
        val saveVersion: Int,
        val appMaxVersion: Int = GameSaveConstants.CURRENT_SAVE_SCHEMA_VERSION
    ) : SaveLoadResult

    data class StorageError(
        val slotId: String,
        val message: String,
        val cause: Throwable? = null
    ) : SaveLoadResult
}
