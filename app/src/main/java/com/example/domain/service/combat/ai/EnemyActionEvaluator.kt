package com.example.domain.service.combat.ai

import com.example.domain.model.*
import com.example.domain.service.combat.CombatActionCatalog
import com.example.domain.service.combat.CombatActionExecutor
import com.example.domain.service.combat.CombatTargetValidator
import java.util.Random

/**
 * Generates and utility-scores potential combat action candidates for an AI combatant.
 */
object EnemyActionEvaluator {

    /**
     * Evaluates all possible valid actions and targets for the AI actor, returning scored candidates.
     */
    fun evaluateCandidates(
        actor: Combatant,
        state: CombatState,
        profile: EnemyAIProfile = EnemyAIProfileCatalog.getProfile(actor.aiProfileId),
        seed: Long = state.instanceSeed + (state.roundNumber * 100L) + (state.currentTurnIndex * 10L),
        stepIndex: Int = 0,
        difficulty: AIDifficulty = AIDifficulty.NORMAL
    ): List<AIActionCandidate> {
        val random = Random(seed + stepIndex)
        val availableActions = getAvailableActionsForCombatant(actor)
        val candidates = mutableListOf<AIActionCandidate>()

        for (action in availableActions) {
            // Check if actor has enough AP, not on cooldown, etc.
            val (canExec, _) = CombatActionExecutor.canExecuteAction(actor, action)
            if (!canExec) continue

            val validTargets = CombatTargetValidator.getValidTargets(action, actor, state.combatants)

            if (validTargets.isEmpty()) {
                if (action.targetType == TargetType.NONE || action.targetType == TargetType.SELF) {
                    val candidate = scoreCandidate(
                        actor = actor,
                        target = if (action.targetType == TargetType.SELF) actor else null,
                        action = action,
                        profile = profile,
                        state = state,
                        random = random,
                        difficulty = difficulty
                    )
                    candidates.add(candidate)
                }
            } else {
                for (target in validTargets) {
                    val candidate = scoreCandidate(
                        actor = actor,
                        target = target,
                        action = action,
                        profile = profile,
                        state = state,
                        random = random,
                        difficulty = difficulty
                    )
                    candidates.add(candidate)
                }
            }
        }

        // Always ensure END_TURN is available as a fallback option
        val endTurnCandidate = AIActionCandidate(
            action = CombatActionCatalog.END_TURN,
            targetId = null,
            targetName = null,
            baseScore = 5.0f,
            modifiers = emptyMap(),
            finalScore = 5.0f,
            explanation = "Завершить ход при отсутствии выгодных действий"
        )
        candidates.add(endTurnCandidate)

        return candidates.sortedByDescending { it.finalScore }
    }

    private fun scoreCandidate(
        actor: Combatant,
        target: Combatant?,
        action: CombatAction,
        profile: EnemyAIProfile,
        state: CombatState,
        random: Random,
        difficulty: AIDifficulty
    ): AIActionCandidate {
        var baseScore = 20.0f
        val modifiers = mutableMapOf<String, Float>()
        val explanations = mutableListOf<String>()

        when (action.actionType) {
            CombatActionType.ATTACK -> {
                baseScore = profile.aggression * 60.0f
                explanations.add("Агрессия: ${(profile.aggression * 100).toInt()}%")

                if (target != null) {
                    val targetEval = EnemyTargetEvaluator.evaluateTarget(actor, target, action, profile, state)
                    modifiers["Оценка цели"] = targetEval.score
                    explanations.add("Цель [${target.displayName}]: ${targetEval.explanation}")

                    if (targetEval.isKillPossible) {
                        modifiers["Шанс на устранение"] = 50.0f
                        explanations.add("Возможно устранение цели (+50)")
                    }
                }
            }

            CombatActionType.SKILL -> {
                baseScore = profile.abilityUsageWeight * 65.0f
                explanations.add("Приоритет навыков: ${(profile.abilityUsageWeight * 100).toInt()}%")

                // Distinguish offensive vs supportive skills
                if (action.healingAmount > 0) {
                    // Healing
                    if (target != null) {
                        val targetEval = EnemyTargetEvaluator.evaluateTarget(actor, target, action, profile, state)
                        modifiers["Оценка исцеления"] = targetEval.score
                        explanations.add("Лечение [${target.displayName}]: ${targetEval.explanation}")
                    }
                } else if (action.bonusDefenseGain > 0 && target != null) {
                    // Fortify ally
                    val targetEval = EnemyTargetEvaluator.evaluateTarget(actor, target, action, profile, state)
                    modifiers["Оценка укрепления"] = targetEval.score
                    explanations.add("Броня [${target.displayName}]: ${targetEval.explanation}")
                } else if (action.targetType == TargetType.SELF) {
                    // Self buff (e.g. Rally Focus)
                    val alreadyBuffed = actor.activeEffects.any { it.effectType == CombatEffectType.BUFF_ATTACK }
                    if (alreadyBuffed) {
                        modifiers["Повторный бафф"] = -70.0f
                        explanations.add("Бафф уже действует (-70)")
                    } else {
                        modifiers["Боевой раж"] = 30.0f
                        explanations.add("Усиление атаки (+30)")
                    }
                } else if (target != null) {
                    // Offensive skill (e.g. Snipe or Scout Mark)
                    val targetEval = EnemyTargetEvaluator.evaluateTarget(actor, target, action, profile, state)
                    modifiers["Оценка тактической атаки"] = targetEval.score
                    explanations.add("Навык на [${target.displayName}]: ${targetEval.explanation}")

                    if (action.powerMultiplier > 1.3f) {
                        modifiers["Высокий урон"] = 20.0f
                        explanations.add("Высокий множитель урона (+20)")
                    }
                }
            }

            CombatActionType.DEFEND -> {
                baseScore = profile.defensePreference * 40.0f
                explanations.add("Оборонительный уклон: ${(profile.defensePreference * 100).toInt()}%")

                // Check if already defending
                val alreadyDefending = actor.activeEffects.any { it.effectType == CombatEffectType.DEFENDING }
                if (alreadyDefending) {
                    modifiers["Повторная стойка"] = -90.0f
                    explanations.add("Уже находится в укрытии (-90)")
                }

                // Wounded bonus for defense
                if (actor.hpFraction <= 0.40f) {
                    val lowHpBonus = when (profile.archetype) {
                        EnemyAIArchetype.CAUTIOUS -> 50.0f
                        EnemyAIArchetype.SUPPORT -> 35.0f
                        EnemyAIArchetype.BALANCED -> 30.0f
                        EnemyAIArchetype.OPPORTUNIST -> 15.0f
                        EnemyAIArchetype.AGGRESSIVE -> 5.0f
                    }
                    modifiers["Низкое здоровье"] = lowHpBonus
                    explanations.add("Опасный уровень HP (+${lowHpBonus.toInt()})")
                } else if (actor.hpFraction >= 0.85f) {
                    modifiers["Полное здоровье"] = -25.0f
                    explanations.add("Высокое здоровье (-25)")
                }

                // Leftover 1 AP efficiency bonus
                if (actor.actionPoints == 1) {
                    modifiers["Эффективность 1 ОД"] = 25.0f
                    explanations.add("Полезный расход остатка ОД (+25)")
                }
            }

            CombatActionType.PASS -> {
                baseScore = 5.0f
                explanations.add("Завершение хода")
            }

            CombatActionType.ITEM -> {
                baseScore = 15.0f
            }
        }

        // Add Trait modifiers
        for (trait in profile.traits) {
            when (trait) {
                EnemyAITrait.BRAVE -> {
                    if (action.actionType == CombatActionType.ATTACK || action.actionType == CombatActionType.SKILL) {
                        modifiers["Черта: Бесстрашный"] = (modifiers["Черта: Бесстрашный"] ?: 0f) + 10.0f
                    }
                }
                EnemyAITrait.COWARDLY -> {
                    if (action.actionType == CombatActionType.DEFEND && actor.hpFraction < 0.6f) {
                        modifiers["Черта: Осторожный"] = (modifiers["Черта: Осторожный"] ?: 0f) + 20.0f
                    }
                }
                EnemyAITrait.RELENTLESS -> {
                    if (action.actionType == CombatActionType.ATTACK) {
                        modifiers["Черта: Неумолимый"] = (modifiers["Черта: Неумолимый"] ?: 0f) + 15.0f
                    }
                }
                EnemyAITrait.PROTECTIVE -> {
                    if (action.healingAmount > 0 || action.bonusDefenseGain > 0) {
                        modifiers["Черта: Защитник"] = (modifiers["Черта: Защитник"] ?: 0f) + 15.0f
                    }
                }
                EnemyAITrait.TACTICAL -> {
                    if (action.actionType == CombatActionType.SKILL) {
                        modifiers["Черта: Тактик"] = (modifiers["Черта: Тактик"] ?: 0f) + 10.0f
                    }
                }
            }
        }

        // Controlled deterministic jitter
        val jitterRange = profile.randomnessWeight * difficulty.randomnessScale * 15.0f
        val jitter = ((random.nextFloat() * 2f) - 1.0f) * jitterRange
        if (jitter.toInt() != 0) {
            modifiers["Вариативность"] = jitter
        }

        val totalModifiers = modifiers.values.sum()
        val finalScore = (baseScore + totalModifiers).coerceAtLeast(0.0f)

        return AIActionCandidate(
            action = action,
            targetId = target?.id,
            targetName = target?.displayName,
            baseScore = baseScore,
            modifiers = modifiers,
            finalScore = finalScore,
            explanation = explanations.joinToString("; ")
        )
    }

    /**
     * Gathers all valid ability and basic action options for a combatant.
     */
    fun getAvailableActionsForCombatant(combatant: Combatant): List<CombatAction> {
        val actions = mutableListOf(
            CombatActionCatalog.BASIC_ATTACK,
            CombatActionCatalog.DEFEND
        )

        // Add role tactical skill if available
        val roleSkill = CombatActionCatalog.getSkillForRole(combatant.role)
        actions.add(roleSkill)

        // Add Rally Focus if applicable
        actions.add(CombatActionCatalog.RALLY_FOCUS)

        // Add end turn
        actions.add(CombatActionCatalog.END_TURN)

        return actions.distinctBy { it.id }
    }
}
