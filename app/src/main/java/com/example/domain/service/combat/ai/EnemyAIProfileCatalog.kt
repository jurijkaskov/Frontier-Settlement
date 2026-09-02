package com.example.domain.service.combat.ai

import com.example.domain.model.*

/**
 * Catalog of standard AI profiles representing distinct tactical archetypes.
 */
object EnemyAIProfileCatalog {

    val AGGRESSIVE = EnemyAIProfile(
        profileId = "ai_aggressive",
        name = "Безудержный штурмовик",
        archetype = EnemyAIArchetype.AGGRESSIVE,
        aggression = 0.95f,
        defensePreference = 0.1f,
        targetPreference = TargetPreference.LOWEST_HP,
        abilityUsageWeight = 1.3f,
        itemUsage = ItemUsagePreference.RARELY,
        riskTolerance = 0.85f,
        randomnessWeight = 0.08f,
        minScoreThreshold = 5.0f,
        traits = listOf(EnemyAITrait.BRAVE, EnemyAITrait.RELENTLESS)
    )

    val CAUTIOUS = EnemyAIProfile(
        profileId = "ai_cautious",
        name = "Осторожный защитник",
        archetype = EnemyAIArchetype.CAUTIOUS,
        aggression = 0.35f,
        defensePreference = 0.85f,
        targetPreference = TargetPreference.BALANCED,
        abilityUsageWeight = 0.9f,
        itemUsage = ItemUsagePreference.FREQUENT,
        riskTolerance = 0.2f,
        randomnessWeight = 0.06f,
        minScoreThreshold = 5.0f,
        traits = listOf(EnemyAITrait.COWARDLY, EnemyAITrait.TACTICAL)
    )

    val OPPORTUNIST = EnemyAIProfile(
        profileId = "ai_opportunist",
        name = "Охотник за слабыми",
        archetype = EnemyAIArchetype.OPPORTUNIST,
        aggression = 0.75f,
        defensePreference = 0.3f,
        targetPreference = TargetPreference.LOWEST_HP,
        abilityUsageWeight = 1.5f,
        itemUsage = ItemUsagePreference.NORMAL,
        riskTolerance = 0.6f,
        randomnessWeight = 0.1f,
        minScoreThreshold = 5.0f,
        traits = listOf(EnemyAITrait.TACTICAL)
    )

    val SUPPORT = EnemyAIProfile(
        profileId = "ai_support",
        name = "Полевой медик / шаман",
        archetype = EnemyAIArchetype.SUPPORT,
        aggression = 0.25f,
        defensePreference = 0.45f,
        targetPreference = TargetPreference.SUPPORT_ROLE,
        abilityUsageWeight = 1.7f,
        itemUsage = ItemUsagePreference.FREQUENT,
        riskTolerance = 0.3f,
        randomnessWeight = 0.05f,
        minScoreThreshold = 5.0f,
        traits = listOf(EnemyAITrait.PROTECTIVE, EnemyAITrait.TACTICAL)
    )

    val BALANCED = EnemyAIProfile(
        profileId = "ai_balanced",
        name = "Опытный наёмник",
        archetype = EnemyAIArchetype.BALANCED,
        aggression = 0.6f,
        defensePreference = 0.45f,
        targetPreference = TargetPreference.BALANCED,
        abilityUsageWeight = 1.0f,
        itemUsage = ItemUsagePreference.NORMAL,
        riskTolerance = 0.5f,
        randomnessWeight = 0.1f,
        minScoreThreshold = 5.0f,
        traits = listOf(EnemyAITrait.TACTICAL)
    )

    private val profileMap: Map<String, EnemyAIProfile> = listOf(
        AGGRESSIVE,
        CAUTIOUS,
        OPPORTUNIST,
        SUPPORT,
        BALANCED
    ).associateBy { it.profileId }

    fun getProfile(profileId: String?): EnemyAIProfile {
        if (profileId == null) return BALANCED
        return profileMap[profileId] ?: BALANCED
    }

    fun getAllProfiles(): List<EnemyAIProfile> {
        return profileMap.values.toList()
    }
}
