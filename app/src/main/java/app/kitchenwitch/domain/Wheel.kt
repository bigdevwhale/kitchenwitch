package app.kitchenwitch.domain

import java.time.LocalDate
import java.time.MonthDay

/**
 * The eight feasts of the witch's year. [menu] are library recipe ids; a feast counts as kept
 * when any dish of its menu, or any dish cooked within a day of it, is in the book.
 */
class Sabbat(val id: String, val ru: String, val en: String, val day: MonthDay, val aboutRu: String, val aboutEn: String, val menu: List<String>) {
    val name get() = tr(ru, en)
    val about get() = tr(aboutRu, aboutEn)
    fun next(from: LocalDate): LocalDate { val d = day.atYear(from.year); return if (d.isBefore(from)) day.atYear(from.year + 1) else d }
    fun last(from: LocalDate): LocalDate { val d = day.atYear(from.year); return if (d.isAfter(from)) day.atYear(from.year - 1) else d }
}

object Wheel {
    val all = listOf(
        Sabbat("yule", "Йоль", "Yule", MonthDay.of(12, 21), "Самая длинная ночь, солнце поворачивает к весне", "The longest night; the sun turns back to spring", listOf("mulled", "gingerbread", "yule_log", "roast_pork")),
        Sabbat("imbolc", "Имболк", "Imbolc", MonthDay.of(2, 1), "Первое молоко, свет возвращается", "First milk, the light returns", listOf("syrniki", "bliny", "oatcakes")),
        Sabbat("ostara", "Остара", "Ostara", MonthDay.of(3, 20), "Весеннее равноденствие", "The spring equinox", listOf("frittata", "braided_bread", "onion_eggs")),
        Sabbat("beltane", "Белтайн", "Beltane", MonthDay.of(5, 1), "Костры, цветы и первая зелень", "Bonfires, flowers and the first greens", listOf("strawberries_cream", "bannock", "nettle_soup")),
        Sabbat("litha", "Лита", "Litha", MonthDay.of(6, 21), "Самый длинный день", "The longest day", listOf("honey_cake", "kholodnik", "grilled_veg")),
        Sabbat("lammas", "Ламмас", "Lammas", MonthDay.of(8, 1), "Первый хлеб нового урожая", "The first bread of the harvest", listOf("harvest_bread", "corn_coals", "berry_pie")),
        Sabbat("mabon", "Мабон", "Mabon", MonthDay.of(9, 22), "Осеннее равноденствие, благодарность за урожай", "The autumn equinox; thanks for the harvest", listOf("apple_crumble", "roast_pumpkin", "mushroom_pie")),
        Sabbat("samhain", "Самайн", "Samhain", MonthDay.of(10, 31), "Ночь памяти: ужин в честь тех, кого с нами нет", "The veil is thin; a supper to remember your own", listOf("pumpkin_soup", "baked_apples", "soul_cakes")),
    )
    val byId = all.associateBy { it.id }

    fun upcoming(today: LocalDate): List<Pair<Sabbat, LocalDate>> = all.map { it to it.next(today) }.sortedBy { it.second }
    /** The feast a day belongs to: the day itself or the day after. */
    fun at(day: LocalDate): Sabbat? = all.firstOrNull { val d = it.last(day); !day.isBefore(d) && day.toEpochDay() - d.toEpochDay() <= 1 }
}
