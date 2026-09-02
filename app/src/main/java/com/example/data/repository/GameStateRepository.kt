package com.example.data.repository

import com.example.core.log.GameLogger
import com.example.data.InitialGameData
import com.example.data.save.*
import com.example.data.save.coordinator.GameSaveCoordinator
import com.example.data.source.GameSaveDataSource
import com.example.data.source.InMemoryGameSaveDataSource
import com.example.domain.model.GameState
import com.example.domain.validator.GameStateNormalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Single source of truth for the entire game state.
 * Manages reactive observation ([StateFlow]), atomic concurrent updates ([Mutex]),
 * and persistence coordination with multi-slot support.
 */
interface GameStateRepository {

    /**
     * Reactive read-only stream of the authoritative [GameState].
     */
    val gameState: StateFlow<GameState>

    /**
     * Immediate snapshot of the current authoritative [GameState].
     */
    val currentGameState: GameState

    /**
     * Dedicated coordinator for persistent operations.
     */
    val coordinator: GameSaveCoordinator

    /**
     * Atomically transforms the current [GameState] within a coroutine-safe [Mutex] lock.
     * Prevents race conditions and lost updates between concurrent operations.
     */
    suspend fun updateGameState(transform: (GameState) -> GameState): GameState

    /**
     * Synchronously transforms the current [GameState] with atomic swap semantics.
     */
    fun updateGameStateSync(transform: (GameState) -> GameState): GameState

    /**
     * Triggers persistence of the current [GameState] to the default autosave slot.
     */
    suspend fun saveGame(): Boolean

    /**
     * Saves game state to the autosave slot with critical milestone flag.
     */
    suspend fun saveAutosave(isCritical: Boolean = false): SaveOperationResult

    /**
     * Saves snapshot of current game state into a designated manual slot.
     */
    suspend fun saveSlot(slotId: String, displayName: String): SaveOperationResult

    /**
     * Loads the saved [GameState] from persistent storage for default autosave.
     */
    suspend fun loadGame(): GameState

    /**
     * Loads a specific save slot and applies state normalizations.
     */
    suspend fun loadSlot(slotId: String): SaveLoadResult

    /**
     * Deletes a save slot.
     */
    suspend fun deleteSlot(slotId: String): Boolean

    /**
     * Retrieves metadata for a specific save slot.
     */
    suspend fun getSlotMetadata(slotId: String): SaveMetadata?

    /**
     * Retrieves metadata for all registered save slots.
     */
    suspend fun getAllMetadata(): Map<String, SaveMetadata>

    /**
     * Restores a save slot from backup.
     */
    suspend fun restoreFromBackup(
        backupSlotId: String = SaveSlotId.AUTOSAVE_BACKUP.id,
        targetSlotId: String = SaveSlotId.AUTOSAVE.id
    ): SaveLoadResult

    /**
     * Intentionally corrupts a slot for testing recovery flows.
     */
    suspend fun corruptSlotForTesting(slotId: String): Boolean

    /**
     * Resets the game state back to the initial default state.
     */
    fun resetGame(initialState: GameState = InitialGameData.createInitialGameState()): GameState
}

/**
 * Default production implementation of [GameStateRepository].
 */
class DefaultGameStateRepository(
    private val saveDataSource: GameSaveDataSource = InMemoryGameSaveDataSource(),
    initialState: GameState = InitialGameData.createInitialGameState()
) : GameStateRepository {

    private val mutex = Mutex()
    private val _gameState = MutableStateFlow(initialState)
    override val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    override val currentGameState: GameState
        get() = _gameState.value

    override val coordinator: GameSaveCoordinator = GameSaveCoordinator(saveDataSource)

    override suspend fun updateGameState(transform: (GameState) -> GameState): GameState {
        return mutex.withLock {
            val oldState = _gameState.value
            val newState = transform(oldState)
            _gameState.value = newState
            GameLogger.d("GameStateRepository", "Atomic update: Day ${newState.day}, Time ${newState.gameDateTime}")
            newState
        }
    }

    override fun updateGameStateSync(transform: (GameState) -> GameState): GameState {
        synchronized(this) {
            val oldState = _gameState.value
            val newState = transform(oldState)
            _gameState.value = newState
            return newState
        }
    }

    override suspend fun saveGame(): Boolean {
        return saveAutosave(isCritical = false) is SaveOperationResult.Success
    }

    override suspend fun saveAutosave(isCritical: Boolean): SaveOperationResult {
        val currentState = currentGameState
        return coordinator.saveAutosave(currentState, isCritical = isCritical)
    }

    override suspend fun saveSlot(slotId: String, displayName: String): SaveOperationResult {
        val currentState = currentGameState
        return coordinator.saveManualSlot(slotId, displayName, currentState)
    }

    override suspend fun loadGame(): GameState {
        return when (val res = loadSlot(SaveSlotId.AUTOSAVE.id)) {
            is SaveLoadResult.Success -> res.saveFile.gameState
            else -> {
                val initial = InitialGameData.createInitialGameState()
                _gameState.value = initial
                initial
            }
        }
    }

    override suspend fun loadSlot(slotId: String): SaveLoadResult {
        val result = coordinator.loadSlot(slotId)
        if (result is SaveLoadResult.Success) {
            val normResult = GameStateNormalizer.normalize(result.saveFile.gameState)
            _gameState.value = normResult.normalizedState
            GameLogger.i("GameStateRepository", "Loaded slot '$slotId' successfully (Day ${normResult.normalizedState.day})")
            return SaveLoadResult.Success(
                saveFile = result.saveFile.copy(gameState = normResult.normalizedState),
                loadedFromBackup = result.loadedFromBackup
            )
        }
        return result
    }

    override suspend fun deleteSlot(slotId: String): Boolean {
        return coordinator.deleteSlot(slotId)
    }

    override suspend fun getSlotMetadata(slotId: String): SaveMetadata? {
        return coordinator.getSlotMetadata(slotId)
    }

    override suspend fun getAllMetadata(): Map<String, SaveMetadata> {
        return coordinator.getAllMetadata()
    }

    override suspend fun restoreFromBackup(backupSlotId: String, targetSlotId: String): SaveLoadResult {
        val res = coordinator.restoreFromBackup(backupSlotId, targetSlotId)
        if (res is SaveLoadResult.Success) {
            val norm = GameStateNormalizer.normalize(res.saveFile.gameState)
            _gameState.value = norm.normalizedState
            return SaveLoadResult.Success(
                saveFile = res.saveFile.copy(gameState = norm.normalizedState),
                loadedFromBackup = true
            )
        }
        return res
    }

    override suspend fun corruptSlotForTesting(slotId: String): Boolean {
        return coordinator.corruptSlotForTesting(slotId)
    }

    override fun resetGame(initialState: GameState): GameState {
        synchronized(this) {
            _gameState.value = initialState
            return initialState
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: DefaultGameStateRepository? = null

        fun getInstance(
            saveDataSource: GameSaveDataSource = InMemoryGameSaveDataSource()
        ): DefaultGameStateRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DefaultGameStateRepository(saveDataSource).also { INSTANCE = it }
            }
        }

        fun resetInstanceForTesting(
            repository: DefaultGameStateRepository? = null
        ) {
            INSTANCE = repository
        }
    }
}
