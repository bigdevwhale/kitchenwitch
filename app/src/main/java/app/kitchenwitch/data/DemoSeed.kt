package app.kitchenwitch.data

import app.kitchenwitch.AppContainer
import app.kitchenwitch.domain.Jars
import app.kitchenwitch.domain.Moon
import app.kitchenwitch.domain.tr
import java.time.LocalDate
import java.time.ZoneId

/** Demo builds: half a year of a witch's kitchen, so every screen has something to show. */
object DemoSeed {
    private class Past(val lib: String, val daysAgo: Int, val hour: Int, val intent: String, val verdict: String, val noteRu: String, val noteEn: String,
                       val guests: String = "", val weather: String = "", val temp: Double = Double.NaN, val sabbat: String = "", val marks: String = "")

    suspend fun seedIfEmpty(c: AppContainer) {
        if (c.dao.castList().isNotEmpty() || c.dao.recipeList().isNotEmpty()) return
        val today = LocalDate.now()
        fun ago(d: LocalDate) = today.toEpochDay() - d.toEpochDay()
        val samhain = LocalDate.of(if (today.monthValue > 10) today.year else today.year - 1, 10, 31)
        val mabon = LocalDate.of(if (today.monthValue >= 10) today.year else today.year - 1, 9, 22)
        val past = listOf(
            Past("pumpkin_soup", ago(samhain).toInt(), 19, "warmth", "ok", "Подали прямо в тыкве. Имбиря можно смелее.", "Served in the pumpkin itself. More ginger next time.",
                tr("Аня, Лёша", "Anna, Alex"), "FOG", 5.0, "samhain", "wheel,sabbat"),
            Past("focaccia", 19, 21, "joy", "ok", "Ямки пальцами – самое приятное.", "Dimpling it with your fingers is the best part.", weather = "CLEAR", temp = 7.0, marks = "full_bread"),
            Past("apple_crumble", ago(mabon).toInt(), 17, "", "ok", "Яблоки антоновка. Овсянку в крошку!", "Tart apples. Oats in the crumble!", weather = "RAIN", temp = 11.0, sabbat = "mabon", marks = "rain"),
            Past("borscht", 58, 13, "calm", "ok", "Свёклу запекать в фольге целиком – цвет держится.", "Roast the beetroot whole in foil – the colour holds.", weather = "STORM", temp = 16.0, marks = "storm"),
            Past("carbonara", 70, 22, "", "meh", "Без сливок! Чуть свернулось – снимать с огня раньше.", "No cream! Curdled a bit – take it off the heat sooner.", weather = "CLEAR", temp = 19.0),
            Past("shakshuka", 40, 10, "courage", "ok", "Зира пахнет дорогой.", "Cumin smells of the road.", weather = "CLOUDY", temp = 12.0, marks = "far_jar"),
            Past("gyoza", 33, 1, "", "miss", "Разварились: тесто слишком тонкое.", "Fell apart: the wrappers were too thin.", weather = "CLEAR", temp = 4.0, marks = "midnight"),
            Past("gyoza", 26, 20, "love", "ok", "Со второго раза! Защипы гармошкой получаются.", "Second time lucky! The pleats work now.", tr("Мама", "Mum"), "CLOUDY", 6.0, marks = "second_chance,for_someone"),
            Past("dal", 12, 19, "warmth", "ok", "Лайм в конце обязательно.", "The lime at the end is a must.", weather = "RAIN", temp = 6.0),
        )
        val ids = HashMap<String, Long>()
        for (p in past) {
            val l = Library.byId(c.app, p.lib) ?: continue
            val day = today.minusDays(p.daysAgo.toLong())
            val at = day.atTime(p.hour, 20).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val id = ids.getOrPut(p.lib) { c.dao.insert(l.toRecipe(at - 86_400_000L * 3)) }
            val d = Dish.of(l)
            c.dao.insert(Cast(recipeId = id, title = l.title, reagents = d.reagents.joinToString(","), at = at, day = day.toEpochDay(), age = Moon.age(at), cycle = Moon.cycle(at),
                intent = p.intent, verdict = p.verdict, note = tr(p.noteRu, p.noteEn), guests = p.guests, weather = p.weather, temp = p.temp, sabbat = p.sabbat, marks = p.marks,
                jars = d.ingredients.mapNotNull { Jars.of(it)?.id }.filter { it in setOf("sumac", "cumin", "ginger") }.distinct().joinToString(",")))
        }
        listOf("miso_soup", "hummus", "pho").forEach { lib -> Library.byId(c.app, lib)?.let { c.dao.insert(it.toRecipe(System.currentTimeMillis() - 86_400_000L)) } }
        c.dao.insert(app.kitchenwitch.newRecipe(tr("Пирог «Зебра»", "Zebra cake"), tr("тетрадь бабушки", "grandmother's notebook"), 70,
            tr("3 яйца\n1 стакан сахара\n200 г сметаны\n100 г сливочного масла\n2 стакана муки\n2 ст. л. какао\nваниль", "3 eggs\n1 cup sugar\n200 g soured cream\n100 g butter\n2 cups flour\n2 tbsp cocoa\nvanilla"),
            tr("Взбей яйца с сахаром добела.\nДобавь сметану, растопленное масло и муку.\nРаздели тесто пополам и в одну половину добавь какао.\nВыкладывай по ложке в центр формы, чередуя цвета.\nВыпекай при 180° 45 минут.",
                "Whisk the eggs and sugar until pale.\nAdd the soured cream, melted butter and flour.\nSplit the batter and add cocoa to one half.\nSpoon into the middle of the tin, alternating colours.\nBake at 180° for 45 minutes."), "own"))
        listOf(tr("кинза", "coriander"), tr("тыква", "pumpkin"), tr("сливки", "cream")).forEach { c.addLeftover(it) }
        val spell = Library.byId(c.app, "musakhan")!!
        val spellId = c.dao.insert(spell.toRecipe(System.currentTimeMillis()))
        c.settings.update { it.copy(onboarded = true, spell = spellId, spellCycle = Moon.cycle(),
            seen = if (app.kitchenwitch.BuildConfig.SHOTS) setOf("kitchen", "moon", "book", "shelf", "wheel", "cook", "dice", "write") else emptySet(),
            jars = setOf("salt", "pepper", "bay", "rosemary", "basil", "cinnamon", "paprika", "caraway", "nutmeg", "clove", "vanilla", "dill", "honey", "ginger", "turmeric", "thyme", "sage", "cumin", "garlic"),
            marks = mapOf("wheel" to ago(samhain), "sabbat" to ago(samhain), "full_bread" to 19L, "rain" to 12L, "storm" to 58L, "far_jar" to 40L, "midnight" to 33L, "second_chance" to 26L, "for_someone" to 26L)
                .mapValues { (_, a) -> today.toEpochDay() - a }) }
    }
}
