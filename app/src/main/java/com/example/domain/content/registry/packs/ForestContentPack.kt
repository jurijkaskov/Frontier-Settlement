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
 * Forest & Wilderness content pack featuring natural biomes, abandoned farms,
 * wildlife threats, foraging loot, and food gathering contracts.
 */
object ForestContentPack : ContentPack {
    override val packId: String = "pack_forest"
    override val packTitleRu: String = "Диколесье и угодья"
    override val version: Int = 1

    override val locationTemplates: List<LocationTemplate> = listOf(
        LocationTemplate(
            id = "loc_tmpl_forest_farm",
            type = LocationType.FARM,
            namePrefixList = listOf("Старая ферма", "Агрокомплекс", "Элеватор", "Угодья"),
            nameBaseList = listOf("«Заря»", "«Колос»", "«Зелёный ключ»", "«Дубрава»", "«Роща»"),
            nameSuffixList = listOf("у ручья", "в низине", "на опушке", "северная"),
            descriptionTemplates = listOf(
                "Заброшенный агрокомплекс с уцелевшими теплицами и амбарами.",
                "Сельскохозяйственные угодья, заросшие кустарником. Сохранились запасы зерна и колодец."
            ),
            allowedTerrains = setOf(TerrainType.WATER, TerrainType.WASTELAND),
            minDangerLevel = DangerLevel.SAFE,
            maxDangerLevel = DangerLevel.MODERATE,
            mandatoryAreas = listOf(
                LocalAreaTemplate("area_greenhouse", "Гидропонные теплицы", "Агросектор", isMandatory = true, lootTableId = "loot_farm_provisions"),
                LocalAreaTemplate("area_barn", "Зернохранилище и сараи", "Склад урожая", isMandatory = true, lootTableId = "loot_farm_provisions")
            ),
            optionalAreaPool = listOf(
                LocalAreaTemplate("area_windmill", "Ветряная насосная станция", "Гидроузел", weight = 1.0f, lootTableId = "loot_general_supplies"),
                LocalAreaTemplate("area_orchard", "Одичавший яблоневый сад", "Сбор плодов", weight = 0.8f, lootTableId = "loot_farm_provisions"),
                LocalAreaTemplate("area_cellar", "Подвальный погреб", "Схрон провизии", weight = 0.5f, lootTableId = "loot_scavenger_stash")
            ),
            potentialLootKeywordsRu = listOf("Зерно", "Питьевая вода", "Удобрения", "Медицинские травы"),
            visualAssetPool = listOf("loc_farm"),
            tags = setOf(ContentTag.FOREST, ContentTag.NATURE, ContentTag.SURVIVAL),
            baseWeight = 110f
        ),
        LocationTemplate(
            id = "loc_tmpl_deep_forest",
            type = LocationType.FOREST,
            namePrefixList = listOf("Густой лес", "Лесничество", "Урочище", "Охотничья заимка"),
            nameBaseList = listOf("«Чёрный бор»", "«Сосновый бор»", "«Волчий яр»", "«Лесной кордон»"),
            nameSuffixList = listOf("северного кряжа", "у тихой заводи", "древней чащи"),
            descriptionTemplates = listOf(
                "Дремучий хвойный массив, скрывающий старые охотничьи схроны и чистые родники.",
                "Лесной сектор с высокой кроной. Отличное место для охоты и заготовки древесины."
            ),
            allowedTerrains = setOf(TerrainType.WATER, TerrainType.HILLS),
            minDangerLevel = DangerLevel.LOW,
            maxDangerLevel = DangerLevel.HIGH,
            mandatoryAreas = listOf(
                LocalAreaTemplate("area_spring", "Чистый лесной родник", "Источник воды", isMandatory = true),
                LocalAreaTemplate("area_cabin", "Хижина егеря", "Укрытие", isMandatory = true, lootTableId = "loot_farm_provisions")
            ),
            optionalAreaPool = listOf(
                LocalAreaTemplate("area_game_trail", "Охотничьи тропы", "Охотничья зона", weight = 1.2f),
                LocalAreaTemplate("area_herbal_glade", "Поляна целебных трав", "Сбор трав", weight = 0.9f, lootTableId = "loot_medical_herbs")
            ),
            potentialLootKeywordsRu = listOf("Дичь", "Древесина", "Травы", "Чистая вода"),
            visualAssetPool = listOf("loc_forest"),
            tags = setOf(ContentTag.FOREST, ContentTag.NATURE, ContentTag.COMMON),
            baseWeight = 95f
        )
    )

    override val enemyTemplates: List<EnemyTemplate> = listOf(
        EnemyTemplate(
            id = "enemy_forest_wolf",
            nameRu = "Мутировавший вожак стаи",
            descriptionRu = "Крупный хищник пустоши с острым обонянием и крепкими челюстями.",
            avatarTag = "enemy_beast",
            role = CharacterRole.SCOUT,
            aiProfileId = "ai_aggressive",
            baseHp = 48,
            baseAttack = 15,
            baseDefense = 4,
            baseInitiative = 15,
            dangerTier = DangerLevel.LOW,
            tags = setOf(ContentTag.NATURE, ContentTag.FOREST)
        ),
        EnemyTemplate(
            id = "enemy_poacher",
            nameRu = "Одичалый браконьер",
            descriptionRu = "Охотник-одиночка с капканами и самодельным арбалетом.",
            avatarTag = "enemy_raider",
            role = CharacterRole.SCOUT,
            aiProfileId = "ai_opportunist",
            baseHp = 45,
            baseAttack = 13,
            baseDefense = 5,
            baseInitiative = 13,
            dangerTier = DangerLevel.LOW,
            tags = setOf(ContentTag.FOREST, ContentTag.SURVIVAL)
        )
    )

    override val encounterTemplates: List<EncounterTemplate> = listOf(
        EncounterTemplate(
            id = "enc_wild_pack",
            titleRu = "Стая одичавших волков",
            descriptionRu = "Хищники окружили группу на лесной тропе.",
            allowedLocationTypes = setOf(LocationType.FARM, LocationType.FOREST, LocationType.VILLAGE),
            minDangerLevel = DangerLevel.LOW,
            maxDangerLevel = DangerLevel.HIGH,
            enemyPool = listOf(
                EnemyPoolEntry("enemy_forest_wolf", weight = 2.0f, minCount = 1, maxCount = 3),
                EnemyPoolEntry("enemy_poacher", weight = 0.8f, minCount = 0, maxCount = 1)
            ),
            minEnemies = 1,
            maxEnemies = 3,
            lootTableId = "loot_farm_provisions",
            baseRewardXp = 85,
            tags = setOf(ContentTag.NATURE, ContentTag.FOREST),
            baseWeight = 110f
        )
    )

    override val lootTables: List<LootTableDefinition> = listOf(
        LootTableDefinition(
            id = "loot_farm_provisions",
            titleRu = "Запасы агрокомплекса",
            minCredits = 20,
            maxCredits = 70,
            resourceEntries = listOf(
                LootResourceEntry(ResourceType.FOOD, 25, 60, dropChance = 1.0f),
                LootResourceEntry(ResourceType.WATER, 20, 50, dropChance = 0.9f),
                LootResourceEntry(ResourceType.MATERIALS, 10, 25, dropChance = 0.6f)
            ),
            itemEntries = listOf(
                LootItemEntry("item_rations_canned", weight = 1.2f, minCount = 1, maxCount = 2, dropChance = 0.6f),
                LootItemEntry("item_filter_water", weight = 0.8f, dropChance = 0.3f)
            ),
            tags = setOf(ContentTag.FOREST, ContentTag.SURVIVAL)
        ),
        LootTableDefinition(
            id = "loot_medical_herbs",
            titleRu = "Сбор целебных трав и реагентов",
            minCredits = 30,
            maxCredits = 90,
            resourceEntries = listOf(
                LootResourceEntry(ResourceType.MEDICINE, 6, 18, dropChance = 1.0f),
                LootResourceEntry(ResourceType.WATER, 15, 30, dropChance = 0.7f)
            ),
            itemEntries = listOf(
                LootItemEntry("item_spec_firstaid", weight = 0.7f, dropChance = 0.3f),
                LootItemEntry("item_medkit_small", weight = 1.0f, dropChance = 0.5f)
            ),
            tags = setOf(ContentTag.MEDICINE, ContentTag.NATURE)
        )
    )

    override val repeatableQuestTemplates: List<RepeatableQuestTemplate> = listOf(
        RepeatableQuestTemplate(
            id = "rep_contract_harvest_food",
            titleTemplateRu = "Заготовка провизии: %s",
            descriptionTemplateRu = "Поселению требуется пополнить запасы свежей пищи (%d ед.) перед сменой сезона.",
            category = QuestCategory.REPEATABLE,
            source = QuestSource.SETTLEMENT,
            giverNameRu = "Агроном аванпоста",
            objectiveType = QuestObjectiveType.COLLECT_RESOURCE,
            targetResourcePool = listOf(ResourceType.FOOD),
            minRequiredAmount = 25,
            maxRequiredAmount = 55,
            baseRewardCredits = 110,
            baseRewardXp = 30,
            baseReputationReward = 5,
            cooldownDays = 2,
            tags = setOf(ContentTag.FOREST, ContentTag.SURVIVAL)
        )
    )
}
