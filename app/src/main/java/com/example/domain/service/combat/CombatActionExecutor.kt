package com.example.domain.service.combat

import com.example.domain.model.*
import java.util.Random

/**
 * Unified execution pipeline for all tactical actions in combat (attacks, skills, defenses, items, pass).
 * Pipeline: Validate -> Resolve -> Apply -> Log -> Evaluate Outcome -> Advance Turn if needed.
 */
object CombatActionExecutor {

    data class ExecutionResult(
        val updatedCombatState: CombatState,
        val actionResult: CombatActionResult,
        val updatedGameState: GameState? = null
    )

    /**
     * Checks if a combatant can afford the AP cost of an action.
     */
    fun canAffordAction(combatant: Combatant, action: CombatAction): Boolean {
        if (action.actionType == CombatActionType.PASS) return true
        return combatant.actionPoints >= action.apCost
    }

    /**
     * Checks if a combatant can execute a specific action (AP + cooldown + status).
     */
    fun canExecuteAction(combatant: Combatant, action: CombatAction): Pair<Boolean, String?> {
        if (combatant.isDefeated) {
            return Pair(false, "Боец выведен из строя.")
        }
        if (combatant.status == CombatantStatus.STUNNED) {
            return Pair(false, "Боец оглушён и пропускает ход.")
        }
        if (!canAffordAction(combatant, action)) {
            return Pair(false, "Недостаточно ОД! Требуется: ${action.apCost}, доступно: ${combatant.actionPoints} ОД.")
        }
        if (action.cooldownTurns > 0 && combatant.isAbilityOnCooldown(action.id)) {
            val remaining = combatant.getAbilityRemainingCooldown(action.id)
            return Pair(false, "Способность на перезарядке (ещё $remaining хода).")
        }
        return Pair(true, null)
    }

    /**
     * Executes any CombatAction through the standard pipeline.
     */
    fun executeAction(
        state: CombatState,
        action: CombatAction,
        targetId: String? = state.selectedTargetId,
        gameState: GameState? = null,
        seed: Long = state.instanceSeed + (state.roundNumber * 31L) + state.currentTurnIndex
    ): ExecutionResult {
        val actor = state.currentActiveCombatant
            ?: return createFailedResult(state, action, "Нет активного бойца в очереди.")

        if (state.isEnded) {
            return createFailedResult(state, action, "Бой уже завершён.")
        }

        // 1. Validation: Capability, AP and Cooldown
        val (canExec, errorReason) = canExecuteAction(actor, action)
        if (!canExec) {
            return createFailedResult(state, action, errorReason ?: "Невозможно выполнить действие.")
        }

        // 2. Validation: Targeting
        val target = CombatTargetValidator.resolveDefaultTarget(
            action = action,
            actor = actor,
            preferredTargetId = targetId,
            allCombatants = state.combatants
        )

        val targetValidation = CombatTargetValidator.validateTarget(
            action = action,
            actor = actor,
            target = target,
            allCombatants = state.combatants
        )

        if (targetValidation is TargetValidationResult.Invalid) {
            return createFailedResult(state, action, targetValidation.reason)
        }

        val random = Random(seed)
        var currentCombatants = state.combatants
        val newLogs = state.logs.toMutableList()
        val generatedEffects = mutableListOf<CombatEffectInstance>()
        val healthChanges = mutableMapOf<String, Int>()

        // 3. Resolve Mechanics by Action Type
        when (action.actionType) {
            CombatActionType.ATTACK, CombatActionType.SKILL -> {
                if (action.healingAmount > 0 && target != null) {
                    val nonNullTarget: Combatant = target
                    // Healing
                    val healedHp = (nonNullTarget.currentHealth + action.healingAmount).coerceAtMost(nonNullTarget.maxHealth)
                    val actualGain = healedHp - nonNullTarget.currentHealth
                    healthChanges[nonNullTarget.id] = actualGain

                    var targetWithEffects: Combatant = nonNullTarget.copy(currentHealth = healedHp)
                    action.appliedEffects.forEach { eff ->
                        val boundEffect = eff.copy(
                            instanceId = "eff_${eff.effectType}_${System.currentTimeMillis()}_${(100..999).random()}",
                            sourceCombatantId = actor.id,
                            targetCombatantId = nonNullTarget.id
                        )
                        generatedEffects.add(boundEffect)
                        targetWithEffects = CombatEffectManager.applyEffect(targetWithEffects, boundEffect)
                    }

                    currentCombatants = currentCombatants.map {
                        if (it.id == nonNullTarget.id) targetWithEffects else it
                    }

                    newLogs.add(
                        CombatLogEntry(
                            turn = state.roundNumber,
                            text = "💉 ${actor.displayName} применяет «${action.name}» на [${nonNullTarget.displayName}], восстанавливая +$actualGain HP ($healedHp/${nonNullTarget.maxHealth}).",
                            isPlayerAction = actor.team == CombatantTeam.PLAYER,
                            logType = CombatLogType.ACTION_USED,
                            actorId = actor.id,
                            targetId = nonNullTarget.id,
                            value = actualGain
                        )
                    )
                } else if (action.bonusDefenseGain > 0 && target != null) {
                    val nonNullTarget: Combatant = target
                    // Defensive Buff on target
                    var targetWithEffects: Combatant = nonNullTarget
                    val effectsToApply = if (action.appliedEffects.isNotEmpty()) {
                        action.appliedEffects
                    } else {
                        listOf(
                            CombatEffectCatalog.FORTIFIED_ARMOR.createInstance(
                                sourceId = actor.id,
                                targetId = nonNullTarget.id,
                                customModifier = action.bonusDefenseGain
                            )
                        )
                    }

                    effectsToApply.forEach { eff ->
                        val boundEffect = eff.copy(
                            instanceId = "eff_${eff.effectType}_${System.currentTimeMillis()}_${(100..999).random()}",
                            sourceCombatantId = actor.id,
                            targetCombatantId = nonNullTarget.id
                        )
                        generatedEffects.add(boundEffect)
                        targetWithEffects = CombatEffectManager.applyEffect(targetWithEffects, boundEffect)
                    }

                    currentCombatants = currentCombatants.map {
                        if (it.id == nonNullTarget.id) targetWithEffects else it
                    }

                    newLogs.add(
                        CombatLogEntry(
                            turn = state.roundNumber,
                            text = "🛡️ ${actor.displayName} применяет «${action.name}» на [${nonNullTarget.displayName}] (+${action.bonusDefenseGain} к броне на 2 хода).",
                            isPlayerAction = actor.team == CombatantTeam.PLAYER,
                            logType = CombatLogType.EFFECT_APPLIED,
                            actorId = actor.id,
                            targetId = nonNullTarget.id
                        )
                    )
                } else if (target != null) {
                    val nonNullTarget: Combatant = target
                    // Offensive Attack / Strike
                    val effectiveAtk = (actor.effectiveAttack * action.powerMultiplier).toInt()
                    val targetDef = nonNullTarget.effectiveDefense
                    val baseDamage = (effectiveAtk - (targetDef * CombatBalanceConfig.DEFENSE_DAMAGE_MITIGATION_FACTOR).toInt())
                        .coerceAtLeast(CombatBalanceConfig.BASE_MIN_DAMAGE)

                    val isCrit = random.nextInt(100) < CombatBalanceConfig.CRIT_CHANCE_PERCENT
                    val critBonus = if (isCrit) CombatBalanceConfig.CRIT_DAMAGE_MULTIPLIER else 1.0f
                    val finalDamage = (baseDamage * critBonus).toInt()

                    val remainingHp = (nonNullTarget.currentHealth - finalDamage).coerceAtLeast(0)
                    val isDefeated = remainingHp <= 0
                    healthChanges[nonNullTarget.id] = -finalDamage

                    // Apply any rider effects (e.g. Scout Mark or Flashbang)
                    var targetWithEffects: Combatant = nonNullTarget
                    action.appliedEffects.forEach { eff ->
                        val boundEffect = eff.copy(
                            instanceId = "eff_${eff.effectType}_${System.currentTimeMillis()}",
                            sourceCombatantId = actor.id,
                            targetCombatantId = nonNullTarget.id
                        )
                        generatedEffects.add(boundEffect)
                        targetWithEffects = CombatEffectManager.applyEffect(targetWithEffects, boundEffect)
                    }

                    val updatedStatus = if (isDefeated) {
                        if (nonNullTarget.team == CombatantTeam.PLAYER) CombatantStatus.INCAPACITATED else CombatantStatus.DEFEATED
                    } else targetWithEffects.status

                    currentCombatants = currentCombatants.map { c ->
                        if (c.id == nonNullTarget.id) {
                            targetWithEffects.copy(
                                currentHealth = remainingHp,
                                status = updatedStatus
                            )
                        } else c
                    }

                    val critText = if (isCrit) " (КРИТИЧЕСКИЙ УДАР!)" else ""
                    val logText = if (isDefeated) {
                        "💥 ${actor.displayName} наносит $finalDamage урона$critText! [${target.displayName}] выведен из строя!"
                    } else {
                        "🎯 ${actor.displayName} атакует [${target.displayName}] действием «${action.name}», нанося $finalDamage урона$critText ($remainingHp/${target.maxHealth} HP)."
                    }

                    newLogs.add(
                        CombatLogEntry(
                            turn = state.roundNumber,
                            text = logText,
                            isPlayerAction = actor.team == CombatantTeam.PLAYER,
                            logType = if (isDefeated) CombatLogType.COMBATANT_INCAPACITATED else CombatLogType.ACTION_USED,
                            actorId = actor.id,
                            targetId = target.id,
                            value = finalDamage
                        )
                    )
                } else if (action.appliedEffects.isNotEmpty()) {
                    // Self-applied buff (e.g. Rally / Focus)
                    var actorWithEffects = actor
                    action.appliedEffects.forEach { eff ->
                        val boundEffect = eff.copy(
                            instanceId = "eff_${eff.effectType}_${System.currentTimeMillis()}",
                            sourceCombatantId = actor.id,
                            targetCombatantId = actor.id
                        )
                        generatedEffects.add(boundEffect)
                        actorWithEffects = CombatEffectManager.applyEffect(actorWithEffects, boundEffect)
                    }

                    currentCombatants = currentCombatants.map {
                        if (it.id == actor.id) actorWithEffects else it
                    }

                    newLogs.add(
                        CombatLogEntry(
                            turn = state.roundNumber,
                            text = "⚡ ${actor.displayName} применяет «${action.name}».",
                            isPlayerAction = actor.team == CombatantTeam.PLAYER,
                            logType = CombatLogType.EFFECT_APPLIED,
                            actorId = actor.id,
                            targetId = actor.id
                        )
                    )
                }
            }

            CombatActionType.DEFEND -> {
                // Defensive stance
                val effectInstance = CombatEffectInstance(
                    instanceId = "eff_def_${actor.id}_${System.currentTimeMillis()}",
                    effectType = CombatEffectType.DEFENDING,
                    name = "В укрытии",
                    description = "+${action.bonusDefenseGain} к броне",
                    sourceCombatantId = actor.id,
                    targetCombatantId = actor.id,
                    durationType = EffectDurationType.UNTIL_NEXT_TURN,
                    remainingTurns = 1,
                    modifier = action.bonusDefenseGain,
                    stackingRule = EffectStackingRule.REFRESH_DURATION
                )
                generatedEffects.add(effectInstance)

                currentCombatants = currentCombatants.map {
                    if (it.id == actor.id) {
                        val withEff = CombatEffectManager.applyEffect(it, effectInstance)
                        withEff.copy(defenseBonusStance = it.defenseBonusStance + action.bonusDefenseGain)
                    } else it
                }

                newLogs.add(
                    CombatLogEntry(
                        turn = state.roundNumber,
                        text = "🛡️ ${actor.displayName} переходит в защитную стойку (+${action.bonusDefenseGain} к броне).",
                        isPlayerAction = actor.team == CombatantTeam.PLAYER,
                        logType = CombatLogType.ACTION_USED,
                        actorId = actor.id,
                        targetId = actor.id
                    )
                )
            }

            CombatActionType.PASS -> {
                newLogs.add(
                    CombatLogEntry(
                        turn = state.roundNumber,
                        text = "⏱️ ${actor.displayName} завершает свой ход.",
                        isPlayerAction = actor.team == CombatantTeam.PLAYER,
                        logType = CombatLogType.ACTION_USED,
                        actorId = actor.id
                    )
                )
            }

            CombatActionType.ITEM -> Unit
        }

        // 4. Deduct AP & Set Cooldowns on Actor
        val remainingAp = if (action.actionType == CombatActionType.PASS) {
            0
        } else {
            (actor.actionPoints - action.apCost).coerceAtLeast(0)
        }

        val updatedAbilityCooldowns = if (action.cooldownTurns > 0) {
            actor.abilityCooldowns + (action.id to action.cooldownTurns)
        } else {
            actor.abilityCooldowns
        }

        currentCombatants = currentCombatants.map {
            if (it.id == actor.id) {
                it.copy(
                    actionPoints = remainingAp,
                    abilityCooldowns = updatedAbilityCooldowns
                )
            } else it
        }

        var postActionState = state.copy(
            combatants = currentCombatants,
            targetingAction = null,
            logs = newLogs.takeLast(CombatBalanceConfig.MAX_LOG_HISTORY_SIZE)
        )

        // 5. Evaluate Battle Outcome (Victory / Defeat)
        postActionState = CombatTurnManager.evaluateBattleOutcome(postActionState)

        val combatEnded = postActionState.isEnded

        // 6. Check Turn Progression: if AP exhausted or action was PASS, advance turn
        if (remainingAp <= 0 && !combatEnded) {
            postActionState = CombatTurnManager.advanceTurn(postActionState)
        }

        val actionResult = CombatActionResult(
            success = true,
            actorId = actor.id,
            actionId = action.id,
            targetIds = listOfNotNull(target?.id),
            apSpent = if (action.actionType == CombatActionType.PASS) actor.actionPoints else action.apCost,
            generatedEffects = generatedEffects,
            healthChanges = healthChanges,
            logEntries = newLogs.takeLast(1),
            combatEnded = combatEnded,
            outcome = if (combatEnded) postActionState.currentPhase else null
        )

        return ExecutionResult(
            updatedCombatState = postActionState,
            actionResult = actionResult,
            updatedGameState = gameState
        )
    }

    /**
     * Executes consumable item usage through the unified pipeline.
     */
    fun executeItem(
        state: CombatState,
        itemId: String,
        targetId: String? = state.selectedTargetId,
        gameState: GameState?
    ): ExecutionResult {
        val actor = state.currentActiveCombatant
            ?: return createFailedResult(state, CombatActionCatalog.BASIC_ATTACK, "Нет активного бойца.")

        val apCost = CombatBalanceConfig.USE_ITEM_AP_COST
        if (actor.actionPoints < apCost) {
            return createFailedResult(state, CombatActionCatalog.BASIC_ATTACK, "Недостаточно ОД для предмета ($apCost ОД).")
        }

        val target = state.combatants.find { it.id == (targetId ?: actor.id) && it.team == CombatantTeam.PLAYER && !it.isDefeated }
            ?: actor

        var currentCombatants = state.combatants
        val newLogs = state.logs.toMutableList()
        val healthChanges = mutableMapOf<String, Int>()

        val (itemName, effectLog) = when {
            itemId.contains("firstaid", ignoreCase = true) || itemId.contains("medkit", ignoreCase = true) -> {
                val healed = (target.currentHealth + 40).coerceAtMost(target.maxHealth)
                val diff = healed - target.currentHealth
                healthChanges[target.id] = diff
                // Cleanses debuffs and applies Regeneration
                val cleansed = CombatEffectManager.cleanseEffects(target, CombatEffectCategory.DEBUFF)
                val withRegen = CombatEffectManager.applyEffect(
                    cleansed,
                    CombatEffectCatalog.REGENERATION.createInstance(actor.id, target.id)
                )
                currentCombatants = currentCombatants.map {
                    if (it.id == target.id) withRegen.copy(currentHealth = healed) else it
                }
                Pair("Аптечка первой помощи", "снимает негативные эффекты, восстанавливает +$diff HP [${target.displayName}] и даёт регенерацию")
            }
            itemId.contains("bandage", ignoreCase = true) -> {
                val healed = (target.currentHealth + 20).coerceAtMost(target.maxHealth)
                val diff = healed - target.currentHealth
                healthChanges[target.id] = diff
                // Cleanses Bleeding and applies Regeneration
                val withoutBleed = CombatEffectManager.removeEffectsByType(target, CombatEffectType.DEBUFF_BLEED)
                val withRegen = CombatEffectManager.applyEffect(
                    withoutBleed,
                    CombatEffectCatalog.REGENERATION.createInstance(actor.id, target.id, customDuration = 1)
                )
                currentCombatants = currentCombatants.map {
                    if (it.id == target.id) withRegen.copy(currentHealth = healed) else it
                }
                Pair("Стерильный бинт", "останавливает кровотечение и дает +$diff HP ($healed/${target.maxHealth})")
            }
            itemId.contains("repair", ignoreCase = true) || itemId.contains("tool", ignoreCase = true) -> {
                val fortifyEffect = CombatEffectCatalog.FORTIFIED_ARMOR.createInstance(actor.id, target.id)
                val updatedTarget = CombatEffectManager.applyEffect(target, fortifyEffect)
                currentCombatants = currentCombatants.map {
                    if (it.id == target.id) updatedTarget else it
                }
                Pair("Ремкомплект брони", "укрепляет пластины брони (+8 к защите на 2 хода)")
            }
            else -> {
                val healed = (target.currentHealth + 25).coerceAtMost(target.maxHealth)
                val diff = healed - target.currentHealth
                healthChanges[target.id] = diff
                val adrenaline = CombatEffectCatalog.ADRENALINE_SURGE.createInstance(actor.id, target.id)
                val updatedTarget = CombatEffectManager.applyEffect(target, adrenaline)
                currentCombatants = currentCombatants.map {
                    if (it.id == target.id) updatedTarget.copy(currentHealth = healed) else it
                }
                Pair("Полевой стимулятор", "восстанавливает силы (+ $diff HP, +1 ОД на след. ход)")
            }
        }

        newLogs.add(
            CombatLogEntry(
                turn = state.roundNumber,
                text = "🎒 ${actor.displayName} использует «$itemName»: $effectLog.",
                isPlayerAction = true,
                logType = CombatLogType.ITEM_USED,
                actorId = actor.id,
                targetId = target.id
            )
        )

        // Deduct AP
        val remainingAp = (actor.actionPoints - apCost).coerceAtLeast(0)
        currentCombatants = currentCombatants.map {
            if (it.id == actor.id) it.copy(actionPoints = remainingAp) else it
        }

        // Deduct 1 item from inventory
        val updatedInventory = gameState?.inventoryItems?.map {
            if (it.id == itemId && it.quantity > 1) it.copy(quantity = it.quantity - 1)
            else it
        }?.filter { !(it.id == itemId && it.quantity <= 1) }

        val updatedGameState = gameState?.let {
            if (updatedInventory != null) it.copy(inventoryItems = updatedInventory) else it
        }

        var postActionState = state.copy(
            combatants = currentCombatants,
            targetingAction = null,
            logs = newLogs.takeLast(CombatBalanceConfig.MAX_LOG_HISTORY_SIZE)
        )

        if (remainingAp <= 0 && !postActionState.isEnded) {
            postActionState = CombatTurnManager.advanceTurn(postActionState)
        }

        val actionResult = CombatActionResult(
            success = true,
            actorId = actor.id,
            actionId = "item_$itemId",
            targetIds = listOf(target.id),
            apSpent = apCost,
            healthChanges = healthChanges,
            itemChanges = mapOf(itemId to -1),
            logEntries = newLogs.takeLast(1),
            combatEnded = postActionState.isEnded,
            outcome = if (postActionState.isEnded) postActionState.currentPhase else null
        )

        return ExecutionResult(
            updatedCombatState = postActionState,
            actionResult = actionResult,
            updatedGameState = updatedGameState
        )
    }

    private fun createFailedResult(
        state: CombatState,
        action: CombatAction,
        errorMessage: String
    ): ExecutionResult {
        val result = CombatActionResult(
            success = false,
            actorId = state.activeCombatantId ?: "",
            actionId = action.id,
            errorMessage = errorMessage
        )
        return ExecutionResult(state, result, null)
    }
}
