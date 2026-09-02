package com.example.domain.model.help

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * High-level topic category for help and survival guides.
 */
enum class HelpCategory(
    val id: String,
    val titleRu: String,
    val subtitleRu: String
) {
    SETTLEMENT("settlement", "Поселение и здания", "Строительство, модернизация и жители базы"),
    RESOURCES("resources", "Ресурсы и склад", "Провизия, вода, топливо и вместимость склада"),
    EXPEDITIONS("expeditions", "Экспедиции и вылазки", "Подготовка отряда, припасы и события пути"),
    MAP("map", "Карта и перемещение", "Локации пустоши, типы местности и транспорт"),
    COMBAT("combat", "Тактический бой", "Очки действий (AP), оружие, укрытия и отступление"),
    LOOT("loot", "Добыча и трофеи", "Вместимость багажа, ценности и разгрузка"),
    ECONOMY("economy", "Торговля и экономика", "Цены, спрос торговцев и суточный баланс"),
    REPUTATION("reputation", "Репутация и фракции", "Отношения с группировками и угрозы рейдов"),
    QUESTS("quests", "Задания штаба", "Поручения, контрольные цели и награды");

    companion object {
        fun fromId(id: String): HelpCategory {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: SETTLEMENT
        }
    }
}

/**
 * Data-driven article entry in the survival guide.
 */
data class HelpArticleDefinition(
    val id: String,
    val category: HelpCategory,
    val title: String,
    val summary: String,
    val keyPoints: List<String>,
    val proTips: List<String> = emptyList()
)
