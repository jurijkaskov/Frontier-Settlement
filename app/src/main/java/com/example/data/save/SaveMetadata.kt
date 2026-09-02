package com.example.data.save

import com.example.core.format.GameFormatters
import com.example.domain.model.GameState

/**
 * Lightweight header model representing save slot metadata.
 * Loaded quickly without parsing full GameState hierarchy.
 */
data class SaveMetadata(
    val saveId: String,
    val slotId: String,
    val displayName: String,
    val gameDay: Int,
    val gameTimeFormatted: String,
    val settlementName: String,
    val settlementLevel: Int,
    val updatedAt: Long = System.currentTimeMillis(),
    val playthroughId: String,
    val schemaVersion: Int = GameSaveConstants.CURRENT_SAVE_SCHEMA_VERSION,
    val gameVersion: String = GameSaveConstants.DEFAULT_GAME_VERSION,
    val locationContext: SaveLocationContext = SaveLocationContext.IN_SETTLEMENT,
    val locationName: String = "Аванпост-7",
    val isCorrupted: Boolean = false,
    val fileSizeBytes: Long = 0L
) {
    companion object {
        fun fromGameState(
            slotId: String,
            displayName: String,
            gameState: GameState,
            saveId: String = "save_${System.currentTimeMillis()}",
            schemaVersion: Int = GameSaveConstants.CURRENT_SAVE_SCHEMA_VERSION,
            gameVersion: String = GameSaveConstants.DEFAULT_GAME_VERSION,
            fileSizeBytes: Long = 0L
        ): SaveMetadata {
            val context = SaveLocationContext.fromGameState(gameState)
            val locName = when (context) {
                SaveLocationContext.IN_SETTLEMENT -> gameState.settlement.name
                SaveLocationContext.TRAVELING -> gameState.activeTravel?.toLocationId ?: "Пустошь"
                SaveLocationContext.AT_LOCATION,
                SaveLocationContext.EXPLORING,
                SaveLocationContext.IN_COMBAT,
                SaveLocationContext.EVENT_ACTIVE,
                SaveLocationContext.LOOT_ACTIVE -> gameState.activeExpedition?.location?.name ?: gameState.currentLocation?.name ?: "Пустошь"
                SaveLocationContext.RETURNING -> "Возвращение в ${gameState.settlement.name}"
            }

            return SaveMetadata(
                saveId = saveId,
                slotId = slotId,
                displayName = displayName,
                gameDay = gameState.day,
                gameTimeFormatted = gameState.gameDateTime.formattedTime,
                settlementName = gameState.settlement.name,
                settlementLevel = gameState.settlement.level,
                updatedAt = System.currentTimeMillis(),
                playthroughId = gameState.playthroughId,
                schemaVersion = schemaVersion,
                gameVersion = gameVersion,
                locationContext = context,
                locationName = locName,
                isCorrupted = false,
                fileSizeBytes = fileSizeBytes
            )
        }
    }
}
