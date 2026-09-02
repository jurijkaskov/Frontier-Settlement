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
 * Settlement, Trade & Social content pack featuring trading posts, survivor camps,
 * merchant caravans, diplomatic contracts, and barter tables.
 */
object SettlementContentPack : ContentPack {
    override val packId: String = "pack_settlement"
    override val packTitleRu: String = "Торговля и анклавы"
    override val version: Int = 1

    override val locationTemplates: List<LocationTemplate> = listOf(
        LocationTemplate(
            id = "loc_tmpl_trade_outpost",
            type = LocationType.TRADING_POST,
            namePrefixList = listOf("Торговый форпост", "Караван-сарай", "Фактория", "Базарный пункт"),
            nameBaseList = listOf("«Перекрёсток»", "«Золотой караван»", "«Привал»", "«Свободный рынок»"),
            nameSuffixList = listOf("на тракте", "семи ветров", "у источника"),
            descriptionTemplates = listOf(
                "Укреплённый лагерь кочующих торговцев и караванов. Безопасная нейтральная зона для бартера.",
                "Торговая стоянка гильдии купцов с охраняемым складом и постоялым двором."
            ),
            allowedTerrains = setOf(TerrainType.WASTELAND, TerrainType.HILLS),
            minDangerLevel = DangerLevel.SAFE,
            maxDangerLevel = DangerLevel.LOW,
            mandatoryAreas = listOf(
                LocalAreaTemplate("area_market", "Базарная площадь", "Торговая площадка", isMandatory = true, lootTableId = "loot_merchant_stock"),
                LocalAreaTemplate("area_inn", "Караванный постоялый двор", "Зона отдыха", isMandatory = true)
            ),
            optionalAreaPool = listOf(
                LocalAreaTemplate("area_vault", "Склад гильдии купцов", "Охраняемый склад", weight = 1.0f, lootTableId = "loot_merchant_stock"),
                LocalAreaTemplate("area_corral", "Загон для тягловых вьючных животных", "Хозяйственный двор", weight = 0.8f)
            ),
            potentialLootKeywordsRu = listOf("Кредиты", "Экипировка", "Медикаменты", "Слухи"),
            visualAssetPool = listOf("loc_trading_post"),
            tags = setOf(ContentTag.TRADE, ContentTag.SETTLEMENT, ContentTag.SAFE),
            baseWeight = 100f
        )
    )

    override val enemyTemplates: List<EnemyTemplate> = listOf(
        EnemyTemplate(
            id = "enemy_corrupt_guard",
            nameRu = "Подкупленный наёмник",
            descriptionRu = "Наёмный охранник, решивший поживиться за счёт путешественников.",
            avatarTag = "soldier",
            role = CharacterRole.SOLDIER,
            aiProfileId = "ai_balanced",
            baseHp = 58,
            baseAttack = 13,
            baseDefense = 8,
            baseInitiative = 10,
            dangerTier = DangerLevel.LOW,
            tags = setOf(ContentTag.TRADE, ContentTag.MILITARY)
        )
    )

    override val encounterTemplates: List<EncounterTemplate> = listOf(
        EncounterTemplate(
            id = "enc_caravan_raiders",
            titleRu = "Нападение на купеческий караван",
            descriptionRu = "Шайка разбойников пытается отрезать повозку торговца от основного каравана.",
            allowedLocationTypes = setOf(LocationType.TRADING_POST, LocationType.VILLAGE),
            minDangerLevel = DangerLevel.LOW,
            maxDangerLevel = DangerLevel.HIGH,
            enemyPool = listOf(
                EnemyPoolEntry("enemy_raider_scout", weight = 1.5f, minCount = 1, maxCount = 2),
                EnemyPoolEntry("enemy_corrupt_guard", weight = 1.0f, minCount = 0, maxCount = 1)
            ),
            minEnemies = 1,
            maxEnemies = 3,
            lootTableId = "loot_merchant_stock",
            baseRewardXp = 100,
            tags = setOf(ContentTag.TRADE, ContentTag.FACTION_RAIDERS),
            baseWeight = 90f
        )
    )

    override val lootTables: List<LootTableDefinition> = listOf(
        LootTableDefinition(
            id = "loot_merchant_stock",
            titleRu = "Купеческий сундук с товаром",
            minCredits = 90,
            maxCredits = 250,
            resourceEntries = listOf(
                LootResourceEntry(ResourceType.FOOD, 15, 35, dropChance = 0.8f),
                LootResourceEntry(ResourceType.MEDICINE, 4, 12, dropChance = 0.7f),
                LootResourceEntry(ResourceType.COMPONENTS, 5, 15, dropChance = 0.6f)
            ),
            itemEntries = listOf(
                LootItemEntry("item_spec_amulet", weight = 0.5f, dropChance = 0.2f),
                LootItemEntry("item_jacket_reinforced", weight = 0.7f, dropChance = 0.25f)
            ),
            tags = setOf(ContentTag.TRADE, ContentTag.SURVIVAL)
        )
    )

    override val repeatableQuestTemplates: List<RepeatableQuestTemplate> = listOf(
        RepeatableQuestTemplate(
            id = "rep_contract_trade_route",
            titleTemplateRu = "Купеческий контракт: %s",
            descriptionTemplateRu = "Гильдия купцов готова щедро заплатить за доставку медикаментов (%d ед.) на торговую факторию.",
            category = QuestCategory.REPEATABLE,
            source = QuestSource.FACTION,
            giverNameRu = "Караванбаши",
            objectiveType = QuestObjectiveType.COLLECT_RESOURCE,
            targetResourcePool = listOf(ResourceType.MEDICINE),
            minRequiredAmount = 5,
            maxRequiredAmount = 15,
            baseRewardCredits = 200,
            baseRewardXp = 50,
            baseReputationReward = 12,
            cooldownDays = 4,
            tags = setOf(ContentTag.TRADE, ContentTag.FACTION_MERCHANTS)
        )
    )
}
