package com.example.domain.service.combat.ai

import com.example.domain.model.*
import com.example.domain.service.combat.CombatBalanceConfig

/**
 * Evaluates and scores potential targets for an AI combatant based on AI profile preferences and tactical situation.
 */
object EnemyTargetEvaluator {

    data class TargetEvaluation(
        val target: Combatant,
        val score: Float,
        val explanation: String,
        val isKillPossible: Boolean = false
    )

    /**
     * Evaluates a candidate target for a specific action.
     */
    fun evaluateTarget(
        actor: Combatant,
        target: Combatant,
        action: CombatAction,
        profile: EnemyAIProfile,
        state: CombatState
    ): TargetEvaluation {
        if (target.isDefeated) {
            return TargetEvaluation(target, -100f, "Цель уже выведена из строя.")
        }

        return if (target.team != actor.team) {
            evaluateHostileTarget(actor, target, action, profile)
        } else {
            evaluateFriendlyTarget(actor, target, action, profile)
        }
    }

    private fun evaluateHostileTarget(
        actor: Combatant,
        target: Combatant,
        action: CombatAction,
        profile: EnemyAIProfile
    ): TargetEvaluation {
        var score = 30.0f
        val reasons = mutableListOf<String>()

        // 1. Estimated damage & Kill threshold
        val estimatedDamage = ((actor.effectiveAttack * action.powerMultiplier) - 
            (target.effectiveDefense * CombatBalanceConfig.DEFENSE_DAMAGE_MITIGATION_FACTOR))
            .coerceAtLeast(CombatBalanceConfig.BASE_MIN_DAMAGE.toFloat())

        val isKillPossible = estimatedDamage >= target.currentHealth
        if (isKillPossible) {
            score += 55.0f
            reasons.add("Возможно добить цель (+55)")
        }

        // 2. Health & Vulnerability evaluation
        val missingHpRatio = 1.0f - target.hpFraction
        when (profile.targetPreference) {
            TargetPreference.LOWEST_HP -> {
                val hpScore = missingHpRatio * 45.0f
                score += hpScore
                reasons.add("Приоритет слабого здоровья (+${hpScore.toInt()})")
            }
            TargetPreference.HIGHEST_THREAT -> {
                val hpScore = missingHpRatio * 15.0f
                score += hpScore
            }
            TargetPreference.BALANCED -> {
                val hpScore = missingHpRatio * 25.0f
                score += hpScore
            }
            TargetPreference.RANDOM_VALID -> {
                // Neutral
            }
            TargetPreference.SUPPORT_ROLE -> {
                val hpScore = missingHpRatio * 20.0f
                score += hpScore
            }
        }

        // 3. Threat Assessment
        val threatScore = calculateThreat(target)
        if (profile.targetPreference == TargetPreference.HIGHEST_THREAT) {
            score += threatScore * 1.6f
            reasons.add("Приоритет высокой угрозы (+${(threatScore * 1.6f).toInt()})")
        } else if (profile.targetPreference == TargetPreference.BALANCED) {
            score += threatScore * 0.8f
        }

        // 4. Target Defensive Status & Debuff Synergies
        val isDefending = target.activeEffects.any { it.effectType == CombatEffectType.DEFENDING }
        if (isDefending && !isKillPossible) {
            score -= 10.0f
            reasons.add("Цель в укрытии (-10)")
        }

        val hasDefenseDebuff = target.activeEffects.any { it.effectType == CombatEffectType.DEBUFF_DEFENSE }
        if (hasDefenseDebuff) {
            score += 12.0f
            reasons.add("Цель уязвима (+12)")
        }

        // Debuff action specifics: avoid redundant debuffing
        val hasSameDebuffAlready = action.appliedEffects.any { eff ->
            target.activeEffects.any { it.effectType == eff.effectType }
        }
        if (hasSameDebuffAlready) {
            score -= 25.0f
            reasons.add("Эффект дебаффа уже активен (-25)")
        }

        val explanation = reasons.joinToString(", ").ifEmpty { "Стандартный выбор цели" }
        return TargetEvaluation(
            target = target,
            score = score.coerceAtLeast(0f),
            explanation = explanation,
            isKillPossible = isKillPossible
        )
    }

    private fun evaluateFriendlyTarget(
        actor: Combatant,
        target: Combatant,
        action: CombatAction,
        profile: EnemyAIProfile
    ): TargetEvaluation {
        var score = 20.0f
        val reasons = mutableListOf<String>()

        // 1. Healing action evaluation
        if (action.healingAmount > 0) {
            val missingHp = target.maxHealth - target.currentHealth
            if (missingHp <= 0) {
                return TargetEvaluation(target, -100.0f, "Союзник полностью здоров (0 HP урона)")
            }

            if (target.hpFraction <= 0.35f) {
                score += 70.0f
                reasons.add("Критическое состояние союзника (+70)")
            } else if (target.hpFraction <= 0.70f) {
                score += 45.0f
                reasons.add("Раненый союзник (+45)")
            } else {
                score += 10.0f
                reasons.add("Незначительный урон (+10)")
            }

            // Prioritize higher value targets
            if (target.role == CharacterRole.SOLDIER || target.maxHealth >= 60) {
                score += 10.0f
                reasons.add("Ключевой союзник (+10)")
            }
        }

        // 2. Buff / Fortification evaluation
        if (action.bonusDefenseGain > 0 || action.appliedEffects.any { it.effectType == CombatEffectType.BUFF_DEFENSE }) {
            val alreadyBuffed = target.activeEffects.any { 
                it.effectType == CombatEffectType.BUFF_DEFENSE || it.effectType == CombatEffectType.DEFENDING 
            }
            if (alreadyBuffed) {
                return TargetEvaluation(target, -50.0f, "Союзник уже защищён/укреплён")
            }

            if (target.hpFraction <= 0.60f) {
                score += 40.0f
                reasons.add("Защита уязвимого союзника (+40)")
            } else {
                score += 15.0f
                reasons.add("Профилактическое укрепление (+15)")
            }
        }

        // 3. Attack buff (Rally / Focus)
        if (action.appliedEffects.any { it.effectType == CombatEffectType.BUFF_ATTACK }) {
            val alreadyHasAttackBuff = target.activeEffects.any { it.effectType == CombatEffectType.BUFF_ATTACK }
            if (alreadyHasAttackBuff) {
                return TargetEvaluation(target, -50.0f, "Бафф атаки уже активен")
            }
            score += 35.0f
            reasons.add("Усиление атаки (+35)")
        }

        val explanation = reasons.joinToString(", ").ifEmpty { "Поддержка союзника" }
        return TargetEvaluation(
            target = target,
            score = score.coerceAtLeast(0f),
            explanation = explanation,
            isKillPossible = false
        )
    }

    private fun calculateThreat(target: Combatant): Float {
        var threat = 0f
        threat += (target.effectiveAttack - 8).coerceAtLeast(0) * 1.8f
        when (target.role) {
            CharacterRole.MEDIC -> threat += 15.0f // eliminate player healer
            CharacterRole.SOLDIER -> threat += 12.0f // eliminate main damage dealer
            CharacterRole.SCOUT -> threat += 6.0f
            CharacterRole.ENGINEER -> threat += 5.0f
            CharacterRole.SCAVENGER -> threat += 4.0f
            null -> Unit
        }
        return threat
    }
}
