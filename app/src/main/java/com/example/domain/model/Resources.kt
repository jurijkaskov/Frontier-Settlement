package com.example.domain.model


/**
 * Type of game resource in Frontier Settlement.
 * Designed to be easily extensible for future items (medicine, ammo, rare alloys, electronic parts, etc.).
 *
 * @param id Unique string identifier
 * @param nameRu Localized display name
 * @param unitSize Volume occupied in warehouse storage per 1 unit. Money has unitSize = 0 (occupies no storage).
 * @param isPhysical Whether the resource is a physical supply stored in the warehouse
 * @param defaultDailyConsumption Default base daily consumption by colony systems (if applicable)
 */
enum class ResourceType(
    val id: String,
    val nameRu: String,
    val unitSize: Int,
    val isPhysical: Boolean,
    val defaultDailyConsumption: Int = 0
) {
    MONEY("money", "Кредиты", unitSize = 0, isPhysical = false),
    FOOD("food", "Провизия", unitSize = 1, isPhysical = true, defaultDailyConsumption = 18),
    WATER("water", "Очищенная вода", unitSize = 1, isPhysical = true, defaultDailyConsumption = 20),
    FUEL("fuel", "Топливо", unitSize = 1, isPhysical = true, defaultDailyConsumption = 5),
    MATERIALS("materials", "Стройматериалы", unitSize = 1, isPhysical = true),
    MEDICINE("medicine", "Медикаменты", unitSize = 1, isPhysical = true),
    AMMO("ammo", "Боеприпасы", unitSize = 1, isPhysical = true),
    COMPONENTS("components", "Электроника и детали", unitSize = 1, isPhysical = true),
    RARE_ALLOY("rare_alloy", "Редкие сплавы", unitSize = 2, isPhysical = true);

    val titleRu: String get() = nameRu

    val symbol: String
        get() = when (this) {
            MONEY -> "💰"
            FOOD -> "🍞"
            WATER -> "💧"
            FUEL -> "⛽"
            MATERIALS -> "📦"
            MEDICINE -> "💊"
            AMMO -> "🎯"
            COMPONENTS -> "⚙️"
            RARE_ALLOY -> "💎"
        }

    companion object {
        fun fromId(id: String): ResourceType? {
            return entries.find { it.id.equals(id, ignoreCase = true) }
        }
    }
}

/**
 * Resource category classification.
 */
enum class ResourceCategory(val titleRu: String) {
    CURRENCY("Валюта и финансы"),
    SURVIVAL("Жизнеобеспечение"),
    LOGISTICS("Энергия и логистика"),
    CONSTRUCTION("Строительство и производство"),
    MILITARY("Военное снаряжение"),
    SPECIAL("Специальные материалы")
}

/**
 * Metadata descriptor for game resources.
 */
data class ResourceDescriptor(
    val type: ResourceType,
    val category: ResourceCategory,
    val descriptionRu: String,
    val baseMarketValueCredits: Int,
    val unitSize: Int = type.unitSize,
    val lowThreshold: Int = 30,
    val criticalThreshold: Int = 10,
    val warehouseCategory: WarehouseFilterCategory = WarehouseFilterCategory.PRIMARY,
    val purposeRu: String = "",
    val sourcesRu: List<String> = emptyList(),
    val usesRu: List<String> = emptyList()
)

/**
 * Registry of resource descriptors.
 */
object ResourceRegistry {
    val descriptors: Map<ResourceType, ResourceDescriptor> = mapOf(
        ResourceType.MONEY to ResourceDescriptor(
            type = ResourceType.MONEY,
            category = ResourceCategory.CURRENCY,
            warehouseCategory = WarehouseFilterCategory.VALUABLES,
            purposeRu = "Универсальная валюта для торговли и найма",
            descriptionRu = "Универсальные электронные кредиты Торговой Гильдии пустошей. Используются для расчётов с караванщиками, найма бойцов и оплаты инженерных контрактов. Хранятся на защищённых электронных терминалах и не занимают физического места на складе.",
            baseMarketValueCredits = 1,
            lowThreshold = 100,
            criticalThreshold = 20,
            sourcesRu = listOf(
                "Торговый доход поселения (+25 кр./день)",
                "Награды за выполнение квестов и контрактов",
                "Продажа излишков припасов на рынке"
            ),
            usesRu = listOf(
                "Строительство и улучшение объектов базы",
                "Покупка материалов и топлива у торговцев",
                "Оплата научных исследований"
            )
        ),
        ResourceType.FOOD to ResourceDescriptor(
            type = ResourceType.FOOD,
            category = ResourceCategory.SURVIVAL,
            warehouseCategory = WarehouseFilterCategory.PRIMARY,
            purposeRu = "Пропитание жителей и сухие пайки для вылазок",
            descriptionRu = "Концентраты, пастеризованные пайки и свежий урожай с гидропонных ферм. Необходимы для ежедневного выживания населения аванпоста и комплектования сухих пайков для дальних экспедиций в пустошь.",
            baseMarketValueCredits = 4,
            lowThreshold = 40,
            criticalThreshold = 18,
            sourcesRu = listOf(
                "Гидропонная ферма (+25 ед./день)",
                "Автономный биокупол (+50 ед./день)",
                "Сбор припасов в заброшенных супермаркетах и бункерах"
            ),
            usesRu = listOf(
                "Ежедневное потребление жителями базы (-1 ед./чел.)",
                "Снаряжение отрядов в дальние экспедиции",
                "Обмен на рынке"
            )
        ),
        ResourceType.WATER to ResourceDescriptor(
            type = ResourceType.WATER,
            category = ResourceCategory.SURVIVAL,
            warehouseCategory = WarehouseFilterCategory.PRIMARY,
            purposeRu = "Питьевая вода, гигиена и полив ферм",
            descriptionRu = "Глубоко очищенная питьевая вода из артезианских скважин аванпоста. Проходит многоступенчатую ионизацию и очистку от радионуклидов. Критический ресурс для людей и полива гидропоники.",
            baseMarketValueCredits = 3,
            lowThreshold = 50,
            criticalThreshold = 20,
            sourcesRu = listOf(
                "Скважинный фильтр (+30 ед./день)",
                "Очистные сооружения и сборники конденсата",
                "Торговые караваны Пустоши"
            ),
            usesRu = listOf(
                "Ежедневный расход жителями поселения",
                "Полив гидропонных культур",
                "Обязательный запас для разведывательных групп"
            )
        ),
        ResourceType.FUEL to ResourceDescriptor(
            type = ResourceType.FUEL,
            category = ResourceCategory.LOGISTICS,
            warehouseCategory = WarehouseFilterCategory.PRIMARY,
            purposeRu = "Энергия дизель-генератора и заправка транспорта",
            descriptionRu = "Очищенное дизельное топливо и концентрированные энергоячейки. Требуются для стабильной работы генераторов базы и заправки разведывательного транспорта для дальних вылазок.",
            baseMarketValueCredits = 8,
            lowThreshold = 25,
            criticalThreshold = 10,
            sourcesRu = listOf(
                "Заброшенные АЗС, военные склады и промзоны",
                "Нефтеперерабатывающие установки",
                "Покупка у топливных караванов"
            ),
            usesRu = listOf(
                "Дизель-генератор базы (-5 ед./день)",
                "Заправка багги, грузовиков и броневиков",
                "Обеспечение работы оборонительных систем"
            )
        ),
        ResourceType.MATERIALS to ResourceDescriptor(
            type = ResourceType.MATERIALS,
            category = ResourceCategory.CONSTRUCTION,
            warehouseCategory = WarehouseFilterCategory.MATERIALS,
            purposeRu = "Возведение, улучшение и ремонт инфраструктуры",
            descriptionRu = "Металлопрокат, бетонные композиты, арматура и переплавленный утиль. Главный ресурс для возведения и улучшения построек, укрепления защитного периметра и ремонта техники.",
            baseMarketValueCredits = 6,
            lowThreshold = 50,
            criticalThreshold = 15,
            sourcesRu = listOf(
                "Разбор металлолома и конструкций в руинах",
                "Мастерская аванпоста (+15 ед./день)",
                "Разведочные вылазки на заводы и шахты"
            ),
            usesRu = listOf(
                "Строительство новых зданий базы",
                "Модернизация склада, ферм и штаба",
                "Ремонт техники и защитных турелей"
            )
        ),
        ResourceType.MEDICINE to ResourceDescriptor(
            type = ResourceType.MEDICINE,
            category = ResourceCategory.SURVIVAL,
            warehouseCategory = WarehouseFilterCategory.ITEMS,
            purposeRu = "Лечение раненых, полевая хирургия и стимуляторы",
            descriptionRu = "Антибиотики, биостимуляторы, обезболивающие и стерильные перевязочные пакеты. Ускоряют лечение раненых бойцов в медпункте и снижают риски во время опасных вылазок.",
            baseMarketValueCredits = 15,
            lowThreshold = 10,
            criticalThreshold = 2,
            sourcesRu = listOf(
                "Заброшенные полевые госпитали и аптеки",
                "Лаборатория автономного биокупола",
                "Торговля с бродячими докторами"
            ),
            usesRu = listOf(
                "Лечение ранений и травм бойцов отряда",
                "Аптечки первой помощи в экспедициях",
                "Борьба с эпидемиями в поселении"
            )
        ),
        ResourceType.AMMO to ResourceDescriptor(
            type = ResourceType.AMMO,
            category = ResourceCategory.MILITARY,
            warehouseCategory = WarehouseFilterCategory.ITEMS,
            purposeRu = "Боезапас стрелкового оружия и охранных турелей",
            descriptionRu = "Патроны различных калибров, картечь и бронебойные боеприпасы для штурмового оружия отряда и стационарных оборонительных пулемётов периметра.",
            baseMarketValueCredits = 12,
            lowThreshold = 20,
            criticalThreshold = 5,
            sourcesRu = listOf(
                "Военные бункеры, блокпосты и оружейные схроны",
                "Оружейная лаборатория базы",
                "Трофеи после отражения рейдов бандитов"
            ),
            usesRu = listOf(
                "Снаряжение штурмовых групп в боевых миссиях",
                "Оборонительные турели периметра",
                "Отражение внезапных нападений мутантов"
            )
        ),
        ResourceType.COMPONENTS to ResourceDescriptor(
            type = ResourceType.COMPONENTS,
            category = ResourceCategory.CONSTRUCTION,
            warehouseCategory = WarehouseFilterCategory.MATERIALS,
            purposeRu = "Высокотехнологичная электроника и сервоприводы",
            descriptionRu = "Микросхемы, сервомоторы, интерфейсные платы и процессоры довоенного производства. Необходимы для продвинутых исследований и глубокой модернизации систем аванпоста.",
            baseMarketValueCredits = 25,
            lowThreshold = 10,
            criticalThreshold = 2,
            sourcesRu = listOf(
                "Серверные комплексы, лаборатории и заводы робототехники",
                "Торговая гильдия Пустоши",
                "Разбор высокотехнологичных реликвий"
            ),
            usesRu = listOf(
                "Научные исследования в лаборатории",
                "Строительство радиовышки и биокупола",
                "Улучшение систем сканирования техники"
            )
        ),
        ResourceType.RARE_ALLOY to ResourceDescriptor(
            type = ResourceType.RARE_ALLOY,
            category = ResourceCategory.SPECIAL,
            warehouseCategory = WarehouseFilterCategory.VALUABLES,
            purposeRu = "Титано-вольфрамовые слитки для элитного снаряжения",
            descriptionRu = "Титаново-вольфрамовые и композитные слитки довоенной выплавки высокой плотности. Имеют повышенную массу (2 ед. склада на 1 шт.). Необходимы для создания топового оружия и брони.",
            baseMarketValueCredits = 45,
            lowThreshold = 5,
            criticalThreshold = 1,
            sourcesRu = listOf(
                "Экспедиции в эпические аномальные зоны и шахты",
                "Подземные бункеры спецназначения",
                "Редкие торговые караваны элитных фракций"
            ),
            usesRu = listOf(
                "Создание легендарной брони и оружия",
                "Финальные уровни укреплений периметра",
                "Высокодоходная торговля на чёрном рынке"
            )
        )
    )

    fun getDescriptor(type: ResourceType): ResourceDescriptor {
        return descriptors[type] ?: ResourceDescriptor(
            type = type,
            category = ResourceCategory.SPECIAL,
            descriptionRu = type.nameRu,
            baseMarketValueCredits = 10,
            warehouseCategory = WarehouseFilterCategory.MATERIALS
        )
    }
}

/**
 * Level of urgency/depletion for a given resource.
 */
enum class ResourceStateLevel {
    NORMAL,
    LOW,
    CRITICAL
}

/**
 * Detailed result of any resource addition or consumption operation.
 */
sealed interface ResourceOperationResult {
    val isSuccess: Boolean
    val message: String

    /**
     * Operation fully succeeded.
     */
    data class Success(
        val type: ResourceType? = null,
        val amountChanged: Int = 0,
        override val message: String = "Операция успешно выполнена."
    ) : ResourceOperationResult {
        override val isSuccess: Boolean get() = true
    }

    /**
     * Operation partially succeeded (e.g. warehouse became full during addition).
     */
    data class PartialSuccess(
        val type: ResourceType,
        val requestedAmount: Int,
        val actualAmountAdded: Int,
        val rejectedAmount: Int,
        override val message: String
    ) : ResourceOperationResult {
        override val isSuccess: Boolean get() = true
    }

    /**
     * Operation failed (e.g. not enough resources or warehouse completely full).
     */
    data class Failure(
        val reason: FailureReason,
        override val message: String,
        val deficitType: ResourceType? = null,
        val deficitAmount: Int = 0
    ) : ResourceOperationResult {
        override val isSuccess: Boolean get() = false
    }

    enum class FailureReason {
        INSUFFICIENT_RESOURCE,
        INSUFFICIENT_STORAGE,
        STORAGE_FULL,
        INVALID_AMOUNT
    }
}

/**
 * Core game resources model for Frontier Settlement.
 * Represents credits (money) and physical supplies stored in the settlement warehouse.
 *
 * Implements:
 * - Direct named access for primary resources (money, food, water, fuel, materials);
 * - Generic dictionary access for future extensible resources (medicine, ammo, components, etc.);
 * - Dynamic warehouse volume computation without data desynchronization;
 * - Safe addition and consumption methods preventing negative values and storage overflows;
 * - Bundle checking and atomic batch transactions.
 */
data class GameResources(
    val money: Int = 1200,          // Credits (does not occupy warehouse storage: unitSize = 0)
    val food: Int = 150,            // Consumed by population (unitSize = 1)
    val water: Int = 180,           // Consumed by population (unitSize = 1)
    val fuel: Int = 80,             // Used by vehicles and generators (unitSize = 1)
    val materials: Int = 220,       // Used for construction & upgrades (unitSize = 1)
    val warehouseMaxCapacity: Int = 800,
    val extraResources: Map<ResourceType, Int> = emptyMap()
) {

    /**
     * Dynamic calculation of stored volume in the warehouse.
     * Calculated from each physical resource amount multiplied by its individual unitSize.
     * Money has unitSize = 0 and is excluded from storage.
     */
    val totalStoredVolume: Int
        get() {
            val primaryVolume = (food * ResourceType.FOOD.unitSize) +
                    (water * ResourceType.WATER.unitSize) +
                    (fuel * ResourceType.FUEL.unitSize) +
                    (materials * ResourceType.MATERIALS.unitSize)

            val extraVolume = extraResources.entries.sumOf { (type, amount) ->
                if (type.isPhysical) amount * type.unitSize else 0
            }

            return primaryVolume + extraVolume
        }

    val isStorageFull: Boolean
        get() = totalStoredVolume >= warehouseMaxCapacity

    val storageUsageFraction: Float
        get() = if (warehouseMaxCapacity > 0) {
            (totalStoredVolume.toFloat() / warehouseMaxCapacity.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val availableCapacity: Int
        get() = (warehouseMaxCapacity - totalStoredVolume).coerceAtLeast(0)

    /**
     * Get the quantity of a specific resource by ResourceType enum.
     */
    operator fun get(type: ResourceType): Int {
        return when (type) {
            ResourceType.MONEY -> money
            ResourceType.FOOD -> food
            ResourceType.WATER -> water
            ResourceType.FUEL -> fuel
            ResourceType.MATERIALS -> materials
            else -> extraResources[type] ?: 0
        }
    }

    /**
     * Create a copy with an updated amount for a specific ResourceType.
     */
    fun withResource(type: ResourceType, amount: Int): GameResources {
        val nonNegativeAmount = amount.coerceAtLeast(0)
        return when (type) {
            ResourceType.MONEY -> copy(money = nonNegativeAmount)
            ResourceType.FOOD -> copy(food = nonNegativeAmount)
            ResourceType.WATER -> copy(water = nonNegativeAmount)
            ResourceType.FUEL -> copy(fuel = nonNegativeAmount)
            ResourceType.MATERIALS -> copy(materials = nonNegativeAmount)
            else -> {
                val updatedExtras = extraResources.toMutableMap()
                if (nonNegativeAmount > 0) {
                    updatedExtras[type] = nonNegativeAmount
                } else {
                    updatedExtras.remove(type)
                }
                copy(extraResources = updatedExtras)
            }
        }
    }

    /**
     * Checks if the player has at least [amount] of [type].
     */
    fun hasResource(type: ResourceType, amount: Int): Boolean {
        if (amount <= 0) return true
        return get(type) >= amount
    }

    /**
     * Checks if the player can afford a map of required resources.
     */
    fun hasResources(required: Map<ResourceType, Int>): Boolean {
        return required.all { (type, needed) -> hasResource(type, needed) }
    }

    /**
     * Legacy afford check for a GameResources object.
     */
    fun canAfford(cost: GameResources): Boolean {
        if (money < cost.money || food < cost.food || water < cost.water || fuel < cost.fuel || materials < cost.materials) {
            return false
        }
        return cost.extraResources.all { (type, amount) -> get(type) >= amount }
    }

    /**
     * Calculates the storage space required to store [amount] of [type].
     */
    fun calculateRequiredSpace(type: ResourceType, amount: Int): Int {
        if (!type.isPhysical || amount <= 0) return 0
        return amount * type.unitSize
    }

    /**
     * Checks if the warehouse can accommodate [amount] of [type].
     */
    fun canStore(type: ResourceType, amount: Int): Boolean {
        if (!type.isPhysical || amount <= 0) return true
        return calculateRequiredSpace(type, amount) <= availableCapacity
    }

    /**
     * Checks if the warehouse can store a bundle of incoming resources.
     */
    fun canStoreBundle(bundle: Map<ResourceType, Int>): Boolean {
        val totalSpaceNeeded = bundle.entries.sumOf { (type, amount) ->
            if (type.isPhysical && amount > 0) amount * type.unitSize else 0
        }
        return totalSpaceNeeded <= availableCapacity
    }

    /**
     * Safe resource addition with capacity checking.
     *
     * @param type The type of resource to add
     * @param amount Amount to add (must be > 0)
     * @param allowPartial If true, adds as much as possible if storage is constrained. If false, rejects completely when space is insufficient.
     * @return Pair of updated GameResources and the OperationResult.
     */
    fun addResourceSafe(
        type: ResourceType,
        amount: Int,
        allowPartial: Boolean = true
    ): Pair<GameResources, ResourceOperationResult> {
        if (amount <= 0) {
            return this to ResourceOperationResult.Failure(
                reason = ResourceOperationResult.FailureReason.INVALID_AMOUNT,
                message = "Количество ресурса должно быть больше нуля."
            )
        }

        // Money does not occupy warehouse storage
        if (!type.isPhysical) {
            val updated = withResource(type, get(type) + amount)
            return updated to ResourceOperationResult.Success(
                type = type,
                amountChanged = amount,
                message = "Получено: +$amount ${type.nameRu}"
            )
        }

        val spaceNeeded = amount * type.unitSize
        val freeSpace = availableCapacity

        if (spaceNeeded <= freeSpace) {
            // Full addition fits in warehouse
            val updated = withResource(type, get(type) + amount)
            return updated to ResourceOperationResult.Success(
                type = type,
                amountChanged = amount,
                message = "Добавлено на склад: +$amount ${type.nameRu}"
            )
        }

        if (!allowPartial) {
            return this to ResourceOperationResult.Failure(
                reason = ResourceOperationResult.FailureReason.INSUFFICIENT_STORAGE,
                message = "Недостаточно места на складе для размещения $amount ед. «${type.nameRu}». Требуется $spaceNeeded ед. места, свободно $freeSpace.",
                deficitType = type,
                deficitAmount = spaceNeeded - freeSpace
            )
        }

        // Partial addition
        val maxUnitsCanAdd = if (type.unitSize > 0) freeSpace / type.unitSize else 0
        if (maxUnitsCanAdd <= 0) {
            return this to ResourceOperationResult.Failure(
                reason = ResourceOperationResult.FailureReason.STORAGE_FULL,
                message = "Склад полностью заполнен! Невозможно разместить «${type.nameRu}».",
                deficitType = type,
                deficitAmount = spaceNeeded
            )
        }

        val updated = withResource(type, get(type) + maxUnitsCanAdd)
        val rejected = amount - maxUnitsCanAdd
        return updated to ResourceOperationResult.PartialSuccess(
            type = type,
            requestedAmount = amount,
            actualAmountAdded = maxUnitsCanAdd,
            rejectedAmount = rejected,
            message = "Склад заполнен! Добавлено $maxUnitsCanAdd из $amount ед. «${type.nameRu}» (не поместилось: $rejected ед.)."
        )
    }

    /**
     * Safe resource consumption ensuring values never drop below zero.
     *
     * @param type The type of resource to consume
     * @param amount Amount to consume
     * @return Pair of updated GameResources and the OperationResult.
     */
    fun consumeResourceSafe(
        type: ResourceType,
        amount: Int
    ): Pair<GameResources, ResourceOperationResult> {
        if (amount <= 0) {
            return this to ResourceOperationResult.Failure(
                reason = ResourceOperationResult.FailureReason.INVALID_AMOUNT,
                message = "Количество для расхода должно быть больше нуля."
            )
        }

        val currentAmount = get(type)
        if (currentAmount < amount) {
            return this to ResourceOperationResult.Failure(
                reason = ResourceOperationResult.FailureReason.INSUFFICIENT_RESOURCE,
                message = "Недостаточно ресурса «${type.nameRu}»! Требуется: $amount, в наличии: $currentAmount.",
                deficitType = type,
                deficitAmount = amount - currentAmount
            )
        }

        val updated = withResource(type, currentAmount - amount)
        return updated to ResourceOperationResult.Success(
            type = type,
            amountChanged = -amount,
            message = "Израсходовано: -$amount ${type.nameRu}"
        )
    }

    /**
     * Safely consumes a bundle/map of resources atomically.
     * If any required resource is lacking, the entire transaction is aborted.
     */
    fun consumeBundleSafe(
        bundle: Map<ResourceType, Int>
    ): Pair<GameResources, ResourceOperationResult> {
        // Check all requirements first
        for ((type, needed) in bundle) {
            if (needed <= 0) continue
            val current = get(type)
            if (current < needed) {
                return this to ResourceOperationResult.Failure(
                    reason = ResourceOperationResult.FailureReason.INSUFFICIENT_RESOURCE,
                    message = "Недостаточно ресурса «${type.nameRu}»! Требуется: $needed, в наличии: $current.",
                    deficitType = type,
                    deficitAmount = needed - current
                )
            }
        }

        var currentResources = this
        for ((type, needed) in bundle) {
            if (needed > 0) {
                currentResources = currentResources.withResource(type, currentResources.get(type) - needed)
            }
        }

        return currentResources to ResourceOperationResult.Success(
            message = "Ресурсы успешно списаны."
        )
    }

    /**
     * Safely adds a bundle/map of resources.
     */
    fun addBundleSafe(
        bundle: Map<ResourceType, Int>,
        allowPartial: Boolean = true
    ): Pair<GameResources, ResourceOperationResult> {
        var currentRes = this
        var anyPartial = false
        val summaryMessages = mutableListOf<String>()

        for ((type, amount) in bundle) {
            if (amount <= 0) continue
            val (updated, result) = currentRes.addResourceSafe(type, amount, allowPartial)
            currentRes = updated
            if (result is ResourceOperationResult.PartialSuccess) {
                anyPartial = true
            }
            if (result is ResourceOperationResult.Failure && !allowPartial) {
                return this to result // Abort entire bundle
            }
            summaryMessages.add("+${if (result is ResourceOperationResult.PartialSuccess) result.actualAmountAdded else amount} ${type.nameRu}")
        }

        val finalMsg = if (anyPartial) {
            "Добыча доставлена на склад (часть не поместилась из-за лимита места)."
        } else {
            "Получено: " + summaryMessages.joinToString(", ")
        }

        return currentRes to ResourceOperationResult.Success(message = finalMsg)
    }

    /**
     * Backward-compatible subtract method.
     */
    fun subtract(cost: GameResources): GameResources {
        var updated = copy(
            money = (money - cost.money).coerceAtLeast(0),
            food = (food - cost.food).coerceAtLeast(0),
            water = (water - cost.water).coerceAtLeast(0),
            fuel = (fuel - cost.fuel).coerceAtLeast(0),
            materials = (materials - cost.materials).coerceAtLeast(0)
        )
        cost.extraResources.forEach { (type, amount) ->
            val cur = updated.get(type)
            updated = updated.withResource(type, (cur - amount).coerceAtLeast(0))
        }
        return updated
    }

    /**
     * Backward-compatible add method with warehouse capacity protection.
     */
    fun add(other: GameResources): GameResources {
        val bundle = mutableMapOf<ResourceType, Int>()
        if (other.money > 0) bundle[ResourceType.MONEY] = other.money
        if (other.food > 0) bundle[ResourceType.FOOD] = other.food
        if (other.water > 0) bundle[ResourceType.WATER] = other.water
        if (other.fuel > 0) bundle[ResourceType.FUEL] = other.fuel
        if (other.materials > 0) bundle[ResourceType.MATERIALS] = other.materials
        other.extraResources.forEach { (t, a) ->
            if (a > 0) bundle[t] = a
        }

        val (result, _) = addBundleSafe(bundle, allowPartial = true)
        return result
    }

    /**
     * Evaluates state urgency level for a given resource.
     */
    fun getDepletionLevel(type: ResourceType, dailyConsumption: Int = 0): ResourceStateLevel {
        val current = get(type)
        val descriptor = ResourceRegistry.getDescriptor(type)
        val effectiveCritical = if (dailyConsumption > 0) dailyConsumption else descriptor.criticalThreshold
        val effectiveLow = if (dailyConsumption > 0) dailyConsumption * 2 else descriptor.lowThreshold

        return when {
            current <= effectiveCritical -> ResourceStateLevel.CRITICAL
            current <= effectiveLow -> ResourceStateLevel.LOW
            else -> ResourceStateLevel.NORMAL
        }
    }
}
