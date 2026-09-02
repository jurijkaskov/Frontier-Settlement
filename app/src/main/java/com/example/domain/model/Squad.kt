package com.example.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.TechCyan
import com.example.ui.theme.WarningAmber

/**
 * Status of the primary expedition squad.
 */
enum class SquadStatus(val titleRu: String) {
    EMPTY("Не укомплектован"),
    PREPARING("Формируется на базе"),
    READY("Готов к экспедиции"),
    ON_EXPEDITION("В экспедиции")
}

/**
 * Squad domain model.
 * Holds references to residents by unique IDs, assigned leader, and selected transport.
 * Avoids duplicating Character objects to maintain single source of truth.
 */
data class Squad(
    val id: String = "squad_main",
    val name: String = "Экспедиционная группа «Фронтир»",
    val memberIds: List<String> = listOf("char_1", "char_2"),
    val leaderId: String? = "char_1",
    val assignedVehicleId: String? = null,
    val carriedItemIds: List<String> = emptyList(),
    val status: SquadStatus = SquadStatus.READY,
    val notes: String = "Основной разведывательно-боевой отряд аванпоста"
) {
    val memberCount: Int get() = memberIds.size
    val isEmpty: Boolean get() = memberIds.isEmpty()
    val isNotEmpty: Boolean get() = memberIds.isNotEmpty()
    val hasLeader: Boolean get() = !leaderId.isNullOrBlank() && memberIds.contains(leaderId)
}

/**
 * Single verification item in squad readiness checklist.
 */
data class SquadReadinessCheck(
    val id: String,
    val title: String,
    val isPassed: Boolean,
    val detail: String,
    val isCritical: Boolean = true
)

/**
 * Aggregated summary of squad readiness.
 */
data class SquadReadinessSummary(
    val isFullyReady: Boolean,
    val readinessPercent: Int,
    val statusTitle: String,
    val statusColor: Color,
    val checks: List<SquadReadinessCheck>,
    val blockers: List<String> = emptyList()
)

/**
 * Tactical aggregated statistics of the entire squad.
 */
data class SquadAggregatedStats(
    val memberCount: Int = 0,
    val maxCapacity: Int = 4,
    val totalAttack: Int = 0,
    val totalDefense: Int = 0,
    val totalScavenging: Int = 0,
    val totalEngineering: Int = 0,
    val totalMedical: Int = 0,
    val averageHealthPercent: Int = 100,
    val roleCounts: Map<CharacterRole, Int> = emptyMap(),
    val leader: Character? = null,
    val vehicle: Vehicle? = null,
    val isOverCapacity: Boolean = false,
    val injuredCount: Int = 0,
    val totalCarryCapacityKg: Int = 0,
    val membersWithBackpacksCount: Int = 0,
    val totalEquippedSlotsCount: Int = 0
) {
    val totalCombatPower: Int
        get() = totalAttack + totalDefense + (memberCount * 5)
}

/**
 * Outcome of a squad management operation.
 */
sealed class SquadOperationResult {
    data class Success(val message: String) : SquadOperationResult()
    data class Failure(val message: String) : SquadOperationResult()

    val isSuccess: Boolean get() = this is Success
}

/**
 * Centralized calculation & limits helper for Squad.
 */
object SquadLimits {
    const val DEFAULT_FOOT_CAPACITY = 4

    fun getMaxCapacity(vehicle: Vehicle?): Int {
        return vehicle?.maxPassengers?.coerceAtLeast(1) ?: DEFAULT_FOOT_CAPACITY
    }
}

/**
 * Pure calculation engine for squad metrics, aggregated stats, and readiness checklists.
 */
object SquadCalculator {

    /**
     * Resolves the members from the characters list based on squad IDs.
     * Automatically filters out any stale or nonexistent IDs.
     */
    fun resolveMembers(squad: Squad, characters: List<Character>): List<Character> {
        val charMap = characters.associateBy { it.id }
        return squad.memberIds.mapNotNull { charMap[it] }
    }

    /**
     * Resolves the leader Character if valid.
     */
    fun resolveLeader(squad: Squad, characters: List<Character>): Character? {
        val leaderId = squad.leaderId ?: return null
        if (!squad.memberIds.contains(leaderId)) return null
        return characters.find { it.id == leaderId }
    }

    /**
     * Computes the aggregated statistics for the squad.
     */
    fun calculateSummary(
        squad: Squad,
        characters: List<Character>,
        vehicle: Vehicle?,
        inventoryItems: List<WarehouseItem> = emptyList()
    ): SquadAggregatedStats {
        val members = resolveMembers(squad, characters)
        val maxCap = SquadLimits.getMaxCapacity(vehicle)
        val leader = resolveLeader(squad, characters)

        if (members.isEmpty()) {
            return SquadAggregatedStats(
                memberCount = 0,
                maxCapacity = maxCap,
                vehicle = vehicle,
                leader = null,
                totalCarryCapacityKg = vehicle?.capacityKg ?: 25
            )
        }

        val totalAtk = members.sumOf { it.getEffectiveStats(inventoryItems).attack }
        val totalDef = members.sumOf { it.getEffectiveStats(inventoryItems).defense }
        val totalScav = members.sumOf { it.getEffectiveStats(inventoryItems).scavengingSkill }
        val totalEng = members.sumOf { it.getEffectiveStats(inventoryItems).engineeringSkill }
        val totalMed = members.sumOf { it.getEffectiveStats(inventoryItems).medicalSkill }

        val avgHp = (members.sumOf { it.health }.toFloat() / members.sumOf { it.getEffectiveMaxHealth(inventoryItems).coerceAtLeast(1) } * 100).toInt().coerceIn(0, 100)
        val roles = members.groupingBy { it.role }.eachCount()
        val injured = members.count { it.status == CharacterStatus.INJURED || it.health < 25 }

        val membersCarryCapacity = members.sumOf { it.getEffectiveCarryCapacityKg(inventoryItems) }
        val vehicleCapacity = vehicle?.capacityKg ?: 0
        val totalCarry = vehicleCapacity + membersCarryCapacity
        val backpacksCount = members.count { it.equipment.hasBackpack }
        val equippedSlots = members.sumOf { it.equipment.equippedSlotsCount }

        return SquadAggregatedStats(
            memberCount = members.size,
            maxCapacity = maxCap,
            totalAttack = totalAtk,
            totalDefense = totalDef,
            totalScavenging = totalScav,
            totalEngineering = totalEng,
            totalMedical = totalMed,
            averageHealthPercent = avgHp,
            roleCounts = roles,
            leader = leader,
            vehicle = vehicle,
            isOverCapacity = members.size > maxCap,
            injuredCount = injured,
            totalCarryCapacityKg = totalCarry,
            membersWithBackpacksCount = backpacksCount,
            totalEquippedSlotsCount = equippedSlots
        )
    }

    /**
     * Evaluates complete readiness of squad for field expeditions.
     */
    fun calculateReadiness(
        squad: Squad,
        characters: List<Character>,
        vehicle: Vehicle?,
        inventoryItems: List<WarehouseItem> = emptyList()
    ): SquadReadinessSummary {
        val members = resolveMembers(squad, characters)
        val maxCap = SquadLimits.getMaxCapacity(vehicle)
        val leader = resolveLeader(squad, characters)

        val checks = mutableListOf<SquadReadinessCheck>()
        val blockers = mutableListOf<String>()

        // 1. Members count check
        val hasMembers = members.isNotEmpty()
        checks.add(
            SquadReadinessCheck(
                id = "check_members",
                title = "Состав отряда",
                isPassed = hasMembers,
                detail = if (hasMembers) "${members.size} бойцов в строю" else "В отряде нет участников",
                isCritical = true
            )
        )
        if (!hasMembers) blockers.add("Сначала добавьте хотя бы одного жителя в отряд.")

        // 2. Leader assigned
        val hasLeader = leader != null
        checks.add(
            SquadReadinessCheck(
                id = "check_leader",
                title = "Командир группы",
                isPassed = hasLeader,
                detail = if (hasLeader) "Командир: ${leader?.name}" else "Командир не назначен",
                isCritical = false
            )
        )
        if (!hasLeader && hasMembers) blockers.add("Рекомендуется назначить командира группы.")

        // 3. Vehicle capacity
        val fitsVehicle = members.size <= maxCap
        val vehName = vehicle?.name ?: "Пеший ход"
        checks.add(
            SquadReadinessCheck(
                id = "check_capacity",
                title = "Вместимость транспорта",
                isPassed = fitsVehicle,
                detail = "$vehName: ${members.size} / $maxCap мест",
                isCritical = true
            )
        )
        if (!fitsVehicle) blockers.add("Размер отряда (${members.size}) превышает вместимость транспорта ($maxCap).")

        // 4. Health / No critical injuries
        val noInjured = members.none { it.status == CharacterStatus.INJURED || it.health <= 20 }
        checks.add(
            SquadReadinessCheck(
                id = "check_health",
                title = "Боеготовность и здоровье",
                isPassed = noInjured,
                detail = if (noInjured) "Все участники здоровы и готовы к маршу" else "В отряде есть тяжелораненые бойцы",
                isCritical = true
            )
        )
        if (!noInjured) blockers.add("Вылечите раненых участников в медпункте перед вылазкой.")

        // 5. Equipment readiness
        val equippedSlots = members.sumOf { it.equipment.equippedSlotsCount }
        val backpacks = members.count { it.equipment.hasBackpack }
        val hasGoodGear = equippedSlots >= members.size // At least 1 item per member
        checks.add(
            SquadReadinessCheck(
                id = "check_equipment",
                title = "Снаряжение и экипировка",
                isPassed = hasGoodGear || members.isEmpty(),
                detail = if (members.isEmpty()) "Отряд пуст" else "Укомплектовано $equippedSlots слотов ($backpacks с рюкзаками)",
                isCritical = false
            )
        )

        val passedCount = checks.count { it.isPassed }
        val percent = ((passedCount.toFloat() / checks.size.toFloat()) * 100).toInt().coerceIn(0, 100)
        val isFullyReady = hasMembers && fitsVehicle && noInjured

        val (statusTitle, statusColor) = when {
            members.isEmpty() -> "Отряд не сформирован" to WarningAmber
            !fitsVehicle -> "Превышена вместимость" to CriticalRed
            !noInjured -> "Требуется медпомощь" to CriticalRed
            !hasLeader -> "Командир не выбран" to WarningAmber
            else -> "Отряд полностью готов" to SafeEmerald
        }

        return SquadReadinessSummary(
            isFullyReady = isFullyReady,
            readinessPercent = percent,
            statusTitle = statusTitle,
            statusColor = statusColor,
            checks = checks,
            blockers = blockers
        )
    }
}
