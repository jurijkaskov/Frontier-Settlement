package com.example.domain.content.registry

import com.example.domain.content.character.CharacterArchetype
import com.example.domain.content.encounter.EncounterTemplate
import com.example.domain.content.encounter.EnemyTemplate
import com.example.domain.content.location.LocationTemplate
import com.example.domain.content.loot.LootTableDefinition
import com.example.domain.content.quest.RepeatableQuestTemplate
import com.example.domain.model.ExpeditionEvent
import com.example.domain.model.WarehouseItem
import com.example.domain.model.quest.QuestDefinition

/**
 * Modular content pack bundling themed definitions, templates, and static catalog items.
 */
interface ContentPack {
    val packId: String
    val packTitleRu: String
    val version: Int get() = 1

    val locationTemplates: List<LocationTemplate> get() = emptyList()
    val events: List<ExpeditionEvent> get() = emptyList()
    val enemyTemplates: List<EnemyTemplate> get() = emptyList()
    val encounterTemplates: List<EncounterTemplate> get() = emptyList()
    val lootTables: List<LootTableDefinition> get() = emptyList()
    val characterArchetypes: List<CharacterArchetype> get() = emptyList()
    val repeatableQuestTemplates: List<RepeatableQuestTemplate> get() = emptyList()
    val storyQuests: List<QuestDefinition> get() = emptyList()
    val items: List<WarehouseItem> get() = emptyList()
}
