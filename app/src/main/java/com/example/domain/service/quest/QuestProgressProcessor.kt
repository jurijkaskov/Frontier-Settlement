package com.example.domain.service.quest

import com.example.domain.model.*
import com.example.domain.model.quest.*

/**
 * Central event processor that takes GameEvents and deterministically updates all active
 * quest objectives, evaluates unlocks, dependencies, failure conditions, and auto-completions.
 */
object QuestProgressProcessor {

    fun process(event: GameEvent, state: GameState): GameState {
        var currentState = state
        val currentQuestStates = currentState.questStates.toMutableMap()

        // 1. Process active quests against the incoming event
        QuestCatalog.ALL_QUESTS.forEach { def ->
            val existingState = currentQuestStates[def.id]

            if (existingState != null && existingState.isActive) {
                var updatedQuest: QuestState = existingState
                var hasChanges = false

                // Evaluate each objective
                def.objectives.forEach { objDef ->
                    val objProgress = updatedQuest.objectiveProgress[objDef.id]
                        ?: QuestObjectiveProgress(
                            objectiveId = objDef.id,
                            targetAmount = objDef.requiredAmount,
                            status = ObjectiveStatus.NOT_STARTED
                        )

                    // Check objective dependencies (staged objectives)
                    val depsSatisfied = objDef.dependsOnObjectiveIds.all { depId ->
                        updatedQuest.objectiveProgress[depId]?.isCompleted == true
                    }

                    if (depsSatisfied && objProgress.status == ObjectiveStatus.NOT_STARTED) {
                        updatedQuest = updatedQuest.copy(
                            objectiveProgress = updatedQuest.objectiveProgress + (objDef.id to objProgress.copy(
                                status = ObjectiveStatus.IN_PROGRESS
                            ))
                        )
                        hasChanges = true
                    }

                    val currentObjProg = updatedQuest.objectiveProgress[objDef.id] ?: objProgress

                    // Only update if in progress
                    if (currentObjProg.status == ObjectiveStatus.IN_PROGRESS) {
                        val newProg = evaluateObjectiveForEvent(objDef, currentObjProg, event, currentState)
                        if (newProg != currentObjProg) {
                            updatedQuest = updatedQuest.copy(
                                objectiveProgress = updatedQuest.objectiveProgress + (objDef.id to newProg)
                            )
                            hasChanges = true
                        }
                    }
                }

                // Check failure conditions
                val failureResult = QuestFailureEvaluator.evaluate(def, updatedQuest, currentState)
                if (failureResult.isFailed) {
                    updatedQuest = updatedQuest.copy(
                        status = QuestStatus.FAILED,
                        failedGameDateTime = currentState.gameDateTime,
                        failureReasonRu = failureResult.failureReasonRu
                    )
                    hasChanges = true
                } else {
                    // Check completion
                    val isReady = QuestCompletionEvaluator.isReadyForCompletion(def, updatedQuest)
                    if (isReady) {
                        if (def.completionMode == QuestCompletionMode.AUTO_COMPLETE) {
                            val rewardResult = QuestRewardProcessor.applyRewards(currentState, updatedQuest, def)
                            currentState = rewardResult.updatedGameState
                            updatedQuest = rewardResult.updatedQuestState
                            hasChanges = true
                        } else {
                            if (updatedQuest.status != QuestStatus.READY_TO_CLAIM) {
                                updatedQuest = updatedQuest.copy(status = QuestStatus.READY_TO_CLAIM)
                                hasChanges = true
                            }
                        }
                    }
                }

                if (hasChanges) {
                    currentQuestStates[def.id] = updatedQuest
                }
            }
        }

        // 2. Evaluate locked/unlocked availability of all catalog quests
        QuestCatalog.ALL_QUESTS.forEach { def ->
            val existing = currentQuestStates[def.id]

            if (existing == null || existing.status == QuestStatus.LOCKED) {
                val reqResult = QuestRequirementEvaluator.evaluate(def, currentState)
                if (reqResult.isMet) {
                    val initialProgress = def.objectives.associate { objDef ->
                        val depsMet = objDef.dependsOnObjectiveIds.isEmpty()
                        objDef.id to QuestObjectiveProgress(
                            objectiveId = objDef.id,
                            targetAmount = objDef.requiredAmount,
                            status = if (depsMet) ObjectiveStatus.IN_PROGRESS else ObjectiveStatus.NOT_STARTED
                        )
                    }

                    val deadline = def.timeLimitDays?.let { days ->
                        currentState.gameDateTime.plusDuration(com.example.domain.model.GameDuration.ofDays(days))
                    }

                    val newStatus = if (def.autoAccept) QuestStatus.ACTIVE else QuestStatus.AVAILABLE

                    currentQuestStates[def.id] = QuestState(
                        questId = def.id,
                        status = newStatus,
                        acceptedGameDateTime = if (def.autoAccept) currentState.gameDateTime else null,
                        deadlineGameDateTime = deadline,
                        objectiveProgress = initialProgress
                    )
                }
            } else if (existing.status == QuestStatus.AVAILABLE) {
                // If autoAccept became true or requirements changed
                if (def.autoAccept) {
                    currentQuestStates[def.id] = existing.copy(
                        status = QuestStatus.ACTIVE,
                        acceptedGameDateTime = currentState.gameDateTime
                    )
                }
            }
        }

        return currentState.copy(questStates = currentQuestStates)
    }

    private fun evaluateObjectiveForEvent(
        objDef: QuestObjectiveDefinition,
        prog: QuestObjectiveProgress,
        event: GameEvent,
        state: GameState
    ): QuestObjectiveProgress {
        return when (objDef.type) {
            QuestObjectiveType.COLLECT_RESOURCE -> {
                when (event) {
                    is GameEvent.ResourceStockUpdated -> {
                        val matchType = objDef.targetId == null || objDef.targetId.equals(event.type.name, ignoreCase = true)
                        if (matchType) {
                            if (objDef.progressMode == ObjectiveProgressMode.CURRENT_AMOUNT) {
                                val amount = event.currentAmount
                                val isDone = amount >= objDef.requiredAmount
                                prog.copy(
                                    currentAmount = amount.coerceAtMost(objDef.requiredAmount),
                                    status = if (isDone) ObjectiveStatus.COMPLETED else ObjectiveStatus.IN_PROGRESS
                                )
                            } else {
                                val newAcc = prog.accumulatedAmount + (if (event.delta > 0) event.delta else 0)
                                val isDone = newAcc >= objDef.requiredAmount
                                prog.copy(
                                    accumulatedAmount = newAcc,
                                    currentAmount = newAcc.coerceAtMost(objDef.requiredAmount),
                                    status = if (isDone) ObjectiveStatus.COMPLETED else ObjectiveStatus.IN_PROGRESS
                                )
                            }
                        } else prog
                    }
                    else -> prog
                }
            }

            QuestObjectiveType.DELIVER_RESOURCE -> {
                when (event) {
                    is GameEvent.ResourceDelivered -> {
                        if (event.objectiveId == objDef.id) {
                            val newAmt = prog.currentAmount + event.amount
                            val isDone = newAmt >= objDef.requiredAmount
                            prog.copy(
                                currentAmount = newAmt.coerceAtMost(objDef.requiredAmount),
                                status = if (isDone) ObjectiveStatus.COMPLETED else ObjectiveStatus.IN_PROGRESS
                            )
                        } else prog
                    }
                    else -> prog
                }
            }

            QuestObjectiveType.OBTAIN_ITEM -> {
                when (event) {
                    is GameEvent.ItemObtained -> {
                        val match = objDef.targetId == null || objDef.targetId == event.itemId
                        if (match) {
                            val newAmt = prog.currentAmount + event.count
                            val isDone = newAmt >= objDef.requiredAmount
                            prog.copy(
                                currentAmount = newAmt.coerceAtMost(objDef.requiredAmount),
                                status = if (isDone) ObjectiveStatus.COMPLETED else ObjectiveStatus.IN_PROGRESS
                            )
                        } else prog
                    }
                    else -> prog
                }
            }

            QuestObjectiveType.DELIVER_ITEM -> {
                when (event) {
                    is GameEvent.ItemDelivered -> {
                        if (event.objectiveId == objDef.id) {
                            val newAmt = prog.currentAmount + 1
                            val isDone = newAmt >= objDef.requiredAmount
                            prog.copy(
                                currentAmount = newAmt.coerceAtMost(objDef.requiredAmount),
                                status = if (isDone) ObjectiveStatus.COMPLETED else ObjectiveStatus.IN_PROGRESS
                            )
                        } else prog
                    }
                    else -> prog
                }
            }

            QuestObjectiveType.VISIT_LOCATION -> {
                when (event) {
                    is GameEvent.LocationVisited -> {
                        val targetLoc = objDef.targetLocationId ?: objDef.targetId
                        if (targetLoc == null || targetLoc == event.locationId) {
                            prog.copy(
                                currentAmount = 1,
                                status = ObjectiveStatus.COMPLETED
                            )
                        } else prog
                    }
                    else -> prog
                }
            }

            QuestObjectiveType.EXPLORE_LOCATION -> {
                when (event) {
                    is GameEvent.LocationExplored -> {
                        val targetLoc = objDef.targetLocationId ?: objDef.targetId
                        if (targetLoc == null || targetLoc == event.locationId) {
                            val cur = event.explorationProgress
                            val isDone = cur >= objDef.requiredAmount
                            prog.copy(
                                currentAmount = cur.coerceAtMost(objDef.requiredAmount),
                                status = if (isDone) ObjectiveStatus.COMPLETED else ObjectiveStatus.IN_PROGRESS
                            )
                        } else prog
                    }
                    else -> prog
                }
            }

            QuestObjectiveType.EXPLORE_AREA -> {
                when (event) {
                    is GameEvent.AreaExplored -> {
                        val targetArea = objDef.targetAreaId ?: objDef.targetId
                        if (targetArea == null || targetArea == event.areaId) {
                            prog.copy(
                                currentAmount = 1,
                                status = ObjectiveStatus.COMPLETED
                            )
                        } else prog
                    }
                    else -> prog
                }
            }

            QuestObjectiveType.COMPLETE_EVENT -> {
                when (event) {
                    is GameEvent.EventResolved -> {
                        if (objDef.targetId == null || objDef.targetId == event.eventId) {
                            prog.copy(
                                currentAmount = 1,
                                status = ObjectiveStatus.COMPLETED
                            )
                        } else prog
                    }
                    else -> prog
                }
            }

            QuestObjectiveType.MAKE_DECISION -> {
                when (event) {
                    is GameEvent.WorldFlagChanged -> {
                        if (objDef.targetId == event.flag && event.value) {
                            prog.copy(
                                currentAmount = 1,
                                status = ObjectiveStatus.COMPLETED
                            )
                        } else prog
                    }
                    is GameEvent.EventResolved -> {
                        if (objDef.targetId == event.choiceId) {
                            prog.copy(
                                currentAmount = 1,
                                status = ObjectiveStatus.COMPLETED
                            )
                        } else prog
                    }
                    else -> prog
                }
            }

            QuestObjectiveType.WIN_COMBAT -> {
                when (event) {
                    is GameEvent.CombatVictory -> {
                        val matchFaction = objDef.targetId == null || objDef.targetId == event.enemyFaction || objDef.targetId == event.encounterId
                        if (matchFaction) {
                            val newAmt = prog.currentAmount + 1
                            val isDone = newAmt >= objDef.requiredAmount
                            prog.copy(
                                currentAmount = newAmt.coerceAtMost(objDef.requiredAmount),
                                status = if (isDone) ObjectiveStatus.COMPLETED else ObjectiveStatus.IN_PROGRESS
                            )
                        } else prog
                    }
                    else -> prog
                }
            }

            QuestObjectiveType.BUILD -> {
                when (event) {
                    is GameEvent.BuildingConstructed -> {
                        val match = objDef.targetBuildingType == null || objDef.targetBuildingType == event.buildingType
                        if (match) {
                            prog.copy(
                                currentAmount = 1,
                                status = ObjectiveStatus.COMPLETED
                            )
                        } else prog
                    }
                    else -> prog
                }
            }

            QuestObjectiveType.UPGRADE_BUILDING -> {
                when (event) {
                    is GameEvent.BuildingUpgraded -> {
                        val match = objDef.targetBuildingType == null || objDef.targetBuildingType == event.buildingType
                        if (match) {
                            val newAmt = prog.currentAmount + 1
                            val isDone = newAmt >= objDef.requiredAmount
                            prog.copy(
                                currentAmount = newAmt.coerceAtMost(objDef.requiredAmount),
                                status = if (isDone) ObjectiveStatus.COMPLETED else ObjectiveStatus.IN_PROGRESS
                            )
                        } else prog
                    }
                    else -> prog
                }
            }

            QuestObjectiveType.RESEARCH -> {
                when (event) {
                    is GameEvent.ResearchCompleted -> {
                        val match = objDef.targetId == null || objDef.targetId == event.techId
                        if (match) {
                            val newAmt = prog.currentAmount + 1
                            val isDone = newAmt >= objDef.requiredAmount
                            prog.copy(
                                currentAmount = newAmt.coerceAtMost(objDef.requiredAmount),
                                status = if (isDone) ObjectiveStatus.COMPLETED else ObjectiveStatus.IN_PROGRESS
                            )
                        } else prog
                    }
                    else -> prog
                }
            }

            QuestObjectiveType.REACH_REPUTATION -> {
                when (event) {
                    is GameEvent.ReputationChanged -> {
                        val isDone = event.newPoints >= objDef.requiredAmount
                        prog.copy(
                            currentAmount = event.newPoints.coerceAtMost(objDef.requiredAmount),
                            status = if (isDone) ObjectiveStatus.COMPLETED else ObjectiveStatus.IN_PROGRESS
                        )
                    }
                    else -> prog
                }
            }

            QuestObjectiveType.REACH_FACTION_RELATION -> {
                when (event) {
                    is GameEvent.FactionRelationChanged -> {
                        val targetFaction = objDef.targetFactionId ?: objDef.targetId
                        if (targetFaction == null || targetFaction == event.factionId) {
                            val isDone = event.newPoints >= objDef.requiredAmount
                            prog.copy(
                                currentAmount = event.newPoints.coerceAtMost(objDef.requiredAmount),
                                status = if (isDone) ObjectiveStatus.COMPLETED else ObjectiveStatus.IN_PROGRESS
                            )
                        } else prog
                    }
                    else -> prog
                }
            }

            QuestObjectiveType.RETURN_TO_SETTLEMENT -> {
                when (event) {
                    is GameEvent.ExpeditionReturned -> {
                        val targetLoc = objDef.targetLocationId ?: objDef.targetId
                        if (targetLoc == null || targetLoc == event.locationId) {
                            prog.copy(
                                currentAmount = 1,
                                status = ObjectiveStatus.COMPLETED
                            )
                        } else prog
                    }
                    is GameEvent.LocationVisited -> {
                        if (event.locationId == "loc_base" || event.locationId == objDef.targetLocationId) {
                            prog.copy(
                                currentAmount = 1,
                                status = ObjectiveStatus.COMPLETED
                            )
                        } else prog
                    }
                    else -> prog
                }
            }
        }
    }
}
