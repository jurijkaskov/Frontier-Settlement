package com.example.data.save

import com.example.domain.model.ExpeditionPhase
import com.example.domain.model.GameState

/**
 * Concise semantic context indicator for save metadata previews.
 */
enum class SaveLocationContext(
    val titleRu: String,
    val iconKey: String
) {
    IN_SETTLEMENT("В поселении", "home"),
    TRAVELING("В пути к локации", "directions_car"),
    AT_LOCATION("На локации", "place"),
    EXPLORING("Исследование", "explore"),
    IN_COMBAT("В бою", "sports_kabaddi"),
    RETURNING("Возвращение на базу", "keyboard_return"),
    EVENT_ACTIVE("Событие", "event_available"),
    LOOT_ACTIVE("Сбор добычи", "inventory_2");

    companion object {
        fun fromGameState(gameState: GameState): SaveLocationContext {
            val expedition = gameState.activeExpedition
            return when {
                gameState.activeCombat != null -> IN_COMBAT
                expedition?.activeEventState?.isResolved == false || expedition?.currentEvent != null -> EVENT_ACTIVE
                expedition?.lootItemIds?.isNotEmpty() == true || (expedition?.gatheredLoot?.totalStoredVolume ?: 0) > 0 -> LOOT_ACTIVE
                expedition?.phase == ExpeditionPhase.EXPLORING -> EXPLORING
                expedition?.phase == ExpeditionPhase.RETURNING -> RETURNING
                gameState.isCurrentlyTraveling -> TRAVELING
                else -> IN_SETTLEMENT
            }
        }
    }
}
