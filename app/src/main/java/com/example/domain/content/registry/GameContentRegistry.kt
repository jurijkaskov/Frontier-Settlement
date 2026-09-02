package com.example.domain.content.registry

import com.example.domain.content.character.CharacterArchetype
import com.example.domain.content.core.ContentTag
import com.example.domain.content.encounter.EncounterTemplate
import com.example.domain.content.encounter.EnemyTemplate
import com.example.domain.content.location.LocationTemplate
import com.example.domain.content.loot.LootTableDefinition
import com.example.domain.content.quest.RepeatableQuestTemplate
import com.example.domain.content.registry.packs.*
import com.example.domain.model.CharacterRole
import com.example.domain.model.ExpeditionEvent
import com.example.domain.model.LocationType
import com.example.domain.model.WarehouseItem
import com.example.domain.model.quest.QuestDefinition

/**
 * Authoritative Central Catalog and Registry for all data-driven game definitions,
 * templates, and content packs.
 */
object GameContentRegistry {

    private val packs: MutableList<ContentPack> = mutableListOf(
        CoreContentPack,
        ForestContentPack,
        IndustrialContentPack,
        SettlementContentPack,
        SpecialContentPack
    )

    // Cached indexed collections
    val locationTemplates: Map<String, LocationTemplate> by lazy {
        packs.flatMap { it.locationTemplates }.associateBy { it.id }
    }

    val events: Map<String, ExpeditionEvent> by lazy {
        packs.flatMap { it.events }.associateBy { it.id }
    }

    val enemyTemplates: Map<String, EnemyTemplate> by lazy {
        packs.flatMap { it.enemyTemplates }.associateBy { it.id }
    }

    val encounterTemplates: Map<String, EncounterTemplate> by lazy {
        packs.flatMap { it.encounterTemplates }.associateBy { it.id }
    }

    val lootTables: Map<String, LootTableDefinition> by lazy {
        packs.flatMap { it.lootTables }.associateBy { it.id }
    }

    val characterArchetypes: Map<String, CharacterArchetype> by lazy {
        packs.flatMap { it.characterArchetypes }.associateBy { it.id }
    }

    val repeatableQuestTemplates: Map<String, RepeatableQuestTemplate> by lazy {
        packs.flatMap { it.repeatableQuestTemplates }.associateBy { it.id }
    }

    val storyQuests: Map<String, QuestDefinition> by lazy {
        packs.flatMap { it.storyQuests }.associateBy { it.id }
    }

    val allPacks: List<ContentPack> get() = packs.toList()

    /**
     * Registers an additional custom or modded content pack at runtime.
     */
    fun registerPack(pack: ContentPack) {
        if (packs.none { it.packId == pack.packId }) {
            packs.add(pack)
        }
    }

    // Direct Lookup Helpers
    fun getLocationTemplate(id: String): LocationTemplate? = locationTemplates[id]
    fun getEvent(id: String): ExpeditionEvent? = events[id]
    fun getEnemyTemplate(id: String): EnemyTemplate? = enemyTemplates[id]
    fun getEncounterTemplate(id: String): EncounterTemplate? = encounterTemplates[id]
    fun getLootTable(id: String): LootTableDefinition? = lootTables[id]
    fun getCharacterArchetype(id: String): CharacterArchetype? = characterArchetypes[id]
    fun getCharacterArchetypeByRole(role: CharacterRole): CharacterArchetype? =
        characterArchetypes.values.firstOrNull { it.role == role }
    fun getRepeatableQuestTemplate(id: String): RepeatableQuestTemplate? = repeatableQuestTemplates[id]
    fun getStoryQuest(id: String): QuestDefinition? = storyQuests[id]

    // Tag and Type Filtering Helpers
    fun getLocationTemplatesByType(type: LocationType): List<LocationTemplate> =
        locationTemplates.values.filter { it.type == type }

    fun getLocationTemplatesByTag(tag: ContentTag): List<LocationTemplate> =
        locationTemplates.values.filter { it.tags.contains(tag) }

    fun getEventsByTag(tag: ContentTag): List<ExpeditionEvent> =
        events.values.filter { event ->
            event.targetAreaIds.isNotEmpty() || event.allowedLocationTypes.isNotEmpty()
        }

    fun getEncountersForLocationType(type: LocationType): List<EncounterTemplate> =
        encounterTemplates.values.filter { it.allowedLocationTypes.isEmpty() || it.allowedLocationTypes.contains(type) }

    fun getLootTablesByTag(tag: ContentTag): List<LootTableDefinition> =
        lootTables.values.filter { it.tags.contains(tag) }
}
