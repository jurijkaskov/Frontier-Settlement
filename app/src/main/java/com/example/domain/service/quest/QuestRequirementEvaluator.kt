package com.example.domain.service.quest

import com.example.domain.model.GameState
import com.example.domain.model.QuestStatus
import com.example.domain.model.quest.QuestDefinition
import com.example.domain.model.quest.QuestRequirement

/**
 * Result of evaluating quest unlock and accept requirements.
 */
data class QuestRequirementResult(
    val isMet: Boolean,
    val unmetRequirements: List<String> = emptyList()
)

/**
 * Centralized evaluator for declarative quest requirements.
 */
object QuestRequirementEvaluator {

    fun evaluate(definition: QuestDefinition, gameState: GameState): QuestRequirementResult {
        return evaluate(definition.requirements, gameState)
    }

    fun evaluate(requirements: List<QuestRequirement>, gameState: GameState): QuestRequirementResult {
        val unmet = mutableListOf<String>()

        requirements.forEach { req ->
            when (req) {
                is QuestRequirement.MinSettlementLevel -> {
                    if (gameState.settlement.level < req.level) {
                        unmet.add("Требуется уровень базы ${req.level} (текущий: ${gameState.settlement.level})")
                    }
                }
                is QuestRequirement.MinReputation -> {
                    if (gameState.settlement.reputation < req.minPoints) {
                        unmet.add("Требуется репутация ${req.minPoints} (текущая: ${gameState.settlement.reputation})")
                    }
                }
                is QuestRequirement.MinFactionRelation -> {
                    val currentPoints = gameState.factionRelations[req.factionId]?.points ?: 0
                    if (currentPoints < req.minPoints) {
                        val factionName = com.example.data.ReputationBalanceConfig.getFaction(req.factionId)?.nameRu ?: req.factionId
                        unmet.add("Требуются отношения с «$factionName» не менее ${req.minPoints} (текущие: $currentPoints)")
                    }
                }
                is QuestRequirement.WorldFlag -> {
                    val current = gameState.worldFlags[req.flag] ?: false
                    if (current != req.expectedValue) {
                        unmet.add("Требуется мировое условие: ${req.flag}")
                    }
                }
                is QuestRequirement.CompletedQuest -> {
                    val questState = gameState.questStates[req.questId]
                    val isDone = questState?.status == QuestStatus.COMPLETED ||
                            gameState.quests.any { it.id == req.questId && it.status == QuestStatus.COMPLETED }
                    if (!isDone) {
                        val title = QuestCatalog.get(req.questId)?.titleRu ?: req.questId
                        unmet.add("Необходимо завершить задание «$title»")
                    }
                }
                is QuestRequirement.LocationDiscovered -> {
                    val loc = gameState.locations.find { it.id == req.locationId }
                    if (loc == null || loc.isHiddenOrUnknown) {
                        val name = loc?.name ?: req.locationId
                        unmet.add("Необходимо открыть локацию «$name»")
                    }
                }
                is QuestRequirement.TechnologyResearched -> {
                    val tech = gameState.technologies.find { it.id == req.techId }
                    if (tech == null || !tech.isResearched) {
                        val name = tech?.title ?: req.techId
                        unmet.add("Необходимо исследовать технологию «$name»")
                    }
                }
                is QuestRequirement.BuildingConstructed -> {
                    val building = gameState.settlement.buildings.find { it.type == req.buildingType }
                    if (building == null || !building.isConstructed || building.level < req.minLevel) {
                        unmet.add("Требуется здание «${req.buildingType.titleRu}» уровня ${req.minLevel}")
                    }
                }
                is QuestRequirement.MinDay -> {
                    if (gameState.day < req.day) {
                        unmet.add("Доступно начиная с Дня ${req.day}")
                    }
                }
            }
        }

        return QuestRequirementResult(
            isMet = unmet.isEmpty(),
            unmetRequirements = unmet
        )
    }
}
