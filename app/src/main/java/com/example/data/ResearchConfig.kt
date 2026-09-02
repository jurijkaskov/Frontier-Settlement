package com.example.data

import com.example.domain.model.*

/**
 * Centralized configuration, tech catalog, and rule engine for Research & Technology (Point 9).
 */
object ResearchConfig {

    /**
     * Complete catalogue of research technologies with categories, progressive tiers,
     * dependencies, costs, and real game effects.
     */
    fun createDefaultTechnologies(): List<ResearchTech> {
        return listOf(
            // =========================================================================
            // 1. КАТЕГОРИЯ: ПОСЕЛЕНИЕ (SETTLEMENT)
            // =========================================================================

            // Поселение Ур. 1: Оптимизация стеллажей склада (Начальная активная/доступная технология)
            ResearchTech(
                id = "tech_storage_1",
                title = "Оптимизация стеллажей склада",
                category = TechCategory.SETTLEMENT,
                tier = 1,
                description = "Новые схемы эргономичной укладки контейнеров и многоярусные подвесные полки увеличивают полезный объем хранилища.",
                loreRu = "«Правильно уложенный штабель ящиков экономит треть площади ангара». — Завскладом Базы",
                requirements = ResearchRequirement(
                    resourceCosts = mapOf(
                        ResourceType.MONEY to 200,
                        ResourceType.MATERIALS to 120
                    ),
                    minLabLevel = 1,
                    minSettlementLevel = 1,
                    prerequisiteTechIds = emptyList()
                ),
                effects = listOf(
                    TechEffect.StorageCapacityBoost(additionalCapacity = 200)
                ),
                iconKey = "storage",
                isResearched = true // Initial unlocked tech so player starts with real benefits
            ),

            // Поселение Ур. 2: Модульные грузовые ангары (Зависит от tech_storage_1)
            ResearchTech(
                id = "tech_storage_2",
                title = "Модульные грузовые ангары",
                category = TechCategory.SETTLEMENT,
                tier = 2,
                description = "Сборные герметичные боксы с гидравлическими подъемниками для крупногабаритных материалов и сырья.",
                loreRu = "Стандартизированные шлюзовые блоки защищают запасы от радиационной пыли.",
                requirements = ResearchRequirement(
                    resourceCosts = mapOf(
                        ResourceType.MONEY to 350,
                        ResourceType.MATERIALS to 220,
                        ResourceType.COMPONENTS to 2
                    ),
                    minLabLevel = 2,
                    minSettlementLevel = 2,
                    prerequisiteTechIds = listOf("tech_storage_1")
                ),
                effects = listOf(
                    TechEffect.StorageCapacityBoost(additionalCapacity = 400)
                ),
                iconKey = "storage_advanced",
                isResearched = false
            ),

            // Поселение Ур. 1: Мембранная очистка воды
            ResearchTech(
                id = "tech_water_purify",
                title = "Мембранная наноочистка воды",
                category = TechCategory.SETTLEMENT,
                tier = 1,
                description = "Тонкая нанофильтрация позволяет добывать на 25% больше чистой артезианской воды без повышенного износа насосов.",
                loreRu = "Ионообменные смолы связывают тяжелые соли и очищают грунтовую воду до идеального состояния.",
                requirements = ResearchRequirement(
                    resourceCosts = mapOf(
                        ResourceType.MONEY to 220,
                        ResourceType.MATERIALS to 120
                    ),
                    minLabLevel = 1,
                    minSettlementLevel = 1,
                    prerequisiteTechIds = emptyList()
                ),
                effects = listOf(
                    TechEffect.ResourceProductionMultiplier(
                        resourceType = ResourceType.WATER,
                        multiplierPercent = 25
                    )
                ),
                iconKey = "water",
                isResearched = false
            ),

            // Поселение Ур. 2: Энергосберегающая сеть
            ResearchTech(
                id = "tech_energy_grid",
                title = "Энергосберегающая сеть",
                category = TechCategory.SETTLEMENT,
                tier = 2,
                description = "Умные трансформаторы и буферные конденсаторы снижают пиковые нагрузки и экономят 20% дизельного топлива базы.",
                loreRu = "Оптимизация кабельных трасс исключает паразитные утечки тока в сырых коллекторах.",
                requirements = ResearchRequirement(
                    resourceCosts = mapOf(
                        ResourceType.MONEY to 300,
                        ResourceType.MATERIALS to 180,
                        ResourceType.COMPONENTS to 2
                    ),
                    minLabLevel = 2,
                    minSettlementLevel = 2,
                    prerequisiteTechIds = emptyList()
                ),
                effects = listOf(
                    TechEffect.FuelEfficiency(reductionPercent = 20)
                ),
                iconKey = "energy",
                isResearched = false
            ),

            // =========================================================================
            // 2. КАТЕГОРИЯ: ПРОИЗВОДСТВО (PRODUCTION)
            // =========================================================================

            // Производство Ур. 1: Гидропонные фитолампы
            ResearchTech(
                id = "tech_hydroponics_light",
                title = "Гидропонные фитолампы",
                category = TechCategory.PRODUCTION,
                tier = 1,
                description = "Спектральные LED-панели ускоряют фотосинтез культур в теплицах, увеличивая суточный сбор Еды на 20%.",
                loreRu = "Двухдиапазонный сине-красный свет стимулирует рост зерновых культур даже при скудном поливе.",
                requirements = ResearchRequirement(
                    resourceCosts = mapOf(
                        ResourceType.MONEY to 200,
                        ResourceType.MATERIALS to 100
                    ),
                    minLabLevel = 1,
                    minSettlementLevel = 1,
                    prerequisiteTechIds = emptyList()
                ),
                effects = listOf(
                    TechEffect.ResourceProductionMultiplier(
                        resourceType = ResourceType.FOOD,
                        multiplierPercent = 20
                    )
                ),
                iconKey = "food",
                isResearched = false
            ),

            // Производство Ур. 1: Продвинутые инструменты (Открывает набор инженера)
            ResearchTech(
                id = "tech_advanced_tools",
                title = "Продвинутые слесарные инструменты",
                category = TechCategory.PRODUCTION,
                tier = 1,
                description = "Чертежи закалки легированной стали и сборки точных измерительных приборов для Мастерской поселения.",
                loreRu = "«Без нормального инструмента ремонт багги превращается в ритуальные пляски с молотком».",
                requirements = ResearchRequirement(
                    resourceCosts = mapOf(
                        ResourceType.MONEY to 240,
                        ResourceType.MATERIALS to 140
                    ),
                    minLabLevel = 1,
                    minSettlementLevel = 1,
                    prerequisiteTechIds = emptyList()
                ),
                effects = listOf(
                    TechEffect.RecipeUnlock(
                        recipeId = "recipe_toolkit",
                        recipeNameRu = "Набор полевого инженера"
                    )
                ),
                iconKey = "toolkit",
                isResearched = false
            ),

            // Производство Ур. 2: Микроэлектроника и чипы (Зависит от tech_advanced_tools)
            ResearchTech(
                id = "tech_microelectronics",
                title = "Микроэлектроника и чипы",
                category = TechCategory.PRODUCTION,
                tier = 2,
                description = "Паяльные станции с ультразвуковой очисткой позволяют восстанавливать довоенные печатные платы и логические блоки.",
                loreRu = "Восстановление кремниевых микросхем открывает путь к управляемой автоматике.",
                requirements = ResearchRequirement(
                    resourceCosts = mapOf(
                        ResourceType.MONEY to 380,
                        ResourceType.MATERIALS to 200,
                        ResourceType.COMPONENTS to 3
                    ),
                    minLabLevel = 2,
                    minSettlementLevel = 2,
                    prerequisiteTechIds = listOf("tech_advanced_tools")
                ),
                effects = listOf(
                    TechEffect.RecipeUnlock(
                        recipeId = "recipe_control_module",
                        recipeNameRu = "Электронный модуль управления"
                    )
                ),
                iconKey = "chip",
                isResearched = false
            ),

            // Производство Ур. 2: Вторичная переплавка утиля
            ResearchTech(
                id = "tech_recycling",
                title = "Вторичная переплавка утиля",
                category = TechCategory.PRODUCTION,
                tier = 2,
                description = "Индукционные тигли высокой температуры извлекают пригодный металл из сильно окисленного металлолома (+30% Материалов).",
                loreRu = "Электродуговая плавка не оставляет шлака и дает прочный конструкционный прокат.",
                requirements = ResearchRequirement(
                    resourceCosts = mapOf(
                        ResourceType.MONEY to 300,
                        ResourceType.MATERIALS to 150
                    ),
                    minLabLevel = 2,
                    minSettlementLevel = 2,
                    prerequisiteTechIds = emptyList()
                ),
                effects = listOf(
                    TechEffect.ResourceProductionMultiplier(
                        resourceType = ResourceType.MATERIALS,
                        multiplierPercent = 30
                    )
                ),
                iconKey = "workshop",
                isResearched = false
            ),

            // =========================================================================
            // 3. КАТЕГОРИЯ: ЭКОНОМИКА (ECONOMY)
            // =========================================================================

            // Экономика Ур. 1: Бартерный кодекс пустоши
            ResearchTech(
                id = "tech_barter_code",
                title = "Бартерный кодекс пустоши",
                category = TechCategory.ECONOMY,
                tier = 1,
                description = "Унифицированные весовые гири и стандарты оценки чистоты сплавов вызывают доверие у караванщиков (-5% к ценам покупки).",
                loreRu = "Честный торговый обмен привлекает караваны даже из дальних оазисов.",
                requirements = ResearchRequirement(
                    resourceCosts = mapOf(
                        ResourceType.MONEY to 180,
                        ResourceType.MATERIALS to 90
                    ),
                    minLabLevel = 1,
                    minSettlementLevel = 1,
                    prerequisiteTechIds = emptyList()
                ),
                effects = listOf(
                    TechEffect.TradeBonus(discountPercent = 5, unlocksRareGoods = false)
                ),
                iconKey = "trade",
                isResearched = false
            ),

            // Экономика Ур. 2: Торговые караваны и связи (Зависит от tech_barter_code)
            ResearchTech(
                id = "tech_trade_routes",
                title = "Торговые караваны и связи",
                category = TechCategory.ECONOMY,
                tier = 2,
                description = "Заключение соглашений с гильдией купцов обеспечивает скидку 10% и поставку редких сплавов и деталей.",
                loreRu = "Флаг Аванпоста на караванной тропе гарантирует защиту от дорожных поборов.",
                requirements = ResearchRequirement(
                    resourceCosts = mapOf(
                        ResourceType.MONEY to 320,
                        ResourceType.MATERIALS to 160
                    ),
                    minLabLevel = 2,
                    minSettlementLevel = 2,
                    prerequisiteTechIds = listOf("tech_barter_code")
                ),
                effects = listOf(
                    TechEffect.TradeBonus(discountPercent = 10, unlocksRareGoods = true)
                ),
                iconKey = "caravan",
                isResearched = false
            ),

            // =========================================================================
            // 4. КАТЕГОРИЯ: ВЫЖИВАНИЕ (SURVIVAL)
            // =========================================================================

            // Выживание Ур. 1: Полевая хирургия и антисептика
            ResearchTech(
                id = "tech_field_medicine",
                title = "Полевая хирургия и антисептика",
                category = TechCategory.SURVIVAL,
                tier = 1,
                description = "Технологии синтеза коагулянтов и стерилизации перевязочных материалов ускоряют восстановление раненых на +10 HP/день.",
                loreRu = "«Главное — остановить кровотечение в первые три минуты после попадания». — Доктор Власова",
                requirements = ResearchRequirement(
                    resourceCosts = mapOf(
                        ResourceType.MONEY to 220,
                        ResourceType.MATERIALS to 110,
                        ResourceType.MEDICINE to 3
                    ),
                    minLabLevel = 1,
                    minSettlementLevel = 1,
                    prerequisiteTechIds = emptyList()
                ),
                effects = listOf(
                    TechEffect.MedicalEfficiency(regenBonusHp = 10),
                    TechEffect.RecipeUnlock(
                        recipeId = "recipe_medkit",
                        recipeNameRu = "Полевая аптечка"
                    )
                ),
                iconKey = "medicine",
                isResearched = false
            ),

            // Выживание Ур. 1: Усиленный багажный каркас
            ResearchTech(
                id = "tech_cargo_rack",
                title = "Усиленный багажный каркас",
                category = TechCategory.SURVIVAL,
                tier = 1,
                description = "Установка модульных багажных рам на транспорт повышает грузоподъемность машин в экспедициях на 35%.",
                loreRu = "Трубчатый каркас из хром-ванадиевой стали выдерживает любые прыжки по барханам.",
                requirements = ResearchRequirement(
                    resourceCosts = mapOf(
                        ResourceType.MONEY to 240,
                        ResourceType.MATERIALS to 140
                    ),
                    minLabLevel = 1,
                    minSettlementLevel = 1,
                    prerequisiteTechIds = emptyList()
                ),
                effects = listOf(
                    TechEffect.VehicleCargoMultiplier(multiplierPercent = 35)
                ),
                iconKey = "cargo",
                isResearched = false
            ),

            // Выживание Ур. 2: Кевларовое армирование (Открывает бронепластины и дает +4 к броне)
            ResearchTech(
                id = "tech_armor_craft",
                title = "Кевларовое армирование",
                category = TechCategory.SURVIVAL,
                tier = 2,
                description = "Укрепление полевой экипировки и брони бойцов композитными накладками (+4 к Защите) и чертеж бронепластин в Мастерской.",
                loreRu = "Плетеные арамидные волокна гасят кинетическую энергию осколков и пуль.",
                requirements = ResearchRequirement(
                    resourceCosts = mapOf(
                        ResourceType.MONEY to 320,
                        ResourceType.MATERIALS to 180,
                        ResourceType.COMPONENTS to 2
                    ),
                    minLabLevel = 2,
                    minSettlementLevel = 2,
                    prerequisiteTechIds = emptyList()
                ),
                effects = listOf(
                    TechEffect.SquadStatBonus(attackBonus = 0, defenseBonus = 4),
                    TechEffect.RecipeUnlock(
                        recipeId = "recipe_armor_plate",
                        recipeNameRu = "Композитная бронепластина"
                    )
                ),
                iconKey = "armor",
                isResearched = false
            ),

            // Выживание Ур. 3: Биосканеры и радиоразведка (Зависит от tech_armor_craft, открывает сканер и бункер)
            ResearchTech(
                id = "tech_bio_scan",
                title = "Биосканеры и радиоразведка",
                category = TechCategory.SURVIVAL,
                tier = 3,
                description = "Резонансные частотные анализаторы для обнаружения аномалий, скрытых схронов и засекреченного Бункера «Объект 42».",
                loreRu = "Спектральный анализ почвы безошибочно выявляет герметичные подземные шахты.",
                requirements = ResearchRequirement(
                    resourceCosts = mapOf(
                        ResourceType.MONEY to 450,
                        ResourceType.MATERIALS to 260,
                        ResourceType.COMPONENTS to 4,
                        ResourceType.RARE_ALLOY to 2
                    ),
                    minLabLevel = 3,
                    minSettlementLevel = 3,
                    prerequisiteTechIds = listOf("tech_armor_craft")
                ),
                effects = listOf(
                    TechEffect.RecipeUnlock(
                        recipeId = "recipe_bio_scanner",
                        recipeNameRu = "Портативный биосканер пустоши"
                    ),
                    TechEffect.LocationUnlock(
                        locationId = "loc_6",
                        locationNameRu = "Бункер «Объект 42»"
                    )
                ),
                iconKey = "scanner",
                isResearched = false
            )
        )
    }

    /**
     * Validates an individual technology against current game state and building progression.
     */
    fun validateTech(
        tech: ResearchTech,
        allTechs: List<ResearchTech>,
        labBuilding: Building?,
        settlementLevel: Int,
        resources: GameResources
    ): TechValidationInfo {
        val reqs = tech.requirements
        val isLabBuilt = labBuilding?.isConstructed == true
        val labLevel = if (isLabBuilt) (labBuilding?.level ?: 0) else 0

        // 1. Check prerequisite technologies
        val unsatisfiedPrereqs = reqs.prerequisiteTechIds.mapNotNull { prereqId ->
            val prereqTech = allTechs.find { it.id == prereqId }
            if (prereqTech == null || !prereqTech.isResearched) prereqTech else null
        }

        // 2. Check missing resources
        val missingRes = mutableMapOf<ResourceType, Int>()
        reqs.resourceCosts.forEach { (type, cost) ->
            val available = resources[type]
            if (available < cost) {
                missingRes[type] = cost - available
            }
        }

        // 3. Construct detailed checklist
        val checklist = mutableListOf<TechRequirementStatus>()

        // Lab requirement
        checklist.add(
            TechRequirementStatus(
                isSatisfied = isLabBuilt && labLevel >= reqs.minLabLevel,
                labelRu = if (!isLabBuilt) {
                    "Построить Исследовательский центр"
                } else {
                    "Исследовательский центр: Ур. ${reqs.minLabLevel}"
                },
                currentProgressRu = if (isLabBuilt) "Текущий: Ур. $labLevel" else "Не построен"
            )
        )

        // Settlement level
        if (reqs.minSettlementLevel > 1) {
            checklist.add(
                TechRequirementStatus(
                    isSatisfied = settlementLevel >= reqs.minSettlementLevel,
                    labelRu = "Уровень поселения: ${reqs.minSettlementLevel}",
                    currentProgressRu = "Текущий: Ур. $settlementLevel"
                )
            )
        }

        // Prerequisite techs
        reqs.prerequisiteTechIds.forEach { prereqId ->
            val prereq = allTechs.find { it.id == prereqId }
            val isDone = prereq?.isResearched == true
            checklist.add(
                TechRequirementStatus(
                    isSatisfied = isDone,
                    labelRu = "Технология «${prereq?.title ?: prereqId}»",
                    currentProgressRu = if (isDone) "Изучено" else "Не изучено"
                )
            )
        }

        // Resource costs
        reqs.resourceCosts.forEach { (type, cost) ->
            val currentAmount = resources[type]
            checklist.add(
                TechRequirementStatus(
                    isSatisfied = currentAmount >= cost,
                    labelRu = "${type.titleRu}: $cost",
                    currentProgressRu = "$currentAmount / $cost"
                )
            )
        }

        // Determine final tech status
        val status = when {
            tech.isResearched -> TechStatus.RESEARCHED
            !isLabBuilt -> TechStatus.LOCKED_LAB_UNBUILT
            labLevel < reqs.minLabLevel -> TechStatus.LOCKED_LAB_LEVEL
            settlementLevel < reqs.minSettlementLevel -> TechStatus.LOCKED_SETTLEMENT_LEVEL
            unsatisfiedPrereqs.isNotEmpty() -> TechStatus.LOCKED_DEPENDENCY
            missingRes.isNotEmpty() -> TechStatus.INSUFFICIENT_RESOURCES
            else -> TechStatus.AVAILABLE
        }

        return TechValidationInfo(
            tech = tech,
            status = status,
            isLabBuilt = isLabBuilt,
            labLevel = labLevel,
            settlementLevel = settlementLevel,
            missingResources = missingRes,
            unsatisfiedPrerequisites = unsatisfiedPrereqs,
            allRequirements = checklist
        )
    }

    /**
     * Extracts all active gameplay effects from currently researched technologies.
     */
    fun getActiveEffects(technologies: List<ResearchTech>): List<TechEffect> {
        return technologies.filter { it.isResearched }.flatMap { it.effects }
    }

    /**
     * Calculates total storage capacity boost from researched technologies.
     */
    fun getStorageBonus(technologies: List<ResearchTech>): Int {
        return getActiveEffects(technologies)
            .filterIsInstance<TechEffect.StorageCapacityBoost>()
            .sumOf { it.additionalCapacity }
    }

    /**
     * Calculates trading discount percentage from researched technologies.
     */
    fun getTradeDiscount(technologies: List<ResearchTech>): Int {
        return getActiveEffects(technologies)
            .filterIsInstance<TechEffect.TradeBonus>()
            .maxOfOrNull { it.discountPercent } ?: 0
    }

    /**
     * Calculates total squad attack and defense boosts from researched technologies.
     */
    fun getSquadCombatBonus(technologies: List<ResearchTech>): Pair<Int, Int> {
        val effects = getActiveEffects(technologies).filterIsInstance<TechEffect.SquadStatBonus>()
        val totalAtk = effects.sumOf { it.attackBonus }
        val totalDef = effects.sumOf { it.defenseBonus }
        return totalAtk to totalDef
    }

    /**
     * Calculates production multiplier for a given resource type from technologies (1.0 = base).
     */
    fun getProductionMultiplier(technologies: List<ResearchTech>, resourceType: ResourceType): Float {
        val bonuses = getActiveEffects(technologies)
            .filterIsInstance<TechEffect.ResourceProductionMultiplier>()
            .filter { it.resourceType == resourceType }
            .sumOf { it.multiplierPercent }
        return 1.0f + (bonuses / 100f)
    }

    /**
     * Calculates generator fuel consumption reduction percentage from technologies.
     */
    fun getFuelEfficiencyPercent(technologies: List<ResearchTech>): Int {
        return getActiveEffects(technologies)
            .filterIsInstance<TechEffect.FuelEfficiency>()
            .sumOf { it.reductionPercent }
    }

    /**
     * Calculates medical clinic HP regeneration bonus from technologies.
     */
    fun getMedicalRegenBonus(technologies: List<ResearchTech>): Int {
        return getActiveEffects(technologies)
            .filterIsInstance<TechEffect.MedicalEfficiency>()
            .sumOf { it.regenBonusHp }
    }

    /**
     * Calculates vehicle cargo multiplier for expeditions (1.0 = base).
     */
    fun getVehicleCargoMultiplier(technologies: List<ResearchTech>): Float {
        val bonusPercent = getActiveEffects(technologies)
            .filterIsInstance<TechEffect.VehicleCargoMultiplier>()
            .sumOf { it.multiplierPercent }
        return 1.0f + (bonusPercent / 100f)
    }

    /**
     * Set of all researched technology IDs.
     */
    fun getResearchedTechIds(technologies: List<ResearchTech>): Set<String> {
        return technologies.filter { it.isResearched }.map { it.id }.toSet()
    }
}
