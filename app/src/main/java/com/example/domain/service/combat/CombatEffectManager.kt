package com.example.domain.service.combat

import com.example.domain.model.*

/**
 * Universal manager for combat status effects lifecycle, stacking rules, duration decay,
 * periodic triggers (DoT/HoT), stat calculations, and state cleanups.
 */
object CombatEffectManager {

    data class TurnStartEffectResult(
        val combatant: Combatant,
        val logs: List<CombatLogEntry>,
        val extraAp: Int = 0
    )

    /**
     * Applies a new effect to the target combatant according to its stacking rule and category.
     */
    fun applyEffect(target: Combatant, effect: CombatEffectInstance): Combatant {
        if (target.isDefeated) return target

        val existingEffects = target.activeEffects.toMutableList()
        val index = existingEffects.indexOfFirst {
            if (effect.definitionId != null && it.definitionId != null) {
                it.definitionId == effect.definitionId
            } else {
                it.effectType == effect.effectType
            }
        }

        if (index >= 0) {
            val existing = existingEffects[index]
            when (effect.stackingRule) {
                EffectStackingRule.REPLACE -> {
                    existingEffects[index] = effect
                }
                EffectStackingRule.REFRESH_DURATION -> {
                    existingEffects[index] = existing.copy(
                        remainingTurns = maxOf(existing.remainingTurns, effect.remainingTurns),
                        remainingRounds = maxOf(existing.remainingRounds, effect.remainingRounds),
                        modifier = maxOf(existing.modifier, effect.modifier),
                        powerMultiplier = maxOf(existing.powerMultiplier, effect.powerMultiplier),
                        tickDamage = maxOf(existing.tickDamage, effect.tickDamage),
                        tickHeal = maxOf(existing.tickHeal, effect.tickHeal)
                    )
                }
                EffectStackingRule.STACK -> {
                    val newStacks = (existing.stacks + effect.stacks).coerceAtMost(existing.maxStacks)
                    existingEffects[index] = existing.copy(
                        stacks = newStacks,
                        remainingTurns = maxOf(existing.remainingTurns, effect.remainingTurns),
                        remainingRounds = maxOf(existing.remainingRounds, effect.remainingRounds),
                        modifier = existing.modifier,
                        powerMultiplier = existing.powerMultiplier
                    )
                }
                EffectStackingRule.IGNORE_DUPLICATE -> {
                    // Do nothing, existing instance remains unchanged
                }
            }
        } else {
            existingEffects.add(effect)
        }

        // Update combatant status if DEFENDING or STUNNED
        var updatedStatus = target.status
        if (effect.effectType == CombatEffectType.DEFENDING && target.status == CombatantStatus.ACTIVE) {
            updatedStatus = CombatantStatus.DEFENDING
        } else if (effect.effectType == CombatEffectType.STUN && !target.isDefeated) {
            updatedStatus = CombatantStatus.STUNNED
        }

        return target.copy(
            activeEffects = existingEffects,
            status = updatedStatus
        )
    }

    /**
     * Removes a specific effect instance by its unique ID.
     */
    fun removeEffect(target: Combatant, instanceId: String): Combatant {
        val updatedEffects = target.activeEffects.filter { it.instanceId != instanceId }
        return sanitizeCombatantStatus(target.copy(activeEffects = updatedEffects))
    }

    /**
     * Removes all active effects of the given type.
     */
    fun removeEffectsByType(target: Combatant, effectType: CombatEffectType): Combatant {
        val updatedEffects = target.activeEffects.filter { it.effectType != effectType }
        return sanitizeCombatantStatus(target.copy(activeEffects = updatedEffects))
    }

    /**
     * Cleanses all effects matching a specific category (e.g. all debuffs or control effects).
     */
    fun cleanseEffects(target: Combatant, category: CombatEffectCategory): Combatant {
        val updatedEffects = target.activeEffects.filter { it.category != category }
        return sanitizeCombatantStatus(target.copy(activeEffects = updatedEffects))
    }

    /**
     * Ticks periodic effects (DoT/HoT), decays durations, updates cooldowns and statuses at the START of turn.
     */
    fun processTurnStart(combatant: Combatant, roundNumber: Int): TurnStartEffectResult {
        if (combatant.isDefeated) {
            return TurnStartEffectResult(combatant, emptyList())
        }

        val generatedLogs = mutableListOf<CombatLogEntry>()
        val isPlayerSide = combatant.team == CombatantTeam.PLAYER
        var currentHp = combatant.currentHealth
        var extraAp = 0

        // 1. Process Periodic Triggers (ON_TURN_START: Damage-over-time, Heal-over-time, AP boosts)
        combatant.activeEffects.forEach { effect ->
            if (effect.tickTrigger == EffectTickTrigger.ON_TURN_START || effect.tickDamage > 0 || effect.tickHeal > 0 || effect.effectType == CombatEffectType.BUFF_AP) {
                // DoT (Bleeding / Poison)
                if (effect.tickDamage > 0 && currentHp > 0) {
                    val damage = effect.tickDamage * effect.stacks.coerceAtLeast(1)
                    currentHp = (currentHp - damage).coerceAtLeast(0)
                    val isDead = currentHp <= 0

                    val dotIcon = if (effect.effectType == CombatEffectType.DEBUFF_BLEED) "🩸" else "🧪"
                    val logText = if (isDead) {
                        "$dotIcon [${combatant.displayName}] получает $damage периодического урона от «${effect.name}» и теряет сознание!"
                    } else {
                        "$dotIcon [${combatant.displayName}] получает $damage урона от «${effect.name}» ($currentHp/${combatant.maxHealth} HP)."
                    }

                    generatedLogs.add(
                        CombatLogEntry(
                            turn = roundNumber,
                            text = logText,
                            isPlayerAction = isPlayerSide,
                            logType = if (isDead) CombatLogType.COMBATANT_INCAPACITATED else CombatLogType.ACTION_USED,
                            targetId = combatant.id,
                            value = damage
                        )
                    )
                }

                // HoT (Regeneration)
                if (effect.tickHeal > 0 && currentHp > 0) {
                    val heal = effect.tickHeal * effect.stacks.coerceAtLeast(1)
                    val healedHp = (currentHp + heal).coerceAtMost(combatant.maxHealth)
                    val actualDiff = healedHp - currentHp
                    currentHp = healedHp

                    if (actualDiff > 0) {
                        generatedLogs.add(
                            CombatLogEntry(
                                turn = roundNumber,
                                text = "💉 [${combatant.displayName}] восстанавливает +$actualDiff HP от «${effect.name}» ($currentHp/${combatant.maxHealth} HP).",
                                isPlayerAction = isPlayerSide,
                                logType = CombatLogType.ACTION_USED,
                                targetId = combatant.id,
                                value = actualDiff
                            )
                        )
                    }
                }

                // AP Boost (Adrenaline)
                if (effect.effectType == CombatEffectType.BUFF_AP) {
                    val apBonus = effect.modifier * effect.stacks.coerceAtLeast(1)
                    extraAp += apBonus
                    generatedLogs.add(
                        CombatLogEntry(
                            turn = roundNumber,
                            text = "⚡ [${combatant.displayName}] получает +$apBonus ОД от «${effect.name}».",
                            isPlayerAction = isPlayerSide,
                            logType = CombatLogType.ACTION_USED,
                            targetId = combatant.id
                        )
                    )
                }
            }
        }

        // Check if combatant died from DoT
        if (currentHp <= 0) {
            val defeatedStatus = if (isPlayerSide) CombatantStatus.INCAPACITATED else CombatantStatus.DEFEATED
            val deadCombatant = combatant.copy(
                currentHealth = 0,
                status = defeatedStatus,
                activeEffects = emptyList()
            )
            return TurnStartEffectResult(deadCombatant, generatedLogs)
        }

        // 2. Control / Stun Check
        val isStunned = combatant.status == CombatantStatus.STUNNED || combatant.hasEffect(CombatEffectType.STUN)
        if (isStunned) {
            generatedLogs.add(
                CombatLogEntry(
                    turn = roundNumber,
                    text = "💫 [${combatant.displayName}] оглушён и пропускает свой ход!",
                    isPlayerAction = isPlayerSide,
                    logType = CombatLogType.ACTION_USED,
                    targetId = combatant.id
                )
            )
        }

        // 3. Duration Decays (UNTIL_NEXT_TURN, TURNS)
        val updatedEffects = mutableListOf<CombatEffectInstance>()
        combatant.activeEffects.forEach { effect ->
            when (effect.durationType) {
                EffectDurationType.UNTIL_NEXT_TURN -> {
                    // Expires at turn start
                    generatedLogs.add(
                        CombatLogEntry(
                            turn = roundNumber,
                            text = "⏳ Действие «${effect.name}» на [${combatant.displayName}] завершилось.",
                            isPlayerAction = isPlayerSide,
                            logType = CombatLogType.EFFECT_EXPIRED,
                            targetId = combatant.id
                        )
                    )
                }
                EffectDurationType.TURNS -> {
                    val nextTurns = effect.remainingTurns - 1
                    if (nextTurns <= 0) {
                        generatedLogs.add(
                            CombatLogEntry(
                                turn = roundNumber,
                                text = "⏳ Действие эффекта «${effect.name}» на [${combatant.displayName}] истекло.",
                                isPlayerAction = isPlayerSide,
                                logType = CombatLogType.EFFECT_EXPIRED,
                                targetId = combatant.id
                            )
                        )
                    } else {
                        updatedEffects.add(effect.copy(remainingTurns = nextTurns))
                    }
                }
                else -> {
                    // UNTIL_END_OF_TURN and ROUNDS preserved for other lifecycle hooks
                    updatedEffects.add(effect)
                }
            }
        }

        // 4. Decrement Ability Cooldowns
        val updatedCooldowns = combatant.abilityCooldowns.mapValues { (_, cd) ->
            (cd - 1).coerceAtLeast(0)
        }.filterValues { it > 0 }

        val intermediate = combatant.copy(
            currentHealth = currentHp,
            activeEffects = updatedEffects,
            abilityCooldowns = updatedCooldowns,
            specialCooldownTurns = (combatant.specialCooldownTurns - 1).coerceAtLeast(0)
        )

        val sanitized = sanitizeCombatantStatus(intermediate)
        return TurnStartEffectResult(sanitized, generatedLogs, extraAp)
    }

    /**
     * Ticks effect durations at the END of a combatant's turn.
     */
    fun processTurnEnd(combatant: Combatant, roundNumber: Int): Pair<Combatant, List<CombatLogEntry>> {
        if (combatant.isDefeated) return Pair(combatant, emptyList())

        val generatedLogs = mutableListOf<CombatLogEntry>()
        val updatedEffects = mutableListOf<CombatEffectInstance>()

        combatant.activeEffects.forEach { effect ->
            if (effect.durationType == EffectDurationType.UNTIL_END_OF_TURN) {
                generatedLogs.add(
                    CombatLogEntry(
                        turn = roundNumber,
                        text = "⏳ Эффект конца хода «${effect.name}» спадает с [${combatant.displayName}].",
                        isPlayerAction = combatant.team == CombatantTeam.PLAYER,
                        logType = CombatLogType.EFFECT_EXPIRED,
                        targetId = combatant.id
                    )
                )
            } else {
                updatedEffects.add(effect)
            }
        }

        val updated = sanitizeCombatantStatus(combatant.copy(activeEffects = updatedEffects))
        return Pair(updated, generatedLogs)
    }

    /**
     * Ticks round-based effects when advancing to a new round.
     */
    fun processRoundEnd(combatants: List<Combatant>, nextRoundNumber: Int): Pair<List<Combatant>, List<CombatLogEntry>> {
        val generatedLogs = mutableListOf<CombatLogEntry>()

        val updatedCombatants = combatants.map { combatant ->
            if (combatant.isDefeated) return@map combatant

            val updatedEffects = mutableListOf<CombatEffectInstance>()
            combatant.activeEffects.forEach { effect ->
                if (effect.durationType == EffectDurationType.ROUNDS) {
                    val nextRounds = effect.remainingRounds - 1
                    if (nextRounds <= 0) {
                        generatedLogs.add(
                            CombatLogEntry(
                                turn = nextRoundNumber,
                                text = "⏳ Раундовый эффект «${effect.name}» на [${combatant.displayName}] рассеялся.",
                                isPlayerAction = combatant.team == CombatantTeam.PLAYER,
                                logType = CombatLogType.EFFECT_EXPIRED,
                                targetId = combatant.id
                            )
                        )
                    } else {
                        updatedEffects.add(effect.copy(remainingRounds = nextRounds))
                    }
                } else {
                    updatedEffects.add(effect)
                }
            }

            sanitizeCombatantStatus(combatant.copy(activeEffects = updatedEffects))
        }

        return Pair(updatedCombatants, generatedLogs)
    }

    /**
     * Validates and harmonizes status flags (DEFENDING, STUNNED, ACTIVE) based on remaining active effects.
     */
    private fun sanitizeCombatantStatus(combatant: Combatant): Combatant {
        if (combatant.isDefeated) return combatant

        val hasDefendingEffect = combatant.activeEffects.any { it.effectType == CombatEffectType.DEFENDING }
        val hasStunEffect = combatant.activeEffects.any { it.effectType == CombatEffectType.STUN }

        var newStatus = combatant.status
        if (hasStunEffect) {
            newStatus = CombatantStatus.STUNNED
        } else if (hasDefendingEffect) {
            newStatus = CombatantStatus.DEFENDING
        } else if (combatant.status == CombatantStatus.DEFENDING || combatant.status == CombatantStatus.STUNNED) {
            newStatus = CombatantStatus.ACTIVE
        }

        val updatedDefenseStance = if (!hasDefendingEffect) 0 else combatant.defenseBonusStance

        return combatant.copy(
            status = newStatus,
            defenseBonusStance = updatedDefenseStance
        )
    }

    // ==========================================
    // Tactical Query Helpers (for AI and Engine)
    // ==========================================

    fun canApplyEffect(target: Combatant, effectType: CombatEffectType, stackingRule: EffectStackingRule): Boolean {
        if (target.isDefeated) return false
        val existing = target.getEffect(effectType) ?: return true
        return when (stackingRule) {
            EffectStackingRule.REPLACE, EffectStackingRule.REFRESH_DURATION -> true
            EffectStackingRule.STACK -> existing.stacks < existing.maxStacks
            EffectStackingRule.IGNORE_DUPLICATE -> false
        }
    }

    fun getRemainingDuration(target: Combatant, effectType: CombatEffectType): Int {
        val effect = target.getEffect(effectType) ?: return 0
        return when (effect.durationType) {
            EffectDurationType.UNTIL_NEXT_TURN, EffectDurationType.UNTIL_END_OF_TURN -> 1
            EffectDurationType.TURNS -> effect.remainingTurns
            EffectDurationType.ROUNDS -> effect.remainingRounds
        }
    }

    fun getStackCount(target: Combatant, effectType: CombatEffectType): Int {
        return target.getEffect(effectType)?.stacks ?: 0
    }

    fun hasActiveBuff(target: Combatant, effectType: CombatEffectType): Boolean {
        return target.activeEffects.any { it.effectType == effectType && (it.category == CombatEffectCategory.BUFF || it.category == CombatEffectCategory.SPECIAL) }
    }

    fun hasActiveDebuff(target: Combatant, effectType: CombatEffectType): Boolean {
        return target.activeEffects.any { it.effectType == effectType && (it.category == CombatEffectCategory.DEBUFF || it.category == CombatEffectCategory.CONTROL) }
    }
}
