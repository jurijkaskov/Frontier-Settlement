package com.example.domain.service.time

import com.example.data.ResearchConfig
import com.example.data.TradeConfig
import com.example.domain.model.BuildingStatus
import com.example.domain.model.BuildingType
import com.example.domain.model.CharacterStatus
import com.example.domain.model.DailySummary
import com.example.domain.model.EconomicShortageType
import com.example.domain.model.GameState
import com.example.domain.model.QuestStatus
import com.example.domain.model.ResourceType
import com.example.domain.service.economy.SettlementEconomyProcessor

/**
 * Pure domain service executing the daily tick pipeline when the game calendar crosses into a new day.
 * Ensures strict idempotency by recording processed days into [GameState.processedDays].
 */
object DailyTickProcessor {

    /**
     * Executes the daily pipeline for multiple sequential days (e.g. if time advanced by 3 days).
     * Applies each day step-by-step to properly trigger intermediate consequences and state updates.
     */
    fun processMultipleDays(
        initialState: GameState,
        crossedDays: List<Int>
    ): Pair<GameState, List<DailySummary>> {
        if (crossedDays.isEmpty()) {
            return Pair(initialState, emptyList())
        }

        var currentState = initialState
        val summaries = mutableListOf<DailySummary>()

        for (dayNumber in crossedDays) {
            val (nextState, summary) = processSingleDay(currentState, dayNumber)
            currentState = nextState
            summaries.add(summary)
        }

        return Pair(currentState, summaries)
    }

    /**
     * Executes the daily maintenance pipeline for a single target [dayNumber].
     * If the day was already processed, returns the unmodified state and an empty/cached summary to guarantee idempotency.
     */
    fun processSingleDay(
        state: GameState,
        dayNumber: Int
    ): Pair<GameState, DailySummary> {
        // 1. Idempotency Check
        if (state.processedDays.contains(dayNumber)) {
            val cachedSummary = DailySummary(
                day = dayNumber,
                previousDay = dayNumber - 1,
                economyReport = state.lastEconomyReport,
                summaryLogs = listOf("День $dayNumber уже был обработан ранее.")
            )
            return Pair(state, cachedSummary)
        }

        // 2. Centralized Settlement Economy Tick
        val (stateAfterEconomy, economyReport) = SettlementEconomyProcessor.processDailyEconomy(state, dayNumber)
        val res = stateAfterEconomy.resources
        val sett = stateAfterEconomy.settlement

        val isStarving = economyReport.shortages.any { it.type == EconomicShortageType.FOOD_SHORTAGE }
        val isDehydrated = economyReport.shortages.any { it.type == EconomicShortageType.WATER_SHORTAGE }

        // 3. Character Healing & Days in Settlement
        val clinicLevel = sett.buildings.find {
            it.type == BuildingType.MEDICAL_CLINIC && it.status == BuildingStatus.OPERATIONAL
        }?.level ?: 0
        val techMedicalRegenBonus = ResearchConfig.getMedicalRegenBonus(state.technologies)

        var healedCount = 0
        val updatedChars = stateAfterEconomy.characters.map { c ->
            val isRestingInSettlement = c.status != CharacterStatus.ON_EXPEDITION
            val nextDays = if (isRestingInSettlement) c.daysInSettlement + 1 else c.daysInSettlement

            if (isRestingInSettlement && c.health < c.maxHealth) {
                healedCount++
                val baseHeal = if (isStarving || isDehydrated) 2 else 15
                val clinicBonus = (clinicLevel * 10) + techMedicalRegenBonus
                c.copy(
                    health = (c.health + baseHeal + clinicBonus).coerceAtMost(c.maxHealth),
                    daysInSettlement = nextDays
                )
            } else {
                c.copy(daysInSettlement = nextDays)
            }
        }

        // 4. Merchant Restock
        val updatedOffers = TradeConfig.restockOffers(
            if (stateAfterEconomy.merchantState.offers.isNotEmpty()) stateAfterEconomy.merchantState.offers else TradeConfig.createDefaultTradeOffers()
        )
        val updatedMerchantState = stateAfterEconomy.merchantState.copy(offers = updatedOffers)

        // 5. Quests Evaluation
        val updatedQuests = stateAfterEconomy.quests.map { q ->
            if (q.id == "quest_2" && q.status == QuestStatus.IN_PROGRESS) {
                val progress = res.materials
                val isComplete = progress >= q.target
                q.copy(
                    progress = progress,
                    status = if (isComplete) QuestStatus.READY_TO_CLAIM else QuestStatus.IN_PROGRESS
                )
            } else q
        }

        // 6. Compilation of Summary & Logs
        val foodGain = economyReport.producedResources[ResourceType.FOOD] ?: 0
        val waterGain = economyReport.producedResources[ResourceType.WATER] ?: 0
        val matGain = economyReport.producedResources[ResourceType.MATERIALS] ?: 0
        val medGain = economyReport.producedResources[ResourceType.MEDICINE] ?: 0
        val foodNeeded = economyReport.consumedResources[ResourceType.FOOD] ?: 0
        val waterNeeded = economyReport.consumedResources[ResourceType.WATER] ?: 0
        val fuelNeeded = economyReport.consumedResources[ResourceType.FUEL] ?: 0

        val logMsg = buildString {
            append("День $dayNumber: Произведено +$foodGain Еды, +$waterGain Воды, +$matGain Мат., +${economyReport.incomeCredits} Кр.")
            if (medGain > 0) append(", +$medGain Мед.")
            if (isStarving) append(" [Внимание: Нехватка провизии!]")
            if (isDehydrated) append(" [Внимание: Дефицит воды!]")
            if (economyReport.overflowLost.isNotEmpty()) append(" [Склад: излишки потеряны]")
        }
        val newLogs = listOf(logMsg) + stateAfterEconomy.dayLogs.take(19)

        val summary = DailySummary(
            day = dayNumber,
            previousDay = dayNumber - 1,
            foodProduced = foodGain,
            foodConsumed = foodNeeded,
            waterProduced = waterGain,
            waterConsumed = waterNeeded,
            materialsProduced = matGain,
            creditsProduced = economyReport.incomeCredits,
            medicineProduced = medGain,
            fuelConsumed = fuelNeeded,
            isStarving = isStarving,
            isDehydrated = isDehydrated,
            charactersHealedCount = healedCount,
            merchantRestocked = true,
            economyReport = economyReport,
            overflowLost = economyReport.overflowLost,
            summaryLogs = listOf(logMsg)
        )

        val stateWithDailyData = stateAfterEconomy.copy(
            characters = updatedChars,
            dayLogs = newLogs,
            quests = updatedQuests,
            merchantState = updatedMerchantState,
            processedDays = stateAfterEconomy.processedDays + dayNumber,
            lastDailySummary = summary
        )

        // Process Quest tick and deadlines
        val stateAfterQuestTick = com.example.domain.service.quest.QuestManager.onDailyTick(stateWithDailyData)
        val nextState = com.example.domain.service.quest.QuestManager.syncQuestsOnStateChange(stateAfterQuestTick)

        return Pair(nextState, summary)
    }
}
