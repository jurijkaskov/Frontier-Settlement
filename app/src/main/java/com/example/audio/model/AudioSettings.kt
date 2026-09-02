package com.example.audio.model

/**
 * User-configurable persistent audio settings.
 */
data class AudioSettings(
    val masterVolume: Float = 0.85f,
    val musicVolume: Float = 0.70f,
    val ambientVolume: Float = 0.75f,
    val sfxVolume: Float = 0.85f,
    val isMuted: Boolean = false
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

    val effectiveUiVolume: Float
        get() = effectiveSfxVolume

    fun calculateFinalVolume(category: SoundCategory, relativeVolume: Float = 1.0f): Float {
        if (isMuted) return 0f
        val catVolume = when (category) {
            SoundCategory.MUSIC -> musicVolume
            SoundCategory.AMBIENT -> ambientVolume
            SoundCategory.SFX, SoundCategory.UI -> sfxVolume
        }
        return (masterVolume * catVolume * relativeVolume).coerceIn(0f, 1f)
    }

    companion object {
        val Default = AudioSettings()
    }
}

/**
 * Static engine constraints and tuning parameters for the audio layer.
 */
object GameAudioConfig {
    const val MAX_SIMULTANEOUS_SFX: Int = 8
    const val DEFAULT_CROSSFADE_MS: Int = 1200
    const val FAST_CROSSFADE_MS: Int = 600
    const val SFX_SPAM_COOLDOWN_MS: Long = 75L
    const val PITCH_VARIATION_DELTA: Float = 0.04f // +/- 4% pitch shift for repetitive SFX
    const val AMBIENT_RANDOM_STING_MIN_INTERVAL_MS: Long = 18000L // Min 18s between atmospheric one-shots
    const val AMBIENT_RANDOM_STING_MAX_INTERVAL_MS: Long = 45000L // Max 45s
}
