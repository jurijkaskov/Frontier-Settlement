package com.example.data

import com.example.domain.model.reputation.*

/**
 * Centralized game balance configuration for the Frontier Wasteland Reputation & Faction System.
 */
object ReputationBalanceConfig {

    // Faction Unique String IDs
    const val FACTION_TRADERS = "faction_traders"
    const val FACTION_ENGINEERS = "faction_engineers"
    const val FACTION_NOMADS = "faction_nomads"
    const val FACTION_SURVIVORS = "faction_survivors"
    const val FACTION_RAIDERS = "faction_raiders"

    const val MIN_POINTS = -100
    const val MAX_POINTS = 100

    /**
     * Alias for Factions list.
     */
    val FACTION_CATALOG: List<FactionDefinition> get() = FACTIONS

    /**
     * Complete Wasteland Faction Catalog.
     */
    val FACTIONS: List<FactionDefinition> = listOf(
        FactionDefinition(
            id = FACTION_TRADERS,
            nameRu = "Торговая Гильдия Пустоши",
            titleRu = "Караванщики и негоцианты",
            taglineRu = "«Кредиты не пахнут пылью, а надежный партнер дороже ящика патронов.»",
            descriptionRu = "Влиятельный торговый синдикат, контролирующий ключевые караванные пути через руины мегаполисов. Обеспечивают стабильный подвоз топлива, медикаментов и редких сплавов.",
            iconKey = "storefront",
            colorHex = 0xFFFFD54F,
            homeLocationId = "loc_station",
            baseRelation = 15,
            leaderNameRu = "Мастер Морган «Вексель»",
            leaderTitleRu = "Глава Совета Торговых Караванов",
            perks = listOf(
                FactionPerk(
                    id = "perk_trade_discount_1",
                    titleRu = "Привилегии купца",
                    descriptionRu = "Скидка -5% на все закупки в Торговом Посту и +5% к выручке от продажи.",
                    requiredTier = FactionRelationTier.FRIENDLY,
                    iconKey = "local_offer"
                ),
                FactionPerk(
                    id = "perk_trade_discount_2",
                    titleRu = "Золотой статус Гильдии",
                    descriptionRu = "Скидка -15% на закупки, +10% к продаже, увеличенный запас редких сплавов и деталей у каравана.",
                    requiredTier = FactionRelationTier.ALLIED,
                    iconKey = "workspace_premium"
                )
            )
        ),
        FactionDefinition(
            id = FACTION_ENGINEERS,
            nameRu = "Братство Инженеров",
            titleRu = "Техно-жрецы и хранители схем",
            taglineRu = "«Металл помнит, чертежи не врут, а знание восстановит этот мир.»",
            descriptionRu = "Орден учёных и механиков довоенной школы. Занимаются раскопками научных бункеров, восстановлением энергосетей и разработкой передового оружия.",
            iconKey = "science",
            colorHex = 0xFF4DD0E1,
            homeLocationId = "loc_factory",
            baseRelation = 5,
            leaderNameRu = "Архитектор Воронов",
            leaderTitleRu = "Куратор Технологического Репозитория",
            perks = listOf(
                FactionPerk(
                    id = "perk_tech_speed_1",
                    titleRu = "Обмен чертежами",
                    descriptionRu = "+15% к скорости научных исследований и -10% стоимости крафта деталей в Мастерской.",
                    requiredTier = FactionRelationTier.FRIENDLY,
                    iconKey = "architecture"
                ),
                FactionPerk(
                    id = "perk_tech_speed_2",
                    titleRu = "Технологический альянс",
                    descriptionRu = "+30% к скорости исследований, доступ к прототипам энергоядер и сниженный износ техники.",
                    requiredTier = FactionRelationTier.ALLIED,
                    iconKey = "precision_manufacturing"
                )
            )
        ),
        FactionDefinition(
            id = FACTION_NOMADS,
            nameRu = "Вольные Кочевники",
            titleRu = "Следопыты дюн и проводники",
            taglineRu = "«Ветер знает каждый бархан, а наши разведчики вернутся из любого ада.»",
            descriptionRu = "Мобильные кланы кочевников, приспособившихся к жизни в радиоактивных пустынях. В совершенстве знают потайные тропы, тайники и повадки мутантов.",
            iconKey = "explore",
            colorHex = 0xFF81C784,
            homeLocationId = "loc_quarry",
            baseRelation = 10,
            leaderNameRu = "Старейшина Наира",
            leaderTitleRu = "Матриарх Клана Песчаных Лисиц",
            perks = listOf(
                FactionPerk(
                    id = "perk_nomad_speed_1",
                    titleRu = "Карты тайных троп",
                    descriptionRu = "+20% к скорости перемещения экспедиций и +10% к шансу обнаружения тайников.",
                    requiredTier = FactionRelationTier.FRIENDLY,
                    iconKey = "map"
                ),
                FactionPerk(
                    id = "perk_nomad_speed_2",
                    titleRu = "Кровное побратимство",
                    descriptionRu = "+35% к скорости пеших и автоэкспедиций, снижение опасности секторов на 1 ранг.",
                    requiredTier = FactionRelationTier.ALLIED,
                    iconKey = "terrain"
                )
            )
        ),
        FactionDefinition(
            id = FACTION_SURVIVORS,
            nameRu = "Союз Выживших",
            titleRu = "Беженцы и свободные колонисты",
            taglineRu = "«Вместе мы восстановим дом среди пепла.»",
            descriptionRu = "Сеть мирных фермерских коммун и беженцев. Ищут защиты от рейдеров и готовы делиться провизией, рабочими руками и верностью.",
            iconKey = "groups",
            colorHex = 0xFFAED581,
            homeLocationId = "loc_farm",
            baseRelation = 20,
            leaderNameRu = "Мария Северова",
            leaderTitleRu = "Координатор Комитета Беженцев",
            perks = listOf(
                FactionPerk(
                    id = "perk_survivor_morale_1",
                    titleRu = "Убежище для страждущих",
                    descriptionRu = "+10 к стартовой морали всех новобранцев и -15% затрат на наём специалистов.",
                    requiredTier = FactionRelationTier.FRIENDLY,
                    iconKey = "volunteer_activism"
                ),
                FactionPerk(
                    id = "perk_survivor_morale_2",
                    titleRu = "Оплот единения",
                    descriptionRu = "+20 к морали поселенцев, регулярный приток добровольцев с уникальными чертами.",
                    requiredTier = FactionRelationTier.ALLIED,
                    iconKey = "diversity_3"
                )
            )
        ),
        FactionDefinition(
            id = FACTION_RAIDERS,
            nameRu = "Синдикат Скрапперов",
            titleRu = "Банды пустошей и налётчики",
            taglineRu = "«Слабый отдаёт припасы, сильный устанавливает правила.»",
            descriptionRu = "Разрозненные, но хорошо вооружённые банды налётчиков. Могут устраивать засады на караваны или соблюдать пакт о ненападении, если встретят силу или выгодный выкуп.",
            iconKey = "gavel",
            colorHex = 0xFFE57373,
            homeLocationId = "loc_settlement_ruins",
            baseRelation = -20,
            leaderNameRu = "Атаман Череп",
            leaderTitleRu = "Главарь Банды Ржавого Клыка",
            perks = listOf(
                FactionPerk(
                    id = "perk_raider_truce",
                    titleRu = "Пакт о ненападении",
                    descriptionRu = "Рейдеры перестают атаковать ваши конвои на ближних секторах (Нейтралитет).",
                    requiredTier = FactionRelationTier.NEUTRAL,
                    iconKey = "handshake"
                ),
                FactionPerk(
                    id = "perk_black_market",
                    titleRu = "Чёрный рынок оружия",
                    descriptionRu = "Доступ к трофейному бронебойному оружию и взрывчатке скрапперов со скидкой -20%.",
                    requiredTier = FactionRelationTier.FRIENDLY,
                    iconKey = "local_fire_department"
                )
            )
        )
    )

    fun getFaction(id: String): FactionDefinition? = FACTIONS.find { it.id == id }

    fun createInitialFactionRelations(): Map<String, FactionRelation> {
        return FACTIONS.associate { faction ->
            faction.id to FactionRelation(
                factionId = faction.id,
                points = faction.baseRelation,
                isDiscovered = true,
                notes = "Базовый уровень дипломатических отношений."
            )
        }
    }
}
