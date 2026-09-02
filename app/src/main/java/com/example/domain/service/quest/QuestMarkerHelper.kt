package com.example.domain.service.quest

import com.example.domain.model.GameState
import com.example.domain.model.Location
import com.example.domain.model.QuestStatus
import com.example.domain.model.quest.ObjectiveStatus

enum class QuestMarkerType {
    ACTIVE_OBJECTIVE,
    AVAILABLE_QUEST,
    TURN_IN
}

data class LocationQuestMarker(
    val locationId: String,
    val markerType: QuestMarkerType,
    val questTitle: String,
    val objectiveDescription: String? = null
)

/**
 * Computes tactical global map quest markers without mutating LocationState.
 */
object QuestMarkerHelper {

    fun getMarkersForLocation(locationId: String, state: GameState): List<LocationQuestMarker> {
        val markers = mutableListOf<LocationQuestMarker>()

        state.questStates.forEach { (questId, qState) ->
            val def = QuestCatalog.get(questId) ?: return@forEach

            // Check Turn In
            if (qState.status == QuestStatus.READY_TO_CLAIM) {
                val targetLoc = def.turnInLocationId ?: if (def.turnInLocationId == null) "loc_base" else null
                if (targetLoc == locationId) {
                    markers.add(
                        LocationQuestMarker(
                            locationId = locationId,
                            markerType = QuestMarkerType.TURN_IN,
                            questTitle = def.titleRu,
                            objectiveDescription = "Сдать задание в штабе"
                        )
                    )
                }
            }

            // Check Active Objectives
            if (qState.isActive) {
                def.objectives.forEach { objDef ->
                    val prog = qState.objectiveProgress[objDef.id]
                    if (prog?.status == ObjectiveStatus.IN_PROGRESS) {
                        val targetLoc = objDef.targetLocationId ?: objDef.targetId
                        if (targetLoc == locationId) {
                            markers.add(
                                LocationQuestMarker(
                                    locationId = locationId,
                                    markerType = QuestMarkerType.ACTIVE_OBJECTIVE,
                                    questTitle = def.titleRu,
                                    objectiveDescription = objDef.descriptionRu
                                )
                            )
                        }
                    }
                }
            }

            // Check Available quests
            if (qState.status == QuestStatus.AVAILABLE) {
                // If quest is available from this location/faction
                if (def.turnInLocationId == locationId) {
                    markers.add(
                        LocationQuestMarker(
                            locationId = locationId,
                            markerType = QuestMarkerType.AVAILABLE_QUEST,
                            questTitle = def.titleRu,
                            objectiveDescription = "Доступно новое задание"
                        )
                    )
                }
            }
        }

        return markers
    }

    fun hasActiveQuestMarker(location: Location, state: GameState): Boolean {
        return getMarkersForLocation(location.id, state).any { it.markerType == QuestMarkerType.ACTIVE_OBJECTIVE }
    }
}
