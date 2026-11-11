package app.kitchenwitch.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.kitchenwitch.domain.Jar
import app.kitchenwitch.domain.Rand
import app.kitchenwitch.domain.hash
import app.kitchenwitch.ui.theme.C
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** The moon in phase [p] (0 new, .5 full): a dark disc with the lit part on top. */
fun DrawScope.moon(c: Offset, r: Float, p: Double, lit: Color = C.moonLit, dark: Color = C.moonDark, craters: Boolean = true) {
    drawCircle(dark, r, c)
    val rx = (abs(cos(2 * PI * p)) * r).toFloat()
    val full = Rect(c.x - r, c.y - r, c.x + r, c.y + r)
    val ell = Rect(c.x - rx, c.y - r, c.x + rx, c.y + r)
    val path = Path().apply {
        moveTo(c.x, c.y - r)
        if (p < .5) { arcTo(full, -90f, 180f, false); arcTo(ell, 90f, if (p < .25) -180f else 180f, false) }
        else { arcTo(full, -90f, -180f, false); arcTo(ell, 90f, if (p < .75) -180f else 180f, false) }
        close()
    }
    drawPath(path, lit)
    if (craters) {
        val sh = Color.Black.copy(alpha = .07f)
        drawCircle(sh, r * .16f, Offset(c.x - r * .3f, c.y - r * .2f)); drawCircle(sh, r * .11f, Offset(c.x + r * .25f, c.y + r * .35f)); drawCircle(sh, r * .07f, Offset(c.x + r * .1f, c.y - r * .45f))
    }
}

@Composable
fun MoonDot(p: Double, size: Dp, modifier: Modifier = Modifier) = Canvas(modifier.size(size)) {
    val r = this.size.minDimension / 2; moon(center, r - 1f, p); drawCircle(C.moonLit.copy(alpha = .25f), r - 1f, center, style = Stroke(1.2f))
}

/** The points of a sigil: each reagent's hash places one point on a circle of 100. */
private fun sigilPoints(reagents: List<String>): List<Offset> = reagents.ifEmpty { listOf("?") }.map { n ->
    val v = hash(n.lowercase().trim()).toLong() and 0xffffffffL
    val a = (v % 360) * PI / 180; val rr = 12 + ((v ushr 9) % 28)
    Offset((50 + cos(a) * rr).toFloat(), (50 + sin(a) * rr).toFloat())
}

/**
 * A dish's sigil, drawn from its reagents: a ring with ticks, a path through the reagents' points, a ring where it starts
 * and a bar where it ends. [progress] draws it stroke by stroke; [glow] adds the golden haze.
 */
@Composable
fun Sigil(reagents: List<String>, size: Dp, modifier: Modifier = Modifier, color: Color = C.gold, progress: Float = 1f, glow: Boolean = true) {
    val pts = sigilPoints(reagents)
    Canvas(modifier.size(size)) {
        val k = this.size.minDimension / 100f
        fun o(p: Offset) = Offset(p.x * k, p.y * k)
        val c = Offset(50 * k, 50 * k)
        if (glow) drawCircle(Brush.radialGradient(listOf(color.copy(alpha = .16f * progress), Color.Transparent), c, 50 * k), 50 * k, c)
        drawCircle(color.copy(alpha = .6f), 48 * k, c, style = Stroke(.9f * k))
        for (i in 0 until 12) { val a = i * PI / 6; drawCircle(color, (if (i % 3 == 0) 1.7f else .8f) * k, Offset(c.x + (cos(a) * 44 * k).toFloat(), c.y + (sin(a) * 44 * k).toFloat())) }
        drawCircle(color.copy(alpha = .5f), 38 * k, c, style = Stroke(.7f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(1f * k, 3f * k))))
        val path = Path().apply { moveTo(o(pts[0]).x, o(pts[0]).y); pts.drop(1).forEach { lineTo(o(it).x, o(it).y) } }
        val shown = if (progress >= 1f) path else Path().also { dst -> PathMeasure().apply { setPath(path, false); getSegment(0f, length * progress, dst, true) } }
        if (glow) drawPath(shown, color.copy(alpha = .25f), style = Stroke(6.5f * k, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(shown, color, style = Stroke(2.3f * k, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(color, 3.4f * k, o(pts[0]), style = Stroke(1.8f * k))
        if (progress >= .98f && pts.size > 1) {
            val a = pts.last(); val b = pts[pts.size - 2]; val l = hypot(a.x - b.x, a.y - b.y).coerceAtLeast(.01f)
            val n = Offset(-(a.y - b.y) / l * 6, (a.x - b.x) / l * 6)
            drawLine(color, o(a + n), o(a - n), 2.3f * k, StrokeCap.Round)
        }
    }
}

/** A jar on the shelf: glass, its contents, a cork and a paper label with the initial. Empty jars are a dashed outline. */
fun DrawScope.jar(j: Jar?, initial: Char?, empty: Boolean) {
    val k = size.width / 60f
    fun r(x: Float, y: Float, w: Float, h: Float) = Rect(x * k, y * k, (x + w) * k, (y + h) * k)
    if (empty || j == null) {
        val dash = Stroke(1.3f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3 * k, 3 * k)))
        drawRoundRect(C.dim, r(9f, 20f, 42f, 52f).topLeft, r(9f, 20f, 42f, 52f).size, CornerRadius(10 * k), style = dash)
        drawRoundRect(C.dim, r(17f, 10f, 26f, 9f).topLeft, r(17f, 10f, 26f, 9f).size, CornerRadius(2 * k), style = dash)
        return
    }
    val body = r(9f, 20f, 42f, 52f)
    val glass = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(body, CornerRadius(10 * k))) }
    clipPath(glass) {
        val top = 30 + (abs(hash(j.id)) % 10)
        drawRect(Color(0xFF000000 or j.color), Offset(9 * k, top * k), Size(42 * k, 50 * k), alpha = .92f)
    }
    drawPath(glass, Color.White.copy(alpha = .06f))
    drawPath(glass, Color.White.copy(alpha = .33f), style = Stroke(1.1f * k))
    drawRoundRect(Color.White.copy(alpha = .2f), Offset(13 * k, 26 * k), Size(4 * k, 34 * k), CornerRadius(2 * k))
    drawRoundRect(Color.White.copy(alpha = .08f), r(17f, 12f, 26f, 9f).topLeft, r(17f, 12f, 26f, 9f).size, CornerRadius(2 * k))
    drawRoundRect(Color(0xFF8A6440), Offset(18 * k, 3 * k), Size(24 * k, 10 * k), CornerRadius(2 * k))
    drawRoundRect(C.paper, Offset(16 * k, 40 * k), Size(28 * k, 18 * k), CornerRadius(2 * k))
}

/** A drawn plate for casts without a photo: the dish's colours on a plate on a dark table. */
fun DrawScope.plate(title: String, colors: List<Color>) {
    val r = Rand(hash(title)); val w = size.width; val c = center
    drawRect(Color(0xFF1C2A20))
    drawCircle(Color.Black.copy(alpha = .35f), w * .42f, c + Offset(w * .02f, w * .04f))
    drawCircle(Color(0xFFF1EBE0), w * .41f, c); drawCircle(Color(0xFFE2DACB), w * .33f, c)
    clipPath(Path().apply { addOval(Rect(c, w * .31f)) }) {
        for (i in 0 until 9) {
            val a = r.next() * 6.28f; val d = r.next() * .2f * w
            val p = Offset(c.x + cos(a) * d, c.y + sin(a) * d)
            translate(p.x, p.y) { drawOval(colors[i % colors.size], Offset(-(.07f + r.next() * .1f) * w, -(.05f + r.next() * .07f) * w), Size((.14f + r.next() * .2f) * w, (.1f + r.next() * .14f) * w)) }
        }
        for (i in 0 until 4) drawOval(Color(0xFF6F9A4E), Offset(w * (.34f + r.next() * .3f), w * (.34f + r.next() * .3f)), Size(w * .06f, w * .03f))
    }
    drawRect(Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .4f)), c, w * .75f), blendMode = BlendMode.SrcOver)
}

/** Warm colours for a dish's drawn plate, from its jars when it has any. */
fun plateColors(title: String, jars: List<Jar>): List<Color> {
    val base = listOf(0xD9A050L, 0xC8823AL, 0xE8C890L, 0x7A8A3AL, 0xB8432EL, 0xF2E2B8L)
    val r = Rand(hash(title))
    return (jars.map { it.color } + base.shuffled(java.util.Random(hash(title).toLong()))).take(4).map { Color(0xFF000000 or it) }.also { r.next() }
}

/** A bone die face. */
fun DrawScope.die(n: Int) {
    val k = size.width / 60f
    drawRoundRect(Color.Black.copy(alpha = .35f), Offset(5 * k, 8 * k), Size(54 * k, 54 * k), CornerRadius(12 * k))
    drawRoundRect(Color(0xFFE9DFC9), Offset(3 * k, 3 * k), Size(54 * k, 54 * k), CornerRadius(12 * k))
    drawRoundRect(Color(0xFFB8A888), Offset(3 * k, 3 * k), Size(54 * k, 54 * k), CornerRadius(12 * k), style = Stroke(2 * k))
    val p = mapOf(1 to listOf(2 to 2), 2 to listOf(1 to 1, 3 to 3), 3 to listOf(1 to 1, 2 to 2, 3 to 3), 4 to listOf(1 to 1, 3 to 1, 1 to 3, 3 to 3),
        5 to listOf(1 to 1, 3 to 1, 2 to 2, 1 to 3, 3 to 3), 6 to listOf(1 to 1, 3 to 1, 1 to 2, 3 to 2, 1 to 3, 3 to 3))
    p.getValue(n.coerceIn(1, 6)).forEach { (x, y) -> drawCircle(Color(0xFF3A2A30), 5 * k, Offset(x * 15 * k, y * 15 * k)) }
}

/** A witch's mark: a double ring with one of four signs. */
fun DrawScope.markGlyph(id: String, color: Color) {
    val k = size.width / 42f
    drawCircle(color, 19 * k, center, style = Stroke(1.2f * k))
    drawCircle(color, 16 * k, center, style = Stroke(.6f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(1 * k, 2.5f * k))))
    val s = Stroke(1.7f * k, cap = StrokeCap.Round, join = StrokeJoin.Round)
    fun P(vararg xy: Float) = Path().apply { moveTo(xy[0] * k, xy[1] * k); var i = 2; while (i < xy.size) { lineTo(xy[i] * k, xy[i + 1] * k); i += 2 } }
    when (abs(hash(id)) % 4) {
        0 -> drawPath(P(21f, 10f, 24f, 18f, 32f, 18f, 26f, 23f, 28f, 31f, 21f, 26f, 14f, 31f, 16f, 23f, 10f, 18f, 18f, 18f, 21f, 10f), color, style = s)
        1 -> { moon(center, 11 * k, .2, color, Color.Transparent, false) }
        2 -> { drawPath(P(12f, 28f, 21f, 10f, 30f, 28f), color, style = s); drawLine(color, Offset(15 * k, 22 * k), Offset(27 * k, 22 * k), 1.7f * k) }
        else -> { drawLine(color, Offset(21 * k, 10 * k), Offset(21 * k, 32 * k), 1.6f * k); drawLine(color, Offset(12 * k, 16 * k), Offset(30 * k, 26 * k), 1.6f * k); drawLine(color, Offset(30 * k, 16 * k), Offset(12 * k, 26 * k), 1.6f * k) }
    }
}
