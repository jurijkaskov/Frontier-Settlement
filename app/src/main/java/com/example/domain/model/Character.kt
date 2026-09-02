package com.example.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class CharacterRole(val titleRu: String) {
    SCOUT("Разведчик"),
    SOLDIER("Штурмовик"),
    ENGINEER("Инженер"),
    MEDIC("Медик"),
    SCAVENGER("Следопыт-добытчик")
}

enum class CharacterStatus(val titleRu: String) {
    READY("Готов к вылазке"),
    IN_SQUAD("В составе отряда"),
    ON_EXPEDITION("На задании"),
    INJURED("Ранен / В лазарете"),
    RESTING("Отдыхает")
}

data class CharacterStats(
    val attack: Int = 12,
    val defense: Int = 8,
    val scavengingSkill: Int = 10,
    val engineeringSkill: Int = 5,
    val medicalSkill: Int = 4
) {
    val totalSkillPower: Int
        get() = attack + defense + scavengingSkill + engineeringSkill + medicalSkill
}

data class Character(
    val id: String,
    val name: String,
    val role: CharacterRole,
    val level: Int = 1,
    val experience: Int = 0,
    val maxExperience: Int = 100,
    val health: Int = 100,
    val maxHealth: Int = 100,
    val status: CharacterStatus = CharacterStatus.READY,
    val stats: CharacterStats = CharacterStats(),
    val equipmentSummary: String = "Стандартная экипировка",
    val avatarTag: String = "char_1",
    val bio: String = "Житель аванпоста, приспособившийся к суровым условиям фронтира.",
    val specialization: String = "Универсал",
    // Extended RPG Character attributes
    val unspentSkillPoints: Int = 0,
    val morale: Int = 100,
    val energy: Int = 100,
    val traits: List<CharacterTrait> = emptyList(),
    val expeditionsCount: Int = 0,
    val daysInSettlement: Int = 1,
    val threatsNeutralizedCount: Int = 0,
    val assignedBuildingId: String? = null,
    val equipment: CharacterEquipment = CharacterEquipment(),
    val baseCarryCapacityKg: Int = 15
) {
    /**
     * Resolves all currently equipped WarehouseItem instances from the central inventory.
     */
    fun getEquippedItems(allItems: List<WarehouseItem>): Map<EquipmentSlotType, WarehouseItem> {
        if (equipment.slots.isEmpty() || allItems.isEmpty()) return emptyMap()
        val itemMap = allItems.associateBy { it.id }
        val result = mutableMapOf<EquipmentSlotType, WarehouseItem>()
        equipment.slots.forEach { (slot, itemId) ->
            if (!itemId.isNullOrBlank()) {
                itemMap[itemId]?.let { result[slot] = it }
            }
        }
        return result
    }

    fun getEquippedItemsMap(allItems: List<WarehouseItem>): Map<EquipmentSlotType, WarehouseItem> = getEquippedItems(allItems)

    fun getTotalEquippedWeightKg(allItems: List<WarehouseItem>): Float =
        getEquippedItems(allItems).values.sumOf { it.weightKg.toDouble() }.toFloat()

    /**
     * Trait- and Equipment-augmented effective stats including all passive bonuses.
     */
    fun getEffectiveStats(allItems: List<WarehouseItem> = emptyList()): CharacterStats {
        val equippedItems = getEquippedItems(allItems).values
        val eqAtk = equippedItems.sumOf { it.equipmentBonus.bonusAttack }
        val eqDef = equippedItems.sumOf { it.equipmentBonus.bonusDefense }
        val eqScav = equippedItems.sumOf { it.equipmentBonus.bonusScavenging }
        val eqEng = equippedItems.sumOf { it.equipmentBonus.bonusEngineering }
        val eqMed = equippedItems.sumOf { it.equipmentBonus.bonusMedical }

        val traitAtk = traits.sumOf { it.bonusAttack }
        val traitDef = traits.sumOf { it.bonusDefense }
        val traitScav = traits.sumOf { it.bonusScavenging }
        val traitEng = traits.sumOf { it.bonusEngineering }
        val traitMed = traits.sumOf { it.bonusMedical }

        return CharacterStats(
            attack = (stats.attack + traitAtk + eqAtk).coerceAtLeast(1),
            defense = (stats.defense + traitDef + eqDef).coerceAtLeast(1),
            scavengingSkill = (stats.scavengingSkill + traitScav + eqScav).coerceAtLeast(1),
            engineeringSkill = (stats.engineeringSkill + traitEng + eqEng).coerceAtLeast(1),
            medicalSkill = (stats.medicalSkill + traitMed + eqMed).coerceAtLeast(1)
        )
    }

    /**
     * Effective maximum health including trait and equipment bonuses.
     */
    fun getEffectiveMaxHealth(allItems: List<WarehouseItem> = emptyList()): Int {
        val equippedItems = getEquippedItems(allItems).values
        val eqHp = equippedItems.sumOf { it.equipmentBonus.bonusMaxHealth }
        val traitHp = traits.sumOf { it.bonusMaxHealth }
        return (maxHealth + traitHp + eqHp).coerceAtLeast(20)
    }

    /**
     * Calculates total carry capacity for expeditions (Base + Traits + Backpack/Gear bonuses).
     */
    fun getEffectiveCarryCapacityKg(allItems: List<WarehouseItem> = emptyList()): Int {
        val equippedItems = getEquippedItems(allItems).values
        val gearCapacityBonus = equippedItems.sumOf { it.equipmentBonus.bonusCarryCapacityKg }
        val strengthBonus = (stats.attack / 4) + (stats.defense / 4)
        return (baseCarryCapacityKg + gearCapacityBonus + strengthBonus).coerceAtLeast(5)
    }

    /**
     * Generates a readable string summary of equipped gear or fallback.
     */
    fun getDynamicEquipmentSummary(allItems: List<WarehouseItem>): String {
        val equipped = getEquippedItems(allItems)
        if (equipped.isEmpty()) return "Нет экипированного снаряжения"
        return equipped.values.joinToString(separator = ", ") { it.name }
    }

    /**
     * Backward-compatible property for effective stats when item list is not directly supplied.
     */
    val effectiveStats: CharacterStats
        get() = getEffectiveStats(emptyList())

    val effectiveMaxHealth: Int
        get() = getEffectiveMaxHealth(emptyList())

    val healthFraction: Float
        get() = if (effectiveMaxHealth > 0) (health.toFloat() / effectiveMaxHealth.toFloat()).coerceIn(0f, 1f) else 0f

    val xpFraction: Float
        get() = if (maxExperience > 0) (experience.toFloat() / maxExperience.toFloat()).coerceIn(0f, 1f) else 0f

    val moraleFraction: Float
        get() = (morale.toFloat() / 100f).coerceIn(0f, 1f)

    val energyFraction: Float
        get() = (energy.toFloat() / 100f).coerceIn(0f, 1f)

    val isAlive: Boolean
        get() = health > 0

    val canLevelUp: Boolean
        get() = experience >= maxExperience

    val moraleStatusLabel: String
        get() = when {
            morale >= 80 -> "Бодрый дух"
            morale >= 50 -> "Стабильно"
            morale >= 25 -> "Тревога"
            else -> "Стресс / Паника"
        }

    val moraleStatusColor: Color
        get() = when {
            morale >= 80 -> SafeEmerald
            morale >= 50 -> TechCyan
            morale >= 25 -> WarningAmber
            else -> DangerCrimson
        }

    val rolePerkSummary: String
        get() = when (role) {
            CharacterRole.SCOUT -> "Повышает шанс обнаружения редких локаций и снижает риск засады на 25%"
            CharacterRole.SOLDIER -> "Даёт +30% к урону отряда и удерживает оборону при нападениях мутантов"
            CharacterRole.ENGINEER -> "Ускоряет починку транспорта и увеличивает выход материалов в мастерской"
            CharacterRole.MEDIC -> "Ускоряет регенерацию бойцов в лазарете и снижает расход медикаментов"
            CharacterRole.SCAVENGER -> "+35% к объёму находимых ценных компонентов и металлолома в руинах"
        }

    val roleIconKey: String
        get() = when (role) {
            CharacterRole.SCOUT -> "explore"
            CharacterRole.SOLDIER -> "shield"
            CharacterRole.ENGINEER -> "build"
            CharacterRole.MEDIC -> "medical_services"
            CharacterRole.SCAVENGER -> "backpack"
        }
}

