package com.example.domain.content.quest

import com.example.domain.model.quest.QuestRewardDefinition

/**
 * Budget and reward calculator for repeatable contracts and dynamic quests.
 */
object RewardBudgetCalculator {

    fun calculateQuestRewards(
        baseCredits: Int,
        baseXp: Int,
        baseReputation: Int,
        settlementLevel: Int,
        dangerMultiplier: Float = 1.0f
    ): QuestRewardDefinition {
        val levelFactor = 1.0f + ((settlementLevel - 1) * 0.15f)
        val finalCredits = (baseCredits * levelFactor * dangerMultiplier).toInt().coerceAtLeast(50)
        val finalXp = (baseXp * levelFactor * dangerMultiplier).toInt().coerceAtLeast(15)
        val finalRep = (baseReputation * dangerMultiplier).toInt().coerceAtLeast(3)

        return QuestRewardDefinition(
            credits = finalCredits,
            xp = finalXp,
            reputationDelta = finalRep,
            summaryRu = "+$finalCredits Кредитов, +$finalXp XP, +$finalRep Репутации"
        )
    }
}
