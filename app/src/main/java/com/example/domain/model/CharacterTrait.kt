package com.example.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

/**
 * Category of a character's unique psychological, physical, or tactical trait.
 */
enum class TraitCategory(val titleRu: String, val icon: String) {
    COMBAT("Боевая", "⚔️"),
    SURVIVAL("Выживание", "🛡️"),
    TECHNICAL("Техническая", "🔧"),
    MENTAL("Психология", "🧠"),
    EXPLORATION("Исследования", "🧭")
}

/**
 * Character stat types available for upgrade or trait modification.
 */
enum class CharacterStatType(val titleRu: String, val icon: String) {
    ATTACK("Атака", "⚔️"),
    DEFENSE("Защита", "🛡️"),
    SCAVENGING("Добыча", "🎒"),
    ENGINEERING("Инженерия", "🔧"),
    MEDICAL("Медицина", "💉"),
    MAX_HEALTH("Запас сил (HP)", "❤️")
}

/**
 * Domain model representing a unique perk or character quirk.
 */
data class CharacterTrait(
    val id: String,
    val name: String,
    val description: String,
    val category: TraitCategory,
    val isPositive: Boolean = true,
    val bonusAttack: Int = 0,
    val bonusDefense: Int = 0,
    val bonusScavenging: Int = 0,
    val bonusEngineering: Int = 0,
    val bonusMedical: Int = 0,
    val bonusMaxHealth: Int = 0,
    val effectSummary: String = "",
    val iconKey: String = "trait_generic"
) {
    val categoryColor: Color
        get() = when (category) {
            TraitCategory.COMBAT -> DangerCrimson
            TraitCategory.SURVIVAL -> SafeEmerald
            TraitCategory.TECHNICAL -> WarningAmber
            TraitCategory.MENTAL -> TechCyan
            TraitCategory.EXPLORATION -> MaterialsOrange
        }
}

/**
 * Catalog of predefined authentic traits for survivors in Frontier Settlement.
 */
object TraitCatalog {

    val EAGLE_EYE = CharacterTrait(
        id = "trait_eagle_eye",
        name = "Орлиный глаз",
        description = "Исключительная наблюдательность и реакция при обнаружении засад на дальних дистанциях.",
        category = TraitCategory.EXPLORATION,
        bonusAttack = 2,
        bonusScavenging = 4,
        effectSummary = "+4 Добыча, +2 Атака, снижение риска засады",
        iconKey = "visibility"
    )

    val IRON_NERVES = CharacterTrait(
        id = "trait_iron_nerves",
        name = "Железные нервы",
        description = "Не теряет хладнокровия под шквальным огнем рейдеров и в аномальных зонах.",
        category = TraitCategory.MENTAL,
        bonusDefense = 3,
        bonusAttack = 2,
        effectSummary = "+3 Защита, +2 Атака, высокая стойкость морали",
        iconKey = "psychology"
    )

    val SCRAP_HOARDER = CharacterTrait(
        id = "trait_scrap_hoarder",
        name = "Мастер утиля",
        description = "Находит полезные детали и редкие сплавы даже в полностью выжженных секторах.",
        category = TraitCategory.TECHNICAL,
        bonusScavenging = 6,
        bonusEngineering = 2,
        effectSummary = "+6 Добыча, +2 Инженерия",
        iconKey = "precision_manufacturing"
    )

    val COMBAT_MEDIC = CharacterTrait(
        id = "trait_combat_medic",
        name = "Полевой перевязчик",
        description = "Владеет техникой быстрой остановки артериальных кровотечений в боевых условиях.",
        category = TraitCategory.SURVIVAL,
        bonusMedical = 6,
        bonusMaxHealth = 10,
        effectSummary = "+6 Медицина, +10 Макс. HP",
        iconKey = "medical_services"
    )

    val TANK_BUILD = CharacterTrait(
        id = "trait_tank_build",
        name = "Крепкое телосложение",
        description = "Высокая природная выносливость и способность выдерживать физические перегрузки.",
        category = TraitCategory.SURVIVAL,
        bonusDefense = 4,
        bonusMaxHealth = 25,
        effectSummary = "+25 Макс. HP, +4 Защита",
        iconKey = "fitness_center"
    )

    val TACTICAL_SNIPER = CharacterTrait(
        id = "trait_tactical_sniper",
        name = "Снайперская выдержка",
        description = "Привык работать из укрытий, нанося точечные критические выстрелы по уязвимым узлам.",
        category = TraitCategory.COMBAT,
        bonusAttack = 5,
        bonusDefense = 1,
        effectSummary = "+5 Атака, +1 Защита",
        iconKey = "gps_fixed"
    )

    val GREASE_MONKEY = CharacterTrait(
        id = "trait_grease_monkey",
        name = "Техник-самоучка",
        description = "Интуитивно понимает устройство любых дизелей, генераторов и электронных плат.",
        category = TraitCategory.TECHNICAL,
        bonusEngineering = 7,
        effectSummary = "+7 Инженерия, ускоренный ремонт транспорта",
        iconKey = "handyman"
    )

    val NIGHT_STALKER = CharacterTrait(
        id = "trait_night_stalker",
        name = "Ночной сталкер",
        description = "Бесшумно ориентируется в темноте и развалинах при полном отсутствии освещения.",
        category = TraitCategory.EXPLORATION,
        bonusScavenging = 3,
        bonusAttack = 3,
        effectSummary = "+3 Добыча, +3 Атака в ночных операциях",
        iconKey = "nights_stay"
    )

    val RADIATION_RESISTANT = CharacterTrait(
        id = "trait_rad_resist",
        name = "Стойкий иммунитет",
        description = "Организм выработал частичный иммунитет к радиационному фону и токсичным спорам.",
        category = TraitCategory.SURVIVAL,
        bonusMaxHealth = 15,
        bonusDefense = 2,
        effectSummary = "+15 HP, повышенная выживаемость в заражённых секторах",
        iconKey = "shield"
    )

    val INSPIRING_LEADER = CharacterTrait(
        id = "trait_inspiring_leader",
        name = "Прирождённый лидер",
        description = "Вдохновляет товарищей по отряду, повышая общую боеготовность и слаженность действий.",
        category = TraitCategory.MENTAL,
        bonusAttack = 2,
        bonusDefense = 2,
        bonusScavenging = 2,
        effectSummary = "+2 ко всем параметрам, бонус к морали группы",
        iconKey = "military_tech"
    )

    val CAUTIOUS_PACER = CharacterTrait(
        id = "trait_cautious_pacer",
        name = "Осторожная поступь",
        description = "Никогда не спешит, проверяет каждый шаг щупом и снижает шанс попадания в растяжки.",
        category = TraitCategory.SURVIVAL,
        bonusDefense = 3,
        bonusScavenging = 2,
        effectSummary = "+3 Защита, +2 Добыча",
        iconKey = "security"
    )

    val GUNSMITH = CharacterTrait(
        id = "trait_gunsmith",
        name = "Оружейных дел мастер",
        description = "Знает балансировку затворов и полировку стволов, повышая огневую мощь любого оружия.",
        category = TraitCategory.COMBAT,
        bonusAttack = 4,
        bonusEngineering = 3,
        effectSummary = "+4 Атака, +3 Инженерия",
        iconKey = "construction"
    )

    val ALL_TRAITS: List<CharacterTrait> = listOf(
        EAGLE_EYE,
        IRON_NERVES,
        SCRAP_HOARDER,
        COMBAT_MEDIC,
        TANK_BUILD,
        TACTICAL_SNIPER,
        GREASE_MONKEY,
        NIGHT_STALKER,
        RADIATION_RESISTANT,
        INSPIRING_LEADER,
        CAUTIOUS_PACER,
        GUNSMITH
    )

    fun getTraitById(id: String): CharacterTrait? {
        return ALL_TRAITS.find { it.id == id }
    }

    fun getRandomTraits(count: Int = 1, preferredCategory: TraitCategory? = null): List<CharacterTrait> {
        val pool = if (preferredCategory != null) {
            val matching = ALL_TRAITS.filter { it.category == preferredCategory }
            if (matching.isNotEmpty()) matching else ALL_TRAITS
        } else {
            ALL_TRAITS
        }
        return pool.shuffled().take(count.coerceAtLeast(1))
    }
}
