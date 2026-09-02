package com.example.audio.player

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.SystemClock
import android.util.Log
import com.example.audio.model.AudioSettings
import com.example.audio.model.GameAudioConfig
import com.example.audio.model.GameSoundId
import com.example.audio.model.SoundCategory
import com.example.audio.registry.AudioPriority
import com.example.audio.registry.GameAudioRegistry

/**
 * High-performance SoundPool SFX player with cooldown spam protection, pitch jitter, and priority management.
 */
class SfxPlayer(private val context: Context) {

    private val tag = "SfxPlayer"

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(GameAudioConfig.MAX_SIMULTANEOUS_SFX)
        .setAudioAttributes(audioAttributes)
        .build()

    private val loadedResMap = mutableMapOf<String, Int>() // resourceName -> soundPoolSampleId
    private val lastPlayedTimeMap = mutableMapOf<GameSoundId, Long>()
    private var currentSettings: AudioSettings = AudioSettings.Default

    init {
        preloadCommonSounds()
    }

    private fun preloadCommonSounds() {
        val prioritySounds = listOf(
            GameSoundId.UI_CLICK,
            GameSoundId.UI_CONFIRM,
            GameSoundId.UI_CANCEL,
            GameSoundId.COMBAT_HIT,
            GameSoundId.COMBAT_SHOT_PISTOL
        )
        for (soundId in prioritySounds) {
            val entry = GameAudioRegistry.getSoundEntry(soundId)
            loadResourceIfPresent(entry.resourceName)
        }
    }

    private fun loadResourceIfPresent(resourceName: String): Int {
        loadedResMap[resourceName]?.let { return it }

        val resId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        if (resId == 0) return 0

        try {
            val sampleId = soundPool.load(context, resId, 1)
            loadedResMap[resourceName] = sampleId
            return sampleId
        } catch (e: Exception) {
            Log.w(tag, "Failed to load sample '$resourceName' into SoundPool", e)
            return 0
        }
    }

    fun play(soundId: GameSoundId, relativeVolume: Float = 1.0f) {
        if (currentSettings.isMuted) return

        // 1. Spam Cooldown check (prevent audio chaos from 20 clicks in 50ms)
        val now = SystemClock.uptimeMillis()
        val lastPlayed = lastPlayedTimeMap[soundId] ?: 0L
        if (now - lastPlayed < GameAudioConfig.SFX_SPAM_COOLDOWN_MS) {
            return
        }
        lastPlayedTimeMap[soundId] = now

        // 2. Resolve resource name (picks variant if defined via Presentation RNG)
        val entry = GameAudioRegistry.getSoundEntry(soundId)
        val resName = GameAudioRegistry.resolveSoundResourceName(soundId)

        // 3. Load or get sample ID
        val sampleId = loadResourceIfPresent(resName)
        if (sampleId == 0) {
            // Safe fallback / no-op
            return
        }

        // 4. Calculate final volume & pitch
        val category = entry.category
        val finalVolume = currentSettings.calculateFinalVolume(category, entry.defaultVolume * relativeVolume)
        if (finalVolume <= 0.001f) return

        val rate = GameAudioRegistry.calculatePitchJitter(entry.pitchVariationAllowed)
        val priority = when (entry.priority) {
            AudioPriority.HIGH -> 2
            AudioPriority.NORMAL -> 1
            AudioPriority.LOW -> 0
        }

        try {
            soundPool.play(sampleId, finalVolume, finalVolume, priority, 0, rate)
        } catch (e: Exception) {
            Log.w(tag, "Failed to play sound: ${soundId.name}", e)
        }
    }

    fun updateSettings(settings: AudioSettings) {
        currentSettings = settings
    }

    fun release() {
        try {
            soundPool.release()
            loadedResMap.clear()
            lastPlayedTimeMap.clear()
        } catch (e: Exception) {
            Log.w(tag, "Error releasing SoundPool", e)
        }
    }
}
