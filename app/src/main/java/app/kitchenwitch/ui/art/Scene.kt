package app.kitchenwitch.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.runtime.withFrameMillis
import app.kitchenwitch.BuildConfig
import app.kitchenwitch.domain.Rand
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

/** The light outside: 0 night, 1 day; in between at dawn and dusk. */
fun daylight(now: Long, sunrise: Long, sunset: Long): Float {
    val h = java.time.LocalTime.now().let { it.hour + it.minute / 60f }
    if (sunrise <= 0 || sunset <= 0) return when { h < 6 || h >= 18.5f -> 0f; h < 7.5f -> (h - 6) / 1.5f; h > 17 -> (18.5f - h) / 1.5f; else -> 1f }
    val hour = 3600_000f
    return when {
        now < sunrise - hour / 2 || now > sunset + hour / 2 -> 0f
        now < sunrise + hour -> ((now - (sunrise - hour / 2)) / (1.5f * hour)).coerceIn(0f, 1f)
        now > sunset - hour -> (((sunset + hour / 2) - now) / (1.5f * hour)).coerceIn(0f, 1f)
        else -> 1f
    }
}

/** Seconds since the scene appeared; frozen at 0 in screenshot builds. */
@Composable
fun clock(): Float {
    val t by produceState(0f) { if (BuildConfig.SHOTS) return@produceState; val start = withFrameMillis { it }; while (true) withFrameMillis { value = (it - start) / 1000f } }
    return t
}

private class Tree(val x: Float, val base: Float, val h: Float, val w: Float)

private fun DrawScope.tree(t: Tree, c: Color) {
    val p = Path()
    for (k in 0 until 4) {
        val top = t.base - t.h + k * t.h * .2f; val bot = top + t.h * .38f; val hw = t.w * (.22f + .17f * k)
        p.moveTo(t.x, top); p.lineTo(t.x + hw, bot); p.lineTo(t.x - hw, bot); p.close()
    }
    drawPath(p, c); drawRect(c, Offset(t.x - 1.5f, t.base - t.h * .25f), Size(3f, t.h * .25f))
}

/**
 * The kitchen window: an arched frame onto a pine forest under the real moon and the real weather, fog drifting,
 * fireflies at night. On the sill: rosemary, candles, a cauldron that glows and steams, a jar of fireflies, books.
 * Drawn in a 360 × 262 space and scaled to the width.
 */
@Composable
fun WindowScene(phase: Double, weather: String, light: Float, modifier: Modifier = Modifier) {
    val t = clock()
    val r = remember { Rand(11) }
    val far = remember { buildList { var x = 30f; while (x < 335) { add(Tree(x, 196f, 48 + r.next() * 36, 24 + r.next() * 10)); x += 14 + r.next() * 14 } } }
    val mid = remember { buildList { var x = 24f; while (x < 340) { add(Tree(x, 214f, 70 + r.next() * 50, 30 + r.next() * 14)); x += 22 + r.next() * 22 } } }
    val near = remember { listOf(Tree(54f, 236f, 150f, 58f), Tree(306f, 236f, 170f, 62f), Tree(92f, 236f, 96f, 40f), Tree(276f, 236f, 88f, 38f)) }
    val stars = remember { val q = Rand(7); List(44) { floatArrayOf(44 + q.next() * 272, 14 + q.next() * 110, .4f + q.next(), q.next() * 6f) } }
    val flies = remember { val q = Rand(3); List(9) { floatArrayOf(60 + q.next() * 240, 120 + q.next() * 90, q.next() * 6f, 4 + q.next() * 4) } }
    val drops = remember { val q = Rand(5); List(70) { floatArrayOf(q.next() * 360, q.next() * 240, .6f + q.next() * .6f) } }
    Canvas(modifier.fillMaxWidth().aspectRatio(360f / 262f)) {
        val k = size.width / 360f
        scale(k, k, Offset.Zero) {
            val night = 1f - light
            val arch = Path().apply { moveTo(44f, 236f); lineTo(44f, 104f); arcTo(Rect(44f, 12f, 316f, 196f), 180f, 180f, false); lineTo(316f, 236f); close() }
            val grey = when (weather) { "OVERCAST", "RAIN", "STORM" -> .55f; "CLOUDY", "SNOW" -> .3f; "FOG" -> .45f; else -> 0f }
            clipPath(arch) {
                val skyTop = lerp(lerp(Color(0xFF06110D), Color(0xFF5E8270), light), Color(0xFF5A665E), grey * light)
                val skyBot = lerp(lerp(Color(0xFF1A3A30), Color(0xFFE2C98E), light), Color(0xFFA0A89A), grey * light)
                drawRect(Brush.verticalGradient(listOf(skyTop, skyBot), 0f, 236f), Offset.Zero, Size(360f, 262f))
                if (night > .3f && grey < .5f) stars.forEach { s -> drawCircle(Color.White.copy(alpha = (night * (.35f + .45f * (.5f + .5f * sin(t * 1.3f + s[3])))).coerceIn(0f, 1f)), s[2], Offset(s[0], s[1])) }
                if (grey < .5f) {
                    drawCircle(Brush.radialGradient(listOf(Color(0xFFF4ECD0).copy(alpha = .35f * (.4f + .6f * night)), Color.Transparent), Offset(248f, 62f), 56f), 56f, Offset(248f, 62f))
                    moon(Offset(248f, 62f), 20f, phase, Color(0xFFF4ECD0), lerp(Color(0xFF1D3A30), Color(0xFF7E9A86), light))
                }
                if (grey > 0f) for (i in 0 until 5) {
                    val cx = ((i * 83f + t * (4 + i)) % 420f) - 30f; val cy = 30f + (i * 23) % 60
                    drawOval(Color(0xFFCAD3CC).copy(alpha = grey * (.35f + .25f * light)), Offset(cx - 60, cy - 14), Size(120f, 28f))
                }
                val fog = if (weather == "FOG") 2.2f else 1f
                drawOval(Color(0xFFD8EADF).copy(alpha = (.12f + .1f * light) * fog), Offset(-10f + 30 * sin(t / 7f), 172f), Size(300f, 20f))
                far.forEach { tree(it, lerp(Color(0xFF163A2E), Color(0xFF56786A), light)) }
                mid.forEach { tree(it, lerp(Color(0xFF0D271D), Color(0xFF36584A), light)) }
                near.forEach { tree(it, lerp(Color(0xFF06140D), Color(0xFF1D3528), light)) }
                if (night > .4f && weather !in setOf("RAIN", "STORM", "SNOW")) flies.forEach { f ->
                    val a = (.5f + .5f * sin(t * 2 * PI.toFloat() / f[3] + f[2])).coerceIn(0f, 1f) * night
                    val p = Offset(f[0] + 6 * sin(t / f[3] * 2 + f[2]), f[1] - 8 * sin(t / f[3] * 1.3f + f[2]))
                    drawCircle(Color(0xFFFFE39A).copy(alpha = a * .35f), 5f, p); drawCircle(Color(0xFFFFE39A).copy(alpha = a), 1.6f, p)
                }
                drawOval(Color(0xFFD8EADF).copy(alpha = (.14f + .14f * light) * fog), Offset(-20f + 40 * sin(t / 9f + 1), 208f), Size(400f, 30f))
                drawOval(Color(0xFFD8EADF).copy(alpha = (.08f + .08f * light) * fog), Offset(-40f + 30 * sin(t / 5f), 198f), Size(200f, 18f))
                when (weather) {
                    "RAIN", "STORM" -> drops.forEach { d -> val y = (d[1] + t * 220 * d[2]) % 250f; val x = d[0] - y * .12f
                        drawLine(Color(0xFFCFE0E8).copy(alpha = .35f), Offset(x, y), Offset(x - 2.4f, y + 10f), 1f) }
                    "SNOW" -> drops.forEach { d -> val y = (d[1] + t * 18 * d[2]) % 250f; val x = d[0] + 6 * sin(t * d[2] + d[1])
                        drawCircle(Color.White.copy(alpha = .8f), 1.1f + d[2], Offset(x, y)) }
                }
                if (weather == "STORM" && (t % 9f) in 4.0f..4.12f) drawRect(Color.White.copy(alpha = .35f), Offset.Zero, Size(360f, 262f))
            }
            drawPath(arch, Color(0xFF2A1C10), style = Stroke(12f))
            drawPath(arch, Color(0xFF5A4024), style = Stroke(1.5f))
            drawRect(Color(0xFF2A1C10), Offset(177f, 12f), Size(6f, 224f))
            drawRect(Color(0xFF2A1C10), Offset(44f, 136f), Size(272f, 5f))
            drawCircle(Brush.radialGradient(listOf(Color(0xFFF2B45A).copy(alpha = .22f), Color.Transparent), Offset(180f, 230f), 90f), 90f, Offset(180f, 230f))
            herbs(78f, 30f); herbs(284f, 40f)
            // sill
            drawRoundRect(Brush.verticalGradient(listOf(Color(0xFF4A3420), Color(0xFF22160C)), 234f, 250f), Offset(8f, 234f), Size(344f, 16f), CornerRadius(2f))
            drawRect(Color(0xFF140D07), Offset(8f, 248f), Size(344f, 6f))
            rosemary(36f); candles(92f, t); cauldron(180f, t); fireflyJar(244f, t, night); books(286f)
        }
    }
}

private fun DrawScope.herbs(x: Float, len: Float) {
    drawLine(Color(0xFF6A5030), Offset(x, 0f), Offset(x, len), 1f)
    val q = Rand(x.toInt())
    repeat(9) { i ->
        val a = -.6f + q.next() * 1.2f; val l = 14 + q.next() * 14
        val e = Offset(x + sin(a) * l * .5f, len + kotlin.math.cos(a) * l)
        val p = Path().apply { moveTo(x, len); quadraticTo((x + e.x) / 2 + 3, (len + e.y) / 2, e.x, e.y) }
        drawPath(p, if (i % 3 != 0) Color(0xFF6F7F48) else Color(0xFF8A7A45), style = Stroke(1.6f))
        if (i % 2 == 1) drawCircle(if (i % 4 == 1) Color(0xFFD9A54A) else Color(0xFFC9C39A), 1.8f, e)
    }
    drawRoundRect(Color(0xFF7A5A34), Offset(x - 4, len - 3), Size(8f, 5f), CornerRadius(1f))
}

private fun DrawScope.rosemary(x: Float) {
    for (i in 0 until 8) {
        val sx = x + 4 + i * 2.8f; val tx = x - 6 + i * 5.5f; val ty = 170f + (i * 37) % 18
        drawPath(Path().apply { moveTo(sx, 210f); quadraticTo((sx + tx) / 2, ty + 18, tx, ty) }, Color(0xFF5F7A52), style = Stroke(1.6f))
    }
    drawPath(Path().apply { moveTo(x, 212f); lineTo(x + 3, 234f); lineTo(x + 25, 234f); lineTo(x + 28, 212f); close() }, Color(0xFF7A3E22))
    drawRoundRect(Color(0xFF8A4A2A), Offset(x - 2, 208f), Size(32f, 6f), CornerRadius(1f))
}

private fun DrawScope.flame(cx: Float, base: Float, t: Float, seed: Float, s: Float = 1f) {
    val f = 1f + .08f * sin(t * 9 + seed) + .05f * sin(t * 23 + seed * 2)
    val sway = 1.2f * sin(t * 5 + seed)
    val h = 12f * s * f
    val p = Path().apply { moveTo(cx + sway, base - h); cubicTo(cx + 3.9f * s, base - h * .5f, cx + 3.9f * s, base - 1, cx, base); cubicTo(cx - 3.9f * s, base - 1, cx - 3.9f * s, base - h * .5f, cx + sway, base - h) }
    drawPath(p, Color(0xFFFFC760))
    drawCircle(Color(0xFFFFF4CC), 1.6f * s, Offset(cx, base - 3.5f * s))
}

private fun DrawScope.candles(x: Float, t: Float) {
    val pulse = .85f + .15f * sin(t * 3)
    drawCircle(Brush.radialGradient(listOf(Color(0xFFF2B45A).copy(alpha = .45f * pulse), Color.Transparent), Offset(x + 14, 200f), 40f), 40f, Offset(x + 14, 200f))
    listOf(0f to 30f, 11f to 22f, 22f to 15f).forEachIndexed { i, (dx, h) ->
        drawRoundRect(Color(0xFFE6DCC0), Offset(x + dx, 234 - h), Size(9f, h), CornerRadius(1.5f))
        drawRect(Color(0xFF2A2020), Offset(x + dx + 4, 229 - h), Size(1.2f, 6f))
        flame(x + dx + 4.6f, 230 - h, t, i * 1.7f)
    }
}

private fun DrawScope.cauldron(x: Float, t: Float) {
    val pulse = .7f + .3f * sin(t * 2)
    drawOval(Brush.radialGradient(listOf(Color(0xFF9DF0B4).copy(alpha = .5f * pulse), Color.Transparent), Offset(x, 198f), 42f), Offset(x - 42, 184f), Size(84f, 28f))
    for (i in 0 until 3) {
        val ph = ((t + i * 1.2f) % 3.6f) / 3.6f
        val a = (if (ph < .3f) ph / .3f else (1 - ph) / .7f) * .35f
        val dx = -10f + i * 11f; val y = 192f - ph * 26f
        drawPath(Path().apply { moveTo(x + dx, y); quadraticTo(x + dx - 6, y - 8, x + dx, y - 16); quadraticTo(x + dx + 6, y - 24, x + dx, y - 32) },
            Color(0xFFDFEEE4).copy(alpha = a), style = Stroke(3f, cap = StrokeCap.Round))
    }
    val body = Path().apply { moveTo(x - 30, 200f); quadraticTo(x - 32, 232f, x - 14, 236f); lineTo(x + 14, 236f); quadraticTo(x + 32, 232f, x + 30, 200f); close() }
    drawPath(body, Color(0xFF0B0F0C))
    drawLine(Color(0xFF0B0F0C), Offset(x - 22, 236f), Offset(x - 26, 240f), 3f); drawLine(Color(0xFF0B0F0C), Offset(x + 22, 236f), Offset(x + 26, 240f), 3f)
    drawOval(Color(0xFF1F2A22), Offset(x - 31, 194f), Size(62f, 12f)); drawOval(Color(0xFF3A4A3E), Offset(x - 31, 194f), Size(62f, 12f), style = Stroke(1.5f))
    drawOval(Color(0xFF7FD19A).copy(alpha = .75f + .1f * pulse), Offset(x - 26, 196f), Size(52f, 8f))
    for (i in 0 until 2) { val ph = ((t + i * 1.1f) % 2.4f) / 2.4f; drawCircle(Color(0xFFC8F5D4).copy(alpha = if (ph < .5f) ph * 2 else (1 - ph) * 2), 2f - i * .4f, Offset(x - 8 + i * 17, 199f - ph * 8)) }
}

private fun DrawScope.fireflyJar(x: Float, t: Float, night: Float) {
    val glow = .5f + .5f * night
    drawCircle(Brush.radialGradient(listOf(Color(0xFFF2B45A).copy(alpha = .4f * glow), Color.Transparent), Offset(x + 13, 214f), 26f), 26f, Offset(x + 13, 214f))
    listOf(Triple(8f, 216f, 0f), Triple(17f, 222f, 1.4f), Triple(14f, 209f, 2.6f)).forEach { (dx, y, d) ->
        val a = (.2f + .8f * (.5f + .5f * sin(t * 1.3f + d))) * glow
        val p = Offset(x + dx + 2 * sin(t + d), y - 2 * sin(t * .8f + d))
        drawCircle(Color(0xFFFFE39A).copy(alpha = a * .4f), 4f, p); drawCircle(Color(0xFFFFE39A).copy(alpha = a), 1.5f, p)
    }
    drawRoundRect(Color.White.copy(alpha = .07f), Offset(x, 200f), Size(26f, 34f), CornerRadius(6f))
    drawRoundRect(Color.White.copy(alpha = .27f), Offset(x, 200f), Size(26f, 34f), CornerRadius(6f), style = Stroke(1f))
    drawRoundRect(Color(0xFF7A5A34), Offset(x + 3, 194f), Size(20f, 7f), CornerRadius(1f))
}

private fun DrawScope.books(x: Float) {
    drawRoundRect(Color(0xFF3A2A1A), Offset(x, 226f), Size(44f, 8f), CornerRadius(1f))
    drawRoundRect(Color(0xFF24402F), Offset(x + 3, 219f), Size(38f, 7f), CornerRadius(1f))
    drawRoundRect(Color(0xFF5A2A1E), Offset(x + 2, 212f), Size(40f, 7f), CornerRadius(1f))
    drawRect(Color(0xFFD9B25E).copy(alpha = .7f), Offset(x + 6, 214.5f), Size(20f, 1.4f))
    drawPath(Path().apply { moveTo(x + 30, 212f); lineTo(x + 33, 200f); lineTo(x + 36, 212f); close() }, Color(0xFFCFE7DA).copy(alpha = .7f))
}

/** Fireflies drifting over the whole screen, and two slow bands of mist. Purely decoration; it never takes a touch. */
@Composable
fun Fireflies(modifier: Modifier = Modifier) {
    val t = clock()
    val flies = remember { val q = Rand(31); List(7) { floatArrayOf(q.next(), q.next(), 6 + q.next() * 6, q.next() * 7, q.next() * 60 - 30, -q.next() * 60) } }
    Canvas(modifier) {
        drawOval(Brush.radialGradient(listOf(Color(0xFFCFE6D8).copy(alpha = .05f), Color.Transparent)), Offset(-size.width * .3f + size.width * .18f * sin(t / 22f), size.height * .1f), Size(size.width * 1.6f, 140f))
        drawOval(Brush.radialGradient(listOf(Color(0xFFCFE6D8).copy(alpha = .05f), Color.Transparent)), Offset(-size.width * .3f - size.width * .18f * sin(t / 30f), size.height * .6f), Size(size.width * 1.6f, 140f))
        flies.forEach { f ->
            // Each one glows for a moment while it drifts, then goes fully dark for the rest of its cycle.
            val ph = ((t + f[3]) % (f[2] * 1.6f)) / f[2]
            if (ph >= 1f) return@forEach
            val a = sin(ph * PI.toFloat()).let { it * it }
            val p = Offset(f[0] * size.width + f[4] * density * ph, f[1] * size.height + f[5] * density * ph)
            drawCircle(Brush.radialGradient(listOf(Color(0xFFFFD27A).copy(alpha = a * .45f), Color.Transparent), p, 9f * density), 9f * density, p)
            drawCircle(Color(0xFFFFF1C2).copy(alpha = a), 1.8f * density, p)
        }
    }
}
