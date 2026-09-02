package com.example.domain.content.registry.packs

import com.example.domain.content.character.CharacterArchetype
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
import com.example.domain.model.quest.QuestDefinition
import com.example.domain.model.quest.QuestObjectiveType
import com.example.domain.model.quest.QuestSource
import com.example.domain.service.events.EventCatalog
import com.example.domain.service.quest.QuestCatalog

/**
 * Core foundational content pack providing primary locations, enemies, loot tables,
 * archetypes, and repeatable contracts.
 */
object CoreContentPack : ContentPack {
    override val packId: String = "pack_core"
    override val packTitleRu: String = "Базовый контент пустоши"
    override val version: Int = 1

    override val events: List<ExpeditionEvent> = EventCatalog.ALL_EVENTS
    override val storyQuests: List<QuestDefinition> = QuestCatalog.ALL_QUESTS

    override val locationTemplates: List<LocationTemplate> = listOf(
        LocationTemplate(
            id = "loc_tmpl_station",
            type = LocationType.ABANDONED_STATION,
            namePrefixList = listOf("Узловая станция", "Разъезд", "Железнодорожный пост", "Депо"),
            nameBaseList = listOf("«Северный»", "«Восточный»", "«Тупиковый»", "«Магистраль»", "«Стрела»"),
            nameSuffixList = listOf("Сектора 2", "Линии B", "Пустоши", "Ветки 4"),
            descriptionTemplates = listOf(
                "Узловая станция с заброшенными грузовыми составами и ремонтными боксами.",
                "Ржавые товарные вагоны сошли с рельсов. На перроне сохранились ящики снабжения."
            ),
            allowedTerrains = setOf(TerrainType.RUINS, TerrainType.WASTELAND),
            minDangerLevel = DangerLevel.LOW,
            maxDangerLevel = DangerLevel.HIGH,
            mandatoryAreas = listOf(
                LocalAreaTemplate("area_platform", "Главный перрон", "Открытая зона", isMandatory = true, lootTableId = "loot_general_supplies"),
                LocalAreaTemplate("area_depot", "Ремонтное депо", "Промышленный ангар", isMandatory = true, lootTableId = "loot_scavenger_stash")
            ),
            optionalAreaPool = listOf(
                LocalAreaTemplate("area_tower", "Диспетчерская вышка", "Служебный пост", weight = 1.2f, lootTableId = "loot_general_supplies"),
                LocalAreaTemplate("area_fuel_tank", "Топливные цистерны", "Склад ГСМ", weight = 0.8f, lootTableId = "loot_general_supplies"),
                LocalAreaTemplate("area_substation", "Трансформаторная подстанция", "Технический узел", weight = 0.6f, lootTableId = "loot_scavenger_stash")
            ),
            potentialLootKeywordsRu = listOf("Стройматериалы", "Топливо", "Запчасти", "Инструменты"),
            observationTemplatesRu = listOf(
                "Рельсы покрыты ржавчиной, но пути проходимы для легкого транспорта",
                "В окнах диспетчерской не горит свет, признаков активности не видно"
            ),
            threatTemplatesRu = listOf("Острые обломки металла", "Нестабильные перекрытия", "Одиночные мародёры"),
            visualAssetPool = listOf("loc_station"),
            tags = setOf(ContentTag.WASTELAND, ContentTag.RUINS, ContentTag.SCAVENGING, ContentTag.COMMON),
            baseWeight = 120f
        ),
        LocationTemplate(
            id = "loc_tmpl_scavenger_camp",
            type = LocationType.MILITARY_BUNKER,
            namePrefixList = listOf("Лагерь", "Стоянка", "Убежище", "Форпост"),
            nameBaseList = listOf("«Ржавый клык»", "«Бархан»", "«Омега»", "«Холм»"),
            nameSuffixList = listOf("на холмах", "в распадке", "у скал"),
            descriptionTemplates = listOf(
                "Временный укреплённый лагерь кочевников и охотников за утилем.",
                "Небольшой форпост, собранный из контейнеров и листовой брони."
            ),
            allowedTerrains = setOf(TerrainType.WASTELAND, TerrainType.HILLS),
            minDangerLevel = DangerLevel.LOW,
            maxDangerLevel = DangerLevel.HIGH,
            mandatoryAreas = listOf(
                LocalAreaTemplate("area_gate", "Баррикада и въездные ворота", "Оборонительный рубеж", isMandatory = true),
                LocalAreaTemplate("area_tents", "Жилые контейнеры", "Жилой сектор", isMandatory = true, lootTableId = "loot_general_supplies")
            ),
            optionalAreaPool = listOf(
                LocalAreaTemplate("area_watchtower", "Сторожевая вышка", "Наблюдательный пункт", weight = 1.0f),
                LocalAreaTemplate("area_storage", "Склад трофеев", "Складской навес", weight = 0.9f, lootTableId = "loot_scavenger_stash")
            ),
            potentialLootKeywordsRu = listOf("Провизия", "Патроны", "Кредиты", "Утиль"),
            visualAssetPool = listOf("loc_outpost"),
            tags = setOf(ContentTag.WASTELAND, ContentTag.SURVIVAL, ContentTag.COMMON),
            baseWeight = 100f
        )
    )

    override val enemyTemplates: List<EnemyTemplate> = listOf(
        EnemyTemplate(
            id = "enemy_raider_scout",
            nameRu = "Мародёр-разведчик",
            descriptionRu = "Быстрый и верткий стрелок пустоши с самодельным карабином.",
            avatarTag = "enemy_raider",
            role = CharacterRole.SCOUT,
            aiProfileId = "ai_opportunist",
            baseHp = 42,
            baseAttack = 13,
            baseDefense = 4,
            baseInitiative = 14,
            dangerTier = DangerLevel.LOW,
            tags = setOf(ContentTag.FACTION_RAIDERS, ContentTag.LOW_DANGER)
        ),
        EnemyTemplate(
            id = "enemy_raider_boss",
            nameRu = "Главарь банды",
            descriptionRu = "Закаленный в боях лидер бандитов в усиленной броне.",
            avatarTag = "enemy_boss",
            role = CharacterRole.SOLDIER,
            aiProfileId = "ai_aggressive",
            baseHp = 75,
            baseAttack = 17,
            baseDefense = 9,
            baseInitiative = 10,
            dangerTier = DangerLevel.MODERATE,
            tags = setOf(ContentTag.FACTION_RAIDERS, ContentTag.HIGH_DANGER)
        ),
        EnemyTemplate(
            id = "enemy_deserter",
            nameRu = "Одичалый дезертир",
            descriptionRu = "Бывший патрульный с остатками стандартного снаряжения.",
            avatarTag = "enemy_deserter",
            role = CharacterRole.SOLDIER,
            aiProfileId = "ai_balanced",
            baseHp = 52,
            baseAttack = 14,
            baseDefense = 6,
            baseInitiative = 11,
            dangerTier = DangerLevel.LOW,
            tags = setOf(ContentTag.SURVIVAL, ContentTag.LOW_DANGER)
        ),
        EnemyTemplate(
            id = "enemy_shield_guard",
            nameRu = "Бронированный щитовик",
            descriptionRu = "Тяжеловооруженный боец с ростовым щитом.",
            avatarTag = "enemy_guard",
            role = CharacterRole.ENGINEER,
            aiProfileId = "ai_cautious",
            baseHp = 68,
            baseAttack = 11,
            baseDefense = 11,
            baseInitiative = 8,
            dangerTier = DangerLevel.MODERATE,
            tags = setOf(ContentTag.MILITARY, ContentTag.HIGH_DANGER)
        ),
        EnemyTemplate(
            id = "enemy_healer",
            nameRu = "Пустошный знахарь",
            descriptionRu = "Поддерживает союзников перевязками и стимулирующими смесями.",
            avatarTag = "enemy_medic",
            role = CharacterRole.MEDIC,
            aiProfileId = "ai_support",
            baseHp = 44,
            baseAttack = 9,
            baseDefense = 5,
            baseInitiative = 12,
            dangerTier = DangerLevel.LOW,
            tags = setOf(ContentTag.MEDICINE, ContentTag.LOW_DANGER)
        )
    )

    override val encounterTemplates: List<EncounterTemplate> = listOf(
        EncounterTemplate(
            id = "enc_raider_patrol",
            titleRu = "Патруль мародёров",
            descriptionRu = "Группа разведчиков пустоши пытается устроить засаду на открытой местности.",
            allowedLocationTypes = setOf(LocationType.ABANDONED_STATION, LocationType.MILITARY_BUNKER, LocationType.VILLAGE),
            minDangerLevel = DangerLevel.LOW,
            maxDangerLevel = DangerLevel.HIGH,
            enemyPool = listOf(
                EnemyPoolEntry("enemy_raider_scout", weight = 2.0f, minCount = 1, maxCount = 2),
                EnemyPoolEntry("enemy_deserter", weight = 1.5f, minCount = 0, maxCount = 2),
                EnemyPoolEntry("enemy_raider_boss", weight = 0.5f, minCount = 0, maxCount = 1)
            ),
            minEnemies = 1,
            maxEnemies = 3,
            lootTableId = "loot_scavenger_stash",
            baseRewardXp = 90,
            tags = setOf(ContentTag.FACTION_RAIDERS, ContentTag.COMMON),
            baseWeight = 100f
        ),
        EncounterTemplate(
            id = "enc_fortified_camp",
            titleRu = "Охрана укреплённого лагеря",
            descriptionRu = "Опытный отряд бандитов держит оборону за баррикадами.",
            allowedLocationTypes = setOf(LocationType.MILITARY_BUNKER, LocationType.ANOMALY_ZONE),
            minDangerLevel = DangerLevel.MODERATE,
            maxDangerLevel = DangerLevel.EXTREME,
            enemyPool = listOf(
                EnemyPoolEntry("enemy_raider_boss", weight = 1.0f, minCount = 1, maxCount = 1),
                EnemyPoolEntry("enemy_shield_guard", weight = 1.5f, minCount = 1, maxCount = 2),
                EnemyPoolEntry("enemy_healer", weight = 1.0f, minCount = 0, maxCount = 1)
            ),
            minEnemies = 2,
            maxEnemies = 3,
            lootTableId = "loot_military_crate",
            baseRewardXp = 140,
            tags = setOf(ContentTag.MILITARY, ContentTag.HIGH_DANGER),
            baseWeight = 80f
        )
    )

    override val lootTables: List<LootTableDefinition> = listOf(
        LootTableDefinition(
            id = "loot_general_supplies",
            titleRu = "Ящик с припасами",
            minCredits = 25,
            maxCredits = 80,
            resourceEntries = listOf(
                LootResourceEntry(ResourceType.FOOD, 10, 25, dropChance = 0.9f),
                LootResourceEntry(ResourceType.WATER, 10, 30, dropChance = 0.9f),
                LootResourceEntry(ResourceType.MATERIALS, 15, 40, dropChance = 0.8f),
                LootResourceEntry(ResourceType.FUEL, 5, 15, dropChance = 0.6f)
            ),
            itemEntries = listOf(
                LootItemEntry("item_medkit_small", weight = 1.5f, minCount = 1, maxCount = 2, dropChance = 0.5f),
                LootItemEntry("item_filter_water", weight = 1.0f, minCount = 1, maxCount = 1, dropChance = 0.35f)
            ),
            tags = setOf(ContentTag.SURVIVAL, ContentTag.COMMON)
        ),
        LootTableDefinition(
            id = "loot_scavenger_stash",
            titleRu = "Тайник собирателя утиля",
            minCredits = 50,
            maxCredits = 150,
            resourceEntries = listOf(
                LootResourceEntry(ResourceType.MATERIALS, 30, 75, dropChance = 1.0f),
                LootResourceEntry(ResourceType.FUEL, 10, 25, dropChance = 0.7f),
                LootResourceEntry(ResourceType.COMPONENTS, 4, 12, dropChance = 0.6f)
            ),
            itemEntries = listOf(
                LootItemEntry("item_tool_scanner", weight = 1.0f, dropChance = 0.3f),
                LootItemEntry("item_backpack_medium", weight = 0.8f, dropChance = 0.25f)
            ),
            tags = setOf(ContentTag.SCAVENGING, ContentTag.TECHNICAL)
        ),
        LootTableDefinition(
            id = "loot_military_crate",
            titleRu = "Армейский оружейный ящик",
            minCredits = 80,
            maxCredits = 220,
            resourceEntries = listOf(
                LootResourceEntry(ResourceType.AMMO, 15, 40, dropChance = 1.0f),
                LootResourceEntry(ResourceType.MEDICINE, 5, 15, dropChance = 0.8f),
                LootResourceEntry(ResourceType.RARE_ALLOY, 2, 6, dropChance = 0.5f)
            ),
            itemEntries = listOf(
                LootItemEntry("item_armor_composite", weight = 0.7f, dropChance = 0.25f),
                LootItemEntry("item_spec_nvg", weight = 0.5f, dropChance = 0.2f),
                LootItemEntry("item_tool_machete", weight = 1.2f, dropChance = 0.4f)
            ),
            tags = setOf(ContentTag.MILITARY, ContentTag.HIGH_DANGER)
        )
    )

    override val characterArchetypes: List<CharacterArchetype> = listOf(
        CharacterArchetype(
            id = "arch_scout",
            role = CharacterRole.SCOUT,
            titleRu = "Следопыт пустоши",
            bioTemplates = listOf(
                "Бывший дальнобойщик пустоши, знающий каждый каньон и скрытый проход.",
                "Опытный следопыт из разрушенного форпоста. В темноте видит лучше, чем при свете дня.",
                "Снайпер-наблюдатель, проведший месяцы на радиомачтах в радиоактивной пыли."
            ),
            statWeights = mapOf(
                CharacterStatType.SCAVENGING to 0.45f,
                CharacterStatType.ATTACK to 0.25f,
                CharacterStatType.DEFENSE to 0.15f,
                CharacterStatType.ENGINEERING to 0.10f,
                CharacterStatType.MEDICAL to 0.05f
            ),
            minStatBudget = 48,
            maxStatBudget = 62,
            preferredTraits = listOf("trait_pathfinder", "trait_night_owl", "trait_tactical_sniper"),
            forbiddenTraits = listOf("trait_clumsy"),
            specializations = listOf("Дальняя разведка", "Снайперская стрельба", "Ориентирование"),
            avatarTags = listOf("scout", "scout_female", "sniper"),
            tags = setOf(ContentTag.SURVIVAL, ContentTag.COMMON)
        ),
        CharacterArchetype(
            id = "arch_soldier",
            role = CharacterRole.SOLDIER,
            titleRu = "Боец аванпоста",
            bioTemplates = listOf(
                "Ветеран охраны торговых караванов. Привык держать оборону против стай мутантов.",
                "Бывший боец патрульной службы фронтира с тяжёлой модифицированной экипировкой.",
                "Бесстрашный штурмовик, выживший при осаде бункера."
            ),
            statWeights = mapOf(
                CharacterStatType.ATTACK to 0.45f,
                CharacterStatType.DEFENSE to 0.35f,
                CharacterStatType.SCAVENGING to 0.10f,
                CharacterStatType.ENGINEERING to 0.05f,
                CharacterStatType.MEDICAL to 0.05f
            ),
            minStatBudget = 50,
            maxStatBudget = 64,
            preferredTraits = listOf("trait_tank_build", "trait_iron_nerves", "trait_inspiring_leader"),
            forbiddenTraits = listOf("trait_cowardly"),
            specializations = listOf("Тяжёлый штурм", "Оборона рубежа", "Ближний бой"),
            avatarTags = listOf("soldier", "soldier_heavy", "guard"),
            tags = setOf(ContentTag.MILITARY, ContentTag.COMMON)
        ),
        CharacterArchetype(
            id = "arch_medic",
            role = CharacterRole.MEDIC,
            titleRu = "Полевой врач",
            bioTemplates = listOf(
                "Полевой хирург, умеющий останавливать кровотечения в самых безнадёжных полевых условиях.",
                "Фармацевт и травник, умеющий синтезировать противорадиационные сыворотки из флоры пустоши.",
                "Бывший санитар экспедиционного корпуса, всегда готовый прикрыть раненых союзников."
            ),
            statWeights = mapOf(
                CharacterStatType.MEDICAL to 0.50f,
                CharacterStatType.SCAVENGING to 0.20f,
                CharacterStatType.DEFENSE to 0.15f,
                CharacterStatType.ENGINEERING to 0.10f,
                CharacterStatType.ATTACK to 0.05f
            ),
            minStatBudget = 46,
            maxStatBudget = 58,
            preferredTraits = listOf("trait_combat_medic", "trait_radiation_resistant"),
            forbiddenTraits = listOf("trait_squeamish"),
            specializations = listOf("Полевая хирургия", "Токсикология", "Реанимация"),
            avatarTags = listOf("medic", "medic_field"),
            tags = setOf(ContentTag.MEDICINE, ContentTag.COMMON)
        ),
        CharacterArchetype(
            id = "arch_engineer",
            role = CharacterRole.ENGINEER,
            titleRu = "Техник-механик",
            bioTemplates = listOf(
                "Умелец, способный собрать работающий генератор из консервных банок и медных проводов.",
                "Техник-механик, восстанавливавший бронетранспортёры и станки в подземных мастерских.",
                "Электронщик, специализирующийся на перепрошивке сенсоров и ремонте радиовышек."
            ),
            statWeights = mapOf(
                CharacterStatType.ENGINEERING to 0.50f,
                CharacterStatType.SCAVENGING to 0.20f,
                CharacterStatType.DEFENSE to 0.15f,
                CharacterStatType.ATTACK to 0.10f,
                CharacterStatType.MEDICAL to 0.05f
            ),
            minStatBudget = 48,
            maxStatBudget = 60,
            preferredTraits = listOf("trait_master_craftsman", "trait_meticulous"),
            forbiddenTraits = listOf("trait_clumsy"),
            specializations = listOf("Ремонт техники", "Электроника", "Фортификация"),
            avatarTags = listOf("engineer", "mechanic"),
            tags = setOf(ContentTag.TECHNICAL, ContentTag.COMMON)
        ),
        CharacterArchetype(
            id = "arch_scavenger",
            role = CharacterRole.SCAVENGER,
            titleRu = "Собиратель утиля",
            bioTemplates = listOf(
                "Мастер поиска тайников в полуразрушенных городах и заброшенных бункерах.",
                "Следопыт с чутьём на редкие микросхемы и уцелевшие контейнеры с провизией.",
                "Охотник за утилем и редкими сплавами, готовый залезть в самые опасные катакомбы."
            ),
            statWeights = mapOf(
                CharacterStatType.SCAVENGING to 0.50f,
                CharacterStatType.ENGINEERING to 0.20f,
                CharacterStatType.DEFENSE to 0.15f,
                CharacterStatType.ATTACK to 0.10f,
                CharacterStatType.MEDICAL to 0.05f
            ),
            minStatBudget = 47,
            maxStatBudget = 60,
            preferredTraits = listOf("trait_scrap_hoarder", "trait_cautious_pacer"),
            forbiddenTraits = listOf("trait_careless"),
            specializations = listOf("Поиск схронов", "Сортировка утиля", "Взлом контейнеров"),
            avatarTags = listOf("scavenger", "scavenger_veteran"),
            tags = setOf(ContentTag.SCAVENGING, ContentTag.COMMON)
        )
    )

    override val repeatableQuestTemplates: List<RepeatableQuestTemplate> = listOf(
        RepeatableQuestTemplate(
            id = "rep_contract_materials",
            titleTemplateRu = "Поставка стройматериалов: %s",
            descriptionTemplateRu = "Строительным бригадам аванпоста срочно требуется партия из %d стройматериалов для ремонта укреплений.",
            category = QuestCategory.REPEATABLE,
            source = QuestSource.SETTLEMENT,
            giverNameRu = "Мастер снабжения",
            objectiveType = QuestObjectiveType.COLLECT_RESOURCE,
            targetResourcePool = listOf(ResourceType.MATERIALS),
            minRequiredAmount = 30,
            maxRequiredAmount = 60,
            baseRewardCredits = 120,
            baseRewardXp = 35,
            baseReputationReward = 6,
            cooldownDays = 2,
            tags = setOf(ContentTag.SETTLEMENT, ContentTag.SURVIVAL)
        ),
        RepeatableQuestTemplate(
            id = "rep_contract_scout_location",
            titleTemplateRu = "Патрулирование сектора: %s",
            descriptionTemplateRu = "Штаб обороны запрашивает проведение разведывательного рейда в сектор «%s» для оценки угрозы.",
            category = QuestCategory.REPEATABLE,
            source = QuestSource.SETTLEMENT,
            giverNameRu = "Координатор разведки",
            objectiveType = QuestObjectiveType.VISIT_LOCATION,
            targetLocationTypes = setOf(LocationType.ABANDONED_STATION, LocationType.MILITARY_BUNKER, LocationType.FARM),
            baseRewardCredits = 140,
            baseRewardXp = 45,
            baseReputationReward = 8,
            cooldownDays = 3,
            tags = setOf(ContentTag.SURVIVAL, ContentTag.MILITARY)
        )
    )
}
