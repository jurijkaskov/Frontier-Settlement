package com.example.data

import com.example.domain.model.*
import kotlin.math.min

/**
 * Centralized configuration and algorithms for Frontier Settlement trading post.
 * Handles pricing rules, stock generation, and safe transaction boundary calculations.
 */
object TradeConfig {

    /**
     * Standard Wasteland Trade Offers Catalog.
     */
    fun createDefaultTradeOffers(): List<TradeOffer> {
        return listOf(
            TradeOffer(
                id = "offer_food",
                resourceType = ResourceType.FOOD,
                buyPricePerUnit = 3,
                sellPricePerUnit = 2,
                merchantStock = 80,
                maxMerchantStock = 120,
                minTradingPostLevel = 0,
                minSettlementLevel = 1,
                rarity = ItemRarity.COMMON,
                category = WarehouseFilterCategory.PRIMARY,
                descriptionRu = "Герметичные пищевые рационы, вяленое мясо и сушёные овощи для отрядов и колонистов."
            ),
            TradeOffer(
                id = "offer_water",
                resourceType = ResourceType.WATER,
                buyPricePerUnit = 2,
                sellPricePerUnit = 1,
                merchantStock = 100,
                maxMerchantStock = 150,
                minTradingPostLevel = 0,
                minSettlementLevel = 1,
                rarity = ItemRarity.COMMON,
                category = WarehouseFilterCategory.PRIMARY,
                descriptionRu = "Фляги и канистры с очищенной питьевой водой, дефицитной в сухих секторах пустошей."
            ),
            TradeOffer(
                id = "offer_fuel",
                resourceType = ResourceType.FUEL,
                buyPricePerUnit = 5,
                sellPricePerUnit = 3,
                merchantStock = 50,
                maxMerchantStock = 80,
                minTradingPostLevel = 0,
                minSettlementLevel = 1,
                rarity = ItemRarity.UNCOMMON,
                category = WarehouseFilterCategory.PRIMARY,
                descriptionRu = "Высокооктановое топливо и солярка для дизель-генераторов и бронированных машин."
            ),
            TradeOffer(
                id = "offer_materials",
                resourceType = ResourceType.MATERIALS,
                buyPricePerUnit = 4,
                sellPricePerUnit = 2,
                merchantStock = 60,
                maxMerchantStock = 100,
                minTradingPostLevel = 0,
                minSettlementLevel = 1,
                rarity = ItemRarity.COMMON,
                category = WarehouseFilterCategory.MATERIALS,
                descriptionRu = "Прокатная сталь, швеллеры и композитные панели для строительства и укрепления базы."
            ),
            TradeOffer(
                id = "offer_medicine",
                resourceType = ResourceType.MEDICINE,
                buyPricePerUnit = 15,
                sellPricePerUnit = 9,
                merchantStock = 20,
                maxMerchantStock = 30,
                minTradingPostLevel = 1,
                minSettlementLevel = 1,
                rarity = ItemRarity.UNCOMMON,
                category = WarehouseFilterCategory.ITEMS,
                descriptionRu = "Аптечки первой помощи, ампулы антирадина и хирургические наборы для полевого медпункта."
            ),
            TradeOffer(
                id = "offer_ammo",
                resourceType = ResourceType.AMMO,
                buyPricePerUnit = 12,
                sellPricePerUnit = 7,
                merchantStock = 30,
                maxMerchantStock = 45,
                minTradingPostLevel = 1,
                minSettlementLevel = 1,
                rarity = ItemRarity.UNCOMMON,
                category = WarehouseFilterCategory.ITEMS,
                descriptionRu = "Цинки с бронебойными патронами и дробью для стрелков экспедиционного корпуса."
            ),
            TradeOffer(
                id = "offer_components",
                resourceType = ResourceType.COMPONENTS,
                buyPricePerUnit = 25,
                sellPricePerUnit = 15,
                merchantStock = 15,
                maxMerchantStock = 25,
                minTradingPostLevel = 2,
                minSettlementLevel = 2,
                rarity = ItemRarity.RARE,
                category = WarehouseFilterCategory.MATERIALS,
                descriptionRu = "Довоенные электронные чипы, контроллеры и сервоприводы для продвинутых исследований."
            ),
            TradeOffer(
                id = "offer_rare_alloy",
                resourceType = ResourceType.RARE_ALLOY,
                buyPricePerUnit = 50,
                sellPricePerUnit = 30,
                merchantStock = 8,
                maxMerchantStock = 15,
                minTradingPostLevel = 3,
                minSettlementLevel = 3,
                rarity = ItemRarity.EPIC,
                category = WarehouseFilterCategory.VALUABLES,
                descriptionRu = "Титано-вольфрамовые слитки высокой плотности для создания топового оружия и брони."
            )
        )
    }

    /**
     * Calculates the maximum quantity a player can buy considering all constraints:
     * 1. Player's available credits
     * 2. Available warehouse storage capacity (unit size factor)
     * 3. Merchant available stock
     */
    fun calculateMaxBuyQuantity(
        playerCredits: Int,
        unitBuyPrice: Int,
        availableWarehouseCapacity: Int,
        unitSize: Int,
        merchantStock: Int
    ): Int {
        if (unitBuyPrice <= 0 || merchantStock <= 0) return 0

        // Financial limit
        val creditLimit = playerCredits / unitBuyPrice

        // Warehouse physical capacity limit
        val storageLimit = if (unitSize > 0) {
            availableWarehouseCapacity / unitSize
        } else {
            Int.MAX_VALUE
        }

        return min(merchantStock, min(creditLimit, storageLimit)).coerceAtLeast(0)
    }

    /**
     * Calculates the maximum quantity a player can sell:
     * Capped purely by the player's stored inventory quantity.
     */
    fun calculateMaxSellQuantity(playerStock: Int): Int {
        return playerStock.coerceAtLeast(0)
    }

    /**
     * Restocks merchant inventory up to maximum capacity.
     */
    fun restockOffers(currentOffers: List<TradeOffer>): List<TradeOffer> {
        return currentOffers.map { offer ->
            val restockAmount = (offer.maxMerchantStock * 0.4f).toInt().coerceAtLeast(5)
            val newStock = (offer.merchantStock + restockAmount).coerceAtMost(offer.maxMerchantStock)
            offer.copy(merchantStock = newStock)
        }
    }
}
