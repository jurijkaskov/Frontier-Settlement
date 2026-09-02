package com.example.domain.content.loot

import com.example.domain.content.core.ContentTag
import com.example.domain.model.GameResources
import com.example.domain.model.ResourceType
import com.example.domain.model.WarehouseItem

/**
 * Entry for bulk resource rolls in a loot table.
 */
data class LootResourceEntry(
    val resourceType: ResourceType,
    val minAmount: Int,
    val maxAmount: Int,
    val dropChance: Float = 1.0f
)

/**
 * Entry for discrete item drops in a loot table.
 */
data class LootItemEntry(
    val itemId: String,
    val weight: Float = 1.0f,
    val minCount: Int = 1,
    val maxCount: Int = 1,
    val dropChance: Float = 0.5f
)

/**
 * Comprehensive data-driven loot table definition.
 */
data class LootTableDefinition(
    val id: String,
    val titleRu: String = "Контейнер с припасами",
    val minCredits: Int = 20,
    val maxCredits: Int = 100,
    val resourceEntries: List<LootResourceEntry> = emptyList(),
    val itemEntries: List<LootItemEntry> = emptyList(),
    val guaranteedItemIds: List<String> = emptyList(),
    val maxItemDrops: Int = 3,
    val tags: Set<ContentTag> = emptySet()
)

/**
 * Structured outcome of generating loot.
 */
data class GeneratedLoot(
    val credits: Int = 0,
    val resources: Map<ResourceType, Int> = emptyMap(),
    val items: List<WarehouseItem> = emptyList(),
    val summaryRu: String = ""
) {
    fun toGameResources(): GameResources {
        return GameResources(
            money = credits,
            food = resources[ResourceType.FOOD] ?: 0,
            water = resources[ResourceType.WATER] ?: 0,
            fuel = resources[ResourceType.FUEL] ?: 0,
            materials = resources[ResourceType.MATERIALS] ?: 0,
            extraResources = resources.filterKeys {
                it !in setOf(ResourceType.MONEY, ResourceType.FOOD, ResourceType.WATER, ResourceType.FUEL, ResourceType.MATERIALS)
            }
        )
    }
}
