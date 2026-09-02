package com.example.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.TechCyan
import com.example.ui.theme.WarningAmber

/**
 * Temporary draft state representing user selections and configuration
 * while preparing an expedition to a target location.
 * Uses lightweight identifiers to avoid duplicating core domain models.
 */
data class ExpeditionPreparationState(
    val destinationLocationId: String,
    val originLocationId: String = "loc_base",
    val participantIds: List<String> = emptyList(),
    val leaderId: String? = null,
    val travelMode: TravelTransportMode = TravelTransportMode.FOOT,
    val selectedVehicleId: String? = null,
    val supplies: Map<ResourceType, Int> = mapOf(
        ResourceType.WATER to 6,
        ResourceType.FOOD to 4,
        ResourceType.FUEL to 0,
        ResourceType.MEDICINE to 1
    ),
    val carriedItemIds: List<String> = emptyList()
) {
    val participantCount: Int get() = participantIds.size
    val hasParticipants: Boolean get() = participantIds.isNotEmpty()
    val hasLeader: Boolean get() = !leaderId.isNullOrBlank() && participantIds.contains(leaderId)

    fun getSupplyAmount(resourceType: ResourceType): Int = supplies[resourceType] ?: 0

    fun withSupply(resourceType: ResourceType, amount: Int): ExpeditionPreparationState {
        val updatedMap = supplies.toMutableMap()
        if (amount <= 0) {
            updatedMap.remove(resourceType)
        } else {
            updatedMap[resourceType] = amount
        }
        return copy(supplies = updatedMap)
    }

    fun withToggledParticipant(characterId: String): ExpeditionPreparationState {
        val newParticipants = if (participantIds.contains(characterId)) {
            participantIds - characterId
        } else {
            participantIds + characterId
        }
        val newLeader = if (newParticipants.contains(leaderId)) {
            leaderId
        } else {
            newParticipants.firstOrNull()
        }
        return copy(
            participantIds = newParticipants,
            leaderId = newLeader
        )
    }

    fun withLeader(newLeaderId: String): ExpeditionPreparationState {
        return if (participantIds.contains(newLeaderId)) {
            copy(leaderId = newLeaderId)
        } else {
            this
        }
    }
}

/**
 * Aggregated weight and cargo breakdown for the planned expedition.
 */
data class ExpeditionCargoSummary(
    val totalCapacityKg: Int = 25,
    val suppliesWeightKg: Float = 0f,
    val gearWeightKg: Float = 0f,
    val totalCurrentWeightKg: Float = 0f,
    val freeLootCapacityKg: Float = 25f,
    val isOverloaded: Boolean = false,
    val capacityPercent: Int = 0
) {
    val loadStatusColor: Color
        get() = when {
            isOverloaded -> CriticalRed
            capacityPercent >= 80 -> WarningAmber
            capacityPercent >= 40 -> TechCyan
            else -> SafeEmerald
        }
}

/**
 * Single item in the expedition readiness checklist.
 */
data class ExpeditionCheckItem(
    val id: String,
    val title: String,
    val detail: String,
    val isPassed: Boolean,
    val isCritical: Boolean = true
)

/**
 * Centralized result of validating an expedition preparation plan against the current GameState.
 */
data class ExpeditionValidationResult(
    val canDepart: Boolean,
    val blockingIssues: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val checkItems: List<ExpeditionCheckItem> = emptyList(),
    val travelCost: TravelCost = TravelCost(),
    val cargoSummary: ExpeditionCargoSummary = ExpeditionCargoSummary()
) {
    val readinessPercent: Int
        get() = if (checkItems.isNotEmpty()) {
            ((checkItems.count { it.isPassed }.toFloat() / checkItems.size.toFloat()) * 100).toInt()
        } else 0
}

/**
 * Outcome of attempting to atomically launch an expedition.
 */
sealed class ExpeditionTransactionResult {
    abstract val message: String

    data class Success(
        val expedition: Expedition,
        val travelState: TravelState,
        val consumedSupplies: Map<ResourceType, Int>,
        override val message: String
    ) : ExpeditionTransactionResult()

    data class Failure(
        val blockingIssues: List<String>,
        override val message: String
    ) : ExpeditionTransactionResult()

    val isSuccess: Boolean get() = this is Success
}
