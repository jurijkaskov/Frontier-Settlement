package com.example.domain.model.quest

import com.example.domain.model.*

/**
 * Category of the quest for filtering and thematic grouping.
 */
enum class QuestCategory(val titleRu: String, val badgeColorHex: Long) {
    MAIN("Сюжетное", 0xFFE6A15C),        // Frontier Orange / Amber
    SIDE("Побочное", 0xFF64B5F6),        // Tech Cyan / Blue
    FACTION("Фракционное", 0xFFBA68C8),   // Violet / Purple
    SETTLEMENT("Развитие базы", 0xFF81C784),// Safe Emerald / Green
    EXPLORATION("Исследование", 0xFFFFD54F),// Golden Yellow
    REPEATABLE("Контракт", 0xFF90A4AE)    // Slate Steel
}

/**
 * Source/giver of the quest.
 */
enum class QuestSource(val titleRu: String) {
    SETTLEMENT("Штаб поселения"),
    FACTION("Представитель фракции"),
    QUEST_GIVER("Очевидец / Житель"),
    EVENT("Событие в пути"),
    SYSTEM("Радиоперехват / Система")
}

/**
 * How a quest is completed once all required objectives are met.
 */
enum class QuestCompletionMode(val titleRu: String) {
    AUTO_COMPLETE("Автоматическое завершение"),
    TURN_IN("Сдача задания в поселении / заказчику")
}

/**
 * Repeatability policy for quests and contracts.
 */
enum class QuestRepeatability {
    ONCE,
    REPEATABLE,
    REPEATABLE_WITH_COOLDOWN
}

/**
 * Group logic for multiple objectives.
 */
enum class ObjectiveGroupMode {
    ALL_REQUIRED,
    ANY_ONE
}

/**
 * How progress is measured for an objective.
 */
enum class ObjectiveProgressMode {
    CURRENT_AMOUNT, // Checks current inventory/stock (e.g. materials currently in warehouse)
    ACCUMULATED,    // Counts increments since quest acceptance (e.g. earned +500 credits)
    ONE_TIME_EVENT, // Triggers on single action (e.g. win combat, resolve event)
    STATE_CHECK     // Checks world boolean/state (e.g. building level >= 2)
}

/**
 * Types of quest objectives supported by the game engine.
 */
enum class QuestObjectiveType(val titleRu: String) {
    COLLECT_RESOURCE("Собрать ресурс"),
    DELIVER_RESOURCE("Доставить ресурс"),
    OBTAIN_ITEM("Добыть предмет"),
    DELIVER_ITEM("Передать предмет"),
    VISIT_LOCATION("Посетить локацию"),
    EXPLORE_LOCATION("Исследовать локацию"),
    EXPLORE_AREA("Исследовать зону"),
    COMPLETE_EVENT("Разрешить событие"),
    MAKE_DECISION("Принять решение / Флаг"),
    WIN_COMBAT("Победить в бою"),
    BUILD("Построить здание"),
    UPGRADE_BUILDING("Улучшить здание"),
    RESEARCH("Изучить технологию"),
    REACH_REPUTATION("Достичь уровня репутации"),
    REACH_FACTION_RELATION("Достичь отношений с фракцией"),
    RETURN_TO_SETTLEMENT("Вернуться в поселение")
}

/**
 * Declarative unlock requirement for a quest.
 */
sealed interface QuestRequirement {
    data class MinSettlementLevel(val level: Int) : QuestRequirement
    data class MinReputation(val minPoints: Int) : QuestRequirement
    data class MinFactionRelation(val factionId: String, val minPoints: Int) : QuestRequirement
    data class WorldFlag(val flag: String, val expectedValue: Boolean = true) : QuestRequirement
    data class CompletedQuest(val questId: String) : QuestRequirement
    data class LocationDiscovered(val locationId: String) : QuestRequirement
    data class TechnologyResearched(val techId: String) : QuestRequirement
    data class BuildingConstructed(val buildingType: BuildingType, val minLevel: Int = 1) : QuestRequirement
    data class MinDay(val day: Int) : QuestRequirement
}

/**
 * Declarative failure condition for a quest.
 */
sealed interface QuestFailureCondition {
    data class IncompatibleWorldFlag(val flag: String, val failureValue: Boolean = true, val reasonRu: String = "Принято несовместимое решение") : QuestFailureCondition
    data class FactionRelationBelow(val factionId: String, val thresholdPoints: Int, val reasonRu: String = "Отношения с фракцией безнадёжно испорчены") : QuestFailureCondition
    data class TimeLimitExpired(val reasonRu: String = "Истёк срок выполнения контракта") : QuestFailureCondition
    data class TargetDestroyed(val targetId: String, val reasonRu: String = "Целевой объект уничтожен") : QuestFailureCondition
    data class CustomCondition(val conditionId: String, val reasonRu: String) : QuestFailureCondition
}

/**
 * Definition of a single quest objective.
 */
data class QuestObjectiveDefinition(
    val id: String,
    val type: QuestObjectiveType,
    val descriptionRu: String,
    val targetId: String? = null,              // Resource key, Item id, Location id, Tech id, Building name, etc.
    val requiredAmount: Int = 1,
    val progressMode: ObjectiveProgressMode = ObjectiveProgressMode.CURRENT_AMOUNT,
    val optional: Boolean = false,
    val hidden: Boolean = false,
    val dependsOnObjectiveIds: List<String> = emptyList(),
    val groupMode: ObjectiveGroupMode = ObjectiveGroupMode.ALL_REQUIRED,
    val targetLocationId: String? = null,
    val targetAreaId: String? = null,
    val targetFactionId: String? = null,
    val targetBuildingType: BuildingType? = null
)

/**
 * Definition of quest rewards applied safely upon completion.
 */
data class QuestRewardDefinition(
    val credits: Int = 0,
    val resources: Map<ResourceType, Int> = emptyMap(),
    val itemIds: List<String> = emptyList(),
    val xp: Int = 0,
    val reputationDelta: Int = 0,
    val factionRelationDeltas: Map<String, Int> = emptyMap(),
    val worldFlags: Map<String, Boolean> = emptyMap(),
    val unlockLocationIds: List<String> = emptyList(),
    val unlockTechIds: List<String> = emptyList(),
    val unlockNextQuestIds: List<String> = emptyList(),
    val summaryRu: String = ""
)

/**
 * Static, data-driven definition of a quest in the catalog.
 */
data class QuestDefinition(
    val id: String,
    val titleRu: String,
    val descriptionRu: String,
    val category: QuestCategory,
    val source: QuestSource = QuestSource.SETTLEMENT,
    val factionId: String? = null,
    val giverNameRu: String? = null,
    val requirements: List<QuestRequirement> = emptyList(),
    val objectives: List<QuestObjectiveDefinition> = emptyList(),
    val rewards: QuestRewardDefinition = QuestRewardDefinition(),
    val failureConditions: List<QuestFailureCondition> = emptyList(),
    val timeLimitDays: Int? = null,
    val nextQuestIds: List<String> = emptyList(),
    val repeatability: QuestRepeatability = QuestRepeatability.ONCE,
    val cooldownDays: Int = 0,
    val priority: Int = 10,
    val autoAccept: Boolean = false,
    val canDecline: Boolean = true,
    val completionMode: QuestCompletionMode = QuestCompletionMode.TURN_IN,
    val turnInLocationId: String? = null,
    val turnInFactionId: String? = null,
    val hiddenUntilUnlocked: Boolean = false,
    val iconAsset: String? = null
)

/**
 * Status of an individual objective within an active quest.
 */
enum class ObjectiveStatus(val titleRu: String) {
    NOT_STARTED("Не начата"),
    IN_PROGRESS("В процессе"),
    COMPLETED("Выполнена"),
    FAILED("Провалена")
}

/**
 * Runtime progress tracker for an individual objective.
 */
data class QuestObjectiveProgress(
    val objectiveId: String,
    val currentAmount: Int = 0,
    val targetAmount: Int = 1,
    val status: ObjectiveStatus = ObjectiveStatus.NOT_STARTED,
    val accumulatedAmount: Int = 0,
    val customData: Map<String, String> = emptyMap()
) {
    val isCompleted: Boolean
        get() = status == ObjectiveStatus.COMPLETED

    val progressFraction: Float
        get() = if (targetAmount > 0) (currentAmount.toFloat() / targetAmount.toFloat()).coerceIn(0f, 1f) else 1f
}

/**
 * Saveable runtime state of a specific quest assigned to the player.
 */
data class QuestState(
    val questId: String,
    val status: QuestStatus = QuestStatus.LOCKED,
    val objectiveProgress: Map<String, QuestObjectiveProgress> = emptyMap(),
    val acceptedGameDateTime: GameDateTime? = null,
    val completedGameDateTime: GameDateTime? = null,
    val failedGameDateTime: GameDateTime? = null,
    val deadlineGameDateTime: GameDateTime? = null,
    val failureReasonRu: String? = null,
    val sourceId: String? = null,
    val instanceId: String? = null,
    val lastCompletedDay: Int? = null,
    val appliedRewardIds: Set<String> = emptySet(),
    val customFlags: Map<String, String> = emptyMap()
) {
    val isActive: Boolean
        get() = status == QuestStatus.ACTIVE || status == QuestStatus.IN_PROGRESS

    val isAvailable: Boolean
        get() = status == QuestStatus.AVAILABLE

    val isCompleted: Boolean
        get() = status == QuestStatus.COMPLETED

    val isFailed: Boolean
        get() = status == QuestStatus.FAILED || status == QuestStatus.EXPIRED

    val isReadyToClaim: Boolean
        get() = status == QuestStatus.READY_TO_CLAIM
}
