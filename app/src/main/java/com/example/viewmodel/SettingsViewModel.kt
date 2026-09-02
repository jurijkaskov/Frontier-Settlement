package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.core.settings.model.AppLanguage
import com.example.core.settings.model.AppSettings
import com.example.data.repository.AppSettingsRepository
import com.example.data.repository.DefaultGameStateRepository
import com.example.data.repository.GameStateRepository
import com.example.data.save.GameSaveConstants
import com.example.data.save.SaveMetadata
import com.example.data.save.SaveOperationResult
import com.example.data.save.SaveSlotId
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Immutable UI State for the Settings and preferences screens.
 */
data class SettingsUiState(
    val masterVolume: Float = 0.85f,
    val musicVolume: Float = 0.70f,
    val ambientVolume: Float = 0.75f,
    val sfxVolume: Float = 0.85f,
    val isMuted: Boolean = false,
    val isReducedMotion: Boolean = false,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val showTutorialHints: Boolean = true,
    val confirmDayEnd: Boolean = false,
    val confirmDangerActions: Boolean = true,
    val enableDevToolsInMenu: Boolean = true,
    val latestAutosaveMetadata: SaveMetadata? = null,
    val isSaving: Boolean = false,
    val lastSaveResult: SaveOperationResult? = null,
    val appVersionName: String = BuildConfig.VERSION_NAME,
    val appVersionCode: Int = BuildConfig.VERSION_CODE,
    val saveSchemaVersion: Int = GameSaveConstants.CURRENT_SAVE_SCHEMA_VERSION,
    val isDebugBuild: Boolean = BuildConfig.DEBUG
)

class SettingsViewModel(
    application: Application,
    private val appSettingsRepository: AppSettingsRepository = AppSettingsRepository.getInstance(application),
    private val gameStateRepository: GameStateRepository = DefaultGameStateRepository.getInstance()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
        observeSaveCoordinator()
        refreshAutosaveMetadata()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            appSettingsRepository.settingsFlow.collect { settings ->
                _uiState.update { current ->
                    current.copy(
                        masterVolume = settings.masterVolume,
                        musicVolume = settings.musicVolume,
                        ambientVolume = settings.ambientVolume,
                        sfxVolume = settings.sfxVolume,
                        isMuted = settings.isMuted,
                        isReducedMotion = settings.isReducedMotion,
                        language = settings.language,
                        showTutorialHints = settings.showTutorialHints,
                        confirmDayEnd = settings.confirmDayEnd,
                        confirmDangerActions = settings.confirmDangerActions,
                        enableDevToolsInMenu = settings.enableDevToolsInMenu
                    )
                }
            }
        }
    }

    private fun observeSaveCoordinator() {
        viewModelScope.launch {
            gameStateRepository.coordinator.isSaving.collect { isSaving ->
                _uiState.update { it.copy(isSaving = isSaving) }
            }
        }
        viewModelScope.launch {
            gameStateRepository.coordinator.lastSaveResult.collect { result ->
                _uiState.update { it.copy(lastSaveResult = result) }
            }
        }
    }

    fun refreshAutosaveMetadata() {
        viewModelScope.launch {
            val allMeta = gameStateRepository.getAllMetadata()
            val autosaveMeta = allMeta[SaveSlotId.AUTOSAVE.id] ?: allMeta.values.maxByOrNull { it.updatedAt }
            _uiState.update { it.copy(latestAutosaveMetadata = autosaveMeta) }
        }
    }

    fun setMasterVolume(vol: Float) = appSettingsRepository.setMasterVolume(vol)
    fun setMusicVolume(vol: Float) = appSettingsRepository.setMusicVolume(vol)
    fun setAmbientVolume(vol: Float) = appSettingsRepository.setAmbientVolume(vol)
    fun setSfxVolume(vol: Float) = appSettingsRepository.setSfxVolume(vol)
    fun setMuted(muted: Boolean) = appSettingsRepository.setMuted(muted)
    fun setReducedMotion(reduced: Boolean) = appSettingsRepository.setReducedMotion(reduced)
    fun setLanguage(lang: AppLanguage) = appSettingsRepository.setLanguage(lang)
    fun setShowTutorialHints(show: Boolean) = appSettingsRepository.setShowTutorialHints(show)
    fun setConfirmDayEnd(confirm: Boolean) = appSettingsRepository.setConfirmDayEnd(confirm)
    fun setConfirmDangerActions(confirm: Boolean) = appSettingsRepository.setConfirmDangerActions(confirm)
    fun setEnableDevTools(enable: Boolean) = appSettingsRepository.setEnableDevTools(enable)

    fun resetSettingsToDefaults() {
        appSettingsRepository.resetToDefaults()
    }

    fun resetAudioToDefaults() {
        appSettingsRepository.resetAudioToDefaults()
    }

    fun resetTutorialHints() {
        appSettingsRepository.resetTutorialHints()
    }

    fun saveNow() {
        viewModelScope.launch {
            gameStateRepository.saveAutosave(isCritical = true)
            refreshAutosaveMetadata()
        }
    }

    fun fullResetAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            // 1. Delete all save files
            SaveSlotId.entries.forEach { slot ->
                gameStateRepository.deleteSlot(slot.id)
            }
            // 2. Reset runtime GameState
            gameStateRepository.resetGame()
            // 3. Reset application settings
            appSettingsRepository.resetToDefaults()
            // 4. Trigger completion
            onComplete()
        }
    }
}

