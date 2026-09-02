package com.example.audio.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import com.example.audio.model.*
import com.example.audio.player.*
import com.example.domain.model.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Primary coordinator and facade for all audio playback, atmospheric transitions,
 * audio focus handling, and lifecycle events in Frontier Settlement.
 */
class GameAudioManager(
    private val context: Context,
    private val settingsRepo: AudioSettingsRepository = AudioSettingsRepository(context)
) : GameAudioEngine {

    private val tag = "GameAudioManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val musicPlayer = MusicPlayer(context)
    private var sfxPlayer: SfxPlayer? = null
    private val ambientPlayer = AmbientPlayer(context) { sfxPlayer }

    init {
        sfxPlayer = SfxPlayer(context)
    }

    private val _currentContext = MutableStateFlow(GameAudioContext.SETTLEMENT)
    override val currentContext: StateFlow<GameAudioContext> = _currentContext.asStateFlow()

    private val _currentMusicId = MutableStateFlow(GameMusicId.SETTLEMENT)
    override val currentMusicId: StateFlow<GameMusicId> = _currentMusicId.asStateFlow()

    private val _currentAmbientId = MutableStateFlow(GameAmbientId.SETTLEMENT_DAY)
    override val currentAmbientId: StateFlow<GameAmbientId> = _currentAmbientId.asStateFlow()

    override val audioSettings: StateFlow<AudioSettings> = settingsRepo.settingsFlow

    override val isMuted: Boolean
        get() = audioSettings.value.isMuted

    private val _audioFocusState = MutableStateFlow("FOCUSED")
    override val audioFocusState: StateFlow<String> = _audioFocusState.asStateFlow()

    private val systemAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                _audioFocusState.value = "FOCUSED"
                resumeAll()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                _audioFocusState.value = "LOST"
                pauseAll()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                _audioFocusState.value = "LOST_TRANSIENT"
                pauseAll()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                _audioFocusState.value = "DUCKING"
                // Temporarily reduce volume
                applyDucking(0.3f)
            }
        }
    }

    init {
        // Observe settings changes to sync players
        scope.launch {
            settingsRepo.settingsFlow.collect { settings ->
                musicPlayer.updateSettings(settings)
                ambientPlayer.updateSettings(settings)
                sfxPlayer?.updateSettings(settings)
            }
        }
        requestSystemAudioFocus()
    }

    private fun requestSystemAudioFocus() {
        val audioManager = systemAudioManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build()
                audioFocusRequest = request
                audioManager.requestAudioFocus(request)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to request audio focus", e)
        }
    }

    private fun abandonSystemAudioFocus() {
        val audioManager = systemAudioManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(audioFocusChangeListener)
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to abandon audio focus", e)
        }
    }

    fun syncWithGameState(currentRoute: String, gameState: GameState?) {
        val profile = GameAudioContextResolver.resolveProfile(currentRoute, gameState)
        setContext(profile.context, profile)
    }

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
        sfxPlayer?.play(soundId, relativeVolume)
    }

    override fun playMusic(musicId: GameMusicId, crossfadeMs: Int) {
        _currentMusicId.value = musicId
        musicPlayer.play(musicId, crossfadeMs)
    }

    override fun setAmbient(ambientId: GameAmbientId, crossfadeMs: Int) {
        _currentAmbientId.value = ambientId
        ambientPlayer.play(ambientId, crossfadeMs)
    }

    override fun stopMusic(fadeMs: Int) {
        _currentMusicId.value = GameMusicId.SILENT
        musicPlayer.stop(fadeMs)
    }

    override fun stopAmbient(fadeMs: Int) {
        _currentAmbientId.value = GameAmbientId.SILENT
        ambientPlayer.stop(fadeMs)
    }

    override fun updateSettings(settings: AudioSettings) {
        settingsRepo.updateSettings(settings)
    }

    override fun setMuted(muted: Boolean) {
        settingsRepo.setMuted(muted)
    }

    private fun applyDucking(factor: Float) {
        val current = audioSettings.value
        val ducked = current.copy(
            musicVolume = (current.musicVolume * factor).coerceIn(0f, 1f),
            ambientVolume = (current.ambientVolume * factor).coerceIn(0f, 1f)
        )
        musicPlayer.updateSettings(ducked)
        ambientPlayer.updateSettings(ducked)
    }

    private fun pauseAll() {
        musicPlayer.pause()
        ambientPlayer.pause()
    }

    private fun resumeAll() {
        val current = audioSettings.value
        musicPlayer.updateSettings(current)
        ambientPlayer.updateSettings(current)
        musicPlayer.resume()
        ambientPlayer.resume()
    }

    override fun onAppBackground() {
        _audioFocusState.value = "BACKGROUND_PAUSED"
        pauseAll()
    }

    override fun onAppForeground() {
        _audioFocusState.value = "FOCUSED"
        resumeAll()
    }

    override fun release() {
        abandonSystemAudioFocus()
        scope.cancel()
        musicPlayer.release()
        ambientPlayer.release()
        sfxPlayer?.release()
        sfxPlayer = null
    }

    companion object {
        @Volatile
        private var instance: GameAudioManager? = null

        fun getInstance(context: Context): GameAudioManager {
            return instance ?: synchronized(this) {
                instance ?: GameAudioManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
