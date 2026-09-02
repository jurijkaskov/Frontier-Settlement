package com.example.audio.model

/**
 * Semantic identifiers for short sound effects (SFX & UI cues).
 * Gameplay code references these semantic IDs instead of raw resource integers.
 */
enum class GameSoundId(
    val category: SoundCategory,
    val descriptionRu: String,
    val defaultRelativeVolume: Float = 1.0f,
    val hasVariants: Boolean = false
) {
    // --- UI Sounds ---
    UI_CLICK(SoundCategory.UI, "Стандартный клик по кнопке/элементу", 0.7f),
    UI_CONFIRM(SoundCategory.UI, "Подтверждение действия / выбор", 0.85f),
    UI_CANCEL(SoundCategory.UI, "Отмена / закрытие окна / назад", 0.75f),
    UI_TAB(SoundCategory.UI, "Переключение вкладки или раздела", 0.6f),
    UI_TOGGLE(SoundCategory.UI, "Переключение тумблера / чекбокса", 0.65f),
    UI_ERROR(SoundCategory.UI, "Ошибка / недоступное действие", 0.8f),
    UI_WARNING(SoundCategory.UI, "Предупреждение / нехватка ресурсов", 0.85f),

    // --- Settlement & Economy ---
    RESOURCE_GAIN(SoundCategory.SFX, "Заметное получение ресурсов", 0.75f),
    DAILY_REPORT(SoundCategory.UI, "Сводка суточного отчёта", 0.8f),
    NEW_DAY(SoundCategory.SFX, "Наступление нового игрового дня", 0.9f),
    BUILDING_CONSTRUCT(SoundCategory.SFX, "Строительство нового объекта", 0.9f),
    BUILDING_UPGRADE(SoundCategory.SFX, "Улучшение существующего здания", 0.9f),
    WORKSHOP_CRAFT(SoundCategory.SFX, "Производство предмета в мастерской", 0.85f),
    WORKSHOP_REPAIR(SoundCategory.SFX, "Ремонт техники или снаряжения", 0.85f),
    RESEARCH_COMPLETE(SoundCategory.SFX, "Завершение научного исследования", 0.95f),

    // --- Trade & Market ---
    TRADE_BUY(SoundCategory.SFX, "Покупка товаров у торговца", 0.8f),
    TRADE_SELL(SoundCategory.SFX, "Продажа товаров на рынке", 0.8f),

    // --- Reputation & Factions ---
    REPUTATION_INCREASE(SoundCategory.SFX, "Повышение репутации поселения/фракции", 0.85f),

    // --- Quests ---
    QUEST_ACCEPTED(SoundCategory.SFX, "Принятие задания штаба", 0.85f),
    QUEST_OBJECTIVE_COMPLETE(SoundCategory.SFX, "Выполнение промежуточной цели квеста", 0.85f),
    QUEST_COMPLETED(SoundCategory.SFX, "Успешная сдача квеста и получение наград", 1.0f),
    QUEST_FAILED(SoundCategory.SFX, "Провал задания", 0.85f),

    // --- Map & Exploration ---
    LOCATION_DISCOVERED(SoundCategory.SFX, "Обнаружение новой точки интереса на карте", 0.9f),
    TRAVEL_STEP(SoundCategory.SFX, "Шаг перехода каравана по пустошам", 0.6f),
    VEHICLE_ENGINE(SoundCategory.SFX, "Звук работы двигателя транспорта", 0.7f),

    // --- Events & Choices ---
    EVENT_REVEAL(SoundCategory.SFX, "Появление экрана случайного события", 0.85f),
    EVENT_POSITIVE_RESULT(SoundCategory.SFX, "Благоприятный исход выбора в событии", 0.9f),
    EVENT_NEGATIVE_RESULT(SoundCategory.SFX, "Неблагоприятный исход / урон / потеря", 0.9f),

    // --- Loot ---
    LOOT_REVEAL(SoundCategory.SFX, "Обнаружение тайника или контейнера с добычей", 0.85f),
    LOOT_PICK(SoundCategory.UI, "Сбор отдельного предмета", 0.65f),
    LOOT_RARE(SoundCategory.SFX, "Находка редкого / ценного снаряжения", 0.95f),
    LOOT_TAKE_ALL(SoundCategory.UI, "Сбор всех найденных трофеев", 0.85f),

    // --- Combat SFX ---
    COMBAT_ATTACK_MELEE(SoundCategory.SFX, "Ближняя атака холодным оружием", 0.9f, hasVariants = true),
    COMBAT_SHOT_PISTOL(SoundCategory.SFX, "Выстрел из пистолета", 0.9f, hasVariants = true),
    COMBAT_SHOT_RIFLE(SoundCategory.SFX, "Выстрел из винтовки / автомата", 0.95f, hasVariants = true),
    COMBAT_SHOT_SHOTGUN(SoundCategory.SFX, "Выстрел из дробовика", 1.0f),
    COMBAT_SHOT_HEAVY(SoundCategory.SFX, "Тяжёлый выстрел / взрыв", 1.0f),
    COMBAT_HIT(SoundCategory.SFX, "Попадание по цели / получение урона", 0.9f, hasVariants = true),
    COMBAT_MISS(SoundCategory.SFX, "Промах выстрела или удара", 0.7f),
    COMBAT_ARMOR_BLOCK(SoundCategory.SFX, "Поглощение урона бронёй / рикошет", 0.85f),
    COMBAT_HEAL(SoundCategory.SFX, "Использование аптечки / полевое лечение", 0.85f),
    COMBAT_BUFF(SoundCategory.SFX, "Наложение положительного боевого эффекта", 0.8f),
    COMBAT_DEBUFF(SoundCategory.SFX, "Наложение негативного статуса / кровотечение", 0.85f),
    COMBAT_STATUS_EXPIRE(SoundCategory.SFX, "Окончание действия эффекта", 0.65f),
    COMBAT_TURN_PLAYER(SoundCategory.UI, "Переход хода к отряду игрока", 0.6f),
    COMBAT_TURN_ENEMY(SoundCategory.UI, "Переход хода к противнику", 0.6f),
    COMBAT_VICTORY_STING(SoundCategory.SFX, "Короткий победный аккорд окончания боя", 1.0f),
    COMBAT_DEFEAT_STING(SoundCategory.SFX, "Короткий аккорд поражения отряда", 0.95f);
}

/**
 * Top-level categories for independent audio mixing and volume configuration.
 */
enum class SoundCategory(val titleRu: String) {
    MUSIC("Музыка"),
    AMBIENT("Окружение"),
    SFX("Звуковые эффекты"),
    UI("Интерфейс")
}
