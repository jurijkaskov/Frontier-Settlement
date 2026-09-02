package com.example

import com.example.domain.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Comprehensive unit tests for Milestone 5 (Warehouse Screen & Resource Management).
 */
class WarehouseSystemTest {

    @Test
    fun testWarehouseDisplayEntries_AllCategoriesMappedCorrectly() {
        val resources = GameResources(
            money = 1000,
            food = 150,
            water = 180,
            fuel = 90,
            materials = 220,
            warehouseMaxCapacity = 800,
            extraResources = mapOf(
                ResourceType.MEDICINE to 15,
                ResourceType.AMMO to 40,
                ResourceType.COMPONENTS to 10,
                ResourceType.RARE_ALLOY to 5
            )
        )

        val allEntries = WarehouseDisplayHelper.buildDisplayEntries(
            resources = resources,
            selectedCategory = WarehouseFilterCategory.ALL
        )

        assertEquals(9, allEntries.size)

        // Verify categories
        val primaryEntries = WarehouseDisplayHelper.buildDisplayEntries(
            resources = resources,
            selectedCategory = WarehouseFilterCategory.PRIMARY
        )
        val primaryTypes = primaryEntries.map { it.resourceType }
        assertTrue(primaryTypes.contains(ResourceType.FOOD))
        assertTrue(primaryTypes.contains(ResourceType.WATER))
        assertTrue(primaryTypes.contains(ResourceType.FUEL))
        assertEquals(3, primaryEntries.size)

        val materialsEntries = WarehouseDisplayHelper.buildDisplayEntries(
            resources = resources,
            selectedCategory = WarehouseFilterCategory.MATERIALS
        )
        val materialTypes = materialsEntries.map { it.resourceType }
        assertTrue(materialTypes.contains(ResourceType.MATERIALS))
        assertTrue(materialTypes.contains(ResourceType.COMPONENTS))
        assertEquals(2, materialsEntries.size)

        val itemsEntries = WarehouseDisplayHelper.buildDisplayEntries(
            resources = resources,
            selectedCategory = WarehouseFilterCategory.ITEMS
        )
        val itemTypes = itemsEntries.map { it.resourceType }
        assertTrue(itemTypes.contains(ResourceType.MEDICINE))
        assertTrue(itemTypes.contains(ResourceType.AMMO))
        assertEquals(2, itemsEntries.size)

        val valuablesEntries = WarehouseDisplayHelper.buildDisplayEntries(
            resources = resources,
            selectedCategory = WarehouseFilterCategory.VALUABLES
        )
        val valuableTypes = valuablesEntries.map { it.resourceType }
        assertTrue(valuableTypes.contains(ResourceType.MONEY))
        assertTrue(valuableTypes.contains(ResourceType.RARE_ALLOY))
        assertEquals(2, valuablesEntries.size)
    }

    @Test
    fun testWarehouseVolumeOccupancyCalculations() {
        val resources = GameResources(
            money = 5000, // 0 storage volume
            food = 100,  // 100 volume
            water = 100, // 100 volume
            fuel = 50,   // 50 volume
            materials = 50, // 50 volume
            warehouseMaxCapacity = 1000,
            extraResources = mapOf(
                ResourceType.MEDICINE to 10,   // 10 volume (unitSize = 1)
                ResourceType.AMMO to 20,       // 20 volume (unitSize = 1)
                ResourceType.COMPONENTS to 10, // 10 volume (unitSize = 1)
                ResourceType.RARE_ALLOY to 15  // 30 volume (unitSize = 2)
            )
        )

        // Expected total volume: 100 + 100 + 50 + 50 + 10 + 20 + 10 + 30 = 370
        assertEquals(370, resources.totalStoredVolume)
        assertEquals(630, resources.availableCapacity)
        assertEquals(0.37f, resources.storageUsageFraction, 0.001f)

        val entries = WarehouseDisplayHelper.buildDisplayEntries(resources = resources)
        val moneyEntry = entries.find { it.resourceType == ResourceType.MONEY }
        val rareAlloyEntry = entries.find { it.resourceType == ResourceType.RARE_ALLOY }

        assertNotNull(moneyEntry)
        assertEquals(0, moneyEntry!!.totalStorageVolume)
        assertFalse(moneyEntry.isPhysical)

        assertNotNull(rareAlloyEntry)
        assertEquals(30, rareAlloyEntry!!.totalStorageVolume)
        assertEquals(2, rareAlloyEntry.unitSize)
        assertTrue(rareAlloyEntry.isPhysical)
    }

    @Test
    fun testWarehouseCapacityStatusThresholds() {
        assertEquals(WarehouseCapacityStatus.AMPLE, WarehouseCapacityStatus.fromUsageFraction(0.10f))
        assertEquals(WarehouseCapacityStatus.AMPLE, WarehouseCapacityStatus.fromUsageFraction(0.49f))
        assertEquals(WarehouseCapacityStatus.MODERATE, WarehouseCapacityStatus.fromUsageFraction(0.50f))
        assertEquals(WarehouseCapacityStatus.MODERATE, WarehouseCapacityStatus.fromUsageFraction(0.74f))
        assertEquals(WarehouseCapacityStatus.WARNING, WarehouseCapacityStatus.fromUsageFraction(0.75f))
        assertEquals(WarehouseCapacityStatus.WARNING, WarehouseCapacityStatus.fromUsageFraction(0.94f))
        assertEquals(WarehouseCapacityStatus.CRITICAL_FULL, WarehouseCapacityStatus.fromUsageFraction(0.95f))
        assertEquals(WarehouseCapacityStatus.CRITICAL_FULL, WarehouseCapacityStatus.fromUsageFraction(1.0f))
    }

    @Test
    fun testWarehouseSearchFiltering() {
        val resources = GameResources(
            money = 500,
            food = 100,
            water = 100,
            fuel = 50,
            materials = 50,
            extraResources = mapOf(ResourceType.MEDICINE to 10)
        )

        // Search by name
        val waterSearch = WarehouseDisplayHelper.buildDisplayEntries(
            resources = resources,
            searchQuery = "вода"
        )
        assertEquals(1, waterSearch.size)
        assertEquals(ResourceType.WATER, waterSearch[0].resourceType)

        // Search by description keyword
        val medSearch = WarehouseDisplayHelper.buildDisplayEntries(
            resources = resources,
            searchQuery = "антибиотики"
        )
        assertEquals(1, medSearch.size)
        assertEquals(ResourceType.MEDICINE, medSearch[0].resourceType)

        // Non-existent search
        val emptySearch = WarehouseDisplayHelper.buildDisplayEntries(
            resources = resources,
            searchQuery = "лазерный_меч"
        )
        assertTrue(emptySearch.isEmpty())
    }

    @Test
    fun testWarehouseSortingOptions() {
        val resources = GameResources(
            money = 1000,
            food = 50,
            water = 200,
            fuel = 20,
            materials = 150
        )

        // Sort by amount descending
        val amountSorted = WarehouseDisplayHelper.buildDisplayEntries(
            resources = resources,
            sortOption = WarehouseSortOption.AMOUNT_DESC
        )
        assertEquals(ResourceType.MONEY, amountSorted[0].resourceType) // 1000
        assertEquals(ResourceType.WATER, amountSorted[1].resourceType) // 200
        assertEquals(ResourceType.MATERIALS, amountSorted[2].resourceType) // 150

        // Sort by name ascending
        val nameSorted = WarehouseDisplayHelper.buildDisplayEntries(
            resources = resources,
            sortOption = WarehouseSortOption.NAME_ASC
        )
        val names = nameSorted.map { it.name }
        assertEquals(names.sorted(), names)
    }

    @Test
    fun testFutureDiscreteItemsSupport() {
        val resources = GameResources()
        val customItems = listOf(
            WarehouseItem(
                id = "item_medkit_pro",
                name = "Военная аптечка спецназа",
                category = ItemCategory.MEDICINE_AND_AID,
                quantity = 3,
                unitSize = 1,
                rarity = ItemRarity.RARE,
                description = "Комплект биогеля и армейских стимуляторов.",
                baseValueCredits = 60,
                sourcesRu = listOf("Секретный бункер"),
                usesRu = listOf("Мгновенное исцеление бойца")
            ),
            WarehouseItem(
                id = "item_relic_core",
                name = "Древнее энергоядро",
                category = ItemCategory.VALUABLES_AND_RELICS,
                quantity = 1,
                unitSize = 3,
                rarity = ItemRarity.LEGENDARY,
                description = "Довоенный плазменный генератор.",
                baseValueCredits = 500,
                sourcesRu = listOf("Зона-9"),
                usesRu = listOf("Питание реактора базы")
            )
        )

        val displayEntries = WarehouseDisplayHelper.buildDisplayEntries(
            resources = resources,
            futureItems = customItems,
            selectedCategory = WarehouseFilterCategory.ALL
        )

        val medkitEntry = displayEntries.find { it.id == "item_item_medkit_pro" }
        assertNotNull(medkitEntry)
        assertEquals(ItemRarity.RARE, medkitEntry!!.rarity)
        assertEquals(WarehouseFilterCategory.ITEMS, medkitEntry.category)
        assertEquals(3, medkitEntry.quantity)

        val relicEntry = displayEntries.find { it.id == "item_item_relic_core" }
        assertNotNull(relicEntry)
        assertEquals(ItemRarity.LEGENDARY, relicEntry!!.rarity)
        assertEquals(WarehouseFilterCategory.VALUABLES, relicEntry.category)
        assertEquals(3, relicEntry.totalStorageVolume) // 1 * 3 = 3 volume
    }

    @Test
    fun testResourceDepletionLevelsEvaluation() {
        val resources = GameResources(
            money = 10,   // Critical threshold for money is 20
            food = 15,    // Critical threshold for food is 18
            water = 35,   // Low threshold for water is 50
            fuel = 80,    // Normal
            materials = 150 // Normal
        )

        assertEquals(ResourceStateLevel.CRITICAL, resources.getDepletionLevel(ResourceType.MONEY))
        assertEquals(ResourceStateLevel.CRITICAL, resources.getDepletionLevel(ResourceType.FOOD))
        assertEquals(ResourceStateLevel.LOW, resources.getDepletionLevel(ResourceType.WATER))
        assertEquals(ResourceStateLevel.NORMAL, resources.getDepletionLevel(ResourceType.FUEL))
        assertEquals(ResourceStateLevel.NORMAL, resources.getDepletionLevel(ResourceType.MATERIALS))
    }
}
