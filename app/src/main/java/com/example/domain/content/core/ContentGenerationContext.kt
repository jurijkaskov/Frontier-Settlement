package com.example.domain.content.core

import com.example.domain.model.DangerLevel
import com.example.domain.model.LocationType

/**
 * Context container passed to all generators containing environmental parameters,
 * progress metrics, squad composition, and world state flags.
 */
data class ContentGenerationContext(
    val gameSeed: Long = 133742L,
    val currentGameDay: Int = 1,
    val locationType: LocationType = LocationType.VILLAGE,
    val locationId: String = "loc_base",
    val areaId: String? = null,
    val dangerLevel: DangerLevel = DangerLevel.LOW,
    val settlementLevel: Int = 1,
    val reputation: Int = 50,
    val factionRelations: Map<String, Int> = emptyMap(),
    val worldFlags: Map<String, Boolean> = emptyMap(),
    val researchedTechIds: Set<String> = emptySet(),
    val squadMemberRoles: Set<String> = emptySet(),
    val visitCount: Int = 0,
    val generationIndex: Int = 0,
    val tags: Set<ContentTag> = emptySet()
)
