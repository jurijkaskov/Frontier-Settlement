package com.example.domain.service.combat

/**
 * Centralized game balance configuration parameters for tactical combat.
 */
object CombatBalanceConfig {
    // Action Points
    const val DEFAULT_MAX_AP: Int = 4
    const val BASIC_ATTACK_AP_COST: Int = 2
    const val DEFEND_AP_COST: Int = 1
    const val ROLE_ABILITY_AP_COST: Int = 2
    const val USE_ITEM_AP_COST: Int = 2
    const val END_TURN_AP_COST: Int = 0

    // Combat Defense Stance
    const val BASE_DEFEND_BONUS_DEFENSE: Int = 6
    const val DEFEND_EFFECT_DURATION_TURNS: Int = 1

    // Damage & Critical Hit Parameters
    const val BASE_MIN_DAMAGE: Int = 5
    const val CRIT_CHANCE_PERCENT: Int = 15
    const val CRIT_DAMAGE_MULTIPLIER: Float = 1.5f
    const val DEFENSE_DAMAGE_MITIGATION_FACTOR: Float = 0.5f

    // Standard Ability Cooldowns (in actor turns)
    const val DEFAULT_ABILITY_COOLDOWN_TURNS: Int = 2
    const val RALLY_ABILITY_COOLDOWN_TURNS: Int = 3

    // Experience Tuning
    const val BASE_XP_PER_COMBATANT: Int = 30
    const val XP_PER_DANGER_RATING: Int = 40
    const val BASE_VICTORY_BONUS_XP: Int = 50

    // Log limits
    const val MAX_LOG_HISTORY_SIZE: Int = 50
}
