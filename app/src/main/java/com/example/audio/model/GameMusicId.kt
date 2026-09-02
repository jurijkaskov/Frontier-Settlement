package com.example.audio.model

/**
 * Semantic identifiers for background musical tracks.
 */
enum class GameMusicId(
    val titleRu: String,
    val defaultRelativeVolume: Float = 0.8f,
    val loopable: Boolean = true
) {
    SETTLEMENT("Тема поселения — Надежда и работа", 0.75f, true),
    WORLD_MAP("Глобальная карта — Пустошь и простор", 0.7f, true),
    EXPLORATION("Исследование руин — Напряжённый поиск", 0.75f, true),
    COMBAT("Пошаговый бой — Тактическое напряжение", 0.85f, true),
    VICTORY("Победный финал экспедиции", 0.9f, false),
    DEFEAT("Поражение отряда на пустошах", 0.85f, false),
    MAIN_MENU("Главное меню — Холодный горизонт", 0.7f, true),
    SILENT("Без фоновой музыки (только эмбиент)", 0.0f, false);
}
