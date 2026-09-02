package com.example.domain.service.reputation

import com.example.data.ReputationBalanceConfig
import com.example.domain.model.GameState
import com.example.domain.model.reputation.*
import java.util.UUID

/**
 * Authoritative domain service managing all state mutations, history logging,
 * and cross-system modifier inquiries for settlement reputation and faction standing.
 */
object ReputationManager {

    /**
     * Changes the settlement global reputation score, clamp-safe, and records a chronicle entry.
     * Keeps `settlement.reputation` as the single source of truth.
     */
    fun changeSettlementReputation(
        state: GameState,
        delta: Int,
        sourceTitle: String,
        reason: String,
        type: ReputationChangeType = ReputationChangeType.EVENT_CHOICE
    ): Pair<GameState, ReputationHistoryEntry> {
        val currentRep = state.settlement.reputation
        val newRep = (currentRep + delta).coerceIn(ReputationBalanceConfig.MIN_POINTS, ReputationBalanceConfig.MAX_POINTS)
        val actualDelta = newRep - currentRep

        val entry = ReputationHistoryEntry(
            id = "rep_hist_${UUID.randomUUID().toString().take(8)}",
            day = state.day,
            gameDateTime = state.gameDateTime,
            sourceTitle = sourceTitle,
            reasonDescription = reason,
            delta = actualDelta,
            factionId = null,
            type = type,
            timestamp = System.currentTimeMillis()
        )

        val updatedSettlement = state.settlement.copy(reputation = newRep)
        val updatedHistory = listOf(entry) + state.reputationHistory.take(49)

        val logMsg = "Репутация поселения: ${entry.formattedDelta} ($newRep/100) — $sourceTitle: $reason"
        val updatedDayLogs = listOf(logMsg) + state.dayLogs.take(19)

        val updatedState = state.copy(
            settlement = updatedSettlement,
            reputationHistory = updatedHistory,
            dayLogs = updatedDayLogs
        )

        return Pair(updatedState, entry)
    }

    /**
     * Changes the diplomatic relationship score with a specific faction.
     */
    fun changeFactionRelation(
        state: GameState,
        factionId: String,
        delta: Int,
        sourceTitle: String,
        reason: String,
        type: ReputationChangeType = ReputationChangeType.EVENT_CHOICE
    ): Pair<GameState, ReputationHistoryEntry> {
        val currentRelation = state.factionRelations[factionId] ?: FactionRelation(
            factionId = factionId,
            points = ReputationBalanceConfig.getFaction(factionId)?.baseRelation ?: 0,
            isDiscovered = true
        )

        val currentPoints = currentRelation.points
        val newPoints = (currentPoints + delta).coerceIn(ReputationBalanceConfig.MIN_POINTS, ReputationBalanceConfig.MAX_POINTS)
        val actualDelta = newPoints - currentPoints

        val factionDef = ReputationBalanceConfig.getFaction(factionId)
        val factionName = factionDef?.nameRu ?: factionId

        val entry = ReputationHistoryEntry(
            id = "rep_hist_${UUID.randomUUID().toString().take(8)}",
            day = state.day,
            gameDateTime = state.gameDateTime,
            sourceTitle = sourceTitle,
            reasonDescription = reason,
            delta = actualDelta,
            factionId = factionId,
            type = type,
            timestamp = System.currentTimeMillis()
        )

        val updatedRelation = currentRelation.copy(
            points = newPoints,
            isDiscovered = true
        )

        val updatedRelationsMap = state.factionRelations + (factionId to updatedRelation)
        val updatedHistory = listOf(entry) + state.reputationHistory.take(49)

        val logMsg = "Отношения с «$factionName»: ${entry.formattedDelta} ($newPoints) — $sourceTitle"
        val updatedDayLogs = listOf(logMsg) + state.dayLogs.take(19)

        val updatedState = state.copy(
            factionRelations = updatedRelationsMap,
            reputationHistory = updatedHistory,
            dayLogs = updatedDayLogs
        )

        return Pair(updatedState, entry)
    }

    /**
     * Applies combined reputation delta and multiple faction relation deltas from an event or choice.
     */
    fun applyEventReputationConsequences(
        initialState: GameState,
        reputationDelta: Int,
        factionDeltas: Map<String, Int>,
        sourceTitle: String,
        narrativeReason: String,
        type: ReputationChangeType = ReputationChangeType.EVENT_CHOICE
    ): GameState {
        var currentState = initialState

        if (reputationDelta != 0) {
            val (nextState, _) = changeSettlementReputation(
                state = currentState,
                delta = reputationDelta,
                sourceTitle = sourceTitle,
                reason = narrativeReason,
                type = type
            )
            currentState = nextState
        }

        factionDeltas.forEach { (factionId, delta) ->
            if (delta != 0) {
                val (nextState, _) = changeFactionRelation(
                    state = currentState,
                    factionId = factionId,
                    delta = delta,
                    sourceTitle = sourceTitle,
                    reason = narrativeReason,
                    type = type
                )
                currentState = nextState
            }
        }

        return currentState
    }

    /**
     * Gets effective trade pricing modifier accounting for settlement rank and Trader Guild alignment.
     */
    fun getEffectiveTradeModifier(state: GameState): TradeReputationModifier {
        val traderPoints = state.factionRelations[ReputationBalanceConfig.FACTION_TRADERS]?.points
        return ReputationLevelResolver.resolveTradeModifier(
            settlementReputation = state.settlement.reputation,
            traderFactionRelationPoints = traderPoints
        )
    }

    /**
     * Research speed bonus percentage from Brotherhood of Engineers relations.
     */
    fun getEffectiveResearchBonusPercent(state: GameState): Int {
        val relation = state.factionRelations[ReputationBalanceConfig.FACTION_ENGINEERS] ?: return 0
        return when (relation.tier) {
            FactionRelationTier.ALLIED -> 30
            FactionRelationTier.FRIENDLY -> 15
            FactionRelationTier.HOSTILE -> -10
            else -> 0
        }
    }

    /**
     * Expedition speed bonus percentage from Nomad relations.
     */
    fun getEffectiveTravelSpeedBonusPercent(state: GameState): Int {
        val relation = state.factionRelations[ReputationBalanceConfig.FACTION_NOMADS] ?: return 0
        return when (relation.tier) {
            FactionRelationTier.ALLIED -> 35
            FactionRelationTier.FRIENDLY -> 20
            FactionRelationTier.HOSTILE -> -15
            else -> 0
        }
    }

    /**
     * Scavenging bonus points from Nomad relations.
     */
    fun getEffectiveScavengingBonus(state: GameState): Int {
        val relation = state.factionRelations[ReputationBalanceConfig.FACTION_NOMADS] ?: return 0
        return when (relation.tier) {
            FactionRelationTier.ALLIED -> 5
            FactionRelationTier.FRIENDLY -> 2
            else -> 0
        }
    }

    /**
     * Moral and recruit bonus from Settlement Reputation & Survivor Alliance.
     */
    fun getEffectiveRecruitMoraleBonus(state: GameState): Int {
        val tier = ReputationLevelResolver.resolveSettlementTier(state.settlement.reputation)
        val globalBonus = tier.recruitMoraleBonus

        val survivorRelation = state.factionRelations[ReputationBalanceConfig.FACTION_SURVIVORS]
        val survivorBonus = when (survivorRelation?.tier) {
            FactionRelationTier.ALLIED -> 20
            FactionRelationTier.FRIENDLY -> 10
            FactionRelationTier.HOSTILE -> -15
            else -> 0
        }

        return globalBonus + survivorBonus
    }
}
