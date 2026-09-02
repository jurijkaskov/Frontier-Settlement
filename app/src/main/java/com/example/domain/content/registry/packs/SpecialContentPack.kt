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
 * Special & Anomaly content pack featuring underground research bunkers,
 * high-altitude communication arrays, rare relics, and anomalous threats.
 */
object SpecialContentPack : ContentPack {
    override val packId: String = "pack_special"
    override val packTitleRu: String = "Спецобъекты и реликвии"
    override val version: Int = 1

    override val locationTemplates: List<LocationTemplate> = listOf(
        LocationTemplate(
            id = "loc_tmpl_bunker_vault",
            type = LocationType.MILITARY_BUNKER,
            namePrefixList = listOf("Бункер", "Спецобъект", "Лабораторный комплекс", "Сектор НИИ"),
            nameBaseList = listOf("«Омега-13»", "«Прометей»", "«Гелиос»", "«Сфера»"),
            nameSuffixList = listOf("глубокого заложения", "закрытого типа", "секретного архива"),
            descriptionTemplates = listOf(
                "Герметичный подземный комплекс с автономной системой жизнеобеспечения и архивами технологий.",
                "Секретный научно-исследовательский бункер с защитными бронешлюзами и серверными залами."
            ),
            allowedTerrains = setOf(TerrainType.HILLS, TerrainType.RUINS),
            minDangerLevel = DangerLevel.MODERATE,
            maxDangerLevel = DangerLevel.EXTREME,
            mandatoryAreas = listOf(
                LocalAreaTemplate("area_airlock", "Входной бронешлюз", "Защитный периметр", isMandatory = true),
                LocalAreaTemplate("area_server_room", "Серверный зал архива", "Архив данных", isMandatory = true, lootTableId = "loot_relic_vault")
            ),
            optionalAreaPool = listOf(
                LocalAreaTemplate("area_cryo", "Криогенная камера", "Спецхранилище", weight = 0.8f, lootTableId = "loot_relic_vault"),
                LocalAreaTemplate("area_power_core", "Реакторный отсек", "Энергоблок", weight = 1.0f, lootTableId = "loot_industrial_tech")
            ),
            potentialLootKeywordsRu = listOf("Довоенные реликвии", "Технологии", "Микросхемы", "Сплавы"),
            visualAssetPool = listOf("loc_lab"),
            tags = setOf(ContentTag.UNDERGROUND, ContentTag.TECHNICAL, ContentTag.ANOMALY, ContentTag.RARE),
            baseWeight = 60f,
            isUnique = false
        ),
        LocationTemplate(
            id = "loc_tmpl_comm_array",
            type = LocationType.ANOMALY_ZONE,
            namePrefixList = listOf("Радиомачта", "Ретранслятор", "Узел дальней связи", "Радиолокатор"),
            nameBaseList = listOf("«Горизонт»", "«Эфир»", "«Зенит»", "«Сигнал»"),
            nameSuffixList = listOf("высотного обзора", "дальнего перехвата"),
            descriptionTemplates = listOf(
                "Высокая стальная мачта с уцелевшими антенными решетками и аппаратной комнатой.",
                "Станция загоризонтного слежения на господствующей высоте."
            ),
            allowedTerrains = setOf(TerrainType.HILLS, TerrainType.WASTELAND),
            minDangerLevel = DangerLevel.LOW,
            maxDangerLevel = DangerLevel.HIGH,
            mandatoryAreas = listOf(
                LocalAreaTemplate("area_tower_base", "Основание мачты и антенное поле", "Открытая площадка", isMandatory = true),
                LocalAreaTemplate("area_radio_room", "Аппаратный зал связи", "Узел связи", isMandatory = true, lootTableId = "loot_industrial_tech")
            ),
            potentialLootKeywordsRu = listOf("Радиодетали", "Шифры", "Кабели", "Электроника"),
            visualAssetPool = listOf("loc_tower"),
            tags = setOf(ContentTag.TECHNICAL, ContentTag.MILITARY),
            baseWeight = 75f
        )
    )

    override val enemyTemplates: List<EnemyTemplate> = listOf(
        EnemyTemplate(
            id = "enemy_anomaly_phantom",
            nameRu = "Аномальный фантом",
            descriptionRu = "Энергетическая проекция, дезориентирующая бойцов электромагнитными импульсами.",
            avatarTag = "enemy_boss",
            role = CharacterRole.SOLDIER,
            aiProfileId = "ai_opportunist",
            baseHp = 60,
            baseAttack = 18,
            baseDefense = 10,
            baseInitiative = 16,
            dangerTier = DangerLevel.HIGH,
            tags = setOf(ContentTag.ANOMALY, ContentTag.RARE)
        )
    )

    override val encounterTemplates: List<EncounterTemplate> = listOf(
        EncounterTemplate(
            id = "enc_bunker_anomaly",
            titleRu = "Аномальное возмущение в бункере",
            descriptionRu = "В подземном комплексе зафиксированы необъяснимые энергетические аномалии и агрессивные фантомы.",
            allowedLocationTypes = setOf(LocationType.MILITARY_BUNKER, LocationType.ANOMALY_ZONE),
            minDangerLevel = DangerLevel.MODERATE,
            maxDangerLevel = DangerLevel.EXTREME,
            enemyPool = listOf(
                EnemyPoolEntry("enemy_anomaly_phantom", weight = 1.0f, minCount = 1, maxCount = 1),
                EnemyPoolEntry("enemy_shield_guard", weight = 1.0f, minCount = 1, maxCount = 2)
            ),
            minEnemies = 2,
            maxEnemies = 3,
            lootTableId = "loot_relic_vault",
            baseRewardXp = 180,
            tags = setOf(ContentTag.ANOMALY, ContentTag.RARE),
            baseWeight = 60f
        )
    )

    override val lootTables: List<LootTableDefinition> = listOf(
        LootTableDefinition(
            id = "loot_relic_vault",
            titleRu = "Сейф довоенных реликвий",
            minCredits = 150,
            maxCredits = 400,
            resourceEntries = listOf(
                LootResourceEntry(ResourceType.RARE_ALLOY, 4, 12, dropChance = 1.0f),
                LootResourceEntry(ResourceType.COMPONENTS, 10, 25, dropChance = 1.0f),
                LootResourceEntry(ResourceType.MEDICINE, 8, 20, dropChance = 0.8f)
            ),
            itemEntries = listOf(
                LootItemEntry("item_spec_nvg", weight = 0.8f, dropChance = 0.35f),
                LootItemEntry("item_armor_composite", weight = 0.6f, dropChance = 0.3f),
                LootItemEntry("item_tool_scanner", weight = 1.0f, dropChance = 0.4f)
            ),
            tags = setOf(ContentTag.ANOMALY, ContentTag.RARE, ContentTag.TECHNICAL)
        )
    )

    override val repeatableQuestTemplates: List<RepeatableQuestTemplate> = listOf(
        RepeatableQuestTemplate(
            id = "rep_contract_rare_alloy",
            titleTemplateRu = "Поиск редких сплавов: %s",
            descriptionTemplateRu = "Исследовательский центр аванпоста готов выплатить крупную премию за доставку титановых сплавов (%d ед.).",
            category = QuestCategory.REPEATABLE,
            source = QuestSource.SETTLEMENT,
            giverNameRu = "Ведущий учёный",
            objectiveType = QuestObjectiveType.COLLECT_RESOURCE,
            targetResourcePool = listOf(ResourceType.RARE_ALLOY),
            minRequiredAmount = 3,
            maxRequiredAmount = 8,
            baseRewardCredits = 280,
            baseRewardXp = 65,
            baseReputationReward = 15,
            cooldownDays = 5,
            tags = setOf(ContentTag.TECHNICAL, ContentTag.RARE)
        )
    )
}
