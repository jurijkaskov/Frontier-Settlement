package com.example.domain.content.quest

import com.example.domain.content.core.ContentTag
import com.example.domain.model.LocationType
import com.example.domain.model.ResourceType
import com.example.domain.model.quest.QuestCategory
import com.example.domain.model.quest.QuestObjectiveType
import com.example.domain.model.quest.QuestSource

/**
 * Template for dynamically generated repeatable quests and trade contracts.
 */
data class RepeatableQuestTemplate(
    val id: String,
    val titleTemplateRu: String,
    val descriptionTemplateRu: String,
    val category: QuestCategory = QuestCategory.REPEATABLE,
    val source: QuestSource = QuestSource.SETTLEMENT,
    val giverNameRu: String = "Торговый комендант",
    val factionId: String? = null,
    val objectiveType: QuestObjectiveType = QuestObjectiveType.COLLECT_RESOURCE,
    val targetLocationTypes: Set<LocationType> = emptySet(),
    val targetResourcePool: List<ResourceType> = emptyList(),
    val targetItemPool: List<String> = emptyList(),
    val minRequiredAmount: Int = 10,
    val maxRequiredAmount: Int = 40,
    val baseRewardCredits: Int = 100,
    val baseRewardXp: Int = 25,
    val baseReputationReward: Int = 5,
    val cooldownDays: Int = 2,
    val baseWeight: Float = 100f,
    val tags: Set<ContentTag> = emptySet()
)
