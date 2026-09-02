package com.example.domain.model

/**
 * Main research categories according to the Frontier Settlement specification (Point 9):
 * 1. SETTLEMENT (Поселение) - storage, base infrastructure, housing efficiency, power grid.
 * 2. PRODUCTION (Производство) - workshop blueprints, recycling, tools, components.
 * 3. ECONOMY (Экономика) - trade ties, caravan frequency, barter discounts, market catalog.
 * 4. SURVIVAL (Выживание) - field medicine, combat armor, sensors, expedition equipment.
 */
enum class TechCategory(val id: String, val titleRu: String, val descriptionRu: String) {
    SETTLEMENT(
        id = "settlement",
        titleRu = "Поселение",
        descriptionRu = "Улучшение инфраструктуры базы, вместимости складов и энергосетей."
    ),
    PRODUCTION(
        id = "production",
        titleRu = "Производство",
        descriptionRu = "Расширение возможностей мастерской, новые чертежи и эффективная переработка."
    ),
    ECONOMY(
        id = "economy",
        titleRu = "Экономика",
        descriptionRu = "Торговые соглашения, привлечение караванов и скидки на бартер."
    ),
    SURVIVAL(
        id = "survival",
        titleRu = "Выживание",
        descriptionRu = "Полевая медицина, защитное снаряжение и сканеры для дальних экспедиций."
    ),

    // Legacy compatibility aliases if needed
    INFRASTRUCTURE(
        id = "infrastructure",
        titleRu = "Поселение",
        descriptionRu = "Инфраструктура и хранилища базы."
    ),
    MILITARY(
        id = "military",
        titleRu = "Выживание",
        descriptionRu = "Военное снаряжение и защита."
    ),
    LOGISTICS(
        id = "logistics",
        titleRu = "Экономика",
        descriptionRu = "Логистика и транспортировка."
    )
}

/**
 * Concrete gameplay effects applied by researched technologies.
 * The system evaluates active effects dynamically from the single source of truth (researched techs list).
 */
sealed interface TechEffect {
    val summaryRu: String

    /**
     * Permanent increase to warehouse storage capacity.
     */
    data class StorageCapacityBoost(
        val additionalCapacity: Int,
        override val summaryRu: String = "+$additionalCapacity к вместимости склада"
    ) : TechEffect

    /**
     * Multiplier percentage for daily resource output (e.g. +25% Water from wells).
     */
    data class ResourceProductionMultiplier(
        val resourceType: ResourceType,
        val multiplierPercent: Int,
        override val summaryRu: String = "+$multiplierPercent% к добыче ${resourceType.titleRu}"
    ) : TechEffect

    /**
     * Unlocks a specific crafting blueprint in the Settlement Workshop.
     */
    data class RecipeUnlock(
        val recipeId: String,
        val recipeNameRu: String,
        override val summaryRu: String = "Чертеж «$recipeNameRu» в Мастерской"
    ) : TechEffect

    /**
     * Economic benefit: Merchant discount and rare catalog unlocks.
     */
    data class TradeBonus(
        val discountPercent: Int,
        val unlocksRareGoods: Boolean = false,
        override val summaryRu: String = if (unlocksRareGoods) {
            "Скидка $discountPercent% и редкие товары у торговцев"
        } else {
            "Скидка $discountPercent% у торговцев"
        }
    ) : TechEffect

    /**
     * Combat and physical protection bonus for all squad survivors.
     */
    data class SquadStatBonus(
        val attackBonus: Int = 0,
        val defenseBonus: Int = 0,
        val healthBonus: Int = 0,
        override val summaryRu: String = buildString {
            val parts = mutableListOf<String>()
            if (attackBonus > 0) parts.add("+$attackBonus к Атаке")
            if (defenseBonus > 0) parts.add("+$defenseBonus к Защите")
            if (healthBonus > 0) parts.add("+$healthBonus к HP")
            append(parts.joinToString(", ") + " всем бойцам")
        }
    ) : TechEffect

    /**
     * Increases vehicle cargo volume for expeditions.
     */
    data class VehicleCargoMultiplier(
        val multiplierPercent: Int,
        override val summaryRu: String = "+$multiplierPercent% к грузоподъёмности техники"
    ) : TechEffect

    /**
     * Unlocks a hidden wasteland location on the world map.
     */
    data class LocationUnlock(
        val locationId: String,
        val locationNameRu: String,
        override val summaryRu: String = "Открывает локацию «$locationNameRu»"
    ) : TechEffect

    /**
     * Reduces daily generator fuel consumption.
     */
    data class FuelEfficiency(
        val reductionPercent: Int,
        override val summaryRu: String = "-$reductionPercent% к расходу топлива генератора"
    ) : TechEffect

    /**
     * Increases passive health regeneration in the medical clinic.
     */
    data class MedicalEfficiency(
        val regenBonusHp: Int,
        override val summaryRu: String = "+$regenBonusHp HP/день регенерации в медпункте"
    ) : TechEffect
}

/**
 * Universal prerequisite and cost requirements for unlocking a research technology.
 */
data class ResearchRequirement(
    val resourceCosts: Map<ResourceType, Int> = emptyMap(),
    val minLabLevel: Int = 1,
    val minSettlementLevel: Int = 1,
    val prerequisiteTechIds: List<String> = emptyList()
) {
    val costMoney: Int get() = resourceCosts[ResourceType.MONEY] ?: 0
    val costMaterials: Int get() = resourceCosts[ResourceType.MATERIALS] ?: 0
}

/**
 * Research status evaluated dynamically by the central validation engine.
 */
enum class TechStatus(val titleRu: String) {
    RESEARCHED("Изучено"),
    AVAILABLE("Доступно"),
    INSUFFICIENT_RESOURCES("Не хватает ресурсов"),
    LOCKED_DEPENDENCY("Требуется технология"),
    LOCKED_LAB_LEVEL("Требуется улучшить лабораторию"),
    LOCKED_SETTLEMENT_LEVEL("Требуется уровень поселения"),
    LOCKED_LAB_UNBUILT("Лаборатория не построена")
}

/**
 * Central model representing a single research technology / blueprint.
 */
data class ResearchTech(
    val id: String,
    val title: String,
    val category: TechCategory,
    val tier: Int = 1,
    val description: String,
    val loreRu: String = "",
    val requirements: ResearchRequirement = ResearchRequirement(),
    val effects: List<TechEffect> = emptyList(),
    val effectSummary: String = effects.firstOrNull()?.summaryRu ?: "",
    val iconKey: String = "science",
    val isResearched: Boolean = false
) {
    // Backwards compatibility properties
    val costCredits: Int get() = requirements.costMoney
    val costMaterials: Int get() = requirements.costMaterials
    val isAvailable: Boolean get() = !isResearched
}

/**
 * Status report of an individual requirement check.
 */
data class TechRequirementStatus(
    val isSatisfied: Boolean,
    val labelRu: String,
    val currentProgressRu: String? = null
)

/**
 * Comprehensive runtime validation report for UI rendering and error prevention.
 */
data class TechValidationInfo(
    val tech: ResearchTech,
    val status: TechStatus,
    val isLabBuilt: Boolean,
    val labLevel: Int,
    val settlementLevel: Int,
    val missingResources: Map<ResourceType, Int>,
    val unsatisfiedPrerequisites: List<ResearchTech>,
    val allRequirements: List<TechRequirementStatus>
) {
    val canBeResearched: Boolean get() = status == TechStatus.AVAILABLE
}

/**
 * Atomic transaction outcome for conducting research.
 */
sealed interface ResearchTransactionResult {
    val isSuccess: Boolean
    val message: String

    data class Success(
        val tech: ResearchTech,
        val consumedResources: Map<ResourceType, Int>,
        val appliedEffects: List<TechEffect>,
        override val message: String
    ) : ResearchTransactionResult {
        override val isSuccess: Boolean get() = true
    }

    data class Failure(
        val tech: ResearchTech?,
        val status: TechStatus,
        override val message: String,
        val missingResources: Map<ResourceType, Int> = emptyMap()
    ) : ResearchTransactionResult {
        override val isSuccess: Boolean get() = false
    }
}
