package app.kitchenwitch

import android.app.Application
import android.content.Context
import app.kitchenwitch.data.Cast
import app.kitchenwitch.data.Dish
import app.kitchenwitch.data.Leftover
import app.kitchenwitch.data.Library
import app.kitchenwitch.data.Outside
import app.kitchenwitch.data.OutsideSource
import app.kitchenwitch.data.Recipe
import app.kitchenwitch.data.SettingsStore
import app.kitchenwitch.data.Weather
import app.kitchenwitch.data.WitchDb
import app.kitchenwitch.domain.CastScene
import app.kitchenwitch.domain.Crafts
import app.kitchenwitch.domain.Jar
import app.kitchenwitch.domain.Jars
import app.kitchenwitch.domain.Lang
import app.kitchenwitch.domain.Mark
import app.kitchenwitch.domain.Marks
import app.kitchenwitch.domain.Moon
import app.kitchenwitch.domain.Wheel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

/** What a finished cast brought: shown on the sigil screen. */
data class CastResult(val castId: Long, val newJars: List<Jar>, val marks: List<Mark>, val kept: Boolean, val miss: Boolean)

/** How the bones are thrown. */
data class Throw(val maxMinutes: Int = 0, val fromHome: Boolean = false)

class AppContainer(context: Context) {
    val app: Context = context.applicationContext
    val db = WitchDb.create(app)
    val dao = db.dao()
    val settings = SettingsStore(app)
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val news = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val plates: File get() = File(app.filesDir, "plates").apply { mkdirs() }

    fun write(block: suspend AppContainer.() -> Unit) { scope.launch { block() } }
    fun library() = Library.all(app)

    /** The recipe row a dish lives in; library dishes are copied in on first use. */
    suspend fun ensure(d: Dish): Long {
        d.recipeId?.let { return it }
        val lib = d.libId ?: error("dish without a home")
        dao.byLib(lib)?.let { return it.id }
        val l = Library.byId(app, lib) ?: error("no $lib")
        return dao.insert(l.toRecipe(System.currentTimeMillis()))
    }

    suspend fun setSpell(d: Dish) {
        val id = ensure(d)
        settings.update { it.copy(spell = id, spellCycle = Moon.cycle()) }
    }

    suspend fun toScroll(d: Dish): Long {
        val id = ensure(d)
        dao.recipe(id)?.let { if (!it.scroll) dao.update(it.copy(scroll = true)) }
        return id
    }

    /** Bones thrown today, three a day. Returns false when they're spent. */
    suspend fun spendRoll(): Boolean {
        val s = settings.now(); val today = Moon.today()
        val used = if (s.rollDay == today) s.rolls else 0
        if (used >= 3) return false
        settings.update { it.copy(rollDay = today, rolls = used + 1) }
        return true
    }
    suspend fun rollsLeft(): Int { val s = settings.now(); return 3 - if (s.rollDay == Moon.today()) s.rolls else 0 }

    /** Candidates for the bones: library and own scrolls never cooked, filtered. */
    suspend fun pool(t: Throw): List<Dish> {
        val cooked = dao.castList().filter { it.verdict != "miss" }.map { it.recipeId }.toSet()
        val recipes = dao.recipeList()
        val cookedLib = recipes.filter { it.id in cooked && it.libId.isNotEmpty() }.map { it.libId }.toSet()
        val own = recipes.filter { it.id !in cooked && it.libId.isEmpty() && it.scroll }.map { Dish.of(it) }
        val lib = library().filter { it.id !in cookedLib }.map { Dish.of(it) }
        var all = own + lib
        if (t.maxMinutes > 0) all = all.filter { it.minutes in 1..t.maxMinutes }
        if (t.fromHome) { val left = dao.leftoverList().map { it.name.lowercase() }
            all = all.filter { d -> d.ingredients.any { ing -> left.any { l -> stemMatch(ing, l) } } } }
        val month = LocalDate.now().monthValue
        return all.sortedByDescending { if (it.months.isEmpty() || month in it.months) 1 else 0 }
    }

    /** Does an ingredient line name a leftover? Russian endings are forgiven («кинза» finds «пучок кинзы»). */
    fun stemMatch(ingredient: String, leftover: String): Boolean {
        val l = leftover.lowercase().replace('ё', 'е').trim(); if (l.length < 3) return false
        val stem = if (l.length > 4) l.dropLast(1) else l
        return ingredient.lowercase().replace('ё', 'е').contains(stem)
    }

    suspend fun addLeftover(name: String) { if (name.isNotBlank()) dao.insert(Leftover(name = name.trim(), day = Moon.today())) }

    /** Finishes a cast: writes it to the book, fills the shelf, uses up leftovers, gives marks. */
    suspend fun cast(d: Dish, intent: String, verdict: String, note: String, photo: String, guests: List<String>): CastResult {
        val id = ensure(d)
        val s = settings.now(); val now = System.currentTimeMillis(); val today = Moon.today(); val age = Moon.age(now); val cycle = Moon.cycle(now)
        val before = dao.castList()
        val miss = verdict == "miss"
        val out = outsideNow()
        val newJars = if (miss) emptyList() else d.ingredients.mapNotNull { Jars.of(it) }.distinctBy { it.id }.filter { it.id !in s.jars }
        val keptSpell = !miss && s.spell == id && s.spellCycle == cycle && before.none { it.cycle == cycle && it.spell }
        val sabbat = Wheel.at(LocalDate.now())?.id
        val left = dao.leftoverList()
        val used = if (miss) emptyList() else left.filter { l -> d.ingredients.any { stemMatch(it, l.name) } }
        val prevJarsGained = before.sumOf { c -> c.jars.split(',').count { it.isNotBlank() } }
        val keptCycles = (before.filter { it.spell }.map { it.cycle } + if (keptSpell) listOf(cycle) else emptyList()).toSortedSet()
        var run = 0; var cyc = cycle; while (cyc in keptCycles) { run++; cyc-- }
        val distinct = (before.filter { it.verdict != "miss" }.map { it.recipeId } + if (miss) emptyList() else listOf(id)).toSet().size
        val sabbats = (before.map { it.sabbat }.filter { it.isNotEmpty() } + listOfNotNull(sabbat.takeIf { !miss })).toSet().size
        val md = LocalDate.now().let { it.monthValue to it.dayOfMonth }
        val season = LocalDate.now().let { if (it.monthValue >= 9) LocalDate.of(it.year, 9, 1) else LocalDate.of(it.year - 1, 9, 1) }.toEpochDay()
        val scene = CastScene(LocalTime.now().hour, Moon.quarter(age), age, out?.weather?.name.orEmpty(), Crafts.of(d.steps), guests.size, intent, verdict,
            newJars, sabbat, md == 12 to 21 || md == 6 to 21,
            out?.weather == Weather.SNOW && before.none { it.weather == "SNOW" && it.day >= season },
            !miss && before.any { it.recipeId == id && it.verdict == "miss" }, keptSpell, run, distinct, prevJarsGained + newJars.size, used.size, sabbats)
        val marks = Marks.earned(scene).filter { it !in s.marks }.mapNotNull { Marks.byId[it] }
        val castId = dao.insert(Cast(recipeId = id, title = d.title, reagents = d.reagents.joinToString(","), at = now, day = today, age = age, cycle = cycle,
            intent = intent, verdict = verdict, note = note.trim(), photo = photo, guests = guests.joinToString(", "), weather = out?.weather?.name.orEmpty(),
            temp = out?.temp ?: Double.NaN, sabbat = if (miss) "" else sabbat.orEmpty(), marks = marks.joinToString(",") { it.id },
            jars = newJars.joinToString(",") { it.id }, spell = keptSpell))
        used.forEach { dao.deleteLeftover(it.id) }
        settings.update { it.copy(jars = it.jars + newJars.map { j -> j.id }, marks = it.marks + marks.associate { m -> m.id to today }) }
        return CastResult(castId, newJars, marks, keptSpell, miss)
    }

    suspend fun outsideNow(): Outside? = Outside.of(settings.now().outside)?.takeIf { System.currentTimeMillis() - it.at < 3 * 3600_000L }

    /** Fetches the weather for the window when it is older than half an hour. */
    suspend fun refreshOutside(force: Boolean = false) = withContext(Dispatchers.IO) { runCatching {
        var s = settings.now()
        if (s.lat.isNaN() && !s.guessed) {
            val t = OutsideSource.guess(Lang.ru)
            settings.update { it.copy(guessed = true, town = t?.name ?: it.town, lat = t?.lat ?: it.lat, lon = t?.lon ?: it.lon) }
            s = settings.now()
        }
        if (s.lat.isNaN()) return@runCatching
        val old = Outside.of(s.outside)
        if (!force && old != null && System.currentTimeMillis() - old.at < 30 * 60_000L) return@runCatching
        val o = OutsideSource.fetch(s.lat, s.lon)
        settings.update { it.copy(outside = o.json()) }
    } }
}

class WitchApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        Lang.ru = resources.configuration.locales[0].language == "ru"
        container = AppContainer(this)
        container.scope.launch(Dispatchers.Default) { runCatching { Library.all(this@WitchApp) } }
    }
}

val Context.container: AppContainer get() = (applicationContext as WitchApp).container

/** A new, empty scroll written by hand. */
fun newRecipe(title: String, origin: String, minutes: Int, ingredients: String, steps: String, source: String, url: String = "", page: String = "") =
    Recipe(title = title.trim(), origin = origin.trim(), minutes = minutes, ingredients = ingredients.trim(), steps = steps.trim(), source = source,
        url = url, page = page, created = System.currentTimeMillis())
