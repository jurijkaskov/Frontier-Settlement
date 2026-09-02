package com.example

import com.example.data.InitialGameData
import com.example.data.ReputationBalanceConfig
import com.example.domain.model.*
import com.example.domain.model.reputation.*
import com.example.domain.service.reputation.ReputationLevelResolver
import com.example.domain.service.reputation.ReputationManager
import com.example.domain.service.events.EventOutcomeResolver
import com.example.domain.service.events.EventRequirementEvaluator
import org.junit.Assert.*
import org.junit.Test

class ReputationSystemTest {

    @Test
    fun testReputationTiersResolution() {
        assertEquals(ReputationTier.DESPISED, ReputationLevelResolver.resolveSettlementTier(-80))
        assertEquals(ReputationTier.UNTRUSTED, ReputationLevelResolver.resolveSettlementTier(-25))
        assertEquals(ReputationTier.NEUTRAL, ReputationLevelResolver.resolveSettlementTier(0))
        assertEquals(ReputationTier.RECOGNIZED, ReputationLevelResolver.resolveSettlementTier(30))
        assertEquals(ReputationTier.RESPECTED, ReputationLevelResolver.resolveSettlementTier(60))
        assertEquals(ReputationTier.LEGENDARY, ReputationLevelResolver.resolveSettlementTier(90))
    }

    @Test
    fun testFactionTiersResolution() {
        assertEquals(FactionRelationTier.HOSTILE, ReputationLevelResolver.resolveFactionTier(-60))
        assertEquals(FactionRelationTier.COLD, ReputationLevelResolver.resolveFactionTier(-20))
        assertEquals(FactionRelationTier.NEUTRAL, ReputationLevelResolver.resolveFactionTier(10))
        assertEquals(FactionRelationTier.FRIENDLY, ReputationLevelResolver.resolveFactionTier(40))
        assertEquals(FactionRelationTier.ALLIED, ReputationLevelResolver.resolveFactionTier(75))
    }

    @Test
    fun testChangeSettlementReputationClamping() {
        var state = InitialGameData.createInitialGameState()
        assertEquals(50, state.settlement.reputation)
        val initialHistoryCount = state.reputationHistory.size

        // Add 60 points -> should clamp at 100
        val (stateMax, _) = ReputationManager.changeSettlementReputation(
            state = state,
            delta = 60,
            sourceTitle = "Триумф",
            reason = "Победа над рейдерами"
        )
        assertEquals(100, stateMax.settlement.reputation)
        assertEquals(ReputationTier.LEGENDARY, stateMax.reputationTier)
        assertEquals(initialHistoryCount + 1, stateMax.reputationHistory.size)

        // Deduct 250 points -> should clamp at -100
        val (stateMin, _) = ReputationManager.changeSettlementReputation(
            state = stateMax,
            delta = -250,
            sourceTitle = "Предательство",
            reason = "Разграбление каравана"
        )
        assertEquals(-100, stateMin.settlement.reputation)
        assertEquals(ReputationTier.DESPISED, stateMin.reputationTier)
        assertEquals(initialHistoryCount + 2, stateMin.reputationHistory.size)
    }

    @Test
    fun testFactionRelationChangeAndPerkUnlock() {
        val state = InitialGameData.createInitialGameState()
        val tradersDef = ReputationBalanceConfig.getFaction(ReputationBalanceConfig.FACTION_TRADERS)
        assertNotNull(tradersDef)

        val perk1 = tradersDef!!.perks.first()
        // Initially relation is 15 (Neutral) -> perk1 requires FRIENDLY (25)
        assertFalse(perk1.isUnlocked(15))

        // Improve relations by +20 -> 35 (Friendly)
        val (updatedState, entry) = ReputationManager.changeFactionRelation(
            state = state,
            factionId = ReputationBalanceConfig.FACTION_TRADERS,
            delta = 20,
            sourceTitle = "Контракт",
            reason = "Успешная поставка деталей"
        )

        val points = updatedState.factionRelations[ReputationBalanceConfig.FACTION_TRADERS]?.points ?: 0
        assertEquals(35, points)
        assertTrue(perk1.isUnlocked(points))
        assertEquals(20, entry.delta)
        assertEquals(FactionRelationTier.FRIENDLY, updatedState.factionRelations[ReputationBalanceConfig.FACTION_TRADERS]?.tier)
    }

    @Test
    fun testEffectiveTradeModifier() {
        var state = InitialGameData.createInitialGameState()
        // Initial: Rep=50 (Respected: -12% buy discount, +10% sell bonus), Traders=15 (Neutral: 0)
        var mod = ReputationManager.getEffectiveTradeModifier(state)
        assertEquals(12, mod.totalBuyDiscountPercent)
        assertEquals(10, mod.totalSellBonusPercent)

        // Make traders Friendly (+5% buy discount, +5% sell bonus)
        val (stateFriendly, _) = ReputationManager.changeFactionRelation(
            state = state,
            factionId = ReputationBalanceConfig.FACTION_TRADERS,
            delta = 30,
            sourceTitle = "Торговый договор",
            reason = "Партнерство"
        )
        mod = ReputationManager.getEffectiveTradeModifier(stateFriendly)
        assertEquals(17, mod.totalBuyDiscountPercent) // 12 + 5
        assertEquals(15, mod.totalSellBonusPercent)   // 10 + 5
    }

    @Test
    fun testEventRequirementEvaluationForReputation() {
        val state = InitialGameData.createInitialGameState()
        val loc = state.locations.first()
        val exp = Expedition(
            id = "exp_test",
            location = loc,
            squad = state.characters.take(2),
            vehicle = state.vehicles.first(),
            supplies = emptyMap()
        )

        val reqLow = EventRequirement.RequiresMinReputation(30)
        val reqHigh = EventRequirement.RequiresMinReputation(80)

        assertTrue(EventRequirementEvaluator.evaluate(listOf(reqLow), state, exp).isMet)
        assertFalse(EventRequirementEvaluator.evaluate(listOf(reqHigh), state, exp).isMet)

        val reqFactionFriendly = EventRequirement.RequiresFactionRelation(
            factionId = ReputationBalanceConfig.FACTION_TRADERS,
            minRelationPoints = 25
        )
        // Default traders relation is 15, so minRelationPoints=25 is not met
        assertFalse(EventRequirementEvaluator.evaluate(listOf(reqFactionFriendly), state, exp).isMet)
    }

    @Test
    fun testReputationManagerApplyEventReputationConsequences() {
        val state = InitialGameData.createInitialGameState()
        val initialRep = state.settlement.reputation
        val initialNomads = state.factionRelations[ReputationBalanceConfig.FACTION_NOMADS]?.points ?: 10

        val newState = ReputationManager.applyEventReputationConsequences(
            initialState = state,
            reputationDelta = 10,
            factionDeltas = mapOf(ReputationBalanceConfig.FACTION_NOMADS to 15),
            sourceTitle = "Засада в каньоне",
            narrativeReason = "Вы помогли кочевникам защитить караван от мутантов."
        )

        assertEquals(initialRep + 10, newState.settlement.reputation)
        assertEquals(initialNomads + 15, newState.factionRelations[ReputationBalanceConfig.FACTION_NOMADS]?.points)
        assertTrue(newState.reputationHistory.isNotEmpty())
        val lastEntry = newState.reputationHistory.first()
        assertEquals(15, lastEntry.delta)
        assertEquals(ReputationBalanceConfig.FACTION_NOMADS, lastEntry.factionId)
    }
}
