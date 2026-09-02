package com.example.domain.usecase.economy

import com.example.core.result.GameError
import com.example.core.result.GameResult
import com.example.data.repository.GameStateRepository
import com.example.domain.model.CraftRecipe
import com.example.domain.model.GameState
import com.example.domain.model.WarehouseItem
import com.example.domain.model.quest.GameEvent
import com.example.domain.service.quest.QuestProgressProcessor

/**
 * Use case responsible for crafting items at the workshop.
 */
class CraftItemUseCase(
    private val repository: GameStateRepository
) {

    operator fun invoke(recipe: CraftRecipe, craftCount: Int = 1): GameResult<GameState> {
        val currentState = repository.currentGameState
        if (craftCount <= 0) {
            return GameResult.Failure(GameError.RuleViolation("Количество для крафта должно быть больше 0"))
        }

        // Validate resource requirements
        for ((resType, costPerUnit) in recipe.requiredResources) {
            val totalCost = costPerUnit * craftCount
            val available = currentState.resources[resType]
            if (available < totalCost) {
                return GameResult.Failure(
                    GameError.InsufficientResources,
                    "Недостаточно ${resType.nameRu}. Требуется: $totalCost, доступно: $available"
                )
            }
        }

        val updatedState = repository.updateGameStateSync { state ->
            var newResources = state.resources
            for ((resType, costPerUnit) in recipe.requiredResources) {
                val totalCost = costPerUnit * craftCount
                newResources = newResources.withResource(resType, newResources[resType] - totalCost)
            }

            val totalProduced = craftCount * recipe.outputQuantity
            val existingItemIndex = state.inventoryItems.indexOfFirst { it.id == recipe.outputItem.id }
            val updatedInventory = if (existingItemIndex >= 0) {
                state.inventoryItems.mapIndexed { index, item ->
                    if (index == existingItemIndex) item.copy(quantity = item.quantity + totalProduced) else item
                }
            } else {
                state.inventoryItems + recipe.outputItem.copy(quantity = totalProduced)
            }

            val intermediate = state.copy(
                resources = newResources,
                inventoryItems = updatedInventory
            )

            QuestProgressProcessor.process(
                GameEvent.ItemObtained(recipe.outputItem.id, totalProduced),
                intermediate
            )
        }

        return GameResult.Success(updatedState, "Создано: ${recipe.nameRu} (x${craftCount * recipe.outputQuantity})")
    }
}
