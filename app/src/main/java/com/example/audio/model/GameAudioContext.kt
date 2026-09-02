package com.example.audio.model

/**
 * High-level presentation context describing the player's active gameplay scene.
 * Context resolves to a specific [GameAudioProfile] providing music, ambient, and mix parameters.
 */
enum class GameAudioContext(val titleRu: String) {
    MAIN_MENU("Главное меню"),
    SETTLEMENT("Управление поселением"),
    WORLD_MAP("Глобальная карта региона"),
    TRAVEL("Караванный переход"),
    LOCATION("Исследование локации / аванпоста"),
    EVENT("Случайное событие на пустошах"),
    COMBAT("Тактический пошаговый бой"),
    LOOT("Сбор трофеев и снаряжения"),
    RETURN_SUMMARY("Итоги экспедиции и возвращение");
}

/**
 * Concrete audio acoustic profile associated with a [GameAudioContext] or customized state.
 */
data class GameAudioProfile(
    val context: GameAudioContext,
    val musicId: GameMusicId,
    val ambientId: GameAmbientId,
    val musicVolumeMultiplier: Float = 1.0f,
    val ambientVolumeMultiplier: Float = 1.0f,
    val crossfadeDurationMs: Int = 1200,
    val enableDucking: Boolean = false
) {
    companion object {
        val MainMenu = GameAudioProfile(
            context = GameAudioContext.MAIN_MENU,
            musicId = GameMusicId.MAIN_MENU,
            ambientId = GameAmbientId.ROAD,
            musicVolumeMultiplier = 0.85f,
            ambientVolumeMultiplier = 0.6f
        )

        val SettlementDay = GameAudioProfile(
            context = GameAudioContext.SETTLEMENT,
            musicId = GameMusicId.SETTLEMENT,
            ambientId = GameAmbientId.SETTLEMENT_DAY,
            musicVolumeMultiplier = 0.8f,
            ambientVolumeMultiplier = 0.9f
        )

        val SettlementNight = GameAudioProfile(
            context = GameAudioContext.SETTLEMENT,
            musicId = GameMusicId.SETTLEMENT,
            ambientId = GameAmbientId.SETTLEMENT_NIGHT,
            musicVolumeMultiplier = 0.65f,
            ambientVolumeMultiplier = 0.8f
        )

        val WorldMap = GameAudioProfile(
            context = GameAudioContext.WORLD_MAP,
            musicId = GameMusicId.WORLD_MAP,
            ambientId = GameAmbientId.ROAD,
            musicVolumeMultiplier = 0.85f,
            ambientVolumeMultiplier = 0.7f
        )

        val Travel = GameAudioProfile(
            context = GameAudioContext.TRAVEL,
            musicId = GameMusicId.SILENT, // Atmospheric focus on travel ambient
            ambientId = GameAmbientId.ROAD,
            musicVolumeMultiplier = 0.0f,
            ambientVolumeMultiplier = 1.0f
        )

        val LocationRuins = GameAudioProfile(
            context = GameAudioContext.LOCATION,
            musicId = GameMusicId.EXPLORATION,
            ambientId = GameAmbientId.RUINS,
            musicVolumeMultiplier = 0.75f,
            ambientVolumeMultiplier = 1.0f
        )

        val LocationIndustrial = GameAudioProfile(
            context = GameAudioContext.LOCATION,
            musicId = GameMusicId.EXPLORATION,
            ambientId = GameAmbientId.INDUSTRIAL,
            musicVolumeMultiplier = 0.75f,
            ambientVolumeMultiplier = 1.0f
        )

        val LocationForest = GameAudioProfile(
            context = GameAudioContext.LOCATION,
            musicId = GameMusicId.EXPLORATION,
            ambientId = GameAmbientId.FOREST,
            musicVolumeMultiplier = 0.75f,
            ambientVolumeMultiplier = 0.95f
        )

        val Event = GameAudioProfile(
            context = GameAudioContext.EVENT,
            musicId = GameMusicId.EXPLORATION,
            ambientId = GameAmbientId.RUINS,
            musicVolumeMultiplier = 0.6f, // Slightly ducked to focus on decision
            ambientVolumeMultiplier = 0.7f,
            enableDucking = true
        )

        val Combat = GameAudioProfile(
            context = GameAudioContext.COMBAT,
            musicId = GameMusicId.COMBAT,
            ambientId = GameAmbientId.SILENT,
            musicVolumeMultiplier = 1.0f,
            ambientVolumeMultiplier = 0.0f,
            crossfadeDurationMs = 800
        )

        val Loot = GameAudioProfile(
            context = GameAudioContext.LOOT,
            musicId = GameMusicId.EXPLORATION,
            ambientId = GameAmbientId.RUINS,
            musicVolumeMultiplier = 0.7f,
            ambientVolumeMultiplier = 0.8f
        )

        val ReturnSummary = GameAudioProfile(
            context = GameAudioContext.RETURN_SUMMARY,
            musicId = GameMusicId.SETTLEMENT,
            ambientId = GameAmbientId.SETTLEMENT_DAY,
            musicVolumeMultiplier = 0.9f,
            ambientVolumeMultiplier = 0.8f
        )
    }
}
