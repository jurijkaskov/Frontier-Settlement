package com.example.domain.content.location

import com.example.domain.content.core.GameRandomProvider
import com.example.domain.model.LocationType
import kotlin.random.Random

/**
 * Procedural name generator for locations using structured Russian tokens,
 * thematic prefixes, bases, and suffixes.
 */
object LocationNameGenerator {

    private val DEFAULT_PREFIXES = listOf(
        "Заброшенный", "Старый", "Северный", "Восточный", "Узловой", "Глубокий",
        "Тайный", "Ржавый", "Покинутый", "Дальний", "Забытый"
    )

    private val DEFAULT_BASES_BY_TYPE = mapOf(
        LocationType.ABANDONED_STATION to listOf("«Разъезд-4»", "«Тупик»", "«Магистраль»", "«Северный вокзал»", "«Депо-7»"),
        LocationType.MILITARY_BUNKER to listOf("«Форт Омега»", "«Блокпост №3»", "«Рубеж»", "«Заслон»", "«Цитадель»"),
        LocationType.INDUSTRIAL_PLANT to listOf("«Вектор-М»", "«Промсталь»", "«Комбинат»", "«Литейный»", "«Сборка-9»"),
        LocationType.WAREHOUSE_COMPLEX to listOf("«Логистик-А»", "«Терминал-4»", "«Хранилище ГСМ»", "«Склад Резерв»"),
        LocationType.ANOMALY_ZONE to listOf("«Сектор Альфа»", "«Разлом»", "«Зона Молчания»", "«Эпицентр»"),
        LocationType.FARM to listOf("«Агрокомплекс Заря»", "«Нива»", "«Угодья Колос»", "«Зелёный ручей»"),
        LocationType.TRADING_POST to listOf("«Перекрёсток»", "«Золотой караван»", "«Базарная фактория»", "«Привал»"),
        LocationType.FOREST to listOf("«Охотничья заимка»", "«Лесной кордон»", "«Дубрава»", "«Сосновый бор»"),
        LocationType.VILLAGE to listOf("«Красные Ключи»", "«Отрадное»", "«Сосновка»", "«Береговое»"),
        LocationType.CITY_RUINS to listOf("«Мёртвый Квартал»", "«Руины Проспекта»", "«Сектор 7»", "«Высотка»"),
        LocationType.SETTLEMENT to listOf("«Аванпост Фронтир»", "«Новый рассвет»", "«Убежище-1»")
    )

    /**
     * Generates a flavorful location name from a template and random seed.
     */
    fun generateName(template: LocationTemplate, random: Random): String {
        val prefix = if (template.namePrefixList.isNotEmpty()) {
            template.namePrefixList[random.nextInt(template.namePrefixList.size)]
        } else {
            DEFAULT_PREFIXES[random.nextInt(DEFAULT_PREFIXES.size)]
        }

        val base = if (template.nameBaseList.isNotEmpty()) {
            template.nameBaseList[random.nextInt(template.nameBaseList.size)]
        } else {
            val typeBases = DEFAULT_BASES_BY_TYPE[template.type] ?: DEFAULT_BASES_BY_TYPE.getValue(LocationType.VILLAGE)
            typeBases[random.nextInt(typeBases.size)]
        }

        val hasSuffix = template.nameSuffixList.isNotEmpty() && random.nextFloat() > 0.4f
        return if (hasSuffix) {
            val suffix = template.nameSuffixList[random.nextInt(template.nameSuffixList.size)]
            "$prefix $base ($suffix)"
        } else {
            "$prefix $base"
        }
    }
}
