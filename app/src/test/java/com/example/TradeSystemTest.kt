package com.example

import com.example.data.InitialGameData
import com.example.data.TradeConfig
import com.example.domain.model.*
import com.example.viewmodel.GameViewModel
import org.junit.Assert.*
import org.junit.Test

/**
 * Comprehensive Unit Tests for Point 6 (Shop & Trade System).
 *
 * Verifies:
 * - Safe atomic purchase operations with credit and warehouse checks
 * - Safe atomic sell operations with stock and revenue updates
 * - Invariant preservation (zero partial mutations on failure)
 * - Maximum buy/sell calculation algorithms
 * - Trading Post level unlock constraints and discount calculations
 * - Restock mechanics
 */
class TradeSystemTest {

    @Test
    fun testTradeConfig_DefaultOffersInitialization() {
        val offers = TradeConfig.createDefaultTradeOffers()
        assertTrue(offers.isNotEmpty())
        assertTrue(offers.size >= 8)

        val foodOffer = offers.find { it.resourceType == ResourceType.FOOD }
        assertNotNull(foodOffer)
        assertEquals(3, foodOffer!!.buyPricePerUnit)
        assertEquals(2, foodOffer.sellPricePerUnit)
        assertTrue(foodOffer.merchantStock > 0)
    }

    @Test
    fun testCalculateMaxBuyQuantity_RespectsCreditsLimitation() {
        // Player has 10 credits, unit price is 3. Max from credits = 3. Storage has 100 space, stock is 50.
        val maxBuy = TradeConfig.calculateMaxBuyQuantity(
            playerCredits = 10,
            unitBuyPrice = 3,
            availableWarehouseCapacity = 100,
            unitSize = 1,
            merchantStock = 50
        )
        assertEquals(3, maxBuy)
    }

    @Test
    fun testCalculateMaxBuyQuantity_RespectsStorageLimitation() {
        // Player has 1000 credits, unit price is 2. Storage has only 5 free slots, unitSize is 1. Stock is 50.
        val maxBuy = TradeConfig.calculateMaxBuyQuantity(
            playerCredits = 1000,
            unitBuyPrice = 2,
            availableWarehouseCapacity = 5,
            unitSize = 1,
            merchantStock = 50
        )
        assertEquals(5, maxBuy)
    }

    @Test
    fun testCalculateMaxBuyQuantity_RespectsMerchantStockLimitation() {
        // Player has 1000 credits, storage 1000, but merchant only has 4 items in stock.
        val maxBuy = TradeConfig.calculateMaxBuyQuantity(
            playerCredits = 1000,
            unitBuyPrice = 2,
            availableWarehouseCapacity = 1000,
            unitSize = 1,
            merchantStock = 4
        )
        assertEquals(4, maxBuy)
    }

    @Test
    fun testCalculateMaxSellQuantity_MatchesPlayerStock() {
        val maxSell = TradeConfig.calculateMaxSellQuantity(playerStock = 42)
        assertEquals(42, maxSell)

        val zeroSell = TradeConfig.calculateMaxSellQuantity(playerStock = 0)
        assertEquals(0, zeroSell)
    }

    @Test
    fun testTradingPostDiscountsAndBonuses() {
        val offer = TradeOffer(
            id = "offer_test",
            resourceType = ResourceType.FOOD,
            buyPricePerUnit = 20,
            sellPricePerUnit = 10
        )

        // 10% discount on buy: 20 * 0.9 = 18
        val discountedBuy = offer.getEffectiveBuyPrice(discountPercent = 10)
        assertEquals(18, discountedBuy)

        // 10% bonus on sell: 10 * 1.1 = 11
        val boostedSell = offer.getEffectiveSellPrice(bonusPercent = 10)
        assertEquals(11, boostedSell)
    }

    @Test
    fun testSuccessfulBuyTransaction_AtomicStateUpdates() {
        val viewModel = GameViewModel()
        val initialCredits = viewModel.gameState.value.resources.money
        val initialFood = viewModel.gameState.value.resources.food

        val quantityToBuy = 10
        val foodOffer = viewModel.gameState.value.merchantState.offers.find { it.resourceType == ResourceType.FOOD }!!
        val unitPrice = foodOffer.getEffectiveBuyPrice(0)
        val expectedCost = quantityToBuy * unitPrice
        val initialMerchantStock = foodOffer.merchantStock

        val result = viewModel.executeTrade(foodOffer.id, quantityToBuy, TradeMode.BUY)

        assertTrue(result.isSuccess)
        assertTrue(result is TradeTransactionResult.Success)

        val updatedState = viewModel.gameState.value
        assertEquals(initialCredits - expectedCost, updatedState.resources.money)
        assertEquals(initialFood + quantityToBuy, updatedState.resources.food)

        val updatedOffer = updatedState.merchantState.offers.find { it.id == foodOffer.id }!!
        assertEquals(initialMerchantStock - quantityToBuy, updatedOffer.merchantStock)
        assertEquals(expectedCost, updatedState.merchantState.totalCreditsTurnover)
        assertEquals(1, updatedState.merchantState.totalDealsCompleted)
    }

    @Test
    fun testFailedBuy_InsufficientCredits_ZeroStateMutation() {
        val viewModel = GameViewModel()
        // Drain money to 0
        viewModel.debugModifyResource(ResourceType.MONEY, -viewModel.gameState.value.resources.money)
        assertEquals(0, viewModel.gameState.value.resources.money)

        val initialFood = viewModel.gameState.value.resources.food
        val foodOffer = viewModel.gameState.value.merchantState.offers.find { it.resourceType == ResourceType.FOOD }!!
        val initialMerchantStock = foodOffer.merchantStock

        val result = viewModel.executeTrade(foodOffer.id, 5, TradeMode.BUY)

        assertFalse(result.isSuccess)
        assertTrue(result is TradeTransactionResult.Failure)
        assertEquals(TradeFailureReason.INSUFFICIENT_CREDITS, (result as TradeTransactionResult.Failure).reason)

        // Verify zero state corruption
        val state = viewModel.gameState.value
        assertEquals(0, state.resources.money)
        assertEquals(initialFood, state.resources.food)
        assertEquals(initialMerchantStock, state.merchantState.offers.find { it.id == foodOffer.id }!!.merchantStock)
        assertEquals(0, state.merchantState.totalDealsCompleted)
    }

    @Test
    fun testFailedBuy_WarehouseFull_ZeroStateMutation() {
        val viewModel = GameViewModel()
        // Fill warehouse completely
        viewModel.debugFillWarehouseTo(1.0f)
        assertTrue(viewModel.gameState.value.resources.isStorageFull)

        val initialCredits = viewModel.gameState.value.resources.money
        val foodOffer = viewModel.gameState.value.merchantState.offers.find { it.resourceType == ResourceType.FOOD }!!

        val result = viewModel.executeTrade(foodOffer.id, 5, TradeMode.BUY)

        assertFalse(result.isSuccess)
        assertTrue(result is TradeTransactionResult.Failure)
        assertEquals(TradeFailureReason.INSUFFICIENT_STORAGE, (result as TradeTransactionResult.Failure).reason)

        // Verify credits were NOT deducted
        assertEquals(initialCredits, viewModel.gameState.value.resources.money)
    }

    @Test
    fun testSuccessfulSellTransaction_AtomicStateUpdates() {
        val viewModel = GameViewModel()
        val initialCredits = viewModel.gameState.value.resources.money
        val initialFood = viewModel.gameState.value.resources.food
        val quantityToSell = 20
        assertTrue(initialFood >= quantityToSell)

        val foodOffer = viewModel.gameState.value.merchantState.offers.find { it.resourceType == ResourceType.FOOD }!!
        val unitSellPrice = foodOffer.getEffectiveSellPrice(0)
        val expectedRevenue = quantityToSell * unitSellPrice
        val initialMerchantStock = foodOffer.merchantStock

        val result = viewModel.executeTrade(foodOffer.id, quantityToSell, TradeMode.SELL)

        assertTrue(result.isSuccess)
        assertTrue(result is TradeTransactionResult.Success)

        val updatedState = viewModel.gameState.value
        assertEquals(initialCredits + expectedRevenue, updatedState.resources.money)
        assertEquals(initialFood - quantityToSell, updatedState.resources.food)

        val updatedOffer = updatedState.merchantState.offers.find { it.id == foodOffer.id }!!
        assertEquals(initialMerchantStock + quantityToSell, updatedOffer.merchantStock)
    }

    @Test
    fun testFailedSell_InsufficientStock_ZeroStateMutation() {
        val viewModel = GameViewModel()
        val initialCredits = viewModel.gameState.value.resources.money
        val foodOffer = viewModel.gameState.value.merchantState.offers.find { it.resourceType == ResourceType.FOOD }!!

        // Try to sell way more than player has
        val crazyQuantity = 99999
        val result = viewModel.executeTrade(foodOffer.id, crazyQuantity, TradeMode.SELL)

        assertFalse(result.isSuccess)
        assertTrue(result is TradeTransactionResult.Failure)
        assertEquals(TradeFailureReason.INSUFFICIENT_PLAYER_STOCK, (result as TradeTransactionResult.Failure).reason)

        // Verify zero mutation
        assertEquals(initialCredits, viewModel.gameState.value.resources.money)
    }

    @Test
    fun testLockedTradeOffer_UnlocksWithTradingPostLevel() {
        val viewModel = GameViewModel()
        val rareAlloyOffer = viewModel.gameState.value.merchantState.offers.find { it.resourceType == ResourceType.RARE_ALLOY }!!
        assertEquals(3, rareAlloyOffer.minTradingPostLevel)

        // Settlement starts with Trading Post unbuilt (level 0)
        assertFalse(rareAlloyOffer.isAvailable(tradingPostLevel = 0, settlementLevel = 1))

        val result = viewModel.executeTrade(rareAlloyOffer.id, 1, TradeMode.BUY)
        assertFalse(result.isSuccess)
        assertEquals(TradeFailureReason.LOCKED_ITEM, (result as TradeTransactionResult.Failure).reason)

        // When Trading Post is level 3 and settlement level 3, it is available
        assertTrue(rareAlloyOffer.isAvailable(tradingPostLevel = 3, settlementLevel = 3))
    }

    @Test
    fun testRestockMarketMechanics() {
        val offers = TradeConfig.createDefaultTradeOffers().map {
            it.copy(merchantStock = 2, maxMerchantStock = 100)
        }
        val restocked = TradeConfig.restockOffers(offers)
        for (i in offers.indices) {
            assertTrue(restocked[i].merchantStock > offers[i].merchantStock)
            assertTrue(restocked[i].merchantStock <= restocked[i].maxMerchantStock)
        }
    }
}
