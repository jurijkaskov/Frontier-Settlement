package com.example.domain.content.trader

import com.example.domain.content.core.ContentGenerationContext
import com.example.domain.content.core.GameRandomProvider
import com.example.domain.model.*
import kotlin.random.Random

/**
 * Procedural trader inventory and pricing generator.
 * Refreshes daily merchant stock with guaranteed staples and randomized rotating resources.
 */
object TraderStockGenerator {

    /**
     * Generates updated trade offers for a merchant based on current day, settlement level, and seed.
     */
    fun generateDailyStock(
        merchant: MerchantState,
        context: ContentGenerationContext
    ): List<TradeOffer> {
        val seed = GameRandomProvider.deriveSeed(context.gameSeed, "trader", merchant.merchant.id, context.currentGameDay)
        val random = Random(seed)

        val resultOffers = mutableListOf<TradeOffer>()

        // Base trade profiles for resources
        val resourceConfigs = listOf(
            Triple(ResourceType.FOOD, 10 to 6, "Свежий сухпаёк и очищенные концентраты"),
            Triple(ResourceType.WATER, 8 to 4, "Фильтрованная артезианская вода"),
            Triple(ResourceType.FUEL, 18 to 11, "Канистры очищенного дизельного топлива"),
            Triple(ResourceType.MATERIALS, 14 to 8, "Листовой металл, арматура и крепёж"),
            Triple(ResourceType.MEDICINE, 35 to 20, "Стерильные бинты, антисептики и стимуляторы"),
            Triple(ResourceType.AMMO, 28 to 16, "Патроны калибра 7.62 и 9мм в цинках"),
            Triple(ResourceType.COMPONENTS, 45 to 25, "Контроллеры, микрочипы и сервоприводы"),
            Triple(ResourceType.RARE_ALLOY, 75 to 45, "Титановые и композитные слитки высокой очистки")
        )

        for ((resType, pricePair, desc) in resourceConfigs) {
            val (baseBuy, baseSell) = pricePair
            val priceFluctuation = 0.90f + (random.nextFloat() * 0.22f) // 0.90..1.12
            val buyPrice = (baseBuy * priceFluctuation).toInt().coerceAtLeast(1)
            val sellPrice = (baseSell * priceFluctuation).toInt().coerceAtLeast(1)

            val stock = when (resType) {
                ResourceType.FOOD, ResourceType.WATER -> random.nextInt(30, 80)
                ResourceType.MATERIALS, ResourceType.FUEL -> random.nextInt(20, 50)
                ResourceType.MEDICINE, ResourceType.AMMO -> random.nextInt(10, 30)
                ResourceType.COMPONENTS -> random.nextInt(5, 20)
                ResourceType.RARE_ALLOY -> random.nextInt(2, 10)
                else -> 20
            }

            val maxStock = (stock * 1.5f).toInt()
            val minTradingPostLvl = when (resType) {
                ResourceType.COMPONENTS -> 1
                ResourceType.RARE_ALLOY -> 2
                else -> 0
            }

            resultOffers.add(
                TradeOffer(
                    id = "trade_offer_${resType.id}",
                    resourceType = resType,
                    buyPricePerUnit = buyPrice,
                    sellPricePerUnit = sellPrice,
                    merchantStock = stock,
                    maxMerchantStock = maxStock,
                    minTradingPostLevel = minTradingPostLvl,
                    minSettlementLevel = if (resType == ResourceType.RARE_ALLOY) 2 else 1,
                    descriptionRu = desc
                )
            )
        }

        return resultOffers
    }
}
