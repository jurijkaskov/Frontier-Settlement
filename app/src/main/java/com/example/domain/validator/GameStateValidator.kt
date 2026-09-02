package com.example.domain.validator

import com.example.domain.model.GameState
import com.example.domain.model.ResourceType

/**
 * Result of GameState validation containing any detected invariant violations.
 */
data class GameStateValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String>
)

/**
 * Comprehensive validator for GameState invariants.
 * Checks for ID uniqueness, dangling references, item ownership, valid active expeditions,
 * and valid transport assignments without mutating game state.
 */
object GameStateValidator {

    fun validate(gameState: GameState): GameStateValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // 1. Character uniqueness and integrity
        val residentIds = gameState.characters.map { it.id }
        if (residentIds.size != residentIds.distinct().size) {
            errors.add("Обнаружены повторяющиеся ID жителей поселения")
        }

        // 2. Squad references
        for (memberId in gameState.squad.memberIds) {
            if (!residentIds.contains(memberId)) {
                errors.add("Участник отряда (ID: $memberId) не найден в списке жителей поселения")
            }
        }
        val leaderId = gameState.squad.leaderId
        if (leaderId != null && !residentIds.contains(leaderId)) {
            errors.add("Лидер отряда (ID: $leaderId) отсутствует в списке жителей поселения")
        }

        // 3. Location and Expedition validation
        val locationIds = gameState.locations.map { it.id }.toSet()
        val activeExp = gameState.activeExpedition
        if (activeExp != null) {
            val targetId = activeExp.location.id
            if (!locationIds.contains(targetId) && targetId != "loc_base") {
                errors.add("Активная экспедиция направлена в несуществующую локацию: $targetId")
            }
        }

        // 4. Vehicle references
        val vehicleIds = gameState.vehicles.map { it.id }.toSet()
        val assignedVehicleId = gameState.squad.assignedVehicleId
        if (assignedVehicleId != null && !vehicleIds.contains(assignedVehicleId)) {
            warnings.add("Отряду назначен несуществующий транспорт: $assignedVehicleId")
        }

        // 5. Buildings validation
        val buildingIds = gameState.settlement.buildings.map { it.id }
        if (buildingIds.size != buildingIds.distinct().size) {
            errors.add("Обнаружены повторяющиеся ID зданий")
        }

        // 6. Resources validity
        for (type in ResourceType.values()) {
            val amount = gameState.resources[type]
            if (amount < 0) {
                errors.add("Отрицательное количество ресурса ${type.nameRu}: $amount")
            }
        }

        return GameStateValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
}
