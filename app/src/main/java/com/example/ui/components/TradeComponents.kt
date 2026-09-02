package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TradeConfig
import com.example.domain.model.*
import com.example.ui.theme.*

/**
 * Merchant Header & Caravan Status Card.
 */
@Composable
fun MerchantHeaderCard(
    merchant: MerchantProfile,
    credits: Int,
    usedStorage: Int,
    maxStorage: Int,
    tradingPostLevel: Int,
    onRestockClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val discountPercent = tradingPostLevel * 5

    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = CreditsYellow.copy(alpha = 0.6f),
        modifier = modifier.testTag("card_merchant_header")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Row: Avatar & Title & Credits Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CreditsYellow.copy(alpha = 0.2f))
                            .border(1.5.dp, CreditsYellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = merchant.name,
                            tint = CreditsYellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = merchant.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            )
                            if (tradingPostLevel > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = SafeEmerald.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, SafeEmerald)
                                ) {
                                    Text(
                                        text = "Пост Ур. $tradingPostLevel",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SafeEmerald,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = merchant.titleRu,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Credits Balance Pill
                Surface(
                    color = FrontierDarkSurfaceHighlight,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CreditsYellow)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("chip_market_credits")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = CreditsYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$credits кр.",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Merchant Speech Bubble
            Surface(
                color = FrontierDarkBackground.copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💬",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = merchant.greetingRu,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Warehouse Space Indicator & Perks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = StoragePurple,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Склад: $usedStorage / $maxStorage (${maxStorage - usedStorage} свободно)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = StoragePurple,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )
                }

                if (discountPercent > 0) {
                    Text(
                        text = "Скидка каравана: -$discountPercent%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SafeEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Trade Mode Segmented Control (Buy vs Sell).
 */
@Composable
fun TradeModeSegmentedControl(
    selectedMode: TradeMode,
    onModeSelected: (TradeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("control_trade_mode")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Buy Mode Tab
            val isBuy = selectedMode == TradeMode.BUY
            Surface(
                color = if (isBuy) SafeEmerald else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onModeSelected(TradeMode.BUY) }
                    .testTag("tab_trade_buy")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = if (isBuy) FrontierOnPrimary else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Купить припасы",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isBuy) FrontierOnPrimary else TextMuted
                        )
                    )
                }
            }

            // Sell Mode Tab
            val isSell = selectedMode == TradeMode.SELL
            Surface(
                color = if (isSell) CreditsYellow else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onModeSelected(TradeMode.SELL) }
                    .testTag("tab_trade_sell")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sell,
                        contentDescription = null,
                        tint = if (isSell) FrontierDarkBackground else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Продать излишки",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSell) FrontierDarkBackground else TextMuted
                        )
                    )
                }
            }
        }
    }
}

/**
 * Category Filter Chips Row.
 */
@Composable
fun TradeCategoryFilterRow(
    selectedCategory: WarehouseFilterCategory,
    onCategorySelected: (WarehouseFilterCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(WarehouseFilterCategory.entries) { cat ->
            val isSelected = cat == selectedCategory
            Surface(
                color = if (isSelected) FrontierDarkSurfaceHighlight else FrontierDarkSurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) SafeEmerald else FrontierBorder
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onCategorySelected(cat) }
                    .testTag("chip_trade_category_${cat.id}")
            ) {
                Text(
                    text = cat.titleRu,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) TextWhite else TextMuted
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * Individual Trade Offer Item Card.
 */
@Composable
fun TradeOfferCard(
    offer: TradeOffer,
    mode: TradeMode,
    gameState: GameState,
    tradingPostLevel: Int,
    onQuickTrade: (quantity: Int) -> Unit,
    onOpenDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val res = gameState.resources
    val discountPercent = tradingPostLevel * 5
    val unitPrice = if (mode == TradeMode.BUY) {
        offer.getEffectiveBuyPrice(discountPercent)
    } else {
        offer.getEffectiveSellPrice(discountPercent)
    }

    val isUnlocked = offer.isAvailable(tradingPostLevel, gameState.settlement.level)
    val playerStock = res[offer.resourceType]
    val maxTradeQuantity = if (mode == TradeMode.BUY) {
        TradeConfig.calculateMaxBuyQuantity(
            playerCredits = res.money,
            unitBuyPrice = unitPrice,
            availableWarehouseCapacity = res.availableCapacity,
            unitSize = offer.unitSize,
            merchantStock = offer.merchantStock
        )
    } else {
        TradeConfig.calculateMaxSellQuantity(playerStock)
    }

    val resourceColor = getResourceColor(offer.resourceType)
    val actionColor = if (mode == TradeMode.BUY) SafeEmerald else CreditsYellow

    GameCard(
        backgroundColor = if (isUnlocked) FrontierDarkSurfaceElevated else FrontierDarkSurfaceElevated.copy(alpha = 0.5f),
        borderColor = if (isUnlocked) FrontierBorder else FrontierBorder.copy(alpha = 0.4f),
        modifier = modifier.testTag("card_trade_offer_${offer.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Resource Icon + Name + Unit Price Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(resourceColor.copy(alpha = 0.15f))
                            .border(1.dp, resourceColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getResourceIcon(offer.resourceType),
                            contentDescription = offer.nameRu,
                            tint = resourceColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = offer.nameRu,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnlocked) TextWhite else TextMuted
                                )
                            )
                            if (offer.rarity != ItemRarity.COMMON) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = offer.rarity.color.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, offer.rarity.color)
                                ) {
                                    Text(
                                        text = offer.rarity.titleRu,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = offer.rarity.color,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        // Stock counters
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "У торговца: ${offer.merchantStock} ед.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (offer.merchantStock > 0) TextMuted else DangerCrimson,
                                    fontSize = 11.sp
                                )
                            )
                            Text(text = "•", color = TextSubtle, fontSize = 10.sp)
                            Text(
                                text = "У вас: $playerStock ед.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (playerStock > 0) SafeEmerald else TextSubtle,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Price Tag Pill
                Surface(
                    color = FrontierDarkSurfaceHighlight,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CreditsYellow.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = CreditsYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$unitPrice кр.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                    }
                }
            }

            if (offer.descriptionRu.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = offer.descriptionRu,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 10.5.sp,
                        lineHeight = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons or Locked Notice
            if (!isUnlocked) {
                Surface(
                    color = FrontierDarkBackground,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Требуется «Торговый Пост» Ур. ${offer.minTradingPostLevel} (сейчас: Ур. $tradingPostLevel)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = WarningAmber,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Quick +1 / -1 Action Button
                    val quick1Cost = 1 * unitPrice
                    Button(
                        onClick = { onQuickTrade(1) },
                        enabled = maxTradeQuantity >= 1,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = actionColor,
                            disabledContainerColor = FrontierDarkSurfaceHighlight
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("btn_quick_trade_1_${offer.id}")
                    ) {
                        Text(
                            text = if (mode == TradeMode.BUY) "+1 ($quick1Cost кр)" else "-1 (+$quick1Cost кр)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (mode == TradeMode.BUY) FrontierOnPrimary else FrontierDarkBackground,
                                fontSize = 11.sp
                            )
                        )
                    }

                    // Quick +10 / -10 Action Button
                    val packSize = 10
                    val quickPackCost = packSize * unitPrice
                    val canDoPack = maxTradeQuantity >= packSize
                    OutlinedButton(
                        onClick = { onQuickTrade(packSize) },
                        enabled = canDoPack,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (canDoPack) actionColor else FrontierBorder
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(38.dp)
                            .testTag("btn_quick_trade_10_${offer.id}")
                    ) {
                        Text(
                            text = if (mode == TradeMode.BUY) "+$packSize ($quickPackCost кр)" else "-$packSize (+$quickPackCost кр)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (canDoPack) TextWhite else TextSubtle,
                                fontSize = 11.sp
                            )
                        )
                    }

                    // Detailed Custom Volume Dialog Button
                    Surface(
                        color = FrontierDarkSurfaceHighlight,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorderLight),
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenDialog() }
                            .testTag("btn_open_trade_dialog_${offer.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Выбрать количество",
                                tint = TextWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated Transaction Feedback Banner.
 */
@Composable
fun TradeResultBanner(
    result: TradeTransactionResult?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = result != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        if (result != null) {
            val isSuccess = result.isSuccess
            val bannerColor = if (isSuccess) SafeEmerald else DangerCrimson

            Surface(
                color = bannerColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, bannerColor.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("banner_trade_result")
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
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = bannerColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = result.message,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite,
                                fontSize = 12.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getResourceColor(type: ResourceType) = when (type) {
    ResourceType.FOOD -> FoodGreen
    ResourceType.WATER -> WaterCyan
    ResourceType.FUEL -> FuelAmber
    ResourceType.MATERIALS -> MaterialsOrange
    ResourceType.MEDICINE -> SafeEmerald
    ResourceType.AMMO -> MilitaryRed
    ResourceType.COMPONENTS -> TechCyan
    ResourceType.RARE_ALLOY -> StoragePurple
    ResourceType.MONEY -> CreditsYellow
}

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
