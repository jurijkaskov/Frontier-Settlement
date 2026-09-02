package com.example.domain.service.combat

import com.example.domain.model.*

/**
 * Validation result for combat targeting.
 */
sealed class TargetValidationResult {
    object Valid : TargetValidationResult()
    data class Invalid(val reason: String) : TargetValidationResult()
}

/**
 * Centralized domain validation for tactical combat targets.
 */
object CombatTargetValidator {

    /**
     * Checks whether a specific target combatant is valid for an action invoked by an actor.
     */
    fun validateTarget(
        action: CombatAction,
        actor: Combatant,
        target: Combatant?,
        allCombatants: List<Combatant>
    ): TargetValidationResult {
        if (action.targetType == TargetType.NONE) {
            return TargetValidationResult.Valid
        }

        if (action.targetType == TargetType.SELF) {
            if (target == null || target.id == actor.id) {
                return TargetValidationResult.Valid
            }
            return TargetValidationResult.Invalid("Действие применяется исключительно на себя.")
        }

        if (target == null) {
            return TargetValidationResult.Invalid("Цель не выбрана.")
        }

        if (target.isDefeated) {
            return TargetValidationResult.Invalid("Цель уже выведена из строя.")
        }

        return when (action.targetType) {
            TargetType.ENEMY -> {
                val isEnemy = (actor.team == CombatantTeam.PLAYER && target.team == CombatantTeam.ENEMY) ||
                        (actor.team == CombatantTeam.ENEMY && target.team == CombatantTeam.PLAYER)
                if (isEnemy) TargetValidationResult.Valid
                else TargetValidationResult.Invalid("Цель должна быть противником.")
            }
            TargetType.ALLY -> {
                val isAlly = (actor.team == target.team) || (actor.team == CombatantTeam.PLAYER && target.team == CombatantTeam.ALLY)
                if (isAlly) TargetValidationResult.Valid
                else TargetValidationResult.Invalid("Цель должна быть союзником.")
            }
            TargetType.ANY -> TargetValidationResult.Valid
            TargetType.ALL_ENEMIES, TargetType.ALL_ALLIES -> TargetValidationResult.Valid
            TargetType.SELF, TargetType.NONE -> TargetValidationResult.Valid
        }
    }

    /**
     * Filters list of all combatants and returns only valid targets for the specified action.
     */
    fun getValidTargets(
        action: CombatAction,
        actor: Combatant,
        allCombatants: List<Combatant>
    ): List<Combatant> {
        return when (action.targetType) {
            TargetType.NONE -> emptyList()
            TargetType.SELF -> listOf(actor).filter { !it.isDefeated }
            TargetType.ENEMY -> {
                val targetTeam = if (actor.team == CombatantTeam.PLAYER) CombatantTeam.ENEMY else CombatantTeam.PLAYER
                allCombatants.filter { it.team == targetTeam && !it.isDefeated }
            }
            TargetType.ALLY -> {
                allCombatants.filter { (it.team == actor.team || (actor.team == CombatantTeam.PLAYER && it.team == CombatantTeam.ALLY)) && !it.isDefeated }
            }
            TargetType.ANY -> allCombatants.filter { !it.isDefeated }
            TargetType.ALL_ENEMIES -> {
                val targetTeam = if (actor.team == CombatantTeam.PLAYER) CombatantTeam.ENEMY else CombatantTeam.PLAYER
                allCombatants.filter { it.team == targetTeam && !it.isDefeated }
            }
            TargetType.ALL_ALLIES -> {
                allCombatants.filter { (it.team == actor.team || (actor.team == CombatantTeam.PLAYER && it.team == CombatantTeam.ALLY)) && !it.isDefeated }
            }
        }
    }

    /**
     * Resolves default or fallback target for an action if none is actively selected.
     */
    fun resolveDefaultTarget(
        action: CombatAction,
        actor: Combatant,
        preferredTargetId: String?,
        allCombatants: List<Combatant>
    ): Combatant? {
        val validTargets = getValidTargets(action, actor, allCombatants)
        if (validTargets.isEmpty()) return null

        val preferred = validTargets.find { it.id == preferredTargetId }
        return preferred ?: validTargets.firstOrNull()
    }
}
