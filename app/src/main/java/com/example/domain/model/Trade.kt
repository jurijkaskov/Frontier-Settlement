package com.example.domain.model

/**
 * Mode of trading operation in the Frontier Market.
 */
enum class TradeMode(val titleRu: String, val actionVerbRu: String) {
    BUY("Покупка", "Купить"),
    SELL("Продажа", "Продать")
}

/**
 * Reason for trade transaction failure for clear UX transparency.
 */
enum class TradeFailureReason(val messageRu: String) {
    INSUFFICIENT_CREDITS("Недостаточно кредитов для совершения сделки."),
    INSUFFICIENT_STORAGE("Недостаточно места на складе для размещения купленных товаров."),
    INSUFFICIENT_PLAYER_STOCK("Недостаточно ресурсов на складе для продажи."),
    INSUFFICIENT_MERCHANT_STOCK("У торговца нет такого количества товара в наличии."),
    LOCKED_ITEM("Данный товар заблокирован. Требуется повышение уровня Торгового Поста."),
    INVALID_QUANTITY("Количество для обмена должно быть больше нуля.")
}

/**
 * A trade offer available in the wasteland market.
 *
 * @param id Unique identifier of the trade offer
 * @param resourceType The underlying resource type
 * @param buyPricePerUnit Cost in credits player pays to merchant for 1 unit
 * @param sellPricePerUnit Credits merchant pays to player for 1 unit
 * @param merchantStock Current stock available at the merchant
 * @param maxMerchantStock Restock ceiling for the merchant
 * @param minTradingPostLevel Required Trading Post building level in settlement (0 = always available)
 * @param minSettlementLevel Required Settlement level
 * @param rarity Item visual rarity tier
 * @param category Category for tab filtering
 * @param descriptionRu Flavor description
 */
data class TradeOffer(
    val id: String,
    val resourceType: ResourceType,
    val buyPricePerUnit: Int,
    val sellPricePerUnit: Int,
    val merchantStock: Int = 50,
    val maxMerchantStock: Int = 100,
    val minTradingPostLevel: Int = 0,
    val minSettlementLevel: Int = 1,
    val rarity: ItemRarity = ItemRarity.COMMON,
    val category: WarehouseFilterCategory = WarehouseFilterCategory.PRIMARY,
    val descriptionRu: String = ""
) {
    val nameRu: String get() = resourceType.nameRu
    val unitSize: Int get() = resourceType.unitSize

    /**
     * Checks if this trade item is unlocked for the player based on settlement buildings.
     */
    fun isAvailable(tradingPostLevel: Int, settlementLevel: Int): Boolean {
        return tradingPostLevel >= minTradingPostLevel && settlementLevel >= minSettlementLevel
    }

    /**
     * Calculates discounted buy price given a trading post discount percentage.
     */
    fun getEffectiveBuyPrice(discountPercent: Int = 0): Int {
        if (discountPercent <= 0) return buyPricePerUnit
        val discounted = (buyPricePerUnit * (100 - discountPercent.coerceIn(0, 50))) / 100
        return discounted.coerceAtLeast(1)
    }

    /**
     * Calculates boosted sell price given a trading post bonus percentage.
     */
    fun getEffectiveSellPrice(bonusPercent: Int = 0): Int {
        if (bonusPercent <= 0) return sellPricePerUnit
        val boosted = (sellPricePerUnit * (100 + bonusPercent.coerceIn(0, 50))) / 100
        return boosted.coerceAtLeast(1)
    }
}

/**
 * Merchant persona and trade profile.
 */
data class MerchantProfile(
    val id: String = "merchant_caravan_lead",
    val name: String = "Морган «Вексель»",
    val titleRu: String = "Караванщик Торговой Гильдии Пустошей",
    val greetingRu: String = "«Припасы, топливо, запчасти — у Гильдии есть всё. Плати кредитами или не трать моё время, поселенец.»",
    val faction: String = "Торговая Гильдия Пустоши",
    val daysUntilRestock: Int = 1
)

/**
 * Overall trading state stored within GameState.
 */
data class MerchantState(
    val merchant: MerchantProfile = MerchantProfile(),
    val offers: List<TradeOffer> = emptyList(),
    val totalCreditsTurnover: Int = 0,
    val totalDealsCompleted: Int = 0,
    val lastDealMessage: String? = null
)

/**
 * Typed result of a trade transaction.
 */
sealed interface TradeTransactionResult {
    val isSuccess: Boolean
    val message: String

    data class Success(
        val mode: TradeMode,
        val resourceType: ResourceType,
        val quantity: Int,
        val totalCredits: Int,
        override val message: String
    ) : TradeTransactionResult {
        override val isSuccess: Boolean get() = true
    }

    data class Failure(
        val reason: TradeFailureReason,
        override val message: String,
        val requiredCredits: Int = 0,
        val requiredCapacity: Int = 0,
        val availableQuantity: Int = 0
    ) : TradeTransactionResult {
        override val isSuccess: Boolean get() = false
    }
}
