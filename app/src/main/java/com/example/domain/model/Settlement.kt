package com.example.domain.model

enum class SettlementTier(
    val titleRu: String,
    val minLevel: Int,
    val perkDescription: String
) {
    SURVIVOR_CAMP(
        titleRu = "Лагерь выживших",
        minLevel = 1,
        perkDescription = "Базовые постройки выживания, скромные запасы"
    ),
    SECURED_OUTPOST(
        titleRu = "Укреплённый форпост",
        minLevel = 2,
        perkDescription = "Открыты радиовышка и торговый караван, усилен периметр"
    ),
    FRONTIER_SETTLEMENT(
        titleRu = "Фронтирное поселение",
        minLevel = 4,
        perkDescription = "Открыта оружейная лаборатория, +30% к торговле и притоку жителей"
    ),
    REGIONAL_HAVEN(
        titleRu = "Региональный центр",
        minLevel = 6,
        perkDescription = "Автономный биокупол, максимальная защита и влияние в пустоши"
    );

    companion object {
        fun fromLevel(level: Int): SettlementTier {
            return when {
                level >= REGIONAL_HAVEN.minLevel -> REGIONAL_HAVEN
                level >= FRONTIER_SETTLEMENT.minLevel -> FRONTIER_SETTLEMENT
                level >= SECURED_OUTPOST.minLevel -> SECURED_OUTPOST
                else -> SURVIVOR_CAMP
            }
        }
    }
}

data class Settlement(
    val name: String = "Аванпост-7",
    val level: Int = 1,
    val xp: Int = 40,
    val xpToNextLevel: Int = 200,
    val reputation: Int = 45,
    val population: Int = 18,
    val maxPopulation: Int = 30,
    val defenseRating: Int = 65,
    val tier: SettlementTier = SettlementTier.SURVIVOR_CAMP,
    val buildings: List<Building> = emptyList(),
    val dailyFoodConsumption: Int = 18,
    val dailyWaterConsumption: Int = 20
) {
    val xpProgressFraction: Float
        get() = (xp.toFloat() / xpToNextLevel.toFloat()).coerceIn(0f, 1f)

    val constructedBuildingsCount: Int
        get() = buildings.count { it.isConstructed }

    val totalBuildingLevels: Int
        get() = buildings.filter { it.isConstructed }.sumOf { it.level }

    val availableToBuildCount: Int
        get() = buildings.count { it.status == BuildingStatus.AVAILABLE_TO_BUILD }

    /**
     * Adds XP and handles potential level-ups and tier advancement.
     */
    fun addXp(amount: Int): Pair<Settlement, Boolean> {
        var currentLevel = level
        var currentXp = xp + amount
        var neededXp = xpToNextLevel
        var leveledUp = false

        while (currentXp >= neededXp) {
            currentXp -= neededXp
            currentLevel += 1
            neededXp = calculateXpForLevel(currentLevel)
            leveledUp = true
        }

        val updatedTier = SettlementTier.fromLevel(currentLevel)
        val updatedSettlement = copy(
            level = currentLevel,
            xp = currentXp,
            xpToNextLevel = neededXp,
            tier = updatedTier
        )

        return Pair(updatedSettlement, leveledUp)
    }

    companion object {
        fun calculateXpForLevel(level: Int): Int {
            return 150 + (level * 75)
        }
    }
}

