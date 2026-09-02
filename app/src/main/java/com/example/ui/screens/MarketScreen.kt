package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TradeConfig
import com.example.domain.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

/**
 * Atmospheric Wasteland Market & Trading Post Screen (Point 6).
 *
 * Features:
 * - Dual Mode: Buy (Supplies procurement) & Sell (Surplus barter)
 * - Category filter tabs (All, Primary, Materials, Items, Valuables)
 * - Atomic transactions with instant visual feedback and error prevention
 * - Tactical Trade Quantity Dialog with step buttons and MAX calculators
 * - Tied to Settlement Trading Post building levels for discounts & item unlocks
 */
@Composable
fun MarketScreen(
    gameState: GameState,
    onTrade: (offerId: String, quantity: Int, mode: TradeMode) -> Unit,
    tradeResult: TradeTransactionResult? = null,
    onDismissTradeResult: () -> Unit = {},
    onRestockMarket: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val res = gameState.resources
    val sett = gameState.settlement
    val merchantState = gameState.merchantState

    val tradingPostBuilding = sett.buildings.find {
        it.type == BuildingType.TRADING_POST && it.isConstructed
    }
    val tradingPostLevel = tradingPostBuilding?.level ?: 0

    // Local UI State
    var selectedMode by remember { mutableStateOf(TradeMode.BUY) }
    var selectedCategory by remember { mutableStateOf(WarehouseFilterCategory.ALL) }
    var dialogOffer by remember { mutableStateOf<TradeOffer?>(null) }

    val rawOffers = if (merchantState.offers.isNotEmpty()) {
        merchantState.offers
    } else {
        TradeConfig.createDefaultTradeOffers()
    }

    val filteredOffers = rawOffers.filter { offer ->
        if (selectedCategory == WarehouseFilterCategory.ALL) true
        else offer.category == selectedCategory
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp)
        ) {
            // Merchant & Caravan Header Card
            item {
                MerchantHeaderCard(
                    merchant = merchantState.merchant,
                    credits = res.money,
                    usedStorage = res.totalStoredVolume,
                    maxStorage = res.warehouseMaxCapacity,
                    tradingPostLevel = tradingPostLevel,
                    onRestockClick = onRestockMarket
                )
            }

            // Transaction Result Feedback Banner
            if (tradeResult != null) {
                item {
                    TradeResultBanner(
                        result = tradeResult,
                        onDismiss = onDismissTradeResult
                    )
                }
            }

            // Mode Selector: Buy vs Sell
            item {
                TradeModeSegmentedControl(
                    selectedMode = selectedMode,
                    onModeSelected = { selectedMode = it }
                )
            }

            // Category Filter Row
            item {
                TradeCategoryFilterRow(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = if (selectedMode == TradeMode.BUY) "Каталог товаров торговца" else "Ваши ресурсы для сбыта",
                        accentColor = if (selectedMode == TradeMode.BUY) SafeEmerald else CreditsYellow
                    )

                    Text(
                        text = "Товаров: ${filteredOffers.size}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Offers List
            if (filteredOffers.isEmpty()) {
                item {
                    Surface(
                        color = FrontierDarkSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ProductionQuantityLimits,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "В этой категории нет доступных товаров.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            } else {
                items(filteredOffers, key = { it.id }) { offer ->
                    TradeOfferCard(
                        offer = offer,
                        mode = selectedMode,
                        gameState = gameState,
                        tradingPostLevel = tradingPostLevel,
                        onQuickTrade = { qty ->
                            onTrade(offer.id, qty, selectedMode)
                        },
                        onOpenDialog = {
                            dialogOffer = offer
                        }
                    )
                }
            }
        }

        // Tactical Trade Quantity Dialog
        dialogOffer?.let { offer ->
            TradeQuantityDialog(
                offer = offer,
                mode = selectedMode,
                gameState = gameState,
                tradingPostLevel = tradingPostLevel,
                onConfirm = { quantity ->
                    onTrade(offer.id, quantity, selectedMode)
                },
                onDismiss = { dialogOffer = null }
            )
        }
    }
}
