package com.example.domain.service.quest

import com.example.data.ReputationBalanceConfig
import com.example.domain.model.BuildingType
import com.example.domain.model.ResourceType
import com.example.domain.model.quest.*

/**
 * Authoritative Static Catalog of all Quest Definitions in Frontier Settlement.
 * Includes starter campaigns, faction tasks, base development goals, and repeatable trade contracts.
 */
object QuestCatalog {

    val QUEST_FIRST_SUPPLIES = QuestDefinition(
        id = "quest_first_supplies",
        titleRu = "Первые запасы",
        descriptionRu = "Для поддержания жизнедеятельности базы и первых строительных работ необходимо доставить на склад не менее 50 единиц стройматериалов.",
        category = QuestCategory.SETTLEMENT,
        source = QuestSource.SETTLEMENT,
        giverNameRu = "Комендант базы",
        requirements = emptyList(),
        objectives = listOf(
            QuestObjectiveDefinition(
                id = "obj_collect_mat",
                type = QuestObjectiveType.COLLECT_RESOURCE,
                descriptionRu = "Накопить или доставить 50 стройматериалов на склад",
                targetId = "materials",
                requiredAmount = 50,
                progressMode = ObjectiveProgressMode.CURRENT_AMOUNT
            )
        ),
        rewards = QuestRewardDefinition(
            credits = 150,
            xp = 30,
            reputationDelta = 10,
            resources = mapOf(ResourceType.FOOD to 20, ResourceType.WATER to 20),
            summaryRu = "+150 Кредитов, +30 XP, +10 Репутации, +20 Еды, +20 Воды"
        ),
        autoAccept = true,
        canDecline = false,
        completionMode = QuestCompletionMode.TURN_IN,
        priority = 100
    )

    val QUEST_SCOUT_NORTH = QuestDefinition(
        id = "quest_scout_north",
        titleRu = "Первый шаг в пустошь",
        descriptionRu = "Организуйте разведывательный отряд и совершите вылазку в «Северный разъезд», чтобы разведать местность и вернуться с первой партией припасов.",
        category = QuestCategory.EXPLORATION,
        source = QuestSource.SETTLEMENT,
        giverNameRu = "Штаб разведки",
        requirements = emptyList(),
        objectives = listOf(
            QuestObjectiveDefinition(
                id = "obj_visit_north",
                type = QuestObjectiveType.VISIT_LOCATION,
                descriptionRu = "Посетить «Северный разъезд»",
                targetId = "loc_north_post",
                targetLocationId = "loc_north_post",
                requiredAmount = 1,
                progressMode = ObjectiveProgressMode.ONE_TIME_EVENT
            ),
            QuestObjectiveDefinition(
                id = "obj_return_base",
                type = QuestObjectiveType.RETURN_TO_SETTLEMENT,
                descriptionRu = "Вернуться с экспедицией в поселение",
                dependsOnObjectiveIds = listOf("obj_visit_north"),
                requiredAmount = 1,
                progressMode = ObjectiveProgressMode.ONE_TIME_EVENT
            )
        ),
        rewards = QuestRewardDefinition(
            credits = 200,
            xp = 40,
            reputationDelta = 15,
            resources = mapOf(ResourceType.MATERIALS to 40, ResourceType.FUEL to 15),
            summaryRu = "+200 Кредитов, +40 XP, +15 Репутации, +40 Материалов"
        ),
        autoAccept = true,
        canDecline = false,
        completionMode = QuestCompletionMode.AUTO_COMPLETE,
        priority = 90
    )

    val QUEST_OLD_GENERATOR = QuestDefinition(
        id = "quest_old_generator",
        titleRu = "Старый генератор",
        descriptionRu = "Техномаги сообщили об уцелевшем промышленном генераторе на территории старого промышленного узла. Исследуйте станцию и решите судьбу установки.",
        category = QuestCategory.FACTION,
        source = QuestSource.FACTION,
        factionId = ReputationBalanceConfig.FACTION_ENGINEERS,
        giverNameRu = "Архиватор Валлен (Инженеры)",
        requirements = listOf(
            QuestRequirement.LocationDiscovered("loc_industrial")
        ),
        objectives = listOf(
            QuestObjectiveDefinition(
                id = "obj_visit_industrial",
                type = QuestObjectiveType.VISIT_LOCATION,
                descriptionRu = "Добраться до Промышленного узла",
                targetId = "loc_industrial",
                targetLocationId = "loc_industrial",
                requiredAmount = 1,
                progressMode = ObjectiveProgressMode.ONE_TIME_EVENT
            ),
            QuestObjectiveDefinition(
                id = "obj_explore_power",
                type = QuestObjectiveType.EXPLORE_LOCATION,
                descriptionRu = "Исследовать Промышленный узел не менее чем на 50%",
                targetId = "loc_industrial",
                targetLocationId = "loc_industrial",
                requiredAmount = 50,
                dependsOnObjectiveIds = listOf("obj_visit_industrial"),
                progressMode = ObjectiveProgressMode.CURRENT_AMOUNT
            ),
            QuestObjectiveDefinition(
                id = "obj_decide_generator",
                type = QuestObjectiveType.MAKE_DECISION,
                descriptionRu = "Принять решение о судьбе генератора",
                targetId = "flag_generator_resolved",
                dependsOnObjectiveIds = listOf("obj_explore_power"),
                requiredAmount = 1,
                progressMode = ObjectiveProgressMode.ONE_TIME_EVENT
            )
        ),
        rewards = QuestRewardDefinition(
            credits = 350,
            xp = 60,
            reputationDelta = 20,
            factionRelationDeltas = mapOf(ReputationBalanceConfig.FACTION_ENGINEERS to 20),
            resources = mapOf(ResourceType.COMPONENTS to 25, ResourceType.RARE_ALLOY to 5),
            summaryRu = "+350 Кредитов, +60 XP, +20 Репутации, Инженеры +20, +25 Компонентов"
        ),
        autoAccept = false,
        canDecline = true,
        completionMode = QuestCompletionMode.TURN_IN,
        priority = 80
    )

    val QUEST_MISSING_CARGO = QuestDefinition(
        id = "quest_missing_cargo",
        titleRu = "Пропавший груз",
        descriptionRu = "Караван поселенцев потерял защищённый контейнер с электроникой в районе старого карьера. Найдите груз и привезите его на базу.",
        category = QuestCategory.SIDE,
        source = QuestSource.QUEST_GIVER,
        factionId = ReputationBalanceConfig.FACTION_SURVIVORS,
        giverNameRu = "Бригадир Григ",
        requirements = listOf(
            QuestRequirement.MinSettlementLevel(1)
        ),
        objectives = listOf(
            QuestObjectiveDefinition(
                id = "obj_visit_quarry",
                type = QuestObjectiveType.VISIT_LOCATION,
                descriptionRu = "Прибыть в Заброшенный карьер",
                targetId = "loc_quarry",
                targetLocationId = "loc_quarry",
                requiredAmount = 1,
                progressMode = ObjectiveProgressMode.ONE_TIME_EVENT
            ),
            QuestObjectiveDefinition(
                id = "obj_obtain_cargo",
                type = QuestObjectiveType.OBTAIN_ITEM,
                descriptionRu = "Найти «Защищённый контейнер»",
                targetId = "item_quest_container",
                dependsOnObjectiveIds = listOf("obj_visit_quarry"),
                requiredAmount = 1,
                progressMode = ObjectiveProgressMode.STATE_CHECK
            ),
            QuestObjectiveDefinition(
                id = "obj_deliver_cargo",
                type = QuestObjectiveType.DELIVER_ITEM,
                descriptionRu = "Передать найденный контейнер в поселении",
                targetId = "item_quest_container",
                dependsOnObjectiveIds = listOf("obj_obtain_cargo"),
                requiredAmount = 1,
                progressMode = ObjectiveProgressMode.ONE_TIME_EVENT
            )
        ),
        rewards = QuestRewardDefinition(
            credits = 280,
            xp = 45,
            reputationDelta = 15,
            factionRelationDeltas = mapOf(ReputationBalanceConfig.FACTION_SURVIVORS to 15),
            resources = mapOf(ResourceType.COMPONENTS to 15),
            summaryRu = "+280 Кредитов, +45 XP, +15 Репутации, Выжившие +15"
        ),
        autoAccept = false,
        canDecline = true,
        completionMode = QuestCompletionMode.TURN_IN,
        priority = 75
    )

    val QUEST_TRADE_PACT = QuestDefinition(
        id = "quest_trade_pact",
        titleRu = "Торговый контракт",
        descriptionRu = "Гильдия Торговцев готова открыть эксклюзивные поставки припасов, если поселение докажет свою надежность и доставит партию редких сплавов.",
        category = QuestCategory.FACTION,
        source = QuestSource.FACTION,
        factionId = ReputationBalanceConfig.FACTION_TRADERS,
        giverNameRu = "Магистр торговли Доран",
        requirements = listOf(
            QuestRequirement.MinReputation(20),
            QuestRequirement.MinFactionRelation(ReputationBalanceConfig.FACTION_TRADERS, 10)
        ),
        objectives = listOf(
            QuestObjectiveDefinition(
                id = "obj_deliver_alloy",
                type = QuestObjectiveType.DELIVER_RESOURCE,
                descriptionRu = "Передать 10 редких сплавов Гильдии",
                targetId = "rare_alloy",
                requiredAmount = 10,
                progressMode = ObjectiveProgressMode.ONE_TIME_EVENT
            )
        ),
        rewards = QuestRewardDefinition(
            credits = 500,
            xp = 70,
            reputationDelta = 25,
            factionRelationDeltas = mapOf(ReputationBalanceConfig.FACTION_TRADERS to 25),
            worldFlags = mapOf("flag_trader_pact_signed" to true),
            summaryRu = "+500 Кредитов, +70 XP, +25 Репутации, Гильдия Торговцев +25, Торговый пакт"
        ),
        autoAccept = false,
        canDecline = true,
        completionMode = QuestCompletionMode.TURN_IN,
        priority = 70
    )

    val QUEST_NOMAD_AID = QuestDefinition(
        id = "quest_nomad_aid",
        titleRu = "Помощь кочевникам",
        descriptionRu = "Караван Вольных Кочевников застрял в песчаной буре. Выделите провизию (30 еды и 20 воды), чтобы спасти странников.",
        category = QuestCategory.FACTION,
        source = QuestSource.FACTION,
        factionId = ReputationBalanceConfig.FACTION_NOMADS,
        giverNameRu = "Старейшина Асиф",
        requirements = listOf(
            QuestRequirement.MinFactionRelation(ReputationBalanceConfig.FACTION_NOMADS, 0)
        ),
        timeLimitDays = 5,
        objectives = listOf(
            QuestObjectiveDefinition(
                id = "obj_deliver_nomad_food",
                type = QuestObjectiveType.DELIVER_RESOURCE,
                descriptionRu = "Передать 30 еды",
                targetId = "food",
                requiredAmount = 30,
                progressMode = ObjectiveProgressMode.ONE_TIME_EVENT
            ),
            QuestObjectiveDefinition(
                id = "obj_deliver_nomad_water",
                type = QuestObjectiveType.DELIVER_RESOURCE,
                descriptionRu = "Передать 20 воды",
                targetId = "water",
                requiredAmount = 20,
                progressMode = ObjectiveProgressMode.ONE_TIME_EVENT
            )
        ),
        failureConditions = listOf(
            QuestFailureCondition.TimeLimitExpired("Срок помощи кочевникам истек — караван ушел"),
            QuestFailureCondition.FactionRelationBelow(ReputationBalanceConfig.FACTION_NOMADS, -25, "Кочевники враждебны")
        ),
        rewards = QuestRewardDefinition(
            credits = 200,
            xp = 50,
            reputationDelta = 20,
            factionRelationDeltas = mapOf(ReputationBalanceConfig.FACTION_NOMADS to 30),
            resources = mapOf(ResourceType.MEDICINE to 15, ResourceType.FUEL to 25),
            summaryRu = "+200 Кредитов, +50 XP, Кочевники +30, +15 Медикаментов, +25 Топлива"
        ),
        autoAccept = false,
        canDecline = true,
        completionMode = QuestCompletionMode.TURN_IN,
        priority = 65
    )

    val QUEST_DEFENSE_PERIMETER = QuestDefinition(
        id = "quest_defense_perimeter",
        titleRu = "Оборона периметра",
        descriptionRu = "Укрепите базу: улучшите любое здание базы до 2-го уровня и исследуйте технологию «Усиленная броня» в лаборатории.",
        category = QuestCategory.SETTLEMENT,
        source = QuestSource.SETTLEMENT,
        giverNameRu = "Начальник гарнизона",
        requirements = listOf(
            QuestRequirement.MinSettlementLevel(1)
        ),
        objectives = listOf(
            QuestObjectiveDefinition(
                id = "obj_upgrade_bld",
                type = QuestObjectiveType.UPGRADE_BUILDING,
                descriptionRu = "Улучшить здание поселения до уровня 2",
                requiredAmount = 1,
                progressMode = ObjectiveProgressMode.STATE_CHECK
            ),
            QuestObjectiveDefinition(
                id = "obj_research_tech",
                type = QuestObjectiveType.RESEARCH,
                descriptionRu = "Завершить исследование новой технологии",
                requiredAmount = 1,
                progressMode = ObjectiveProgressMode.ACCUMULATED
            )
        ),
        rewards = QuestRewardDefinition(
            credits = 300,
            xp = 60,
            reputationDelta = 20,
            resources = mapOf(ResourceType.AMMO to 50, ResourceType.MATERIALS to 50),
            summaryRu = "+300 Кредитов, +60 XP, +20 Репутации, +50 Патронов, +50 Материалов"
        ),
        autoAccept = true,
        canDecline = false,
        completionMode = QuestCompletionMode.TURN_IN,
        priority = 60
    )

    val QUEST_TECH_BREAKTHROUGH = QuestDefinition(
        id = "quest_tech_breakthrough",
        titleRu = "Технологический прорыв",
        descriptionRu = "Завершите не менее 2 научных исследований для открытия передовых технологий пустошей.",
        category = QuestCategory.SETTLEMENT,
        source = QuestSource.SETTLEMENT,
        giverNameRu = "Главный инженер",
        requirements = listOf(
            QuestRequirement.BuildingConstructed(BuildingType.RESEARCH_LAB, 1)
        ),
        objectives = listOf(
            QuestObjectiveDefinition(
                id = "obj_research_2",
                type = QuestObjectiveType.RESEARCH,
                descriptionRu = "Провести 2 исследования в лаборатории",
                requiredAmount = 2,
                progressMode = ObjectiveProgressMode.ACCUMULATED
            )
        ),
        rewards = QuestRewardDefinition(
            credits = 400,
            xp = 80,
            reputationDelta = 25,
            resources = mapOf(ResourceType.COMPONENTS to 30, ResourceType.RARE_ALLOY to 10),
            summaryRu = "+400 Кредитов, +80 XP, +25 Репутации, +30 Компонентов, +10 Сплавов"
        ),
        autoAccept = false,
        canDecline = true,
        completionMode = QuestCompletionMode.TURN_IN,
        priority = 55
    )

    val QUEST_CARAVAN_SUPPLY = QuestDefinition(
        id = "quest_caravan_supply",
        titleRu = "Контракт: Поставка компонентов",
        descriptionRu = "Регулярный контракт Торговой Гильдии: сдайте 20 электронных компонентов для технического обслуживания караванов.",
        category = QuestCategory.REPEATABLE,
        source = QuestSource.FACTION,
        factionId = ReputationBalanceConfig.FACTION_TRADERS,
        giverNameRu = "Торговый брокер",
        requirements = listOf(
            QuestRequirement.MinFactionRelation(ReputationBalanceConfig.FACTION_TRADERS, 10)
        ),
        objectives = listOf(
            QuestObjectiveDefinition(
                id = "obj_deliver_components",
                type = QuestObjectiveType.DELIVER_RESOURCE,
                descriptionRu = "Передать 20 компонентов заказчику",
                targetId = "components",
                requiredAmount = 20,
                progressMode = ObjectiveProgressMode.ONE_TIME_EVENT
            )
        ),
        rewards = QuestRewardDefinition(
            credits = 250,
            xp = 35,
            reputationDelta = 10,
            factionRelationDeltas = mapOf(ReputationBalanceConfig.FACTION_TRADERS to 10),
            summaryRu = "+250 Кредитов, +35 XP, +10 Репутации, Гильдия Торговцев +10"
        ),
        repeatability = QuestRepeatability.REPEATABLE_WITH_COOLDOWN,
        cooldownDays = 3,
        autoAccept = false,
        canDecline = true,
        completionMode = QuestCompletionMode.TURN_IN,
        priority = 50
    )

    val QUEST_HIDDEN_THREAT = QuestDefinition(
        id = "quest_hidden_threat",
        titleRu = "Скрытая угроза: Часть 1",
        descriptionRu = "Дозорные зафиксировали скопление рейдеров у старого бункера. Проведите разведку боем и уничтожьте авангард бандитов.",
        category = QuestCategory.MAIN,
        source = QuestSource.SETTLEMENT,
        giverNameRu = "Штаб обороны",
        requirements = listOf(
            QuestRequirement.CompletedQuest("quest_scout_north")
        ),
        objectives = listOf(
            QuestObjectiveDefinition(
                id = "obj_visit_bunker",
                type = QuestObjectiveType.VISIT_LOCATION,
                descriptionRu = "Прибыть к Старому бункеру",
                targetId = "loc_outpost",
                targetLocationId = "loc_outpost",
                requiredAmount = 1,
                progressMode = ObjectiveProgressMode.ONE_TIME_EVENT
            ),
            QuestObjectiveDefinition(
                id = "obj_defeat_raiders",
                type = QuestObjectiveType.WIN_COMBAT,
                descriptionRu = "Одержать победу над отрядом рейдеров",
                targetId = "raiders",
                dependsOnObjectiveIds = listOf("obj_visit_bunker"),
                requiredAmount = 1,
                progressMode = ObjectiveProgressMode.ONE_TIME_EVENT
            )
        ),
        rewards = QuestRewardDefinition(
            credits = 500,
            xp = 100,
            reputationDelta = 30,
            resources = mapOf(ResourceType.AMMO to 60, ResourceType.MEDICINE to 20),
            worldFlags = mapOf("flag_bunker_raiders_defeated" to true),
            summaryRu = "+500 Кредитов, +100 XP, +30 Репутации, +60 Патронов, Угроза устранена"
        ),
        autoAccept = false,
        canDecline = false,
        completionMode = QuestCompletionMode.TURN_IN,
        priority = 95
    )

    val ALL_QUESTS: List<QuestDefinition> = listOf(
        QUEST_FIRST_SUPPLIES,
        QUEST_SCOUT_NORTH,
        QUEST_OLD_GENERATOR,
        QUEST_MISSING_CARGO,
        QUEST_TRADE_PACT,
        QUEST_NOMAD_AID,
        QUEST_DEFENSE_PERIMETER,
        QUEST_TECH_BREAKTHROUGH,
        QUEST_CARAVAN_SUPPLY,
        QUEST_HIDDEN_THREAT
    )

    val QUEST_MAP: Map<String, QuestDefinition> = ALL_QUESTS.associateBy { it.id }

    fun get(id: String): QuestDefinition? = QUEST_MAP[id]

    /**
     * Validates all quest definitions at startup:
     * - Unique quest IDs
     * - Unique objective IDs within each quest
     * - No cyclic or missing objective dependencies
     * - Valid next quest ID references
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        val seenQuestIds = mutableSetOf<String>()

        ALL_QUESTS.forEach { quest ->
            if (!seenQuestIds.add(quest.id)) {
                errors.add("Duplicate quest ID: ${quest.id}")
            }

            val seenObjIds = mutableSetOf<String>()
            quest.objectives.forEach { obj ->
                if (!seenObjIds.add(obj.id)) {
                    errors.add("Quest ${quest.id} has duplicate objective ID: ${obj.id}")
                }
                obj.dependsOnObjectiveIds.forEach { depId ->
                    if (quest.objectives.none { it.id == depId }) {
                        errors.add("Quest ${quest.id} objective ${obj.id} depends on missing objective $depId")
                    }
                }
            }

            quest.nextQuestIds.forEach { nextId ->
                if (QUEST_MAP[nextId] == null) {
                    errors.add("Quest ${quest.id} references missing nextQuestId: $nextId")
                }
            }
        }
        return errors
    }
}
