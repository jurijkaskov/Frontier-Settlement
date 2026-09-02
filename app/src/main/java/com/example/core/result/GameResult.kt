package com.example.core.result

/**
 * Generic result type for game operations, commands, and use cases.
 * Encapsulates success state with payload data or failure state with domain error reason.
 */
sealed class GameResult<out T> {

    data class Success<out T>(
        val data: T,
        val messageRu: String = "Операция выполнена успешно"
    ) : GameResult<T>()

    data class Failure(
        val error: GameError,
        val messageRu: String = error.defaultMessageRu
    ) : GameResult<Nothing>()

    val isSuccess: Boolean
        get() = this is Success

    val isFailure: Boolean
        get() = this is Failure

    fun getOrNull(): T? = (this as? Success)?.data

    fun getOrDefault(defaultValue: @UnsafeVariance T): T = (this as? Success)?.data ?: defaultValue

    inline fun onSuccess(action: (T) -> Unit): GameResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onFailure(action: (GameError, String) -> Unit): GameResult<T> {
        if (this is Failure) action(error, messageRu)
        return this
    }

    inline fun <R> map(transform: (T) -> R): GameResult<R> {
        return when (this) {
            is Success -> Success(transform(data), messageRu)
            is Failure -> Failure(error, messageRu)
        }
    }
}

/**
 * Domain errors representing expected game-rule violations without throwing exceptions.
 */
sealed class GameError(val defaultMessageRu: String) {
    object InsufficientResources : GameError("Недостаточно ресурсов на складе")
    object WarehouseFull : GameError("Склад поселения переполнен")
    object InsufficientPopulation : GameError("Недостаточно свободных жителей")
    object HousingFull : GameError("Нет свободных жилых мест")
    object BuildingAlreadyMaxLevel : GameError("Здание уже улучшено до максимального уровня")
    object TechPrerequisitesNotMet : GameError("Требования для исследования технологии не выполнены")
    object TechAlreadyResearched : GameError("Технология уже изучена")
    object SquadFull : GameError("Отряд разведки полностью укомплектован")
    object SquadEmpty : GameError("В отряде нет участников")
    object CharacterBusy : GameError("Персонаж занят другой задачей или на лечении")
    object CharacterNotFound : GameError("Персонаж не найден")
    object ItemNotFound : GameError("Предмет не найден")
    object RecipeLocked : GameError("Чертёж заблокирован")
    object VehicleUnavailable : GameError("Транспорт недоступен или повреждён")
    object InvalidLocation : GameError("Локация недоступна")
    object AlreadyTraveling : GameError("Группа уже находится в пути")
    object NotInCombat : GameError("Нет активного боевого столкновения")
    object InsufficientActionPoints : GameError("Недостаточно очков действия (ОД)")
    object QuestRequirementsNotMet : GameError("Условия задания не выполнены")
    object QuestAlreadyCompleted : GameError("Задание уже выполнено")
    object QuestNotFound : GameError("Задание не найдено в журнале")
    data class RuleViolation(val reason: String) : GameError(reason)
}
