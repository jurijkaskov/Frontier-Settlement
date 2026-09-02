package com.example.core.format

import com.example.domain.model.GameDateTime
import com.example.domain.model.ResourceType
import com.example.domain.model.reputation.ReputationTier

/**
 * Centralized presentation formatters for formatting numbers, weights, times, and domain entities into readable strings.
 */
object GameFormatters {

    fun formatWeight(kg: Int): String {
        return "$kg кг"
    }

    fun formatWeight(currentKg: Int, maxKg: Int): String {
        return "$currentKg / $maxKg кг"
    }

    fun formatDistance(km: Int): String {
        return "$km км"
    }

    fun formatResource(amount: Int, type: ResourceType): String {
        return "$amount ${type.nameRu}"
    }

    fun formatDateTime(dateTime: GameDateTime): String {
        return "День ${dateTime.day}, ${String.format("%02d:%02d", dateTime.hour, dateTime.minute)}"
    }

    fun formatTimeOnly(dateTime: GameDateTime): String {
        return String.format("%02d:%02d", dateTime.hour, dateTime.minute)
    }

    fun formatReputationPoints(points: Int): String {
        return if (points >= 0) "+$points" else "$points"
    }

    fun formatReputationTier(tier: ReputationTier): String {
        return tier.titleRu
    }
}
