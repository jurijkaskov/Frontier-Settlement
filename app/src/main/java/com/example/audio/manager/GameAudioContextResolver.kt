package com.example.audio.manager

import com.example.audio.model.*
import com.example.domain.model.DayPeriod
import com.example.domain.model.GameState
import com.example.domain.model.LocationType

/**
 * Resolves the active [GameAudioContext] and target [GameAudioProfile] from current navigation route
 * and runtime [GameState].
 */
object GameAudioContextResolver {

    fun resolveProfile(
        currentRoute: String,
        gameState: GameState?
    ): GameAudioProfile {
        val baseRoute = currentRoute.substringBefore("/").trim()

        // 1. Combat priority override (if combat is active or on combat screen)
        if (baseRoute == "combat" || (gameState != null && gameState.activeCombat != null && !gameState.activeCombat.isEnded)) {
            return GameAudioProfile.Combat
        }

        // 2. Expedition Return Summary dialog active
        if (gameState?.lastReturnSummary != null) {
            return GameAudioProfile.ReturnSummary
        }

        // 3. Route-based resolution
        return when (baseRoute) {
            "settlement", "warehouse", "workshop", "buildings", "research", "residents", "economy", "reputation", "quests", "squad", "vehicles" -> {
                val isNight = gameState?.gameDateTime?.dayPeriod == DayPeriod.NIGHT
                if (isNight) GameAudioProfile.SettlementNight else GameAudioProfile.SettlementDay
            }

            "map", "market" -> GameAudioProfile.WorldMap

            "expedition_prep" -> GameAudioProfile.WorldMap

            "travel" -> GameAudioProfile.Travel

            "arrival", "expedition_live" -> {
                if (gameState?.activeExpedition != null) {
                    val location = gameState.activeExpedition.location
                    when (location.type) {
                        LocationType.FOREST, LocationType.FARM -> GameAudioProfile.LocationForest
                        LocationType.INDUSTRIAL_PLANT, LocationType.WAREHOUSE_COMPLEX, LocationType.MILITARY_BUNKER -> GameAudioProfile.LocationIndustrial
                        LocationType.CITY_RUINS, LocationType.ABANDONED_STATION, LocationType.ANOMALY_ZONE -> GameAudioProfile.LocationRuins
                        else -> GameAudioProfile.LocationRuins
                    }
                } else {
                    GameAudioProfile.LocationRuins
                }
            }

            "event" -> GameAudioProfile.Event

            "loot" -> GameAudioProfile.Loot

            "menu", "save", "load", "debug_save", "content_browser", "generator_debug", "ui_gallery", "visual_asset_browser" -> {
                // Background continues peacefully in menu/settings
                val isNight = gameState?.gameDateTime?.dayPeriod == DayPeriod.NIGHT
                if (isNight) GameAudioProfile.SettlementNight else GameAudioProfile.SettlementDay
            }

            else -> GameAudioProfile.SettlementDay
        }
    }
}
