package com.example.domain.content.encounter

import com.example.domain.content.core.ContentTag
import com.example.domain.model.DangerLevel
import com.example.domain.model.LocationType

/**
 * Weighted entry of an enemy template within an encounter pool.
 */
data class EnemyPoolEntry(
    val enemyTemplateId: String,
    val weight: Float = 1.0f,
    val minCount: Int = 0,
    val maxCount: Int = 2
)

/**
 * Tactical combat encounter template specifying enemy compositions,
 * narrative encounter title, and loot table reference.
 */
data class EncounterTemplate(
    val id: String,
    val titleRu: String,
    val descriptionRu: String = "",
    val allowedLocationTypes: Set<LocationType> = emptySet(),
    val minDangerLevel: DangerLevel = DangerLevel.SAFE,
    val maxDangerLevel: DangerLevel = DangerLevel.EXTREME,
    val enemyPool: List<EnemyPoolEntry> = emptyList(),
    val minEnemies: Int = 1,
    val maxEnemies: Int = 3,
    val lootTableId: String? = null,
    val baseRewardXp: Int = 80,
    val tags: Set<ContentTag> = emptySet(),
    val baseWeight: Float = 100f
)
