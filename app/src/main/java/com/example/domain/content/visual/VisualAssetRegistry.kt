package com.example.domain.content.visual

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import com.example.domain.model.CharacterRole
import com.example.domain.model.LocationType
import com.example.domain.model.ResourceType

/**
 * Authoritative Central Registry for all Visual Game Assets in Frontier Settlement.
 *
 * Keeps game logic strictly referencing stable `VisualAssetId` strings without direct
 * dependencies on Android `R.drawable` numeric constants.
 */
object VisualAssetRegistry {

    private const val MASTER_STYLE_PREFIX =
        "Frontier Settlement visual style: grounded semi-realistic 2D digital game illustration, " +
        "modern post-collapse environment, muted earthy palette, subtle cinematic lighting, " +
        "realistic proportions, slightly painterly texture, cohesive mobile strategy game art, no text, no logo, no UI"

    private val assetList: List<VisualAssetDefinition> = listOf(
        // ==========================================
        // 1. CHARACTER PORTRAITS (1:1, 768x768)
        // ==========================================
        VisualAssetDefinition(
            assetId = "char_portrait_scout_01",
            fileName = "char_portrait_scout_01.webp",
            category = VisualAssetCategory.CHARACTER_PORTRAIT,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Следопыт (Разведчик)",
            descriptionRu = "Внимательный разведчик в лёгкой штормовке, тактических очках и с рацией на плече.",
            fallbackIcon = Icons.Default.Visibility,
            fallbackColor = Color(0xFF38BDF8),
            recommendedResolution = "768x768",
            aspectRatio = "1:1",
            tags = listOf("scout", "survivor", "starter", "male"),
            screenUsage = listOf("CharactersScreen", "SquadScreen", "ExpeditionScreen", "CombatScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, waist-up character portrait of a gritty wasteland scout survivor, tactical dust goggles on forehead, weather-resistant hooded windbreaker, shoulder radio strap, keen focused expression, dark neutral textured background, soft directional cinematic lighting, square composition."
        ),
        VisualAssetDefinition(
            assetId = "char_portrait_soldier_01",
            fileName = "char_portrait_soldier_01.webp",
            category = VisualAssetCategory.CHARACTER_PORTRAIT,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Штурмовик (Защитник)",
            descriptionRu = "Закалённый боец в укреплённом бронежилете со строгим взглядом.",
            fallbackIcon = Icons.Default.Shield,
            fallbackColor = Color(0xFFE11D48),
            recommendedResolution = "768x768",
            aspectRatio = "1:1",
            tags = listOf("soldier", "combat", "starter", "male"),
            screenUsage = listOf("CharactersScreen", "SquadScreen", "CombatScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, waist-up character portrait of a battle-hardened survival soldier, modular reinforced ballistic vest, rugged tactical collar, stern scarred face, dark neutral textured background, dramatic rim lighting, square composition."
        ),
        VisualAssetDefinition(
            assetId = "char_portrait_engineer_01",
            fileName = "char_portrait_engineer_01.webp",
            category = VisualAssetCategory.CHARACTER_PORTRAIT,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Инженер-механик",
            descriptionRu = "Опытный техник в защитном комбинезоне с инструментами и сварочной маской на шее.",
            fallbackIcon = Icons.Default.Build,
            fallbackColor = Color(0xFF818CF8),
            recommendedResolution = "768x768",
            aspectRatio = "1:1",
            tags = listOf("engineer", "crafting", "starter", "female"),
            screenUsage = listOf("CharactersScreen", "WorkshopScreen", "SettlementScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, waist-up character portrait of a skilled settlement engineer, worn utility work overalls, tool pouches, welding goggles around neck, smudges of grease on cheek, determined calm expression, dark neutral textured background, square composition."
        ),
        VisualAssetDefinition(
            assetId = "char_portrait_medic_01",
            fileName = "char_portrait_medic_01.webp",
            category = VisualAssetCategory.CHARACTER_PORTRAIT,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Полевой медик",
            descriptionRu = "Врач в практичной полевой форме с медицинской сумкой с красным крестом.",
            fallbackIcon = Icons.Default.MedicalServices,
            fallbackColor = Color(0xFF10B981),
            recommendedResolution = "768x768",
            aspectRatio = "1:1",
            tags = listOf("medic", "support", "starter", "female"),
            screenUsage = listOf("CharactersScreen", "ClinicScreen", "SquadScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, waist-up character portrait of an emergency field medic, tactical medic vest with faded medical cross patch, clean sterile collar under worn jacket, compassionate yet resilient gaze, dark neutral textured background, square composition."
        ),
        VisualAssetDefinition(
            assetId = "char_portrait_scavenger_01",
            fileName = "char_portrait_scavenger_01.webp",
            category = VisualAssetCategory.CHARACTER_PORTRAIT,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Добытчик утиля",
            descriptionRu = "Находчивый собиратель ресурсов с многофункциональным рюкзаком и перчатками.",
            fallbackIcon = Icons.Default.ShoppingBag,
            fallbackColor = Color(0xFFFB923C),
            recommendedResolution = "768x768",
            aspectRatio = "1:1",
            tags = listOf("scavenger", "loot", "starter", "male"),
            screenUsage = listOf("CharactersScreen", "StorageScreen", "ExpeditionScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, waist-up character portrait of a wasteland scavenger, multi-pocket utility harness, reinforced leather gloves, respirator mask hanging loosely, resourceful crafty expression, dark textured background, square composition."
        ),
        VisualAssetDefinition(
            assetId = "char_portrait_sniper_01",
            fileName = "char_portrait_sniper_01.webp",
            category = VisualAssetCategory.CHARACTER_PORTRAIT,
            status = AssetStatus.PLACEHOLDER,
            priority = AssetPriority.B,
            titleRu = "Снайпер-наблюдатель",
            descriptionRu = "Терпеливый стрелок в маскировочной накидке с оптическим монокуляром.",
            fallbackIcon = Icons.Default.FilterCenterFocus,
            fallbackColor = Color(0xFF38BDF8),
            recommendedResolution = "768x768",
            aspectRatio = "1:1",
            tags = listOf("sniper", "scout", "advanced"),
            screenUsage = listOf("CharactersScreen", "CombatScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, waist-up character portrait of a patient wasteland sniper, ragged camouflage ghillie cape, high-tech monocular eyepiece, observant expression, dark textured background, square composition."
        ),
        VisualAssetDefinition(
            assetId = "char_portrait_mechanic_01",
            fileName = "char_portrait_mechanic_01.webp",
            category = VisualAssetCategory.CHARACTER_PORTRAIT,
            status = AssetStatus.PLACEHOLDER,
            priority = AssetPriority.B,
            titleRu = "Автомеханик",
            descriptionRu = "Специалист по ремонту двигателей и модификации вездеходов.",
            fallbackIcon = Icons.Default.Handyman,
            fallbackColor = Color(0xFFF59E0B),
            recommendedResolution = "768x768",
            aspectRatio = "1:1",
            tags = listOf("mechanic", "engineer", "vehicles"),
            screenUsage = listOf("TransportScreen", "WorkshopScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, waist-up character portrait of a vehicle mechanic in heavy canvas coveralls, adjustable wrench in chest pocket, grease stains, rugged confident smile, dark background, square composition."
        ),

        // ==========================================
        // 2. LOCATION HERO ARTWORKS (16:9, 1280x720)
        // ==========================================
        VisualAssetDefinition(
            assetId = "loc_base",
            fileName = "loc_settlement_outpost_01.webp",
            category = VisualAssetCategory.LOCATION_HERO,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Аванпост «Новый Рубеж»",
            descriptionRu = "Главная база игрока: укреплённый периметр, дозорная вышка, склад и ангары.",
            fallbackIcon = Icons.Default.Castle,
            fallbackColor = Color(0xFF10B981),
            recommendedResolution = "1280x720",
            aspectRatio = "16:9",
            tags = listOf("settlement", "base", "hub", "exterior"),
            screenUsage = listOf("SettlementScreen", "ArrivalScreen", "GlobalMapScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, wide atmospheric 2D digital concept illustration of a grounded modular post-collapse survivor settlement, fortified metal perimeter, observation watchtower with searchlight, modular residential domes, engineering workshop with warm glowing windows, muted earthy palette, cinematic overcast lighting, realistic proportions, 16:9 landscape aspect ratio."
        ),
        VisualAssetDefinition(
            assetId = "loc_trading_post",
            fileName = "loc_trading_outpost_01.webp",
            category = VisualAssetCategory.LOCATION_HERO,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Торговый караванный пост",
            descriptionRu = "Укреплённый рынок караванщиков под брезентовыми навесами и фонарями.",
            fallbackIcon = Icons.Default.Storefront,
            fallbackColor = Color(0xFFFACC15),
            recommendedResolution = "1280x720",
            aspectRatio = "16:9",
            tags = listOf("trading", "caravan", "market", "exterior"),
            screenUsage = listOf("TradeScreen", "ArrivalScreen", "GlobalMapScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, wide atmospheric 2D digital concept illustration of a fortified wasteland trading post, canvas canopies, merchant wagons, crates of scavenged supplies, lanterns casting warm glow, muted earthy palette, cinematic dusk lighting, 16:9 landscape aspect ratio."
        ),
        VisualAssetDefinition(
            assetId = "loc_industrial",
            fileName = "loc_industrial_plant_01.webp",
            category = VisualAssetCategory.LOCATION_HERO,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Заброшенный промкомбинат",
            descriptionRu = "Железобетонные цеха, ржавые краны и заваленные эстакады.",
            fallbackIcon = Icons.Default.Factory,
            fallbackColor = Color(0xFFFB923C),
            recommendedResolution = "1280x720",
            aspectRatio = "16:9",
            tags = listOf("industrial", "loot", "ruins", "exterior"),
            screenUsage = listOf("ArrivalScreen", "GlobalMapScreen", "CombatScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, wide atmospheric 2D digital concept illustration of an abandoned heavy industrial machinery plant, rusted gantry cranes, cracked concrete silos, overcast moody sky, weathered textures, muted earthy palette, 16:9 landscape aspect ratio."
        ),
        VisualAssetDefinition(
            assetId = "loc_forest",
            fileName = "loc_overgrown_forest_01.webp",
            category = VisualAssetCategory.LOCATION_HERO,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Заросший лесной массив",
            descriptionRu = "Густой сосновый бор в утреннем тумане с полуразрушенной сторожкой.",
            fallbackIcon = Icons.Default.Forest,
            fallbackColor = Color(0xFF22C55E),
            recommendedResolution = "1280x720",
            aspectRatio = "16:9",
            tags = listOf("forest", "wilderness", "nature", "exterior"),
            screenUsage = listOf("ArrivalScreen", "GlobalMapScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, wide atmospheric 2D digital concept illustration of a dense overgrown pine forest in a post-collapse world, morning mist filtering through tall trees, moss-covered abandoned ranger shack, muted earthy green palette, 16:9 landscape aspect ratio."
        ),
        VisualAssetDefinition(
            assetId = "loc_bunker",
            fileName = "loc_military_bunker_01.webp",
            category = VisualAssetCategory.LOCATION_HERO,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Загерметизированный бункер",
            descriptionRu = "Подземный военный комплекс с массивной приоткрытой гермодверью.",
            fallbackIcon = Icons.Default.Security,
            fallbackColor = Color(0xFFE11D48),
            recommendedResolution = "1280x720",
            aspectRatio = "16:9",
            tags = listOf("military", "bunker", "dungeon", "underground"),
            screenUsage = listOf("ArrivalScreen", "GlobalMapScreen", "CombatScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, wide atmospheric 2D digital concept illustration of a reinforced subterranean military bunker entrance, massive steel blast door slightly ajar, hazard yellow warning stripes faded by rust, emergency amber lights, 16:9 landscape aspect ratio."
        ),
        VisualAssetDefinition(
            assetId = "loc_anomaly",
            fileName = "loc_anomaly_zone_01.webp",
            category = VisualAssetCategory.LOCATION_HERO,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Энергетическая аномалия",
            descriptionRu = "Искажение гравитации и пространства с кристаллическими осколками и ионизацией.",
            fallbackIcon = Icons.Default.Diamond,
            fallbackColor = Color(0xFFA855F7),
            recommendedResolution = "1280x720",
            aspectRatio = "16:9",
            tags = listOf("anomaly", "energy", "danger", "exterior"),
            screenUsage = listOf("ArrivalScreen", "GlobalMapScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, wide atmospheric 2D digital concept illustration of a mysterious energy anomaly crater in the wasteland, glowing violet ion discharge, cracked irradiated ground, atmospheric haze, muted earthy palette with violet accents, 16:9 landscape aspect ratio."
        ),
        VisualAssetDefinition(
            assetId = "loc_station",
            fileName = "loc_abandoned_station_01.webp",
            category = VisualAssetCategory.LOCATION_HERO,
            status = AssetStatus.PLACEHOLDER,
            priority = AssetPriority.B,
            titleRu = "Узловая ж/д станция",
            descriptionRu = "Заброшенные железнодорожные пути, опрокинутые грузовые вагоны и полуразрушенный перрон.",
            fallbackIcon = Icons.Default.DirectionsTransit,
            fallbackColor = Color(0xFF38BDF8),
            recommendedResolution = "1280x720",
            aspectRatio = "16:9",
            tags = listOf("transit", "station", "trains", "exterior"),
            screenUsage = listOf("ArrivalScreen", "GlobalMapScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, wide atmospheric 2D digital concept illustration of an abandoned transit train depot, overgrown railway tracks, derailed rusted freight cars, concrete platform covered in weeds, overcast sky, 16:9 landscape aspect ratio."
        ),
        VisualAssetDefinition(
            assetId = "loc_farm",
            fileName = "loc_abandoned_farm_01.webp",
            category = VisualAssetCategory.LOCATION_HERO,
            status = AssetStatus.PLACEHOLDER,
            priority = AssetPriority.B,
            titleRu = "Заброшенная агроферма",
            descriptionRu = "Сухие поля, покосившийся элеватор и каркасы старых комбайнов.",
            fallbackIcon = Icons.Default.Agriculture,
            fallbackColor = Color(0xFFEAB308),
            recommendedResolution = "1280x720",
            aspectRatio = "16:9",
            tags = listOf("farm", "rural", "food", "exterior"),
            screenUsage = listOf("ArrivalScreen", "GlobalMapScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, wide atmospheric 2D digital concept illustration of a derelict agricultural farmland, rusted grain silo, dilapidated wooden barn, dead overgrown fields, windy overcast lighting, 16:9 landscape aspect ratio."
        ),
        VisualAssetDefinition(
            assetId = "loc_warehouse",
            fileName = "loc_logistics_warehouse_01.webp",
            category = VisualAssetCategory.LOCATION_HERO,
            status = AssetStatus.PLACEHOLDER,
            priority = AssetPriority.B,
            titleRu = "Логистический складской терминал",
            descriptionRu = "Ряды металлических ангаров, погрузочные рампы и штабели пустых контейнеров.",
            fallbackIcon = Icons.Default.Warehouse,
            fallbackColor = Color(0xFF64748B),
            recommendedResolution = "1280x720",
            aspectRatio = "16:9",
            tags = listOf("warehouse", "logistics", "loot", "exterior"),
            screenUsage = listOf("ArrivalScreen", "GlobalMapScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, wide atmospheric 2D digital concept illustration of a weathered commercial warehouse complex, corrugated metal storage units, rusted shipping containers stacked high, cracked asphalt yard, 16:9 landscape aspect ratio."
        ),
        VisualAssetDefinition(
            assetId = "loc_village",
            fileName = "loc_ruined_settlement_01.webp",
            category = VisualAssetCategory.LOCATION_HERO,
            status = AssetStatus.PLACEHOLDER,
            priority = AssetPriority.B,
            titleRu = "Разорённая деревня",
            descriptionRu = "Остатки деревянных домов, колодец и баррикады из покрышек.",
            fallbackIcon = Icons.Default.HomeWork,
            fallbackColor = Color(0xFFF97316),
            recommendedResolution = "1280x720",
            aspectRatio = "16:9",
            tags = listOf("village", "ruins", "housing", "exterior"),
            screenUsage = listOf("ArrivalScreen", "GlobalMapScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, wide atmospheric 2D digital concept illustration of a small deserted wasteland village, collapsed wooden huts, makeshift tire barricades, silent muddy road, dramatic cloudscape, 16:9 landscape aspect ratio."
        ),

        // ==========================================
        // 3. VEHICLES (1:1 / 3:2, Transparent BG)
        // ==========================================
        VisualAssetDefinition(
            assetId = "veh_foot",
            fileName = "veh_foot_scouts_01.webp",
            category = VisualAssetCategory.VEHICLE,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Пеший переход",
            descriptionRu = "Группа следопытов с тактическими рюкзаками и снаряжением.",
            fallbackIcon = Icons.Default.DirectionsWalk,
            fallbackColor = Color(0xFF94A3B8),
            recommendedResolution = "768x768",
            aspectRatio = "1:1",
            hasTransparency = true,
            tags = listOf("vehicle", "starter", "foot"),
            screenUsage = listOf("TransportScreen", "ExpeditionScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, isolated tactical game illustration of survival expedition gear: rugged backpack, hiking walking sticks, compass, canvas fabric, transparent background, clean silhouette, centered."
        ),
        VisualAssetDefinition(
            assetId = "veh_bike",
            fileName = "veh_cargo_bicycle_01.webp",
            category = VisualAssetCategory.VEHICLE,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Грузовой велобайк",
            descriptionRu = "Усиленный велосипед со стальной багажной корзиной и шипастыми шинами.",
            fallbackIcon = Icons.Default.PedalBike,
            fallbackColor = Color(0xFF38BDF8),
            recommendedResolution = "768x768",
            aspectRatio = "1:1",
            hasTransparency = true,
            tags = listOf("vehicle", "bike", "light"),
            screenUsage = listOf("TransportScreen", "ExpeditionScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, isolated 2D game illustration of a rugged cargo bicycle, heavy reinforced steel frame, front metal basket, rear pannier bags, knobby all-terrain tires, realistic wear, transparent background, three-quarter view, centered."
        ),
        VisualAssetDefinition(
            assetId = "veh_buggy",
            fileName = "veh_scout_buggy_01.webp",
            category = VisualAssetCategory.VEHICLE,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Разведывательный багги",
            descriptionRu = "Каркасный внедорожник с прожекторами и канистрами.",
            fallbackIcon = Icons.Default.DirectionsCar,
            fallbackColor = Color(0xFFF59E0B),
            recommendedResolution = "768x768",
            aspectRatio = "1:1",
            hasTransparency = true,
            tags = listOf("vehicle", "buggy", "medium"),
            screenUsage = listOf("TransportScreen", "ExpeditionScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, isolated 2D game illustration of a lightweight wasteland scout buggy, tubular roll cage, knobby suspension, roof spotlights, jerry cans mounted on rear rack, worn matte olive-drab paint, transparent background, three-quarter view, centered."
        ),
        VisualAssetDefinition(
            assetId = "veh_truck",
            fileName = "veh_heavy_truck_01.webp",
            category = VisualAssetCategory.VEHICLE,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Тяжёлый бронегрузовик",
            descriptionRu = "Шестиколёсный бронированный тягач для дальних экспедиций.",
            fallbackIcon = Icons.Default.LocalShipping,
            fallbackColor = Color(0xFFFB923C),
            recommendedResolution = "768x768",
            aspectRatio = "1:1",
            hasTransparency = true,
            tags = listOf("vehicle", "truck", "heavy"),
            screenUsage = listOf("TransportScreen", "ExpeditionScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, isolated 2D game illustration of a heavy armored 6x6 transport truck, reinforced steel grill guard, covered cargo bed with canvas tarp, rugged weathered plating, transparent background, three-quarter view, centered."
        ),

        // ==========================================
        // 4. ENEMIES & CREATURES (1:1, 768x768)
        // ==========================================
        VisualAssetDefinition(
            assetId = "enemy_raider",
            fileName = "enemy_raider_scout_01.webp",
            category = VisualAssetCategory.ENEMY,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Мародёр Пустошей",
            descriptionRu = "Вооружённый разбойник в кевларовой жилетке и пылезащитной маске.",
            fallbackIcon = Icons.Default.PersonOff,
            fallbackColor = Color(0xFFEF4444),
            recommendedResolution = "768x768",
            aspectRatio = "1:1",
            tags = listOf("enemy", "human", "raider"),
            screenUsage = listOf("CombatScreen", "EncounterScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, waist-up character portrait of a wasteland raider scout, patched kevlar vest, gas mask hanging on neck, weathered face, neutral textured dark background, soft directional cinematic lighting, square composition."
        ),
        VisualAssetDefinition(
            assetId = "enemy_mutant",
            fileName = "enemy_mutant_beast_01.webp",
            category = VisualAssetCategory.ENEMY,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Одичавший мутант",
            descriptionRu = "Агрессивный мутировавший хищник с ороговевшей кожей.",
            fallbackIcon = Icons.Default.Pets,
            fallbackColor = Color(0xFFE11D48),
            recommendedResolution = "768x768",
            aspectRatio = "1:1",
            tags = listOf("enemy", "beast", "mutant"),
            screenUsage = listOf("CombatScreen", "EncounterScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, waist-up creature portrait of a mutated wasteland hound predator, scarred leathery hide, glowing amber eyes, dark neutral textured background, soft cinematic rim lighting, square composition."
        ),
        VisualAssetDefinition(
            assetId = "enemy_drone",
            fileName = "enemy_security_drone_01.webp",
            category = VisualAssetCategory.ENEMY,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Охранный дрон",
            descriptionRu = "Автономный боевой квадрокоптер охранного периметра.",
            fallbackIcon = Icons.Default.Flight,
            fallbackColor = Color(0xFF38BDF8),
            recommendedResolution = "768x768",
            aspectRatio = "1:1",
            tags = listOf("enemy", "robot", "drone"),
            screenUsage = listOf("CombatScreen", "EncounterScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, isolated tactical game illustration of an automated military security drone, four ducted rotors, red optical scanning lens, matte black polymer chassis, transparent background, three-quarter view, centered."
        ),
        VisualAssetDefinition(
            assetId = "enemy_boss",
            fileName = "enemy_warlord_boss_01.webp",
            category = VisualAssetCategory.ENEMY,
            status = AssetStatus.PLACEHOLDER,
            priority = AssetPriority.B,
            titleRu = "Главарь банды (Босс)",
            descriptionRu = "Тяжеловооружённый предводитель мародёров в модифицированном экзокостюме.",
            fallbackIcon = Icons.Default.Warning,
            fallbackColor = Color(0xFFDC2626),
            recommendedResolution = "768x768",
            aspectRatio = "1:1",
            tags = listOf("enemy", "boss", "elite"),
            screenUsage = listOf("CombatScreen", "EncounterScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, waist-up character portrait of an intimidating wasteland warlord, improvised scrap power-armor collar, skull motif war-paint, imposing grim glare, dark textured background, dramatic lighting, square composition."
        ),

        // ==========================================
        // 5. ITEMS & EQUIPMENT (1:1, 512x512, Transparent)
        // ==========================================
        VisualAssetDefinition(
            assetId = "item_backpack_basic",
            fileName = "item_backpack_basic.webp",
            category = VisualAssetCategory.ITEM,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Простой рюкзак",
            descriptionRu = "Брезентовый походный рюкзак с боковыми карманами (+10 кг).",
            fallbackIcon = Icons.Default.Backpack,
            fallbackColor = Color(0xFF10B981),
            recommendedResolution = "512x512",
            aspectRatio = "1:1",
            hasTransparency = true,
            tags = listOf("item", "backpack", "starter"),
            screenUsage = listOf("StorageScreen", "EquipmentScreen", "TradeScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, isolated game item illustration of a rugged canvas survival backpack, olive green fabric, brass buckles, side utility pouches, clean silhouette, transparent background, three-quarter view, centered."
        ),
        VisualAssetDefinition(
            assetId = "item_backpack_tactical",
            fileName = "item_backpack_tactical.webp",
            category = VisualAssetCategory.ITEM,
            status = AssetStatus.PLACEHOLDER,
            priority = AssetPriority.A,
            titleRu = "Тактический рюкзак",
            descriptionRu = "Модульный рюкзак с системой строп MOLLE и креплением для карабинов (+20 кг).",
            fallbackIcon = Icons.Default.Backpack,
            fallbackColor = Color(0xFF38BDF8),
            recommendedResolution = "512x512",
            aspectRatio = "1:1",
            hasTransparency = true,
            tags = listOf("item", "backpack", "tactical"),
            screenUsage = listOf("StorageScreen", "EquipmentScreen", "TradeScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, isolated game item illustration of a tactical modular rucksack, MOLLE webbing straps, matte coyote tan cordura, hydration tube, clean silhouette, transparent background, centered."
        ),
        VisualAssetDefinition(
            assetId = "item_jacket_worn",
            fileName = "item_jacket_worn.webp",
            category = VisualAssetCategory.ITEM,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Потёртая куртка",
            descriptionRu = "Плотная кожаная куртка с нашивками для защиты от непогоды.",
            fallbackIcon = Icons.Default.Checkroom,
            fallbackColor = Color(0xFF10B981),
            recommendedResolution = "512x512",
            aspectRatio = "1:1",
            hasTransparency = true,
            tags = listOf("item", "armor", "body", "starter"),
            screenUsage = listOf("StorageScreen", "EquipmentScreen", "TradeScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, isolated game item illustration of a heavy weathered leather survivor jacket, reinforced canvas elbow patches, high collar, clean silhouette, transparent background, centered."
        ),
        VisualAssetDefinition(
            assetId = "item_armor_heavy",
            fileName = "item_armor_heavy.webp",
            category = VisualAssetCategory.ITEM,
            status = AssetStatus.PLACEHOLDER,
            priority = AssetPriority.A,
            titleRu = "Укреплённый бронежилет",
            descriptionRu = "Тяжёлый бронежилет со стальными пластинами для штурмовиков.",
            fallbackIcon = Icons.Default.Security,
            fallbackColor = Color(0xFFE11D48),
            recommendedResolution = "512x512",
            aspectRatio = "1:1",
            hasTransparency = true,
            tags = listOf("item", "armor", "heavy"),
            screenUsage = listOf("StorageScreen", "EquipmentScreen", "TradeScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, isolated game item illustration of a heavy modular body armor vest, welded steel plate inserts, tactical magazine pouches, dark slate color, transparent background, centered."
        ),
        VisualAssetDefinition(
            assetId = "item_boots_scout",
            fileName = "item_boots_scout.webp",
            category = VisualAssetCategory.ITEM,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Следопытские ботинки",
            descriptionRu = "Лёгкие треккинговые ботинки с амортизирующей подошвой.",
            fallbackIcon = Icons.Default.RollerSkating,
            fallbackColor = Color(0xFF10B981),
            recommendedResolution = "512x512",
            aspectRatio = "1:1",
            hasTransparency = true,
            tags = listOf("item", "boots", "feet", "starter"),
            screenUsage = listOf("StorageScreen", "EquipmentScreen", "TradeScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, isolated game item illustration of a pair of high-traction tactical hiking boots, reinforced rubber toe caps, worn leather uppers, transparent background, centered."
        ),
        VisualAssetDefinition(
            assetId = "item_multitool",
            fileName = "item_multitool.webp",
            category = VisualAssetCategory.ITEM,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Универсальный мультитул",
            descriptionRu = "Раскладной стальной инструмент: пассатижи, пилка, кусачки и отвёртки.",
            fallbackIcon = Icons.Default.Build,
            fallbackColor = Color(0xFF38BDF8),
            recommendedResolution = "512x512",
            aspectRatio = "1:1",
            hasTransparency = true,
            tags = listOf("item", "tool", "accessory"),
            screenUsage = listOf("StorageScreen", "EquipmentScreen", "WorkshopScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, isolated game item illustration of a rugged folding steel multitool pliers, polished titanium finish with realistic scuffs, transparent background, three-quarter view, centered."
        ),
        VisualAssetDefinition(
            assetId = "item_medkit",
            fileName = "item_medkit.webp",
            category = VisualAssetCategory.ITEM,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Полевая аптечка",
            descriptionRu = "Герметичный бокс с бинтами, антисептиками, обезболивающим и шприц-тюбиками.",
            fallbackIcon = Icons.Default.MedicalServices,
            fallbackColor = Color(0xFF10B981),
            recommendedResolution = "512x512",
            aspectRatio = "1:1",
            hasTransparency = true,
            tags = listOf("item", "medicine", "consumable"),
            screenUsage = listOf("StorageScreen", "EquipmentScreen", "TradeScreen", "CraftScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, isolated game item illustration of a tactical hard-case military medical kit, faded green plastic with red cross emblem, latches, transparent background, centered."
        ),

        // ==========================================
        // 6. BUILDINGS (1:1, 640x640)
        // ==========================================
        VisualAssetDefinition(
            assetId = "bld_hq_command",
            fileName = "bld_hq_command_01.webp",
            category = VisualAssetCategory.BUILDING,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Штаб поселения",
            descriptionRu = "Центральный командный модуль базы с радиомачтой и картой региона.",
            fallbackIcon = Icons.Default.AccountBalance,
            fallbackColor = Color(0xFF10B981),
            recommendedResolution = "640x640",
            aspectRatio = "1:1",
            tags = listOf("building", "hq", "core"),
            screenUsage = listOf("SettlementScreen", "BuildingsScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, isolated 2D game building illustration of a reinforced post-collapse settlement command center, communication dishes on roof, armored steel doors, soft warm window glow, dark neutral background, centered."
        ),
        VisualAssetDefinition(
            assetId = "bld_workshop",
            fileName = "bld_workshop_01.webp",
            category = VisualAssetCategory.BUILDING,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Мастерская утиля",
            descriptionRu = "Инженерный ангар с верстаками, тисками и сварочными аппаратами.",
            fallbackIcon = Icons.Default.Handyman,
            fallbackColor = Color(0xFFFB923C),
            recommendedResolution = "640x640",
            aspectRatio = "1:1",
            tags = listOf("building", "craft", "workshop"),
            screenUsage = listOf("SettlementScreen", "WorkshopScreen", "BuildingsScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, isolated 2D game building illustration of an industrial craft workshop hangar, ventilation pipes, tools and metalwork machinery visible inside, weathered metal siding, dark neutral background, centered."
        ),
        VisualAssetDefinition(
            assetId = "bld_storage_depot",
            fileName = "bld_storage_depot_01.webp",
            category = VisualAssetCategory.BUILDING,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Укреплённый склад",
            descriptionRu = "Складской модуль с усиленными гермоворотами и вентиляцией.",
            fallbackIcon = Icons.Default.Inventory2,
            fallbackColor = Color(0xFFA855F7),
            recommendedResolution = "640x640",
            aspectRatio = "1:1",
            tags = listOf("building", "storage", "logistics"),
            screenUsage = listOf("SettlementScreen", "StorageScreen", "BuildingsScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, isolated 2D game building illustration of a reinforced logistics supply warehouse, heavy cargo roll-up doors, stacked wooden crates on loading dock, dark neutral background, centered."
        ),

        // ==========================================
        // 7. EXPEDITION EVENTS (16:9, 1280x720)
        // ==========================================
        VisualAssetDefinition(
            assetId = "evt_warehouse",
            fileName = "evt_abandoned_warehouse_01.webp",
            category = VisualAssetCategory.EVENT,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Заброшенный склад",
            descriptionRu = "Ряды покосившихся стеллажей и полуоткрытые деревянные ящики.",
            fallbackIcon = Icons.Default.Inventory2,
            fallbackColor = Color(0xFFFB923C),
            recommendedResolution = "1280x720",
            aspectRatio = "16:9",
            tags = listOf("event", "loot", "indoor"),
            screenUsage = listOf("EventScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, wide atmospheric 2D digital concept illustration of an interior of an abandoned dark warehouse, dusty light beams breaking through ceiling holes, ransacked metal shelves, 16:9 aspect ratio."
        ),
        VisualAssetDefinition(
            assetId = "evt_campfire",
            fileName = "evt_nomad_campfire_01.webp",
            category = VisualAssetCategory.EVENT,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Ночной костёр бродяг",
            descriptionRu = "Теплящийся костёр в бочке, брезентовый навес и следы стоянки.",
            fallbackIcon = Icons.Default.LocalFireDepartment,
            fallbackColor = Color(0xFFF59E0B),
            recommendedResolution = "1280x720",
            aspectRatio = "16:9",
            tags = listOf("event", "social", "night"),
            screenUsage = listOf("EventScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, wide atmospheric 2D digital concept illustration of a makeshift survivor campsite at dusk, glowing campfire in a steel barrel, canvas shelter, shadows of resting wanderers, 16:9 aspect ratio."
        ),
        VisualAssetDefinition(
            assetId = "evt_cache",
            fileName = "evt_hidden_cache_01.webp",
            category = VisualAssetCategory.EVENT,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Замаскированный схрон",
            descriptionRu = "Тайник в каменной нише, прикрытый ветками и старым брезентом.",
            fallbackIcon = Icons.Default.Lock,
            fallbackColor = Color(0xFF10B981),
            recommendedResolution = "1280x720",
            aspectRatio = "16:9",
            tags = listOf("event", "cache", "loot"),
            screenUsage = listOf("EventScreen"),
            englishPrompt = "$MASTER_STYLE_PREFIX, wide atmospheric 2D digital concept illustration of a concealed survival supply cache hidden under camouflage netting and rock boulders, military ammo boxes, 16:9 aspect ratio."
        ),

        // ==========================================
        // 8. RESOURCE ICONS (Vector, Tintable)
        // ==========================================
        VisualAssetDefinition(
            assetId = "res_icon_food",
            fileName = "icon_resource_food.xml",
            category = VisualAssetCategory.RESOURCE_ICON,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Провизия",
            descriptionRu = "Иконка суточных рационов и продуктов.",
            fallbackIcon = Icons.Default.Restaurant,
            fallbackColor = Color(0xFF22C55E),
            hasTransparency = true,
            supportsTint = true,
            tags = listOf("resource", "food"),
            screenUsage = listOf("All")
        ),
        VisualAssetDefinition(
            assetId = "res_icon_water",
            fileName = "icon_resource_water.xml",
            category = VisualAssetCategory.RESOURCE_ICON,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Чистая вода",
            descriptionRu = "Иконка запасов питьевой воды.",
            fallbackIcon = Icons.Default.WaterDrop,
            fallbackColor = Color(0xFF06B6D4),
            hasTransparency = true,
            supportsTint = true,
            tags = listOf("resource", "water"),
            screenUsage = listOf("All")
        ),
        VisualAssetDefinition(
            assetId = "res_icon_fuel",
            fileName = "icon_resource_fuel.xml",
            category = VisualAssetCategory.RESOURCE_ICON,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Топливо",
            descriptionRu = "Иконка горючего для генератора и техники.",
            fallbackIcon = Icons.Default.LocalGasStation,
            fallbackColor = Color(0xFFF97316),
            hasTransparency = true,
            supportsTint = true,
            tags = listOf("resource", "fuel"),
            screenUsage = listOf("All")
        ),
        VisualAssetDefinition(
            assetId = "res_icon_materials",
            fileName = "icon_resource_materials.xml",
            category = VisualAssetCategory.RESOURCE_ICON,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Стройматериалы",
            descriptionRu = "Иконка металла, древесины и стройматериалов.",
            fallbackIcon = Icons.Default.SquareFoot,
            fallbackColor = Color(0xFFFB923C),
            hasTransparency = true,
            supportsTint = true,
            tags = listOf("resource", "materials"),
            screenUsage = listOf("All")
        ),
        VisualAssetDefinition(
            assetId = "res_icon_money",
            fileName = "icon_resource_credits.xml",
            category = VisualAssetCategory.RESOURCE_ICON,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.A,
            titleRu = "Кредиты / Талоны",
            descriptionRu = "Иконка универсальной валюты региона.",
            fallbackIcon = Icons.Default.MonetizationOn,
            fallbackColor = Color(0xFFFACC15),
            hasTransparency = true,
            supportsTint = true,
            tags = listOf("resource", "credits"),
            screenUsage = listOf("All")
        ),

        // ==========================================
        // 9. FACTION ICONS (1:1, Vector/Badge)
        // ==========================================
        VisualAssetDefinition(
            assetId = "fac_traders_guild",
            fileName = "icon_faction_traders.xml",
            category = VisualAssetCategory.FACTION_ICON,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.B,
            titleRu = "Гильдия Караванщиков",
            descriptionRu = "Торговцы и караванщики пустошей.",
            fallbackIcon = Icons.Default.Storefront,
            fallbackColor = Color(0xFFFACC15),
            tags = listOf("faction", "trade"),
            screenUsage = listOf("ReputationScreen", "TradeScreen")
        ),
        VisualAssetDefinition(
            assetId = "fac_tech_preservers",
            fileName = "icon_faction_technocrats.xml",
            category = VisualAssetCategory.FACTION_ICON,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.B,
            titleRu = "Хранители Технологий",
            descriptionRu = "Учёные и инженеры старого мира.",
            fallbackIcon = Icons.Default.Science,
            fallbackColor = Color(0xFF38BDF8),
            tags = listOf("faction", "tech"),
            screenUsage = listOf("ReputationScreen", "ResearchScreen")
        ),
        VisualAssetDefinition(
            assetId = "fac_free_scavengers",
            fileName = "icon_faction_scavengers.xml",
            category = VisualAssetCategory.FACTION_ICON,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.B,
            titleRu = "Вольные Старатели",
            descriptionRu = "Независимые группы сталкеров и добытчиков утиля.",
            fallbackIcon = Icons.Default.Explore,
            fallbackColor = Color(0xFFFB923C),
            tags = listOf("faction", "scavenger"),
            screenUsage = listOf("ReputationScreen")
        ),
        VisualAssetDefinition(
            assetId = "fac_settlers_union",
            fileName = "icon_faction_settlers.xml",
            category = VisualAssetCategory.FACTION_ICON,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.B,
            titleRu = "Союз Поселенцев",
            descriptionRu = "Кооперация мирных агро-поселений региона.",
            fallbackIcon = Icons.Default.Groups,
            fallbackColor = Color(0xFF10B981),
            tags = listOf("faction", "settlers"),
            screenUsage = listOf("ReputationScreen")
        ),
        VisualAssetDefinition(
            assetId = "fac_enforcers",
            fileName = "icon_faction_enforcers.xml",
            category = VisualAssetCategory.FACTION_ICON,
            status = AssetStatus.APPROVED,
            priority = AssetPriority.B,
            titleRu = "Служба Порядка",
            descriptionRu = "Милитаризованная охрана порядка и закона.",
            fallbackIcon = Icons.Default.Gavel,
            fallbackColor = Color(0xFFE11D48),
            tags = listOf("faction", "order"),
            screenUsage = listOf("ReputationScreen")
        )
    )

    private val assetMap: Map<String, VisualAssetDefinition> = assetList.associateBy { it.assetId }

    /**
     * Map of Category -> Fallback AssetDefinition
     */
    private val categoryFallbacks: Map<VisualAssetCategory, VisualAssetDefinition> by lazy {
        mapOf(
            VisualAssetCategory.CHARACTER_PORTRAIT to (assetMap["char_portrait_scout_01"] ?: assetList.first()),
            VisualAssetCategory.LOCATION_HERO to (assetMap["loc_base"] ?: assetList.first()),
            VisualAssetCategory.LOCATION_THUMBNAIL to (assetMap["loc_base"] ?: assetList.first()),
            VisualAssetCategory.VEHICLE to (assetMap["veh_foot"] ?: assetList.first()),
            VisualAssetCategory.ENEMY to (assetMap["enemy_raider"] ?: assetList.first()),
            VisualAssetCategory.ITEM to (assetMap["item_backpack_basic"] ?: assetList.first()),
            VisualAssetCategory.BUILDING to (assetMap["bld_hq_command"] ?: assetList.first()),
            VisualAssetCategory.EVENT to (assetMap["evt_warehouse"] ?: assetList.first()),
            VisualAssetCategory.RESOURCE_ICON to (assetMap["res_icon_materials"] ?: assetList.first()),
            VisualAssetCategory.FACTION_ICON to (assetMap["fac_settlers_union"] ?: assetList.first()),
            VisualAssetCategory.MAP_MARKER to (assetMap["loc_base"] ?: assetList.first()),
            VisualAssetCategory.STATUS_ICON to (assetMap["res_icon_materials"] ?: assetList.first()),
            VisualAssetCategory.UI_DECORATION to (assetMap["loc_base"] ?: assetList.first()),
            VisualAssetCategory.BACKGROUND to (assetMap["loc_base"] ?: assetList.first())
        )
    }

    // Direct Lookup Helpers
    fun getDefinition(assetId: String?): VisualAssetDefinition? {
        if (assetId.isNullOrBlank()) return null
        return assetMap[assetId]
    }

    fun getDefinitionOrFallback(assetId: String?, category: VisualAssetCategory): VisualAssetDefinition {
        return getDefinition(assetId) ?: getCategoryFallback(category)
    }

    fun getCategoryFallback(category: VisualAssetCategory): VisualAssetDefinition {
        return categoryFallbacks[category] ?: assetList.first()
    }

    fun getAllDefinitions(): List<VisualAssetDefinition> = assetList

    fun getDefinitionsByCategory(category: VisualAssetCategory): List<VisualAssetDefinition> =
        assetList.filter { it.category == category }

    fun getDefinitionsByPriority(priority: AssetPriority): List<VisualAssetDefinition> =
        assetList.filter { it.priority == priority }

    fun getDefinitionsByStatus(status: AssetStatus): List<VisualAssetDefinition> =
        assetList.filter { it.status == status }

    // Helpers for domain mappings
    fun getPortraitAssetIdForRole(role: CharacterRole): String = when (role) {
        CharacterRole.SCOUT -> "char_portrait_scout_01"
        CharacterRole.SOLDIER -> "char_portrait_soldier_01"
        CharacterRole.ENGINEER -> "char_portrait_engineer_01"
        CharacterRole.MEDIC -> "char_portrait_medic_01"
        CharacterRole.SCAVENGER -> "char_portrait_scavenger_01"
    }

    fun getHeroAssetIdForLocationType(type: LocationType): String = when (type) {
        LocationType.SETTLEMENT -> "loc_base"
        LocationType.TRADING_POST -> "loc_trading_post"
        LocationType.INDUSTRIAL_PLANT -> "loc_industrial"
        LocationType.FOREST -> "loc_forest"
        LocationType.MILITARY_BUNKER -> "loc_bunker"
        LocationType.ANOMALY_ZONE -> "loc_anomaly"
        LocationType.ABANDONED_STATION -> "loc_station"
        LocationType.FARM -> "loc_farm"
        LocationType.WAREHOUSE_COMPLEX -> "loc_warehouse"
        LocationType.VILLAGE -> "loc_village"
        LocationType.CITY_RUINS -> "loc_industrial"
    }

    fun getResourceAssetId(type: ResourceType): String = when (type) {
        ResourceType.FOOD -> "res_icon_food"
        ResourceType.WATER -> "res_icon_water"
        ResourceType.FUEL -> "res_icon_fuel"
        ResourceType.MATERIALS -> "res_icon_materials"
        ResourceType.MONEY -> "res_icon_money"
        ResourceType.MEDICINE -> "res_icon_food" // fallback
        ResourceType.AMMO -> "res_icon_materials" // fallback
        ResourceType.COMPONENTS -> "res_icon_materials" // fallback
        ResourceType.RARE_ALLOY -> "res_icon_materials" // fallback
    }
}
