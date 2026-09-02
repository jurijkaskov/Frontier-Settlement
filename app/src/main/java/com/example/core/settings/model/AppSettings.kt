package com.example.core.settings.model

import com.example.audio.model.AudioSettings

/**
 * Supported application UI languages.
 */
enum class AppLanguage(val code: String, val titleRu: String, val nativeName: String) {
    SYSTEM("system", "Системный язык", "System Default"),
    RUSSIAN("ru", "Русский", "Русский"),
    ENGLISH("en", "Английский (English)", "English");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: SYSTEM
        }
    }
}

/**
 * Single immutable source of truth for device-level application preferences.
 * Separated strictly from [com.example.domain.model.GameState] save files.
 */
data class AppSettings(
    val masterVolume: Float = 0.85f,
    val musicVolume: Float = 0.70f,
    val ambientVolume: Float = 0.75f,
    val sfxVolume: Float = 0.85f,
    val isMuted: Boolean = false,
    val isReducedMotion: Boolean = false,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val showTutorialHints: Boolean = true,
    val seenTutorialHints: Set<String> = emptySet(),
    val confirmDayEnd: Boolean = false,
    val confirmDangerActions: Boolean = true,
    val enableDevToolsInMenu: Boolean = true,
    val schemaVersion: Int = 1
) {
    init {
        require(masterVolume.isFinite() && masterVolume in 0f..1f) { "masterVolume must be in 0f..1f" }
        require(musicVolume.isFinite() && musicVolume in 0f..1f) { "musicVolume must be in 0f..1f" }
        require(ambientVolume.isFinite() && ambientVolume in 0f..1f) { "ambientVolume must be in 0f..1f" }
        require(sfxVolume.isFinite() && sfxVolume in 0f..1f) { "sfxVolume must be in 0f..1f" }
    }

    val effectiveMusicVolume: Float
        get() = if (isMuted) 0f else (masterVolume * musicVolume).coerceIn(0f, 1f)

    val effectiveAmbientVolume: Float
        get() = if (isMuted) 0f else (masterVolume * ambientVolume).coerceIn(0f, 1f)

    val effectiveSfxVolume: Float
        get() = if (isMuted) 0f else (masterVolume * sfxVolume).coerceIn(0f, 1f)

    fun toAudioSettings(): AudioSettings {
        return AudioSettings(
            masterVolume = masterVolume,
            musicVolume = musicVolume,
            ambientVolume = ambientVolume,
            sfxVolume = sfxVolume,
            isMuted = isMuted
        )
    }

    fun isHintSeen(hintId: String): Boolean = seenTutorialHints.contains(hintId)
}

/**
 * Baseline default preferences for clean initial launch and resetting.
 */
object DefaultAppSettings {
    const val CURRENT_SCHEMA_VERSION = 1
    val DEFAULT = AppSettings()
}
