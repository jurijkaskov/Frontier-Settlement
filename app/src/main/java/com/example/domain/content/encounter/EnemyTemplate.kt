package com.example.domain.content.encounter

import com.example.domain.content.core.ContentTag
import com.example.domain.model.CharacterRole
import com.example.domain.model.DangerLevel

/**
 * Data-driven template defining an enemy combatant unit.
 */
data class EnemyTemplate(
    val id: String,
    val nameRu: String,
    val descriptionRu: String = "",
    val avatarTag: String = "enemy_raider",
    val role: CharacterRole = CharacterRole.SOLDIER,
    val aiProfileId: String = "ai_balanced",
    val baseHp: Int = 45,
    val baseAttack: Int = 12,
    val baseDefense: Int = 5,
    val baseInitiative: Int = 10,
    val statVariancePercent: Float = 0.15f,
    val dangerTier: DangerLevel = DangerLevel.LOW,
    val actionAbilityIds: List<String> = emptyList(),
    val visualAssetId: String = "enemy_raider",
    val tags: Set<ContentTag> = emptySet()
)
