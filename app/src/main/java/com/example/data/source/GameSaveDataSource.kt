package com.example.data.source

import com.example.core.log.GameLogger
import com.example.data.InitialGameData
import com.example.data.save.*
import com.example.domain.model.GameState
import java.util.concurrent.ConcurrentHashMap

/**
 * Data source interface responsible for reading, writing, caching, and backing up persistent game saves.
 */
interface GameSaveDataSource {
    suspend fun saveSlot(slotId: String, saveFile: GameSaveFile): SaveOperationResult
    suspend fun loadSlot(slotId: String): SaveLoadResult
    suspend fun hasSlot(slotId: String): Boolean
    suspend fun deleteSlot(slotId: String): Boolean
    suspend fun getSlotMetadata(slotId: String): SaveMetadata?
    suspend fun getAllMetadata(): Map<String, SaveMetadata>
    suspend fun createBackup(sourceSlotId: String = SaveSlotId.AUTOSAVE.id, backupSlotId: String = SaveSlotId.AUTOSAVE_BACKUP.id): Boolean
    suspend fun restoreFromBackup(backupSlotId: String = SaveSlotId.AUTOSAVE_BACKUP.id, targetSlotId: String = SaveSlotId.AUTOSAVE.id): SaveLoadResult
    suspend fun corruptSlotForTesting(slotId: String): Boolean

    // Legacy / simple convenience methods
    suspend fun save(gameState: GameState): Boolean = saveSlot(
        SaveSlotId.AUTOSAVE.id,
        GameSaveFile.create(SaveSlotId.AUTOSAVE.id, SaveSlotId.AUTOSAVE.displayName, gameState)
    ) is SaveOperationResult.Success

    suspend fun load(): GameState = when (val res = loadSlot(SaveSlotId.AUTOSAVE.id)) {
        is SaveLoadResult.Success -> res.saveFile.gameState
        else -> InitialGameData.createInitialGameState()
    }

    suspend fun clear(): Boolean = deleteSlot(SaveSlotId.AUTOSAVE.id)
}

/**
 * Memory-backed implementation of [GameSaveDataSource] designed for fast unit testing and isolated environments.
 */
class InMemoryGameSaveDataSource(
    private val initialProvider: () -> GameState = { InitialGameData.createInitialGameState() }
) : GameSaveDataSource {

    private val slots = ConcurrentHashMap<String, GameSaveFile>()
    private val corruptedSlots = ConcurrentHashMap.newKeySet<String>()

    override suspend fun saveSlot(slotId: String, saveFile: GameSaveFile): SaveOperationResult {
        return try {
            corruptedSlots.remove(slotId)
            slots[slotId] = saveFile
            GameLogger.d("InMemoryGameSaveDataSource", "Saved slot $slotId successfully (Day ${saveFile.gameState.day})")
            SaveOperationResult.Success(saveFile.metadata)
        } catch (e: Exception) {
            GameLogger.e("InMemoryGameSaveDataSource", "Failed to save slot $slotId", e)
            SaveOperationResult.Failure(slotId, e.message ?: "Failed to save slot", e)
        }
    }

    override suspend fun loadSlot(slotId: String): SaveLoadResult {
        if (corruptedSlots.contains(slotId)) {
            val hasBackup = slots.containsKey(SaveSlotId.AUTOSAVE_BACKUP.id)
            return SaveLoadResult.Corrupted(slotId, "Slot $slotId marked as corrupted in memory", canTryBackup = hasBackup)
        }

        val file = slots[slotId]
        if (file == null) {
            if (slotId == SaveSlotId.AUTOSAVE.id) {
                // Initialize default autosave if not present
                val initial = initialProvider()
                val newFile = GameSaveFile.create(SaveSlotId.AUTOSAVE.id, SaveSlotId.AUTOSAVE.displayName, initial)
                slots[slotId] = newFile
                return SaveLoadResult.Success(newFile)
            }
            return SaveLoadResult.NotFound(slotId)
        }

        return SaveLoadResult.Success(file)
    }

    override suspend fun hasSlot(slotId: String): Boolean {
        return slots.containsKey(slotId) && !corruptedSlots.contains(slotId)
    }

    override suspend fun deleteSlot(slotId: String): Boolean {
        slots.remove(slotId)
        corruptedSlots.remove(slotId)
        return true
    }

    override suspend fun getSlotMetadata(slotId: String): SaveMetadata? {
        if (corruptedSlots.contains(slotId)) {
            return slots[slotId]?.metadata?.copy(isCorrupted = true)
        }
        return slots[slotId]?.metadata
    }

    override suspend fun getAllMetadata(): Map<String, SaveMetadata> {
        val result = mutableMapOf<String, SaveMetadata>()
        for ((slotId, file) in slots) {
            val isCorrupt = corruptedSlots.contains(slotId)
            result[slotId] = file.metadata.copy(isCorrupted = isCorrupt)
        }
        return result
    }

    override suspend fun createBackup(sourceSlotId: String, backupSlotId: String): Boolean {
        val source = slots[sourceSlotId] ?: return false
        slots[backupSlotId] = source.copy(slotId = backupSlotId)
        return true
    }

    override suspend fun restoreFromBackup(backupSlotId: String, targetSlotId: String): SaveLoadResult {
        val backup = slots[backupSlotId] ?: return SaveLoadResult.NotFound(backupSlotId)
        val restored = backup.copy(slotId = targetSlotId)
        slots[targetSlotId] = restored
        corruptedSlots.remove(targetSlotId)
        return SaveLoadResult.Success(restored, loadedFromBackup = true)
    }

    override suspend fun corruptSlotForTesting(slotId: String): Boolean {
        corruptedSlots.add(slotId)
        return true
    }
}
