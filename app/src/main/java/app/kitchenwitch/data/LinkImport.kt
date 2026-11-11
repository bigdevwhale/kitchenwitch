package app.kitchenwitch.data

import android.text.Html
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

const val AGENT = "Mozilla/5.0 (Linux; Android 14) KitchenWitch/1.0"

/** What a recipe page gave: anything may be empty; the witch checks it in the editor. */
data class Found(val title: String, val origin: String, val minutes: Int, val ingredients: List<String>, val steps: List<String>, val url: String) {
    val complete get() = title.isNotBlank() && ingredients.isNotEmpty() && steps.isNotEmpty()
}

/**
 * Reads a recipe from a web page through its schema.org/Recipe markup (JSON-LD), which most recipe sites publish.
 * Only the page itself is fetched; nothing is sent anywhere else.
 */
object LinkImport {
    private val urlRe = Regex("""https?://\S+""")
    fun urlIn(text: String): String? = urlRe.find(text)?.value?.trimEnd('.', ',', ')', '»', '"')

    fun read(url: String): Found {
        val html = fetch(url)
        val blocks = Regex("""<script[^>]*application/ld\+json[^>]*>(.*?)</script>""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
            .findAll(html).map { it.groupValues[1].trim() }
        for (b in blocks) {
            val node = runCatching { if (b.startsWith("[")) JSONArray(b) else JSONObject(b) }.getOrNull() ?: continue
            findRecipe(node)?.let { return parse(it, url) }
        }
        val og = Regex("""<meta[^>]+property=["']og:title["'][^>]+content=["']([^"']+)""", RegexOption.IGNORE_CASE).find(html)?.groupValues?.get(1)
            ?: Regex("""<title>(.*?)</title>""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)).find(html)?.groupValues?.get(1)
        return Found(clean(og.orEmpty()), "", 0, emptyList(), emptyList(), url)
    }

    private fun fetch(url: String): String {
        var u = URL(url)
        repeat(5) {
            val c = u.openConnection() as HttpURLConnection
            c.connectTimeout = 9000; c.readTimeout = 12000; c.instanceFollowRedirects = true
            c.setRequestProperty("User-Agent", AGENT); c.setRequestProperty("Accept", "text/html")
            try {
                val code = c.responseCode
                if (code in 300..399) { c.getHeaderField("Location")?.let { u = URL(u, it) }; return@repeat }
                if (code !in 200..299) error("HTTP $code")
                return c.inputStream.bufferedReader().use { it.readText() }
            } finally { c.disconnect() }
        }
        error("too many redirects")
    }

    private fun isRecipe(o: JSONObject): Boolean = when (val t = o.opt("@type")) {
        is String -> t.equals("Recipe", true)
        is JSONArray -> (0 until t.length()).any { t.optString(it).equals("Recipe", true) }
        else -> false
    }

    private fun findRecipe(node: Any?): JSONObject? = when (node) {
        is JSONObject -> if (isRecipe(node)) node else findRecipe(node.opt("@graph")) ?: findRecipe(node.opt("mainEntity"))
        is JSONArray -> (0 until node.length()).firstNotNullOfOrNull { findRecipe(node.opt(it)) }
        else -> null
    }

    private fun parse(o: JSONObject, url: String): Found {
        val ings = strings(o.opt("recipeIngredient") ?: o.opt("ingredients")).map(::clean).filter { it.isNotBlank() }
        val steps = steps(o.opt("recipeInstructions")).map(::clean).filter { it.isNotBlank() }
        val mins = duration(o.optString("totalTime")).takeIf { it > 0 } ?: (duration(o.optString("prepTime")) + duration(o.optString("cookTime")))
        val origin = strings(o.opt("recipeCuisine")).firstOrNull().orEmpty()
        return Found(clean(o.optString("name")), clean(origin), mins, ings, steps, url)
    }

    private fun strings(v: Any?): List<String> = when (v) {
        is String -> listOf(v)
        is JSONArray -> (0 until v.length()).flatMap { strings(v.opt(it)) }
        is JSONObject -> listOfNotNull(v.optString("text").ifBlank { v.optString("name") }.ifBlank { null })
        else -> emptyList()
    }

    private fun steps(v: Any?): List<String> = when (v) {
        is String -> v.split(Regex("""\n+|(?<=\.)\s{2,}""")).map { it.trim() }
        is JSONArray -> (0 until v.length()).flatMap { steps(v.opt(it)) }
        is JSONObject -> if (v.has("itemListElement")) steps(v.opt("itemListElement")) else listOfNotNull(v.optString("text").ifBlank { v.optString("name") }.ifBlank { null })
        else -> emptyList()
    }

    /** ISO 8601 durations as recipes write them: PT1H30M → 90. */
    fun duration(s: String): Int {
        val m = Regex("""P(?:(\d+)D)?T?(?:(\d+)H)?(?:(\d+)M)?""").find(s.uppercase()) ?: return 0
        val (d, h, mi) = m.destructured
        return (d.toIntOrNull() ?: 0) * 1440 + (h.toIntOrNull() ?: 0) * 60 + (mi.toIntOrNull() ?: 0)
    }

    private fun clean(s: String): String = Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY).toString().replace(' ', ' ').replace(Regex("""\s+"""), " ").trim()
}
