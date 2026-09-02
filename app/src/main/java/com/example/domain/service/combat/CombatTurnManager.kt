package com.example.domain.service.combat

import com.example.domain.model.*

/**
 * Manages combat turns, queue progression, round transitions, status effect lifecycles, and victory/defeat evaluation.
 */
object CombatTurnManager {

    /**
     * Checks if battle outcome is reached (all enemies defeated or all players down).
     * If finished, transitions to VICTORY/DEFEAT and prepares an idempotent BattleResult.
     */
    fun evaluateBattleOutcome(state: CombatState): CombatState {
        if (state.isEnded && state.battleResult != null) {
            return state
        }

        val livingEnemies = state.combatants.filter { it.team == CombatantTeam.ENEMY && !it.isDefeated }
        val livingPlayers = state.combatants.filter { it.team == CombatantTeam.PLAYER && !it.isDefeated }

        if (livingEnemies.isEmpty()) {
            val victoryLog = CombatLogEntry(
                turn = state.roundNumber,
                text = "🎉 Победа! Все противники нейтрализованы. Поле боя зачищено.",
                isPlayerAction = true,
                logType = CombatLogType.VICTORY
            )
            val battleResult = BattleResult(
                combatId = state.id,
                result = CombatPhase.VICTORY,
                finalPlayerStates = state.combatants.filter { it.team == CombatantTeam.PLAYER },
                xpEarned = state.xpReward,
                bonusLoot = state.bonusLoot,
                sourceEventInstanceId = state.sourceEventId,
                isApplied = false
            )
            return state.copy(
                currentPhase = CombatPhase.VICTORY,
                battleResult = battleResult,
                logs = (state.logs + victoryLog).takeLast(CombatBalanceConfig.MAX_LOG_HISTORY_SIZE)
            )
        }

        if (livingPlayers.isEmpty()) {
            val defeatLog = CombatLogEntry(
                turn = state.roundNumber,
                text = "⚠️ Все бойцы отряда выведены из строя! Требуется срочная эвакуация.",
                isPlayerAction = false,
                logType = CombatLogType.DEFEAT
            )
            val battleResult = BattleResult(
                combatId = state.id,
                result = CombatPhase.DEFEAT,
                finalPlayerStates = state.combatants.filter { it.team == CombatantTeam.PLAYER },
                xpEarned = (state.xpReward / 3).coerceAtLeast(10),
                bonusLoot = GameResources(),
                sourceEventInstanceId = state.sourceEventId,
                isApplied = false
            )
            return state.copy(
                currentPhase = CombatPhase.DEFEAT,
                battleResult = battleResult,
                logs = (state.logs + defeatLog).takeLast(CombatBalanceConfig.MAX_LOG_HISTORY_SIZE)
            )
        }

        return state
    }

    /**
     * Advances the turn queue to the next active, living combatant.
     * Full Lifecycle:
     * 1. Check battle outcome
     * 2. Process turn-end on previous combatant
     * 3. Advance index or start new round
     * 4. Auto-skip defeated/incapacitated participants
     * 5. Process turn-start (AP recovery, duration tick, cooldown decrement)
     */
    fun advanceTurn(state: CombatState): CombatState {
        val postOutcomeState = evaluateBattleOutcome(state)
        if (postOutcomeState.isEnded) return postOutcomeState

        var currentCombatants = postOutcomeState.combatants
        val newLogs = postOutcomeState.logs.toMutableList()

        // 1. Process Turn-End on current actor before switching
        val previousActor = postOutcomeState.currentActiveCombatant
        if (previousActor != null && !previousActor.isDefeated) {
            val (actorAfterEnd, endLogs) = CombatEffectManager.processTurnEnd(previousActor, postOutcomeState.roundNumber)
            currentCombatants = currentCombatants.map { if (it.id == actorAfterEnd.id) actorAfterEnd else it }
            newLogs.addAll(endLogs)
        }

        var nextIndex = postOutcomeState.currentTurnIndex + 1
        var nextRound = postOutcomeState.roundNumber

        // 2. If index reaches end of turn order, transition to Next Round
        if (nextIndex >= postOutcomeState.turnOrder.size) {
            nextIndex = 0
            nextRound += 1

            // Process round-based effects
            val (combatantsAfterRound, roundLogs) = CombatEffectManager.processRoundEnd(currentCombatants, nextRound)
            currentCombatants = combatantsAfterRound
            newLogs.addAll(roundLogs)

            newLogs.add(
                CombatLogEntry(
                    turn = nextRound,
                    text = "━━━ Раунд $nextRound ━━━",
                    isPlayerAction = true,
                    logType = CombatLogType.ROUND_STARTED
                )
            )
        }

        // 3. Find next living, eligible combatant with auto-skip protection
        var searchAttempts = 0
        while (searchAttempts < postOutcomeState.turnOrder.size) {
            val combatantId = postOutcomeState.turnOrder.getOrNull(nextIndex)
            val candidate = currentCombatants.find { it.id == combatantId }

            if (candidate != null && !candidate.isDefeated) {
                // Process Turn Start: restore AP, tick effects & cooldowns
                val turnStartResult = CombatEffectManager.processTurnStart(candidate, nextRound)
                val startedCombatant = turnStartResult.combatant
                val startLogs = turnStartResult.logs
                val extraAp = turnStartResult.extraAp

                newLogs.addAll(startLogs)

                if (startedCombatant.isDefeated) {
                    // Died from DoT (Bleeding/Poison)
                    currentCombatants = currentCombatants.map {
                        if (it.id == startedCombatant.id) startedCombatant else it
                    }
                    val postDotState = evaluateBattleOutcome(
                        postOutcomeState.copy(
                            combatants = currentCombatants,
                            logs = newLogs.takeLast(CombatBalanceConfig.MAX_LOG_HISTORY_SIZE)
                        )
                    )
                    if (postDotState.isEnded) return postDotState

                    // Auto-skip to next
                    nextIndex = (nextIndex + 1) % postOutcomeState.turnOrder.size
                    if (nextIndex == 0) {
                        nextRound += 1
                    }
                    searchAttempts++
                    continue
                }

                val refreshedCombatant = startedCombatant.copy(
                    actionPoints = (startedCombatant.maxActionPoints + extraAp).coerceAtLeast(0)
                )

                currentCombatants = currentCombatants.map {
                    if (it.id == refreshedCombatant.id) refreshedCombatant else it
                }

                // If candidate is stunned, auto-pass after consuming turn
                if (refreshedCombatant.isStunned) {
                    val postStunState = postOutcomeState.copy(
                        combatants = currentCombatants,
                        currentTurnIndex = nextIndex,
                        roundNumber = nextRound,
                        logs = newLogs.takeLast(CombatBalanceConfig.MAX_LOG_HISTORY_SIZE)
                    )
                    // Advance to next since this turn was skipped due to stun
                    nextIndex = (nextIndex + 1) % postOutcomeState.turnOrder.size
                    if (nextIndex == 0) {
                        nextRound += 1
                    }
                    searchAttempts++
                    continue
                }

                val nextPhase = if (refreshedCombatant.team == CombatantTeam.PLAYER) {
                    CombatPhase.PLAYER_TURN
                } else {
                    CombatPhase.ENEMY_TURN
                }

                // Ensure selected target is valid for the new actor
                val currentTarget = currentCombatants.find { it.id == postOutcomeState.selectedTargetId }
                val validTargetId = if (currentTarget == null || currentTarget.isDefeated) {
                    if (refreshedCombatant.team == CombatantTeam.PLAYER) {
                        currentCombatants.firstOrNull { it.team == CombatantTeam.ENEMY && !it.isDefeated }?.id
                    } else {
                        currentCombatants.firstOrNull { it.team == CombatantTeam.PLAYER && !it.isDefeated }?.id
                    }
                } else {
                    postOutcomeState.selectedTargetId
                }

                return postOutcomeState.copy(
                    combatants = currentCombatants,
                    currentTurnIndex = nextIndex,
                    roundNumber = nextRound,
                    currentPhase = nextPhase,
                    selectedTargetId = validTargetId,
                    targetingAction = null,
                    logs = newLogs.takeLast(CombatBalanceConfig.MAX_LOG_HISTORY_SIZE)
                )
            }

            // Candidate is defeated/incapacitated -> auto-skip to next
            nextIndex = (nextIndex + 1) % postOutcomeState.turnOrder.size
            if (nextIndex == 0) {
                nextRound += 1
            }
            searchAttempts++
        }

        // If no active combatants found, evaluate final outcome
        return evaluateBattleOutcome(postOutcomeState)
    }

    /**
     * Helper to verify if a combatant can afford an action.
     */
    fun canAffordAction(combatant: Combatant, action: CombatAction): Boolean {
        return CombatActionExecutor.canAffordAction(combatant, action)
    }
}

