package com.example.audio.manager

import android.content.Context
import com.example.audio.model.AudioSettings
import com.example.core.settings.model.AppSettings
import com.example.data.repository.AppSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Audio persistence bridge that seamlessly coordinates with [AppSettingsRepository]
 * as the unified single source of truth for all application preferences.
 */
class AudioSettingsRepository(private val context: Context) {

    private val appSettingsRepo = AppSettingsRepository.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    private val _settingsFlow = MutableStateFlow(appSettingsRepo.getSettings().toAudioSettings())
    val settingsFlow: StateFlow<AudioSettings> = _settingsFlow.asStateFlow()

    init {
        scope.launch {
            appSettingsRepo.settingsFlow.collect { appSettings ->
                _settingsFlow.value = appSettings.toAudioSettings()
            }
        }
    }

    fun getSettings(): AudioSettings = _settingsFlow.value

    fun updateSettings(newSettings: AudioSettings) {
        val currentApp = appSettingsRepo.getSettings()
        val updatedApp = currentApp.copy(
            masterVolume = newSettings.masterVolume.coerceIn(0f, 1f),
            musicVolume = newSettings.musicVolume.coerceIn(0f, 1f),
            ambientVolume = newSettings.ambientVolume.coerceIn(0f, 1f),
            sfxVolume = newSettings.sfxVolume.coerceIn(0f, 1f),
            isMuted = newSettings.isMuted
        )
        appSettingsRepo.updateSettings(updatedApp)
    }

    fun setMasterVolume(volume: Float) {
        appSettingsRepo.setMasterVolume(volume)
    }

    fun setMusicVolume(volume: Float) {
        appSettingsRepo.setMusicVolume(volume)
    }

    fun setAmbientVolume(volume: Float) {
        appSettingsRepo.setAmbientVolume(volume)
    }

    fun setSfxVolume(volume: Float) {
        appSettingsRepo.setSfxVolume(volume)
    }

    fun setMuted(muted: Boolean) {
        appSettingsRepo.setMuted(muted)
    }

    fun resetToDefaults() {
        appSettingsRepo.resetAudioToDefaults()
    }
}
