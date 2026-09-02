package com.example.domain.model

/**
 * Filter categories for crafting recipes in the Workshop screen.
 */
enum class CraftRecipeCategory(val id: String, val titleRu: String) {
    ALL("all", "Все рецепты"),
    SURVIVAL("survival", "Выживание"),
    MEDICINE("medicine", "Медицина"),
    TOOLS("tools", "Инструменты"),
    EQUIPMENT("equipment", "Снаряжение"),
    COMPONENTS("components", "Компоненты")
}

/**
 * Definition of a craftable item blueprint in the settlement Workshop.
 *
 * @param id Unique identifier of the recipe
 * @param nameRu Localized display name
 * @param category Category for tab filtering
 * @param descriptionRu Flavor and functional description
 * @param outputItem Template of the warehouse item created upon crafting
 * @param outputQuantity Number of items produced per single craft operation
 * @param requiredResources Map of resources consumed for single craft operation
 * @param minWorkshopLevel Minimum level of Workshop building required to unlock
 * @param requiredSettlementLevel Minimum settlement level required
 * @param requiredTechId Optional tech ID that must be researched first
 * @param craftingTimeSeconds Base production duration in seconds (prepared for future time-based queues)
 * @param iconKey Icon visual key
 * @param loreRu Background lore or mechanic notes
 */
data class CraftRecipe(
    val id: String,
    val nameRu: String,
    val category: CraftRecipeCategory,
    val descriptionRu: String,
    val outputItem: WarehouseItem,
    val outputQuantity: Int = 1,
    val requiredResources: Map<ResourceType, Int>,
    val minWorkshopLevel: Int = 1,
    val requiredSettlementLevel: Int = 1,
    val requiredTechId: String? = null,
    val craftingTimeSeconds: Int = 0,
    val iconKey: String = "workbench",
    val loreRu: String = ""
) {
    val totalOutputVolume: Int get() = outputQuantity * outputItem.unitSize

    /**
     * Checks if this blueprint is unlocked based on workshop level, settlement level, and research.
     */
    fun isUnlocked(
        workshopLevel: Int,
        settlementLevel: Int,
        researchedTechIds: Set<String> = emptySet()
    ): Boolean {
        if (workshopLevel < minWorkshopLevel) return false
        if (settlementLevel < requiredSettlementLevel) return false
        if (requiredTechId != null && !researchedTechIds.contains(requiredTechId)) return false
        return true
    }
}

/**
 * Specific reason for craft failure for user feedback and error prevention.
 */
enum class CraftFailureReason(val messageRu: String) {
    WORKSHOP_NOT_BUILT("Мастерская ещё не возведена в поселении. Постройте её в меню объектов."),
    INSUFFICIENT_WORKSHOP_LEVEL("Недостаточный уровень Мастерской для создания этого предмета."),
    INSUFFICIENT_SETTLEMENT_LEVEL("Недостаточный уровень поселения."),
    INSUFFICIENT_RESOURCES("Недостаточно ресурсов на складе для крафта."),
    INSUFFICIENT_STORAGE("Недостаточно свободного места на складе для готовых предметов."),
    INVALID_QUANTITY("Количество для создания должно быть больше 0."),
    TECH_NOT_RESEARCHED("Требуется изучить соответствующую технологию в лаборатории.")
}

/**
 * Typed result of an atomic craft transaction.
 */
sealed interface CraftTransactionResult {
    val isSuccess: Boolean
    val message: String

    data class Success(
        val recipe: CraftRecipe,
        val craftCount: Int,
        val totalItemsCreated: Int,
        val consumedResources: Map<ResourceType, Int>,
        override val message: String
    ) : CraftTransactionResult {
        override val isSuccess: Boolean get() = true
    }

    data class Failure(
        val reason: CraftFailureReason,
        override val message: String,
        val missingResources: Map<ResourceType, Int> = emptyMap(),
        val requiredCapacity: Int = 0,
        val availableCapacity: Int = 0
    ) : CraftTransactionResult {
        override val isSuccess: Boolean get() = false
    }
}
