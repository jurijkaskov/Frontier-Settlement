package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CraftConfig
import com.example.domain.model.*
import com.example.ui.theme.*
import kotlin.math.max
import kotlin.math.min

/**
 * Tactical Craft Quantity Dialog for precise manufacturing batches.
 * Displays real-time material requirements, warehouse storage delta, item attributes,
 * and handles atomic validation before triggering manufacturing.
 */
@Composable
fun CraftQuantityDialog(
    recipe: CraftRecipe,
    gameState: GameState,
    workshopLevel: Int,
    onConfirm: (quantity: Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val res = gameState.resources
    val maxCraftBatches = CraftConfig.calculateMaxCraftCount(
        recipe = recipe,
        resources = res,
        currentInventory = gameState.inventoryItems
    )

    var craftBatches by remember {
        mutableStateOf(if (maxCraftBatches > 0) 1 else 0)
    }

    val totalItemsYield = craftBatches * recipe.outputQuantity
    val totalVolumeYield = craftBatches * recipe.totalOutputVolume

    // Calculate physical consumed volume
    val totalConsumedPhysicalVolume = recipe.requiredResources.entries.sumOf { (type, amount) ->
        if (type.isPhysical) amount * type.unitSize * craftBatches else 0
    }
    val netStorageDelta = totalVolumeYield - totalConsumedPhysicalVolume

    val resultingStorageVolume = (gameState.totalWarehouseOccupiedVolume + netStorageDelta).coerceAtLeast(0)
    val isStorageExceeded = resultingStorageVolume > res.warehouseMaxCapacity

    // Validation checks
    val isZero = craftBatches <= 0
    val isAboveMax = craftBatches > maxCraftBatches
    val canConfirm = !isZero && !isAboveMax && !isStorageExceeded

    val item = recipe.outputItem
    val rarityColor = item.rarity.color

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = FrontierDarkSurface,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.6f)),
            modifier = modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp)
                .testTag("dialog_craft_quantity")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(rarityColor.copy(alpha = 0.2f))
                                .border(1.dp, rarityColor, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val iconVector = when (recipe.iconKey) {
                                "medkit" -> Icons.Default.MedicalServices
                                "toolkit" -> Icons.Default.Handyman
                                "ration" -> Icons.Default.LunchDining
                                "fuel_canister" -> Icons.Default.LocalGasStation
                                "armor_plate" -> Icons.Default.Shield
                                "ammo_crate" -> Icons.Default.Inventory
                                "scanner" -> Icons.Default.Sensors
                                "microchip" -> Icons.Default.Memory
                                else -> Icons.Default.Build
                            }
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = rarityColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = recipe.nameRu,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = "${item.category.titleRu} • ${item.rarity.titleRu}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = rarityColor,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_craft_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = TextMuted)
                    }
                }

                // Batch Stepper Control
                Surface(
                    color = FrontierCardBg,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "КОЛИЧЕСТВО ПАРТИЙ СБОРКИ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                        )

                        // Main Stepper Row (-10, -1, [Count], +1, +10)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // -5
                            IconButton(
                                onClick = { craftBatches = max(0, craftBatches - 5) },
                                enabled = craftBatches > 0,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(FrontierDarkSurface)
                                    .testTag("btn_craft_minus_5")
                            ) {
                                Text("-5", color = if (craftBatches > 0) TextWhite else TextMuted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            // -1
                            IconButton(
                                onClick = { craftBatches = max(0, craftBatches - 1) },
                                enabled = craftBatches > 0,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(FrontierDarkSurface)
                                    .testTag("btn_craft_minus_1")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Уменьшить", tint = if (craftBatches > 0) TextWhite else TextMuted)
                            }

                            // Current Quantity Display
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$craftBatches",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (canConfirm) TechCyan else DangerCrimson,
                                        fontSize = 28.sp
                                    ),
                                    modifier = Modifier.testTag("text_craft_batches_count")
                                )
                                Text(
                                    text = "Итого: $totalItemsYield шт.",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SafeEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            // +1
                            IconButton(
                                onClick = { craftBatches = min(maxCraftBatches, craftBatches + 1) },
                                enabled = craftBatches < maxCraftBatches,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(FrontierDarkSurface)
                                    .testTag("btn_craft_plus_1")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Увеличить", tint = if (craftBatches < maxCraftBatches) TextWhite else TextMuted)
                            }

                            // +5
                            IconButton(
                                onClick = { craftBatches = min(maxCraftBatches, craftBatches + 5) },
                                enabled = craftBatches < maxCraftBatches,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(FrontierDarkSurface)
                                    .testTag("btn_craft_plus_5")
                            ) {
                                Text("+5", color = if (craftBatches < maxCraftBatches) TextWhite else TextMuted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        // Quick Presets: 1, 5, MAX
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { craftBatches = if (maxCraftBatches >= 1) 1 else 0 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("1 шт.", fontSize = 11.sp, color = TextWhite)
                            }

                            OutlinedButton(
                                onClick = { craftBatches = if (maxCraftBatches >= 5) 5 else maxCraftBatches },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("5 шт.", fontSize = 11.sp, color = TextWhite)
                            }

                            Button(
                                onClick = { craftBatches = maxCraftBatches },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(34.dp)
                                    .testTag("btn_craft_max"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TechCyan.copy(alpha = 0.3f),
                                    contentColor = TechCyan
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("МАКС ($maxCraftBatches)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Dynamic Material Requirements Breakdown
                Surface(
                    color = FrontierDarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "РАСХОД МАТЕРИАЛОВ И СЫРЬЯ:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )

                        recipe.requiredResources.forEach { (type, reqPerBatch) ->
                            val totalReq = reqPerBatch * craftBatches
                            val playerStock = res[type]
                            val isEnough = playerStock >= totalReq

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = type.nameRu,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextWhite,
                                        fontSize = 12.sp
                                    )
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "$totalReq ед.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isEnough) WarningAmber else DangerCrimson,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    )
                                    Text(
                                        text = "(В наличии: $playerStock)",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Warehouse Storage Capacity Impact
                Surface(
                    color = FrontierDarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = if (isStorageExceeded) DangerCrimson else StoragePurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Загрузка склада:",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextWhite, fontSize = 12.sp)
                            )
                        }

                        Text(
                            text = "$resultingStorageVolume / ${res.warehouseMaxCapacity} ед. (${if (netStorageDelta >= 0) "+$netStorageDelta" else "$netStorageDelta"})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isStorageExceeded) DangerCrimson else SafeEmerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                // Lore / Attributes Accordion
                if (recipe.loreRu.isNotBlank()) {
                    Text(
                        text = recipe.loreRu,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    )
                }

                // Action Confirm Button
                Button(
                    onClick = {
                        if (canConfirm) {
                            onConfirm(craftBatches)
                            onDismiss()
                        }
                    },
                    enabled = canConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TechCyan,
                        contentColor = FrontierDarkBackground,
                        disabledContainerColor = FrontierDarkSurface,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_confirm_craft_action")
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isStorageExceeded) "Склад переполнен"
                        else if (maxCraftBatches <= 0) "Недостаточно ресурсов"
                        else "Изготовить ($totalItemsYield шт.)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }
}
