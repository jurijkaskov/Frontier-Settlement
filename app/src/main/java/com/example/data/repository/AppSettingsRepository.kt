package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.core.settings.model.AppLanguage
import com.example.core.settings.model.AppSettings
import com.example.core.settings.model.DefaultAppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Thread-safe persistent repository managing user application preferences.
 * Fully decoupled from game progression saves.
 */
class AppSettingsRepository(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    fun getSettings(): AppSettings = _settingsFlow.value

    fun updateSettings(newSettings: AppSettings) {
        val validated = AppSettings(
            masterVolume = if (newSettings.masterVolume.isNaN()) 0.85f else newSettings.masterVolume.coerceIn(0f, 1f),
            musicVolume = if (newSettings.musicVolume.isNaN()) 0.70f else newSettings.musicVolume.coerceIn(0f, 1f),
            ambientVolume = if (newSettings.ambientVolume.isNaN()) 0.75f else newSettings.ambientVolume.coerceIn(0f, 1f),
            sfxVolume = if (newSettings.sfxVolume.isNaN()) 0.85f else newSettings.sfxVolume.coerceIn(0f, 1f),
            isMuted = newSettings.isMuted,
            isReducedMotion = newSettings.isReducedMotion,
            language = newSettings.language,
            showTutorialHints = newSettings.showTutorialHints,
            seenTutorialHints = newSettings.seenTutorialHints,
            confirmDayEnd = newSettings.confirmDayEnd,
            confirmDangerActions = newSettings.confirmDangerActions,
            enableDevToolsInMenu = newSettings.enableDevToolsInMenu,
            schemaVersion = DefaultAppSettings.CURRENT_SCHEMA_VERSION
        )
        _settingsFlow.value = validated
        saveSettings(validated)
    }

    fun setMasterVolume(volume: Float) {
        updateSettings(_settingsFlow.value.copy(masterVolume = volume))
    }

    fun setMusicVolume(volume: Float) {
        updateSettings(_settingsFlow.value.copy(musicVolume = volume))
    }

    fun setAmbientVolume(volume: Float) {
        updateSettings(_settingsFlow.value.copy(ambientVolume = volume))
    }

    fun setSfxVolume(volume: Float) {
        updateSettings(_settingsFlow.value.copy(sfxVolume = volume))
    }

    fun setMuted(muted: Boolean) {
        updateSettings(_settingsFlow.value.copy(isMuted = muted))
    }

    fun setReducedMotion(reducedMotion: Boolean) {
        updateSettings(_settingsFlow.value.copy(isReducedMotion = reducedMotion))
    }

    fun setLanguage(language: AppLanguage) {
        updateSettings(_settingsFlow.value.copy(language = language))
    }

    fun setShowTutorialHints(show: Boolean) {
        updateSettings(_settingsFlow.value.copy(showTutorialHints = show))
    }

    fun markTutorialHintSeen(hintId: String) {
        val currentSet = _settingsFlow.value.seenTutorialHints
        if (!currentSet.contains(hintId)) {
            val updated = currentSet + hintId
            updateSettings(_settingsFlow.value.copy(seenTutorialHints = updated))
        }
    }

    fun resetTutorialHints() {
        updateSettings(_settingsFlow.value.copy(seenTutorialHints = emptySet(), showTutorialHints = true))
    }

    fun setConfirmDayEnd(confirm: Boolean) {
        updateSettings(_settingsFlow.value.copy(confirmDayEnd = confirm))
    }

    fun setConfirmDangerActions(confirm: Boolean) {
        updateSettings(_settingsFlow.value.copy(confirmDangerActions = confirm))
    }

    fun setEnableDevTools(enable: Boolean) {
        updateSettings(_settingsFlow.value.copy(enableDevToolsInMenu = enable))
    }

    /**
     * Resets application preferences to default values without affecting GameState save files.
     */
    fun resetToDefaults() {
        val defaults = DefaultAppSettings.DEFAULT
        _settingsFlow.value = defaults
        saveSettings(defaults)
    }

    /**
     * Resets only audio parameters to their defaults.
     */
    fun resetAudioToDefaults() {
        val current = _settingsFlow.value
        val defaults = DefaultAppSettings.DEFAULT
        updateSettings(
            current.copy(
                masterVolume = defaults.masterVolume,
                musicVolume = defaults.musicVolume,
                ambientVolume = defaults.ambientVolume,
                sfxVolume = defaults.sfxVolume,
                isMuted = defaults.isMuted
            )
        )
    }

    private fun loadSettings(): AppSettings {
        return try {
            val master = prefs.getFloat(KEY_MASTER_VOL, 0.85f).takeIf { it.isFinite() } ?: 0.85f
            val music = prefs.getFloat(KEY_MUSIC_VOL, 0.70f).takeIf { it.isFinite() } ?: 0.70f
            val ambient = prefs.getFloat(KEY_AMBIENT_VOL, 0.75f).takeIf { it.isFinite() } ?: 0.75f
            val sfx = prefs.getFloat(KEY_SFX_VOL, 0.85f).takeIf { it.isFinite() } ?: 0.85f
            val muted = prefs.getBoolean(KEY_IS_MUTED, false)
            val reducedMotion = prefs.getBoolean(KEY_REDUCED_MOTION, false)
            val langCode = prefs.getString(KEY_LANGUAGE, AppLanguage.SYSTEM.code) ?: AppLanguage.SYSTEM.code
            val showHints = prefs.getBoolean(KEY_SHOW_HINTS, true)
            val seenHintsRaw = prefs.getStringSet(KEY_SEEN_HINTS, emptySet()) ?: emptySet()
            val confirmDay = prefs.getBoolean(KEY_CONFIRM_DAY_END, false)
            val confirmDanger = prefs.getBoolean(KEY_CONFIRM_DANGER, true)
            val devTools = prefs.getBoolean(KEY_DEV_TOOLS, true)
            val schemaVer = prefs.getInt(KEY_SCHEMA_VERSION, 1)

            AppSettings(
                masterVolume = master.coerceIn(0f, 1f),
                musicVolume = music.coerceIn(0f, 1f),
                ambientVolume = ambient.coerceIn(0f, 1f),
                sfxVolume = sfx.coerceIn(0f, 1f),
                isMuted = muted,
                isReducedMotion = reducedMotion,
                language = AppLanguage.fromCode(langCode),
                showTutorialHints = showHints,
                seenTutorialHints = seenHintsRaw,
                confirmDayEnd = confirmDay,
                confirmDangerActions = confirmDanger,
                enableDevToolsInMenu = devTools,
                schemaVersion = schemaVer
            )
        } catch (e: Exception) {
            DefaultAppSettings.DEFAULT
        }
    }

    private fun saveSettings(settings: AppSettings) {
        try {
            prefs.edit()
                .putFloat(KEY_MASTER_VOL, settings.masterVolume)
                .putFloat(KEY_MUSIC_VOL, settings.musicVolume)
                .putFloat(KEY_AMBIENT_VOL, settings.ambientVolume)
                .putFloat(KEY_SFX_VOL, settings.sfxVolume)
                .putBoolean(KEY_IS_MUTED, settings.isMuted)
                .putBoolean(KEY_REDUCED_MOTION, settings.isReducedMotion)
                .putString(KEY_LANGUAGE, settings.language.code)
                .putBoolean(KEY_SHOW_HINTS, settings.showTutorialHints)
                .putStringSet(KEY_SEEN_HINTS, settings.seenTutorialHints)
                .putBoolean(KEY_CONFIRM_DAY_END, settings.confirmDayEnd)
                .putBoolean(KEY_CONFIRM_DANGER, settings.confirmDangerActions)
                .putBoolean(KEY_DEV_TOOLS, settings.enableDevToolsInMenu)
                .putInt(KEY_SCHEMA_VERSION, settings.schemaVersion)
                .apply()
        } catch (e: Exception) {
            // Ignored to avoid crashes on edge cases
        }
    }

    companion object {
        private const val PREFS_NAME = "frontier_app_settings"

        private const val KEY_MASTER_VOL = "key_master_volume"
        private const val KEY_MUSIC_VOL = "key_music_volume"
        private const val KEY_AMBIENT_VOL = "key_ambient_volume"
        private const val KEY_SFX_VOL = "key_sfx_volume"
        private const val KEY_IS_MUTED = "key_is_muted"
        private const val KEY_REDUCED_MOTION = "key_reduced_motion"
        private const val KEY_LANGUAGE = "key_app_language"
        private const val KEY_SHOW_HINTS = "key_show_tutorial_hints"
        private const val KEY_SEEN_HINTS = "key_seen_tutorial_hints"
        private const val KEY_CONFIRM_DAY_END = "key_confirm_day_end"
        private const val KEY_CONFIRM_DANGER = "key_confirm_danger_actions"
        private const val KEY_DEV_TOOLS = "key_enable_dev_tools"
        private const val KEY_SCHEMA_VERSION = "key_schema_version"

        @Volatile
        private var instance: AppSettingsRepository? = null

        fun getInstance(context: Context): AppSettingsRepository {
            return instance ?: synchronized(this) {
                instance ?: AppSettingsRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
