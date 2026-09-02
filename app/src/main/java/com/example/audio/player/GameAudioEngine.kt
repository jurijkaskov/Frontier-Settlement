package com.example.audio.player

import com.example.audio.model.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Common contract for game audio playback and lifecycle management.
 */
interface GameAudioEngine {
    val currentContext: StateFlow<GameAudioContext>
    val currentMusicId: StateFlow<GameMusicId>
    val currentAmbientId: StateFlow<GameAmbientId>
    val audioSettings: StateFlow<AudioSettings>
    val isMuted: Boolean
    val audioFocusState: StateFlow<String>

    fun setContext(context: GameAudioContext, customProfile: GameAudioProfile? = null)
    fun playSfx(soundId: GameSoundId, relativeVolume: Float = 1.0f)
    fun playMusic(musicId: GameMusicId, crossfadeMs: Int = GameAudioConfig.DEFAULT_CROSSFADE_MS)
    fun setAmbient(ambientId: GameAmbientId, crossfadeMs: Int = GameAudioConfig.DEFAULT_CROSSFADE_MS)
    fun stopMusic(fadeMs: Int = GameAudioConfig.FAST_CROSSFADE_MS)
    fun stopAmbient(fadeMs: Int = GameAudioConfig.FAST_CROSSFADE_MS)
    fun updateSettings(settings: AudioSettings)
    fun setMuted(muted: Boolean)
    fun onAppBackground()
    fun onAppForeground()
    fun release()
}
