package app.kitchenwitch.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.kitchenwitch.container
import app.kitchenwitch.data.Cast
import app.kitchenwitch.data.Dish
import app.kitchenwitch.data.Leftover
import app.kitchenwitch.data.Recipe
import app.kitchenwitch.data.Settings
import app.kitchenwitch.domain.tr
import app.kitchenwitch.ui.art.Fireflies
import app.kitchenwitch.ui.book.BookScreen
import app.kitchenwitch.ui.cook.CookScreen
import app.kitchenwitch.ui.home.HomeScreen
import app.kitchenwitch.ui.moon.MoonScreen
import app.kitchenwitch.ui.sheets.Sheets
import app.kitchenwitch.ui.shelf.ShelfScreen
import app.kitchenwitch.ui.theme.C
import app.kitchenwitch.ui.theme.T
import app.kitchenwitch.ui.wheel.WheelScreen
import kotlinx.coroutines.delay

/** Where a demo/screenshot launch opens (set from the intent in demo builds only). */
object Start { var tab = 0; var sheet: String? = null; var cook: String? = null }

/** What is open over the tabs. */
sealed class Sheet {
    data object Dice : Sheet()
    data object Write : Sheet()
    data object Library : Sheet()
    data class Link(val url: String = "") : Sheet()
    data object Page : Sheet()
    data class Editor(val draft: Draft) : Sheet()
    data class Scroll(val dish: Dish) : Sheet()
    data class Entry(val recipeId: Long) : Sheet()
    data class JarInfo(val id: String) : Sheet()
    data object Marks : Sheet()
    data object Pantry : Sheet()
    data object Settings : Sheet()
}

/** A recipe being written in the editor. */
data class Draft(val title: String = "", val origin: String = "", val minutes: String = "", val ingredients: String = "", val steps: String = "",
                 val source: String = "own", val url: String = "", val page: String = "", val note: String = "", val memory: Boolean = false, val editId: Long = 0)

class Nav {
    var tab by mutableIntStateOf(0)
    var sheet by mutableStateOf<Sheet?>(null)
    var cook by mutableStateOf<Dish?>(null)
    var bookTab by mutableIntStateOf(0)
    /** The next cook opens on «how did it go?» – a dish written from memory. */
    var memory by mutableStateOf(false)
    fun open(s: Sheet) { sheet = s }
}

/** Everything the screens read, collected once. */
class Data(val s: Settings, val recipes: List<Recipe>, val casts: List<Cast>, val leftovers: List<Leftover>) {
    val good = casts.filter { it.verdict != "miss" }
    val misses = casts.filter { it.verdict == "miss" }
    val cookedIds = good.map { it.recipeId }.toSet()
    val spellRecipe get() = recipes.firstOrNull { it.id == s.spell }
}

@Composable
fun Root(s: Settings, shared: String?) {
    val c = LocalContext.current.container
    val recipes by c.dao.recipes().collectAsState(initial = emptyList())
    val casts by c.dao.casts().collectAsState(initial = emptyList())
    val leftovers by c.dao.leftovers().collectAsState(initial = emptyList())
    val data = remember(s, recipes, casts, leftovers) { Data(s, recipes, casts, leftovers) }
    val nav = remember { Nav().apply { tab = Start.tab } }
    var note by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { c.news.collect { note = it; delay(2600); note = null } }
    LaunchedEffect(shared) { shared?.let { nav.sheet = Sheet.Link(it) } }
    LaunchedEffect(Unit) { c.refreshOutside() }
    LaunchedEffect(Unit) {
        Start.cook?.let { id -> app.kitchenwitch.data.Library.byId(c.app, id)?.let { nav.cook = Dish.of(it) } }
        nav.sheet = when (Start.sheet) { "dice" -> Sheet.Dice; "write" -> Sheet.Write; "library" -> Sheet.Library; "marks" -> Sheet.Marks; "pantry" -> Sheet.Pantry; else -> null }
        Start.sheet = null; Start.cook = null
    }

    Box(Modifier.fillMaxSize().background(C.bg).background(Brush.radialGradient(listOf(C.bgHi, Color.Transparent), Offset(540f, 0f), 1100f))) {
        val cook = nav.cook
        if (cook != null) {
            BackHandler { nav.cook = null }
            CookScreen(cook, data, nav)
        } else {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).statusBarsPadding()) {
                    when (nav.tab) {
                        0 -> HomeScreen(data, nav)
                        1 -> MoonScreen(data, nav)
                        2 -> BookScreen(data, nav)
                        3 -> ShelfScreen(data, nav)
                        else -> WheelScreen(data, nav)
                    }
                }
                Dock(nav.tab) { nav.tab = it }
            }
        }
        Fireflies(Modifier.fillMaxSize())
        Sheets(data, nav)
        AnimatedVisibility(note != null, Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 12.dp, start = 18.dp, end = 18.dp), enter = fadeIn(), exit = fadeOut()) {
            Text(note ?: "", style = T.body, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(C.card2).padding(horizontal = 16.dp, vertical = 11.dp))
        }
    }
}

@Composable
private fun Dock(sel: Int, onSel: (Int) -> Unit) {
    val tabs = listOf(tr("Кухня", "Kitchen"), tr("Луна", "Moon"), tr("Книга", "Book"), tr("Полка", "Shelf"), tr("Колесо", "Wheel"))
    Row(Modifier.fillMaxWidth().background(C.nav).navigationBarsPadding().padding(top = 8.dp, bottom = 10.dp)) {
        tabs.forEachIndexed { i, label ->
            val col = if (i == sel) C.gold else C.dim
            Column(Modifier.weight(1f).clickable(remember { MutableInteractionSource() }, null) { onSel(i) }, horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(Modifier.size(24.dp)) { tabIcon(i, col) }
                Text(label, style = T.tiny.copy(color = col), modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
}

private fun DrawScope.tabIcon(i: Int, c: Color) {
    val k = size.width / 24f; val s = Stroke(1.6f * k, join = StrokeJoin.Round)
    when (i) {
        0 -> { val p = Path().apply { moveTo(5 * k, 21 * k); lineTo(5 * k, 10 * k); arcTo(Rect(5 * k, 3 * k, 19 * k, 17 * k), 180f, 180f, false); lineTo(19 * k, 21 * k); close() }
            drawPath(p, c, style = s); drawLine(c, Offset(12 * k, 3 * k), Offset(12 * k, 21 * k), 1.2f * k); drawLine(c, Offset(5 * k, 13 * k), Offset(19 * k, 13 * k), 1.2f * k) }
        1 -> { val p = Path().apply { moveTo(15 * k, 3 * k); arcTo(Rect(3 * k, 3 * k, 21 * k, 21 * k), -70f, -260f, false); cubicTo(16 * k, 19 * k, 9 * k, 10 * k, 15 * k, 3 * k); close() }; drawPath(p, c, style = s) }
        2 -> { drawRoundRect(c, Offset(4 * k, 4 * k), Size(16 * k, 16 * k), androidx.compose.ui.geometry.CornerRadius(2 * k), style = s); drawLine(c, Offset(12 * k, 4 * k), Offset(12 * k, 20 * k), 1.2f * k)
            drawCircle(c, 2 * k, Offset(16 * k, 9 * k), style = Stroke(1f * k)) }
        3 -> { drawRoundRect(c, Offset(6 * k, 8 * k), Size(12 * k, 13 * k), androidx.compose.ui.geometry.CornerRadius(3 * k), style = s); drawRect(c, Offset(8 * k, 4 * k), Size(8 * k, 3 * k), style = s)
            drawLine(c, Offset(8 * k, 14 * k), Offset(16 * k, 14 * k), 1.2f * k) }
        else -> { drawCircle(c, 9 * k, center, style = s); for (a in 0 until 8) { val r = Math.toRadians(a * 45.0)
            drawLine(c, center, Offset(center.x + (Math.cos(r) * 9 * k).toFloat(), center.y + (Math.sin(r) * 9 * k).toFloat()), .9f * k) }
            drawCircle(c, 2.2f * k, center) }
    }
}
