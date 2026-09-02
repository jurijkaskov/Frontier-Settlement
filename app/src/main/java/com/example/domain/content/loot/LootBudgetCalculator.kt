package com.example.domain.content.loot

import com.example.domain.model.DangerLevel

/**
 * Budget and scaling calculator for loot rewards.
 * Ensures rewards scale smoothly with danger level, settlement progress, and scavenging perks.
 */
object LootBudgetCalculator {

    /**
     * Calculates the target credit budget multiplier based on danger and scavenging skill.
     */
    fun calculateBudgetMultiplier(
        dangerLevel: DangerLevel,
        scavengingBonusPercent: Int = 0
    ): Float {
        val baseMultiplier = when (dangerLevel) {
            DangerLevel.SAFE -> 1.0f
            DangerLevel.LOW -> 1.25f
            DangerLevel.MODERATE -> 1.6f
            DangerLevel.HIGH -> 2.1f
            DangerLevel.EXTREME -> 2.8f
            DangerLevel.UNKNOWN -> 1.3f
        }
        val skillMultiplier = 1.0f + (scavengingBonusPercent / 100f).coerceIn(0f, 1.0f)
        return baseMultiplier * skillMultiplier
    }

    /**
     * Clamps generated credits within calculated bounds.
     */
    fun clampCredits(baseCredits: Int, dangerLevel: DangerLevel): Int {
        val min = (baseCredits * 0.75f).toInt()
        val max = (baseCredits * calculateBudgetMultiplier(dangerLevel)).toInt().coerceAtLeast(min + 10)
        return (baseCredits).coerceIn(min, max)
    }
}
