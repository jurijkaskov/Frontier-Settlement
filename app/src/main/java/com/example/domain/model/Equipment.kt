package com.example.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.TechCyan
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningOrange

/**
 * Standard equipment slots available for each survivor on the frontier.
 */
enum class EquipmentSlotType(
    val id: String,
    val titleRu: String,
    val iconKey: String,
    val emoji: String,
    val descriptionRu: String,
    val categoryColor: Color
) {
    OUTFIT(
        id = "outfit",
        titleRu = "Верхняя одежда",
        iconKey = "jacket",
        emoji = "🧥",
        descriptionRu = "Защитные куртки, сталкерские плащи, лёгкие и тяжёлые бронекостюмы",
        categoryColor = TechCyan
    ),
    FOOTWEAR(
        id = "footwear",
        titleRu = "Обувь",
        iconKey = "footwear",
        emoji = "🥾",
        descriptionRu = "Походные ботинки, тактические армейские берцы и утепленная обувь",
        categoryColor = SafeEmerald
    ),
    BACKPACK(
        id = "backpack",
        titleRu = "Рюкзак",
        iconKey = "backpack",
        emoji = "🎒",
        descriptionRu = "Экспедиционные ранцы и баулы, увеличивающие переносимый груз",
        categoryColor = WarningAmber
    ),
    TOOL(
        id = "tool",
        titleRu = "Инструмент",
        iconKey = "tool",
        emoji = "🔧",
        descriptionRu = "Полевые мультитулы, ремонтные наборы, сканеры руин и мачете",
        categoryColor = WarningOrange
    ),
    SPECIAL(
        id = "special",
        titleRu = "Спецпредмет",
        iconKey = "special",
        emoji = "🎖️",
        descriptionRu = "Приборы ночного видения, радиокомпасы, реаниматоры и жетоны",
        categoryColor = Color(0xFFB388FF)
    )
}

/**
 * Structured stats and passive bonuses provided by an equipable piece of gear.
 */
data class EquipmentBonus(
    val bonusAttack: Int = 0,
    val bonusDefense: Int = 0,
    val bonusScavenging: Int = 0,
    val bonusEngineering: Int = 0,
    val bonusMedical: Int = 0,
    val bonusMaxHealth: Int = 0,
    val bonusCarryCapacityKg: Int = 0,
    val bonusColdResistance: Int = 0,
    val bonusSpeedPercent: Int = 0,
    val bonusMoraleDrainReduction: Int = 0
) {
    val hasAnyBonus: Boolean
        get() = bonusAttack != 0 || bonusDefense != 0 || bonusScavenging != 0 ||
                bonusEngineering != 0 || bonusMedical != 0 || bonusMaxHealth != 0 ||
                bonusCarryCapacityKg != 0 || bonusColdResistance != 0 ||
                bonusSpeedPercent != 0 || bonusMoraleDrainReduction != 0

    fun getFormattedBonusList(): List<String> {
        val list = mutableListOf<String>()
        if (bonusCarryCapacityKg != 0) {
            list.add("${if (bonusCarryCapacityKg > 0) "+" else ""}$bonusCarryCapacityKg кг груза")
        }
        if (bonusAttack != 0) {
            list.add("${if (bonusAttack > 0) "+" else ""}$bonusAttack к Атаке")
        }
        if (bonusDefense != 0) {
            list.add("${if (bonusDefense > 0) "+" else ""}$bonusDefense к Защите")
        }
        if (bonusMaxHealth != 0) {
            list.add("${if (bonusMaxHealth > 0) "+" else ""}$bonusMaxHealth к Макс. HP")
        }
        if (bonusScavenging != 0) {
            list.add("${if (bonusScavenging > 0) "+" else ""}$bonusScavenging к Поиску")
        }
        if (bonusEngineering != 0) {
            list.add("${if (bonusEngineering > 0) "+" else ""}$bonusEngineering к Инженерии")
        }
        if (bonusMedical != 0) {
            list.add("${if (bonusMedical > 0) "+" else ""}$bonusMedical к Медицине")
        }
        if (bonusColdResistance != 0) {
            list.add("+${bonusColdResistance}% стойкость к холоду")
        }
        if (bonusSpeedPercent != 0) {
            list.add("+${bonusSpeedPercent}% скорость перехода")
        }
        if (bonusMoraleDrainReduction != 0) {
            list.add("-${bonusMoraleDrainReduction}% потеря морали")
        }
        return list
    }

    fun toShortSummary(): String {
        val bonuses = getFormattedBonusList()
        return if (bonuses.isEmpty()) "Без прямых числовых бонусов" else bonuses.joinToString(" • ")
    }
}

/**
 * Character's active equipment loadout.
 * Holds references to discrete item IDs in the central inventory/warehouse.
 * Prevents item duplication while maintaining pure references.
 */
data class CharacterEquipment(
    val slots: Map<EquipmentSlotType, String?> = emptyMap()
) {
    fun getItemId(slot: EquipmentSlotType): String? = slots[slot]

    fun isSlotEquipped(slot: EquipmentSlotType): Boolean = !slots[slot].isNullOrBlank()

    fun equip(slot: EquipmentSlotType, itemId: String): CharacterEquipment {
        val updated = slots.toMutableMap()
        updated[slot] = itemId
        return copy(slots = updated)
    }

    fun unequip(slot: EquipmentSlotType): CharacterEquipment {
        val updated = slots.toMutableMap()
        updated.remove(slot)
        return copy(slots = updated)
    }

    val equippedItemIds: List<String>
        get() = slots.values.filterNotNull().filter { it.isNotBlank() }

    val equippedSlotsCount: Int
        get() = equippedItemIds.size

    val totalSlotsCount: Int
        get() = EquipmentSlotType.entries.size

    val isFullyEquipped: Boolean
        get() = equippedSlotsCount >= totalSlotsCount

    val hasBackpack: Boolean
        get() = !slots[EquipmentSlotType.BACKPACK].isNullOrBlank()

    val hasOutfit: Boolean
        get() = !slots[EquipmentSlotType.OUTFIT].isNullOrBlank()

    val hasFootwear: Boolean
        get() = !slots[EquipmentSlotType.FOOTWEAR].isNullOrBlank()

    val hasTool: Boolean
        get() = !slots[EquipmentSlotType.TOOL].isNullOrBlank()

    val hasSpecial: Boolean
        get() = !slots[EquipmentSlotType.SPECIAL].isNullOrBlank()
}

/**
 * Outcome of an equipment operation (equip, unequip, swap).
 */
sealed class EquipmentOperationResult {
    data class Success(
        val message: String,
        val characterId: String,
        val slot: EquipmentSlotType,
        val itemId: String? = null,
        val replacedItemId: String? = null
    ) : EquipmentOperationResult()

    data class Failure(
        val reason: String,
        val message: String
    ) : EquipmentOperationResult()

    val isSuccess: Boolean get() = this is Success
}
