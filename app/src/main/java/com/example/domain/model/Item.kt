package com.example.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

/**
 * Filter categories for the warehouse screen.
 */
enum class WarehouseFilterCategory(val id: String, val titleRu: String) {
    ALL("all", "Все"),
    PRIMARY("primary", "Основные"),
    MATERIALS("materials", "Материалы"),
    ITEMS("items", "Предметы"),
    VALUABLES("valuables", "Ценные")
}

/**
 * Rarity tier for items and resources.
 */
enum class ItemRarity(
    val titleRu: String,
    val color: Color
) {
    COMMON("Обычный", SafeEmerald),
    UNCOMMON("Необычный", TechCyan),
    RARE("Редкий", StoragePurple),
    EPIC("Эпический", WarningAmber),
    LEGENDARY("Легендарный", DangerCrimson)
}

/**
 * Detailed categorization for discrete items, prepared for future expedition loot,
 * gear, weapons, components and relics.
 */
enum class ItemCategory(
    val titleRu: String,
    val filterCategory: WarehouseFilterCategory
) {
    PRIMARY_SUPPLIES("Основные припасы", WarehouseFilterCategory.PRIMARY),
    CONSTRUCTION_MATERIALS("Стройматериалы", WarehouseFilterCategory.MATERIALS),
    ELECTRONICS_AND_PARTS("Электроника и детали", WarehouseFilterCategory.MATERIALS),
    MEDICINE_AND_AID("Медикаменты", WarehouseFilterCategory.ITEMS),
    AMMO_AND_MILITARY("Боеприпасы и оружие", WarehouseFilterCategory.ITEMS),
    EQUIPMENT_AND_TOOLS("Снаряжение и инструменты", WarehouseFilterCategory.ITEMS),
    VALUABLES_AND_RELICS("Ценности и реликвии", WarehouseFilterCategory.VALUABLES)
}

/**
 * Discrete inventory item structure representing gear, tools, loot, materials and equipment.
 */
data class WarehouseItem(
    val id: String,
    val name: String,
    val category: ItemCategory,
    val quantity: Int = 1,
    val unitSize: Int = 1,
    val rarity: ItemRarity = ItemRarity.COMMON,
    val description: String,
    val baseValueCredits: Int = 10,
    val iconKey: String? = null,
    val sourcesRu: List<String> = emptyList(),
    val usesRu: List<String> = emptyList(),
    val properties: Map<String, String> = emptyMap(),
    val isEquipable: Boolean = false,
    val equipSlot: EquipmentSlotType? = null,
    val weightKg: Float = 1.0f,
    val equipmentBonus: EquipmentBonus = EquipmentBonus()
) {
    val isEquippedSlotCompatible: (EquipmentSlotType) -> Boolean
        get() = { slot -> isEquipable && (equipSlot == slot) }
}

/**
 * Storage load level indicator for the warehouse.
 */
enum class WarehouseCapacityStatus(
    val titleRu: String,
    val subtitleRu: String,
    val accentColor: Color
) {
    AMPLE(
        titleRu = "Свободно",
        subtitleRu = "В хранилище достаточно места для новых припасов.",
        accentColor = SafeEmerald
    ),
    MODERATE(
        titleRu = "Штатная загрузка",
        subtitleRu = "Склад заполнен наполовину. Вместимость стабильна.",
        accentColor = TechCyan
    ),
    WARNING(
        titleRu = "Склад заполняется",
        subtitleRu = "Свободного места мало. Рекомендуется модернизировать склад.",
        accentColor = WarningAmber
    ),
    CRITICAL_FULL(
        titleRu = "Переполнение!",
        subtitleRu = "Хранилище заполнено! Новая добыча из экспедиций не поместится.",
        accentColor = DangerCrimson
    );

    companion object {
        fun fromUsageFraction(fraction: Float): WarehouseCapacityStatus {
            return when {
                fraction >= 0.95f -> CRITICAL_FULL
                fraction >= 0.75f -> WARNING
                fraction >= 0.50f -> MODERATE
                else -> AMPLE
            }
        }
    }
}

/**
 * Sorting options for warehouse display.
 */
enum class WarehouseSortOption(val titleRu: String) {
    DEFAULT("По умолчанию"),
    AMOUNT_DESC("По количеству (убыв.)"),
    VOLUME_DESC("По объёму на складе"),
    NAME_ASC("По названию (А-Я)")
}

/**
 * Unified display model representing any resource or item in the warehouse UI.
 */
data class WarehouseItemDisplay(
    val id: String,
    val name: String,
    val quantity: Int,
    val unitSize: Int,
    val isPhysical: Boolean,
    val totalStorageVolume: Int,
    val category: WarehouseFilterCategory,
    val categoryLabel: String,
    val rarity: ItemRarity,
    val stateLevel: ResourceStateLevel,
    val description: String,
    val purposeRu: String,
    val baseMarketValueCredits: Int,
    val sourcesRu: List<String>,
    val usesRu: List<String>,
    val iconKey: String,
    val resourceType: ResourceType? = null,
    val warehouseItem: WarehouseItem? = null,
    val lowThreshold: Int = 20,
    val criticalThreshold: Int = 5
)

/**
 * Helper object to build unified warehouse display entries from current GameResources and future items.
 */
object WarehouseDisplayHelper {

    fun buildDisplayEntries(
        resources: GameResources,
        futureItems: List<WarehouseItem> = emptyList(),
        selectedCategory: WarehouseFilterCategory = WarehouseFilterCategory.ALL,
        searchQuery: String = "",
        sortOption: WarehouseSortOption = WarehouseSortOption.DEFAULT
    ): List<WarehouseItemDisplay> {
        val entries = mutableListOf<WarehouseItemDisplay>()

        // 1. Process all standard ResourceTypes
        ResourceType.entries.forEach { type ->
            val descriptor = ResourceRegistry.getDescriptor(type)
            val quantity = resources[type]
            val stateLevel = resources.getDepletionLevel(type)
            val totalVolume = if (type.isPhysical) quantity * type.unitSize else 0

            val rarity = when (type) {
                ResourceType.RARE_ALLOY -> ItemRarity.RARE
                ResourceType.COMPONENTS -> ItemRarity.UNCOMMON
                ResourceType.MEDICINE, ResourceType.AMMO -> ItemRarity.UNCOMMON
                else -> ItemRarity.COMMON
            }

            val iconKey = when (type) {
                ResourceType.MONEY -> "money"
                ResourceType.FOOD -> "food"
                ResourceType.WATER -> "water"
                ResourceType.FUEL -> "fuel"
                ResourceType.MATERIALS -> "materials"
                ResourceType.MEDICINE -> "medicine"
                ResourceType.AMMO -> "ammo"
                ResourceType.COMPONENTS -> "components"
                ResourceType.RARE_ALLOY -> "rare_alloy"
            }

            entries.add(
                WarehouseItemDisplay(
                    id = "res_${type.id}",
                    name = type.nameRu,
                    quantity = quantity,
                    unitSize = type.unitSize,
                    isPhysical = type.isPhysical,
                    totalStorageVolume = totalVolume,
                    category = descriptor.warehouseCategory,
                    categoryLabel = descriptor.category.titleRu,
                    rarity = rarity,
                    stateLevel = stateLevel,
                    description = descriptor.descriptionRu,
                    purposeRu = descriptor.purposeRu,
                    baseMarketValueCredits = descriptor.baseMarketValueCredits,
                    sourcesRu = descriptor.sourcesRu,
                    usesRu = descriptor.usesRu,
                    iconKey = iconKey,
                    resourceType = type,
                    lowThreshold = descriptor.lowThreshold,
                    criticalThreshold = descriptor.criticalThreshold
                )
            )
        }

        // 2. Process future discrete items
        futureItems.forEach { item ->
            val totalVolume = item.quantity * item.unitSize
            entries.add(
                WarehouseItemDisplay(
                    id = "item_${item.id}",
                    name = item.name,
                    quantity = item.quantity,
                    unitSize = item.unitSize,
                    isPhysical = true,
                    totalStorageVolume = totalVolume,
                    category = item.category.filterCategory,
                    categoryLabel = item.category.titleRu,
                    rarity = item.rarity,
                    stateLevel = if (item.quantity <= 1) ResourceStateLevel.LOW else ResourceStateLevel.NORMAL,
                    description = item.description,
                    purposeRu = item.category.titleRu,
                    baseMarketValueCredits = item.baseValueCredits,
                    sourcesRu = item.sourcesRu,
                    usesRu = item.usesRu,
                    iconKey = item.iconKey ?: "box",
                    warehouseItem = item,
                    lowThreshold = 2,
                    criticalThreshold = 1
                )
            )
        }

        // 3. Filter by category
        val categoryFiltered = if (selectedCategory == WarehouseFilterCategory.ALL) {
            entries
        } else {
            entries.filter { it.category == selectedCategory }
        }

        // 4. Filter by search query
        val searchFiltered = if (searchQuery.isBlank()) {
            categoryFiltered
        } else {
            val query = searchQuery.trim().lowercase()
            categoryFiltered.filter {
                it.name.lowercase().contains(query) ||
                        it.description.lowercase().contains(query) ||
                        it.categoryLabel.lowercase().contains(query) ||
                        it.purposeRu.lowercase().contains(query)
            }
        }

        // 5. Apply sorting
        return when (sortOption) {
            WarehouseSortOption.DEFAULT -> searchFiltered
            WarehouseSortOption.AMOUNT_DESC -> searchFiltered.sortedByDescending { it.quantity }
            WarehouseSortOption.VOLUME_DESC -> searchFiltered.sortedByDescending { it.totalStorageVolume }
            WarehouseSortOption.NAME_ASC -> searchFiltered.sortedBy { it.name }
        }
    }
}
