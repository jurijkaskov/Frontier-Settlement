package com.example.domain.model

import java.util.Locale
import kotlin.math.max

/**
 * High-level four-phase diurnal cycle dividing the 24-hour game day.
 */
enum class DayPeriod(
    val titleRu: String,
    val startHour: Int,
    val endHour: Int,
    val iconKey: String,
    val ambientDescriptionRu: String
) {
    MORNING(
        titleRu = "Утро",
        startHour = 6,
        endHour = 11,
        iconKey = "wb_twilight",
        ambientDescriptionRu = "Ранний рассвет над пустошью. Оптимальное время для выхода экспедиций."
    ),
    DAY(
        titleRu = "День",
        startHour = 12,
        endHour = 17,
        iconKey = "wb_sunny",
        ambientDescriptionRu = "Зенит солнца. Высокая активность в поселении и на торговых трактах."
    ),
    EVENING(
        titleRu = "Вечер",
        startHour = 18,
        endHour = 21,
        iconKey = "wb_sunset",
        ambientDescriptionRu = "Спуск сумерек. Отряды возвращаются на базу, в аванпосте зажигаются огни."
    ),
    NIGHT(
        titleRu = "Ночь",
        startHour = 22,
        endHour = 5,
        iconKey = "nights_stay",
        ambientDescriptionRu = "Глубокая тьма. Повышенная опасность на пустошах, генераторы работают в ночном режиме."
    );

    companion object {
        /**
         * Calculates the DayPeriod from an hour of day (0..23).
         */
        fun fromHour(hour: Int): DayPeriod {
            val normalizedHour = ((hour % 24) + 24) % 24
            return when (normalizedHour) {
                in 6..11 -> MORNING
                in 12..17 -> DAY
                in 18..21 -> EVENING
                else -> NIGHT
            }
        }
    }
}

/**
 * Immutable representation of an in-game time interval (minutes, hours, days).
 * Used across travel, exploration, events, combat, craft, and building.
 */
data class GameDuration(
    val totalMinutes: Long = 0L
) : Comparable<GameDuration> {

    init {
        require(totalMinutes >= 0L) { "GameDuration cannot be negative (got $totalMinutes minutes)." }
    }

    val minutesPart: Int get() = (totalMinutes % 60).toInt()
    val hoursPart: Int get() = ((totalMinutes / 60) % 24).toInt()
    val daysPart: Int get() = (totalMinutes / 1440).toInt()
    val totalHours: Float get() = totalMinutes / 60f

    operator fun plus(other: GameDuration): GameDuration =
        GameDuration(this.totalMinutes + other.totalMinutes)

    operator fun minus(other: GameDuration): GameDuration =
        GameDuration(max(0L, this.totalMinutes - other.totalMinutes))

    override fun compareTo(other: GameDuration): Int =
        this.totalMinutes.compareTo(other.totalMinutes)

    /**
     * Readable localized string representation (e.g. "2 д 4 ч", "1 ч 30 мин", "45 мин").
     */
    val formatted: String
        get() {
            if (totalMinutes == 0L) return "0 мин"
            val d = daysPart
            val h = hoursPart
            val m = minutesPart

            return buildString {
                if (d > 0) append("$d д ")
                if (h > 0 || (d > 0 && m > 0)) append("$h ч ")
                if (m > 0 || (d == 0 && h == 0)) append("$m мин")
            }.trim()
        }

    val isZero: Boolean get() = totalMinutes == 0L

    companion object {
        val ZERO = GameDuration(0L)

        fun ofMinutes(minutes: Int): GameDuration =
            GameDuration(max(0L, minutes.toLong()))

        fun ofHours(hours: Int): GameDuration =
            GameDuration(max(0L, hours.toLong() * 60L))

        fun ofHoursAndMinutes(hours: Int, minutes: Int): GameDuration =
            GameDuration(max(0L, hours.toLong() * 60L + minutes.toLong()))

        fun ofDays(days: Int): GameDuration =
            GameDuration(max(0L, days.toLong() * 1440L))

        fun fromHoursFloat(hoursFloat: Float): GameDuration {
            val totalMin = max(0L, (hoursFloat * 60f).toLong())
            return GameDuration(totalMin)
        }
    }
}

/**
 * Immutable in-game point in time tracking day, hour (0..23), and minute (0..59).
 * Sole source of truth for the in-game calendar and clock.
 */
data class GameDateTime(
    val day: Int = 1,
    val hour: Int = 8,
    val minute: Int = 0
) : Comparable<GameDateTime> {

    init {
        require(day >= 1) { "GameDay must be >= 1 (got $day)" }
        require(hour in 0..23) { "Hour must be in 0..23 (got $hour)" }
        require(minute in 0..59) { "Minute must be in 0..59 (got $minute)" }
    }

    /**
     * Total elapsed minutes from game epoch (Day 1, 00:00).
     */
    val totalMinutes: Long
        get() = (day.toLong() - 1L) * 1440L + hour.toLong() * 60L + minute.toLong()

    val dayPeriod: DayPeriod
        get() = DayPeriod.fromHour(hour)

    val formattedTime: String
        get() = String.format(Locale.US, "%02d:%02d", hour, minute)

    val formattedDay: String
        get() = "День $day"

    val formattedFull: String
        get() = "День $day · $formattedTime"

    val formattedWithPeriod: String
        get() = "День $day · $formattedTime (${dayPeriod.titleRu})"

    fun plusDuration(duration: GameDuration): GameDateTime {
        if (duration.isZero) return this
        val newTotal = this.totalMinutes + duration.totalMinutes
        return fromTotalMinutes(newTotal)
    }

    fun plusMinutes(minutes: Int): GameDateTime =
        plusDuration(GameDuration.ofMinutes(minutes))

    fun plusHours(hours: Int): GameDateTime =
        plusDuration(GameDuration.ofHours(hours))

    fun plusDays(days: Int): GameDateTime =
        plusDuration(GameDuration.ofDays(days))

    fun durationUntil(other: GameDateTime): GameDuration {
        val diff = max(0L, other.totalMinutes - this.totalMinutes)
        return GameDuration(diff)
    }

    override fun compareTo(other: GameDateTime): Int =
        this.totalMinutes.compareTo(other.totalMinutes)

    companion object {
        /**
         * Standard campaign canonical starting point: Day 1, 08:00 (Morning).
         */
        val START_TIME = GameDateTime(day = 1, hour = 8, minute = 0)

        /**
         * Reconstructs a GameDateTime from total minutes elapsed since Day 1, 00:00.
         */
        fun fromTotalMinutes(totalMinutes: Long): GameDateTime {
            val safeTotal = max(0L, totalMinutes)
            val d = (safeTotal / 1440L).toInt() + 1
            val remainingMin = (safeTotal % 1440L).toInt()
            val h = remainingMin / 60
            val m = remainingMin % 60
            return GameDateTime(day = d, hour = h, minute = m)
        }
    }
}

/**
 * Detailed outcome of advancing in-game time.
 * Lists the old and new timestamps and every crossed day boundary for daily tick execution.
 */
data class TimeAdvanceResult(
    val previousTime: GameDateTime,
    val newTime: GameDateTime,
    val durationAdded: GameDuration,
    val crossedDays: List<Int>
) {
    val isNewDayCrossed: Boolean
        get() = crossedDays.isNotEmpty()
}

/**
 * Structured summary produced when a new game day begins and daily maintenance executes.
 */
data class DailySummary(
    val day: Int,
    val previousDay: Int = day - 1,
    val foodProduced: Int = 0,
    val foodConsumed: Int = 0,
    val waterProduced: Int = 0,
    val waterConsumed: Int = 0,
    val materialsProduced: Int = 0,
    val creditsProduced: Int = 0,
    val medicineProduced: Int = 0,
    val fuelConsumed: Int = 0,
    val isStarving: Boolean = false,
    val isDehydrated: Boolean = false,
    val charactersHealedCount: Int = 0,
    val merchantRestocked: Boolean = false,
    val triggeredConsequences: List<String> = emptyList(),
    val summaryLogs: List<String> = emptyList(),
    val economyReport: DailyEconomyReport? = null,
    val overflowLost: Map<ResourceType, Int> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)
