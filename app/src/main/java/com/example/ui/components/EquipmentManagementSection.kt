package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.*
import com.example.domain.service.EquipmentService
import com.example.domain.service.StatComparisonItem
import com.example.ui.theme.*

/**
 * Interactive loadout and equipment management UI component for a character.
 */
@Composable
fun EquipmentManagementSection(
    character: Character,
    warehouseItems: List<WarehouseItem>,
    allCharacters: List<Character>,
    onEquipItem: (characterId: String, slot: EquipmentSlotType, itemId: String) -> Unit,
    onUnequipItem: (characterId: String, slot: EquipmentSlotType) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSlotForPicker by remember { mutableStateOf<EquipmentSlotType?>(null) }
    var itemToPreview by remember { mutableStateOf<WarehouseItem?>(null) }

    val equippedItemsMap = character.getEquippedItemsMap(warehouseItems)
    val totalGearWeight = character.getTotalEquippedWeightKg(warehouseItems)
    val effectiveCarryCapacity = character.getEffectiveCarryCapacityKg(warehouseItems)

    Surface(
        color = FrontierDarkBackground,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Экипировка и снаряжение",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                    Text(
                        text = "Слоты снаряжения бойца",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Surface(
                    color = FrontierCardElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, FrontierBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Вес: ${"%.1f".format(totalGearWeight)} кг / Лимит: ${effectiveCarryCapacity} кг",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (totalGearWeight > effectiveCarryCapacity) CriticalRed else WarningAmber
                        )
                    }
                }
            }

            HorizontalDivider(color = FrontierBorder, thickness = 0.5.dp)

            // Slot List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EquipmentSlotType.entries.forEach { slotType: EquipmentSlotType ->
                    val equippedItem = equippedItemsMap[slotType]
                    EquipmentSlotRow(
                        slotType = slotType,
                        equippedItem = equippedItem,
                        onOpenPicker = { selectedSlotForPicker = slotType },
                        onUnequip = { onUnequipItem(character.id, slotType) }
                    )
                }
            }
        }
    }

    // Equipment Picker Modal Dialog
    selectedSlotForPicker?.let { slot ->
        EquipmentItemPickerDialog(
            slot = slot,
            character = character,
            warehouseItems = warehouseItems,
            allCharacters = allCharacters,
            onSelectItem = { itemId ->
                onEquipItem(character.id, slot, itemId)
                selectedSlotForPicker = null
            },
            onDismiss = { selectedSlotForPicker = null }
        )
    }
}

@Composable
private fun EquipmentSlotRow(
    slotType: EquipmentSlotType,
    equippedItem: WarehouseItem?,
    onOpenPicker: () -> Unit,
    onUnequip: () -> Unit
) {
    val slotIcon = when (slotType) {
        EquipmentSlotType.OUTFIT -> Icons.Default.Security
        EquipmentSlotType.FOOTWEAR -> Icons.Default.DirectionsWalk
        EquipmentSlotType.BACKPACK -> Icons.Default.Backpack
        EquipmentSlotType.TOOL -> Icons.Default.Handyman
        EquipmentSlotType.SPECIAL -> Icons.Default.AutoAwesome
    }

    Surface(
        color = if (equippedItem != null) FrontierDarkSurface else FrontierDarkSurface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (equippedItem != null) equippedItem.rarity.color.copy(alpha = 0.5f) else FrontierBorder.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Slot Icon Box
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (equippedItem != null) equippedItem.rarity.color.copy(alpha = 0.15f)
                            else FrontierCardElevated
                        )
                        .border(
                            1.dp,
                            if (equippedItem != null) equippedItem.rarity.color.copy(alpha = 0.6f)
                            else FrontierBorder,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = slotIcon,
                        contentDescription = slotType.titleRu,
                        tint = if (equippedItem != null) equippedItem.rarity.color else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Slot Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = slotType.titleRu,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = TextMuted
                    )

                    if (equippedItem != null) {
                        Text(
                            text = equippedItem.name,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = equippedItem.rarity.color,
                            maxLines = 1
                        )
                        Text(
                            text = equippedItem.equipmentBonus.toShortSummary(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = SafeEmerald,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = "Слот пуст",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (equippedItem != null) {
                    IconButton(
                        onClick = onUnequip,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_unequip_${slotType.name.lowercase()}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Снять",
                            tint = CriticalRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenPicker,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("btn_change_slot_${slotType.name.lowercase()}")
                    ) {
                        Text(
                            text = "Сменить",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AccentCyan
                        )
                    }
                } else {
                    Button(
                        onClick = onOpenPicker,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentCyan.copy(alpha = 0.2f),
                            contentColor = AccentCyan
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("btn_equip_slot_${slotType.name.lowercase()}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Надеть",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Picker dialog displaying available items in warehouse for the specified equipment slot,
 * along with stat comparison against currently equipped item.
 */
@Composable
fun EquipmentItemPickerDialog(
    slot: EquipmentSlotType,
    character: Character,
    warehouseItems: List<WarehouseItem>,
    allCharacters: List<Character>,
    onSelectItem: (itemId: String) -> Unit,
    onDismiss: () -> Unit
) {
    val currentEquippedItem = character.equipment.getItemId(slot)?.let { id ->
        warehouseItems.find { it.id == id }
    }

    // Filter all equipable items in warehouse for this slot
    val compatibleItems = warehouseItems.filter { it.isEquipable && it.equipSlot == slot }

    var selectedPreviewItem by remember { mutableStateOf<WarehouseItem?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .testTag("dialog_equipment_picker"),
            shape = RoundedCornerShape(18.dp),
            color = FrontierDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Слот: ${slot.titleRu}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite
                        )
                        Text(
                            text = "Выберите снаряжение со склада аванпоста",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = FrontierBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))

                if (compatibleItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "На складе нет предметов для этого слота",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                            Text(
                                text = "Создайте снаряжение в Мастерской или найдите в экспедициях",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(compatibleItems) { item ->
                            val isCurrentlyEquippedByThisChar = currentEquippedItem?.id == item.id
                            val equipperInfo = EquipmentService.getCharacterEquippingItem(item.id, allCharacters)
                            val isEquippedByOther = equipperInfo != null && equipperInfo.first.id != character.id

                            val comparisons = EquipmentService.compareItems(currentEquippedItem, item)

                            EquipmentPickerItemCard(
                                item = item,
                                isCurrentlyEquipped = isCurrentlyEquippedByThisChar,
                                equippedByOtherName = if (isEquippedByOther) equipperInfo?.first?.name else null,
                                comparisons = comparisons,
                                onSelect = { onSelectItem(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EquipmentPickerItemCard(
    item: WarehouseItem,
    isCurrentlyEquipped: Boolean,
    equippedByOtherName: String?,
    comparisons: List<StatComparisonItem>,
    onSelect: () -> Unit
) {
    Surface(
        color = if (isCurrentlyEquipped) FrontierCardElevated else FrontierDarkBackground,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCurrentlyEquipped) SafeEmerald else item.rarity.color.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = item.rarity.color
                        )
                        Surface(
                            color = item.rarity.color.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = item.rarity.titleRu,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = item.rarity.color,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        maxLines = 2
                    )
                }
            }

            // Stat diff chips
            if (comparisons.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    comparisons.forEach { cmp ->
                        val chipColor = when {
                            cmp.isPositive -> SafeEmerald
                            cmp.isNegative -> CriticalRed
                            else -> TextMuted
                        }
                        val sign = if (cmp.difference > 0) "+" else ""

                        Surface(
                            color = chipColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, chipColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${cmp.statName}: $sign${cmp.difference}${cmp.unit}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = chipColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Bottom action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Вес: ${"%.1f".format(item.weightKg)} кг",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )

                    if (equippedByOtherName != null) {
                        Surface(
                            color = WarningAmber.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Надето: $equippedByOtherName",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = WarningAmber,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (isCurrentlyEquipped) {
                    Surface(
                        color = SafeEmerald.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "✓ Надето сейчас",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SafeEmerald,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onSelect,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (equippedByOtherName != null) WarningAmber else AccentCyan,
                            contentColor = FrontierDarkBackground
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("btn_select_equip_${item.id}")
                    ) {
                        Text(
                            text = if (equippedByOtherName != null) "Забрать и надеть" else "Экипировать",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
