package com.example.audio.registry

import android.content.Context
import com.example.audio.model.GameAmbientId
import com.example.audio.model.GameMusicId
import com.example.audio.model.GameSoundId
import com.example.audio.model.SoundCategory

data class AudioValidationItem(
    val id: String,
    val category: String,
    val resourceName: String,
    val isPresentInRaw: Boolean,
    val loop: Boolean,
    val defaultVolume: Float,
    val notes: String
)

data class AudioValidationReport(
    val totalSounds: Int,
    val totalMusic: Int,
    val totalAmbient: Int,
    val presentResourceCount: Int,
    val missingResourceCount: Int,
    val allSoundEnumsCovered: Boolean,
    val allMusicEnumsCovered: Boolean,
    val allAmbientEnumsCovered: Boolean,
    val items: List<AudioValidationItem>
)

object GameAudioAssetValidator {

    fun validate(context: Context?): AudioValidationReport {
        val items = mutableListOf<AudioValidationItem>()
        var presentCount = 0
        var missingCount = 0

        // 1. Sounds
        val soundEnums = GameSoundId.values().toSet()
        val allSounds = GameAudioRegistry.getAllSoundEntries()
        val registeredSoundIds = allSounds.map { it.soundId }.toSet()
        val soundsCovered = registeredSoundIds.containsAll(soundEnums)

        for (sound in allSounds) {
            val resId = context?.let { ctx ->
                ctx.resources.getIdentifier(sound.resourceName, "raw", ctx.packageName)
            } ?: 0
            val isPresent = resId != 0
            if (isPresent) presentCount++ else missingCount++

            items.add(
                AudioValidationItem(
                    id = sound.soundId.name,
                    category = sound.category.name,
                    resourceName = sound.resourceName,
                    isPresentInRaw = isPresent,
                    loop = sound.loop,
                    defaultVolume = sound.defaultVolume,
                    notes = if (isPresent) "Готов к воспроизведению" else "Fallback / синтез"
                )
            )
        }

        // 2. Music
        val musicEnums = GameMusicId.values().toSet()
        val allMusic = GameAudioRegistry.getAllMusicEntries()
        val registeredMusicIds = allMusic.map { it.musicId }.toSet()
        val musicCovered = registeredMusicIds.containsAll(musicEnums)

        for (music in allMusic) {
            val isSilent = music.musicId == GameMusicId.SILENT
            val resId = if (isSilent) 1 else context?.let { ctx ->
                ctx.resources.getIdentifier(music.resourceName, "raw", ctx.packageName)
            } ?: 0
            val isPresent = isSilent || resId != 0
            if (isPresent) presentCount++ else missingCount++

            items.add(
                AudioValidationItem(
                    id = music.musicId.name,
                    category = "MUSIC",
                    resourceName = music.resourceName,
                    isPresentInRaw = isPresent,
                    loop = music.loop,
                    defaultVolume = music.defaultVolume,
                    notes = if (isSilent) "Тишина (no-op)" else if (isPresent) "Готов к воспроизведению" else "Fallback / синтез"
                )
            )
        }

        // 3. Ambient
        val ambientEnums = GameAmbientId.values().toSet()
        val allAmbient = GameAudioRegistry.getAllAmbientEntries()
        val registeredAmbientIds = allAmbient.map { it.ambientId }.toSet()
        val ambientCovered = registeredAmbientIds.containsAll(ambientEnums)

        for (ambient in allAmbient) {
            val isSilent = ambient.ambientId == GameAmbientId.SILENT
            val resId = if (isSilent) 1 else context?.let { ctx ->
                ctx.resources.getIdentifier(ambient.resourceName, "raw", ctx.packageName)
            } ?: 0
            val isPresent = isSilent || resId != 0
            if (isPresent) presentCount++ else missingCount++

            items.add(
                AudioValidationItem(
                    id = ambient.ambientId.name,
                    category = "AMBIENT",
                    resourceName = ambient.resourceName,
                    isPresentInRaw = isPresent,
                    loop = ambient.loop,
                    defaultVolume = ambient.defaultVolume,
                    notes = if (isSilent) "Тишина (no-op)" else if (isPresent) "Готов к воспроизведению" else "Fallback / синтез"
                )
            )
        }

        return AudioValidationReport(
            totalSounds = allSounds.size,
            totalMusic = allMusic.size,
            totalAmbient = allAmbient.size,
            presentResourceCount = presentCount,
            missingResourceCount = missingCount,
            allSoundEnumsCovered = soundsCovered,
            allMusicEnumsCovered = musicCovered,
            allAmbientEnumsCovered = ambientCovered,
            items = items
        )
    }
}
