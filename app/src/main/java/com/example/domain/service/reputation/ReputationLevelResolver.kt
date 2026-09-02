package com.example.domain.service.reputation

import com.example.data.ReputationBalanceConfig
import com.example.domain.model.reputation.*

/**
 * Pure resolver computing dynamic tiers, progression metrics, and mechanical modifiers
 * from raw reputation and relation numbers without storing cached redundant values.
 */
object ReputationLevelResolver {

    /**
     * Resolves the [ReputationTier] for a given numerical settlement reputation score (-100 to +100).
     */
    fun resolveSettlementTier(reputation: Int): ReputationTier {
        val clamped = reputation.coerceIn(ReputationBalanceConfig.MIN_POINTS, ReputationBalanceConfig.MAX_POINTS)
        return when {
            clamped >= ReputationTier.LEGENDARY.minPoints -> ReputationTier.LEGENDARY
            clamped >= ReputationTier.RESPECTED.minPoints -> ReputationTier.RESPECTED
            clamped >= ReputationTier.RECOGNIZED.minPoints -> ReputationTier.RECOGNIZED
            clamped >= ReputationTier.NEUTRAL.minPoints -> ReputationTier.NEUTRAL
            clamped >= ReputationTier.UNTRUSTED.minPoints -> ReputationTier.UNTRUSTED
            else -> ReputationTier.DESPISED
        }
    }

    /**
     * Resolves the [FactionRelationTier] for a specific faction relationship points (-100 to +100).
     */
    fun resolveFactionTier(relationPoints: Int): FactionRelationTier {
        val clamped = relationPoints.coerceIn(ReputationBalanceConfig.MIN_POINTS, ReputationBalanceConfig.MAX_POINTS)
        return when {
            clamped >= FactionRelationTier.ALLIED.minPoints -> FactionRelationTier.ALLIED
            clamped >= FactionRelationTier.FRIENDLY.minPoints -> FactionRelationTier.FRIENDLY
            clamped >= FactionRelationTier.NEUTRAL.minPoints -> FactionRelationTier.NEUTRAL
            clamped >= FactionRelationTier.COLD.minPoints -> FactionRelationTier.COLD
            else -> FactionRelationTier.HOSTILE
        }
    }

    /**
     * Calculates the progress fraction (0.0f to 1.0f) within the current tier towards the next tier.
     * Returns: Triple(currentTier, progressFraction, pointsNeededForNextTier).
     */
    fun calculateSettlementTierProgress(reputation: Int): Triple<ReputationTier, Float, Int> {
        val currentTier = resolveSettlementTier(reputation)
        val clamped = reputation.coerceIn(ReputationBalanceConfig.MIN_POINTS, ReputationBalanceConfig.MAX_POINTS)

        if (currentTier == ReputationTier.LEGENDARY) {
            return Triple(currentTier, 1.0f, 0)
        }

        val nextTier = when (currentTier) {
            ReputationTier.DESPISED -> ReputationTier.UNTRUSTED
            ReputationTier.UNTRUSTED -> ReputationTier.NEUTRAL
            ReputationTier.NEUTRAL -> ReputationTier.RECOGNIZED
            ReputationTier.RECOGNIZED -> ReputationTier.RESPECTED
            ReputationTier.RESPECTED -> ReputationTier.LEGENDARY
            ReputationTier.LEGENDARY -> ReputationTier.LEGENDARY
        }

        val range = (nextTier.minPoints - currentTier.minPoints).coerceAtLeast(1)
        val currentOffset = clamped - currentTier.minPoints
        val fraction = (currentOffset.toFloat() / range.toFloat()).coerceIn(0f, 1f)
        val needed = (nextTier.minPoints - clamped).coerceAtLeast(0)

        return Triple(currentTier, fraction, needed)
    }

    /**
     * Calculates the progress fraction (0.0f to 1.0f) for a faction relationship.
     */
    fun calculateFactionTierProgress(relationPoints: Int): Triple<FactionRelationTier, Float, Int> {
        val currentTier = resolveFactionTier(relationPoints)
        val clamped = relationPoints.coerceIn(ReputationBalanceConfig.MIN_POINTS, ReputationBalanceConfig.MAX_POINTS)

        if (currentTier == FactionRelationTier.ALLIED) {
            return Triple(currentTier, 1.0f, 0)
        }

        val nextTier = when (currentTier) {
            FactionRelationTier.HOSTILE -> FactionRelationTier.COLD
            FactionRelationTier.COLD -> FactionRelationTier.NEUTRAL
            FactionRelationTier.NEUTRAL -> FactionRelationTier.FRIENDLY
            FactionRelationTier.FRIENDLY -> FactionRelationTier.ALLIED
            FactionRelationTier.ALLIED -> FactionRelationTier.ALLIED
        }

        val range = (nextTier.minPoints - currentTier.minPoints).coerceAtLeast(1)
        val currentOffset = clamped - currentTier.minPoints
        val fraction = (currentOffset.toFloat() / range.toFloat()).coerceIn(0f, 1f)
        val needed = (nextTier.minPoints - clamped).coerceAtLeast(0)

        return Triple(currentTier, fraction, needed)
    }

    /**
     * Resolves combined market discounts and bonuses based on global reputation and Trader Guild standing.
     */
    fun resolveTradeModifier(
        settlementReputation: Int,
        traderFactionRelationPoints: Int?
    ): TradeReputationModifier {
        val globalTier = resolveSettlementTier(settlementReputation)
        val globalBuyDiscount = globalTier.tradeBuyDiscountPercent
        val globalSellBonus = globalTier.tradeSellBonusPercent

        val traderTier = if (traderFactionRelationPoints != null) {
            resolveFactionTier(traderFactionRelationPoints)
        } else {
            FactionRelationTier.NEUTRAL
        }

        // If friendly: -5%, if allied: -15%
        val factionBuyDiscount = when (traderTier) {
            FactionRelationTier.ALLIED -> 15
            FactionRelationTier.FRIENDLY -> 5
            FactionRelationTier.COLD -> -10 // +10% price
            FactionRelationTier.HOSTILE -> -25 // +25% price
            else -> 0
        }

        val factionSellBonus = when (traderTier) {
            FactionRelationTier.ALLIED -> 10
            FactionRelationTier.FRIENDLY -> 5
            FactionRelationTier.COLD -> -5
            FactionRelationTier.HOSTILE -> -15
            else -> 0
        }

        val totalBuyDiscount = (globalBuyDiscount + factionBuyDiscount).coerceIn(-40, 50)
        val totalSellBonus = (globalSellBonus + factionSellBonus).coerceIn(-30, 40)

        val summary = buildString {
            if (totalBuyDiscount > 0) append("Скидка на покупку -$totalBuyDiscount%")
            else if (totalBuyDiscount < 0) append("Наценка на покупку +${-totalBuyDiscount}%")
            else append("Базовые цены покупки")

            append(", ")
            if (totalSellBonus > 0) append("бонус продажи +$totalSellBonus%")
            else if (totalSellBonus < 0) append("штраф продажи $totalSellBonus%")
            else append("базовые цены сбыта")
        }

        return TradeReputationModifier(
            globalBuyDiscountPercent = globalBuyDiscount,
            globalSellBonusPercent = globalSellBonus,
            factionBuyDiscountPercent = factionBuyDiscount,
            factionSellBonusPercent = factionSellBonus,
            totalBuyDiscountPercent = totalBuyDiscount,
            totalSellBonusPercent = totalSellBonus,
            summaryRu = summary
        )
    }
}
