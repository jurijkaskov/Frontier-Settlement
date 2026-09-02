package com.example

import com.example.data.CraftConfig
import com.example.domain.model.*
import com.example.viewmodel.GameViewModel
import org.junit.Assert.*
import org.junit.Test

/**
 * Comprehensive Unit Tests for Point 7 (Workshop & Crafting System).
 *
 * Verifies:
 * - Recipe catalogue initialization and blueprint correctness
 * - Maximum craftable batch calculations with resource & storage constraints
 * - Safe atomic crafting operations via GameViewModel
 * - Invariant preservation (zero partial mutations on failure)
 * - Workshop building level requirement gating
 * - Storage volume delta tracking when manufacturing physical items
 * - Multiple items merging in inventory
 */
class WorkshopCraftingSystemTest {

    @Test
    fun testCraftConfig_CatalogueInitialization() {
        val recipes = CraftConfig.createDefaultRecipes()
        assertTrue(recipes.isNotEmpty())
        assertTrue("Expected at least 8 crafting recipes", recipes.size >= 8)

        val medkitRecipe = recipes.find { it.id == "recipe_medkit" }
        assertNotNull(medkitRecipe)
        assertEquals("Полевая аптечка", medkitRecipe!!.nameRu)
        assertEquals(CraftRecipeCategory.MEDICINE, medkitRecipe.category)
        assertEquals(1, medkitRecipe.minWorkshopLevel)
        assertTrue(medkitRecipe.requiredResources.containsKey(ResourceType.MATERIALS))
        assertTrue(medkitRecipe.requiredResources.containsKey(ResourceType.MEDICINE))
        assertTrue(medkitRecipe.requiredResources.containsKey(ResourceType.WATER))
    }

    @Test
    fun testCalculateMaxCraftCount_RespectsMaterialsConstraint() {
        val recipe = CraftConfig.createDefaultRecipes().find { it.id == "recipe_medkit" }!!
        // Recipe needs 20 materials, 3 medicine, 10 water
        val resources = GameResources(
            materials = 50, // allows 2 batches
            water = 100,    // allows 10 batches
            extraResources = mapOf(ResourceType.MEDICINE to 10) // allows 3 batches
        )

        val maxBatches = CraftConfig.calculateMaxCraftCount(
            recipe = recipe,
            resources = resources,
            currentInventory = emptyList()
        )

        assertEquals(2, maxBatches)
    }

    @Test
    fun testCalculateMaxCraftCount_ZeroWhenLackingResource() {
        val recipe = CraftConfig.createDefaultRecipes().find { it.id == "recipe_medkit" }!!
        val resources = GameResources(
            materials = 100,
            water = 100,
            extraResources = mapOf(ResourceType.MEDICINE to 0) // zero medicine
        )

        val maxBatches = CraftConfig.calculateMaxCraftCount(
            recipe = recipe,
            resources = resources,
            currentInventory = emptyList()
        )

        assertEquals(0, maxBatches)
    }

    @Test
    fun testCraftItem_SuccessfulAtomicExecution() {
        val viewModel = GameViewModel()
        viewModel.debugAddCraftingSupplies()

        val stateBefore = viewModel.gameState.value
        val initialMaterials = stateBefore.resources.materials
        val initialMedicine = stateBefore.resources[ResourceType.MEDICINE]
        val initialWater = stateBefore.resources.water
        val initialInventoryCount = stateBefore.inventoryItems.size

        val recipe = CraftConfig.createDefaultRecipes().find { it.id == "recipe_medkit" }!!
        val result = viewModel.craftItem(recipe.id, craftCount = 1)

        assertTrue(result.isSuccess)
        val stateAfter = viewModel.gameState.value

        // Verify resource deduction
        assertEquals(initialMaterials - 20, stateAfter.resources.materials)
        assertEquals(initialMedicine - 3, stateAfter.resources[ResourceType.MEDICINE])
        assertEquals(initialWater - 10, stateAfter.resources.water)

        // Verify inventory update
        val medkitItem = stateAfter.inventoryItems.find { it.id == "item_medkit" }
        assertNotNull(medkitItem)
        assertEquals(1, medkitItem!!.quantity)
        assertEquals(1, medkitItem.unitSize)
    }

    @Test
    fun testCraftItem_MultipleBatchesAccumulate() {
        val viewModel = GameViewModel()
        viewModel.debugAddCraftingSupplies()

        val recipe = CraftConfig.createDefaultRecipes().find { it.id == "recipe_ration_pack" }!!
        // Ration pack recipe yields 2 rations per batch, costs 30 food, 15 water
        val result = viewModel.craftItem(recipe.id, craftCount = 2)

        assertTrue(result.isSuccess)
        val stateAfter = viewModel.gameState.value

        val rationItem = stateAfter.inventoryItems.find { it.id == "item_ration_pack" }
        assertNotNull(rationItem)
        assertEquals(4, rationItem!!.quantity) // 2 batches * 2 yield = 4 items
    }

    @Test
    fun testCraftItem_FailsWhenInsufficientResources_NoStateMutation() {
        val viewModel = GameViewModel()
        // Drain resources so crafting is impossible
        viewModel.debugDrainSupplies()

        val stateBefore = viewModel.gameState.value
        val recipe = CraftConfig.createDefaultRecipes().find { it.id == "recipe_medkit" }!!

        val result = viewModel.craftItem(recipe.id, craftCount = 1)

        assertFalse(result.isSuccess)
        assertTrue(result is CraftTransactionResult.Failure)
        assertEquals(CraftFailureReason.INSUFFICIENT_RESOURCES, (result as CraftTransactionResult.Failure).reason)

        // Invariant check: zero state alteration
        val stateAfter = viewModel.gameState.value
        assertEquals(stateBefore.resources, stateAfter.resources)
        assertEquals(stateBefore.inventoryItems, stateAfter.inventoryItems)
    }

    @Test
    fun testCraftItem_FailsWhenInsufficientWorkshopLevel() {
        val viewModel = GameViewModel()
        viewModel.debugAddCraftingSupplies()

        // Level 3 blueprint: Bio-Scanner requires Workshop Level 3 (Initial Workshop is Level 1)
        val recipe = CraftConfig.createDefaultRecipes().find { it.id == "recipe_bio_scanner" }!!
        val result = viewModel.craftItem(recipe.id, craftCount = 1)

        assertFalse(result.isSuccess)
        assertTrue(result is CraftTransactionResult.Failure)
        assertEquals(CraftFailureReason.INSUFFICIENT_WORKSHOP_LEVEL, (result as CraftTransactionResult.Failure).reason)
    }

    @Test
    fun testCraftItem_FailsWhenStorageFull() {
        val recipe = CraftConfig.createDefaultRecipes().find { it.id == "recipe_medkit" }!!
        // Validation fails when final stored volume exceeds max capacity
        val failureReason = CraftConfig.validateCraft(
            recipe = recipe,
            workshopBuilding = Building(
                id = "b_ws",
                name = "Мастерская",
                type = BuildingType.WORKSHOP,
                level = 1,
                description = "Мастерская",
                status = BuildingStatus.OPERATIONAL
            ),
            settlementLevel = 1,
            resources = GameResources(
                materials = 50,
                extraResources = mapOf(ResourceType.MEDICINE to 10),
                water = 30,
                warehouseMaxCapacity = 100
            ),
            currentInventory = listOf(
                WarehouseItem(
                    id = "item_overflow",
                    name = "Тяжёлый груз",
                    category = ItemCategory.PRIMARY_SUPPLIES,
                    quantity = 100,
                    unitSize = 1,
                    description = "Тестовый груз"
                )
            ),
            craftCount = 1
        )

        assertNotNull(failureReason)
        assertEquals(CraftFailureReason.INSUFFICIENT_STORAGE, failureReason)
    }

    @Test
    fun testWarehouseItemDisplay_IncludesCraftedItems() {
        val resources = GameResources(materials = 100, food = 50)
        val craftedItems = listOf(
            WarehouseItem(
                id = "item_medkit",
                name = "Полевая аптечка",
                category = ItemCategory.MEDICINE_AND_AID,
                quantity = 3,
                unitSize = 1,
                rarity = ItemRarity.COMMON,
                description = "Бинты и антисептик",
                baseValueCredits = 25
            )
        )

        val allDisplays = WarehouseDisplayHelper.buildDisplayEntries(
            resources = resources,
            futureItems = craftedItems,
            selectedCategory = WarehouseFilterCategory.ALL
        )

        val medkitDisplay = allDisplays.find { it.id == "item_item_medkit" }
        assertNotNull(medkitDisplay)
        assertEquals("Полевая аптечка", medkitDisplay!!.name)
        assertEquals(3, medkitDisplay.quantity)
        assertEquals(3, medkitDisplay.totalStorageVolume)
        assertEquals(WarehouseFilterCategory.ITEMS, medkitDisplay.category)
    }
}
