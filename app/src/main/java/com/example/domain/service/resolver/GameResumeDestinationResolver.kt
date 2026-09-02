package com.example.domain.service.resolver

import com.example.domain.model.ExpeditionPhase
import com.example.domain.model.GameState

/**
 * High-level destination route resolved from a loaded [GameState].
 */
enum class ResumeDestination(val route: String, val descriptionRu: String) {
    SETTLEMENT("settlement", "Поселение (Главный штаб)"),
    COMBAT("combat", "Тактический бой"),
    EXPEDITION_EVENT("expedition_live", "Событие экспедиции"),
    EXPEDITION_LOOT("expedition_live", "Сбор добычи"),
    EXPEDITION_EXPLORATION("expedition_live", "Активная экспедиция"),
    ARRIVAL("arrival", "Прибытие на локацию"),
    MAP_TRAVEL("map", "Путешествие по глобальной карте")
}

/**
 * Authoritative resolver determining the initial navigation screen when resuming a game.
 */
object GameResumeDestinationResolver {

    /**
     * Resolves the proper navigation destination according to game state priorities.
     */
    fun resolve(gameState: GameState): ResumeDestination {
        return when {
            // 1. Active Tactical Combat has highest priority
            gameState.activeCombat != null -> ResumeDestination.COMBAT

            // 2. Unresolved Expedition Event
            gameState.activeExpedition?.activeEventState?.isResolved == false || gameState.activeExpedition?.currentEvent != null ->
                ResumeDestination.EXPEDITION_EVENT

            // 3. Uncollected Expedition Loot
            (gameState.activeExpedition?.lootItemIds?.isNotEmpty() == true || (gameState.activeExpedition?.gatheredLoot?.totalStoredVolume ?: 0) > 0) && gameState.activeExpedition?.phase == ExpeditionPhase.EXPLORING ->
                ResumeDestination.EXPEDITION_LOOT

            // 4. Expedition arrived at location (exploration not started)
            gameState.activeExpedition?.phase == ExpeditionPhase.AT_LOCATION ->
                ResumeDestination.ARRIVAL

            // 5. Active Exploration Phase
            gameState.activeExpedition?.phase == ExpeditionPhase.EXPLORING ->
                ResumeDestination.EXPEDITION_EXPLORATION

            // 6. Traveling on map or returning
            gameState.isCurrentlyTraveling || gameState.activeExpedition?.phase == ExpeditionPhase.RETURNING ->
                ResumeDestination.MAP_TRAVEL

            // 7. Base settlement default
            else -> ResumeDestination.SETTLEMENT
        }
    }
}
