package com.example.domain.model.reputation

import androidx.compose.ui.graphics.Color
import com.example.domain.model.GameDateTime
import com.example.ui.theme.*

/**
 * Global Settlement Reputation Tiers.
 * Represents the overall renown, trust, and moral standing of the outpost in the Wasteland.
 */
enum class ReputationTier(
    val id: String,
    val titleRu: String,
    val minPoints: Int,
    val maxPoints: Int,
    val descriptionRu: String,
    val badgeColor: Color,
    val tradeBuyDiscountPercent: Int,
    val tradeSellBonusPercent: Int,
    val recruitMoraleBonus: Int,
    val caravanFrequencyBonusRu: String
) {
    DESPISED(
        id = "tier_despised",
        titleRu = "Отщепенцы",
        minPoints = -100,
        maxPoints = -50,
        descriptionRu = "Пустоши считают ваш аванпост пристанищем грабителей и вероломных изгоев. Торговцы завышают цены, а караваны обходят базу стороной.",
        badgeColor = DangerCrimson,
        tradeBuyDiscountPercent = -20, // +20% markup
        tradeSellBonusPercent = -15,   // -15% penalty
        recruitMoraleBonus = -20,
        caravanFrequencyBonusRu = "Караваны избегают сектор"
    ),
    UNTRUSTED(
        id = "tier_untrusted",
        titleRu = "Подозрительный лагерь",
        minPoints = -49,
        maxPoints = -10,
        descriptionRu = "Вашим людям не доверяют. Странники настороже, а караванщики требуют предоплату и охрану.",
        badgeColor = WarningAmber,
        tradeBuyDiscountPercent = -10, // +10% markup
        tradeSellBonusPercent = -5,
        recruitMoraleBonus = -10,
        caravanFrequencyBonusRu = "Редкие караваны"
    ),
    NEUTRAL(
        id = "tier_neutral",
        titleRu = "Неизвестный аванпост",
        minPoints = -9,
        maxPoints = 24,
        descriptionRu = "Обычный укреплённый форпост на окраине пустошей. Ни дурной славы, ни громких подвигов.",
        badgeColor = TextSubtle,
        tradeBuyDiscountPercent = 0,
        tradeSellBonusPercent = 0,
        recruitMoraleBonus = 0,
        caravanFrequencyBonusRu = "Стандартные караваны"
    ),
    RECOGNIZED(
        id = "tier_recognized",
        titleRu = "Признанный лагерь",
        minPoints = 25,
        maxPoints = 49,
        descriptionRu = "О вашем аванпосте говорят как о надёжном укрытии. Окрестные поселенцы готовы торговать и делиться слухами.",
        badgeColor = TechCyan,
        tradeBuyDiscountPercent = 5,
        tradeSellBonusPercent = 5,
        recruitMoraleBonus = 5,
        caravanFrequencyBonusRu = "Регулярный приток торговцев"
    ),
    RESPECTED(
        id = "tier_respected",
        titleRu = "Уважаемый оплот",
        minPoints = 50,
        maxPoints = 79,
        descriptionRu = "Влиятельный центр порядка и безопасности. Караванные гильдии дают скидки, а опытные бойцы стремятся вступить в отряды.",
        badgeColor = SafeEmerald,
        tradeBuyDiscountPercent = 12,
        tradeSellBonusPercent = 10,
        recruitMoraleBonus = 15,
        caravanFrequencyBonusRu = "Частые визиты караванов (+25%)"
    ),
    LEGENDARY(
        id = "tier_legendary",
        titleRu = "Оплот надежды",
        minPoints = 80,
        maxPoints = 100,
        descriptionRu = "Легендарный форпост цивилизации в пустошах. Величайший авторитет среди всех вольных кланов и караванов.",
        badgeColor = CreditsYellow,
        tradeBuyDiscountPercent = 20,
        tradeSellBonusPercent = 15,
        recruitMoraleBonus = 25,
        caravanFrequencyBonusRu = "Элитные караваны и редкие поставки"
    );

    val isPositive: Boolean get() = minPoints >= 0
}

/**
 * Alignment and standing with specific factions.
 */
enum class FactionRelationTier(
    val id: String,
    val titleRu: String,
    val minPoints: Int,
    val maxPoints: Int,
    val descriptionRu: String,
    val badgeColor: Color,
    val tradeMarkupPercent: Int, // Negative means discount
    val isFriendlyOrBetter: Boolean
) {
    HOSTILE(
        id = "rel_hostile",
        titleRu = "Враждебные",
        minPoints = -100,
        maxPoints = -50,
        descriptionRu = "Открытая ненависть и вооружённые засады на пустошных маршрутах.",
        badgeColor = DangerCrimson,
        tradeMarkupPercent = 40,
        isFriendlyOrBetter = false
    ),
    COLD(
        id = "rel_cold",
        titleRu = "Холодные",
        minPoints = -49,
        maxPoints = -15,
        descriptionRu = "Настороженное недоверие, строгий досмотр и завышенные цены.",
        badgeColor = WarningAmber,
        tradeMarkupPercent = 15,
        isFriendlyOrBetter = false
    ),
    NEUTRAL(
        id = "rel_neutral",
        titleRu = "Нейтральные",
        minPoints = -14,
        maxPoints = 24,
        descriptionRu = "Прагматичные деловые отношения без симпатий и вражды.",
        badgeColor = TextSubtle,
        tradeMarkupPercent = 0,
        isFriendlyOrBetter = false
    ),
    FRIENDLY(
        id = "rel_friendly",
        titleRu = "Дружественные",
        minPoints = 25,
        maxPoints = 59,
        descriptionRu = "Тёплый приём, скидки на припасы и обмен разведданными.",
        badgeColor = SafeEmerald,
        tradeMarkupPercent = -10, // 10% discount
        isFriendlyOrBetter = true
    ),
    ALLIED(
        id = "rel_allied",
        titleRu = "Союзнические",
        minPoints = 60,
        maxPoints = 100,
        descriptionRu = "Полное доверие, военная взаимопомощь, уникальные технологии и караваны.",
        badgeColor = TechCyan,
        tradeMarkupPercent = -20, // 20% discount
        isFriendlyOrBetter = true
    );
}

/**
 * Perk or special bonus granted by a faction at a certain relationship threshold.
 */
data class FactionPerk(
    val id: String,
    val titleRu: String,
    val descriptionRu: String,
    val requiredTier: FactionRelationTier,
    val iconKey: String = "star"
) {
    fun isUnlocked(relationPoints: Int): Boolean = relationPoints >= requiredTier.minPoints
}

/**
 * Data definition of an external Wasteland faction.
 */
data class FactionDefinition(
    val id: String,
    val nameRu: String,
    val titleRu: String,
    val taglineRu: String,
    val descriptionRu: String,
    val iconKey: String,
    val colorHex: Long,
    val homeLocationId: String? = null,
    val baseRelation: Int = 0,
    val perks: List<FactionPerk> = emptyList(),
    val leaderNameRu: String = "",
    val leaderTitleRu: String = ""
)

/**
 * Current dynamic relationship state with a specific faction.
 */
data class FactionRelation(
    val factionId: String,
    val points: Int = 0,
    val isDiscovered: Boolean = true,
    val notes: String = ""
) {
    val tier: FactionRelationTier
        get() = when {
            points >= FactionRelationTier.ALLIED.minPoints -> FactionRelationTier.ALLIED
            points >= FactionRelationTier.FRIENDLY.minPoints -> FactionRelationTier.FRIENDLY
            points >= FactionRelationTier.NEUTRAL.minPoints -> FactionRelationTier.NEUTRAL
            points >= FactionRelationTier.COLD.minPoints -> FactionRelationTier.COLD
            else -> FactionRelationTier.HOSTILE
        }
}

/**
 * Categorization of reasons why reputation or relations changed.
 */
enum class ReputationChangeType(val titleRu: String, val iconKey: String) {
    QUEST_COMPLETED("Задание командования", "assignment"),
    EXPEDITION_SUCCESS("Успех экспедиции", "explore"),
    EVENT_CHOICE("Выбор в событии", "event"),
    TRADE_DEAL("Торговая сделка", "storefront"),
    DEFENSE_VICTORY("Оборона поселения", "shield"),
    RESEARCH_BREAKTHROUGH("Научный прорыв", "science"),
    SURVIVOR_RESCUE("Спасение выживших", "person_add"),
    TRIBUTE_OR_AID("Гуманитарная помощь", "volunteer_activism"),
    CRISIS_FAILURE("Кризис или провал", "warning"),
    DEBUG_MOD("Отладочная команда", "bug_report")
}

/**
 * Record of a reputation or faction relation change in the settlement chronicle.
 */
data class ReputationHistoryEntry(
    val id: String,
    val day: Int,
    val gameDateTime: GameDateTime,
    val sourceTitle: String,
    val reasonDescription: String,
    val delta: Int,
    val factionId: String? = null,
    val type: ReputationChangeType = ReputationChangeType.EVENT_CHOICE,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isPositive: Boolean get() = delta > 0
    val formattedDelta: String get() = if (delta > 0) "+$delta" else "$delta"
}

/**
 * Combined modifier for market pricing based on reputation and faction relations.
 */
data class TradeReputationModifier(
    val globalBuyDiscountPercent: Int,
    val globalSellBonusPercent: Int,
    val factionBuyDiscountPercent: Int,
    val factionSellBonusPercent: Int,
    val totalBuyDiscountPercent: Int,
    val totalSellBonusPercent: Int,
    val summaryRu: String
) {
    val buyModifierPercent: Int get() = -totalBuyDiscountPercent
    val sellModifierPercent: Int get() = totalSellBonusPercent
    val descriptionRu: String get() = summaryRu
}
