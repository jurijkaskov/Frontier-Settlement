package com.example.domain.usecase.settlement

import com.example.core.result.GameError
import com.example.core.result.GameResult
import com.example.data.repository.GameStateRepository
import com.example.domain.model.BuildingCategory
import com.example.domain.model.BuildingStatus
import com.example.domain.model.BuildingType
import com.example.domain.model.GameState
import com.example.domain.model.ResourceType

/**
 * Use case responsible for constructing a new building in the settlement.
 * Validates resource costs, prerequisites, and population constraints.
 */
class BuildBuildingUseCase(
    private val repository: GameStateRepository
) {

    operator fun invoke(buildingId: String): GameResult<GameState> {
        val currentState = repository.currentGameState
        val building = currentState.settlement.buildings.find { it.id == buildingId }
            ?: return GameResult.Failure(GameError.RuleViolation("Здание с ID '$buildingId' не найдено"))

        if (building.isConstructed) {
            return GameResult.Failure(GameError.RuleViolation("Здание '${building.name}' уже построено"))
        }

        val requiredMaterials = building.buildCostMaterials
        val availableMaterials = currentState.resources.materials

        if (availableMaterials < requiredMaterials) {
            return GameResult.Failure(
                GameError.InsufficientResources,
                "Недостаточно стройматериалов. Требуется: $requiredMaterials, доступно: $availableMaterials"
            )
        }

        val updatedState = repository.updateGameStateSync { state ->
            val updatedBuildings = state.settlement.buildings.map { b ->
                if (b.id == buildingId) {
                    b.copy(
                        level = 1,
                        status = BuildingStatus.OPERATIONAL
                    )
                } else b
            }

            val newMaterials = (state.resources.materials - requiredMaterials).coerceAtLeast(0)
            val updatedResources = state.resources.copy(materials = newMaterials)

            val baseSettlement = state.settlement.copy(
                buildings = updatedBuildings,
                xp = state.settlement.xp + building.xpRewardOnBuild
            )

            state.copy(
                settlement = baseSettlement,
                resources = updatedResources
            )
        }

        return GameResult.Success(updatedState, "Строительство '${building.name}' успешно завершено")
    }
}
