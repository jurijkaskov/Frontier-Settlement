package com.example.domain.service.combat.ai

import com.example.domain.model.*

/**
 * Data catalog of preset enemy archetypes and templates.
 */
object EnemyTemplateCatalog {

    val RAIDER_SCOUT = EnemyTemplate(
        templateId = "tmpl_raider_scout",
        name = "Разведчик рейдеров",
        baseHealth = 40,
        baseAttack = 13,
        baseDefense = 4,
        baseInitiative = 14,
        avatarTag = "scout",
        aiProfileId = EnemyAIProfileCatalog.OPPORTUNIST.profileId,
        role = CharacterRole.SCOUT,
        abilityIds = listOf("skill_scout_mark"),
        dangerTier = 1
    )

    val RAIDER_GUARD = EnemyTemplate(
        templateId = "tmpl_raider_guard",
        name = "Щитовик рейдеров",
        baseHealth = 65,
        baseAttack = 10,
        baseDefense = 10,
        baseInitiative = 8,
        avatarTag = "guard",
        aiProfileId = EnemyAIProfileCatalog.CAUTIOUS.profileId,
        role = CharacterRole.ENGINEER,
        abilityIds = listOf("skill_engineer_fortify"),
        dangerTier = 1
    )

    val WASTELAND_SHAMAN = EnemyTemplate(
        templateId = "tmpl_wasteland_shaman",
        name = "Пустошный знахарь",
        baseHealth = 45,
        baseAttack = 9,
        baseDefense = 5,
        baseInitiative = 11,
        avatarTag = "medic",
        aiProfileId = EnemyAIProfileCatalog.SUPPORT.profileId,
        role = CharacterRole.MEDIC,
        abilityIds = listOf("skill_medic_heal", "skill_rally_focus"),
        dangerTier = 2
    )

    val RAIDER_BOSS = EnemyTemplate(
        templateId = "tmpl_raider_boss",
        name = "Главарь банды «Ржавые Псы»",
        baseHealth = 85,
        baseAttack = 17,
        baseDefense = 8,
        baseInitiative = 12,
        avatarTag = "boss",
        aiProfileId = EnemyAIProfileCatalog.AGGRESSIVE.profileId,
        role = CharacterRole.SOLDIER,
        abilityIds = listOf("skill_soldier_snipe", "skill_rally_focus"),
        dangerTier = 3
    )

    val DESERTER_MERC = EnemyTemplate(
        templateId = "tmpl_deserter_merc",
        name = "Одичалый дезертир",
        baseHealth = 50,
        baseAttack = 12,
        baseDefense = 6,
        baseInitiative = 10,
        avatarTag = "soldier",
        aiProfileId = EnemyAIProfileCatalog.BALANCED.profileId,
        role = CharacterRole.SOLDIER,
        abilityIds = listOf("skill_soldier_snipe"),
        dangerTier = 1
    )

    private val templateMap: Map<String, EnemyTemplate> = listOf(
        RAIDER_SCOUT,
        RAIDER_GUARD,
        WASTELAND_SHAMAN,
        RAIDER_BOSS,
        DESERTER_MERC
    ).associateBy { it.templateId }

    fun getTemplate(templateId: String?): EnemyTemplate? {
        if (templateId == null) return null
        return templateMap[templateId]
    }

    fun getAllTemplates(): List<EnemyTemplate> {
        return templateMap.values.toList()
    }

    /**
     * Instantiates a fully configured enemy [Combatant] from a template.
     */
    fun createCombatantFromTemplate(
        template: EnemyTemplate,
        idSuffix: String = "${(100..999).random()}"
    ): Combatant {
        return Combatant(
            id = "enemy_${template.templateId}_$idSuffix",
            team = CombatantTeam.ENEMY,
            displayName = template.name,
            enemyTemplateId = template.templateId,
            currentHealth = template.baseHealth,
            maxHealth = template.baseHealth,
            actionPoints = 4,
            maxActionPoints = 4,
            initiative = template.baseInitiative,
            attack = template.baseAttack,
            defense = template.baseDefense,
            status = CombatantStatus.ACTIVE,
            role = template.role,
            avatarTag = template.avatarTag,
            aiProfileId = template.aiProfileId,
            consumables = template.consumables
        )
    }
}
