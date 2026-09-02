package com.example.audio.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.audio.model.AudioSettings
import com.example.audio.model.GameAudioConfig
import com.example.audio.model.GameMusicId
import com.example.audio.model.SoundCategory
import com.example.audio.registry.GameAudioRegistry

/**
 * Robust dual-instance MediaPlayer wrapper supporting smooth volume crossfading and looping.
 */
class MusicPlayer(private val context: Context) {

    private val tag = "MusicPlayer"
    private var currentPlayer: MediaPlayer? = null
    private var fadingPlayer: MediaPlayer? = null
    private var currentMusicId: GameMusicId = GameMusicId.SILENT
    private var currentSettings: AudioSettings = AudioSettings.Default
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isPausedDueToLifecycle: Boolean = false

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    fun play(musicId: GameMusicId, crossfadeMs: Int = GameAudioConfig.DEFAULT_CROSSFADE_MS) {
        if (musicId == currentMusicId && currentPlayer?.isPlaying == true) {
            return
        }

        currentMusicId = musicId

        if (musicId == GameMusicId.SILENT) {
            stop(crossfadeMs)
            return
        }

        val entry = GameAudioRegistry.getMusicEntry(musicId)
        val resId = context.resources.getIdentifier(entry.resourceName, "raw", context.packageName)

        if (resId == 0) {
            Log.d(tag, "Music resource '${entry.resourceName}' not found in res/raw. Silent fallback.")
            stop(crossfadeMs)
            return
        }

        try {
            val newPlayer = MediaPlayer.create(context, resId) ?: run {
                Log.w(tag, "Failed to create MediaPlayer for '${entry.resourceName}'")
                return
            }

            newPlayer.setAudioAttributes(audioAttributes)
            newPlayer.isLooping = entry.loop

            // Start crossfade: old player fades out, new player fades in
            val targetVolume = currentSettings.calculateFinalVolume(SoundCategory.MUSIC, entry.defaultVolume)
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

        } catch (e: Exception) {
            Log.w(tag, "Error playing music track: ${musicId.name}", e)
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
                    // Ignored if released
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
        currentMusicId = GameMusicId.SILENT
        val player = currentPlayer ?: return
        currentPlayer = null

        val steps = 10
        val interval = (fadeMs / steps).coerceAtLeast(16).toLong()
        var step = 0
        val startVol = currentSettings.calculateFinalVolume(SoundCategory.MUSIC)

        val runnable = object : Runnable {
            override fun run() {
                step++
                val progress = (step.toFloat() / steps.toFloat()).coerceIn(0f, 1f)
                val vol = (startVol * (1f - progress)).coerceAtLeast(0f)
                try {
                    player.setVolume(vol, vol)
                } catch (e: Exception) {
                    // player might be already released
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

    fun updateSettings(settings: AudioSettings) {
        currentSettings = settings
        val entry = GameAudioRegistry.getMusicEntry(currentMusicId)
        val finalVolume = settings.calculateFinalVolume(SoundCategory.MUSIC, entry.defaultVolume)
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
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun resume() {
        try {
            if (isPausedDueToLifecycle && currentPlayer != null) {
                currentPlayer?.start()
                isPausedDueToLifecycle = false
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

    fun getCurrentMusicId(): GameMusicId = currentMusicId
}
