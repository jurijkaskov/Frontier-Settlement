package com.example.domain.service.events

import com.example.domain.model.*

/**
 * Static catalog containing 17 comprehensive, data-driven frontier events.
 */
object EventCatalog {

    val ALL_EVENTS: List<ExpeditionEvent> by lazy {
        listOf(
            eventLockedWarehouse,
            eventAbandonedTruck,
            eventCampfireRemnants,
            eventDamagedGenerator,
            eventSupplyCache,
            eventCollapsedTunnel,
            eventVendingTerminal,
            eventMysteriousNoise,
            eventStrangerTracks,
            eventDerelictMachinery,
            eventAbandonedCamp,
            eventScoutDiscovery,
            eventVehicleObstacle,
            eventSafeVantagePoint,
            eventRareRelicVault,
            eventDistressBeacon,
            eventNomadTraderEncounter
        )
    }

    val EVT_SCAVENGE_SUPPLIES: ExpeditionEvent get() = eventSupplyCache
    val EVT_LOCKED_SAFE: ExpeditionEvent get() = eventLockedWarehouse
    val EVT_MYSTERIOUS_TRANSMISSION: ExpeditionEvent get() = eventDistressBeacon

    // 1. Flagship Demonstration Event: Запертый склад
    val eventLockedWarehouse = ExpeditionEvent(
        id = "evt_locked_warehouse",
        title = "Запертый складской бокс",
        description = "Дверь металлического склада плотно заперта на массивный засов с навесным замком старого образца. Через треснувшее армированное окно видны нетронутые герметичные контейнеры и штабели стройматериалов.",
        category = EventCategory.DISCOVERY,
        rarity = EventRarity.COMMON,
        baseWeight = 100,
        repeatMode = EventRepeatMode.ONCE_PER_LOCATION,
        visualAssetId = "evt_warehouse",
        requirements = listOf(),
        choices = listOf(
            EventChoice(
                id = "choice_tools",
                text = "Использовать слесарные инструменты",
                description = "Аккуратно срезать дужку замка инструментами без шума и лишних усилий.",
                requirements = listOf(
                    EventRequirement.RequiresRole(CharacterRole.ENGINEER)
                ),
                riskLevelText = "Низкий риск",
                actionIconKey = "build",
                successOutcome = EventOutcome(
                    title = "Замок срезан",
                    narrativeText = "Инженер умело перекусил стальную дужку. Внутри обнаружены целые упаковки стройматериалов и ящик с деталями.",
                    resourceRewards = mapOf(
                        ResourceType.MATERIALS to 45,
                        ResourceType.COMPONENTS to 8,
                        ResourceType.MONEY to 50
                    ),
                    xpReward = 40,
                    explorationProgressGain = 20,
                    setWorldFlags = mapOf("opened_warehouse" to true)
                )
            ),
            EventChoice(
                id = "choice_strength",
                text = "Сорвать замок монтировкой и силой",
                description = "Применить физическую силу, чтобы выломать проржавевшую проушину двери.",
                skillCheck = SkillCheckRequirement(
                    statType = CharacterStatType.ATTACK,
                    difficulty = 6,
                    applicableRoles = setOf(CharacterRole.SOLDIER, CharacterRole.SCAVENGER),
                    bonusDescription = "Штурмовики и следопыты получают преимущество."
                ),
                riskLevelText = "Средний риск",
                actionIconKey = "fitness_center",
                successOutcome = EventOutcome(
                    title = "Дверь выбита",
                    narrativeText = "С громким скрежетом металлическая проушина поддалась! Отряд проник внутрь и забрал строительные материалы.",
                    resourceRewards = mapOf(
                        ResourceType.MATERIALS to 35,
                        ResourceType.MONEY to 30
                    ),
                    xpReward = 35,
                    explorationProgressGain = 15
                ),
                failureOutcome = EventOutcome(
                    title = "Замок не поддался",
                    narrativeText = "Засов оказался слишком прочным. Боец сорвал руку и потратил силы впустую.",
                    xpReward = 10,
                    healthDelta = -5,
                    explorationProgressGain = 5
                )
            ),
            EventChoice(
                id = "choice_scout_window",
                text = "Осмотреть оконный проём и вентиляцию",
                description = "Разведчик может попробовать протиснуться через вентиляционный люк на крыше.",
                skillCheck = SkillCheckRequirement(
                    statType = CharacterStatType.SCAVENGING,
                    difficulty = 5,
                    applicableRoles = setOf(CharacterRole.SCOUT),
                    bonusDescription = "Разведчики легко находят скрытые лазы."
                ),
                riskLevelText = "Низкий риск",
                actionIconKey = "search",
                successOutcome = EventOutcome(
                    title = "Проникновение через люк",
                    narrativeText = "Разведчик проник через крышу и открыл дверь изнутри, обнаружив ценные электронные платы!",
                    resourceRewards = mapOf(
                        ResourceType.COMPONENTS to 6,
                        ResourceType.MATERIALS to 20,
                        ResourceType.MONEY to 40
                    ),
                    xpReward = 35,
                    explorationProgressGain = 20
                ),
                failureOutcome = EventOutcome(
                    title = "Узкий лаз завален",
                    narrativeText = "Вентиляционная шахта оказалась забита битым бетоном. Пришлось отступить.",
                    xpReward = 10,
                    explorationProgressGain = 5
                )
            ),
            EventChoice(
                id = "choice_leave",
                text = "Не рисковать и продолжить путь",
                description = "Оставить склад нетронутым и сохранить силы для дальнейшего перехода.",
                riskLevelText = "Без риска",
                actionIconKey = "directions_walk",
                successOutcome = EventOutcome(
                    title = "Отряд прошёл мимо",
                    narrativeText = "Командир решил не тратить время на подозрительный объект.",
                    xpReward = 5,
                    explorationProgressGain = 5
                )
            )
        )
    )

    // 2. Заброшенный грузовик снабжения
    val eventAbandonedTruck = ExpeditionEvent(
        id = "evt_abandoned_truck",
        title = "Заброшенный грузовик снабжения",
        description = "На обочине полуразрушенной трассы стоит брошенный грузовой тягач с эмблемой довоенной логистической службы. Топливный бак выглядит целым, а кабина наполовину завалена ветками.",
        category = EventCategory.TECHNICAL,
        rarity = EventRarity.COMMON,
        baseWeight = 90,
        repeatMode = EventRepeatMode.ONCE_PER_LOCATION,
        visualAssetId = "evt_truck",
        choices = listOf(
            EventChoice(
                id = "choice_siphon_fuel",
                text = "Слить остатки дизельного топлива из бака",
                description = "Использовать шланг и канистры для перекачки уцелевшего горючего.",
                skillCheck = SkillCheckRequirement(
                    statType = CharacterStatType.ENGINEERING,
                    difficulty = 5,
                    applicableRoles = setOf(CharacterRole.ENGINEER, CharacterRole.SCAVENGER)
                ),
                riskLevelText = "Низкий риск",
                actionIconKey = "local_gas_station",
                successOutcome = EventOutcome(
                    title = "Топливо получено",
                    narrativeText = "В баке сохранилось чистое дизельное топливо! Отряд наполнил экспедиционные канистры.",
                    resourceRewards = mapOf(
                        ResourceType.FUEL to 30,
                        ResourceType.MATERIALS to 10
                    ),
                    xpReward = 30,
                    explorationProgressGain = 15
                ),
                failureOutcome = EventOutcome(
                    title = "Топливо окислилось",
                    narrativeText = "На дне бака остался лишь вязкий осадок. Удалось слить совсем немного.",
                    resourceRewards = mapOf(ResourceType.FUEL to 8),
                    xpReward = 10,
                    explorationProgressGain = 5
                )
            ),
            EventChoice(
                id = "choice_dismantle_parts",
                text = "Снять аккумулятор и запчасти двигателя",
                description = "Разобрать моторный отсек на компоненты и медную проводку.",
                requirements = listOf(
                    EventRequirement.RequiresRole(CharacterRole.ENGINEER)
                ),
                riskLevelText = "Требуется инженер",
                actionIconKey = "settings",
                successOutcome = EventOutcome(
                    title = "Запчасти демонтированы",
                    narrativeText = "Инженер аккуратно снял рабочий генератор и медные кабели высокой проводимости.",
                    resourceRewards = mapOf(
                        ResourceType.COMPONENTS to 10,
                        ResourceType.MATERIALS to 25
                    ),
                    xpReward = 40,
                    explorationProgressGain = 15
                )
            ),
            EventChoice(
                id = "choice_search_cabin",
                text = "Обыскать бардачок и салон кабины",
                description = "Быстрый поверхностный осмотр салона водителя.",
                riskLevelText = "Безопасно",
                actionIconKey = "search",
                successOutcome = EventOutcome(
                    title = "Находки в кабине",
                    narrativeText = "Под сиденьем обнаружена аптечка первой помощи и пара пайков сухого рациона.",
                    resourceRewards = mapOf(
                        ResourceType.FOOD to 15,
                        ResourceType.WATER to 10,
                        ResourceType.MEDICINE to 2
                    ),
                    xpReward = 20,
                    explorationProgressGain = 10
                )
            )
        )
    )

    // 3. Следы недавней стоянки
    val eventCampfireRemnants = ExpeditionEvent(
        id = "evt_campfire_remnants",
        title = "Следы недавней стоянки",
        description = "В тени бетонных плит обнаружено кострище. Угли ещё сохраняют остаточное тепло, а вокруг видны следы обуви и пустые консервные банки.",
        category = EventCategory.ENCOUNTER,
        rarity = EventRarity.COMMON,
        baseWeight = 85,
        repeatMode = EventRepeatMode.REPEATABLE,
        visualAssetId = "evt_campfire",
        choices = listOf(
            EventChoice(
                id = "choice_track_scout",
                text = "Разведчику взять след ушедшей группы",
                description = "Определить направление движения и численность неизвестных.",
                skillCheck = SkillCheckRequirement(
                    statType = CharacterStatType.SCAVENGING,
                    difficulty = 6,
                    applicableRoles = setOf(CharacterRole.SCOUT)
                ),
                riskLevelText = "Средний шанс",
                actionIconKey = "explore",
                successOutcome = EventOutcome(
                    title = "Следы расшифрованы",
                    narrativeText = "Разведчик установил: здесь отдыхали двое безоружных собирателей. На развилке они обронили походный мешок с припасами.",
                    resourceRewards = mapOf(
                        ResourceType.FOOD to 20,
                        ResourceType.WATER to 15,
                        ResourceType.MONEY to 25
                    ),
                    xpReward = 35,
                    explorationProgressGain = 25,
                    setWorldFlags = mapOf("discovered_trader_trail" to true)
                ),
                failureOutcome = EventOutcome(
                    title = "След затерялся в камнях",
                    narrativeText = "Каменистая почва скрыла отпечатки подошв. Отряд вернулся на исходный маршрут.",
                    xpReward = 10,
                    explorationProgressGain = 10
                )
            ),
            EventChoice(
                id = "choice_search_ashes",
                text = "Осмотреть золу и окрестные тайники",
                description = "Проверить, не закопали ли выжившие консервы или патроны.",
                riskLevelText = "Низкий риск",
                actionIconKey = "saved_search",
                successOutcome = EventOutcome(
                    title = "Тайник в золе",
                    narrativeText = "Под слоем пепла обнаружена жестяная коробка с патронами и спичками.",
                    resourceRewards = mapOf(
                        ResourceType.AMMO to 10,
                        ResourceType.MATERIALS to 12
                    ),
                    xpReward = 20,
                    explorationProgressGain = 10
                )
            )
        )
    )

    // 4. Повреждённый дизель-генератор
    val eventDamagedGenerator = ExpeditionEvent(
        id = "evt_damaged_generator",
        title = "Аварийный резервный генератор",
        description = "В техническом приямке обнаружен промышленный генератор. Кабели искрят, а из трещины в картере капает масло. При правильном подходе его можно запустить или разобрать.",
        category = EventCategory.TECHNICAL,
        rarity = EventRarity.UNCOMMON,
        baseWeight = 70,
        repeatMode = EventRepeatMode.ONCE_PER_LOCATION,
        visualAssetId = "evt_generator",
        choices = listOf(
            EventChoice(
                id = "choice_repair_gen",
                text = "Починить и запустить подстанцию",
                description = "Восстановить подачу электроэнергии для разблокировки автоматических дверей.",
                skillCheck = SkillCheckRequirement(
                    statType = CharacterStatType.ENGINEERING,
                    difficulty = 7,
                    applicableRoles = setOf(CharacterRole.ENGINEER)
                ),
                costResources = mapOf(ResourceType.MATERIALS to 5),
                riskLevelText = "Требует инженерию и 5 материалов",
                actionIconKey = "electrical_services",
                successOutcome = EventOutcome(
                    title = "Генератор ожил!",
                    narrativeText = "Двигатель ровно загудел! Освещение в секторе включилось, открыв доступ к закрытым серверным шкафам.",
                    resourceRewards = mapOf(
                        ResourceType.COMPONENTS to 12,
                        ResourceType.MONEY to 60,
                        ResourceType.FUEL to 15
                    ),
                    xpReward = 50,
                    explorationProgressGain = 30,
                    setWorldFlags = mapOf("station_power_restored" to true)
                ),
                failureOutcome = EventOutcome(
                    title = "Короткое замыкание",
                    narrativeText = "Искровой разряд оплавил плату управления. Инженер получил лёгкий ожог.",
                    xpReward = 15,
                    healthDelta = -10,
                    explorationProgressGain = 5
                )
            ),
            EventChoice(
                id = "choice_scrap_copper",
                text = "Срезать медные кабели и стартер",
                description = "Забрать то, что имеет ценность как лом и сырьё.",
                riskLevelText = "Безопасно",
                actionIconKey = "content_cut",
                successOutcome = EventOutcome(
                    title = "Медь и лом собраны",
                    narrativeText = "Отряд срезал несколько метров качественного медного силового кабеля.",
                    resourceRewards = mapOf(
                        ResourceType.MATERIALS to 30,
                        ResourceType.COMPONENTS to 4
                    ),
                    xpReward = 25,
                    explorationProgressGain = 10
                )
            )
        )
    )

    // 5. Замаскированный тайник старателей
    val eventSupplyCache = ExpeditionEvent(
        id = "evt_supply_cache",
        title = "Замаскированный тайник",
        description = "Под слоем маскировочной сетки и веток следопыт приметил углубление в грунте. Сверху лежит тяжёлый деревянный щит с нанесённым краской знаком опасности.",
        category = EventCategory.RESOURCE,
        rarity = EventRarity.COMMON,
        baseWeight = 80,
        repeatMode = EventRepeatMode.REPEATABLE,
        visualAssetId = "evt_cache",
        choices = listOf(
            EventChoice(
                id = "choice_disarm_trap",
                text = "Проверить тайник на наличие растяжки",
                description = "Осторожно изучить крепление щита перед открытием.",
                skillCheck = SkillCheckRequirement(
                    statType = CharacterStatType.SCAVENGING,
                    difficulty = 6,
                    applicableRoles = setOf(CharacterRole.SCOUT, CharacterRole.SCAVENGER)
                ),
                riskLevelText = "Средний риск",
                actionIconKey = "security",
                successOutcome = EventOutcome(
                    title = "Растяжка обезврежена!",
                    narrativeText = "Следопыт вовремя заметил тонкую леску от сигнальной гранаты. В тайнике найдены неповреждённые медикаменты и патроны!",
                    resourceRewards = mapOf(
                        ResourceType.MEDICINE to 4,
                        ResourceType.AMMO to 15,
                        ResourceType.FOOD to 25
                    ),
                    xpReward = 40,
                    explorationProgressGain = 20
                ),
                failureOutcome = EventOutcome(
                    title = "Сработал запал!",
                    narrativeText = "Ловушка хлопнула, осыпав отряд шрапнелью и повредив часть припасов.",
                    resourceRewards = mapOf(ResourceType.FOOD to 10),
                    xpReward = 15,
                    healthDelta = -15,
                    explorationProgressGain = 10
                )
            ),
            EventChoice(
                id = "choice_brute_open",
                text = "Сдёрнуть щит длинной верёвкой с дистанции",
                description = "Безопасный способ дистанционного вскрытия без риска для бойцов.",
                riskLevelText = "Низкий риск",
                actionIconKey = "shield",
                successOutcome = EventOutcome(
                    title = "Дистанционное вскрытие",
                    narrativeText = "Щит отлетел в сторону. Часть стеклянных флаконов разбилась, но базовые пайки и материалы уцелели.",
                    resourceRewards = mapOf(
                        ResourceType.FOOD to 15,
                        ResourceType.MATERIALS to 15
                    ),
                    xpReward = 20,
                    explorationProgressGain = 10
                )
            )
        )
    )

    // 6. Заваленный проход в подземный бункер
    val eventCollapsedTunnel = ExpeditionEvent(
        id = "evt_collapsed_tunnel",
        title = "Заваленный проход в бункер",
        description = "Вход в подземные тоннели перекрыт рухнувшими железобетонными балками. Сквозь щели ощущается ток прохладного сухого воздуха из глубины комплекса.",
        category = EventCategory.ENVIRONMENT,
        rarity = EventRarity.UNCOMMON,
        baseWeight = 65,
        repeatMode = EventRepeatMode.ONCE_PER_LOCATION,
        visualAssetId = "evt_tunnel",
        choices = listOf(
            EventChoice(
                id = "choice_clear_rubble",
                text = "Расчистить завал совместными усилиями",
                description = "Физически сдвинуть балки с помощью рычагов и упоров.",
                skillCheck = SkillCheckRequirement(
                    statType = CharacterStatType.ATTACK,
                    difficulty = 8,
                    applicableRoles = setOf(CharacterRole.SOLDIER)
                ),
                riskLevelText = "Высокая сложность",
                actionIconKey = "construction",
                successOutcome = EventOutcome(
                    title = "Проход открыт!",
                    narrativeText = "С титаническим усилием бойцы сдвинули балку! В открывшемся коридоре обнаружен склад военной консервации.",
                    resourceRewards = mapOf(
                        ResourceType.MATERIALS to 40,
                        ResourceType.AMMO to 20,
                        ResourceType.RARE_ALLOY to 3
                    ),
                    xpReward = 50,
                    explorationProgressGain = 35,
                    discoveredAreaId = "area_bunker_deep"
                ),
                failureOutcome = EventOutcome(
                    title = "Обвал грунта",
                    narrativeText = "Опорная плита опасно треснула, подняв столб удушливой пыли. Проход остался заблокирован.",
                    xpReward = 15,
                    healthDelta = -8,
                    explorationProgressGain = 5
                )
            ),
            EventChoice(
                id = "choice_detour_scout",
                text = "Найти обходной технический лаз",
                description = "Разведчик обследует вентиляционные шахты в радиусе 50 метров.",
                skillCheck = SkillCheckRequirement(
                    statType = CharacterStatType.SCAVENGING,
                    difficulty = 6,
                    applicableRoles = setOf(CharacterRole.SCOUT)
                ),
                riskLevelText = "Средний шанс",
                actionIconKey = "explore",
                successOutcome = EventOutcome(
                    title = "Обход найден",
                    narrativeText = "Разведчик нашёл пожарный люк, ведущий прямо за линию завала!",
                    resourceRewards = mapOf(
                        ResourceType.MATERIALS to 20,
                        ResourceType.MONEY to 35
                    ),
                    xpReward = 35,
                    explorationProgressGain = 25
                ),
                failureOutcome = EventOutcome(
                    title = "Обходные пути затоплены",
                    narrativeText = "Технические люки оказались затоплены грязной водой. Пришлось отступить.",
                    xpReward = 10,
                    explorationProgressGain = 5
                )
            )
        )
    )

    // 7. Торговый автомат старого мира
    val eventVendingTerminal = ExpeditionEvent(
        id = "evt_vending_terminal",
        title = "Автономный торговый терминал",
        description = "В холле здания стоит бронированный платёжный автомат. Экран мерцает бледным зелёным светом, требуя авторизацию сервисного ключа.",
        category = EventCategory.DISCOVERY,
        rarity = EventRarity.UNCOMMON,
        baseWeight = 60,
        repeatMode = EventRepeatMode.ONCE_PER_LOCATION,
        visualAssetId = "evt_terminal",
        choices = listOf(
            EventChoice(
                id = "choice_hack_terminal",
                text = "Взломать сервисную прошивку",
                description = "Подключить диагностический планшет к шине данных автомата.",
                skillCheck = SkillCheckRequirement(
                    statType = CharacterStatType.ENGINEERING,
                    difficulty = 7,
                    applicableRoles = setOf(CharacterRole.ENGINEER)
                ),
                riskLevelText = "Инженерия 7",
                actionIconKey = "terminal",
                successOutcome = EventOutcome(
                    title = "Сервисный режим активирован",
                    narrativeText = "Автомат с приятным щелчком выдал весь лоток с энергоячейками и чипами старой валюты!",
                    resourceRewards = mapOf(
                        ResourceType.MONEY to 120,
                        ResourceType.COMPONENTS to 8,
                        ResourceType.FOOD to 10
                    ),
                    xpReward = 45,
                    explorationProgressGain = 20
                ),
                failureOutcome = EventOutcome(
                    title = "Блокировка защиты",
                    narrativeText = "Сработал протокол защиты от взлома. Конденсаторы разрядились, спалив плату памяти.",
                    xpReward = 15,
                    explorationProgressGain = 5
                )
            ),
            EventChoice(
                id = "choice_crowbar_terminal",
                text = "Вскрыть монтировкой нижний отсек",
                description = "Грубая сила против тонкого листового металла.",
                riskLevelText = "Низкий шанс ценного лута",
                actionIconKey = "hardware",
                successOutcome = EventOutcome(
                    title = "Лоток вскрыт",
                    narrativeText = "Удалось достать несколько смятых упаковок с сухим пайком и горсть кредитных монет.",
                    resourceRewards = mapOf(
                        ResourceType.MONEY to 35,
                        ResourceType.FOOD to 10
                    ),
                    xpReward = 20,
                    explorationProgressGain = 10
                )
            )
        )
    )

    // 8. Подозрительный шум в здании
    val eventMysteriousNoise = ExpeditionEvent(
        id = "evt_mysterious_noise",
        title = "Подозрительный шум в руинах",
        description = "Из глубины полуразрушенного административного корпуса доносится глухое рычание и скрежет когтей по бетону. Что-то крупное устроило здесь своё логово.",
        category = EventCategory.ENCOUNTER,
        rarity = EventRarity.COMMON,
        baseWeight = 85,
        minDangerLevel = DangerLevel.MODERATE,
        visualAssetId = "evt_noise",
        choices = listOf(
            EventChoice(
                id = "choice_tactical_ambush",
                text = "Занять боевые позиции и подготовиться к бою",
                description = "Штурмовики принимают бой на выгодных дистанциях.",
                riskLevelText = "Боевое столкновение",
                actionIconKey = "shield",
                successOutcome = EventOutcome(
                    title = "Боевая тревога!",
                    narrativeText = "Из темноты выскочила стая мутировавших псов! Отряд готов открыть огонь!",
                    requiresCombat = true,
                    xpReward = 25,
                    explorationProgressGain = 15
                )
            ),
            EventChoice(
                id = "choice_stealth_bypass",
                text = "Осторожно обойти логово по внешнему карнизу",
                description = "Разведчик ведёт группу в режиме скрытности.",
                skillCheck = SkillCheckRequirement(
                    statType = CharacterStatType.SCAVENGING,
                    difficulty = 6,
                    applicableRoles = setOf(CharacterRole.SCOUT)
                ),
                riskLevelText = "Скрытность",
                actionIconKey = "visibility_off",
                successOutcome = EventOutcome(
                    title = "Скрытный обход",
                    narrativeText = "Отряд бесшумно миновал логово хищников, попутно подобрав брошенный кем-то рюкзак.",
                    resourceRewards = mapOf(
                        ResourceType.MATERIALS to 20,
                        ResourceType.MONEY to 25
                    ),
                    xpReward = 35,
                    explorationProgressGain = 20
                ),
                failureOutcome = EventOutcome(
                    title = "Шум привлёк зверей!",
                    narrativeText = "Камень сорвался под ногой! Хищники заметили отряд!",
                    requiresCombat = true,
                    xpReward = 15,
                    explorationProgressGain = 10
                )
            )
        )
    )

    // 9. Следы группы людей
    val eventStrangerTracks = ExpeditionEvent(
        id = "evt_stranger_tracks",
        title = "Следы чужаков в секторе",
        description = "На песчаной косе видны чёткие следы армейских берцев и отпечатки колёс лёгкого мотоцикла. Судя по глубине следа, группа шла с тяжёлым грузом.",
        category = EventCategory.ENCOUNTER,
        rarity = EventRarity.COMMON,
        baseWeight = 80,
        visualAssetId = "evt_tracks",
        choices = listOf(
            EventChoice(
                id = "choice_inspect_tracks",
                text = "Проанализировать следы и тайник сброса",
                description = "Определить, куда направлялся отряд и не сбросили ли они балласт.",
                skillCheck = SkillCheckRequirement(
                    statType = CharacterStatType.SCAVENGING,
                    difficulty = 5,
                    applicableRoles = setOf(CharacterRole.SCOUT, CharacterRole.SCAVENGER)
                ),
                riskLevelText = "Низкий риск",
                actionIconKey = "manage_search",
                successOutcome = EventOutcome(
                    title = "Найден сброшенный груз",
                    narrativeText = "В кустах у развилки обнаружен ящик с консервами и фильтрами для воды, брошенный из-за поломки колеса!",
                    resourceRewards = mapOf(
                        ResourceType.FOOD to 25,
                        ResourceType.WATER to 25,
                        ResourceType.MATERIALS to 15
                    ),
                    xpReward = 35,
                    explorationProgressGain = 20,
                    setWorldFlags = mapOf("stranger_scout_intel" to true)
                ),
                failureOutcome = EventOutcome(
                    title = "Следы ведут в тупик",
                    narrativeText = "Ветер быстро замёл колею. Отряд не стал терять время на преследование.",
                    xpReward = 10,
                    explorationProgressGain = 10
                )
            )
        )
    )

    // 10. Древний промышленный станок
    val eventDerelictMachinery = ExpeditionEvent(
        id = "evt_derelict_machinery",
        title = "Прецизионный токарный станок",
        description = "В полузатопленном цехе завода уцелел тяжёлый фрезерно-токарный станок с числовым управлением. Блок шестерён и твердосплавные резцы не тронуты коррозией.",
        category = EventCategory.TECHNICAL,
        rarity = EventRarity.UNCOMMON,
        baseWeight = 70,
        repeatMode = EventRepeatMode.ONCE_PER_LOCATION,
        visualAssetId = "evt_machinery",
        choices = listOf(
            EventChoice(
                id = "choice_salvage_tooling",
                text = "Демонтировать фрезы и направляющие",
                description = "Аккуратно извлечь твердосплавную оснастку для мастерской поселения.",
                requirements = listOf(
                    EventRequirement.RequiresRole(CharacterRole.ENGINEER)
                ),
                riskLevelText = "Требуется инженер",
                actionIconKey = "handyman",
                successOutcome = EventOutcome(
                    title = "Инструментальная оснастка спасена",
                    narrativeText = "Инженер снял комплект высокоточных резцов и редких подшипников. Это значительно усилит производственную базу аванпоста!",
                    resourceRewards = mapOf(
                        ResourceType.COMPONENTS to 14,
                        ResourceType.MATERIALS to 50,
                        ResourceType.RARE_ALLOY to 2
                    ),
                    xpReward = 50,
                    explorationProgressGain = 25
                )
            ),
            EventChoice(
                id = "choice_quick_scrap",
                text = "Собрать крепёж и доступный металл",
                description = "Быстрый сбор легкодоступных болтов и стальных пластин.",
                riskLevelText = "Безопасно",
                actionIconKey = "build",
                successOutcome = EventOutcome(
                    title = "Металл собран",
                    narrativeText = "Бойцы наполнили рюкзаки болтами и стальными уголками.",
                    resourceRewards = mapOf(ResourceType.MATERIALS to 25),
                    xpReward = 20,
                    explorationProgressGain = 10
                )
            )
        )
    )

    // 11. Опустевший лагерь старателей
    val eventAbandonedCamp = ExpeditionEvent(
        id = "evt_abandoned_camp",
        title = "Опустевший лагерь старателей",
        description = "Потрёпанные палатки и складные стулья брошены в спешке. В центре лагеря лежит раскрытый полевой планшет с фрагментами топографических карт.",
        category = EventCategory.DISCOVERY,
        rarity = EventRarity.COMMON,
        baseWeight = 85,
        repeatMode = EventRepeatMode.ONCE_PER_LOCATION,
        visualAssetId = "evt_camp",
        choices = listOf(
            EventChoice(
                id = "choice_read_map",
                text = "Изучить топографический планшет",
                description = "Перенести отметки о полезных секторах на общую карту отряда.",
                skillCheck = SkillCheckRequirement(
                    statType = CharacterStatType.SCAVENGING,
                    difficulty = 5,
                    applicableRoles = setOf(CharacterRole.SCOUT)
                ),
                riskLevelText = "Разведка 5",
                actionIconKey = "map",
                successOutcome = EventOutcome(
                    title = "Карты скопированы",
                    narrativeText = "Планшет сохранил координаты закрытых складов и безопасных источников воды в этом районе!",
                    resourceRewards = mapOf(
                        ResourceType.MONEY to 40,
                        ResourceType.WATER to 20,
                        ResourceType.FOOD to 20
                    ),
                    xpReward = 40,
                    explorationProgressGain = 30,
                    setWorldFlags = mapOf("prospector_map_found" to true)
                ),
                failureOutcome = EventOutcome(
                    title = "Батарея села",
                    narrativeText = "Планшет отключился прямо во время чтения. Удалось спасти лишь часть припасов из палаток.",
                    resourceRewards = mapOf(ResourceType.FOOD to 10),
                    xpReward = 15,
                    explorationProgressGain = 10
                )
            )
        )
    )

    // 12. Полезная находка разведчика
    val eventScoutDiscovery = ExpeditionEvent(
        id = "evt_scout_discovery",
        title = "Наблюдательный пост разведчика",
        description = "Поднявшись на сохранившуюся водонапорную башню, разведчик обнаружил отличный сектор обзора всей прилегающей долины.",
        category = EventCategory.DISCOVERY,
        rarity = EventRarity.COMMON,
        baseWeight = 80,
        requirements = listOf(
            EventRequirement.RequiresRole(CharacterRole.SCOUT)
        ),
        visualAssetId = "evt_tower",
        choices = listOf(
            EventChoice(
                id = "choice_panoramic_recon",
                text = "Составить детальную схему сектора",
                description = "Нанести на карту все скрытые подходы и потенциальные угрозы.",
                riskLevelText = "Без риска (+30% прогресса)",
                actionIconKey = "visibility",
                successOutcome = EventOutcome(
                    title = "Сектор полностью нанесён на карту",
                    narrativeText = "Благодаря отличной позиции разведчик отметил безопасные маршруты и спрятанные контейнеры!",
                    resourceRewards = mapOf(
                        ResourceType.MATERIALS to 15,
                        ResourceType.MONEY to 25
                    ),
                    xpReward = 35,
                    explorationProgressGain = 30
                )
            )
        )
    )

    // 13. Техническое препятствие на пути транспорта
    val eventVehicleObstacle = ExpeditionEvent(
        id = "evt_vehicle_breakdown_obstacle",
        title = "Завал на главной дороге",
        description = "Упавшая опора ЛЭП перегородила проезд. Пеший отряд может протиснуться, но технике требуется расчистка дороги или объезд через овраг.",
        category = EventCategory.TECHNICAL,
        rarity = EventRarity.COMMON,
        baseWeight = 75,
        visualAssetId = "evt_obstacle",
        choices = listOf(
            EventChoice(
                id = "choice_winch_pull",
                text = "Оттащить опору лебёдкой транспорта",
                description = "Использовать тяговое усилие машины.",
                requirements = listOf(
                    EventRequirement.RequiresVehicle(true)
                ),
                costResources = mapOf(ResourceType.FUEL to 5),
                riskLevelText = "Расход 5 топлива",
                actionIconKey = "directions_car",
                successOutcome = EventOutcome(
                    title = "Дорога расчищена!",
                    narrativeText = "Мощный двигатель сдвинул балку в кювет. Путь для колонны полностью свободен!",
                    resourceRewards = mapOf(ResourceType.MATERIALS to 20),
                    xpReward = 30,
                    explorationProgressGain = 20
                )
            ),
            EventChoice(
                id = "choice_foot_bypass",
                text = "Обойти завал пешком по насыпи",
                description = "Продолжить разведку пешим строем.",
                riskLevelText = "Безопасно",
                actionIconKey = "directions_walk",
                successOutcome = EventOutcome(
                    title = "Обход выполнен",
                    narrativeText = "Отряд аккуратно преодолел насыпь и продолжил выполнение задачи.",
                    xpReward = 15,
                    explorationProgressGain = 10
                )
            )
        )
    )

    // 14. Безопасная точка обзора и привала
    val eventSafeVantagePoint = ExpeditionEvent(
        id = "evt_safe_vantage_point",
        title = "Защищённый наблюдательный пункт",
        description = "Сухой железобетонный ДОТ с целыми амбразурами и герметичной дверью предлагает идеальные условия для кратковременного привала отряда.",
        category = EventCategory.ENVIRONMENT,
        rarity = EventRarity.COMMON,
        baseWeight = 80,
        visualAssetId = "evt_shelter",
        choices = listOf(
            EventChoice(
                id = "choice_rest_morale",
                text = "Сделать привал, обработать раны и поесть",
                description = "Восстановить здоровье бойцов и поднять боевой дух.",
                costResources = mapOf(ResourceType.FOOD to 5, ResourceType.WATER to 5),
                riskLevelText = "Восстановление сил",
                actionIconKey = "restaurant",
                successOutcome = EventOutcome(
                    title = "Отряд отдохнул",
                    narrativeText = "Горячая еда и чистая вода вернули бойцам уверенность. Здоровье и мораль восстановлены!",
                    healthDelta = 15,
                    moraleDelta = 20,
                    xpReward = 20,
                    explorationProgressGain = 10
                )
            ),
            EventChoice(
                id = "choice_fortify_checkpoint",
                text = "Обустроить постоянный опорный пункт",
                description = "Оставить маяк связи для будущих экспедиций.",
                costResources = mapOf(ResourceType.MATERIALS to 10),
                riskLevelText = "Расход 10 материалов",
                actionIconKey = "cell_tower",
                successOutcome = EventOutcome(
                    title = "Опорный пункт развёрнут",
                    narrativeText = "Маяк стабильно передаёт телеметрию в Аванпост «Фронтир». Локация исследована намного быстрее!",
                    xpReward = 40,
                    explorationProgressGain = 35,
                    setWorldFlags = mapOf("outpost_relay_active" to true)
                )
            )
        )
    )

    // 15. Гермохранилище довоенного образца (Rare)
    val eventRareRelicVault = ExpeditionEvent(
        id = "evt_rare_relic_vault",
        title = "Гермохранилище спецрезерва",
        description = "За фальшстеной в подвале бункера обнаружен тяжёлый титановый сейф с электромагнитным замком. Индикаторы питания всё ещё светятся тусклым янтарным светом.",
        category = EventCategory.SPECIAL,
        rarity = EventRarity.RARE,
        baseWeight = 30,
        repeatMode = EventRepeatMode.GLOBAL_ONCE,
        visualAssetId = "evt_vault",
        choices = listOf(
            EventChoice(
                id = "choice_decrypt_vault",
                text = "Подобрать цифровой код дешифратором",
                description = "Сложная электронная расшифровка алгоритма шифрования.",
                skillCheck = SkillCheckRequirement(
                    statType = CharacterStatType.ENGINEERING,
                    difficulty = 9,
                    applicableRoles = setOf(CharacterRole.ENGINEER)
                ),
                riskLevelText = "Высокая сложность",
                actionIconKey = "lock_open",
                successOutcome = EventOutcome(
                    title = "Сейф открыт!",
                    narrativeText = "Титановые ригели с глухим стуком отошли! Внутри обнаружены редкие композитные сплавы, армейские аптечки и золотые сертификаты!",
                    resourceRewards = mapOf(
                        ResourceType.RARE_ALLOY to 10,
                        ResourceType.COMPONENTS to 20,
                        ResourceType.MEDICINE to 6,
                        ResourceType.MONEY to 250
                    ),
                    xpReward = 100,
                    explorationProgressGain = 50,
                    setWorldFlags = mapOf("vault_alpha_unlocked" to true)
                ),
                failureOutcome = EventOutcome(
                    title = "Сработала термоблокировка",
                    narrativeText = "Электроника расплавила внутренние платы при попытке взлома. Удалось достать лишь фрагменты редкого сплава.",
                    resourceRewards = mapOf(
                        ResourceType.RARE_ALLOY to 2,
                        ResourceType.COMPONENTS to 5
                    ),
                    xpReward = 30,
                    explorationProgressGain = 15
                )
            )
        )
    )

    // 16. Аварийный радиомаяк в руинах
    val eventDistressBeacon = ExpeditionEvent(
        id = "evt_distress_beacon",
        title = "Сигнал бедствия на аварийной частоте",
        description = "Радиостанция экспедиции поймала циклическую передачу: автоматический маяк передаёт сигнал SOS и координаты тайного медицинского схрона в соседнем секторе.",
        category = EventCategory.SPECIAL,
        rarity = EventRarity.UNCOMMON,
        baseWeight = 50,
        repeatMode = EventRepeatMode.GLOBAL_ONCE,
        visualAssetId = "evt_beacon",
        choices = listOf(
            EventChoice(
                id = "choice_decode_beacon",
                text = "Декодировать частоту и зафиксировать координаты",
                description = "Сохранить точку интереса на глобальной карте.",
                skillCheck = SkillCheckRequirement(
                    statType = CharacterStatType.ENGINEERING,
                    difficulty = 5,
                    applicableRoles = setOf(CharacterRole.SCOUT, CharacterRole.ENGINEER)
                ),
                riskLevelText = "Низкий риск",
                actionIconKey = "radio",
                successOutcome = EventOutcome(
                    title = "Координаты расшифрованы",
                    narrativeText = "Шифр успешно расшифрован. Командир получил точные данные о расположении законсервированного медицинского склада!",
                    resourceRewards = mapOf(
                        ResourceType.MEDICINE to 5,
                        ResourceType.MONEY to 50
                    ),
                    xpReward = 45,
                    explorationProgressGain = 25,
                    setWorldFlags = mapOf("distress_cache_revealed" to true)
                )
            )
        )
    )

    // 17. Случайная встреча с бродячим торговцем
    val eventNomadTraderEncounter = ExpeditionEvent(
        id = "evt_nomad_trader_encounter",
        title = "Встреча с кочевым караваном",
        description = "У ручья остановился фургон бродячего торговца по имени Салим. Он готов обменять редкие медикаменты и патроны на провизию и строительные материалы.",
        category = EventCategory.ENCOUNTER,
        rarity = EventRarity.UNCOMMON,
        baseWeight = 60,
        repeatMode = EventRepeatMode.REPEATABLE,
        visualAssetId = "evt_trader",
        choices = listOf(
            EventChoice(
                id = "choice_barter_meds",
                text = "Обменять 10 еды и 10 материалов на 4 медикамента",
                description = "Выгодный обмен дефицитных медицинских препаратов.",
                costResources = mapOf(
                    ResourceType.FOOD to 10,
                    ResourceType.MATERIALS to 10
                ),
                riskLevelText = "Бартер припасов",
                actionIconKey = "swap_horiz",
                successOutcome = EventOutcome(
                    title = "Сделка состоялась",
                    narrativeText = "Салим пожал руку командиру и передал герметичные стерильные упаковки антибиотиков и стимуляторов.",
                    resourceRewards = mapOf(
                        ResourceType.MEDICINE to 4,
                        ResourceType.AMMO to 10
                    ),
                    xpReward = 30,
                    explorationProgressGain = 15
                )
            ),
            EventChoice(
                id = "choice_chat_intel",
                text = "Расспросить торговца о новостях и картах пустоши",
                description = "Угостить торговца водой и узнать обстановку в регионе.",
                costResources = mapOf(ResourceType.WATER to 5),
                riskLevelText = "Расход 5 воды",
                actionIconKey = "forum",
                successOutcome = EventOutcome(
                    title = "Ценные слухи",
                    narrativeText = "Салим охотно поделился информацией о безопасных тропах и расположении банд мародёров.",
                    xpReward = 35,
                    explorationProgressGain = 30,
                    moraleDelta = 10
                )
            ),
            EventChoice(
                id = "choice_pass_trader",
                text = "Вежливо попрощаться и разойтись",
                description = "Продолжить исследование сектора без торговли.",
                riskLevelText = "Без риска",
                actionIconKey = "waving_hand",
                successOutcome = EventOutcome(
                    title = "Караван ушёл дальше",
                    narrativeText = "Отряд вернулся к исследованию местности.",
                    xpReward = 10,
                    explorationProgressGain = 10
                )
            )
        )
    )
}
