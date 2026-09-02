package com.example.domain.content.location

import com.example.domain.content.core.ContentTag
import com.example.domain.model.DangerLevel
import com.example.domain.model.LocationType
import com.example.domain.model.TerrainType

/**
 * Template definition for a local sub-area within a location.
 */
data class LocalAreaTemplate(
    val id: String,
    val namePatternRu: String,
    val typeNameRu: String,
    val descriptionTemplateRu: String = "",
    val isMandatory: Boolean = false,
    val weight: Float = 1.0f,
    val tags: Set<ContentTag> = emptySet(),
    val eventIds: List<String> = emptyList(),
    val lootTableId: String? = null
)

/**
 * Procedural template for generating diverse locations on the frontier map.
 */
data class LocationTemplate(
    val id: String,
    val type: LocationType,
    val namePrefixList: List<String> = emptyList(),
    val nameBaseList: List<String> = emptyList(),
    val nameSuffixList: List<String> = emptyList(),
    val descriptionTemplates: List<String> = emptyList(),
    val allowedTerrains: Set<TerrainType> = setOf(TerrainType.WASTELAND),
    val minDangerLevel: DangerLevel = DangerLevel.SAFE,
    val maxDangerLevel: DangerLevel = DangerLevel.HIGH,
    val mandatoryAreas: List<LocalAreaTemplate> = emptyList(),
    val optionalAreaPool: List<LocalAreaTemplate> = emptyList(),
    val minOptionalAreas: Int = 1,
    val maxOptionalAreas: Int = 3,
    val potentialLootKeywordsRu: List<String> = emptyList(),
    val observationTemplatesRu: List<String> = emptyList(),
    val threatTemplatesRu: List<String> = emptyList(),
    val visualAssetPool: List<String> = listOf("loc_station"),
    val eventTags: Set<ContentTag> = emptySet(),
    val lootTags: Set<ContentTag> = emptySet(),
    val tags: Set<ContentTag> = emptySet(),
    val baseWeight: Float = 100f,
    val isUnique: Boolean = false,
    val minDistanceKm: Int = 5,
    val maxDistanceKm: Int = 35
)
