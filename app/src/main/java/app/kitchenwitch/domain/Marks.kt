package app.kitchenwitch.domain

/** A witch's mark: earned quietly by the circumstances of a cast, never shown in advance except as a hint. */
class Mark(val id: String, private val ru: String, private val en: String, private val hintRu: String, private val hintEn: String) {
    val name get() = tr(ru, en)
    val hint get() = tr(hintRu, hintEn)
}

/** Everything about one cast that marks look at. */
class CastScene(
    val hour: Int, val quarter: Quarter, val age: Double, val weather: String,
    val crafts: List<String>, val guests: Int, val intent: String, val verdict: String,
    val newJars: List<Jar>, val sabbat: String?, val isSolstice: Boolean,
    val firstSnow: Boolean, val missedBefore: Boolean, val keptSpell: Boolean, val keptRun: Int,
    val distinct: Int, val jarsGained: Int, val usedLeftovers: Int, val sabbatsKept: Int,
)

object Marks {
    val all = listOf(
        Mark("storm", "Котёл в грозу", "A cauldron in the storm", "Что-то про гром…", "Something about thunder…"),
        Mark("full_bread", "Хлеб в полнолуние", "Bread at the full moon", "Что-то про тесто и луну…", "Something about dough and the moon…"),
        Mark("midnight", "После полуночи", "After midnight", "Что-то про поздний час…", "Something about a late hour…"),
        Mark("dawn", "Рассвет у плиты", "Dawn at the stove", "Что-то про раннее утро…", "Something about early morning…"),
        Mark("first_snow", "Первый снег", "First snow", "Что-то про белое за окном…", "Something white outside…"),
        Mark("rain", "Под шум дождя", "To the sound of rain", "Что-то про стук по стеклу…", "Something tapping on the glass…"),
        Mark("fog", "В тумане", "In the fog", "Что-то про то, чего не видно…", "Something you can't see…"),
        Mark("kept", "Слово сдержано", "A word kept", "Что-то про обещание луне…", "Something about a promise to the moon…"),
        Mark("three_moons", "Три луны подряд", "Three moons running", "Что-то, что делают трижды…", "Something done three times…"),
        Mark("far_jar", "Банка из дальних стран", "A jar from far away", "Что-то про чужой край…", "Something from a far land…"),
        Mark("shelf_grows", "Полка растёт", "The shelf grows", "Что-то про пустые места…", "Something about empty places…"),
        Mark("second_chance", "Второй шанс", "Second chance", "Что-то про то, что не вышло…", "Something that didn't work out…"),
        Mark("ten", "Десять заклинаний", "Ten spells", "Что-то про круглое число…", "Something about a round number…"),
        Mark("fifty", "Пятьдесят заклинаний", "Fifty spells", "Что-то про очень круглое число…", "Something about a very round number…"),
        Mark("wheel", "Колесо повернулось", "The wheel turns", "Что-то про праздник…", "Something about a feast…"),
        Mark("four_feasts", "Полколеса", "Half the wheel", "Что-то про несколько праздников…", "Something about many feasts…"),
        Mark("solstice", "Солнцеворот", "Solstice", "Что-то про самый короткий или длинный день…", "Something about the shortest or longest day…"),
        Mark("sabbat", "Первый шабаш", "First sabbat", "Что-то про гостей и полную луну…", "Something about guests and a full moon…"),
        Mark("feast", "Пир горой", "A great feast", "Что-то про большой стол…", "Something about a big table…"),
        Mark("clean_slate", "С чистого листа", "A clean slate", "Что-то про самый первый день…", "Something about the very first day…"),
        Mark("nothing_lost", "Ничего не пропало", "Nothing wasted", "Что-то про кладовую…", "Something about the pantry…"),
        Mark("for_someone", "С любовью", "For someone", "Что-то про намерение…", "Something about an intention…"),
    )
    val byId = all.associateBy { it.id }

    /** Marks this cast earns (before removing the ones already held). */
    fun earned(c: CastScene): List<String> = buildList {
        val ok = c.verdict != "miss"
        if (c.weather == "STORM") add("storm")
        if (c.weather == "RAIN") add("rain")
        if (c.weather == "FOG") add("fog")
        if (c.firstSnow) add("first_snow")
        if (c.hour in 0..3) add("midnight")
        if (c.hour in 4..6) add("dawn")
        if (!ok) return@buildList
        if (c.quarter == Quarter.FULL && ("dough" in c.crafts || "bake" in c.crafts)) add("full_bread")
        if (c.keptSpell) add("kept")
        if (c.keptRun >= 3) add("three_moons")
        if (c.newJars.any { it.far }) add("far_jar")
        if (c.jarsGained >= 5) add("shelf_grows")
        if (c.missedBefore) add("second_chance")
        if (c.distinct >= 10) add("ten")
        if (c.distinct >= 50) add("fifty")
        if (c.sabbat != null) add("wheel")
        if (c.sabbatsKept >= 4) add("four_feasts")
        if (c.isSolstice) add("solstice")
        if (c.quarter == Quarter.FULL && c.guests >= 1) add("sabbat")
        if (c.guests >= 4) add("feast")
        if (c.age < 1.85) add("clean_slate")
        if (c.usedLeftovers > 0) add("nothing_lost")
        if (c.intent == "love") add("for_someone")
    }
}

/** The intentions a dish can be cooked with. */
object Intents {
    val all = listOf("courage", "calm", "warmth", "joy", "love", "luck")
    fun name(id: String) = when (id) {
        "courage" -> tr("на смелость", "for courage"); "calm" -> tr("на спокойствие", "for calm"); "warmth" -> tr("на тепло", "for warmth")
        "joy" -> tr("на радость", "for joy"); "love" -> tr("на любовь", "for love"); "luck" -> tr("на удачу", "for luck"); else -> ""
    }
}
