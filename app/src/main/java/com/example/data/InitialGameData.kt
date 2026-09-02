package com.example.data

import com.example.domain.model.*
import com.example.domain.model.quest.*
import com.example.domain.service.quest.QuestCatalog

object InitialGameData {

    fun createInitialGameState(): GameState {
        val initialBuildings = listOf(
            Building(
                id = "bld_hq",
                name = "Штаб поселения",
                type = BuildingType.HQ_COMMAND,
                category = BuildingCategory.MANAGEMENT_LOGISTICS,
                level = 1,
                maxLevel = 5,
                description = "Центр координации и планирования аванпоста. Открывает слоты для жителей и новые типы зданий.",
                status = BuildingStatus.OPERATIONAL,
                requiredSettlementLevel = 1,
                buildCostMaterials = 100,
                buildCostMoney = 150,
                upgradeCostMaterials = 150,
                upgradeCostMoney = 250,
                xpRewardOnBuild = 100,
                xpRewardOnUpgrade = 80,
                dailyProductionDescription = "+25 Кр / день, Управление",
                bonusSummary = "+5 к лимиту населения",
                iconKey = "hq"
            ),
            Building(
                id = "bld_farm",
                name = "Гидропонная ферма",
                type = BuildingType.HYDROPONICS_FARM,
                category = BuildingCategory.PRODUCTION,
                level = 1,
                maxLevel = 5,
                description = "Автоматизированные грядки с LED-освещением для круглогодичного выращивания пищевых культур.",
                status = BuildingStatus.OPERATIONAL,
                requiredSettlementLevel = 1,
                buildCostMaterials = 120,
                buildCostMoney = 180,
                upgradeCostMaterials = 120,
                upgradeCostMoney = 180,
                xpRewardOnBuild = 90,
                xpRewardOnUpgrade = 50,
                dailyProductionDescription = "+25 Еды / день",
                bonusSummary = "Базовое пропитание жителей",
                iconKey = "farm"
            ),
            Building(
                id = "bld_water",
                name = "Скважинный фильтр",
                type = BuildingType.WATER_EXTRACTOR,
                category = BuildingCategory.PRODUCTION,
                level = 1,
                maxLevel = 5,
                description = "Глубокая артезианская скважина с многоступенчатой системой ионизации и очистки от радионуклидов.",
                status = BuildingStatus.OPERATIONAL,
                requiredSettlementLevel = 1,
                buildCostMaterials = 110,
                buildCostMoney = 160,
                upgradeCostMaterials = 110,
                upgradeCostMoney = 160,
                xpRewardOnBuild = 90,
                xpRewardOnUpgrade = 50,
                dailyProductionDescription = "+30 Воды / день",
                bonusSummary = "Чистая питьевая вода",
                iconKey = "water"
            ),
            Building(
                id = "bld_generator",
                name = "Дизель-генератор",
                type = BuildingType.GENERATOR_STATION,
                category = BuildingCategory.PRODUCTION,
                level = 1,
                maxLevel = 5,
                description = "Надёжная энергетическая установка, обеспечивающая светом фермы и оборонительные турели.",
                status = BuildingStatus.OPERATIONAL,
                requiredSettlementLevel = 1,
                buildCostMaterials = 140,
                buildCostMoney = 220,
                upgradeCostMaterials = 140,
                upgradeCostMoney = 220,
                xpRewardOnBuild = 100,
                xpRewardOnUpgrade = 60,
                dailyProductionDescription = "-5 Топлива / день, Энергия базы",
                bonusSummary = "Питание всех систем",
                iconKey = "generator"
            ),
            Building(
                id = "bld_workshop",
                name = "Мастерская утиля",
                type = BuildingType.WORKSHOP,
                category = BuildingCategory.PRODUCTION,
                level = 1,
                maxLevel = 5,
                description = "Станки для переплавки найденного металлолома и компонентов в строительные материалы.",
                status = BuildingStatus.OPERATIONAL,
                requiredSettlementLevel = 1,
                buildCostMaterials = 130,
                buildCostMoney = 200,
                upgradeCostMaterials = 130,
                upgradeCostMoney = 200,
                xpRewardOnBuild = 100,
                xpRewardOnUpgrade = 60,
                dailyProductionDescription = "+15 Материалов / день",
                bonusSummary = "Переработка сырья",
                iconKey = "workshop"
            ),
            Building(
                id = "bld_storage",
                name = "Укреплённый склад",
                type = BuildingType.STORAGE_DEPOT,
                category = BuildingCategory.MANAGEMENT_LOGISTICS,
                level = 1,
                maxLevel = 5,
                description = "Герметичные ангары со стеллажами для безопасного хранения собранных ресурсов и провианта.",
                status = BuildingStatus.OPERATIONAL,
                requiredSettlementLevel = 1,
                buildCostMaterials = 160,
                buildCostMoney = 240,
                upgradeCostMaterials = 160,
                upgradeCostMoney = 240,
                xpRewardOnBuild = 110,
                xpRewardOnUpgrade = 70,
                dailyProductionDescription = "+300 к лимиту хранилища",
                bonusSummary = "Текущий лимит: 800 ед.",
                iconKey = "storage"
            ),
            Building(
                id = "bld_clinic",
                name = "Полевой медпункт",
                type = BuildingType.MEDICAL_CLINIC,
                category = BuildingCategory.SURVIVAL_DEFENSE,
                level = 1,
                maxLevel = 3,
                description = "Лазарет для восстановления здоровья бойцов и стерилизации экспедиционных аптечек.",
                status = BuildingStatus.OPERATIONAL,
                requiredSettlementLevel = 1,
                buildCostMaterials = 150,
                buildCostMoney = 300,
                upgradeCostMaterials = 150,
                upgradeCostMoney = 300,
                xpRewardOnBuild = 100,
                xpRewardOnUpgrade = 65,
                dailyProductionDescription = "+15 HP / день раненым",
                bonusSummary = "Ускоренная регенерация",
                iconKey = "clinic"
            ),
            Building(
                id = "bld_defense",
                name = "Сторожевые вышки",
                type = BuildingType.DEFENSE_PERIMETER,
                category = BuildingCategory.SURVIVAL_DEFENSE,
                level = 1,
                maxLevel = 5,
                description = "Оборонительный периметр с прожекторами и огневыми точками для сдерживания рейдеров.",
                status = BuildingStatus.OPERATIONAL,
                requiredSettlementLevel = 1,
                buildCostMaterials = 180,
                buildCostMoney = 280,
                upgradeCostMaterials = 180,
                upgradeCostMoney = 280,
                xpRewardOnBuild = 110,
                xpRewardOnUpgrade = 70,
                dailyProductionDescription = "+20 к защите базы",
                bonusSummary = "Безопасность аванпоста",
                iconKey = "defense"
            ),
            Building(
                id = "bld_radio",
                name = "Радиовышка дальней связи",
                type = BuildingType.RADIO_TOWER,
                category = BuildingCategory.MANAGEMENT_LOGISTICS,
                level = 0,
                maxLevel = 3,
                description = "Мощная мачта с ретранслятором для перехвата радиосигналов и предупреждения о перемещениях банд.",
                status = BuildingStatus.LOCKED,
                requiredSettlementLevel = 2,
                buildCostMaterials = 220,
                buildCostMoney = 350,
                upgradeCostMaterials = 200,
                upgradeCostMoney = 320,
                xpRewardOnBuild = 140,
                xpRewardOnUpgrade = 85,
                dailyProductionDescription = "Раннее обнаружение угроз",
                bonusSummary = "Снижает риск засад в вылазках на 20%",
                iconKey = "radio"
            ),
            Building(
                id = "bld_trading",
                name = "Торговый караван-сарай",
                type = BuildingType.TRADING_POST,
                category = BuildingCategory.MANAGEMENT_LOGISTICS,
                level = 0,
                maxLevel = 3,
                description = "Охраняемая торговая площадка для привлечения странствующих торговцев пустоши.",
                status = BuildingStatus.LOCKED,
                requiredSettlementLevel = 2,
                buildCostMaterials = 250,
                buildCostMoney = 400,
                upgradeCostMaterials = 220,
                upgradeCostMoney = 360,
                xpRewardOnBuild = 150,
                xpRewardOnUpgrade = 90,
                dailyProductionDescription = "+35 Кр / день, скидки на рынке",
                bonusSummary = "Выгодный курс обмена ресурсов",
                iconKey = "trading"
            ),
            Building(
                id = "bld_research",
                name = "Исследовательский центр",
                type = BuildingType.RESEARCH_LAB,
                category = BuildingCategory.MANAGEMENT_LOGISTICS,
                level = 1,
                maxLevel = 5,
                description = "Лаборатория с чертёжными столами, химическими реакторами и архивом довоенных технологий.",
                status = BuildingStatus.OPERATIONAL,
                requiredSettlementLevel = 1,
                buildCostMaterials = 150,
                buildCostMoney = 200,
                upgradeCostMaterials = 150,
                upgradeCostMoney = 220,
                xpRewardOnBuild = 120,
                xpRewardOnUpgrade = 70,
                dailyProductionDescription = "Доступ к веткам исследований",
                bonusSummary = "Открывает технологии Ур. 1",
                iconKey = "science"
            ),
            Building(
                id = "bld_armory",
                name = "Оружейная лаборатория",
                type = BuildingType.ARMORY_LAB,
                category = BuildingCategory.SURVIVAL_DEFENSE,
                level = 0,
                maxLevel = 3,
                description = "Комплекс для модификации оружия, калибровки прицелов и выплавки бронепластин.",
                status = BuildingStatus.LOCKED,
                requiredSettlementLevel = 3,
                buildCostMaterials = 320,
                buildCostMoney = 500,
                upgradeCostMaterials = 280,
                upgradeCostMoney = 450,
                xpRewardOnBuild = 180,
                xpRewardOnUpgrade = 110,
                dailyProductionDescription = "Военное превосходство",
                bonusSummary = "+3 к урону всех бойцов отряда",
                iconKey = "armory"
            ),
            Building(
                id = "bld_greenhouse",
                name = "Автономный биокупол",
                type = BuildingType.GREENHOUSE_COMPLEX,
                category = BuildingCategory.PRODUCTION,
                level = 0,
                maxLevel = 3,
                description = "Высокотехнологичный купол с искусственным климатом для синтеза лекарственных культур и редких трав.",
                status = BuildingStatus.LOCKED,
                requiredSettlementLevel = 4,
                buildCostMaterials = 450,
                buildCostMoney = 700,
                upgradeCostMaterials = 380,
                upgradeCostMoney = 600,
                xpRewardOnBuild = 220,
                xpRewardOnUpgrade = 140,
                dailyProductionDescription = "+50 Еды и сырьё для медикаментов",
                bonusSummary = "Полная продовольственная независимость",
                iconKey = "greenhouse"
            )
        )

        val initialCharacters = listOf(
            Character(
                id = "char_1",
                name = "Алексей «Сокол» Смирнов",
                role = CharacterRole.SCOUT,
                level = 1,
                experience = 25,
                maxExperience = 100,
                health = 100,
                maxHealth = 100,
                status = CharacterStatus.IN_SQUAD,
                stats = CharacterStats(attack = 14, defense = 9, scavengingSkill = 18, engineeringSkill = 6, medicalSkill = 5),
                equipmentSummary = "Утеплённая куртка, сапоги следопыта, походный рюкзак, сканер руин, ПНВ",
                avatarTag = "scout",
                bio = "Ветеран разведывательных рейдов. Знает опасные тропы и слепые зоны патрулей мутантов.",
                specialization = "Дальняя разведка",
                unspentSkillPoints = 1,
                morale = 95,
                energy = 100,
                traits = listOf(TraitCatalog.EAGLE_EYE, TraitCatalog.NIGHT_STALKER),
                expeditionsCount = 4,
                daysInSettlement = 12,
                threatsNeutralizedCount = 2,
                equipment = CharacterEquipment(
                    slots = mapOf(
                        EquipmentSlotType.OUTFIT to "item_jacket_stalker",
                        EquipmentSlotType.FOOTWEAR to "item_boots_scout",
                        EquipmentSlotType.BACKPACK to "item_backpack_medium",
                        EquipmentSlotType.TOOL to "item_tool_scanner",
                        EquipmentSlotType.SPECIAL to "item_spec_nvg"
                    )
                )
            ),
            Character(
                id = "char_2",
                name = "Виктор «Молот» Громов",
                role = CharacterRole.SOLDIER,
                level = 1,
                experience = 10,
                maxExperience = 100,
                health = 120,
                maxHealth = 120,
                status = CharacterStatus.IN_SQUAD,
                stats = CharacterStats(attack = 22, defense = 16, scavengingSkill = 7, engineeringSkill = 4, medicalSkill = 3),
                equipmentSummary = "Композитный бронекостюм, тактические берцы, штурмовой баул, мачете, жетон",
                avatarTag = "soldier",
                bio = "Бывший страж торгового каравана. Непоколебим в ближнем бою и при обороне периметра.",
                specialization = "Тяжёлый штурм",
                unspentSkillPoints = 1,
                morale = 90,
                energy = 100,
                traits = listOf(TraitCatalog.TANK_BUILD, TraitCatalog.IRON_NERVES),
                expeditionsCount = 6,
                daysInSettlement = 15,
                threatsNeutralizedCount = 5,
                equipment = CharacterEquipment(
                    slots = mapOf(
                        EquipmentSlotType.OUTFIT to "item_armor_composite",
                        EquipmentSlotType.FOOTWEAR to "item_boots_armored",
                        EquipmentSlotType.BACKPACK to "item_backpack_military",
                        EquipmentSlotType.TOOL to "item_tool_machete",
                        EquipmentSlotType.SPECIAL to "item_spec_amulet"
                    )
                )
            ),
            Character(
                id = "char_3",
                name = "Елена Власова",
                role = CharacterRole.MEDIC,
                level = 1,
                experience = 15,
                maxExperience = 100,
                health = 90,
                maxHealth = 90,
                status = CharacterStatus.READY,
                stats = CharacterStats(attack = 9, defense = 10, scavengingSkill = 11, engineeringSkill = 7, medicalSkill = 20),
                equipmentSummary = "Кевларовая куртка, треккинговые ботинки, брезентовый рюкзак, мультитул, реаниматор",
                avatarTag = "medic",
                bio = "Полевой врач, спасшая десятки жизней после радиационного выброса на секторе B-4.",
                specialization = "Полевая хирургия",
                unspentSkillPoints = 0,
                morale = 85,
                energy = 100,
                traits = listOf(TraitCatalog.COMBAT_MEDIC, TraitCatalog.RADIATION_RESISTANT),
                expeditionsCount = 2,
                daysInSettlement = 8,
                threatsNeutralizedCount = 0,
                equipment = CharacterEquipment(
                    slots = mapOf(
                        EquipmentSlotType.OUTFIT to "item_jacket_reinforced",
                        EquipmentSlotType.FOOTWEAR to "item_boots_trekking",
                        EquipmentSlotType.BACKPACK to "item_backpack_simple",
                        EquipmentSlotType.TOOL to "item_tool_multitool",
                        EquipmentSlotType.SPECIAL to "item_spec_firstaid"
                    )
                )
            ),
            Character(
                id = "char_4",
                name = "Дмитрий «Ключ» Романов",
                role = CharacterRole.ENGINEER,
                level = 1,
                experience = 5,
                maxExperience = 100,
                health = 95,
                maxHealth = 95,
                status = CharacterStatus.READY,
                stats = CharacterStats(attack = 11, defense = 11, scavengingSkill = 13, engineeringSkill = 21, medicalSkill = 6),
                equipmentSummary = "Базовая экипировка инженера",
                avatarTag = "engineer",
                bio = "Механик от бога. Умеет запускать заглохшие генераторы и собирать оружие из деталей станков.",
                specialization = "Ремонт и крафт",
                unspentSkillPoints = 0,
                morale = 88,
                energy = 100,
                traits = listOf(TraitCatalog.GREASE_MONKEY, TraitCatalog.GUNSMITH),
                expeditionsCount = 3,
                daysInSettlement = 10,
                threatsNeutralizedCount = 1
            ),
            Character(
                id = "char_5",
                name = "София Ли",
                role = CharacterRole.SCAVENGER,
                level = 1,
                experience = 40,
                maxExperience = 100,
                health = 100,
                maxHealth = 100,
                status = CharacterStatus.READY,
                stats = CharacterStats(attack = 13, defense = 8, scavengingSkill = 24, engineeringSkill = 8, medicalSkill = 7),
                equipmentSummary = "Базовая экипировка собирателя",
                avatarTag = "scavenger",
                bio = "Следопыт с феноменальным чутьём на тайники и редкие компоненты в заброшенных бункерах.",
                specialization = "Поиск схронов",
                unspentSkillPoints = 1,
                morale = 92,
                energy = 100,
                traits = listOf(TraitCatalog.SCRAP_HOARDER, TraitCatalog.CAUTIOUS_PACER),
                expeditionsCount = 5,
                daysInSettlement = 14,
                threatsNeutralizedCount = 1
            ),
            Character(
                id = "char_6",
                name = "Михаил «Гранит» Орлов",
                role = CharacterRole.SOLDIER,
                level = 1,
                experience = 0,
                maxExperience = 100,
                health = 115,
                maxHealth = 115,
                status = CharacterStatus.READY,
                stats = CharacterStats(attack = 20, defense = 14, scavengingSkill = 8, engineeringSkill = 5, medicalSkill = 4),
                equipmentSummary = "Базовая экипировка часового",
                avatarTag = "soldier",
                bio = "Часовой аванпоста. Всегда начеку, прикрывает тыл и караулит подступы к складам.",
                specialization = "Оборона базы",
                unspentSkillPoints = 0,
                morale = 90,
                energy = 100,
                traits = listOf(TraitCatalog.TACTICAL_SNIPER, TraitCatalog.INSPIRING_LEADER),
                expeditionsCount = 1,
                daysInSettlement = 6,
                threatsNeutralizedCount = 3
            )
        )

        val initialSettlement = Settlement(
            name = "Аванпост «Фронтир»",
            level = 1,
            xp = 60,
            xpToNextLevel = 225,
            reputation = 50,
            population = initialCharacters.size,
            maxPopulation = 23,
            defenseRating = 70,
            tier = SettlementTier.SURVIVOR_CAMP,
            buildings = initialBuildings,
            dailyFoodConsumption = initialCharacters.size,
            dailyWaterConsumption = initialCharacters.size
        )

        val initialResources = GameResources(
            money = 1200,
            food = 150,
            water = 180,
            fuel = 80,
            materials = 240,
            warehouseMaxCapacity = 800,
            extraResources = mapOf(
                ResourceType.MEDICINE to 12,
                ResourceType.AMMO to 35,
                ResourceType.COMPONENTS to 18,
                ResourceType.RARE_ALLOY to 4
            )
        )

        val initialVehicles = listOf(
            Vehicle(
                id = "veh_foot",
                name = "Пеший переход",
                type = VehicleType.FOOT,
                capacityKg = 25,
                fuelConsumptionPerKm = 0f,
                speedKmH = 5,
                maxPassengers = 4,
                status = VehicleStatus.AVAILABLE,
                isAvailable = true,
                isUnlocked = true,
                durabilityPercent = 100,
                description = "Базовое пешее перемещение группы. Не требует топлива, но группа движется медленно и расходует больше запасов воды и пищи.",
                visualAssetId = "veh_foot"
            ),
            Vehicle(
                id = "veh_bike_1",
                name = "Путевой велосипед «Стриж»",
                type = VehicleType.BICYCLE,
                capacityKg = 45,
                fuelConsumptionPerKm = 0f,
                speedKmH = 14,
                maxPassengers = 1,
                status = VehicleStatus.AVAILABLE,
                isAvailable = true,
                isUnlocked = true,
                durabilityPercent = 100,
                description = "Усиленный дорожный велосипед с багажными сумками. Втрое быстрее пешего шага, не требует горючего.",
                visualAssetId = "veh_bike"
            ),
            Vehicle(
                id = "veh_buggy_1",
                name = "Развед-багги «Бархан»",
                type = VehicleType.LIGHT_BUGGY,
                capacityKg = 190,
                fuelConsumptionPerKm = 0.65f,
                speedKmH = 35,
                maxPassengers = 2,
                status = VehicleStatus.AVAILABLE,
                isAvailable = true,
                isUnlocked = true,
                durabilityPercent = 95,
                description = "Быстрый разведывательный транспорт с лёгкой трубчатой рамой и усиленной подвеской. Требует топлива.",
                visualAssetId = "veh_buggy"
            ),
            Vehicle(
                id = "veh_truck_1",
                name = "Бронегрузовик «Утёс»",
                type = VehicleType.ARMORED_TRUCK,
                capacityKg = 650,
                fuelConsumptionPerKm = 1.6f,
                speedKmH = 22,
                maxPassengers = 5,
                status = VehicleStatus.AVAILABLE,
                isAvailable = true,
                isUnlocked = true,
                durabilityPercent = 100,
                description = "Тяжёлая защищённая машина для масштабных экспедиций в глубокие зоны пустоши с огромным кузовом.",
                visualAssetId = "veh_truck"
            )
        )

        val initialLocations = listOf(
            Location(
                id = "loc_base",
                name = "Аванпост «Фронтир»",
                type = LocationType.SETTLEMENT,
                dangerLevel = DangerLevel.SAFE,
                isUnlocked = true,
                status = LocationStatus.EXPLORED,
                distanceKm = 0,
                potentialLoot = listOf("Убежище", "Склад", "Мастерская"),
                description = "Ваша главная база и центр операций. Защищённые ангары, артезианская скважина и оборонительный периметр.",
                estimatedLootMaterials = 0,
                estimatedLootCredits = 0,
                estimatedLootFood = 0,
                estimatedLootFuel = 0,
                coordinateX = 0.50f,
                coordinateY = 0.50f,
                terrainType = TerrainType.WASTELAND,
                isPlayerBase = true,
                sectorCode = "SEC-00",
                visualAssetId = "loc_base",
                observations = listOf(
                    "Оборонительный периметр находится в полной боеготовности",
                    "Часовые ведут круглосуточное наблюдение с вышек",
                    "Генераторы и насосная станция работают стабильно"
                ),
                threats = listOf("Спокойный сектор под контролем выживших"),
                localAreas = listOf(
                    LocalArea("base_hq", "Командный центр", "Управление", isDiscovered = true, isExplored = true),
                    LocalArea("base_wh", "Главный складской ангар", "Склад", isDiscovered = true, isExplored = true),
                    LocalArea("base_ws", "Инженерная мастерская", "Крафт", isDiscovered = true, isExplored = true)
                )
            ),
            Location(
                id = "loc_3",
                name = "Торговый форпост «Перекрёсток»",
                type = LocationType.TRADING_POST,
                dangerLevel = DangerLevel.SAFE,
                isUnlocked = true,
                status = LocationStatus.AVAILABLE,
                distanceKm = 7,
                potentialLoot = listOf("Торговля", "Слухи", "Экипировка"),
                description = "Укреплённый лагерь кочующих торговцев и караванов. Безопасная нейтральная зона для бартера и найма.",
                estimatedLootMaterials = 25,
                estimatedLootCredits = 190,
                estimatedLootFood = 40,
                estimatedLootFuel = 35,
                coordinateX = 0.42f,
                coordinateY = 0.65f,
                terrainType = TerrainType.WASTELAND,
                sectorCode = "SEC-01",
                visualAssetId = "loc_trading_post",
                observations = listOf(
                    "У въезда припаркованы тяжелогруженые фургоны торговцев",
                    "Вооружённая охрана бдительно проверяет входящих гостей",
                    "Слышен гул базарной площади и звон монет"
                ),
                threats = listOf("Нейтральная территория. Карманники и нечестные торговцы"),
                localAreas = listOf(
                    LocalArea("loc_3_a1", "Базарная площадь", "Торговля", isDiscovered = true),
                    LocalArea("loc_3_a2", "Караванный постоялый двор", "Отдых", isDiscovered = true),
                    LocalArea("loc_3_a3", "Склад гильдии купцов", "Охраняемая зона", isDiscovered = true)
                )
            ),
            Location(
                id = "loc_1",
                name = "Станция «Северный разъезд»",
                type = LocationType.ABANDONED_STATION,
                dangerLevel = DangerLevel.LOW,
                isUnlocked = true,
                status = LocationStatus.AVAILABLE,
                distanceKm = 9,
                potentialLoot = listOf("Материалы", "Топливо", "Запчасти"),
                description = "Полуразрушенная узловая станция с железнодорожными тупиками. Сохранились цистерны с горючим и ремонтные ангары.",
                estimatedLootMaterials = 70,
                estimatedLootCredits = 120,
                estimatedLootFood = 20,
                estimatedLootFuel = 45,
                coordinateX = 0.32f,
                coordinateY = 0.34f,
                terrainType = TerrainType.RUINS,
                sectorCode = "SEC-02",
                visualAssetId = "loc_station",
                observations = listOf(
                    "Ржавые товарные вагоны сошли с путей и перекрыли главный перрон",
                    "Сквозь пыльные окна диспетчерской видны уцелевшие шкафы с документами и инструментами",
                    "На подъездных путях видны свежие следы костров бродячих разведчиков"
                ),
                threats = listOf(
                    "Нестабильные прогнившие перекрытия депо",
                    "Острые обломки металлоконструкций",
                    "Одиночные одичавшие собаки в тупиках"
                ),
                localAreas = listOf(
                    LocalArea("loc_1_a1", "Главный перрон и стрелочный пост", "Открытая площадка", isDiscovered = true),
                    LocalArea("loc_1_a2", "Диспетчерская вышка", "Служебное здание", isDiscovered = true),
                    LocalArea("loc_1_a3", "Локомотивное ремонтное депо", "Промышленный ангар", isDiscovered = true),
                    LocalArea("loc_1_a4", "Топливные цистерны тупика №4", "Склад ГСМ", isDiscovered = true)
                )
            ),
            Location(
                id = "loc_7",
                name = "Старая ферма «Заря»",
                type = LocationType.FARM,
                dangerLevel = DangerLevel.LOW,
                isUnlocked = true,
                status = LocationStatus.AVAILABLE,
                distanceKm = 12,
                potentialLoot = listOf("Зерно", "Вода", "Удобрения"),
                description = "Заброшенный агрокомплекс с уцелевшими элеваторами и пересохшим водохранилищем. Низкая активность диких зверей.",
                estimatedLootMaterials = 35,
                estimatedLootCredits = 80,
                estimatedLootFood = 90,
                estimatedLootFuel = 10,
                coordinateX = 0.36f,
                coordinateY = 0.18f,
                terrainType = TerrainType.WATER,
                sectorCode = "SEC-03",
                visualAssetId = "loc_farm",
                observations = listOf(
                    "Металлический элеватор накренился, но бункеры с семенами закрыты герметично",
                    "В теплицах сохранились остатки автоматического полива и пластиковые ёмкости",
                    "Территория вокруг силосных башен поросла густым колючим кустарником"
                ),
                threats = listOf(
                    "Глубокие незаметные колодцы в траве",
                    "Гнезда ядовитых пустошных ос в силосных башнях"
                ),
                localAreas = listOf(
                    LocalArea("loc_7_a1", "Зерновой элеватор", "Хранилище", isDiscovered = true),
                    LocalArea("loc_7_a2", "Гидропонные теплицы", "Агрокомплекс", isDiscovered = true),
                    LocalArea("loc_7_a3", "Фермерский жилой дом", "Жилое здание", isDiscovered = true)
                )
            ),
            Location(
                id = "loc_2",
                name = "Посёлок «Сосновый бор»",
                type = LocationType.VILLAGE,
                dangerLevel = DangerLevel.MODERATE,
                isUnlocked = true,
                status = LocationStatus.AVAILABLE,
                distanceKm = 16,
                potentialLoot = listOf("Провизия", "Медикаменты", "Вода"),
                description = "Оставленный жителями пригородный посёлок среди сосен. В погребах и аптечном пункте сохранились ценные запасы.",
                estimatedLootMaterials = 45,
                estimatedLootCredits = 95,
                estimatedLootFood = 75,
                estimatedLootFuel = 15,
                coordinateX = 0.65f,
                coordinateY = 0.30f,
                terrainType = TerrainType.FOREST,
                sectorCode = "SEC-04",
                visualAssetId = "loc_village",
                observations = listOf(
                    "Деревянные и кирпичные коттеджи с заколоченными ставнями",
                    "Здание фельдшерского пункта частично уцелело, дверь сорвана с петель",
                    "В глубине улицы видны следы старых баррикад и обгоревший остов легкового авто"
                ),
                threats = listOf(
                    "Старые самодельные растяжки и ловушки мародёров",
                    "Стаи бродячих волков-мутантов на окраинах",
                    "Гнилые полы в подвалах"
                ),
                localAreas = listOf(
                    LocalArea("loc_2_a1", "Центральная улица и магазин", "Торговый ряд", isDiscovered = true),
                    LocalArea("loc_2_a2", "Сельский медпункт", "Медицинское учреждение", isDiscovered = true),
                    LocalArea("loc_2_a3", "Усадьба с погребом", "Укрытие", isDiscovered = true)
                )
            ),
            Location(
                id = "loc_8",
                name = "Хвойная чаща «Мёртвый лес»",
                type = LocationType.FOREST,
                dangerLevel = DangerLevel.MODERATE,
                isUnlocked = true,
                status = LocationStatus.AVAILABLE,
                distanceKm = 22,
                potentialLoot = listOf("Древесина", "Травы", "Дичь"),
                description = "Густой хвойный массив с высокими мутировавшими елями. Природный источник ценных лекарственных трав и сырья.",
                estimatedLootMaterials = 80,
                estimatedLootCredits = 70,
                estimatedLootFood = 60,
                estimatedLootFuel = 25,
                coordinateX = 0.76f,
                coordinateY = 0.44f,
                terrainType = TerrainType.FOREST,
                sectorCode = "SEC-05",
                visualAssetId = "loc_forest",
                observations = listOf(
                    "Стволы исполинских елей покрыты фосфоресцирующим мхом",
                    "Звуки треска сучьев и приглушенный рык в глубине чащи",
                    "Заросшая грунтовая дорога ведёт к заброшенной лесопилке"
                ),
                threats = listOf(
                    "Агрессивные лесные хищники и медведи-мутанты",
                    "Ядовитые споры грибниц в низинах",
                    "Опасность потерять ориентиры в тумане"
                ),
                localAreas = listOf(
                    LocalArea("loc_8_a1", "Край вырубки и старая лесопилка", "Промзона", isDiscovered = true),
                    LocalArea("loc_8_a2", "Охотничья сторожка", "Укрытие", isDiscovered = true),
                    LocalArea("loc_8_a3", "Глухая лощина с реликтовыми соснами", "Природная зона", isDiscovered = true)
                )
            ),
            Location(
                id = "loc_4",
                name = "Логистический парк «Вектор»",
                type = LocationType.WAREHOUSE_COMPLEX,
                dangerLevel = DangerLevel.HIGH,
                isUnlocked = true,
                status = LocationStatus.AVAILABLE,
                distanceKm = 27,
                potentialLoot = listOf("Электроника", "Компоненты", "Инструменты"),
                description = "Огромный складской комплекс с роботизированными погрузчиками. Территория охраняется бандами мародёров.",
                estimatedLootMaterials = 145,
                estimatedLootCredits = 260,
                estimatedLootFood = 50,
                estimatedLootFuel = 80,
                coordinateX = 0.74f,
                coordinateY = 0.72f,
                terrainType = TerrainType.HILLS,
                sectorCode = "SEC-06",
                visualAssetId = "loc_warehouse",
                observations = listOf(
                    "Гигантские ангары из профилированного листа с автоматическими воротами",
                    "На крышах складов оборудованы стрелковые гнезда с наблюдателями",
                    "Работает автономный дизель-генератор, питающий систему прожекторов"
                ),
                threats = listOf(
                    "Организованная банда мародёров с автоматическим оружием",
                    "Автоматические охранные турели складского терминала",
                    "Взрывоопасные цистерны"
                ),
                localAreas = listOf(
                    LocalArea("loc_4_a1", "Погрузочный терминал А-1", "Складской бокс", isDiscovered = true),
                    LocalArea("loc_4_a2", "Административно-бытовой корпус", "Офисы", isDiscovered = true),
                    LocalArea("loc_4_a3", "Защищённое хранилище электроники", "Спецхран", isDiscovered = true)
                )
            ),
            Location(
                id = "loc_5",
                name = "Руины завода «ПромСвязь»",
                type = LocationType.INDUSTRIAL_PLANT,
                dangerLevel = DangerLevel.HIGH,
                isUnlocked = true,
                status = LocationStatus.AVAILABLE,
                distanceKm = 34,
                potentialLoot = listOf("Редкие сплавы", "Микросхемы", "Тяжёлый лом"),
                description = "Разбитый промышленный квартал с обрушившимися литейными цехами. Требует хорошо вооружённого отряда.",
                estimatedLootMaterials = 190,
                estimatedLootCredits = 310,
                estimatedLootFood = 15,
                estimatedLootFuel = 90,
                coordinateX = 0.20f,
                coordinateY = 0.74f,
                terrainType = TerrainType.RUINS,
                sectorCode = "SEC-07",
                visualAssetId = "loc_industrial",
                observations = listOf(
                    "Массивные трубы котельной возвышаются над грудами битого бетона",
                    "В литейном цехе уцелели роботизированные станки и медные шины",
                    "По периметру растянута ржавая колючая проволока"
                ),
                threats = listOf(
                    "Химические протечки и кислотные лужи",
                    "Обрушения ветхих железобетонных балок",
                    "Вооруженные рейдеры-старьевщики"
                ),
                localAreas = listOf(
                    LocalArea("loc_5_a1", "Литейный цех №2", "Промцех", isDiscovered = true),
                    LocalArea("loc_5_a2", "Лаборатория микроэлектроники", "Лаборатория", isDiscovered = true),
                    LocalArea("loc_5_a3", "Трансформаторная подстанция", "Энергоузел", isDiscovered = true)
                )
            ),
            Location(
                id = "loc_6",
                name = "Бункер «Объект 42»",
                type = LocationType.MILITARY_BUNKER,
                dangerLevel = DangerLevel.EXTREME,
                isUnlocked = false,
                status = LocationStatus.LOCKED,
                distanceKm = 52,
                potentialLoot = listOf("Военные чипы", "Энергоядра", "Секретные протоколы"),
                description = "Запечатанный подземный объект довоенной эпохи с кодовой гермодверью. Требует технологию «Биосканирование» или взлом.",
                estimatedLootMaterials = 300,
                estimatedLootCredits = 600,
                estimatedLootFood = 100,
                estimatedLootFuel = 200,
                coordinateX = 0.86f,
                coordinateY = 0.22f,
                terrainType = TerrainType.HILLS,
                requiredTechId = "tech_bio_scan",
                requiredSettlementLevel = 2,
                sectorCode = "SEC-08",
                visualAssetId = "loc_bunker",
                observations = listOf(
                    "Массивная титановая гермодверь с кодовым терминалом биосканирования",
                    "Вентиляционные шахты забиты фильтрами с остатками токсичных газов",
                    "Мигают аварийные красные маяки автономного контура питания"
                ),
                threats = listOf(
                    "Боевые дроиды протокола зачистки объекта",
                    "Смертельный уровень радиации в реакторном отсеке",
                    "Лазерные защитные датчики в коридорах"
                ),
                localAreas = listOf(
                    LocalArea("loc_6_a1", "Шлюзовой блок и КПП", "Охрана", isDiscovered = true),
                    LocalArea("loc_6_a2", "Командный бункер и серверная", "Связь", isDiscovered = true),
                    LocalArea("loc_6_a3", "Арсенал тяжелого вооружения", "Склад оружия", isDiscovered = true)
                )
            ),
            Location(
                id = "loc_9",
                name = "Аномальный сектор «Затмение»",
                type = LocationType.ANOMALY_ZONE,
                dangerLevel = DangerLevel.UNKNOWN,
                isUnlocked = false,
                status = LocationStatus.UNKNOWN,
                distanceKm = 48,
                potentialLoot = listOf("??? Реликтовая энергия", "??? Неизвестные данные"),
                description = "Зона сильных электромагнитных и радиационных искажений. Бортовые компасы сходят с ума.",
                estimatedLootMaterials = 220,
                estimatedLootCredits = 450,
                estimatedLootFood = 0,
                estimatedLootFuel = 150,
                coordinateX = 0.14f,
                coordinateY = 0.44f,
                terrainType = TerrainType.WASTELAND,
                sectorCode = "SEC-09",
                visualAssetId = "loc_anomaly",
                observations = listOf(
                    "Фиолетовые электрические разряды дугами бьют в растрескавшийся грунт",
                    "Стрелки компаса и дозиметров непрерывно вращаются вокруг своей оси",
                    "В воздухе зависли парящие куски оплавленного асфальта"
                ),
                threats = listOf(
                    "Гравитационные аномалии и вихри",
                    "Электромагнитные импульсы, сжигающие электронику",
                    "Психические галлюцинации у бойцов"
                ),
                localAreas = listOf(
                    LocalArea("loc_9_a1", "Эпицентр гравитационного разлома", "Аномалия", isDiscovered = true),
                    LocalArea("loc_9_a2", "Оплавленный научный фургон", "Обломки", isDiscovered = true),
                    LocalArea("loc_9_a3", "Кристаллическое поле", "Реликты", isDiscovered = true)
                )
            )
        )

        val initialTechnologies = ResearchConfig.createDefaultTechnologies()

        val initialQuests = listOf(
            Quest(
                id = "quest_1",
                title = "Первый шаг в пустошь",
                description = "Отправьте разведывательный отряд в «Северный разъезд» и вернитесь с первой партией припасов.",
                requirementDescription = "Завершите экспедицию в любую точку",
                progress = 0,
                target = 1,
                rewardCredits = 150,
                rewardReputation = 15,
                rewardMaterials = 60,
                status = QuestStatus.IN_PROGRESS
            ),
            Quest(
                id = "quest_2",
                title = "Строительный запас",
                description = "Накопите не менее 300 единиц стройматериалов на складе для запланированной модернизации.",
                requirementDescription = "Собрать 300 материалов на складе",
                progress = 240,
                target = 300,
                rewardCredits = 120,
                rewardReputation = 10,
                rewardMaterials = 80,
                status = QuestStatus.IN_PROGRESS
            ),
            Quest(
                id = "quest_3",
                title = "Развитие инфраструктуры",
                description = "Улучшите любое здание поселения до 2-го уровня для укрепления аванпоста.",
                requirementDescription = "Улучшить 1 здание базы",
                progress = 0,
                target = 1,
                rewardCredits = 200,
                rewardReputation = 25,
                rewardMaterials = 100,
                status = QuestStatus.IN_PROGRESS
            )
        )

        val initialQuestStates = mutableMapOf<String, QuestState>()
        
        // Quest 1: First Supplies (Active)
        initialQuestStates[QuestCatalog.QUEST_FIRST_SUPPLIES.id] = QuestState(
            questId = QuestCatalog.QUEST_FIRST_SUPPLIES.id,
            status = QuestStatus.ACTIVE,
            acceptedGameDateTime = GameDateTime.START_TIME,
            objectiveProgress = mapOf(
                "obj_collect_mat" to QuestObjectiveProgress(
                    objectiveId = "obj_collect_mat",
                    currentAmount = initialResources.materials.coerceAtMost(50),
                    targetAmount = 50,
                    status = if (initialResources.materials >= 50) ObjectiveStatus.COMPLETED else ObjectiveStatus.IN_PROGRESS
                )
            )
        )

        // Quest 2: Scout North (Active)
        initialQuestStates[QuestCatalog.QUEST_SCOUT_NORTH.id] = QuestState(
            questId = QuestCatalog.QUEST_SCOUT_NORTH.id,
            status = QuestStatus.ACTIVE,
            acceptedGameDateTime = GameDateTime.START_TIME,
            objectiveProgress = mapOf(
                "obj_visit_north" to QuestObjectiveProgress(
                    objectiveId = "obj_visit_north",
                    currentAmount = 0,
                    targetAmount = 1,
                    status = ObjectiveStatus.IN_PROGRESS
                ),
                "obj_return_base" to QuestObjectiveProgress(
                    objectiveId = "obj_return_base",
                    currentAmount = 0,
                    targetAmount = 1,
                    status = ObjectiveStatus.NOT_STARTED
                )
            )
        )

        // Quest 3: Defense Perimeter (Active)
        initialQuestStates[QuestCatalog.QUEST_DEFENSE_PERIMETER.id] = QuestState(
            questId = QuestCatalog.QUEST_DEFENSE_PERIMETER.id,
            status = QuestStatus.ACTIVE,
            acceptedGameDateTime = GameDateTime.START_TIME,
            objectiveProgress = mapOf(
                "obj_upgrade_bld" to QuestObjectiveProgress(
                    objectiveId = "obj_upgrade_bld",
                    currentAmount = 0,
                    targetAmount = 1,
                    status = ObjectiveStatus.IN_PROGRESS
                ),
                "obj_research_tech" to QuestObjectiveProgress(
                    objectiveId = "obj_research_tech",
                    currentAmount = 0,
                    targetAmount = 1,
                    status = ObjectiveStatus.IN_PROGRESS
                )
            )
        )

        // Quest 4: Missing Cargo (Available)
        initialQuestStates[QuestCatalog.QUEST_MISSING_CARGO.id] = QuestState(
            questId = QuestCatalog.QUEST_MISSING_CARGO.id,
            status = QuestStatus.AVAILABLE,
            objectiveProgress = mapOf(
                "obj_visit_quarry" to QuestObjectiveProgress(
                    objectiveId = "obj_visit_quarry",
                    currentAmount = 0,
                    targetAmount = 1,
                    status = ObjectiveStatus.IN_PROGRESS
                ),
                "obj_obtain_cargo" to QuestObjectiveProgress(
                    objectiveId = "obj_obtain_cargo",
                    currentAmount = 0,
                    targetAmount = 1,
                    status = ObjectiveStatus.NOT_STARTED
                ),
                "obj_deliver_cargo" to QuestObjectiveProgress(
                    objectiveId = "obj_deliver_cargo",
                    currentAmount = 0,
                    targetAmount = 1,
                    status = ObjectiveStatus.NOT_STARTED
                )
            )
        )

        return GameState(
            gameDateTime = GameDateTime.START_TIME,
            processedDays = setOf(1),
            settlement = initialSettlement,
            resources = initialResources,
            characters = initialCharacters,
            vehicles = initialVehicles,
            locations = initialLocations,
            technologies = initialTechnologies,
            quests = initialQuests,
            questStates = initialQuestStates,
            trackedQuestId = QuestCatalog.QUEST_FIRST_SUPPLIES.id,
            squad = Squad(
                id = "squad_main",
                name = "Экспедиционная группа «Фронтир»",
                memberIds = listOf("char_1", "char_2"),
                leaderId = "char_1",
                assignedVehicleId = "veh_foot",
                status = SquadStatus.READY
            ),
            selectedSquadIds = setOf("char_1", "char_2"),
            selectedVehicleId = "veh_foot",
            merchantState = MerchantState(
                merchant = MerchantProfile(),
                offers = TradeConfig.createDefaultTradeOffers()
            ),
            inventoryItems = createDefaultInventoryItems(),
            factionRelations = ReputationBalanceConfig.createInitialFactionRelations(),
            reputationHistory = listOf(
                com.example.domain.model.reputation.ReputationHistoryEntry(
                    id = "rep_init_1",
                    day = 1,
                    gameDateTime = GameDateTime.START_TIME,
                    sourceTitle = "Основание аванпоста",
                    reasonDescription = "Начальное признание среди вольных поселенцев сектора",
                    delta = 50,
                    type = com.example.domain.model.reputation.ReputationChangeType.SURVIVOR_RESCUE
                )
            ),
            dayLogs = listOf(
                "День 1: Аванпост «Фронтир» запущен в автономном режиме.",
                "Связист: «Радиоэфир чист, датчики показывают нормальный радиационный фон вблизи базы».",
                "Командир: «Необходимо проверить экипировку отряда и исследовать ближайшие ориентиры на карте»."
            )
        )
    }

    fun createDefaultInventoryItems(): List<WarehouseItem> {
        return listOf(
            // --- OUTFITS (Верхняя одежда / Броня) ---
            WarehouseItem(
                id = "item_jacket_stalker",
                name = "Утеплённая куртка сталкера",
                category = ItemCategory.EQUIPMENT_AND_TOOLS,
                rarity = ItemRarity.COMMON,
                description = "Плотная прорезиненная ветровка с подкладкой из синтепона. Защищает от сырости и ночного холода.",
                baseValueCredits = 120,
                iconKey = "jacket",
                isEquipable = true,
                equipSlot = EquipmentSlotType.OUTFIT,
                weightKg = 2.5f,
                equipmentBonus = EquipmentBonus(
                    bonusDefense = 4,
                    bonusColdResistance = 15,
                    bonusMaxHealth = 5
                ),
                sourcesRu = listOf("Мастерская аванпоста", "Поиск в руинах"),
                usesRu = listOf("Экипировка в слот верхней одежды")
            ),
            WarehouseItem(
                id = "item_armor_composite",
                name = "Композитный бронекостюм «Страж»",
                category = ItemCategory.EQUIPMENT_AND_TOOLS,
                rarity = ItemRarity.RARE,
                description = "Тяжёлый бронежилет со вставками из титано-керамических пластин. Превосходно держит пули и осколки.",
                baseValueCredits = 350,
                iconKey = "armor",
                isEquipable = true,
                equipSlot = EquipmentSlotType.OUTFIT,
                weightKg = 6.0f,
                equipmentBonus = EquipmentBonus(
                    bonusDefense = 12,
                    bonusAttack = 4,
                    bonusMaxHealth = 20
                ),
                sourcesRu = listOf("Военный склад", "Оружейная лаборатория"),
                usesRu = listOf("Тяжёлая боевая защита")
            ),
            WarehouseItem(
                id = "item_suit_scavenger",
                name = "Комбинезон сборщика утиля",
                category = ItemCategory.EQUIPMENT_AND_TOOLS,
                rarity = ItemRarity.UNCOMMON,
                description = "Множество карманов, усиливающие накладки на коленях и локтях, встроенный пояс для подвески инструментов.",
                baseValueCredits = 180,
                iconKey = "suit",
                isEquipable = true,
                equipSlot = EquipmentSlotType.OUTFIT,
                weightKg = 3.0f,
                equipmentBonus = EquipmentBonus(
                    bonusDefense = 3,
                    bonusScavenging = 8,
                    bonusEngineering = 4,
                    bonusCarryCapacityKg = 5
                ),
                sourcesRu = listOf("Мастерская утиля", "Схроны мусорщиков"),
                usesRu = listOf("Снаряжение для экспедиций за лутом")
            ),
            WarehouseItem(
                id = "item_jacket_reinforced",
                name = "Кожаная куртка с кевларом",
                category = ItemCategory.EQUIPMENT_AND_TOOLS,
                rarity = ItemRarity.UNCOMMON,
                description = "Прочная куртка из дублёной кожи с подшитыми кевларовыми щитками на груди и плечах.",
                baseValueCredits = 210,
                iconKey = "jacket",
                isEquipable = true,
                equipSlot = EquipmentSlotType.OUTFIT,
                weightKg = 3.5f,
                equipmentBonus = EquipmentBonus(
                    bonusDefense = 6,
                    bonusAttack = 2,
                    bonusMaxHealth = 10
                ),
                sourcesRu = listOf("Торговый форпост", "Мастерская"),
                usesRu = listOf("Универсальная защита")
            ),

            // --- FOOTWEAR (Обувь) ---
            WarehouseItem(
                id = "item_boots_trekking",
                name = "Походные треккинговые ботинки",
                category = ItemCategory.EQUIPMENT_AND_TOOLS,
                rarity = ItemRarity.COMMON,
                description = "Удобные ботинки с виброгасящей подошвой. Снижают утомляемость в долгих пеших переходах.",
                baseValueCredits = 90,
                iconKey = "boots",
                isEquipable = true,
                equipSlot = EquipmentSlotType.FOOTWEAR,
                weightKg = 1.2f,
                equipmentBonus = EquipmentBonus(
                    bonusDefense = 2,
                    bonusSpeedPercent = 15,
                    bonusMoraleDrainReduction = 10
                ),
                sourcesRu = listOf("Склад базы", "Торговцы"),
                usesRu = listOf("Пешие переходы")
            ),
            WarehouseItem(
                id = "item_boots_armored",
                name = "Усиленные тактические берцы",
                category = ItemCategory.EQUIPMENT_AND_TOOLS,
                rarity = ItemRarity.RARE,
                description = "Армейские берцы со стальными носками и непрокалываемой кевларовой стелькой.",
                baseValueCredits = 220,
                iconKey = "boots",
                isEquipable = true,
                equipSlot = EquipmentSlotType.FOOTWEAR,
                weightKg = 2.0f,
                equipmentBonus = EquipmentBonus(
                    bonusDefense = 5,
                    bonusAttack = 3,
                    bonusMaxHealth = 10
                ),
                sourcesRu = listOf("Военный бункер", "Мастерская"),
                usesRu = listOf("Штурмовая экипировка")
            ),
            WarehouseItem(
                id = "item_boots_scout",
                name = "Бесшумные сапоги следопыта",
                category = ItemCategory.EQUIPMENT_AND_TOOLS,
                rarity = ItemRarity.UNCOMMON,
                description = "Мягкая прорезиненная подошва и бесшумный шаг позволяют незаметно проникать в опасные зоны.",
                baseValueCredits = 160,
                iconKey = "boots",
                isEquipable = true,
                equipSlot = EquipmentSlotType.FOOTWEAR,
                weightKg = 1.0f,
                equipmentBonus = EquipmentBonus(
                    bonusDefense = 2,
                    bonusScavenging = 5,
                    bonusSpeedPercent = 25
                ),
                sourcesRu = listOf("Разведывательные рейды"),
                usesRu = listOf("Разведка и бесшумное перемещение")
            ),

            // --- BACKPACKS (Рюкзаки / Вместимость) ---
            WarehouseItem(
                id = "item_backpack_simple",
                name = "Простой брезентовый рюкзак",
                category = ItemCategory.EQUIPMENT_AND_TOOLS,
                rarity = ItemRarity.COMMON,
                description = "Неприхотливый холщовый рюкзак на 20 литров. Базовый вариант для коротких вылазок.",
                baseValueCredits = 70,
                iconKey = "backpack",
                isEquipable = true,
                equipSlot = EquipmentSlotType.BACKPACK,
                weightKg = 1.5f,
                equipmentBonus = EquipmentBonus(
                    bonusCarryCapacityKg = 12,
                    bonusScavenging = 2
                ),
                sourcesRu = listOf("Склад аванпоста", "Крафт в мастерской"),
                usesRu = listOf("Увеличение переносимого веса (+12 кг)")
            ),
            WarehouseItem(
                id = "item_backpack_medium",
                name = "Походный каркасный рюкзак",
                category = ItemCategory.EQUIPMENT_AND_TOOLS,
                rarity = ItemRarity.UNCOMMON,
                description = "Анатомический рюкзак с алюминиевыми латами и поясным ремнём, правильно распределяющим нагрузку.",
                baseValueCredits = 150,
                iconKey = "backpack",
                isEquipable = true,
                equipSlot = EquipmentSlotType.BACKPACK,
                weightKg = 2.2f,
                equipmentBonus = EquipmentBonus(
                    bonusCarryCapacityKg = 20,
                    bonusScavenging = 4,
                    bonusSpeedPercent = 10
                ),
                sourcesRu = listOf("Торговый форпост", "Мастерская"),
                usesRu = listOf("Увеличение переносимого веса (+20 кг)")
            ),
            WarehouseItem(
                id = "item_backpack_tactical",
                name = "Тактический экспедиционный ранец",
                category = ItemCategory.EQUIPMENT_AND_TOOLS,
                rarity = ItemRarity.RARE,
                description = "Модульный ранец системы MOLLE с влагозащитными отсеками и быстрым сбросом.",
                baseValueCredits = 280,
                iconKey = "backpack",
                isEquipable = true,
                equipSlot = EquipmentSlotType.BACKPACK,
                weightKg = 2.8f,
                equipmentBonus = EquipmentBonus(
                    bonusCarryCapacityKg = 28,
                    bonusScavenging = 6,
                    bonusDefense = 2
                ),
                sourcesRu = listOf("Рейды в руины", "Мастерская ур. 2"),
                usesRu = listOf("Увеличение переносимого веса (+28 кг)")
            ),
            WarehouseItem(
                id = "item_backpack_military",
                name = "Армейский штурмовой баул",
                category = ItemCategory.EQUIPMENT_AND_TOOLS,
                rarity = ItemRarity.EPIC,
                description = "Сверхпрочный армейский баул из кордуры с кевларовым каркасом для массивного груза и трофеев.",
                baseValueCredits = 460,
                iconKey = "backpack",
                isEquipable = true,
                equipSlot = EquipmentSlotType.BACKPACK,
                weightKg = 4.0f,
                equipmentBonus = EquipmentBonus(
                    bonusCarryCapacityKg = 45,
                    bonusDefense = 4,
                    bonusMaxHealth = 15
                ),
                sourcesRu = listOf("Бункер «Объект 42»", "Оружейная лаборатория"),
                usesRu = listOf("Максимальная грузоподъёмность (+45 кг)")
            ),

            // --- TOOLS (Инструменты) ---
            WarehouseItem(
                id = "item_tool_multitool",
                name = "Универсальный полевой мультитул",
                category = ItemCategory.EQUIPMENT_AND_TOOLS,
                rarity = ItemRarity.COMMON,
                description = "Компактный складной инструмент со сменными битами, пассатижами, пилой и кусачками.",
                baseValueCredits = 110,
                iconKey = "tool",
                isEquipable = true,
                equipSlot = EquipmentSlotType.TOOL,
                weightKg = 0.8f,
                equipmentBonus = EquipmentBonus(
                    bonusEngineering = 7,
                    bonusScavenging = 3
                ),
                sourcesRu = listOf("Мастерская", "Станция Северный разъезд"),
                usesRu = listOf("Быстрый ремонт и демонтаж")
            ),
            WarehouseItem(
                id = "item_tool_repairkit",
                name = "Набор инженера-механика",
                category = ItemCategory.EQUIPMENT_AND_TOOLS,
                rarity = ItemRarity.RARE,
                description = "Профессиональный переносной чемодан с осциллографом, газовой горелкой и набором прецизионных ключей.",
                baseValueCredits = 290,
                iconKey = "tool",
                isEquipable = true,
                equipSlot = EquipmentSlotType.TOOL,
                weightKg = 2.5f,
                equipmentBonus = EquipmentBonus(
                    bonusEngineering = 15,
                    bonusDefense = 3,
                    bonusScavenging = 3
                ),
                sourcesRu = listOf("Мастерская утиля", "Исследовательский центр"),
                usesRu = listOf("Капитальный ремонт транспорта и станков")
            ),
            WarehouseItem(
                id = "item_tool_scanner",
                name = "Портативный сканер руин",
                category = ItemCategory.EQUIPMENT_AND_TOOLS,
                rarity = ItemRarity.RARE,
                description = "Георадарный прибор с дисплеем для обнаружения скрытых металлических конструкций и контейнеров под завалами.",
                baseValueCredits = 320,
                iconKey = "tool",
                isEquipable = true,
                equipSlot = EquipmentSlotType.TOOL,
                weightKg = 1.2f,
                equipmentBonus = EquipmentBonus(
                    bonusScavenging = 14,
                    bonusEngineering = 4
                ),
                sourcesRu = listOf("Исследовательский центр", "Бункер"),
                usesRu = listOf("Поиск скрытых схронов и ценных компонентов")
            ),
            WarehouseItem(
                id = "item_tool_machete",
                name = "Тяжёлое экспедиционное мачете",
                category = ItemCategory.EQUIPMENT_AND_TOOLS,
                rarity = ItemRarity.UNCOMMON,
                description = "Закалённый тесак с зазубренным обухом. Отлично прорубает завалы и служит грозным оружием ближнего боя.",
                baseValueCredits = 140,
                iconKey = "machete",
                isEquipable = true,
                equipSlot = EquipmentSlotType.TOOL,
                weightKg = 1.1f,
                equipmentBonus = EquipmentBonus(
                    bonusAttack = 8,
                    bonusScavenging = 3
                ),
                sourcesRu = listOf("Мастерская", "Торговцы"),
                usesRu = listOf("Ближний бой и расчистка завалов")
            ),

            // --- SPECIAL (Специальное снаряжение) ---
            WarehouseItem(
                id = "item_spec_nvg",
                name = "Бинокль ночного видения",
                category = ItemCategory.EQUIPMENT_AND_TOOLS,
                rarity = ItemRarity.RARE,
                description = "Оптический прибор с ИК-подсветкой. Позволяет заблаговременно обнаруживать мутантов и ценности в темноте.",
                baseValueCredits = 340,
                iconKey = "nvg",
                isEquipable = true,
                equipSlot = EquipmentSlotType.SPECIAL,
                weightKg = 1.0f,
                equipmentBonus = EquipmentBonus(
                    bonusScavenging = 6,
                    bonusAttack = 4,
                    bonusSpeedPercent = 10
                ),
                sourcesRu = listOf("Военный склад", "Бункер"),
                usesRu = listOf("Ночная разведка и обнаружение засад")
            ),
            WarehouseItem(
                id = "item_spec_firstaid",
                name = "Полевой реанимационный комплект",
                category = ItemCategory.MEDICINE_AND_AID,
                rarity = ItemRarity.RARE,
                description = "Автоинъектор с гемостатиками, стимуляторами и стерильными повязками для экстренной помощи.",
                baseValueCredits = 280,
                iconKey = "medical",
                isEquipable = true,
                equipSlot = EquipmentSlotType.SPECIAL,
                weightKg = 1.5f,
                equipmentBonus = EquipmentBonus(
                    bonusMedical = 12,
                    bonusMaxHealth = 15,
                    bonusDefense = 2
                ),
                sourcesRu = listOf("Полевой медпункт", "Аптечный склад"),
                usesRu = listOf("Экстренное лечение и спасение тяжелораненых")
            ),
            WarehouseItem(
                id = "item_spec_radiocompass",
                name = "Геодезический радиокомпас",
                category = ItemCategory.EQUIPMENT_AND_TOOLS,
                rarity = ItemRarity.UNCOMMON,
                description = "Высокоточный навигационный прибор с защитой от электромагнитных аномалий пустоши.",
                baseValueCredits = 190,
                iconKey = "compass",
                isEquipable = true,
                equipSlot = EquipmentSlotType.SPECIAL,
                weightKg = 0.5f,
                equipmentBonus = EquipmentBonus(
                    bonusScavenging = 5,
                    bonusEngineering = 5,
                    bonusSpeedPercent = 15
                ),
                sourcesRu = listOf("Станция связи", "Торговый форпост"),
                usesRu = listOf("Навигация и ориентирование на карте")
            ),
            WarehouseItem(
                id = "item_spec_amulet",
                name = "Стальной жетон ветерана",
                category = ItemCategory.VALUABLES_AND_RELICS,
                rarity = ItemRarity.UNCOMMON,
                description = "Памятный жетон с гравировкой отряда. Поднимает боевой дух бойца и придаёт уверенность в бою.",
                baseValueCredits = 130,
                iconKey = "amulet",
                isEquipable = true,
                equipSlot = EquipmentSlotType.SPECIAL,
                weightKg = 0.2f,
                equipmentBonus = EquipmentBonus(
                    bonusAttack = 4,
                    bonusDefense = 4,
                    bonusMoraleDrainReduction = 15
                ),
                sourcesRu = listOf("Особая награда"),
                usesRu = listOf("Повышение боевой стойкости и морали")
            ),

            // --- General Crafting / Expedition Items in Warehouse ---
            WarehouseItem(
                id = "item_raw_components",
                name = "Микросхемы и радиодетали",
                category = ItemCategory.ELECTRONICS_AND_PARTS,
                quantity = 12,
                rarity = ItemRarity.COMMON,
                description = "Уцелевшие электронные платы, конденсаторы и проводка для сборки приборов и крафта.",
                baseValueCredits = 25,
                iconKey = "chip",
                sourcesRu = listOf("Разборка электроники", "Северный разъезд"),
                usesRu = listOf("Крафт продвинутого снаряжения и ремонт транспорта")
            ),
            WarehouseItem(
                id = "item_raw_alloys",
                name = "Титановые пластины",
                category = ItemCategory.CONSTRUCTION_MATERIALS,
                quantity = 6,
                rarity = ItemRarity.RARE,
                description = "Лёгкий и прочный конструкционный материал для бронирования техники и бронекостюмов.",
                baseValueCredits = 65,
                iconKey = "alloy",
                sourcesRu = listOf("Переплавка в мастерской", "Шахты"),
                usesRu = listOf("Крафт тяжелой брони и каркасов транспорта")
            )
        )
    }
}
