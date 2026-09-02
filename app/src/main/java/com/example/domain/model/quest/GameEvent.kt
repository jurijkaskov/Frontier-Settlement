package com.example.domain.model.quest

import com.example.domain.model.*

/**
 * Domain GameEvent representing world state changes, user interactions, or expedition milestones.
 * Used by QuestProgressProcessor to update objectives idempotently without tightly coupling
 * quest logic to UI or disparate screen code.
 */
sealed interface GameEvent {
    data class ResourceStockUpdated(val type: ResourceType, val currentAmount: Int, val delta: Int = 0) : GameEvent
    data class ResourceDelivered(val questId: String, val objectiveId: String, val type: ResourceType, val amount: Int) : GameEvent
    data class ItemObtained(val itemId: String, val count: Int = 1) : GameEvent
    data class ItemDelivered(val questId: String, val objectiveId: String, val itemId: String) : GameEvent
    data class LocationVisited(val locationId: String) : GameEvent
    data class LocationExplored(val locationId: String, val explorationProgress: Int) : GameEvent
    data class AreaExplored(val locationId: String, val areaId: String) : GameEvent
    data class EventResolved(val eventId: String, val choiceId: String, val wasSuccess: Boolean) : GameEvent
    data class WorldFlagChanged(val flag: String, val value: Boolean) : GameEvent
    data class CombatVictory(val encounterId: String? = null, val enemyFaction: String? = null) : GameEvent
    data class BuildingConstructed(val buildingType: BuildingType, val level: Int = 1) : GameEvent
    data class BuildingUpgraded(val buildingType: BuildingType, val newLevel: Int) : GameEvent
    data class ResearchCompleted(val techId: String) : GameEvent
    data class ReputationChanged(val newPoints: Int, val delta: Int) : GameEvent
    data class FactionRelationChanged(val factionId: String, val newPoints: Int, val delta: Int) : GameEvent
    data class ExpeditionReturned(val locationId: String, val gatheredResources: GameResources, val lootItemIds: List<String>) : GameEvent
    data class DailyTick(val day: Int, val currentDateTime: GameDateTime) : GameEvent
}
