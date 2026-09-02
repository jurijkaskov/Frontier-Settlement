package com.example.domain.service

import com.example.domain.model.*

/**
 * Comparison item used by UI when previewing equipment replacements.
 */
data class StatComparisonItem(
    val statName: String,
    val currentValue: Int,
    val newValue: Int,
    val difference: Int,
    val unit: String = ""
) {
    val isPositive: Boolean get() = difference > 0
    val isNegative: Boolean get() = difference < 0
}

/**
 * Result bundle containing updated domain state and an operation outcome.
 */
data class EquipmentOperationOutcome(
    val updatedCharacters: List<Character>,
    val result: EquipmentOperationResult
)

/**
 * Centralized business logic service for character equipment and gear loadouts.
 * Enforces single source of truth, avoids item duplication, and handles stat aggregation.
 */
object EquipmentService {

    /**
     * Finds all warehouse items that are compatible with the specified slot and currently unequipped,
     * or lists them with their current equipped status.
     */
    fun getAvailableItemsForSlot(
        allItems: List<WarehouseItem>,
        characters: List<Character>,
        slot: EquipmentSlotType,
        excludeCurrentCharacterId: String? = null
    ): List<WarehouseItem> {
        // Collect all currently equipped item IDs across all characters
        val equippedMap = mutableMapOf<String, String>() // itemId -> characterId
        characters.forEach { char ->
            char.equipment.slots.forEach { (_, itemId) ->
                if (!itemId.isNullOrBlank()) {
                    equippedMap[itemId] = char.id
                }
            }
        }

        return allItems.filter { item ->
            // Must be equipable and compatible with this slot
            item.isEquipable && item.equipSlot == slot &&
                    // Either not equipped, or equipped by current character in this slot
                    (!equippedMap.containsKey(item.id) || (excludeCurrentCharacterId != null && equippedMap[item.id] == excludeCurrentCharacterId))
        }
    }

    /**
     * Resolves which character (if any) currently has this item equipped.
     */
    fun getCharacterEquippingItem(itemId: String, characters: List<Character>): Pair<Character, EquipmentSlotType>? {
        for (char in characters) {
            for ((slot, eqItemId) in char.equipment.slots) {
                if (eqItemId == itemId) {
                    return Pair(char, slot)
                }
            }
        }
        return null
    }

    /**
     * Equips an item to a character's specific slot.
     * Safely un-equips any existing item in that slot and handles item transfer from another character if necessary.
     */
    fun equipItem(
        characters: List<Character>,
        allItems: List<WarehouseItem>,
        characterId: String,
        slot: EquipmentSlotType,
        itemId: String
    ): EquipmentOperationOutcome {
        val targetChar = characters.find { it.id == characterId }
            ?: return EquipmentOperationOutcome(
                updatedCharacters = characters,
                result = EquipmentOperationResult.Failure("NOT_FOUND", "Персонаж не найден")
            )

        val itemToEquip = allItems.find { it.id == itemId }
            ?: return EquipmentOperationOutcome(
                updatedCharacters = characters,
                result = EquipmentOperationResult.Failure("ITEM_NOT_FOUND", "Предмет не найден на складе")
            )

        if (!itemToEquip.isEquipable) {
            return EquipmentOperationOutcome(
                updatedCharacters = characters,
                result = EquipmentOperationResult.Failure("NOT_EQUIPABLE", "Этот предмет нельзя экипировать")
            )
        }

        if (itemToEquip.equipSlot != slot) {
            return EquipmentOperationOutcome(
                updatedCharacters = characters,
                result = EquipmentOperationResult.Failure(
                    "INCOMPATIBLE_SLOT",
                    "Предмет ${itemToEquip.name} предназначен для слота «${itemToEquip.equipSlot?.titleRu ?: "Другой"}»"
                )
            )
        }

        val previousEquippedItemId = targetChar.equipment.getItemId(slot)
        val previousItem = previousEquippedItemId?.let { id -> allItems.find { it.id == id } }

        // Check if another character is currently wearing this exact item
        val otherEquipper = getCharacterEquippingItem(itemId, characters)

        val updatedCharacters = characters.map { char ->
            when {
                // If another character had this item, unequip it from them
                otherEquipper != null && char.id == otherEquipper.first.id && char.id != characterId -> {
                    char.copy(equipment = char.equipment.unequip(otherEquipper.second))
                }
                // Target character equips the item in slot
                char.id == characterId -> {
                    char.copy(equipment = char.equipment.equip(slot, itemId))
                }
                else -> char
            }
        }

        val message = if (previousItem != null) {
            "«${targetChar.name}» сменил «${previousItem.name}» на «${itemToEquip.name}» в слоте ${slot.titleRu}."
        } else {
            "«${targetChar.name}» экипировал «${itemToEquip.name}» (${slot.titleRu})."
        }

        return EquipmentOperationOutcome(
            updatedCharacters = updatedCharacters,
            result = EquipmentOperationResult.Success(
                message = message,
                characterId = characterId,
                slot = slot,
                itemId = itemId,
                replacedItemId = previousEquippedItemId
            )
        )
    }

    /**
     * Unequips an item from the specified character slot, returning it to the settlement warehouse.
     */
    fun unequipItem(
        characters: List<Character>,
        allItems: List<WarehouseItem>,
        characterId: String,
        slot: EquipmentSlotType
    ): EquipmentOperationOutcome {
        val targetChar = characters.find { it.id == characterId }
            ?: return EquipmentOperationOutcome(
                updatedCharacters = characters,
                result = EquipmentOperationResult.Failure("NOT_FOUND", "Персонаж не найден")
            )

        val equippedItemId = targetChar.equipment.getItemId(slot)
            ?: return EquipmentOperationOutcome(
                updatedCharacters = characters,
                result = EquipmentOperationResult.Failure("SLOT_EMPTY", "В слоте ${slot.titleRu} нет экипированного предмета")
            )

        val item = allItems.find { it.id == equippedItemId }
        val itemName = item?.name ?: "Предмет"

        val updatedCharacters = characters.map { char ->
            if (char.id == characterId) {
                char.copy(equipment = char.equipment.unequip(slot))
            } else char
        }

        return EquipmentOperationOutcome(
            updatedCharacters = updatedCharacters,
            result = EquipmentOperationResult.Success(
                message = "«${targetChar.name}» снял «$itemName» (${slot.titleRu}). Предмет возвращён на склад.",
                characterId = characterId,
                slot = slot,
                itemId = null,
                replacedItemId = equippedItemId
            )
        )
    }

    /**
     * Compares the stats of two items to assist player decision making.
     */
    fun compareItems(current: WarehouseItem?, prospective: WarehouseItem): List<StatComparisonItem> {
        val curBonus = current?.equipmentBonus ?: EquipmentBonus()
        val newBonus = prospective.equipmentBonus

        val list = mutableListOf<StatComparisonItem>()

        if (curBonus.bonusCarryCapacityKg != 0 || newBonus.bonusCarryCapacityKg != 0) {
            list.add(
                StatComparisonItem(
                    statName = "Вместимость",
                    currentValue = curBonus.bonusCarryCapacityKg,
                    newValue = newBonus.bonusCarryCapacityKg,
                    difference = newBonus.bonusCarryCapacityKg - curBonus.bonusCarryCapacityKg,
                    unit = " кг"
                )
            )
        }

        if (curBonus.bonusAttack != 0 || newBonus.bonusAttack != 0) {
            list.add(
                StatComparisonItem(
                    statName = "Атака",
                    currentValue = curBonus.bonusAttack,
                    newValue = newBonus.bonusAttack,
                    difference = newBonus.bonusAttack - curBonus.bonusAttack
                )
            )
        }

        if (curBonus.bonusDefense != 0 || newBonus.bonusDefense != 0) {
            list.add(
                StatComparisonItem(
                    statName = "Защита",
                    currentValue = curBonus.bonusDefense,
                    newValue = newBonus.bonusDefense,
                    difference = newBonus.bonusDefense - curBonus.bonusDefense
                )
            )
        }

        if (curBonus.bonusMaxHealth != 0 || newBonus.bonusMaxHealth != 0) {
            list.add(
                StatComparisonItem(
                    statName = "Макс. Здоровье",
                    currentValue = curBonus.bonusMaxHealth,
                    newValue = newBonus.bonusMaxHealth,
                    difference = newBonus.bonusMaxHealth - curBonus.bonusMaxHealth,
                    unit = " HP"
                )
            )
        }

        if (curBonus.bonusScavenging != 0 || newBonus.bonusScavenging != 0) {
            list.add(
                StatComparisonItem(
                    statName = "Поиск",
                    currentValue = curBonus.bonusScavenging,
                    newValue = newBonus.bonusScavenging,
                    difference = newBonus.bonusScavenging - curBonus.bonusScavenging
                )
            )
        }

        if (curBonus.bonusEngineering != 0 || newBonus.bonusEngineering != 0) {
            list.add(
                StatComparisonItem(
                    statName = "Инженерия",
                    currentValue = curBonus.bonusEngineering,
                    newValue = newBonus.bonusEngineering,
                    difference = newBonus.bonusEngineering - curBonus.bonusEngineering
                )
            )
        }

        if (curBonus.bonusMedical != 0 || newBonus.bonusMedical != 0) {
            list.add(
                StatComparisonItem(
                    statName = "Медицина",
                    currentValue = curBonus.bonusMedical,
                    newValue = newBonus.bonusMedical,
                    difference = newBonus.bonusMedical - curBonus.bonusMedical
                )
            )
        }

        return list
    }
}
