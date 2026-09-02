package com.example.audio

import com.example.audio.model.*
import com.example.audio.player.FakeGameAudioEngine
import com.example.audio.registry.GameAudioAssetValidator
import com.example.audio.registry.GameAudioRegistry
import com.example.audio.manager.GameAudioContextResolver
import com.example.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class AudioSystemTest {

    @Test
    fun testAudioSettings_VolumeCalculationAndMute() {
        val settings = AudioSettings(
            masterVolume = 0.8f,
            musicVolume = 0.5f,
            ambientVolume = 0.6f,
            sfxVolume = 0.7f,
            isMuted = false
        )

        // Effective volume checks: Master * Category
        assertEquals(0.40f, settings.effectiveMusicVolume, 0.001f)
        assertEquals(0.48f, settings.effectiveAmbientVolume, 0.001f)
        assertEquals(0.56f, settings.effectiveSfxVolume, 0.001f)
        assertEquals(0.56f, settings.effectiveUiVolume, 0.001f)

        // Custom relative volume scaling
        val finalMusic = settings.calculateFinalVolume(SoundCategory.MUSIC, relativeVolume = 0.9f)
        assertEquals(0.8f * 0.5f * 0.9f, finalMusic, 0.001f)

        // Mute overrides all channels to 0
        val mutedSettings = settings.copy(isMuted = true)
        assertEquals(0f, mutedSettings.effectiveMusicVolume, 0.0001f)
        assertEquals(0f, mutedSettings.effectiveAmbientVolume, 0.0001f)
        assertEquals(0f, mutedSettings.effectiveSfxVolume, 0.0001f)
        assertEquals(0f, mutedSettings.calculateFinalVolume(SoundCategory.SFX, 1.0f), 0.0001f)
    }

    @Test
    fun testGameAudioRegistry_EnumCoverageAndIntegrity() {
        // Verify all GameSoundId enums have valid registry entries
        for (soundId in GameSoundId.values()) {
            val entry = GameAudioRegistry.getSoundEntry(soundId)
            assertNotNull("Missing entry for $soundId", entry)
            assertTrue("Resource name cannot be empty for $soundId", entry.resourceName.isNotBlank())
            assertTrue("Default volume must be in (0..1]", entry.defaultVolume in 0.01f..1.0f)
        }

        // Verify all GameMusicId enums
        for (musicId in GameMusicId.values()) {
            val entry = GameAudioRegistry.getMusicEntry(musicId)
            assertNotNull("Missing entry for $musicId", entry)
            assertTrue("Resource name cannot be empty for $musicId", entry.resourceName.isNotBlank())
        }

        // Verify all GameAmbientId enums
        for (ambientId in GameAmbientId.values()) {
            val entry = GameAudioRegistry.getAmbientEntry(ambientId)
            assertNotNull("Missing entry for $ambientId", entry)
            assertTrue("Resource name cannot be empty for $ambientId", entry.resourceName.isNotBlank())
        }
    }

    @Test
    fun testGameAudioRegistry_VariantSelectionAndPitchJitter() {
        // Melee sound has 3 variants
        val resName1 = GameAudioRegistry.resolveSoundResourceName(GameSoundId.COMBAT_ATTACK_MELEE)
        assertTrue("Variant should be one of melee samples", resName1.startsWith("sfx_combat_melee_"))

        // Pitch jitter test
        val jitterAllowed = GameAudioRegistry.calculatePitchJitter(allowPitch = true)
        assertTrue("Pitch jitter must be within [0.95, 1.05]", jitterAllowed in 0.95f..1.05f)

        val jitterDisallowed = GameAudioRegistry.calculatePitchJitter(allowPitch = false)
        assertEquals("Disabled pitch jitter must equal 1.0f", 1.0f, jitterDisallowed, 0.0001f)
    }

    @Test
    fun testGameAudioContextResolver_SettlementDayNight() {
        // Day state
        val dayState = GameState(
            gameDateTime = GameDateTime(day = 1, hour = 14, minute = 0)
        )
        val dayProfile = GameAudioContextResolver.resolveProfile("settlement", dayState)
        assertEquals(GameAudioContext.SETTLEMENT, dayProfile.context)
        assertEquals(GameMusicId.SETTLEMENT, dayProfile.musicId)
        assertEquals(GameAmbientId.SETTLEMENT_DAY, dayProfile.ambientId)

        // Night state
        val nightState = GameState(
            gameDateTime = GameDateTime(day = 1, hour = 23, minute = 30)
        )
        val nightProfile = GameAudioContextResolver.resolveProfile("settlement", nightState)
        assertEquals(GameAudioContext.SETTLEMENT, nightProfile.context)
        assertEquals(GameAmbientId.SETTLEMENT_NIGHT, nightProfile.ambientId)
    }

    @Test
    fun testGameAudioContextResolver_CombatOverrideAndLocations() {
        // Map route
        val mapProfile = GameAudioContextResolver.resolveProfile("map", null)
        assertEquals(GameAudioContext.WORLD_MAP, mapProfile.context)
        assertEquals(GameMusicId.WORLD_MAP, mapProfile.musicId)

        // Combat route
        val combatProfile = GameAudioContextResolver.resolveProfile("combat", null)
        assertEquals(GameAudioContext.COMBAT, combatProfile.context)
        assertEquals(GameMusicId.COMBAT, combatProfile.musicId)
        assertEquals(GameAmbientId.SILENT, combatProfile.ambientId)

        // Event route
        val eventProfile = GameAudioContextResolver.resolveProfile("event", null)
        assertEquals(GameAudioContext.EVENT, eventProfile.context)
        assertTrue("Event profile must enable ducking", eventProfile.enableDucking)
    }

    @Test
    fun testFakeGameAudioEngine_ExecutionAndStateTracking() {
        val fakeEngine = FakeGameAudioEngine()

        // Initial state
        assertEquals(GameAudioContext.SETTLEMENT, fakeEngine.currentContext.value)

        // Context switch
        fakeEngine.setContext(GameAudioContext.COMBAT)
        assertEquals(GameAudioContext.COMBAT, fakeEngine.currentContext.value)
        assertEquals(GameMusicId.COMBAT, fakeEngine.currentMusicId.value)

        // Play SFX
        fakeEngine.playSfx(GameSoundId.UI_CLICK)
        fakeEngine.playSfx(GameSoundId.COMBAT_SHOT_PISTOL)
        assertEquals(2, fakeEngine.playedSfxLog.size)
        assertEquals(GameSoundId.UI_CLICK, fakeEngine.playedSfxLog[0])
        assertEquals(GameSoundId.COMBAT_SHOT_PISTOL, fakeEngine.playedSfxLog[1])

        // Background / Foreground lifecycle
        fakeEngine.onAppBackground()
        assertEquals(1, fakeEngine.backgroundCallCount)
        assertEquals("BACKGROUND_PAUSED", fakeEngine.audioFocusState.value)

        fakeEngine.onAppForeground()
        assertEquals(1, fakeEngine.foregroundCallCount)
        assertEquals("FOCUSED", fakeEngine.audioFocusState.value)
    }

    @Test
    fun testGameAudioAssetValidator_ValidationReport() {
        val report = GameAudioAssetValidator.validate(context = null)
        assertTrue("Sound enums must be 100% covered", report.allSoundEnumsCovered)
        assertTrue("Music enums must be 100% covered", report.allMusicEnumsCovered)
        assertTrue("Ambient enums must be 100% covered", report.allAmbientEnumsCovered)
        assertTrue("Total registered sounds should be > 30", report.totalSounds >= 30)
        assertTrue("Report items must match total asset count", report.items.size == report.totalSounds + report.totalMusic + report.totalAmbient)
    }
}
