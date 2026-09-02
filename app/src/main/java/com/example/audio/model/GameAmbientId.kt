package com.example.audio.model

/**
 * Semantic identifiers for continuous environmental acoustic backgrounds (ambient).
 */
enum class GameAmbientId(
    val titleRu: String,
    val defaultRelativeVolume: Float = 0.85f,
    val loopable: Boolean = true
) {
    SETTLEMENT_DAY("Поселение (День) — ветер, генераторы, отголоски работы", 0.8f, true),
    SETTLEMENT_NIGHT("Поселение (Ночь) — тишина, редкий ветер, далёкий гул", 0.7f, true),
    RUINS("Городские руины — сквозняк в бетоне, далёкий скрежет металла", 0.85f, true),
    INDUSTRIAL("Индустриальный комплекс — тяжёлый низкий гул, пар, конструкции", 0.85f, true),
    FOREST("Дикие заросли / лес — порывы ветра в ветвях, шорохи", 0.75f, true),
    ROAD("Открытый тракт — пылевой ветер, простор пустоши", 0.8f, true),
    STORM("Пылевая буря — сильный вой ветра, абразивный шум", 0.9f, true),
    SILENT("Полная тишина", 0.0f, false);
}
