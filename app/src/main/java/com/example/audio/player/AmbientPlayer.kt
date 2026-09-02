package com.example.audio.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.audio.model.AudioSettings
import com.example.audio.model.GameAmbientId
import com.example.audio.model.GameAudioConfig
import com.example.audio.model.GameSoundId
import com.example.audio.model.SoundCategory
import com.example.audio.registry.GameAudioRegistry
import kotlin.random.Random

/**
 * Continuous environmental audio manager with day/night profile transitions and subtle atmospheric one-shots.
 */
class AmbientPlayer(
    private val context: Context,
    private val sfxPlayerProvider: () -> SfxPlayer?
) {
    private val tag = "AmbientPlayer"
    private var currentPlayer: MediaPlayer? = null
    private var fadingPlayer: MediaPlayer? = null
    private var currentAmbientId: GameAmbientId = GameAmbientId.SILENT
    private var currentSettings: AudioSettings = AudioSettings.Default
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isPausedDueToLifecycle: Boolean = false

    private val audioPresentationRandom = Random(System.currentTimeMillis() + 42)

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val randomStingRunnable = object : Runnable {
        override fun run() {
            scheduleNextRandomSting()
            triggerAtmosphericSting()
        }
    }

    fun play(ambientId: GameAmbientId, crossfadeMs: Int = GameAudioConfig.DEFAULT_CROSSFADE_MS) {
        if (ambientId == currentAmbientId && currentPlayer?.isPlaying == true) {
            return
        }

        currentAmbientId = ambientId

        if (ambientId == GameAmbientId.SILENT) {
            stop(crossfadeMs)
            return
        }

        val entry = GameAudioRegistry.getAmbientEntry(ambientId)
        val resId = context.resources.getIdentifier(entry.resourceName, "raw", context.packageName)

        if (resId == 0) {
            Log.d(tag, "Ambient resource '${entry.resourceName}' not found in res/raw. Silent fallback.")
            stop(crossfadeMs)
            return
        }

        try {
            val newPlayer = MediaPlayer.create(context, resId) ?: run {
                Log.w(tag, "Failed to create MediaPlayer for ambient '${entry.resourceName}'")
                return
            }

            newPlayer.setAudioAttributes(audioAttributes)
            newPlayer.isLooping = entry.loop

            val targetVolume = currentSettings.calculateFinalVolume(SoundCategory.AMBIENT, entry.defaultVolume)
            newPlayer.setVolume(0f, 0f)
            newPlayer.start()

            val oldPlayer = currentPlayer
            fadingPlayer?.release()
            fadingPlayer = oldPlayer
            currentPlayer = newPlayer

            crossfade(
                incoming = newPlayer,
                outgoing = oldPlayer,
                targetVolume = targetVolume,
                durationMs = crossfadeMs
            )

            // Start atmospheric random sting scheduler if profile has stings
            scheduleNextRandomSting()

        } catch (e: Exception) {
            Log.w(tag, "Error playing ambient loop: ${ambientId.name}", e)
        }
    }

    private fun crossfade(
        incoming: MediaPlayer,
        outgoing: MediaPlayer?,
        targetVolume: Float,
        durationMs: Int
    ) {
        val steps = 20
        val stepInterval = (durationMs / steps).coerceAtLeast(16).toLong()
        var step = 0

        val runnable = object : Runnable {
            override fun run() {
                step++
                val progress = (step.toFloat() / steps.toFloat()).coerceIn(0f, 1f)

                try {
                    val inVol = targetVolume * progress
                    incoming.setVolume(inVol, inVol)

                    outgoing?.let { out ->
                        if (out.isPlaying) {
                            val outVol = (targetVolume * (1f - progress)).coerceAtLeast(0f)
                            out.setVolume(outVol, outVol)
                        }
                    }
                } catch (e: Exception) {
                    // Ignored
                }

                if (step < steps) {
                    mainHandler.postDelayed(this, stepInterval)
                } else {
                    try {
                        outgoing?.stop()
                        outgoing?.release()
                        if (fadingPlayer == outgoing) fadingPlayer = null
                    } catch (e: Exception) {
                        // Ignored
                    }
                }
            }
        }
        mainHandler.post(runnable)
    }

    fun stop(fadeMs: Int = GameAudioConfig.FAST_CROSSFADE_MS) {
        currentAmbientId = GameAmbientId.SILENT
        mainHandler.removeCallbacks(randomStingRunnable)
        val player = currentPlayer ?: return
        currentPlayer = null

        val steps = 10
        val interval = (fadeMs / steps).coerceAtLeast(16).toLong()
        var step = 0
        val startVol = currentSettings.calculateFinalVolume(SoundCategory.AMBIENT)

        val runnable = object : Runnable {
            override fun run() {
                step++
                val progress = (step.toFloat() / steps.toFloat()).coerceIn(0f, 1f)
                val vol = (startVol * (1f - progress)).coerceAtLeast(0f)
                try {
                    player.setVolume(vol, vol)
                } catch (e: Exception) {
                    // Ignored
                }

                if (step < steps) {
                    mainHandler.postDelayed(this, interval)
                } else {
                    try {
                        player.stop()
                        player.release()
                    } catch (e: Exception) {
                        // Ignored
                    }
                }
            }
        }
        mainHandler.post(runnable)
    }

    private fun scheduleNextRandomSting() {
        mainHandler.removeCallbacks(randomStingRunnable)
        val entry = GameAudioRegistry.getAmbientEntry(currentAmbientId)
        if (entry.randomStingSoundIds.isEmpty() || currentSettings.isMuted) return

        val minMs = GameAudioConfig.AMBIENT_RANDOM_STING_MIN_INTERVAL_MS
        val maxMs = GameAudioConfig.AMBIENT_RANDOM_STING_MAX_INTERVAL_MS
        val delay = minMs + (audioPresentationRandom.nextFloat() * (maxMs - minMs)).toLong()
        mainHandler.postDelayed(randomStingRunnable, delay)
    }

    private fun triggerAtmosphericSting() {
        val entry = GameAudioRegistry.getAmbientEntry(currentAmbientId)
        if (entry.randomStingSoundIds.isEmpty()) return
        val chosenSound = entry.randomStingSoundIds[audioPresentationRandom.nextInt(entry.randomStingSoundIds.size)]
        // Play with subtle volume (0.4f)
        sfxPlayerProvider()?.play(chosenSound, relativeVolume = 0.4f)
    }

    fun updateSettings(settings: AudioSettings) {
        currentSettings = settings
        val entry = GameAudioRegistry.getAmbientEntry(currentAmbientId)
        val finalVolume = settings.calculateFinalVolume(SoundCategory.AMBIENT, entry.defaultVolume)
        try {
            currentPlayer?.setVolume(finalVolume, finalVolume)
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun pause() {
        try {
            if (currentPlayer?.isPlaying == true) {
                currentPlayer?.pause()
                isPausedDueToLifecycle = true
            }
            mainHandler.removeCallbacks(randomStingRunnable)
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun resume() {
        try {
            if (isPausedDueToLifecycle && currentPlayer != null) {
                currentPlayer?.start()
                isPausedDueToLifecycle = false
                scheduleNextRandomSting()
            }
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun release() {
        mainHandler.removeCallbacksAndMessages(null)
        try {
            currentPlayer?.stop()
            currentPlayer?.release()
            fadingPlayer?.stop()
            fadingPlayer?.release()
        } catch (e: Exception) {
            // Ignored
        } finally {
            currentPlayer = null
            fadingPlayer = null
        }
    }

    fun getCurrentAmbientId(): GameAmbientId = currentAmbientId
}
