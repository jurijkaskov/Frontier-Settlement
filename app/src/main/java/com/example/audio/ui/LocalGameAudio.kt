package com.example.audio.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.audio.model.GameSoundId
import com.example.audio.player.FakeGameAudioEngine
import com.example.audio.player.GameAudioEngine

/**
 * CompositionLocal providing access to the [GameAudioEngine] in Compose UI tree.
 */
val LocalGameAudio = staticCompositionLocalOf<GameAudioEngine> {
    FakeGameAudioEngine()
}

/**
 * Convenience helper to play a UI click or interaction sound.
 */
@Composable
fun rememberAudioPlayer(): (GameSoundId) -> Unit {
    val engine = LocalGameAudio.current
    return { soundId ->
        engine.playSfx(soundId)
    }
}
