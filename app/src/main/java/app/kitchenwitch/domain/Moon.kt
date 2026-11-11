package app.kitchenwitch.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** The four moods of a moon: what the kitchen is for this week. */
enum class Quarter { NEW, WAX, FULL, WANE }

/**
 * The real moon, by the mean synodic month from a known new moon (6 January 2000, 18:14 UTC).
 * Good to a few hours, which is all a kitchen needs.
 */
object Moon {
    const val SYN = 29.530588853
    private const val REF = 947182440000L // 2000-01-06T18:14Z

    fun age(at: Long = System.currentTimeMillis()): Double = (((at - REF) / 86_400_000.0) % SYN + SYN) % SYN
    /** Which moon since the reference this is: spells of the moon are kept per cycle. */
    fun cycle(at: Long = System.currentTimeMillis()): Long = Math.floor((at - REF) / 86_400_000.0 / SYN).toLong()
    fun cycleStart(cycle: Long): Long = REF + (cycle * SYN * 86_400_000.0).toLong()
    fun dayOf(age: Double) = age.toInt() + 1
    fun phase(age: Double) = age / SYN

    fun quarter(age: Double): Quarter = when {
        age < 7.38 -> Quarter.NEW
        age < 12.92 -> Quarter.WAX
        age < 16.61 -> Quarter.FULL
        else -> Quarter.WANE
    }
    /** Where each quarter starts and ends on the ring, in days of the moon. */
    val bounds = listOf(Quarter.NEW to (0.0 to 7.38), Quarter.WAX to (7.38 to 12.92), Quarter.FULL to (12.92 to 16.61), Quarter.WANE to (16.61 to SYN))

    fun name(age: Double): String = when {
        age < 1.85 -> tr("Новолуние", "New moon")
        age < 5.54 -> tr("Молодой серп", "Waxing crescent")
        age < 9.23 -> tr("Первая четверть", "First quarter")
        age < 12.92 -> tr("Прибывающая луна", "Waxing gibbous")
        age < 16.61 -> tr("Полнолуние", "Full moon")
        age < 20.3 -> tr("Убывающая луна", "Waning gibbous")
        age < 23.99 -> tr("Последняя четверть", "Last quarter")
        age < 27.68 -> tr("Старый серп", "Waning crescent")
        else -> tr("Новолуние", "New moon")
    }

    fun title(q: Quarter) = when (q) {
        Quarter.NEW -> tr("Новая луна", "New moon"); Quarter.WAX -> tr("Растущая луна", "Waxing moon")
        Quarter.FULL -> tr("Полнолуние", "Full moon"); Quarter.WANE -> tr("Убывающая луна", "Waning moon")
    }
    fun verb(q: Quarter) = when (q) {
        Quarter.NEW -> tr("время начинать", "time to begin"); Quarter.WAX -> tr("время пробовать", "time to try")
        Quarter.FULL -> tr("время шабаша", "time for a sabbat"); Quarter.WANE -> tr("время чистки", "time to clear")
    }
    fun ring(q: Quarter) = when (q) {
        Quarter.NEW -> tr("Начинать", "Begin"); Quarter.WAX -> tr("Пробовать", "Try")
        Quarter.FULL -> tr("Шабаш", "Sabbat"); Quarter.WANE -> tr("Чистка", "Clear")
    }
    fun hint(q: Quarter) = when (q) {
        Quarter.NEW -> tr("Загадай заклинание на эту луну – блюдо, которого ещё нет в книге.", "Set a spell for this moon – a dish that isn't in the book yet.")
        Quarter.WAX -> tr("Попробуй то, чего ещё нет на полке.", "Try something that isn't on the shelf yet.")
        Quarter.FULL -> tr("Позови близких к столу – гости распишутся в книге.", "Call your people to the table. Guests leave their mark in the book.")
        Quarter.WANE -> tr("Доешь то, что залежалось: кости подберут блюдо из остатков.", "Use up what's been waiting. The bones will find a spell for the leftovers.")
    }

    /** Folk names of the moons, by the month the moon was born in. */
    fun moonName(cycle: Long): String {
        val m = Instant.ofEpochMilli(cycleStart(cycle) + 86_400_000L).atZone(ZoneId.systemDefault()).monthValue
        return listOf(
            tr("Луна волчьего воя", "Wolf Moon"), tr("Луна вьюги", "Snow Moon"), tr("Луна капели", "Worm Moon"), tr("Луна первоцвета", "Pink Moon"),
            tr("Луна цветения", "Flower Moon"), tr("Луна земляники", "Strawberry Moon"), tr("Грозовая луна", "Thunder Moon"), tr("Луна жатвы", "Sturgeon Moon"),
            tr("Луна урожая", "Harvest Moon"), tr("Луна листопада", "Hunter's Moon"), tr("Луна первого снега", "Frost Moon"), tr("Долгая луна", "Long Night Moon"),
        )[m - 1]
    }

    fun epochDay(at: Long): Long = Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
    fun today(): Long = LocalDate.now().toEpochDay()
}
