package com.example.audio.registry

import com.example.audio.model.GameAmbientId
import com.example.audio.model.GameMusicId
import com.example.audio.model.GameSoundId
import com.example.audio.model.SoundCategory
import kotlin.random.Random

/**
 * Central registry mapping semantic audio IDs to asset definitions and fallback entries.
 */
object GameAudioRegistry {

    /**
     * Dedicated isolated Random instance for audio playback variations, pitch jitter, and random stings.
     * CRITICAL: Strictly segregated from deterministic gameplay RNG (combat, loot, generator).
     */
    private val audioPresentationRandom = Random(System.currentTimeMillis())

    private val soundEntries: Map<GameSoundId, SoundAssetEntry> = listOf(
        // --- UI ---
        SoundAssetEntry(
            soundId = GameSoundId.UI_CLICK,
            resourceName = "sfx_ui_click",
            pitchVariationAllowed = false,
            priority = AudioPriority.NORMAL
        ),
        SoundAssetEntry(
            soundId = GameSoundId.UI_CONFIRM,
            resourceName = "sfx_ui_confirm",
            pitchVariationAllowed = false,
            priority = AudioPriority.NORMAL
        ),
        SoundAssetEntry(
            soundId = GameSoundId.UI_CANCEL,
            resourceName = "sfx_ui_cancel",
            pitchVariationAllowed = false,
            priority = AudioPriority.NORMAL
        ),
        SoundAssetEntry(
            soundId = GameSoundId.UI_TAB,
            resourceName = "sfx_ui_tab",
            pitchVariationAllowed = false,
            priority = AudioPriority.LOW
        ),
        SoundAssetEntry(
            soundId = GameSoundId.UI_TOGGLE,
            resourceName = "sfx_ui_toggle",
            pitchVariationAllowed = false,
            priority = AudioPriority.LOW
        ),
        SoundAssetEntry(
            soundId = GameSoundId.UI_ERROR,
            resourceName = "sfx_ui_error",
            pitchVariationAllowed = false,
            priority = AudioPriority.NORMAL
        ),
        SoundAssetEntry(
            soundId = GameSoundId.UI_WARNING,
            resourceName = "sfx_ui_warning",
            pitchVariationAllowed = false,
            priority = AudioPriority.HIGH
        ),

        // --- Settlement & Economy ---
        SoundAssetEntry(
            soundId = GameSoundId.RESOURCE_GAIN,
            resourceName = "sfx_resource_gain",
            pitchVariationAllowed = true,
            priority = AudioPriority.NORMAL
        ),
        SoundAssetEntry(
            soundId = GameSoundId.DAILY_REPORT,
            resourceName = "sfx_daily_report",
            pitchVariationAllowed = false,
            priority = AudioPriority.NORMAL
        ),
        SoundAssetEntry(
            soundId = GameSoundId.NEW_DAY,
            resourceName = "sfx_new_day",
            pitchVariationAllowed = false,
            priority = AudioPriority.HIGH
        ),
        SoundAssetEntry(
            soundId = GameSoundId.BUILDING_CONSTRUCT,
            resourceName = "sfx_building_construct",
            pitchVariationAllowed = true,
            priority = AudioPriority.HIGH
        ),
        SoundAssetEntry(
            soundId = GameSoundId.BUILDING_UPGRADE,
            resourceName = "sfx_building_upgrade",
            pitchVariationAllowed = false,
            priority = AudioPriority.HIGH
        ),
        SoundAssetEntry(
            soundId = GameSoundId.WORKSHOP_CRAFT,
            resourceName = "sfx_craft",
            pitchVariationAllowed = true,
            priority = AudioPriority.NORMAL
        ),
        SoundAssetEntry(
            soundId = GameSoundId.WORKSHOP_REPAIR,
            resourceName = "sfx_repair",
            pitchVariationAllowed = true,
            priority = AudioPriority.NORMAL
        ),
        SoundAssetEntry(
            soundId = GameSoundId.RESEARCH_COMPLETE,
            resourceName = "sfx_research_complete",
            pitchVariationAllowed = false,
            priority = AudioPriority.HIGH
        ),

        // --- Trade ---
        SoundAssetEntry(
            soundId = GameSoundId.TRADE_BUY,
            resourceName = "sfx_trade_buy",
            pitchVariationAllowed = false,
            priority = AudioPriority.NORMAL
        ),
        SoundAssetEntry(
            soundId = GameSoundId.TRADE_SELL,
            resourceName = "sfx_trade_sell",
            pitchVariationAllowed = false,
            priority = AudioPriority.NORMAL
        ),

        // --- Reputation & Quests ---
        SoundAssetEntry(
            soundId = GameSoundId.REPUTATION_INCREASE,
            resourceName = "sfx_reputation_up",
            pitchVariationAllowed = false,
            priority = AudioPriority.NORMAL
        ),
        SoundAssetEntry(
            soundId = GameSoundId.QUEST_ACCEPTED,
            resourceName = "sfx_quest_accept",
            pitchVariationAllowed = false,
            priority = AudioPriority.NORMAL
        ),
        SoundAssetEntry(
            soundId = GameSoundId.QUEST_OBJECTIVE_COMPLETE,
            resourceName = "sfx_quest_objective",
            pitchVariationAllowed = false,
            priority = AudioPriority.HIGH
        ),
        SoundAssetEntry(
            soundId = GameSoundId.QUEST_COMPLETED,
            resourceName = "sfx_quest_complete",
            pitchVariationAllowed = false,
            priority = AudioPriority.HIGH
        ),
        SoundAssetEntry(
            soundId = GameSoundId.QUEST_FAILED,
            resourceName = "sfx_quest_failed",
            pitchVariationAllowed = false,
            priority = AudioPriority.HIGH
        ),

        // --- Map & Travel ---
        SoundAssetEntry(
            soundId = GameSoundId.LOCATION_DISCOVERED,
            resourceName = "sfx_location_discovered",
            pitchVariationAllowed = false,
            priority = AudioPriority.HIGH
        ),
        SoundAssetEntry(
            soundId = GameSoundId.TRAVEL_STEP,
            resourceName = "sfx_travel_step",
            pitchVariationAllowed = true,
            priority = AudioPriority.LOW
        ),
        SoundAssetEntry(
            soundId = GameSoundId.VEHICLE_ENGINE,
            resourceName = "sfx_vehicle_engine",
            pitchVariationAllowed = false,
            priority = AudioPriority.LOW
        ),

        // --- Events ---
        SoundAssetEntry(
            soundId = GameSoundId.EVENT_REVEAL,
            resourceName = "sfx_event_reveal",
            pitchVariationAllowed = false,
            priority = AudioPriority.HIGH
        ),
        SoundAssetEntry(
            soundId = GameSoundId.EVENT_POSITIVE_RESULT,
            resourceName = "sfx_event_positive",
            pitchVariationAllowed = false,
            priority = AudioPriority.HIGH
        ),
        SoundAssetEntry(
            soundId = GameSoundId.EVENT_NEGATIVE_RESULT,
            resourceName = "sfx_event_negative",
            pitchVariationAllowed = false,
            priority = AudioPriority.HIGH
        ),

        // --- Loot ---
        SoundAssetEntry(
            soundId = GameSoundId.LOOT_REVEAL,
            resourceName = "sfx_loot_reveal",
            pitchVariationAllowed = false,
            priority = AudioPriority.NORMAL
        ),
        SoundAssetEntry(
            soundId = GameSoundId.LOOT_PICK,
            resourceName = "sfx_loot_pick",
            pitchVariationAllowed = true,
            priority = AudioPriority.LOW
        ),
        SoundAssetEntry(
            soundId = GameSoundId.LOOT_RARE,
            resourceName = "sfx_loot_rare",
            pitchVariationAllowed = false,
            priority = AudioPriority.HIGH
        ),
        SoundAssetEntry(
            soundId = GameSoundId.LOOT_TAKE_ALL,
            resourceName = "sfx_loot_take_all",
            pitchVariationAllowed = false,
            priority = AudioPriority.NORMAL
        ),

        // --- Combat ---
        SoundAssetEntry(
            soundId = GameSoundId.COMBAT_ATTACK_MELEE,
            resourceName = "sfx_combat_melee_01",
            variantResourceNames = listOf("sfx_combat_melee_01", "sfx_combat_melee_02", "sfx_combat_melee_03"),
            pitchVariationAllowed = true,
            priority = AudioPriority.HIGH
        ),
        SoundAssetEntry(
            soundId = GameSoundId.COMBAT_SHOT_PISTOL,
            resourceName = "sfx_combat_pistol_01",
            variantResourceNames = listOf("sfx_combat_pistol_01", "sfx_combat_pistol_02"),
            pitchVariationAllowed = true,
            priority = AudioPriority.HIGH
        ),
        SoundAssetEntry(
            soundId = GameSoundId.COMBAT_SHOT_RIFLE,
            resourceName = "sfx_combat_rifle_01",
            variantResourceNames = listOf("sfx_combat_rifle_01", "sfx_combat_rifle_02"),
            pitchVariationAllowed = true,
            priority = AudioPriority.HIGH
        ),
        SoundAssetEntry(
            soundId = GameSoundId.COMBAT_SHOT_SHOTGUN,
            resourceName = "sfx_combat_shotgun",
            pitchVariationAllowed = true,
            priority = AudioPriority.HIGH
        ),
        SoundAssetEntry(
            soundId = GameSoundId.COMBAT_SHOT_HEAVY,
            resourceName = "sfx_combat_heavy",
            pitchVariationAllowed = true,
            priority = AudioPriority.HIGH
        ),
        SoundAssetEntry(
            soundId = GameSoundId.COMBAT_HIT,
            resourceName = "sfx_combat_hit_01",
            variantResourceNames = listOf("sfx_combat_hit_01", "sfx_combat_hit_02", "sfx_combat_hit_03"),
            pitchVariationAllowed = true,
            priority = AudioPriority.HIGH
        ),
        SoundAssetEntry(
            soundId = GameSoundId.COMBAT_MISS,
            resourceName = "sfx_combat_miss",
            pitchVariationAllowed = true,
            priority = AudioPriority.NORMAL
        ),
        SoundAssetEntry(
            soundId = GameSoundId.COMBAT_ARMOR_BLOCK,
            resourceName = "sfx_combat_block",
            pitchVariationAllowed = true,
            priority = AudioPriority.HIGH
        ),
        SoundAssetEntry(
            soundId = GameSoundId.COMBAT_HEAL,
            resourceName = "sfx_combat_heal",
            pitchVariationAllowed = false,
            priority = AudioPriority.HIGH
        ),
        SoundAssetEntry(
            soundId = GameSoundId.COMBAT_BUFF,
            resourceName = "sfx_combat_buff",
            pitchVariationAllowed = false,
            priority = AudioPriority.NORMAL
        ),
        SoundAssetEntry(
            soundId = GameSoundId.COMBAT_DEBUFF,
            resourceName = "sfx_combat_debuff",
            pitchVariationAllowed = false,
            priority = AudioPriority.NORMAL
        ),
        SoundAssetEntry(
            soundId = GameSoundId.COMBAT_STATUS_EXPIRE,
            resourceName = "sfx_combat_status_expire",
            pitchVariationAllowed = false,
            priority = AudioPriority.LOW
        ),
        SoundAssetEntry(
            soundId = GameSoundId.COMBAT_TURN_PLAYER,
            resourceName = "sfx_combat_turn_player",
            pitchVariationAllowed = false,
            priority = AudioPriority.LOW
        ),
        SoundAssetEntry(
            soundId = GameSoundId.COMBAT_TURN_ENEMY,
            resourceName = "sfx_combat_turn_enemy",
            pitchVariationAllowed = false,
            priority = AudioPriority.LOW
        ),
        SoundAssetEntry(
            soundId = GameSoundId.COMBAT_VICTORY_STING,
            resourceName = "sfx_combat_victory",
            pitchVariationAllowed = false,
            priority = AudioPriority.HIGH
        ),
        SoundAssetEntry(
            soundId = GameSoundId.COMBAT_DEFEAT_STING,
            resourceName = "sfx_combat_defeat",
            pitchVariationAllowed = false,
            priority = AudioPriority.HIGH
        )
    ).associateBy { it.soundId }

    private val musicEntries: Map<GameMusicId, MusicAssetEntry> = listOf(
        MusicAssetEntry(GameMusicId.SETTLEMENT, "music_settlement_01"),
        MusicAssetEntry(GameMusicId.WORLD_MAP, "music_world_map_01"),
        MusicAssetEntry(GameMusicId.EXPLORATION, "music_exploration_01"),
        MusicAssetEntry(GameMusicId.COMBAT, "music_combat_01"),
        MusicAssetEntry(GameMusicId.VICTORY, "music_victory_01"),
        MusicAssetEntry(GameMusicId.DEFEAT, "music_defeat_01"),
        MusicAssetEntry(GameMusicId.MAIN_MENU, "music_main_menu_01"),
        MusicAssetEntry(GameMusicId.SILENT, "silent")
    ).associateBy { it.musicId }

    private val ambientEntries: Map<GameAmbientId, AmbientAssetEntry> = listOf(
        AmbientAssetEntry(
            ambientId = GameAmbientId.SETTLEMENT_DAY,
            resourceName = "amb_settlement_day",
            randomStingSoundIds = listOf(GameSoundId.WORKSHOP_CRAFT, GameSoundId.TRAVEL_STEP)
        ),
        AmbientAssetEntry(
            ambientId = GameAmbientId.SETTLEMENT_NIGHT,
            resourceName = "amb_settlement_night"
        ),
        AmbientAssetEntry(
            ambientId = GameAmbientId.RUINS,
            resourceName = "amb_ruins",
            randomStingSoundIds = listOf(GameSoundId.TRAVEL_STEP)
        ),
        AmbientAssetEntry(
            ambientId = GameAmbientId.INDUSTRIAL,
            resourceName = "amb_industrial"
        ),
        AmbientAssetEntry(
            ambientId = GameAmbientId.FOREST,
            resourceName = "amb_forest"
        ),
        AmbientAssetEntry(
            ambientId = GameAmbientId.ROAD,
            resourceName = "amb_road"
        ),
        AmbientAssetEntry(
            ambientId = GameAmbientId.STORM,
            resourceName = "amb_storm"
        ),
        AmbientAssetEntry(
            ambientId = GameAmbientId.SILENT,
            resourceName = "silent"
        )
    ).associateBy { it.ambientId }

    fun getSoundEntry(soundId: GameSoundId): SoundAssetEntry {
        return soundEntries[soundId] ?: SoundAssetEntry(
            soundId = soundId,
            resourceName = "sfx_fallback"
        )
    }

    fun getMusicEntry(musicId: GameMusicId): MusicAssetEntry {
        return musicEntries[musicId] ?: MusicAssetEntry(
            musicId = musicId,
            resourceName = "music_fallback"
        )
    }

    fun getAmbientEntry(ambientId: GameAmbientId): AmbientAssetEntry {
        return ambientEntries[ambientId] ?: AmbientAssetEntry(
            ambientId = ambientId,
            resourceName = "amb_fallback"
        )
    }

    /**
     * Resolves resource name choosing from variants if defined.
     * Uses independent audioPresentationRandom without consuming gameplay RNG.
     */
    fun resolveSoundResourceName(soundId: GameSoundId): String {
        val entry = getSoundEntry(soundId)
        if (entry.variantResourceNames.isNotEmpty()) {
            val idx = audioPresentationRandom.nextInt(entry.variantResourceNames.size)
            return entry.variantResourceNames[idx]
        }
        return entry.resourceName
    }

    fun calculatePitchJitter(allowPitch: Boolean): Float {
        if (!allowPitch) return 1.0f
        val delta = 0.04f
        return 1.0f + (audioPresentationRandom.nextFloat() * (delta * 2f) - delta)
    }

    fun getAllSoundEntries(): List<SoundAssetEntry> = soundEntries.values.toList()
    fun getAllMusicEntries(): List<MusicAssetEntry> = musicEntries.values.toList()
    fun getAllAmbientEntries(): List<AmbientAssetEntry> = ambientEntries.values.toList()
}
