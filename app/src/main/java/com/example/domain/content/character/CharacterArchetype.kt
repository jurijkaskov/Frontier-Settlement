package com.example.domain.content.character

import com.example.domain.content.core.ContentTag
import com.example.domain.model.CharacterRole
import com.example.domain.model.CharacterStatType
import com.example.domain.model.EquipmentSlotType

/**
 * Archetype template for procedural character generation.
 */
data class CharacterArchetype(
    val id: String,
    val role: CharacterRole,
    val titleRu: String,
    val bioTemplates: List<String> = emptyList(),
    val statWeights: Map<CharacterStatType, Float> = emptyMap(),
    val minStatBudget: Int = 45,
    val maxStatBudget: Int = 60,
    val preferredTraits: List<String> = emptyList(),
    val forbiddenTraits: List<String> = emptyList(),
    val specializations: List<String> = emptyList(),
    val defaultEquipment: Map<EquipmentSlotType, String> = emptyMap(),
    val avatarTags: List<String> = emptyList(),
    val tags: Set<ContentTag> = emptySet(),
    val baseWeight: Float = 1.0f
)
