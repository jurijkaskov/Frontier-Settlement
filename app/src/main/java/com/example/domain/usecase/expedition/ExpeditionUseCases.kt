package com.example.domain.usecase.expedition

import com.example.core.result.GameError
import com.example.core.result.GameResult
import com.example.data.repository.GameStateRepository
import com.example.domain.model.*
import com.example.domain.model.quest.GameEvent
import com.example.domain.service.ExpeditionReturnService
import com.example.domain.service.quest.QuestProgressProcessor

/**
 * Use case responsible for launching an expedition towards a target location.
 */
class StartExpeditionUseCase(
    private val repository: GameStateRepository
) {

    operator fun invoke(targetLocationId: String): GameResult<GameState> {
        val currentState = repository.currentGameState
        val location = currentState.locations.find { it.id == targetLocationId }
            ?: return GameResult.Failure(GameError.InvalidLocation, "Целевая локация не найдена")

        if (currentState.activeExpedition != null) {
            return GameResult.Failure(GameError.AlreadyTraveling, "Экспедиция уже находится в пути")
        }

        val squadMembers = currentState.characters.filter { currentState.squad.memberIds.contains(it.id) }
        val vehicle = currentState.vehicles.find { it.id == currentState.squad.assignedVehicleId }
            ?: currentState.vehicles.firstOrNull()
            ?: Vehicle(id = "veh_foot", name = "Пеший переход", type = VehicleType.FOOT)

        val updatedState = repository.updateGameStateSync { state ->
            val newExp = Expedition(
                id = "exp_${System.currentTimeMillis()}",
                location = location,
                squad = squadMembers,
                vehicle = vehicle,
                status = ExpeditionStatus.TRAVELING,
                phase = ExpeditionPhase.TRAVELING_TO_LOCATION
            )
            val stateWithExp = state.copy(
                activeExpedition = newExp,
                squad = state.squad.copy(status = SquadStatus.ON_EXPEDITION)
            )
            QuestProgressProcessor.process(
                GameEvent.LocationVisited(targetLocationId),
                stateWithExp
            )
        }

        return GameResult.Success(updatedState, "Экспедиция к '${location.name}' успешно отправлена")
    }
}

/**
 * Use case responsible for concluding an expedition, unloading cargo, awarding experience, and logging return summary.
 */
class CompleteExpeditionReturnUseCase(
    private val repository: GameStateRepository
) {

    operator fun invoke(): GameResult<Pair<GameState, ExpeditionReturnSummary?>> {
        val currentState = repository.currentGameState
        if (currentState.activeExpedition == null && currentState.activeTravel == null) {
            return GameResult.Failure(GameError.RuleViolation("Нет активной экспедиции для возвращения"))
        }

        val (stateAfterReturn, summary) = ExpeditionReturnService.completeExpeditionReturn(currentState)

        val finalState = repository.updateGameStateSync {
            stateAfterReturn
        }

        return GameResult.Success(Pair(finalState, summary), "Экспедиция благополучно вернулась в поселение")
    }
}
