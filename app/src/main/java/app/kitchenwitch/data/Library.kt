package app.kitchenwitch.data

import android.content.Context
import app.kitchenwitch.domain.Lang
import app.kitchenwitch.domain.Reading

/** A recipe of the witch's library (assets/library.tsv), in both languages. */
class LibRecipe(
    val id: String, val minutes: Int, val months: Set<Int>,
    private val titleRu: String, private val titleEn: String, private val fromRu: String, private val fromEn: String,
    private val ingRu: List<String>, private val ingEn: List<String>, private val stepRu: List<String>, private val stepEn: List<String>,
) {
    val title get() = if (Lang.ru) titleRu else titleEn
    val from get() = if (Lang.ru) fromRu else fromEn
    val ingredients get() = if (Lang.ru) ingRu else ingEn
    val steps get() = if (Lang.ru) stepRu else stepEn
    /** Searched by name, origin and ingredients in both languages. */
    val haystack = listOf(titleRu, titleEn, fromRu, fromEn, ingRu.joinToString(" "), ingEn.joinToString(" ")).joinToString(" ").lowercase().replace('ё', 'е')

    fun toRecipe(now: Long) = Recipe(title = title, origin = from, minutes = minutes, ingredients = ingredients.joinToString("\n"),
        steps = steps.joinToString("\n"), source = "lib", libId = id, created = now)
}

object Library {
    @Volatile private var cache: List<LibRecipe>? = null

    fun all(ctx: Context): List<LibRecipe> = cache ?: synchronized(this) {
        cache ?: ctx.assets.open("library.tsv").bufferedReader().readLines().filter { it.isNotBlank() }.map { line ->
            val c = line.split('\t')
            LibRecipe(c[0], c[1].toInt(), c[2].split(',').mapNotNull { it.trim().toIntOrNull() }.toSet(), c[3], c[4], c[5], c[6],
                c[7].split(';').map { it.trim() }, c[8].split(';').map { it.trim() }, c[9].split('|').map { it.trim() }, c[10].split('|').map { it.trim() })
        }.also { cache = it }
    }
    fun byId(ctx: Context, id: String) = all(ctx).firstOrNull { it.id == id }
}

/** What the screens show of a recipe, whether it comes from the library or the witch's own scrolls. */
data class Dish(
    val key: String,                 // "lib:<id>" or "own:<id>"
    val title: String, val from: String, val minutes: Int,
    val ingredients: List<String>, val steps: List<String>,
    val months: Set<Int> = emptySet(),
    val libId: String? = null, val recipeId: Long? = null,
    val page: String = "",
) {
    val reagents get() = Reading.reagents(ingredients)

    companion object {
        fun of(l: LibRecipe) = Dish("lib:${l.id}", l.title, l.from, l.minutes, l.ingredients, l.steps, l.months, libId = l.id)
        fun of(r: Recipe) = Dish(if (r.libId.isNotEmpty()) "lib:${r.libId}" else "own:${r.id}", r.title, r.origin, r.minutes,
            Reading.lines(r.ingredients), Reading.lines(r.steps), libId = r.libId.ifEmpty { null }, recipeId = r.id, page = r.page)
    }
}
