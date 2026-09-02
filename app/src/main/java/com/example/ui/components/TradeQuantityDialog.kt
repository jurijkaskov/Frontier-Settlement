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
import com.example.data.TradeConfig
import com.example.domain.model.*
import com.example.ui.theme.*
import kotlin.math.max
import kotlin.math.min

/**
 * Tactical Trade Quantity Selection Dialog.
 * Allows the player to fine-tune trade volumes (Buy or Sell) with real-time feedback
 * on credits cost/revenue, warehouse capacity changes, and limit constraints.
 */
@Composable
fun TradeQuantityDialog(
    offer: TradeOffer,
    mode: TradeMode,
    gameState: GameState,
    tradingPostLevel: Int,
    onConfirm: (quantity: Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val res = gameState.resources
    val discountPercent = tradingPostLevel * 5
    val unitPrice = if (mode == TradeMode.BUY) {
        offer.getEffectiveBuyPrice(discountPercent)
    } else {
        offer.getEffectiveSellPrice(discountPercent)
    }

    val maxQuantity = if (mode == TradeMode.BUY) {
        TradeConfig.calculateMaxBuyQuantity(
            playerCredits = res.money,
            unitBuyPrice = unitPrice,
            availableWarehouseCapacity = res.availableCapacity,
            unitSize = offer.unitSize,
            merchantStock = offer.merchantStock
        )
    } else {
        TradeConfig.calculateMaxSellQuantity(res[offer.resourceType])
    }

    // Default to 1 unit or max if max < 1
    var selectedQuantity by remember { mutableStateOf(if (maxQuantity > 0) min(10, maxQuantity) else 0) }

    // Dynamic calculations
    val totalCredits = selectedQuantity * unitPrice
    val resultingCredits = if (mode == TradeMode.BUY) {
        res.money - totalCredits
    } else {
        res.money + totalCredits
    }

    val spaceNeeded = selectedQuantity * offer.unitSize
    val resultingStorageVolume = if (mode == TradeMode.BUY) {
        res.totalStoredVolume + spaceNeeded
    } else {
        (res.totalStoredVolume - spaceNeeded).coerceAtLeast(0)
    }

    val themeColor = if (mode == TradeMode.BUY) SafeEmerald else CreditsYellow

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = FrontierDarkSurface,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, themeColor.copy(alpha = 0.6f)),
            modifier = modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp)
                .testTag("dialog_trade_quantity")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(themeColor.copy(alpha = 0.18f))
                                .border(1.dp, themeColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getResourceIcon(offer.resourceType),
                                contentDescription = offer.nameRu,
                                tint = themeColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "${mode.actionVerbRu}: ${offer.nameRu}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            )
                            Text(
                                text = if (mode == TradeMode.BUY) "Закупка у караванщика" else "Сбыт излишков на рынок",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_close_trade_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price and Trade Post Bonus info
                Surface(
                    color = FrontierDarkSurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Цена за единицу:",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = CreditsYellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$unitPrice кр. / ед.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextWhite)
                                )
                            }
                        }

                        if (discountPercent > 0) {
                            Surface(
                                color = SafeEmerald.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "Бонус поста: +$discountPercent%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SafeEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity Selector Controls
                Text(
                    text = "Выберите количество для обмена:",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextMuted, fontSize = 12.sp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Decrement buttons
                    StepButton(
                        text = "-10",
                        enabled = selectedQuantity > 10,
                        onClick = { selectedQuantity = max(1, selectedQuantity - 10) },
                        testTag = "btn_trade_minus_10"
                    )

                    StepButton(
                        text = "-1",
                        enabled = selectedQuantity > 1,
                        onClick = { selectedQuantity = max(1, selectedQuantity - 1) },
                        testTag = "btn_trade_minus_1"
                    )

                    // Quantity Display Box
                    Surface(
                        color = FrontierDarkSurfaceHighlight,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, themeColor),
                        modifier = Modifier
                            .width(80.dp)
                            .height(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$selectedQuantity",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextWhite
                                ),
                                modifier = Modifier.testTag("text_selected_trade_quantity")
                            )
                        }
                    }

                    // Increment buttons
                    StepButton(
                        text = "+1",
                        enabled = selectedQuantity < maxQuantity,
                        onClick = { selectedQuantity = min(maxQuantity, selectedQuantity + 1) },
                        testTag = "btn_trade_plus_1"
                    )

                    StepButton(
                        text = "+10",
                        enabled = selectedQuantity + 10 <= maxQuantity,
                        onClick = { selectedQuantity = min(maxQuantity, selectedQuantity + 10) },
                        testTag = "btn_trade_plus_10"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Preset Pack Buttons Row (5, 10, 25, MAX)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(5, 10, 25).forEach { pack ->
                        Surface(
                            color = if (selectedQuantity == pack) themeColor.copy(alpha = 0.25f) else FrontierDarkSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedQuantity == pack) themeColor else FrontierBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = pack <= maxQuantity) {
                                    selectedQuantity = pack
                                }
                                .testTag("btn_pack_$pack")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$pack",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (pack <= maxQuantity) TextWhite else TextSubtle
                                    )
                                )
                            }
                        }
                    }

                    // MAX Button
                    Surface(
                        color = if (selectedQuantity == maxQuantity && maxQuantity > 0) WarningAmber.copy(alpha = 0.25f) else FrontierDarkSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selectedQuantity == maxQuantity && maxQuantity > 0) WarningAmber else FrontierBorder
                        ),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = maxQuantity > 0) {
                                selectedQuantity = maxQuantity
                            }
                            .testTag("btn_pack_max")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "МАКС ($maxQuantity)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (maxQuantity > 0) WarningAmber else TextSubtle,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Financial & Storage Impact Summary Card
                Surface(
                    color = FrontierDarkSurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Total Credits Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (mode == TradeMode.BUY) "Итоговая стоимость:" else "Итоговая выручка:",
                                style = MaterialTheme.typography.labelMedium.copy(color = TextMuted)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = CreditsYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (mode == TradeMode.BUY) "$totalCredits кр." else "+$totalCredits кр.",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (mode == TradeMode.BUY) DangerCrimson else SafeEmerald
                                    ),
                                    modifier = Modifier.testTag("text_total_credits_dialog")
                                )
                            }
                        }

                        Divider(color = FrontierBorder, thickness = 0.5.dp)

                        // Credits Balance Before -> After
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Баланс кредитов:",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp)
                            )
                            Text(
                                text = "${res.money} кр. › $resultingCredits кр.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // Warehouse Impact
                        if (offer.resourceType.isPhysical) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Загрузка склада:",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp)
                                )
                                Text(
                                    text = "${res.totalStoredVolume} / ${res.warehouseMaxCapacity} › $resultingStorageVolume / ${res.warehouseMaxCapacity}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (resultingStorageVolume > res.warehouseMaxCapacity) DangerCrimson else StoragePurple,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Merchant stock / Player stock
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (mode == TradeMode.BUY) "Остаток у торговца:" else "Остаток на складе:",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp)
                            )
                            val remaining = if (mode == TradeMode.BUY) {
                                (offer.merchantStock - selectedQuantity).coerceAtLeast(0)
                            } else {
                                (res[offer.resourceType] - selectedQuantity).coerceAtLeast(0)
                            }
                            Text(
                                text = "$remaining ед.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Constraint Explanation (if maxQuantity is 0 or restricted)
                if (maxQuantity == 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val constraintMessage = when {
                        mode == TradeMode.BUY && offer.merchantStock <= 0 -> "У торговца закончился этот товар."
                        mode == TradeMode.BUY && res.money < unitPrice -> "Недостаточно кредитов для покупки хотя бы 1 ед."
                        mode == TradeMode.BUY && res.availableCapacity < offer.unitSize -> "Склад полностью заполнен! Нет места."
                        mode == TradeMode.SELL && res[offer.resourceType] <= 0 -> "У вас нет этого ресурса для продажи."
                        else -> "Сделка временно недоступна."
                    }

                    Surface(
                        color = DangerCrimson.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerCrimson.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = DangerCrimson, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = constraintMessage,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DangerCrimson,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_cancel_trade_dialog")
                    ) {
                        Text(
                            text = "Отмена",
                            style = MaterialTheme.typography.labelMedium.copy(color = TextMuted)
                        )
                    }

                    Button(
                        onClick = {
                            if (selectedQuantity > 0) {
                                onConfirm(selectedQuantity)
                                onDismiss()
                            }
                        },
                        enabled = selectedQuantity > 0 && selectedQuantity <= maxQuantity,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColor,
                            disabledContainerColor = FrontierDarkSurfaceHighlight
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("btn_confirm_trade_dialog")
                    ) {
                        Text(
                            text = "${mode.actionVerbRu} ($totalCredits кр.)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (mode == TradeMode.BUY) FrontierOnPrimary else FrontierDarkBackground
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = if (enabled) FrontierDarkSurfaceElevated else FrontierDarkSurfaceElevated.copy(alpha = 0.4f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (enabled) FrontierBorderLight else FrontierBorder.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick() }
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) TextWhite else TextSubtle
                )
            )
        }
    }
}

/**
 * Returns corresponding icon for a ResourceType.
 */
private fun getResourceIcon(type: ResourceType) = when (type) {
    ResourceType.FOOD -> Icons.Default.Restaurant
    ResourceType.WATER -> Icons.Default.WaterDrop
    ResourceType.FUEL -> Icons.Default.LocalGasStation
    ResourceType.MATERIALS -> Icons.Default.Build
    ResourceType.MEDICINE -> Icons.Default.MedicalServices
    ResourceType.AMMO -> Icons.Default.Shield
    ResourceType.COMPONENTS -> Icons.Default.Memory
    ResourceType.RARE_ALLOY -> Icons.Default.Diamond
    ResourceType.MONEY -> Icons.Default.MonetizationOn
}
