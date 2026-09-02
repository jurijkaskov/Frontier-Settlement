package com.example.domain.service.combat

import com.example.domain.model.*

/**
 * Standard registry and catalog of all combat status effect definitions.
 * Provides data-driven templates and factory helpers to instantiate effect instances.
 */
object CombatEffectCatalog {

    val COVER_DEFENSE = CombatEffectDefinition(
        id = "eff_cover_defense",
        name = "В укрытии",
        description = "+6 к броне до начала следующего хода",
        effectType = CombatEffectType.DEFENDING,
        category = CombatEffectCategory.DEFENSIVE,
        durationType = EffectDurationType.UNTIL_NEXT_TURN,
        defaultDuration = 1,
        stackingRule = EffectStackingRule.REFRESH_DURATION,
        baseModifier = 6,
        tickTrigger = EffectTickTrigger.PASSIVE,
        iconKey = "shield"
    )

    val FORTIFIED_ARMOR = CombatEffectDefinition(
        id = "eff_fortified_armor",
        name = "Укреплённая броня",
        description = "+8 к защите на 2 хода",
        effectType = CombatEffectType.BUFF_DEFENSE,
        category = CombatEffectCategory.BUFF,
        durationType = EffectDurationType.TURNS,
        defaultDuration = 2,
        stackingRule = EffectStackingRule.REFRESH_DURATION,
        baseModifier = 8,
        tickTrigger = EffectTickTrigger.PASSIVE,
        iconKey = "shield_plus"
    )

    val VULNERABILITY_MARK = CombatEffectDefinition(
        id = "eff_vulnerability_mark",
        name = "Метка уязвимости",
        description = "-4 к защите цели на 2 хода",
        effectType = CombatEffectType.DEBUFF_DEFENSE,
        category = CombatEffectCategory.DEBUFF,
        durationType = EffectDurationType.TURNS,
        defaultDuration = 2,
        stackingRule = EffectStackingRule.REFRESH_DURATION,
        baseModifier = 4,
        tickTrigger = EffectTickTrigger.PASSIVE,
        iconKey = "crosshair"
    )

    val BATTLE_RAGE = CombatEffectDefinition(
        id = "eff_battle_rage",
        name = "Боевой раж",
        description = "+4 к силе атаки на 2 хода",
        effectType = CombatEffectType.BUFF_ATTACK,
        category = CombatEffectCategory.BUFF,
        durationType = EffectDurationType.TURNS,
        defaultDuration = 2,
        stackingRule = EffectStackingRule.REFRESH_DURATION,
        baseModifier = 4,
        tickTrigger = EffectTickTrigger.PASSIVE,
        iconKey = "swords"
    )

    val BLEEDING = CombatEffectDefinition(
        id = "eff_bleeding",
        name = "Кровотечение",
        description = "Теряет 6 HP в начале хода (до 3 стаков)",
        effectType = CombatEffectType.DEBUFF_BLEED,
        category = CombatEffectCategory.DEBUFF,
        durationType = EffectDurationType.TURNS,
        defaultDuration = 3,
        stackingRule = EffectStackingRule.STACK,
        maxStacks = 3,
        tickDamage = 6,
        tickTrigger = EffectTickTrigger.ON_TURN_START,
        iconKey = "drop"
    )

    val POISON = CombatEffectDefinition(
        id = "eff_poison",
        name = "Отравление",
        description = "Теряет 8 HP в начале хода",
        effectType = CombatEffectType.DEBUFF_POISON,
        category = CombatEffectCategory.DEBUFF,
        durationType = EffectDurationType.TURNS,
        defaultDuration = 2,
        stackingRule = EffectStackingRule.REFRESH_DURATION,
        tickDamage = 8,
        tickTrigger = EffectTickTrigger.ON_TURN_START,
        iconKey = "biohazard"
    )

    val REGENERATION = CombatEffectDefinition(
        id = "eff_regeneration",
        name = "Регенерация",
        description = "Восстанавливает +10 HP в начале хода",
        effectType = CombatEffectType.REGENERATION,
        category = CombatEffectCategory.BUFF,
        durationType = EffectDurationType.TURNS,
        defaultDuration = 2,
        stackingRule = EffectStackingRule.REFRESH_DURATION,
        maxStacks = 2,
        tickHeal = 10,
        tickTrigger = EffectTickTrigger.ON_TURN_START,
        iconKey = "heart_pulse"
    )

    val STUN = CombatEffectDefinition(
        id = "eff_stun",
        name = "Оглушение",
        description = "Боец не может совершать действия и пропускает ход",
        effectType = CombatEffectType.STUN,
        category = CombatEffectCategory.CONTROL,
        durationType = EffectDurationType.UNTIL_NEXT_TURN,
        defaultDuration = 1,
        stackingRule = EffectStackingRule.IGNORE_DUPLICATE,
        tickTrigger = EffectTickTrigger.ON_TURN_START,
        iconKey = "stun"
    )

    val WEAKNESS = CombatEffectDefinition(
        id = "eff_weakness",
        name = "Слабость",
        description = "-4 к силе атаки на 2 хода",
        effectType = CombatEffectType.DEBUFF_ATTACK,
        category = CombatEffectCategory.DEBUFF,
        durationType = EffectDurationType.TURNS,
        defaultDuration = 2,
        stackingRule = EffectStackingRule.REFRESH_DURATION,
        baseModifier = 4,
        tickTrigger = EffectTickTrigger.PASSIVE,
        iconKey = "broken_sword"
    )

    val ADRENALINE_SURGE = CombatEffectDefinition(
        id = "eff_adrenaline",
        name = "Прилив адреналина",
        description = "+1 дополнительное ОД в начале следующего хода",
        effectType = CombatEffectType.BUFF_AP,
        category = CombatEffectCategory.BUFF,
        durationType = EffectDurationType.UNTIL_NEXT_TURN,
        defaultDuration = 1,
        stackingRule = EffectStackingRule.REFRESH_DURATION,
        baseModifier = 1,
        tickTrigger = EffectTickTrigger.ON_TURN_START,
        iconKey = "lightning"
    )

    val DISORIENT = CombatEffectDefinition(
        id = "eff_disorient",
        name = "Дезориентация",
        description = "-3 к инициативе цели на 2 хода",
        effectType = CombatEffectType.DISORIENT,
        category = CombatEffectCategory.DEBUFF,
        durationType = EffectDurationType.TURNS,
        defaultDuration = 2,
        stackingRule = EffectStackingRule.REFRESH_DURATION,
        baseModifier = 3,
        tickTrigger = EffectTickTrigger.PASSIVE,
        iconKey = "spiral"
    )

    val FOCUS_AIM = CombatEffectDefinition(
        id = "eff_focus_aim",
        name = "Прицельная фокусировка",
        description = "+5 к атаке и +15% к урону на 1 ход",
        effectType = CombatEffectType.FOCUS,
        category = CombatEffectCategory.SPECIAL,
        durationType = EffectDurationType.UNTIL_END_OF_TURN,
        defaultDuration = 1,
        stackingRule = EffectStackingRule.REPLACE,
        baseModifier = 5,
        powerMultiplier = 1.15f,
        tickTrigger = EffectTickTrigger.PASSIVE,
        iconKey = "target"
    )

    private val allDefinitions = listOf(
        COVER_DEFENSE,
        FORTIFIED_ARMOR,
        VULNERABILITY_MARK,
        BATTLE_RAGE,
        BLEEDING,
        POISON,
        REGENERATION,
        STUN,
        WEAKNESS,
        ADRENALINE_SURGE,
        DISORIENT,
        FOCUS_AIM
    )

    fun getById(definitionId: String?): CombatEffectDefinition? {
        if (definitionId == null) return null
        return allDefinitions.firstOrNull { it.id == definitionId }
    }

    fun getByEffectType(type: CombatEffectType): CombatEffectDefinition? {
        return allDefinitions.firstOrNull { it.effectType == type }
    }
}
