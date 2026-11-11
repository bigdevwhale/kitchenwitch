package app.kitchenwitch.domain

/** A kitchen craft a recipe teaches. Found from the verbs of its steps. */
class Craft(val id: String, val ru: String, val en: String, val stems: List<String>) { val name get() = tr(ru, en) }

object Crafts {
    val all = listOf(
        Craft("fry", "жарка", "frying", listOf("обжар", "жар", "fry", "sear", "saute", "sauté")),
        Craft("stew", "тушение", "stewing", listOf("туш", "stew", "simmer")),
        Craft("braise", "томление", "slow cooking", listOf("томи", "томл", "braise", "slow")),
        Craft("roast", "запекание", "roasting", listOf("запек", "roast", "grill")),
        Craft("bake", "выпечка", "baking", listOf("выпек", "в духовк", "bake", "oven")),
        Craft("boil", "варка", "boiling", listOf("отвар", "свар", "вари", "кипят", "boil", "poach", "blanch")),
        Craft("whip", "взбивание", "whipping", listOf("взб", "whisk", "whip", "beat")),
        Craft("dough", "тесто", "dough", listOf("тест", "замес", "раскат", "dough", "knead", "roll out")),
        Craft("marinate", "маринад", "marinating", listOf("маринов", "маринад", "marinat")),
        Craft("blend", "блендер", "blending", listOf("пробе", "блендер", "пюрир", "blend", "purée", "puree")),
        Craft("shape", "лепка", "shaping", listOf("слеп", "лепи", "сформир", "shape", "form into")),
        Craft("ferment", "ферментация", "fermenting", listOf("заква", "фермент", "ferment", "pickle")),
        Craft("steam", "на пару", "steaming", listOf("на пару", "steam")),
        Craft("emulsion", "эмульсия", "emulsion", listOf("эмульс", "майонез", "emulsif", "mayonnaise")),
    )
    val byId = all.associateBy { it.id }
    fun of(steps: List<String>): List<String> {
        val t = " " + steps.joinToString(" ").lowercase().replace('ё', 'е')
        return all.filter { c -> c.stems.any { t.contains(" $it") } }.map { it.id }
    }
}

/** What the app reads out of a recipe as it is written: jars, candles, crafts. */
object Reading {
    private val minRe = Regex("""(\d+)\s*(?:-|–)?\s*(?:\d+\s*)?(мин|min)""", RegexOption.IGNORE_CASE)
    private val hourRe = Regex("""(\d+(?:[.,]\d)?)\s*(час|ч\.|h\b|hour)""", RegexOption.IGNORE_CASE)

    /** Minutes a step asks to wait, for its candle; 0 when it says none. */
    fun timer(step: String): Int {
        hourRe.find(step)?.let { return (it.groupValues[1].replace(',', '.').toDouble() * 60).toInt() }
        minRe.find(step)?.let { return it.groupValues[1].toInt() }
        return 0
    }

    private val qty = Regex("""^[\d.,/½¼¾⅓\-–\s]+""")
    private val units = Regex("""^(г|гр|кг|мл|л|шт\.?|ст\.?\s*л\.?|ч\.?\s*л\.?|стакана?|стаканов|зубчика?|зубчиков|пучок|пучка|щепотка|щепотки|банка|банки|g|kg|ml|l|tbsp|tsp|cups?|cloves?|pinch|bunch|cans?)\b\.?\s*""", RegexOption.IGNORE_CASE)

    /** The bare thing an ingredient line names: «200 г тыквы» → «тыквы». */
    fun bare(line: String): String = line.trim().replace(qty, "").replace(units, "").substringBefore(',').substringBefore('(').trim().ifEmpty { line.trim() }

    /** The words a sigil is drawn from: a jar's name when the line names one, else the bare thing. */
    fun reagents(lines: List<String>): List<String> = lines.map { l -> Jars.of(l)?.id ?: bare(l).lowercase() }

    fun lines(text: String): List<String> = text.split('\n').map { it.trim().trimStart('-', '•', '*', '·').trim() }.filter { it.isNotEmpty() }
}
