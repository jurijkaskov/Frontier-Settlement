package com.example.audio.player

import com.example.audio.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory test double of [GameAudioEngine] for Robolectric and JVM unit tests.
 * Records all invocation events for deterministic assertion without requiring Android Audio hardware.
 */
class FakeGameAudioEngine(
    initialSettings: AudioSettings = AudioSettings.Default
) : GameAudioEngine {

    private val _currentContext = MutableStateFlow(GameAudioContext.SETTLEMENT)
    override val currentContext: StateFlow<GameAudioContext> = _currentContext.asStateFlow()

    private val _currentMusicId = MutableStateFlow(GameMusicId.SETTLEMENT)
    override val currentMusicId: StateFlow<GameMusicId> = _currentMusicId.asStateFlow()

    private val _currentAmbientId = MutableStateFlow(GameAmbientId.SETTLEMENT_DAY)
    override val currentAmbientId: StateFlow<GameAmbientId> = _currentAmbientId.asStateFlow()

    private val _audioSettings = MutableStateFlow(initialSettings)
    override val audioSettings: StateFlow<AudioSettings> = _audioSettings.asStateFlow()

    private val _audioFocusState = MutableStateFlow("FOCUSED")
    override val audioFocusState: StateFlow<String> = _audioFocusState.asStateFlow()

    override val isMuted: Boolean get() = _audioSettings.value.isMuted

    val playedSfxLog = mutableListOf<GameSoundId>()
    val musicChangeLog = mutableListOf<GameMusicId>()
    val ambientChangeLog = mutableListOf<GameAmbientId>()
    var backgroundCallCount = 0
    var foregroundCallCount = 0
    var releaseCallCount = 0

    override fun setContext(context: GameAudioContext, customProfile: GameAudioProfile?) {
        _currentContext.value = context
        val profile = customProfile ?: when (context) {
            GameAudioContext.MAIN_MENU -> GameAudioProfile.MainMenu
            GameAudioContext.SETTLEMENT -> GameAudioProfile.SettlementDay
            GameAudioContext.WORLD_MAP -> GameAudioProfile.WorldMap
            GameAudioContext.TRAVEL -> GameAudioProfile.Travel
            GameAudioContext.LOCATION -> GameAudioProfile.LocationRuins
            GameAudioContext.EVENT -> GameAudioProfile.Event
            GameAudioContext.COMBAT -> GameAudioProfile.Combat
            GameAudioContext.LOOT -> GameAudioProfile.Loot
            GameAudioContext.RETURN_SUMMARY -> GameAudioProfile.ReturnSummary
        }
        playMusic(profile.musicId, profile.crossfadeDurationMs)
        setAmbient(profile.ambientId, profile.crossfadeDurationMs)
    }

    override fun playSfx(soundId: GameSoundId, relativeVolume: Float) {
        playedSfxLog.add(soundId)
    }

    override fun playMusic(musicId: GameMusicId, crossfadeMs: Int) {
        _currentMusicId.value = musicId
        musicChangeLog.add(musicId)
    }

    override fun setAmbient(ambientId: GameAmbientId, crossfadeMs: Int) {
        _currentAmbientId.value = ambientId
        ambientChangeLog.add(ambientId)
    }

    override fun stopMusic(fadeMs: Int) {
        _currentMusicId.value = GameMusicId.SILENT
    }

    override fun stopAmbient(fadeMs: Int) {
        _currentAmbientId.value = GameAmbientId.SILENT
    }

    override fun updateSettings(settings: AudioSettings) {
        _audioSettings.value = settings
    }

    override fun setMuted(muted: Boolean) {
        _audioSettings.value = _audioSettings.value.copy(isMuted = muted)
    }

    override fun onAppBackground() {
        backgroundCallCount++
        _audioFocusState.value = "BACKGROUND_PAUSED"
    }

    override fun onAppForeground() {
        foregroundCallCount++
        _audioFocusState.value = "FOCUSED"
    }

    override fun release() {
        releaseCallCount++
    }

    fun clearLogs() {
        playedSfxLog.clear()
        musicChangeLog.clear()
        ambientChangeLog.clear()
    }
}
