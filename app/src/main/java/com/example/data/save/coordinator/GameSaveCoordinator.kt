package com.example.data.save.coordinator

import com.example.core.log.GameLogger
import com.example.data.save.*
import com.example.data.source.GameSaveDataSource
import com.example.data.source.InMemoryGameSaveDataSource
import com.example.domain.model.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Coordinates persistent write/read requests with mutex serialization and debounce policies.
 */
class GameSaveCoordinator(
    private val dataSource: GameSaveDataSource = InMemoryGameSaveDataSource()
) {
    private val saveMutex = Mutex()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _lastSaveResult = MutableStateFlow<SaveOperationResult?>(null)
    val lastSaveResult: StateFlow<SaveOperationResult?> = _lastSaveResult.asStateFlow()

    /**
     * Executes an autosave of current [GameState].
     * @param isCritical If true, forces immediate write and logs as a critical milestone save point.
     */
    suspend fun saveAutosave(
        gameState: GameState,
        isCritical: Boolean = false
    ): SaveOperationResult {
        return saveMutex.withLock {
            _isSaving.value = true
            try {
                if (isCritical) {
                    GameLogger.i("GameSaveCoordinator", "CRITICAL AUTOSAVE point triggered (Day ${gameState.day})")
                }
                val saveFile = GameSaveFile.create(
                    slotId = SaveSlotId.AUTOSAVE.id,
                    displayName = SaveSlotId.AUTOSAVE.displayName,
                    gameState = gameState
                )
                val result = dataSource.saveSlot(SaveSlotId.AUTOSAVE.id, saveFile)
                _lastSaveResult.value = result
                result
            } catch (e: Exception) {
                GameLogger.e("GameSaveCoordinator", "Autosave failed", e)
                val failure = SaveOperationResult.Failure(SaveSlotId.AUTOSAVE.id, e.message ?: "Autosave failed", e)
                _lastSaveResult.value = failure
                failure
            } finally {
                _isSaving.value = false
            }
        }
    }

    /**
     * Saves a snapshot of [GameState] to a designated manual slot.
     */
    suspend fun saveManualSlot(
        slotId: String,
        displayName: String,
        gameState: GameState
    ): SaveOperationResult {
        return saveMutex.withLock {
            _isSaving.value = true
            try {
                val saveFile = GameSaveFile.create(
                    slotId = slotId,
                    displayName = displayName,
                    gameState = gameState
                )
                val result = dataSource.saveSlot(slotId, saveFile)
                _lastSaveResult.value = result
                result
            } catch (e: Exception) {
                GameLogger.e("GameSaveCoordinator", "Manual save to $slotId failed", e)
                val failure = SaveOperationResult.Failure(slotId, e.message ?: "Manual save failed", e)
                _lastSaveResult.value = failure
                failure
            } finally {
                _isSaving.value = false
            }
        }
    }

    /**
     * Loads a save slot.
     */
    suspend fun loadSlot(slotId: String): SaveLoadResult {
        return saveMutex.withLock {
            dataSource.loadSlot(slotId)
        }
    }

    /**
     * Deletes a save slot.
     */
    suspend fun deleteSlot(slotId: String): Boolean {
        return saveMutex.withLock {
            dataSource.deleteSlot(slotId)
        }
    }

    /**
     * Reads metadata for a specific slot.
     */
    suspend fun getSlotMetadata(slotId: String): SaveMetadata? {
        return dataSource.getSlotMetadata(slotId)
    }

    /**
     * Reads metadata for all registered slots.
     */
    suspend fun getAllMetadata(): Map<String, SaveMetadata> {
        return dataSource.getAllMetadata()
    }

    /**
     * Restores a slot from its backup.
     */
    suspend fun restoreFromBackup(
        backupSlotId: String = SaveSlotId.AUTOSAVE_BACKUP.id,
        targetSlotId: String = SaveSlotId.AUTOSAVE.id
    ): SaveLoadResult {
        return saveMutex.withLock {
            dataSource.restoreFromBackup(backupSlotId, targetSlotId)
        }
    }

    /**
     * Corrupts slot for recovery testing.
     */
    suspend fun corruptSlotForTesting(slotId: String): Boolean {
        return saveMutex.withLock {
            dataSource.corruptSlotForTesting(slotId)
        }
    }
}
