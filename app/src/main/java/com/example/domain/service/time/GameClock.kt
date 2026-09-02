package com.example.domain.service.time

import com.example.domain.model.DayPeriod
import com.example.domain.model.GameDateTime
import com.example.domain.model.GameDuration
import com.example.domain.model.GameState
import com.example.domain.model.TimeAdvanceResult

/**
 * Pure domain service for in-game time arithmetic, day boundary detection,
 * and operation readiness validation.
 */
object GameClock {

    /**
     * Advances the clock by the given duration and determines which calendar days (if any) were crossed.
     */
    fun advance(currentTime: GameDateTime, duration: GameDuration): TimeAdvanceResult {
        if (duration.isZero) {
            return TimeAdvanceResult(
                previousTime = currentTime,
                newTime = currentTime,
                durationAdded = duration,
                crossedDays = emptyList()
            )
        }

        val newTime = currentTime.plusDuration(duration)
        val crossedDays = calculateCrossedDays(currentTime, newTime)

        return TimeAdvanceResult(
            previousTime = currentTime,
            newTime = newTime,
            durationAdded = duration,
            crossedDays = crossedDays
        )
    }

    /**
     * Determines all day numbers entered when moving from [fromTime] to [toTime].
     * (e.g. Day 1 -> Day 3 yields listOf(2, 3)).
     */
    fun calculateCrossedDays(fromTime: GameDateTime, toTime: GameDateTime): List<Int> {
        if (toTime.day <= fromTime.day) return emptyList()
        return ((fromTime.day + 1)..toTime.day).toList()
    }

    /**
     * Returns a [GameDateTime] set to next day's morning (08:00 AM).
     */
    fun nextMorning(currentTime: GameDateTime): GameDateTime {
        val nextDay = currentTime.day + 1
        return GameDateTime(day = nextDay, hour = 8, minute = 0)
    }

    /**
     * Calculates the duration needed to rest/wait until the next morning (08:00 AM).
     */
    fun durationToNextMorning(currentTime: GameDateTime): GameDuration {
        val target = nextMorning(currentTime)
        return currentTime.durationUntil(target)
    }

    /**
     * Validates whether the player can manually trigger "End Day / Rest until Morning".
     */
    fun validateManualEndDay(gameState: GameState): ManualEndDayValidation {
        if (gameState.activeCombat != null) {
            return ManualEndDayValidation.Blocked("Невозможно завершить день во время активного боя!")
        }
        if (gameState.activeExpedition != null && gameState.activeExpedition.currentEvent != null) {
            return ManualEndDayValidation.Blocked("Невозможно завершить день: необходимо разрешить текущее событие вылазки!")
        }
        if (gameState.isCurrentlyTraveling) {
            return ManualEndDayValidation.Blocked("Невозможно завершить день: отряд находится в пути по пустошам!")
        }
        return ManualEndDayValidation.Allowed
    }
}

sealed interface ManualEndDayValidation {
    data object Allowed : ManualEndDayValidation
    data class Blocked(val reasonRu: String) : ManualEndDayValidation

    val isAllowed: Boolean get() = this is Allowed
}
