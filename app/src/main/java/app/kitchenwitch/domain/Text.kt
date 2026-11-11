package app.kitchenwitch.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The language the app speaks right now (set from the activity's locale). */
object Lang { @Volatile var ru = true }

fun tr(ru: String, en: String) = if (Lang.ru) ru else en

fun plural(n: Int, one: String, few: String, many: String): String {
    val a = Math.abs(n) % 100; val b = a % 10
    return if (a in 11..14) many else when (b) { 1 -> one; 2, 3, 4 -> few; else -> many }
}

/** «3 заклинания» / «3 spells». */
fun count(n: Int, ru1: String, ru2: String, ru5: String, en1: String, enN: String) =
    if (Lang.ru) "$n ${plural(n, ru1, ru2, ru5)}" else "$n ${if (n == 1) en1 else enN}"

private val ruLoc = Locale.forLanguageTag("ru")
fun fmtDay(day: Long): String = LocalDate.ofEpochDay(day).format(DateTimeFormatter.ofPattern("d MMMM", if (Lang.ru) ruLoc else Locale.UK))
fun fmtDayYear(day: Long): String {
    val d = LocalDate.ofEpochDay(day)
    return if (d.year == LocalDate.now().year) fmtDay(day) else d.format(DateTimeFormatter.ofPattern("d MMMM yyyy", if (Lang.ru) ruLoc else Locale.UK))
}
fun weekday(day: Long): String = LocalDate.ofEpochDay(day).format(DateTimeFormatter.ofPattern("EEEE", if (Lang.ru) ruLoc else Locale.UK))
fun minutes(n: Int) = if (n >= 60 && n % 60 == 0) tr("${n / 60} ч", "${n / 60} h") else if (n > 60) tr("${n / 60} ч ${n % 60} мин", "${n / 60} h ${n % 60} min") else tr("$n мин", "$n min")

/** FNV-1a: stable hashes for sigils, colours and the order of things. */
fun hash(s: String): Int { var x = -0x7ee3623b; for (c in s) { x = x xor c.code; x *= 16777619 }; return x }

/** A small seeded random (mulberry32), so drawn things look the same every time. */
class Rand(seed: Int) {
    private var a = seed
    fun next(): Float { a += 0x6D2B79F5; var t = (a xor (a ushr 15)) * (1 or a); t = (t + ((t xor (t ushr 7)) * (61 or t))) xor t; return ((t xor (t ushr 14)).toLong() and 0xffffffffL).toFloat() / 4294967296f }
    fun range(a: Float, b: Float) = a + (b - a) * next()
}
