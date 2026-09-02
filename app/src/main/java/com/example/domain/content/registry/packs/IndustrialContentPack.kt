package com.example.domain.content.registry.packs

import com.example.domain.content.core.ContentTag
import com.example.domain.content.encounter.EncounterTemplate
import com.example.domain.content.encounter.EnemyPoolEntry
import com.example.domain.content.encounter.EnemyTemplate
import com.example.domain.content.location.LocalAreaTemplate
import com.example.domain.content.location.LocationTemplate
import com.example.domain.content.loot.LootItemEntry
import com.example.domain.content.loot.LootResourceEntry
import com.example.domain.content.loot.LootTableDefinition
import com.example.domain.content.quest.RepeatableQuestTemplate
import com.example.domain.content.registry.ContentPack
import com.example.domain.model.*
import com.example.domain.model.quest.QuestCategory
import com.example.domain.model.quest.QuestObjectiveType
import com.example.domain.model.quest.QuestSource

/**
 * Industrial & Tech content pack featuring factories, warehouses, heavy scrap components,
 * mechanical threats, and high-tier engineering salvage.
 */
object IndustrialContentPack : ContentPack {
    override val packId: String = "pack_industrial"
    override val packTitleRu: String = "Промзона и фабрики"
    override val version: Int = 1

    override val locationTemplates: List<LocationTemplate> = listOf(
        LocationTemplate(
            id = "loc_tmpl_industrial_plant",
            type = LocationType.INDUSTRIAL_PLANT,
            namePrefixList = listOf("Завод", "Промкомбинат", "Цех", "Литейный комплекс"),
            nameBaseList = listOf("«Вектор-М»", "«Титан-Пром»", "«Прогресс»", "«Сталь-Сервис»"),
            nameSuffixList = listOf("северного узла", "тяжёлого машиностроения", "промзоны 9"),
            descriptionTemplates = listOf(
                "Огромный полуразрушенный заводской комплекс со станками с ЧПУ и складами заготовок.",
                "Промышленный гигант с законсервированными сборочными линиями и генераторными цехами."
            ),
            allowedTerrains = setOf(TerrainType.RUINS, TerrainType.WASTELAND),
            minDangerLevel = DangerLevel.MODERATE,
            maxDangerLevel = DangerLevel.EXTREME,
            mandatoryAreas = listOf(
                LocalAreaTemplate("area_assembly_hall", "Главный сборочный цех", "Производственный зал", isMandatory = true, lootTableId = "loot_industrial_tech"),
                LocalAreaTemplate("area_foundry", "Литейный цех и склады металла", "Склад металлопроката", isMandatory = true, lootTableId = "loot_heavy_scrap")
            ),
            optionalAreaPool = listOf(
                LocalAreaTemplate("area_transformer", "Главная подстанция", "Энергоблок", weight = 1.0f, lootTableId = "loot_industrial_tech"),
                LocalAreaTemplate("area_tooling", "Инструментальный склад", "Склад спецоснастки", weight = 0.8f, lootTableId = "loot_heavy_scrap"),
                LocalAreaTemplate("area_chemical_lab", "Контрольная лаборатория", "Лабораторный бокс", weight = 0.5f, lootTableId = "loot_military_crate")
            ),
            potentialLootKeywordsRu = listOf("Редкие сплавы", "Микросхемы", "Инструменты", "Топливо"),
            visualAssetPool = listOf("loc_factory"),
            tags = setOf(ContentTag.INDUSTRIAL, ContentTag.TECHNICAL, ContentTag.HIGH_DANGER),
            baseWeight = 90f
        ),
        LocationTemplate(
            id = "loc_tmpl_fuel_depot",
            type = LocationType.WAREHOUSE_COMPLEX,
            namePrefixList = listOf("Нефтебаза", "Топливный терминал", "Хранилище ГСМ", "Автобаза"),
            nameBaseList = listOf("«Нефть-Холдинг»", "«Резерв-7»", "«Магистраль-Ойл»"),
            nameSuffixList = listOf("резервного фонда", "транспортного узла"),
            descriptionTemplates = listOf(
                "Узел хранения горюче-смазочных материалов с массивными цилиндрическими резервуарами.",
                "Автозаправочный комплекс с подземными танками и насосной станцией."
            ),
            allowedTerrains = setOf(TerrainType.WASTELAND, TerrainType.RUINS),
            minDangerLevel = DangerLevel.MODERATE,
            maxDangerLevel = DangerLevel.HIGH,
            mandatoryAreas = listOf(
                LocalAreaTemplate("area_pump_station", "Насосная станция перекачки", "Технический блок", isMandatory = true, lootTableId = "loot_fuel_cache"),
                LocalAreaTemplate("area_tanks", "Резервуарный парк", "Хранилище цистерн", isMandatory = true, lootTableId = "loot_fuel_cache")
            ),
            optionalAreaPool = listOf(
                LocalAreaTemplate("area_garage", "Ремонтные боксы бензовозов", "Гаражный ангар", weight = 1.0f, lootTableId = "loot_heavy_scrap")
            ),
            potentialLootKeywordsRu = listOf("Топливо", "Фильтры", "Масла", "Металл"),
            visualAssetPool = listOf("loc_station"),
            tags = setOf(ContentTag.INDUSTRIAL, ContentTag.TECHNICAL),
            baseWeight = 85f
        )
    )

    override val enemyTemplates: List<EnemyTemplate> = listOf(
        EnemyTemplate(
            id = "enemy_rogue_engineer",
            nameRu = "Отверженный техник",
            descriptionRu = "Бывший заводской инженер, вооружённый электродуговой сваркой и гранатами.",
            avatarTag = "engineer",
            role = CharacterRole.ENGINEER,
            aiProfileId = "ai_cautious",
            baseHp = 55,
            baseAttack = 14,
            baseDefense = 8,
            baseInitiative = 10,
            dangerTier = DangerLevel.MODERATE,
            tags = setOf(ContentTag.TECHNICAL, ContentTag.INDUSTRIAL)
        ),
        EnemyTemplate(
            id = "enemy_scrap_golem",
            nameRu = "Сварочный дрон-охранник",
            descriptionRu = "Кустарно перепрограммированный промышленный робот с бронеплитами.",
            avatarTag = "enemy_guard",
            role = CharacterRole.SOLDIER,
            aiProfileId = "ai_aggressive",
            baseHp = 80,
            baseAttack = 16,
            baseDefense = 12,
            baseInitiative = 7,
            dangerTier = DangerLevel.HIGH,
            tags = setOf(ContentTag.TECHNICAL, ContentTag.MILITARY)
        )
    )

    override val encounterTemplates: List<EncounterTemplate> = listOf(
        EncounterTemplate(
            id = "enc_factory_security",
            titleRu = "Охрана промышленного комплекса",
            descriptionRu = "Отряд мародёров-техников и защитный дрон блокируют вход в производственный цех.",
            allowedLocationTypes = setOf(LocationType.INDUSTRIAL_PLANT, LocationType.WAREHOUSE_COMPLEX, LocationType.ANOMALY_ZONE),
            minDangerLevel = DangerLevel.MODERATE,
            maxDangerLevel = DangerLevel.EXTREME,
            enemyPool = listOf(
                EnemyPoolEntry("enemy_rogue_engineer", weight = 1.5f, minCount = 1, maxCount = 2),
                EnemyPoolEntry("enemy_scrap_golem", weight = 1.0f, minCount = 0, maxCount = 1),
                EnemyPoolEntry("enemy_shield_guard", weight = 1.0f, minCount = 0, maxCount = 1)
            ),
            minEnemies = 2,
            maxEnemies = 3,
            lootTableId = "loot_industrial_tech",
            baseRewardXp = 130,
            tags = setOf(ContentTag.INDUSTRIAL, ContentTag.TECHNICAL),
            baseWeight = 85f
        )
    )

    override val lootTables: List<LootTableDefinition> = listOf(
        LootTableDefinition(
            id = "loot_industrial_tech",
            titleRu = "Контейнер с электроникой и деталями",
            minCredits = 60,
            maxCredits = 180,
            resourceEntries = listOf(
                LootResourceEntry(ResourceType.COMPONENTS, 8, 20, dropChance = 1.0f),
                LootResourceEntry(ResourceType.RARE_ALLOY, 3, 8, dropChance = 0.7f),
                LootResourceEntry(ResourceType.MATERIALS, 25, 60, dropChance = 0.8f)
            ),
            itemEntries = listOf(
                LootItemEntry("item_tool_scanner", weight = 1.0f, dropChance = 0.3f),
                LootItemEntry("item_tool_multitool", weight = 1.2f, dropChance = 0.4f)
            ),
            tags = setOf(ContentTag.INDUSTRIAL, ContentTag.TECHNICAL)
        ),
        LootTableDefinition(
            id = "loot_heavy_scrap",
            titleRu = "Партия тяжёлого металлопроката",
            minCredits = 40,
            maxCredits = 120,
            resourceEntries = listOf(
                LootResourceEntry(ResourceType.MATERIALS, 50, 110, dropChance = 1.0f),
                LootResourceEntry(ResourceType.COMPONENTS, 4, 10, dropChance = 0.5f)
            ),
            tags = setOf(ContentTag.INDUSTRIAL, ContentTag.SCAVENGING)
        ),
        LootTableDefinition(
            id = "loot_fuel_cache",
            titleRu = "Цистерна очищенного топлива",
            minCredits = 50,
            maxCredits = 130,
            resourceEntries = listOf(
                LootResourceEntry(ResourceType.FUEL, 35, 80, dropChance = 1.0f),
                LootResourceEntry(ResourceType.MATERIALS, 15, 35, dropChance = 0.6f)
            ),
            tags = setOf(ContentTag.INDUSTRIAL, ContentTag.SURVIVAL)
        )
    )

    override val repeatableQuestTemplates: List<RepeatableQuestTemplate> = listOf(
        RepeatableQuestTemplate(
            id = "rep_contract_fuel_delivery",
            titleTemplateRu = "Пополнение запасов ГСМ: %s",
            descriptionTemplateRu = "Генераторной станции аванпоста требуется доставка партии топлива (%d ед.) для обеспечения бесперебойного энергоснабжения.",
            category = QuestCategory.REPEATABLE,
            source = QuestSource.SETTLEMENT,
            giverNameRu = "Главный энергетик",
            objectiveType = QuestObjectiveType.COLLECT_RESOURCE,
            targetResourcePool = listOf(ResourceType.FUEL),
            minRequiredAmount = 20,
            maxRequiredAmount = 45,
            baseRewardCredits = 150,
            baseRewardXp = 40,
            baseReputationReward = 7,
            cooldownDays = 3,
            tags = setOf(ContentTag.INDUSTRIAL, ContentTag.TECHNICAL)
        )
    )
}
