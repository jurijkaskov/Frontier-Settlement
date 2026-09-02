package com.example.domain.model

enum class BuildingType(val titleRu: String) {
    HQ_COMMAND("Штаб поселения"),
    HYDROPONICS_FARM("Гидропонная ферма"),
    WATER_EXTRACTOR("Скважинный фильтр"),
    GENERATOR_STATION("Дизель-генератор"),
    WORKSHOP("Мастерская утиля"),
    STORAGE_DEPOT("Укреплённый склад"),
    MEDICAL_CLINIC("Полевой медпункт"),
    DEFENSE_PERIMETER("Сторожевые вышки"),
    RADIO_TOWER("Радиовышка дальней связи"),
    TRADING_POST("Торговый караван-сарай"),
    RESEARCH_LAB("Исследовательский центр"),
    ARMORY_LAB("Оружейная лаборатория"),
    GREENHOUSE_COMPLEX("Автономный биокупол")
}

enum class BuildingCategory(val titleRu: String) {
    PRODUCTION("Производство"),
    SURVIVAL_DEFENSE("Жизнеобеспечение и защита"),
    MANAGEMENT_LOGISTICS("Логистика и управление")
}

enum class BuildingStatus(val titleRu: String) {
    OPERATIONAL("Работает"),
    AVAILABLE_TO_BUILD("Готово к постройке"),
    LOCKED("Заблокировано"),
    UPGRADING("Модернизация"),
    DAMAGED("Повреждено")
}

data class Building(
    val id: String,
    val name: String,
    val type: BuildingType,
    val category: BuildingCategory = BuildingCategory.PRODUCTION,
    val level: Int = 1,
    val maxLevel: Int = 5,
    val description: String,
    val status: BuildingStatus = BuildingStatus.OPERATIONAL,
    val requiredSettlementLevel: Int = 1,
    val buildCostMaterials: Int = 100,
    val buildCostMoney: Int = 150,
    val upgradeCostMaterials: Int = 100,
    val upgradeCostMoney: Int = 200,
    val xpRewardOnBuild: Int = 100,
    val xpRewardOnUpgrade: Int = 50,
    val dailyProductionDescription: String = "",
    val bonusSummary: String = "",
    val iconKey: String = "building"
) {
    val isConstructed: Boolean
        get() = level > 0 && status != BuildingStatus.LOCKED && status != BuildingStatus.AVAILABLE_TO_BUILD

    val isMaxLevel: Boolean
        get() = level >= maxLevel

    /**
     * Calculates upgrade cost for next level based on formula.
     */
    fun calculateNextUpgradeMaterials(currentLvl: Int = level): Int {
        return (upgradeCostMaterials * (1.0 + (currentLvl - 1) * 0.45)).toInt()
    }

    fun calculateNextUpgradeMoney(currentLvl: Int = level): Int {
        return (upgradeCostMoney * (1.0 + (currentLvl - 1) * 0.40)).toInt()
    }

    /**
     * Preview of building benefits at the next level.
     */
    fun getNextLevelPreview(): String {
        val nextLvl = level + 1
        return when (type) {
            BuildingType.HQ_COMMAND -> "+${5 * nextLvl} лимит населения, +${25 * nextLvl} Кр/день"
            BuildingType.HYDROPONICS_FARM -> "+${25 * nextLvl} Еды/день (сейчас: +${25 * level})"
            BuildingType.WATER_EXTRACTOR -> "+${30 * nextLvl} Воды/день (сейчас: +${30 * level})"
            BuildingType.WORKSHOP -> "+${15 * nextLvl} Материалов/день (сейчас: +${15 * level})"
            BuildingType.STORAGE_DEPOT -> "+${500 + 300 * nextLvl} вместимость склада (сейчас: +${500 + 300 * level})"
            BuildingType.GENERATOR_STATION -> "Энергия базы +${20 * nextLvl} кВт"
            BuildingType.DEFENSE_PERIMETER -> "+${20 * nextLvl} к защите базы (сейчас: +${20 * level})"
            BuildingType.MEDICAL_CLINIC -> "+${15 * nextLvl} HP/день отдых раненых"
            BuildingType.RADIO_TOWER -> "Радиус сканирования +${nextLvl * 20} км, снижает риск засад"
            BuildingType.TRADING_POST -> "Прибытие караванов на ${nextLvl * 15}% чаще, скидка на бартер"
            BuildingType.RESEARCH_LAB -> "Открывает технологии Ур. $nextLvl, ускоряет научный прогресс"
            BuildingType.ARMORY_LAB -> "+${nextLvl * 2} к базовой атаке и защите всех бойцов"
            BuildingType.GREENHOUSE_COMPLEX -> "+${40 * nextLvl} Еды и +${20 * nextLvl} Медикаментов/день"
        }
    }
}

