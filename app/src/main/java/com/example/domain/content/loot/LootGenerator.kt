package com.example.domain.content.loot

import com.example.domain.content.core.ContentGenerationContext
import com.example.domain.content.core.GameRandomProvider
import com.example.domain.content.core.GenerationResult
import com.example.domain.content.core.WeightedSelector
import com.example.domain.content.registry.GameContentRegistry
import com.example.domain.model.ResourceType
import com.example.domain.model.WarehouseItem
import kotlin.random.Random

/**
 * Procedural generator for resource and item loot based on data-driven loot tables.
 */
object LootGenerator {

    /**
     * Generates a concrete [GeneratedLoot] bundle from a loot table definition.
     */
    fun generateLoot(
        table: LootTableDefinition,
        context: ContentGenerationContext,
        availableItemCatalog: List<WarehouseItem> = emptyList(),
        customIndex: Int? = null
    ): GenerationResult<GeneratedLoot> {
        val index = customIndex ?: context.generationIndex
        val seed = GameRandomProvider.deriveSeed(context.gameSeed, "loot", table.id, index)
        val random = Random(seed)

        val budgetMultiplier = LootBudgetCalculator.calculateBudgetMultiplier(context.dangerLevel)

        // 1. Roll Credits
        val rawCredits = if (table.minCredits >= table.maxCredits) {
            table.minCredits
        } else {
            random.nextInt(table.minCredits, table.maxCredits + 1)
        }
        val credits = (rawCredits * budgetMultiplier).toInt()

        // 2. Roll Resources
        val rolledResources = mutableMapOf<ResourceType, Int>()
        for (entry in table.resourceEntries) {
            val roll = random.nextFloat()
            if (roll <= entry.dropChance) {
                val amount = if (entry.minAmount >= entry.maxAmount) {
                    entry.minAmount
                } else {
                    random.nextInt(entry.minAmount, entry.maxAmount + 1)
                }
                val scaledAmount = (amount * budgetMultiplier).toInt().coerceAtLeast(1)
                rolledResources[entry.resourceType] = (rolledResources[entry.resourceType] ?: 0) + scaledAmount
            }
        }

        // 3. Roll Item Drops
        val rolledItems = mutableListOf<WarehouseItem>()

        // Guaranteed items
        for (itemId in table.guaranteedItemIds) {
            val item = availableItemCatalog.find { it.id == itemId }
            if (item != null) {
                rolledItems.add(item)
            }
        }

        // Probabilistic item pool
        val eligibleItemEntries = table.itemEntries.filter { entry ->
            random.nextFloat() <= entry.dropChance
        }

        if (eligibleItemEntries.isNotEmpty()) {
            val itemsToPick = WeightedSelector.selectMultipleWithoutReplacement(
                candidates = eligibleItemEntries,
                count = table.maxItemDrops,
                weightExtractor = { it.weight },
                random = random
            )

            for (entry in itemsToPick) {
                val item = availableItemCatalog.find { it.id == entry.itemId }
                if (item != null) {
                    val count = if (entry.minCount >= entry.maxCount) entry.minCount else random.nextInt(entry.minCount, entry.maxCount + 1)
                    rolledItems.add(item.copy(quantity = count))
                }
            }
        }

        val summaryParts = mutableListOf<String>()
        if (credits > 0) summaryParts.add("+$credits Кредитов")
        for ((res, count) in rolledResources) {
            summaryParts.add("+$count ${res.titleRu}")
        }
        for (item in rolledItems) {
            summaryParts.add("+${item.name} (${item.quantity} шт.)")
        }

        val loot = GeneratedLoot(
            credits = credits,
            resources = rolledResources,
            items = rolledItems,
            summaryRu = summaryParts.joinToString(", ")
        )

        return GenerationResult.Success(loot)
    }

    /**
     * Looks up table in registry and generates loot.
     */
    fun generateLootByTableId(
        tableId: String,
        context: ContentGenerationContext,
        availableItemCatalog: List<WarehouseItem> = emptyList(),
        registry: GameContentRegistry = GameContentRegistry
    ): GenerationResult<GeneratedLoot> {
        val table = registry.getLootTable(tableId)
            ?: return GenerationResult.NoEligibleContent("Loot table '$tableId' not found in registry")
        return generateLoot(table, context, availableItemCatalog)
    }
}
