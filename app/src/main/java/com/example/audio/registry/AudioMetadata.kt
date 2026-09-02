package com.example.audio.registry

import com.example.audio.model.GameAmbientId
import com.example.audio.model.GameMusicId
import com.example.audio.model.GameSoundId
import com.example.audio.model.SoundCategory

enum class AudioPriority {
    HIGH,   // Combat actions, player deaths, major quest completion
    NORMAL, // Standard UI clicks, loot picks, building upgrades
    LOW     // Atmospheric random stings, subtle background steps
}

data class SoundAssetEntry(
    val soundId: GameSoundId,
    val resourceName: String,
    val category: SoundCategory = soundId.category,
    val defaultVolume: Float = soundId.defaultRelativeVolume,
    val loop: Boolean = false,
    val priority: AudioPriority = AudioPriority.NORMAL,
    val variantResourceNames: List<String> = emptyList(),
    val pitchVariationAllowed: Boolean = false,
    val description: String = soundId.descriptionRu
)

data class MusicAssetEntry(
    val musicId: GameMusicId,
    val resourceName: String,
    val defaultVolume: Float = musicId.defaultRelativeVolume,
    val loop: Boolean = musicId.loopable,
    val description: String = musicId.titleRu
)

data class AmbientAssetEntry(
    val ambientId: GameAmbientId,
    val resourceName: String,
    val defaultVolume: Float = ambientId.defaultRelativeVolume,
    val loop: Boolean = ambientId.loopable,
    val randomStingSoundIds: List<GameSoundId> = emptyList(),
    val description: String = ambientId.titleRu
)
