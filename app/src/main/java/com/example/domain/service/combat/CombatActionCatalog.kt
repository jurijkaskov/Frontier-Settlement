package com.example.domain.service.combat

import com.example.domain.model.*

object CombatActionCatalog {

    val BASIC_ATTACK = CombatAction(
        id = "act_attack",
        name = "Штурм / Выстрел",
        description = "Базовая атака по выбранному противнику.",
        apCost = CombatBalanceConfig.BASIC_ATTACK_AP_COST,
        targetType = TargetType.ENEMY,
        actionType = CombatActionType.ATTACK,
        powerMultiplier = 1.0f
    )

    val DEFEND = CombatAction(
        id = "act_defend",
        name = "Защитная стойка",
        description = "Занять укрытие (+6 к броне до следующего хода).",
        apCost = CombatBalanceConfig.DEFEND_AP_COST,
        targetType = TargetType.SELF,
        actionType = CombatActionType.DEFEND,
        bonusDefenseGain = CombatBalanceConfig.BASE_DEFEND_BONUS_DEFENSE,
        appliedEffects = listOf(
            CombatEffectCatalog.COVER_DEFENSE.createInstance("", "")
        )
    )

    val END_TURN = CombatAction(
        id = "act_pass",
        name = "Завершить ход",
        description = "Передать инициативу следующему бойцу в очереди.",
        apCost = CombatBalanceConfig.END_TURN_AP_COST,
        targetType = TargetType.NONE,
        actionType = CombatActionType.PASS
    )

    // Role-Specific Tactical Skills
    val SOLDIER_SNIPE = CombatAction(
        id = "skill_soldier_snipe",
        name = "Прицельный выстрел",
        description = "Мощный бронебойный выстрел (1.8x урона). Кулдаун: 2 хода.",
        apCost = CombatBalanceConfig.ROLE_ABILITY_AP_COST,
        targetType = TargetType.ENEMY,
        actionType = CombatActionType.SKILL,
        requiredRole = CharacterRole.SOLDIER,
        powerMultiplier = 1.8f,
        cooldownTurns = CombatBalanceConfig.DEFAULT_ABILITY_COOLDOWN_TURNS
    )

    val SCOUT_MARK = CombatAction(
        id = "skill_scout_mark",
        name = "Метка уязвимости",
        description = "Выстрел по уязвимым местам (1.2x урона, -4 к броне цели). Кулдаун: 2 хода.",
        apCost = CombatBalanceConfig.ROLE_ABILITY_AP_COST,
        targetType = TargetType.ENEMY,
        actionType = CombatActionType.SKILL,
        requiredRole = CharacterRole.SCOUT,
        powerMultiplier = 1.2f,
        cooldownTurns = CombatBalanceConfig.DEFAULT_ABILITY_COOLDOWN_TURNS,
        appliedEffects = listOf(
            CombatEffectCatalog.VULNERABILITY_MARK.createInstance("", "")
        )
    )

    val MEDIC_HEAL = CombatAction(
        id = "skill_medic_heal",
        name = "Полевая реанимация",
        description = "Экстренная перевязка и стимуляция (+35 HP, регенерация +10 HP/ход). Кулдаун: 2 хода.",
        apCost = CombatBalanceConfig.ROLE_ABILITY_AP_COST,
        targetType = TargetType.ALLY,
        actionType = CombatActionType.SKILL,
        requiredRole = CharacterRole.MEDIC,
        healingAmount = 35,
        cooldownTurns = CombatBalanceConfig.DEFAULT_ABILITY_COOLDOWN_TURNS,
        appliedEffects = listOf(
            CombatEffectCatalog.REGENERATION.createInstance("", "")
        )
    )

    val ENGINEER_FORTIFY = CombatAction(
        id = "skill_engineer_fortify",
        name = "Укрепление снаряжения",
        description = "Установка дополнительного бронелиста (+8 к броне на 2 хода). Кулдаун: 2 хода.",
        apCost = CombatBalanceConfig.ROLE_ABILITY_AP_COST,
        targetType = TargetType.ALLY,
        actionType = CombatActionType.SKILL,
        requiredRole = CharacterRole.ENGINEER,
        bonusDefenseGain = 8,
        cooldownTurns = CombatBalanceConfig.DEFAULT_ABILITY_COOLDOWN_TURNS,
        appliedEffects = listOf(
            CombatEffectCatalog.FORTIFIED_ARMOR.createInstance("", "")
        )
    )

    val SCAVENGER_FLASH = CombatAction(
        id = "skill_scavenger_flash",
        name = "Светошумовая вспышка",
        description = "Ослепление цели с уроном (1.3x урона, -3 к инициативе). Кулдаун: 2 хода.",
        apCost = CombatBalanceConfig.ROLE_ABILITY_AP_COST,
        targetType = TargetType.ENEMY,
        actionType = CombatActionType.SKILL,
        requiredRole = CharacterRole.SCAVENGER,
        powerMultiplier = 1.3f,
        cooldownTurns = CombatBalanceConfig.DEFAULT_ABILITY_COOLDOWN_TURNS,
        appliedEffects = listOf(
            CombatEffectCatalog.DISORIENT.createInstance("", "")
        )
    )

    val RALLY_FOCUS = CombatAction(
        id = "skill_rally_focus",
        name = "Собраться с силами",
        description = "Концентрация внимания (+4 к силе атаки на 2 хода). Кулдаун: 3 хода.",
        apCost = 1,
        targetType = TargetType.SELF,
        actionType = CombatActionType.SKILL,
        cooldownTurns = CombatBalanceConfig.RALLY_ABILITY_COOLDOWN_TURNS,
        appliedEffects = listOf(
            CombatEffectCatalog.BATTLE_RAGE.createInstance("", "")
        )
    )

    // Enemy Specific Tactical Actions
    val ENEMY_POISON_BITE = CombatAction(
        id = "act_enemy_poison_bite",
        name = "Членистый укус / Токсин",
        description = "Атака с нанесением токсичного отравления (8 урона/ход на 2 хода).",
        apCost = 2,
        targetType = TargetType.ENEMY,
        actionType = CombatActionType.SKILL,
        powerMultiplier = 1.0f,
        appliedEffects = listOf(
            CombatEffectCatalog.POISON.createInstance("", "")
        )
    )

    val ENEMY_SERRATED_SLASH = CombatAction(
        id = "act_enemy_serrated_slash",
        name = "Рвущий удар",
        description = "Наносит глубокое кровотечение (6 урона/ход на 3 хода, стакается).",
        apCost = 2,
        targetType = TargetType.ENEMY,
        actionType = CombatActionType.SKILL,
        powerMultiplier = 1.1f,
        appliedEffects = listOf(
            CombatEffectCatalog.BLEEDING.createInstance("", "")
        )
    )

    val ENEMY_SUPPRESSION = CombatAction(
        id = "act_enemy_suppression",
        name = "Подавляющий огонь",
        description = "Прижимает цель огнем, снижая ее атаку на 4 на 2 хода.",
        apCost = 2,
        targetType = TargetType.ENEMY,
        actionType = CombatActionType.SKILL,
        powerMultiplier = 0.9f,
        appliedEffects = listOf(
            CombatEffectCatalog.WEAKNESS.createInstance("", "")
        )
    )

    val ENEMY_STUN_GRENADE = CombatAction(
        id = "act_enemy_stun_grenade",
        name = "Оглушающая граната",
        description = "Взрыв оглушает цель, лишая ее следующего хода.",
        apCost = 3,
        targetType = TargetType.ENEMY,
        actionType = CombatActionType.SKILL,
        powerMultiplier = 0.6f,
        cooldownTurns = 3,
        appliedEffects = listOf(
            CombatEffectCatalog.STUN.createInstance("", "")
        )
    )

    /**
     * Resolves the primary tactical skill appropriate for the given character role.
     */
    fun getSkillForRole(role: CharacterRole?): CombatAction {
        return when (role) {
            CharacterRole.SOLDIER -> SOLDIER_SNIPE
            CharacterRole.SCOUT -> SCOUT_MARK
            CharacterRole.MEDIC -> MEDIC_HEAL
            CharacterRole.ENGINEER -> ENGINEER_FORTIFY
            CharacterRole.SCAVENGER -> SCAVENGER_FLASH
            null -> SOLDIER_SNIPE
        }
    }

    /**
     * Returns list of all available actions for a specific combatant.
     */
    fun getAvailableActionsForCombatant(combatant: Combatant): List<CombatAction> {
        val actions = mutableListOf(BASIC_ATTACK, DEFEND)
        val roleSkill = getSkillForRole(combatant.role)
        actions.add(roleSkill)
        actions.add(RALLY_FOCUS)
        actions.add(END_TURN)
        return actions
    }
}
