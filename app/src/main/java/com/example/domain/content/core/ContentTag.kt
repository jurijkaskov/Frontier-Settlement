package com.example.domain.content.core

/**
 * Universal taxonomy tags used to connect locations, events, loot tables,
 * enemies, quests, and factions in the data-driven content generation engine.
 */
enum class ContentTag(val titleRu: String) {
    // Biomes & Environments
    WASTELAND("Пустошь"),
    FOREST("Лесной массив"),
    HILLS("Холмы и скалы"),
    WATER("Речная зона"),
    RUINS("Городские руины"),
    INDUSTRIAL("Промышленная зона"),
    UNDERGROUND("Подземный комплекс"),
    SETTLEMENT("Поселение и форпост"),

    // Thematic & Activity Profiles
    SURVIVAL("Выживание"),
    TECHNICAL("Техника и электроника"),
    TRADE("Торговля и обмен"),
    MILITARY("Военные объекты"),
    NATURE("Природа и фауна"),
    ANOMALY("Аномалии и мутации"),
    MEDICINE("Медицина"),
    SCAVENGING("Сбор утиля"),

    // Danger & Rarity
    SAFE("Безопасная зона"),
    LOW_DANGER("Низкая угроза"),
    HIGH_DANGER("Высокая угроза"),
    COMMON("Обычный контент"),
    RARE("Редкий контент"),
    UNIQUE("Уникальный / Сюжетный"),

    // Faction Affinities
    FACTION_SURVIVORS("Поселенцы"),
    FACTION_RAIDERS("Мародёры"),
    FACTION_ENGINEERS("Техномаги"),
    FACTION_MERCHANTS("Купцы")
}
