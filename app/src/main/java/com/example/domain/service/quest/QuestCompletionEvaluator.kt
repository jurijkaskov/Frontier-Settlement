package com.example.domain.service.quest

import com.example.domain.model.quest.*

/**
 * Evaluates whether all required objectives of a quest are satisfied.
 */
object QuestCompletionEvaluator {

    fun isReadyForCompletion(
        definition: QuestDefinition,
        questState: QuestState
    ): Boolean {
        if (questState.isFailed || questState.isCompleted) return false

        val mandatoryObjectives = definition.objectives.filter { !it.optional }
        if (mandatoryObjectives.isEmpty()) return true

        // Check grouped objectives if any
        val hasAnyOneGroup = mandatoryObjectives.any { it.groupMode == ObjectiveGroupMode.ANY_ONE }

        if (hasAnyOneGroup) {
            val anyOneCompleted = mandatoryObjectives
                .filter { it.groupMode == ObjectiveGroupMode.ANY_ONE }
                .any { objDef ->
                    questState.objectiveProgress[objDef.id]?.isCompleted == true
                }
            val allOtherCompleted = mandatoryObjectives
                .filter { it.groupMode == ObjectiveGroupMode.ALL_REQUIRED }
                .all { objDef ->
                    questState.objectiveProgress[objDef.id]?.isCompleted == true
                }
            return anyOneCompleted && allOtherCompleted
        }

        // Standard: all mandatory objectives must be COMPLETED
        return mandatoryObjectives.all { objDef ->
            val prog = questState.objectiveProgress[objDef.id]
            prog?.isCompleted == true
        }
    }
}
