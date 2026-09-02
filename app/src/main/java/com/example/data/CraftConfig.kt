package com.example.data

import com.example.domain.model.*
import kotlin.math.min

/**
 * Centralized configuration, blueprints catalogue, and atomic verification engine
 * for the Settlement Workshop and Crafting System (Point 7).
 */
object CraftConfig {

    /**
     * Complete catalogue of crafting blueprints available in the Frontier Settlement Workshop.
     */
    fun createDefaultRecipes(): List<CraftRecipe> {
        return listOf(
            // 1. Полевая аптечка
            CraftRecipe(
                id = "recipe_medkit",
                nameRu = "Полевая аптечка",
                category = CraftRecipeCategory.MEDICINE,
                descriptionRu = "Герметичный индивидуальный перевязочный пакет с антисептиком, коагулянтом и ампулой анальгетика.",
                outputItem = WarehouseItem(
                    id = "item_medkit",
                    name = "Полевая аптечка",
                    category = ItemCategory.MEDICINE_AND_AID,
                    quantity = 1,
                    unitSize = 1,
                    rarity = ItemRarity.COMMON,
                    description = "Индивидуальный санитарный комплект первой помощи. Незаменим для перевязки ранений в экспедициях по пустошам.",
                    baseValueCredits = 28,
                    iconKey = "medkit",
                    sourcesRu = listOf("Крафт в Мастерской", "Аптечные пункты в посёлках"),
                    usesRu = listOf("Восстановление 40 HP бойцу в экспедиции", "Пополнение запасов лазарета"),
                    properties = mapOf("Лечение" to "+40 HP", "Вес" to "1 ед.")
                ),
                outputQuantity = 1,
                requiredResources = mapOf(
                    ResourceType.MATERIALS to 20,
                    ResourceType.MEDICINE to 3,
                    ResourceType.WATER to 10
                ),
                minWorkshopLevel = 1,
                requiredSettlementLevel = 1,
                craftingTimeSeconds = 30,
                iconKey = "medkit",
                loreRu = "«Пара стерильных бинтов и обезболивающее спасли в пустошах больше жизней, чем тяжёлая броня». — Доктор Елена Власова"
            ),

            // 2. Набор полевого инженера
            CraftRecipe(
                id = "recipe_toolkit",
                nameRu = "Набор полевого инженера",
                category = CraftRecipeCategory.TOOLS,
                descriptionRu = "Универсальный комплект прецизионных инструментов, паяльник, мультиметр и монтировка из легированной стали.",
                outputItem = WarehouseItem(
                    id = "item_toolkit",
                    name = "Набор полевого инженера",
                    category = ItemCategory.EQUIPMENT_AND_TOOLS,
                    quantity = 1,
                    unitSize = 2,
                    rarity = ItemRarity.UNCOMMON,
                    description = "Прочный кейс с инструментами. Позволяет вскрывать заклинившие шлюзы, чинить шасси багги и калибровать энергогенераторы.",
                    baseValueCredits = 55,
                    iconKey = "toolkit",
                    sourcesRu = listOf("Крафт в Мастерской (Ур. 1)", "Склады логистических парков"),
                    usesRu = listOf("Вскрытие контейнеров в экспедициях", "Полевой ремонт техники (+30% прочности)"),
                    properties = mapOf("Навык инженерии" to "+5", "Вес" to "2 ед.")
                ),
                outputQuantity = 1,
                requiredResources = mapOf(
                    ResourceType.MATERIALS to 45,
                    ResourceType.COMPONENTS to 2
                ),
                minWorkshopLevel = 1,
                requiredSettlementLevel = 1,
                requiredTechId = "tech_advanced_tools",
                craftingTimeSeconds = 45,
                iconKey = "toolkit",
                loreRu = "«Хороший разводной ключ открывает больше дверей, чем связка ключей от сейфа». — Дмитрий Романов"
            ),

            // 3. Сухой паёк выживальщика (Выход: 2 шт.)
            CraftRecipe(
                id = "recipe_ration_pack",
                nameRu = "Сухой паёк выживальщика",
                category = CraftRecipeCategory.SURVIVAL,
                descriptionRu = "Прессованные высокобелковые брикеты из гидропонных культур с витаминными добавками в вакуумной фольге.",
                outputItem = WarehouseItem(
                    id = "item_ration_pack",
                    name = "Сухой паёк выживальщика",
                    category = ItemCategory.PRIMARY_SUPPLIES,
                    quantity = 2,
                    unitSize = 1,
                    rarity = ItemRarity.COMMON,
                    description = "Сбалансированный аварийный пищевой рацион. Сохраняет питательные свойства годами даже при экстремальных температурах.",
                    baseValueCredits = 18,
                    iconKey = "ration",
                    sourcesRu = listOf("Пищеблок Мастерской", "Торговцы каравана"),
                    usesRu = listOf("Автономное снабжение отрядов в дальних переходах", "Резервный фонд провианта"),
                    properties = mapOf("Калорийность" to "3200 ккал", "Срок годности" to "Неограничен")
                ),
                outputQuantity = 2,
                requiredResources = mapOf(
                    ResourceType.FOOD to 30,
                    ResourceType.WATER to 15
                ),
                minWorkshopLevel = 1,
                requiredSettlementLevel = 1,
                craftingTimeSeconds = 20,
                iconKey = "ration",
                loreRu = "Вкус напоминает картон с солью, зато один такой брикет держит разведчика на ногах весь переход через солончак."
            ),

            // 4. Канистра стабилизированного топлива
            CraftRecipe(
                id = "recipe_fuel_canister",
                nameRu = "Канистра очищенного горючего",
                category = CraftRecipeCategory.SURVIVAL,
                descriptionRu = "Очищенное дистиллированное топливо высокой степени фильтрации в противоударной стальной канистре.",
                outputItem = WarehouseItem(
                    id = "item_fuel_canister",
                    name = "Канистра очищенного горючего",
                    category = ItemCategory.PRIMARY_SUPPLIES,
                    quantity = 1,
                    unitSize = 2,
                    rarity = ItemRarity.COMMON,
                    description = "Герметичная 20-литровая ёмкость с высокооктановым топливом. Используется для экстренной дозаправки техники посреди пустошей.",
                    baseValueCredits = 40,
                    iconKey = "fuel_canister",
                    sourcesRu = listOf("Хим. установка Мастерской", "Заброшенные заправочные станции"),
                    usesRu = listOf("Дозаправка багги и грузовиков (+25 л горючего)", "Питание полевых генераторов"),
                    properties = mapOf("Объём" to "20 литров", "Октановое число" to "98")
                ),
                outputQuantity = 1,
                requiredResources = mapOf(
                    ResourceType.FUEL to 30,
                    ResourceType.MATERIALS to 15
                ),
                minWorkshopLevel = 1,
                requiredSettlementLevel = 1,
                craftingTimeSeconds = 30,
                iconKey = "fuel_canister",
                loreRu = "Очищенный бензин исключает нагар в поршневой группе багги в пылевую бурю."
            ),

            // 5. Композитная бронепластина (Ур. 2)
            CraftRecipe(
                id = "recipe_armor_plate",
                nameRu = "Композитная бронепластина",
                category = CraftRecipeCategory.EQUIPMENT,
                descriptionRu = "Многослойный пакет из баллистического кевлара, керамики и титановой подложки для усиления бронежилетов.",
                outputItem = WarehouseItem(
                    id = "item_armor_plate",
                    name = "Композитная бронепластина",
                    category = ItemCategory.EQUIPMENT_AND_TOOLS,
                    quantity = 1,
                    unitSize = 2,
                    rarity = ItemRarity.RARE,
                    description = "Тяжёлая броневставка 4-го класса защиты. Останавливает бронебойные винтовочные пули и осколки гранат.",
                    baseValueCredits = 95,
                    iconKey = "armor_plate",
                    sourcesRu = listOf("Кузня Мастерской (Ур. 2)", "Военные бункеры довоенного периода"),
                    usesRu = listOf("Экипировка штурмовиков отряда (+8 к Защите)", "Усиление бортовой брони транспорта"),
                    properties = mapOf("Класс защиты" to "IV ГОСТ", "Бонус брони" to "+8 Защиты")
                ),
                outputQuantity = 1,
                requiredResources = mapOf(
                    ResourceType.MATERIALS to 70,
                    ResourceType.COMPONENTS to 4,
                    ResourceType.RARE_ALLOY to 1
                ),
                minWorkshopLevel = 2,
                requiredSettlementLevel = 2,
                requiredTechId = "tech_armor_craft",
                craftingTimeSeconds = 60,
                iconKey = "armor_plate",
                loreRu = "«Когда мародёры бьют из пулемёта, каждый миллиметр титанового сплава становится благословением». — Виктор Громов"
            ),

            // 6. Цинковый ящик патронов 7.62 (Ур. 2)
            CraftRecipe(
                id = "recipe_ammo_crate",
                nameRu = "Цинковый ящик патронов 7.62",
                category = CraftRecipeCategory.EQUIPMENT,
                descriptionRu = "Опечатанный оцинкованный ящик с 200 патронами повышенной пробиваемости со стальным сердечником.",
                outputItem = WarehouseItem(
                    id = "item_ammo_crate",
                    name = "Цинковый ящик патронов 7.62",
                    category = ItemCategory.AMMO_AND_MILITARY,
                    quantity = 1,
                    unitSize = 2,
                    rarity = ItemRarity.UNCOMMON,
                    description = "Армейский боезапас стандартного экспедиционного калибра. Обеспечивает отряд боекомплектом для затяжных перестрелок.",
                    baseValueCredits = 70,
                    iconKey = "ammo_crate",
                    sourcesRu = listOf("Снаряжательный станок Мастерской (Ур. 2)", "Арсеналы рейдеров"),
                    usesRu = listOf("Пополнение боезапаса отряда перед штурмом", "Тактический спецвыстрел в бою (+100% крит)"),
                    properties = mapOf("Калибр" to "7.62x39 мм", "Количество патронов" to "200 шт.")
                ),
                outputQuantity = 1,
                requiredResources = mapOf(
                    ResourceType.MATERIALS to 50,
                    ResourceType.AMMO to 30
                ),
                minWorkshopLevel = 2,
                requiredSettlementLevel = 2,
                craftingTimeSeconds = 40,
                iconKey = "ammo_crate",
                loreRu = "Свежевыплавленные латунные гильзы и очищенный бездымный порох — залог безотказной стрельбы автоматов."
            ),

            // 7. Электронный модуль управления (Ур. 2, Выход: 2 шт.)
            CraftRecipe(
                id = "recipe_control_module",
                nameRu = "Электронный модуль управления",
                category = CraftRecipeCategory.COMPONENTS,
                descriptionRu = "Многослойная печатная плата с программируемым микропроцессором и датчиками телеметрии.",
                outputItem = WarehouseItem(
                    id = "item_control_module",
                    name = "Электронный модуль управления",
                    category = ItemCategory.ELECTRONICS_AND_PARTS,
                    quantity = 2,
                    unitSize = 1,
                    rarity = ItemRarity.RARE,
                    description = "Высокотехнологичный электронный блок. Служит ядром для сборки продвинутых сканеров, турелей и систем навигации.",
                    baseValueCredits = 120,
                    iconKey = "microchip",
                    sourcesRu = listOf("Паяльная станция Мастерской (Ур. 2)"),
                    usesRu = listOf("Сборка сложных приборов и сканеров", "Модернизация турелей периметра базы"),
                    properties = mapOf("Архитектура" to "64-бит RISC", "Шина" to "CAN-Bus")
                ),
                outputQuantity = 2,
                requiredResources = mapOf(
                    ResourceType.COMPONENTS to 4,
                    ResourceType.MATERIALS to 35,
                    ResourceType.MONEY to 80
                ),
                minWorkshopLevel = 2,
                requiredSettlementLevel = 2,
                requiredTechId = "tech_microelectronics",
                craftingTimeSeconds = 50,
                iconKey = "microchip",
                loreRu = "Восстановленные довоенные чипы, очищенные ультразвуком от солей и окислов."
            ),

            // 8. Портативный биосканер пустоши (Ур. 3)
            CraftRecipe(
                id = "recipe_bio_scanner",
                nameRu = "Портативный биосканер пустоши",
                category = CraftRecipeCategory.TOOLS,
                descriptionRu = "Резонансный дозиметрический и биологический сканер с LCD-дисплеем и спектральным детектором аномалий.",
                outputItem = WarehouseItem(
                    id = "item_bio_scanner",
                    name = "Портативный биосканер пустоши",
                    category = ItemCategory.VALUABLES_AND_RELICS,
                    quantity = 1,
                    unitSize = 1,
                    rarity = ItemRarity.EPIC,
                    description = "Вершина инженерной мысли аванпоста. Обнаруживает скрытые схроны, предупреждает о засадах мутантов и замеряет уровень радиации.",
                    baseValueCredits = 260,
                    iconKey = "scanner",
                    sourcesRu = listOf("Лабораторный комплекс Мастерской (Ур. 3)"),
                    usesRu = listOf("Увеличение добычи в вылазках на +40%", "Исключение внезапных нападений в радиоактивных зонах"),
                    properties = mapOf("Радиус действия" to "500 м", "Спектр" to "Гамма / Био / ЭМ")
                ),
                outputQuantity = 1,
                requiredResources = mapOf(
                    ResourceType.COMPONENTS to 8,
                    ResourceType.RARE_ALLOY to 3,
                    ResourceType.MONEY to 220
                ),
                minWorkshopLevel = 3,
                requiredSettlementLevel = 3,
                requiredTechId = "tech_bio_scan",
                craftingTimeSeconds = 90,
                iconKey = "scanner",
                loreRu = "«Этот прибор слышит пульс мутанта за три сотни метров сквозь бетонные перекрытия бункера». — Алексей Соколов"
            )
        )
    }

    /**
     * Calculates the maximum craft multiplier (batches) given current resources and warehouse storage.
     */
    fun calculateMaxCraftCount(
        recipe: CraftRecipe,
        resources: GameResources,
        currentInventory: List<WarehouseItem>
    ): Int {
        // 1. Calculate constraint based on resource costs
        var resourceLimit = Int.MAX_VALUE
        recipe.requiredResources.forEach { (type, costPerBatch) ->
            if (costPerBatch > 0) {
                val playerStock = resources[type]
                val batchesFromThisResource = playerStock / costPerBatch
                resourceLimit = min(resourceLimit, batchesFromThisResource)
            }
        }

        if (resourceLimit <= 0) return 0

        // 2. Calculate warehouse storage constraint
        // Net change in warehouse space per batch:
        // Free space needed = (total output items volume) - (physical resources volume consumed by recipe)
        val outputVolumePerBatch = recipe.totalOutputVolume
        val consumedPhysicalVolumePerBatch = recipe.requiredResources.entries.sumOf { (type, amount) ->
            if (type.isPhysical) amount * type.unitSize else 0
        }
        val netVolumePerBatch = outputVolumePerBatch - consumedPhysicalVolumePerBatch

        val currentStoredVolume = resources.totalStoredVolume + currentInventory.sumOf { it.quantity * it.unitSize }
        val availableSpace = (resources.warehouseMaxCapacity - currentStoredVolume).coerceAtLeast(0)

        val storageLimit = if (netVolumePerBatch > 0) {
            availableSpace / netVolumePerBatch
        } else {
            // Crafting actually frees up or keeps equal storage! (e.g. condensing bulky resources)
            Int.MAX_VALUE
        }

        return min(resourceLimit, storageLimit).coerceAtLeast(0)
    }

    /**
     * Validates if a craft operation is permitted and returns detailed error information if not.
     */
    fun validateCraft(
        recipe: CraftRecipe,
        workshopBuilding: Building?,
        settlementLevel: Int,
        resources: GameResources,
        currentInventory: List<WarehouseItem>,
        craftCount: Int
    ): CraftFailureReason? {
        if (craftCount <= 0) return CraftFailureReason.INVALID_QUANTITY

        // 1. Workshop building existence check
        if (workshopBuilding == null || !workshopBuilding.isConstructed) {
            return CraftFailureReason.WORKSHOP_NOT_BUILT
        }

        // 2. Workshop level check
        if (workshopBuilding.level < recipe.minWorkshopLevel) {
            return CraftFailureReason.INSUFFICIENT_WORKSHOP_LEVEL
        }

        // 3. Settlement level check
        if (settlementLevel < recipe.requiredSettlementLevel) {
            return CraftFailureReason.INSUFFICIENT_SETTLEMENT_LEVEL
        }

        // 4. Resource check
        val missing = recipe.requiredResources.filter { (type, costPerBatch) ->
            resources[type] < (costPerBatch * craftCount)
        }
        if (missing.isNotEmpty()) {
            return CraftFailureReason.INSUFFICIENT_RESOURCES
        }

        // 5. Storage check: verify that after consuming ingredients and adding new products, total stored volume does not exceed max capacity
        val outputVolume = recipe.totalOutputVolume * craftCount
        val consumedPhysicalVolume = recipe.requiredResources.entries.sumOf { (type, amount) ->
            if (type.isPhysical) amount * type.unitSize * craftCount else 0
        }

        val currentStoredVolume = resources.totalStoredVolume + currentInventory.sumOf { it.quantity * it.unitSize }
        val finalStoredVolume = currentStoredVolume - consumedPhysicalVolume + outputVolume

        if (finalStoredVolume > resources.warehouseMaxCapacity) {
            return CraftFailureReason.INSUFFICIENT_STORAGE
        }

        return null // All checks passed!
    }
}
