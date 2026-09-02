package com.example.data

import com.example.domain.model.Character
import com.example.domain.model.CharacterRole
import com.example.domain.model.CharacterStats
import com.example.domain.model.CharacterStatus
import com.example.domain.model.ResourceType
import com.example.domain.model.TraitCatalog
import com.example.domain.service.CharacterProgressionService
import kotlin.random.Random

/**
 * Procedural generator and roster manager for settlement survivors/residents.
 * Generates thematic post-apocalyptic characters with authentic roles, equipment, and backstories.
 */
object SurvivorGenerator {

    val RECRUITMENT_COST: Map<ResourceType, Int> = mapOf(
        ResourceType.MONEY to 60,
        ResourceType.FOOD to 15,
        ResourceType.WATER to 15
    )

    private val FIRST_NAMES = listOf(
        "Артем", "Максим", "Илья", "Денис", "Михаил", "Кирилл", "Роман", "Ярослав",
        "Анна", "Ольга", "Мария", "Дарья", "Валерия", "Ксения", "Алиса", "Татьяна",
        "Сергей", "Павел", "Олег", "Игорь", "Тарас", "Владислав", "Евгений", "Полина"
    )

    private val CALLSIGNS = listOf(
        "«Коршун»", "«Шквал»", "«Кремень»", "«Призрак»", "«Свеча»", "«Игла»",
        "«Шепот»", "«Факел»", "«Буря»", "«Штурвал»", "«Ртуть»", "«Маяк»",
        "«Вектор»", "«Якорь»", "«Гранит»", "«Скала»", "«Компас»", "«Титан»"
    )

    private val LAST_NAMES = listOf(
        "Белов", "Морозов", "Волков", "Соловьев", "Васильев", "Зайцев", "Павлов",
        "Семенов", "Голубев", "Виноградов", "Богданов", "Воронов", "Кузнецов", "Орлов"
    )

    private val BIOS_BY_ROLE = mapOf(
        CharacterRole.SCOUT to listOf(
            "Бывший дальнобойщик пустоши, знающий каждый каньон и скрытый проход в заражённых секторах.",
            "Опытный следопыт из разрушенного северного форпоста. В темноте видит лучше, чем при свете дня.",
            "Снайпер-наблюдатель, проведший месяцы на радиомачтах в радиоактивной пыли."
        ),
        CharacterRole.SOLDIER to listOf(
            "Ветеран охраны торговых караванов. Привык держать оборону против стай мутантов.",
            "Бывший боец патрульной службы фронтира с тяжёлой модифицированной экипировкой.",
            "Бесстрашный штурмовик, выживший при осаде бункера «Омега»."
        ),
        CharacterRole.ENGINEER to listOf(
            "Умелец, способный собрать работающий генератор из консервных банок и медных проводов.",
            "Техник-механик, восстанавливавший бронетранспортёры и станки в подземных мастерских.",
            "Электронщик, специализирующийся на перепрошивке сенсоров и ремонте радиовышек."
        ),
        CharacterRole.MEDIC to listOf(
            "Полевой хирург, умеющий останавливать кровотечения в самых безнадёжных полевых условиях.",
            "Фармацевт и травник, умеющий синтезировать противорадиационные сыворотки из флоры пустоши.",
            "Бывший санитар экспедиционного корпуса, всегда готовый прикрыть раненых союзников."
        ),
        CharacterRole.SCAVENGER to listOf(
            "Мастер поиска тайников в полуразрушенных городах и заброшенных бункерах.",
            "Следопыт с чутьём на редкие микросхемы и уцелевшие контейнеры с провизией.",
            "Охотник за утилем и редкими сплавами, готовый залезть в самые опасные катакомбы."
        )
    )

    private val EQUIPMENT_BY_ROLE = mapOf(
        CharacterRole.SCOUT to listOf(
            "Снайперский карабин с оптикой, маскировочный плащ, дальномер",
            "Легкий пистолет-пулемёт, сканер движения, шлем с ПНВ",
            "Охотничье ружьё, компас, комплект сигнальных ракет"
        ),
        CharacterRole.SOLDIER to listOf(
            "Штурмовой автомат калибра 7.62, композитные бронепластины, шлем",
            "Дробовик с картечью, усиленный кевларовый бронежилет, разгрузка",
            "Ручной пулемёт, тактический щит, боевой нож"
        ),
        CharacterRole.ENGINEER to listOf(
            "Набор прецизионных инструментов, сварочный аппарат, защитные очки",
            "Ударный гайковёрт, мультиметр, защитный комбинезон химзащиты",
            "Плазменный резак утиля, тяжелые рукавицы, мини-паяльник"
        ),
        CharacterRole.MEDIC to listOf(
            "Полевой комплект первой помощи, стимуляторы, инжекторный пистолет",
            "Стерилизатор, набор хирургических зажимов, запас антибиотиков",
            "Дефибриллятор, противорадиационные ампулы, лёгкий бронежилет"
        ),
        CharacterRole.SCAVENGER to listOf(
            "Усиленный экспедиционный рюкзак, монтировка, металлоискатель",
            "Гидравлические кусачки, фонарь высокой мощности, респиратор",
            "Магнитный захват, сапёрная лопатка, дозиметр"
        )
    )

    private val SPECIALIZATIONS_BY_ROLE = mapOf(
        CharacterRole.SCOUT to listOf("Дальняя разведка", "Снайперская стрельба", "Картография пустоши"),
        CharacterRole.SOLDIER to listOf("Огневое прикрытие", "Ближний бой", "Оборона периметра"),
        CharacterRole.ENGINEER to listOf("Модернизация станков", "Ремонт транспорта", "Энергосети базы"),
        CharacterRole.MEDIC to listOf("Полевая реанимация", "Синтез медикаментов", "Лечение травм"),
        CharacterRole.SCAVENGER to listOf("Поиск тайников", "Демонтаж утиля", "Сбор редких сплавов")
    )

    /**
     * Generates a procedurally created survivor with balanced attributes.
     */
    fun generateSurvivor(
        id: String = "char_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}",
        level: Int = 1,
        forcedRole: CharacterRole? = null
    ): Character {
        val role = forcedRole ?: CharacterRole.entries.random()
        val firstName = FIRST_NAMES.random()
        val hasCallsign = Random.nextBoolean()
        val callsign = if (hasCallsign) " ${CALLSIGNS.random()} " else " "
        val lastName = LAST_NAMES.random()
        val fullName = "$firstName$callsign$lastName"

        val baseHp = when (role) {
            CharacterRole.SOLDIER -> 120 + (level - 1) * 15
            CharacterRole.SCOUT -> 95 + (level - 1) * 10
            CharacterRole.ENGINEER -> 100 + (level - 1) * 10
            CharacterRole.MEDIC -> 90 + (level - 1) * 10
            CharacterRole.SCAVENGER -> 105 + (level - 1) * 12
        }

        val stats = when (role) {
            CharacterRole.SOLDIER -> CharacterStats(
                attack = Random.nextInt(18, 24) + level * 2,
                defense = Random.nextInt(14, 18) + level * 2,
                scavengingSkill = Random.nextInt(5, 9),
                engineeringSkill = Random.nextInt(4, 8),
                medicalSkill = Random.nextInt(3, 7)
            )
            CharacterRole.SCOUT -> CharacterStats(
                attack = Random.nextInt(14, 18) + level * 2,
                defense = Random.nextInt(8, 12) + level,
                scavengingSkill = Random.nextInt(16, 22) + level * 2,
                engineeringSkill = Random.nextInt(5, 10),
                medicalSkill = Random.nextInt(5, 9)
            )
            CharacterRole.ENGINEER -> CharacterStats(
                attack = Random.nextInt(10, 14) + level,
                defense = Random.nextInt(10, 14) + level,
                scavengingSkill = Random.nextInt(10, 14) + level,
                engineeringSkill = Random.nextInt(18, 25) + level * 3,
                medicalSkill = Random.nextInt(5, 9)
            )
            CharacterRole.MEDIC -> CharacterStats(
                attack = Random.nextInt(8, 12) + level,
                defense = Random.nextInt(9, 13) + level,
                scavengingSkill = Random.nextInt(8, 13) + level,
                engineeringSkill = Random.nextInt(6, 10),
                medicalSkill = Random.nextInt(18, 25) + level * 3
            )
            CharacterRole.SCAVENGER -> CharacterStats(
                attack = Random.nextInt(12, 16) + level,
                defense = Random.nextInt(8, 12) + level,
                scavengingSkill = Random.nextInt(20, 26) + level * 3,
                engineeringSkill = Random.nextInt(8, 12) + level,
                medicalSkill = Random.nextInt(6, 10)
            )
        }

        val bio = BIOS_BY_ROLE[role]?.random() ?: "Опытный выживший фронтира."
        val equipment = EQUIPMENT_BY_ROLE[role]?.random() ?: "Походное снаряжение пустоши"
        val spec = SPECIALIZATIONS_BY_ROLE[role]?.random() ?: "Специалист"
        val avatarTag = when (role) {
            CharacterRole.SCOUT -> "scout"
            CharacterRole.SOLDIER -> "soldier"
            CharacterRole.ENGINEER -> "engineer"
            CharacterRole.MEDIC -> "medic"
            CharacterRole.SCAVENGER -> "scavenger"
        }

        // Generate 1-2 thematic traits
        val traitsCount = if (Random.nextFloat() < 0.35f) 2 else 1
        val traits = TraitCatalog.getRandomTraits(traitsCount)

        return Character(
            id = id,
            name = fullName,
            role = role,
            level = level,
            experience = 0,
            maxExperience = CharacterProgressionService.calculateMaxXpForLevel(level),
            health = baseHp,
            maxHealth = baseHp,
            status = CharacterStatus.READY,
            stats = stats,
            equipmentSummary = equipment,
            avatarTag = avatarTag,
            bio = bio,
            specialization = spec,
            unspentSkillPoints = 0,
            morale = Random.nextInt(75, 100),
            energy = 100,
            traits = traits,
            expeditionsCount = 0,
            daysInSettlement = 1,
            threatsNeutralizedCount = 0
        )
    }
}
