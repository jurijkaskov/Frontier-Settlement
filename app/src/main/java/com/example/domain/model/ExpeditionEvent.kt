package com.example.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

/**
 * High-level categories for expedition and location exploration random events.
 */
enum class EventCategory(val titleRu: String, val iconKey: String, val badgeColor: Color) {
    DISCOVERY("Находка", "explore", TechCyan),
    RESOURCE("Ресурсы", "inventory", FoodGreen),
    ENVIRONMENT("Окружение", "landscape", WarningAmber),
    ENCOUNTER("Встреча", "groups", StoragePurple),
    TECHNICAL("Техника", "build", MaterialsOrange),
    CHARACTER("Персонаж", "person", SafeEmerald),
    SPECIAL("Особое событие", "star", DangerCrimson)
}

/**
 * Rarity tiers for event occurrence weights and narrative depth.
 */
enum class EventRarity(val titleRu: String, val weightMultiplier: Float, val badgeColor: Color) {
    COMMON("Обычное", 1.0f, SafeEmerald),
    UNCOMMON("Необычное", 0.6f, TechCyan),
    RARE("Редкое", 0.3f, WarningAmber),
    SPECIAL("Уникальное", 0.1f, StoragePurple)
}

/**
 * Repeatability policy for events across expeditions and locations.
 */
enum class EventRepeatMode(val titleRu: String) {
    REPEATABLE("Многократное"),
    ONCE_PER_EXPEDITION("Один раз за вылазку"),
    ONCE_PER_LOCATION("Один раз на локации"),
    GLOBAL_ONCE("Один раз за всю игру")
}

/**
 * Declarative requirement condition for an event to appear or for a choice to be unlocked.
 */
sealed interface EventRequirement {
    data class LocationTypeRequirement(val types: Set<LocationType>) : EventRequirement
    data class MinDangerRequirement(val min: DangerLevel) : EventRequirement
    data class MaxDangerRequirement(val max: DangerLevel) : EventRequirement
    data class RequiresRole(val role: CharacterRole) : EventRequirement
    data class RequiresSpecialization(val specialization: String) : EventRequirement
    data class RequiresTrait(val traitId: String) : EventRequirement
    data class RequiresItem(val itemId: String, val minCount: Int = 1, val itemNameHint: String? = null) : EventRequirement
    data class RequiresResource(val type: ResourceType, val minAmount: Int) : EventRequirement
    data class RequiresVehicle(val hasVehicle: Boolean = true) : EventRequirement
    data class RequiresWorldFlag(val flag: String, val expectedValue: Boolean = true) : EventRequirement
    data class RequiresTech(val techId: String) : EventRequirement
    data class RequiresMinStat(val statType: CharacterStatType, val minValue: Int) : EventRequirement
    data class RequiresArea(val areaId: String) : EventRequirement
    data class RequiresMinExplorationProgress(val minPercent: Int) : EventRequirement
    data class RequiresMaxExplorationProgress(val maxPercent: Int) : EventRequirement
    data class RequiresMinVisits(val minVisits: Int) : EventRequirement
    data class RequiresDayPeriod(val periods: Set<DayPeriod>) : EventRequirement
    data class RequiresTimeRange(val startHour: Int, val endHour: Int) : EventRequirement
    data class RequiresMinReputation(val minReputation: Int) : EventRequirement
    data class RequiresFactionRelation(val factionId: String, val minRelationPoints: Int) : EventRequirement
}

/**
 * Requirement and parameters for a skill check undertaken by a selected squad member.
 */
data class SkillCheckRequirement(
    val statType: CharacterStatType,
    val difficulty: Int,
    val applicableRoles: Set<CharacterRole> = emptySet(),
    val preferredActorId: String? = null,
    val allowManualActorSelection: Boolean = true,
    val bonusDescription: String? = null
)

/**
 * Result of resolving a skill check with roll calculations and breakdown.
 */
data class SkillCheckResult(
    val isSuccess: Boolean,
    val isCriticalSuccess: Boolean = false,
    val isCriticalFailure: Boolean = false,
    val roll: Int,
    val statValue: Int,
    val traitBonus: Int = 0,
    val equipmentBonus: Int = 0,
    val roleBonus: Int = 0,
    val totalScore: Int,
    val difficulty: Int,
    val actorName: String,
    val actorId: String,
    val explanation: String
)

/**
 * Structured consequences applied when an event choice is selected.
 */
data class EventOutcome(
    val title: String,
    val narrativeText: String,
    val resourceRewards: Map<ResourceType, Int> = emptyMap(),
    val itemRewards: List<String> = emptyList(),
    val xpReward: Int = 0,
    val healthDelta: Int = 0,
    val moraleDelta: Int = 0,
    val reputationDelta: Int = 0,
    val factionRelationDeltas: Map<String, Int> = emptyMap(),
    val explorationProgressGain: Int = 0,
    val discoveredAreaId: String? = null,
    val setWorldFlags: Map<String, Boolean> = emptyMap(),
    val chainEventId: String? = null,
    val requiresCombat: Boolean = false,
    val isSecretRevealed: Boolean = false,
    val customLog: String? = null,
    val timeCost: GameDuration = GameDuration.ZERO
)

/**
 * A discrete option presented to the player during an active event.
 */
data class EventChoice(
    val id: String,
    val text: String,
    val description: String? = null,
    val requirements: List<EventRequirement> = emptyList(),
    val skillCheck: SkillCheckRequirement? = null,
    val costResources: Map<ResourceType, Int> = emptyMap(),
    val consumedItemIds: List<String> = emptyList(),
    val successOutcome: EventOutcome,
    val failureOutcome: EventOutcome? = null,
    val riskLevelText: String? = null,
    val actionIconKey: String? = null,
    val timeCost: GameDuration = GameDuration.ZERO
)

/**
 * Complete Data-Driven Random Event Definition for expeditions.
 */
data class ExpeditionEvent(
    val id: String,
    val title: String,
    val description: String,
    val category: EventCategory = EventCategory.DISCOVERY,
    val rarity: EventRarity = EventRarity.COMMON,
    val baseWeight: Int = 100,
    val repeatMode: EventRepeatMode = EventRepeatMode.REPEATABLE,
    val visualAssetId: String? = null,
    val requirements: List<EventRequirement> = emptyList(),
    val choices: List<EventChoice> = emptyList(),
    val targetAreaIds: List<String> = emptyList(),
    val allowedLocationTypes: Set<LocationType> = emptySet(),
    val minDangerLevel: DangerLevel = DangerLevel.SAFE,
    val maxDangerLevel: DangerLevel = DangerLevel.EXTREME
) {
    // Backward-compatible computed accessors for legacy callers
    val choiceA: String
        get() = choices.getOrNull(0)?.text ?: "Исследовать сектор"

    val choiceB: String
        get() = choices.getOrNull(1)?.text ?: "Обойти стороной"

    val outcomeA: String
        get() = choices.getOrNull(0)?.successOutcome?.narrativeText ?: "Исследование завершено."

    val outcomeB: String
        get() = choices.getOrNull(1)?.successOutcome?.narrativeText ?: "Отряд продолжил путь."

    val requiresCombat: Boolean
        get() = choices.any { it.successOutcome.requiresCombat }
}

/**
 * Immutable state representing the currently active, deterministic event instance
 * within an active expedition.
 */
data class ActiveEventState(
    val eventId: String,
    val event: ExpeditionEvent,
    val instanceSeed: Long,
    val selectedActorId: String? = null,
    val selectedChoiceId: String? = null,
    val resolvedSkillCheckResult: SkillCheckResult? = null,
    val resolvedOutcome: EventOutcome? = null,
    val isResolved: Boolean = false,
    val awardedResources: Map<ResourceType, Int> = emptyMap(),
    val awardedItems: List<String> = emptyList(),
    val droppedResourcesDueToCapacity: Map<ResourceType, Int> = emptyMap(),
    val overflowPending: Boolean = false,
    val consumedSupplies: Map<ResourceType, Int> = emptyMap(),
    val consumedItems: List<String> = emptyList()
) {
    val isSkillCheckRequired: Boolean
        get() {
            val choice = event.choices.find { it.id == selectedChoiceId }
            return choice?.skillCheck != null
        }
}

/**
 * Journal entry of a resolved event saved in GameState for repeat rules and story continuity.
 */
data class EventHistoryEntry(
    val eventId: String,
    val locationId: String,
    val expeditionId: String,
    val choiceId: String,
    val wasSuccess: Boolean,
    val day: Int,
    val gameDateTime: GameDateTime = GameDateTime.START_TIME,
    val timestamp: Long = System.currentTimeMillis()
)
