package com.example.data.save

import com.example.domain.model.GameState

/**
 * Top-level wrapper for serialized game save files.
 * Contains schema metadata, integrity checksums, and the core [GameState].
 */
data class GameSaveFile(
    val schemaVersion: Int = GameSaveConstants.CURRENT_SAVE_SCHEMA_VERSION,
    val gameVersion: String = GameSaveConstants.DEFAULT_GAME_VERSION,
    val saveId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val playthroughId: String,
    val slotId: String,
    val metadata: SaveMetadata,
    val gameState: GameState,
    val checksum: String = ""
) {
    companion object {
        fun create(
            slotId: String,
            displayName: String,
            gameState: GameState,
            saveId: String = "save_${System.currentTimeMillis()}",
            schemaVersion: Int = GameSaveConstants.CURRENT_SAVE_SCHEMA_VERSION,
            gameVersion: String = GameSaveConstants.DEFAULT_GAME_VERSION
        ): GameSaveFile {
            val now = System.currentTimeMillis()
            val meta = SaveMetadata.fromGameState(
                slotId = slotId,
                displayName = displayName,
                gameState = gameState,
                saveId = saveId,
                schemaVersion = schemaVersion,
                gameVersion = gameVersion
            )
            return GameSaveFile(
                schemaVersion = schemaVersion,
                gameVersion = gameVersion,
                saveId = saveId,
                createdAt = now,
                updatedAt = now,
                playthroughId = gameState.playthroughId,
                slotId = slotId,
                metadata = meta,
                gameState = gameState
            )
        }
    }
}
