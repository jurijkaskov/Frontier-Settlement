package com.example.domain.model

import com.example.domain.model.CharacterRole

/**
 * High-level state phase of the tactical combat encounter.
 */
enum class CombatPhase(val titleRu: String) {
    INITIALIZING("Инициализация боя"),
    PLAYER_TURN("Ход отряда"),
    ENEMY_TURN("Ход противника"),
    RESOLVING_ACTION("Расчёт действия"),
    ROUND_TRANSITION("Смена раунда"),
    VICTORY("Победа"),
    DEFEAT("Поражение"),
    RESOLVED("Бой завершён")
}

/**
 * Team affiliation of a combatant.
 */
enum class CombatantTeam(val titleRu: String) {
    PLAYER("Отряд игрока"),
    ENEMY("Враги"),
    ALLY("Союзник"),
    NEUTRAL("Нейтрал")
}

/**
 * Tactical status flags for combatants during combat.
 */
enum class CombatantStatus(val titleRu: String) {
    ACTIVE("В строю"),
    DEFENDING("В укрытии / Защитная стойка"),
    INCAPACITATED("Не может продолжать бой"),
    STUNNED("Оглушён"),
    DEFEATED("Нейтрализован")
}

/**
 * Categories of tactical actions available to combatants.
 */
enum class CombatActionType(val titleRu: String) {
    ATTACK("Атака"),
    DEFEND("Оборона"),
    SKILL("Спецнавык"),
    ITEM("Предмет"),
    PASS("Завершить ход")
}

/**
 * Targeting constraint for combat actions.
 */
enum class TargetType(val titleRu: String) {
    ENEMY("Одиночный враг"),
    ALLY("Одиночный союзник"),
    SELF("На себя"),
    ANY("Любая цель"),
    ALL_ENEMIES("Все враги"),
    ALL_ALLIES("Все союзники"),
    NONE("Без цели")
}

/**
 * High-level classification of combat effects.
 */
enum class CombatEffectCategory(val titleRu: String) {
    BUFF("Положительный эффект"),
    DEBUFF("Отрицательный эффект"),
    DEFENSIVE("Защитная стойка / Укрытие"),
    CONTROL("Контроль / Прерывание"),
    SPECIAL("Особое состояние / Утилити")
}

/**
 * Types of tactical combat effects.
 */
enum class CombatEffectType(
    val titleRu: String,
    val defaultCategory: CombatEffectCategory
) {
    DEFENDING("В укрытии / Стойка", CombatEffectCategory.DEFENSIVE),
    BUFF_ATTACK("Усиление атаки", CombatEffectCategory.BUFF),
    BUFF_DEFENSE("Укрепление брони", CombatEffectCategory.BUFF),
    BUFF_AP("Прилив сил (+ОД)", CombatEffectCategory.BUFF),
    BUFF_INITIATIVE("Ускорение реакции", CombatEffectCategory.BUFF),
    DEBUFF_ATTACK("Слабость (Снижение урона)", CombatEffectCategory.DEBUFF),
    DEBUFF_DEFENSE("Уязвимость брони", CombatEffectCategory.DEBUFF),
    DEBUFF_BLEED("Кровотечение", CombatEffectCategory.DEBUFF),
    DEBUFF_POISON("Отравление", CombatEffectCategory.DEBUFF),
    STUN("Оглушение", CombatEffectCategory.CONTROL),
    DISORIENT("Дезориентация", CombatEffectCategory.DEBUFF),
    REGENERATION("Регенерация", CombatEffectCategory.BUFF),
    FOCUS("Концентрация", CombatEffectCategory.SPECIAL)
}

/**
 * Lifetime rules for combat effects.
 */
enum class EffectDurationType {
    UNTIL_NEXT_TURN,
    UNTIL_END_OF_TURN,
    TURNS,
    ROUNDS
}

/**
 * Stacking behavior when applying duplicate effect types.
 */
enum class EffectStackingRule {
    REPLACE,
    REFRESH_DURATION,
    STACK,
    IGNORE_DUPLICATE
}

/**
 * Lifecycle trigger determining when periodic or special effects execute their logic.
 */
enum class EffectTickTrigger {
    ON_TURN_START,
    ON_TURN_END,
    ON_ROUND_END,
    PASSIVE
}

/**
 * Data-driven template defining a static combat effect configuration.
 */
data class CombatEffectDefinition(
    val id: String,
    val name: String,
    val description: String,
    val effectType: CombatEffectType,
    val category: CombatEffectCategory = effectType.defaultCategory,
    val durationType: EffectDurationType = EffectDurationType.TURNS,
    val defaultDuration: Int = 2,
    val defaultRounds: Int = 0,
    val stackingRule: EffectStackingRule = EffectStackingRule.REFRESH_DURATION,
    val maxStacks: Int = 1,
    val baseModifier: Int = 0,
    val powerMultiplier: Float = 1.0f,
    val tickDamage: Int = 0,
    val tickHeal: Int = 0,
    val tickTrigger: EffectTickTrigger = EffectTickTrigger.PASSIVE,
    val iconKey: String? = null
) {
    fun createInstance(
        sourceId: String,
        targetId: String,
        customModifier: Int? = null,
        customDuration: Int? = null,
        customStacks: Int = 1
    ): CombatEffectInstance {
        return CombatEffectInstance(
            instanceId = "eff_${id}_${System.currentTimeMillis()}_${(100..999).random()}",
            definitionId = id,
            effectType = effectType,
            category = category,
            name = name,
            description = description,
            sourceCombatantId = sourceId,
            targetCombatantId = targetId,
            durationType = durationType,
            remainingTurns = customDuration ?: defaultDuration,
            remainingRounds = defaultRounds,
            modifier = customModifier ?: baseModifier,
            powerMultiplier = powerMultiplier,
            stackingRule = stackingRule,
            stacks = customStacks.coerceIn(1, maxStacks),
            maxStacks = maxStacks,
            tickDamage = tickDamage,
            tickHeal = tickHeal,
            tickTrigger = tickTrigger,
            iconKey = iconKey
        )
    }
}

/**
 * Represents an active temporary status effect instance on a combatant.
 */
data class CombatEffectInstance(
    val instanceId: String,
    val effectType: CombatEffectType,
    val name: String,
    val description: String,
    val sourceCombatantId: String,
    val targetCombatantId: String,
    val definitionId: String? = null,
    val category: CombatEffectCategory = effectType.defaultCategory,
    val durationType: EffectDurationType = EffectDurationType.TURNS,
    val remainingTurns: Int = 1,
    val remainingRounds: Int = 0,
    val modifier: Int = 0,
    val powerMultiplier: Float = 1.0f,
    val stackingRule: EffectStackingRule = EffectStackingRule.REFRESH_DURATION,
    val stacks: Int = 1,
    val maxStacks: Int = 1,
    val tickDamage: Int = 0,
    val tickHeal: Int = 0,
    val tickTrigger: EffectTickTrigger = EffectTickTrigger.PASSIVE,
    val iconKey: String? = null
)

/**
 * Data-driven tactical special ability.
 */
data class CombatAbility(
    val abilityId: String,
    val name: String,
    val description: String,
    val apCost: Int = 2,
    val targetType: TargetType = TargetType.ENEMY,
    val requiredRole: CharacterRole? = null,
    val cooldownTurns: Int = 2,
    val powerMultiplier: Float = 1.0f,
    val healingAmount: Int = 0,
    val bonusDefenseGain: Int = 0,
    val appliedEffects: List<CombatEffectInstance> = emptyList(),
    val iconKey: String? = null
)

/**
 * Data-driven representation of a combat action.
 */
data class CombatAction(
    val id: String,
    val name: String,
    val description: String,
    val apCost: Int = 2,
    val targetType: TargetType = TargetType.ENEMY,
    val actionType: CombatActionType = CombatActionType.ATTACK,
    val requiredRole: CharacterRole? = null,
    val iconKey: String? = null,
    val powerMultiplier: Float = 1.0f,
    val bonusDefenseGain: Int = 0,
    val healingAmount: Int = 0,
    val cooldownTurns: Int = 0,
    val appliedEffects: List<CombatEffectInstance> = emptyList()
)

/**
 * Represents an individual participant in turn-based tactical combat.
 */
data class Combatant(
    val id: String,
    val team: CombatantTeam,
    val displayName: String,
    val characterId: String? = null,
    val enemyTemplateId: String? = null,
    val currentHealth: Int,
    val maxHealth: Int,
    val actionPoints: Int = 4,
    val maxActionPoints: Int = 4,
    val initiative: Int = 10,
    val attack: Int = 12,
    val defense: Int = 6,
    val status: CombatantStatus = CombatantStatus.ACTIVE,
    val defenseBonusStance: Int = 0,
    val role: CharacterRole? = null,
    val avatarTag: String? = null,
    val specialCooldownTurns: Int = 0,
    val activeEffects: List<CombatEffectInstance> = emptyList(),
    val abilityCooldowns: Map<String, Int> = emptyMap(),
    val aiProfileId: String? = null,
    val consumables: List<String> = emptyList()
) {
    val isDefeated: Boolean
        get() = currentHealth <= 0 || status == CombatantStatus.DEFEATED || status == CombatantStatus.INCAPACITATED

    val isStunned: Boolean
        get() = status == CombatantStatus.STUNNED || hasEffect(CombatEffectType.STUN)

    val canAct: Boolean
        get() = !isDefeated && !isStunned

    val hpFraction: Float
        get() = if (maxHealth > 0) (currentHealth.toFloat() / maxHealth.toFloat()).coerceIn(0f, 1f) else 0f

    val effectiveDefense: Int
        get() {
            var bonus = defenseBonusStance
            activeEffects.forEach { effect ->
                val mult = effect.stacks.coerceAtLeast(1)
                when (effect.effectType) {
                    CombatEffectType.DEFENDING, CombatEffectType.BUFF_DEFENSE -> bonus += (effect.modifier * mult)
                    CombatEffectType.DEBUFF_DEFENSE -> bonus -= (effect.modifier * mult)
                    else -> Unit
                }
            }
            return (defense + bonus).coerceAtLeast(0)
        }

    val effectiveAttack: Int
        get() {
            var bonus = 0
            var multiplier = 1.0f
            activeEffects.forEach { effect ->
                val mult = effect.stacks.coerceAtLeast(1)
                when (effect.effectType) {
                    CombatEffectType.BUFF_ATTACK, CombatEffectType.FOCUS -> {
                        bonus += (effect.modifier * mult)
                        multiplier *= effect.powerMultiplier
                    }
                    CombatEffectType.DEBUFF_ATTACK -> {
                        bonus -= (effect.modifier * mult)
                    }
                    else -> Unit
                }
            }
            return ((attack + bonus) * multiplier).toInt().coerceAtLeast(1)
        }

    val effectiveInitiative: Int
        get() {
            var bonus = 0
            activeEffects.forEach { effect ->
                val mult = effect.stacks.coerceAtLeast(1)
                when (effect.effectType) {
                    CombatEffectType.BUFF_INITIATIVE -> bonus += (effect.modifier * mult)
                    CombatEffectType.DISORIENT -> bonus -= (effect.modifier * mult)
                    else -> Unit
                }
            }
            return (initiative + bonus).coerceAtLeast(1)
        }

    fun hasEffect(type: CombatEffectType): Boolean {
        return activeEffects.any { it.effectType == type }
    }

    fun hasEffect(definitionId: String): Boolean {
        return activeEffects.any { it.definitionId == definitionId }
    }

    fun getEffect(type: CombatEffectType): CombatEffectInstance? {
        return activeEffects.firstOrNull { it.effectType == type }
    }

    fun getEffects(category: CombatEffectCategory): List<CombatEffectInstance> {
        return activeEffects.filter { it.category == category }
    }

    fun countBuffs(): Int {
        return activeEffects.count { it.category == CombatEffectCategory.BUFF || it.category == CombatEffectCategory.SPECIAL }
    }

    fun countDebuffs(): Int {
        return activeEffects.count { it.category == CombatEffectCategory.DEBUFF || it.category == CombatEffectCategory.CONTROL }
    }

    fun isAbilityOnCooldown(abilityId: String): Boolean {
        return (abilityCooldowns[abilityId] ?: 0) > 0
    }

    fun getAbilityRemainingCooldown(abilityId: String): Int {
        return abilityCooldowns[abilityId] ?: 0
    }

    fun toCombatFighter(): CombatFighter {
        return CombatFighter(
            id = id,
            name = displayName,
            hp = currentHealth,
            maxHp = maxHealth,
            attack = effectiveAttack,
            defense = effectiveDefense,
            isPlayerSide = team == CombatantTeam.PLAYER,
            isDefeated = isDefeated
        )
    }
}

/**
 * Legacy fighter model retained for backwards compatibility.
 */
data class CombatFighter(
    val id: String,
    val name: String,
    val hp: Int,
    val maxHp: Int,
    val attack: Int,
    val defense: Int,
    val isPlayerSide: Boolean,
    val isDefeated: Boolean = false
) {
    val hpFraction: Float
        get() = if (maxHp > 0) (hp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f) else 0f
}

/**
 * Categorized tactical combat log entry types.
 */
enum class CombatLogType {
    ROUND_STARTED,
    TURN_STARTED,
    ACTION_USED,
    EFFECT_APPLIED,
    EFFECT_EXPIRED,
    ITEM_USED,
    COMBATANT_INCAPACITATED,
    VICTORY,
    DEFEAT
}

/**
 * Structured log entry capturing an action or event in the tactical battle.
 */
data class CombatLogEntry(
    val turn: Int,
    val text: String,
    val isPlayerAction: Boolean,
    val logId: String = "log_${System.currentTimeMillis()}_${(0..9999).random()}",
    val logType: CombatLogType = CombatLogType.ACTION_USED,
    val actorId: String? = null,
    val targetId: String? = null,
    val value: Int = 0
)

/**
 * Structured outcome of an individual combat action.
 */
data class CombatActionResult(
    val success: Boolean,
    val actorId: String,
    val actionId: String,
    val targetIds: List<String> = emptyList(),
    val apSpent: Int = 0,
    val generatedEffects: List<CombatEffectInstance> = emptyList(),
    val healthChanges: Map<String, Int> = emptyMap(),
    val itemChanges: Map<String, Int> = emptyMap(),
    val logEntries: List<CombatLogEntry> = emptyList(),
    val combatEnded: Boolean = false,
    val outcome: CombatPhase? = null,
    val errorMessage: String? = null
)

/**
 * Structured final summary of a completed tactical battle.
 */
data class BattleResult(
    val combatId: String,
    val result: CombatPhase,
    val finalPlayerStates: List<Combatant>,
    val usedItems: Map<String, Int> = emptyMap(),
    val xpEarned: Int = 0,
    val bonusLoot: GameResources = GameResources(),
    val sourceEventInstanceId: String? = null,
    val isApplied: Boolean = false
)

/**
 * Master state for tactical turn-based combat.
 * Stored directly within [GameState.activeCombat] to ensure deterministic state across rotations,
 * navigation, and app restarts without rerolling.
 */
data class CombatState(
    val id: String,
    val encounterTitle: String = "Тактическая стычка",
    val locationId: String = "loc_base",
    val sourceEventId: String? = null,
    val sourceChoiceId: String? = null,
    val combatants: List<Combatant> = emptyList(),
    val turnOrder: List<String> = emptyList(),
    val currentTurnIndex: Int = 0,
    val roundNumber: Int = 1,
    val selectedTargetId: String? = null,
    val targetingAction: CombatAction? = null,
    val currentPhase: CombatPhase = CombatPhase.PLAYER_TURN,
    val logs: List<CombatLogEntry> = listOf(
        CombatLogEntry(
            turn = 1,
            text = "Столкновение началось! Оцените диспозицию и отдайте приказ.",
            isPlayerAction = true,
            logType = CombatLogType.TURN_STARTED
        )
    ),
    val instanceSeed: Long = 0L,
    val bonusLoot: GameResources = GameResources(),
    val xpReward: Int = 120,
    val battleResult: BattleResult? = null,
    val aiDecisionLogs: List<AIDecisionLog> = emptyList()
) {
    val lastAIDecisionLog: AIDecisionLog?
        get() = aiDecisionLogs.lastOrNull()
    val isVictory: Boolean
        get() = currentPhase == CombatPhase.VICTORY ||
                (combatants.isNotEmpty() && combatants.filter { it.team == CombatantTeam.ENEMY }.all { it.isDefeated })

    val isDefeat: Boolean
        get() = currentPhase == CombatPhase.DEFEAT ||
                (combatants.isNotEmpty() && combatants.filter { it.team == CombatantTeam.PLAYER }.all { it.isDefeated })

    val isEnded: Boolean
        get() = isVictory || isDefeat || currentPhase == CombatPhase.RESOLVED

    val currentTurn: Int
        get() = roundNumber

    val activeCombatantId: String?
        get() = turnOrder.getOrNull(currentTurnIndex)

    val currentActiveCombatant: Combatant?
        get() = activeCombatantId?.let { cid -> combatants.find { it.id == cid } }

    val isPlayerTurn: Boolean
        get() = currentActiveCombatant?.team == CombatantTeam.PLAYER && !isEnded

    val selectedTarget: Combatant?
        get() = selectedTargetId?.let { tid -> combatants.find { it.id == tid } }

    val isTargetingMode: Boolean
        get() = targetingAction != null

    val playerSquad: List<CombatFighter>
        get() = combatants.filter { it.team == CombatantTeam.PLAYER }.map { it.toCombatFighter() }

    val enemies: List<CombatFighter>
        get() = combatants.filter { it.team == CombatantTeam.ENEMY }.map { it.toCombatFighter() }

    val livingEnemies: List<Combatant>
        get() = combatants.filter { it.team == CombatantTeam.ENEMY && !it.isDefeated }

    val livingPlayers: List<Combatant>
        get() = combatants.filter { it.team == CombatantTeam.PLAYER && !it.isDefeated }
}

