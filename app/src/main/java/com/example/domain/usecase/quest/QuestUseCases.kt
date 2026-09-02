package com.example.domain.usecase.quest

import com.example.core.result.GameError
import com.example.core.result.GameResult
import com.example.data.repository.GameStateRepository
import com.example.domain.model.GameState
import com.example.domain.service.quest.QuestManager

/**
 * Use case responsible for accepting an available quest from the quest board or factions.
 */
class AcceptQuestUseCase(
    private val repository: GameStateRepository
) {

    operator fun invoke(questId: String): GameResult<GameState> {
        val currentState = repository.currentGameState
        val opResult = QuestManager.acceptQuest(currentState, questId)

        return if (opResult.isSuccess) {
            val finalState = repository.updateGameStateSync { opResult.updatedGameState }
            GameResult.Success(finalState, opResult.messageRu)
        } else {
            GameResult.Failure(GameError.RuleViolation(opResult.messageRu), opResult.messageRu)
        }
    }
}

/**
 * Use case responsible for claiming completed quest rewards and updating faction reputation.
 */
class TurnInQuestUseCase(
    private val repository: GameStateRepository
) {

    operator fun invoke(questId: String): GameResult<GameState> {
        val currentState = repository.currentGameState
        val opResult = QuestManager.turnInQuest(currentState, questId)

        return if (opResult.isSuccess) {
            val finalState = repository.updateGameStateSync { opResult.updatedGameState }
            GameResult.Success(finalState, opResult.messageRu)
        } else {
            GameResult.Failure(GameError.RuleViolation(opResult.messageRu), opResult.messageRu)
        }
    }
}
