package com.example.domain.content.character

import com.example.domain.content.core.ContentGenerationContext
import com.example.domain.content.core.GameRandomProvider
import com.example.domain.content.core.GenerationResult
import com.example.domain.content.core.WeightedSelector
import com.example.domain.content.registry.GameContentRegistry
import com.example.domain.model.*
import kotlin.random.Random

/**
 * Procedural generator for settlement residents, recruits, and survivors.
 */
object CharacterGenerator {

    private val FIRST_NAMES_MALE = listOf(
        "Артем", "Максим", "Илья", "Денис", "Михаил", "Кирилл", "Роман", "Ярослав",
        "Сергей", "Павел", "Олег", "Игорь", "Тарас", "Владислав", "Евгений", "Антон"
    )

    private val FIRST_NAMES_FEMALE = listOf(
        "Анна", "Ольга", "Мария", "Дарья", "Валерия", "Ксения", "Алиса", "Татьяна",
        "Полина", "Елена", "Наталья", "Светлана", "Ирина", "Виктория", "Екатерина"
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

    /**
     * Generates a unique [Character] instance based on an archetype and context.
     */
    fun generateCharacter(
        archetype: CharacterArchetype,
        context: ContentGenerationContext,
        customIndex: Int? = null
    ): GenerationResult<Character> {
        val index = customIndex ?: context.generationIndex
        val seed = GameRandomProvider.deriveSeed(context.gameSeed, "character", archetype.id, index)
        val random = Random(seed)

        val isFemale = random.nextBoolean()
        val firstName = if (isFemale) {
            FIRST_NAMES_FEMALE[random.nextInt(FIRST_NAMES_FEMALE.size)]
        } else {
            FIRST_NAMES_MALE[random.nextInt(FIRST_NAMES_MALE.size)]
        }

        val hasCallsign = random.nextFloat() > 0.35f
        val callsign = if (hasCallsign) CALLSIGNS[random.nextInt(CALLSIGNS.size)] + " " else ""
        val lastName = LAST_NAMES[random.nextInt(LAST_NAMES.size)] + if (isFemale) "а" else ""
        val fullName = "$firstName $callsign$lastName"

        // 1. Distribute Stat Budget
        val totalBudget = if (archetype.minStatBudget >= archetype.maxStatBudget) {
            archetype.minStatBudget
        } else {
            random.nextInt(archetype.minStatBudget, archetype.maxStatBudget + 1)
        }

        val weights = archetype.statWeights
        val atkWeight = weights[CharacterStatType.ATTACK] ?: 0.2f
        val defWeight = weights[CharacterStatType.DEFENSE] ?: 0.2f
        val scavWeight = weights[CharacterStatType.SCAVENGING] ?: 0.2f
        val engWeight = weights[CharacterStatType.ENGINEERING] ?: 0.2f
        val medWeight = weights[CharacterStatType.MEDICAL] ?: 0.2f

        val baseAtk = (totalBudget * atkWeight).toInt().coerceAtLeast(5)
        val baseDef = (totalBudget * defWeight).toInt().coerceAtLeast(5)
        val baseScav = (totalBudget * scavWeight).toInt().coerceAtLeast(5)
        val baseEng = (totalBudget * engWeight).toInt().coerceAtLeast(5)
        val baseMed = (totalBudget * medWeight).toInt().coerceAtLeast(5)

        val stats = CharacterStats(
            attack = baseAtk + random.nextInt(-2, 3).coerceAtLeast(0),
            defense = baseDef + random.nextInt(-2, 3).coerceAtLeast(0),
            scavengingSkill = baseScav + random.nextInt(-2, 3).coerceAtLeast(0),
            engineeringSkill = baseEng + random.nextInt(-2, 3).coerceAtLeast(0),
            medicalSkill = baseMed + random.nextInt(-2, 3).coerceAtLeast(0)
        )

        // 2. Select Traits avoiding forbidden/conflicts
        val traitPool = TraitCatalog.ALL_TRAITS.filter { trait ->
            !archetype.forbiddenTraits.contains(trait.id)
        }
        val numTraits = random.nextInt(1, 3)
        val selectedTraits = WeightedSelector.selectMultipleWithoutReplacement(
            candidates = traitPool,
            count = numTraits,
            weightExtractor = { trait ->
                if (archetype.preferredTraits.contains(trait.id)) 3.0f else 1.0f
            },
            random = random
        )

        // 3. Bio & Specialization
        val bio = if (archetype.bioTemplates.isNotEmpty()) {
            archetype.bioTemplates[random.nextInt(archetype.bioTemplates.size)]
        } else {
            "Опытный выживший, присоединившийся к поселению."
        }

        val spec = if (archetype.specializations.isNotEmpty()) {
            archetype.specializations[random.nextInt(archetype.specializations.size)]
        } else {
            "Общий профиль"
        }

        val avatar = if (archetype.avatarTags.isNotEmpty()) {
            archetype.avatarTags[random.nextInt(archetype.avatarTags.size)]
        } else {
            archetype.role.name.lowercase()
        }

        val charId = "char_gen_${archetype.role.name.lowercase()}_$index"

        val character = Character(
            id = charId,
            name = fullName,
            role = archetype.role,
            level = 1,
            experience = 0,
            maxExperience = 100,
            health = 90 + (stats.defense * 2),
            maxHealth = 90 + (stats.defense * 2),
            status = CharacterStatus.READY,
            stats = stats,
            equipmentSummary = "Базовый комплект выживания",
            avatarTag = avatar,
            bio = bio,
            specialization = spec,
            unspentSkillPoints = 1,
            morale = 85 + random.nextInt(15),
            energy = 100,
            traits = selectedTraits,
            expeditionsCount = 0,
            daysInSettlement = 1,
            threatsNeutralizedCount = 0
        )

        return GenerationResult.Success(character)
    }

    /**
     * Generates a recruit candidate for the recruitment board.
     */
    fun generateRecruit(
        context: ContentGenerationContext,
        registry: GameContentRegistry = GameContentRegistry,
        role: CharacterRole? = null
    ): GenerationResult<Character> {
        val archetypes = if (role != null) {
            registry.characterArchetypes.values.filter { it.role == role }
        } else {
            registry.characterArchetypes.values.toList()
        }

        if (archetypes.isEmpty()) {
            return GenerationResult.NoEligibleContent("No archetype found for role $role")
        }

        val random = GameRandomProvider.createRandom(context.gameSeed, "recruit_select", context.generationIndex)
        val selectedArchetype = archetypes[random.nextInt(archetypes.size)]

        return generateCharacter(selectedArchetype, context)
    }
}
