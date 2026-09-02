package com.example.domain.content.quest

import com.example.domain.content.core.ContentGenerationContext
import com.example.domain.content.core.GameRandomProvider
import com.example.domain.content.core.GenerationResult
import com.example.domain.content.core.WeightedSelector
import com.example.domain.content.registry.GameContentRegistry
import com.example.domain.model.Location
import com.example.domain.model.QuestStatus
import com.example.domain.model.ResourceType
import com.example.domain.model.quest.*
import kotlin.random.Random

/**
 * Procedural generator for dynamic repeatable contracts and supply missions.
 */
object RepeatableQuestGenerator {

    /**
     * Generates a concrete [QuestDefinition] and initialized [QuestState] from a template.
     */
    fun generateQuest(
        template: RepeatableQuestTemplate,
        context: ContentGenerationContext,
        availableLocations: List<Location>,
        customIndex: Int? = null
    ): GenerationResult<Pair<QuestDefinition, QuestState>> {
        val index = customIndex ?: context.generationIndex
        val seed = GameRandomProvider.deriveSeed(context.gameSeed, "quest", template.id, index)
        val random = Random(seed)

        val questId = "quest_gen_${template.id}_$index"

        // 1. Resolve Objective Target & Parameters
        val (objective, title, description) = when (template.objectiveType) {
            QuestObjectiveType.COLLECT_RESOURCE, QuestObjectiveType.DELIVER_RESOURCE -> {
                val resource = if (template.targetResourcePool.isNotEmpty()) {
                    template.targetResourcePool[random.nextInt(template.targetResourcePool.size)]
                } else {
                    ResourceType.MATERIALS
                }
                val amount = if (template.minRequiredAmount >= template.maxRequiredAmount) {
                    template.minRequiredAmount
                } else {
                    random.nextInt(template.minRequiredAmount, template.maxRequiredAmount + 1)
                }

                val obj = QuestObjectiveDefinition(
                    id = "${questId}_obj",
                    type = template.objectiveType,
                    descriptionRu = "Доставить ${resource.titleRu}: $amount ед.",
                    targetId = resource.id,
                    requiredAmount = amount,
                    progressMode = ObjectiveProgressMode.CURRENT_AMOUNT
                )

                val formattedTitle = try {
                    String.format(template.titleTemplateRu, resource.titleRu)
                } catch (e: Exception) {
                    template.titleTemplateRu
                }

                val formattedDesc = try {
                    String.format(template.descriptionTemplateRu, amount)
                } catch (e: Exception) {
                    template.descriptionTemplateRu
                }

                Triple(obj, formattedTitle, formattedDesc)
            }

            QuestObjectiveType.VISIT_LOCATION, QuestObjectiveType.EXPLORE_LOCATION -> {
                val matchingLocations = availableLocations.filter { loc ->
                    template.targetLocationTypes.isEmpty() || template.targetLocationTypes.contains(loc.type)
                }

                if (matchingLocations.isEmpty()) {
                    return GenerationResult.NoEligibleContent("No matching world locations available for quest template ${template.id}")
                }

                val targetLoc = matchingLocations[random.nextInt(matchingLocations.size)]

                val obj = QuestObjectiveDefinition(
                    id = "${questId}_obj",
                    type = template.objectiveType,
                    descriptionRu = "Посетить сектор «${targetLoc.name}»",
                    targetId = targetLoc.id,
                    targetLocationId = targetLoc.id,
                    requiredAmount = 1,
                    progressMode = ObjectiveProgressMode.ONE_TIME_EVENT
                )

                val formattedTitle = try {
                    String.format(template.titleTemplateRu, targetLoc.name)
                } catch (e: Exception) {
                    template.titleTemplateRu
                }

                val formattedDesc = try {
                    String.format(template.descriptionTemplateRu, targetLoc.name)
                } catch (e: Exception) {
                    template.descriptionTemplateRu
                }

                Triple(obj, formattedTitle, formattedDesc)
            }

            else -> {
                val obj = QuestObjectiveDefinition(
                    id = "${questId}_obj",
                    type = template.objectiveType,
                    descriptionRu = "Выполнить контракт снабжения",
                    targetId = "default",
                    requiredAmount = 1,
                    progressMode = ObjectiveProgressMode.ONE_TIME_EVENT
                )
                Triple(obj, template.titleTemplateRu, template.descriptionTemplateRu)
            }
        }

        // 2. Compute Rewards
        val reward = RewardBudgetCalculator.calculateQuestRewards(
            baseCredits = template.baseRewardCredits,
            baseXp = template.baseRewardXp,
            baseReputation = template.baseReputationReward,
            settlementLevel = context.settlementLevel
        )

        val questDef = QuestDefinition(
            id = questId,
            titleRu = title,
            descriptionRu = description,
            category = template.category,
            source = template.source,
            giverNameRu = template.giverNameRu,
            factionId = template.factionId,
            requirements = emptyList(),
            objectives = listOf(objective),
            rewards = reward,
            autoAccept = true,
            canDecline = true,
            completionMode = QuestCompletionMode.TURN_IN,
            repeatability = QuestRepeatability.REPEATABLE_WITH_COOLDOWN,
            cooldownDays = template.cooldownDays,
            priority = 40
        )

        val questState = QuestState(
            questId = questId,
            status = QuestStatus.ACTIVE,
            objectiveProgress = mapOf(
                objective.id to QuestObjectiveProgress(
                    objectiveId = objective.id,
                    currentAmount = 0,
                    targetAmount = objective.requiredAmount,
                    status = ObjectiveStatus.IN_PROGRESS
                )
            )
        )

        return GenerationResult.Success(Pair(questDef, questState))
    }

    /**
     * Picks a quest template and generates a new contract.
     */
    fun generateRandomContract(
        context: ContentGenerationContext,
        availableLocations: List<Location>,
        registry: GameContentRegistry = GameContentRegistry
    ): GenerationResult<Pair<QuestDefinition, QuestState>> {
        val candidates = registry.repeatableQuestTemplates.values.toList()
        if (candidates.isEmpty()) {
            return GenerationResult.NoEligibleContent("No repeatable quest templates registered")
        }

        val random = GameRandomProvider.createRandom(context.gameSeed, "quest_select", context.generationIndex)
        val selected = WeightedSelector.select(
            candidates = candidates,
            weightExtractor = { it.baseWeight },
            random = random,
            context = context
        ) ?: candidates.first()

        return generateQuest(selected, context, availableLocations)
    }
}
