package app.kitchenwitch.ui.moon

import app.kitchenwitch.ui.components.Intro
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kitchenwitch.domain.Moon
import app.kitchenwitch.domain.Quarter
import app.kitchenwitch.domain.count
import app.kitchenwitch.domain.fmtDay
import app.kitchenwitch.domain.tr
import app.kitchenwitch.ui.Data
import app.kitchenwitch.ui.Nav
import app.kitchenwitch.ui.Sheet
import app.kitchenwitch.ui.art.MoonDot
import app.kitchenwitch.ui.art.moon
import app.kitchenwitch.ui.components.Eyebrow
import app.kitchenwitch.ui.components.Gap
import app.kitchenwitch.ui.components.Panel
import app.kitchenwitch.ui.theme.C
import app.kitchenwitch.ui.theme.Inter
import app.kitchenwitch.ui.theme.T
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MoonScreen(d: Data, nav: Nav) {
    val age = Moon.age(); val q = Moon.quarter(age); val cycle = Moon.cycle()
    val tm = rememberTextMeasurer()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(top = 18.dp, bottom = 24.dp)) {
        Eyebrow(Moon.moonName(cycle))
        Gap(6.dp)
        Text(Moon.name(age), style = T.title)
        Text(tr("${Moon.dayOf(age)}-й день из 30 · ${Moon.title(q).lowercase()}, ${Moon.verb(q)}", "Day ${Moon.dayOf(age)} of 30 · ${Moon.title(q).lowercase()}, ${Moon.verb(q)}"), style = T.sub)
        Intro("moon", d.s, Modifier.padding(top = 12.dp))
        Canvas(Modifier.fillMaxWidth().aspectRatio(1f).padding(8.dp)) {
            val c = center; val R = size.minDimension * .36f
            fun ang(day: Double) = (day / Moon.SYN * 2 * Math.PI - Math.PI / 2).toFloat()
            drawCircle(Brush.radialGradient(listOf(C.moonLit.copy(alpha = .1f), Color.Transparent), c, R * .9f), R * .9f, c)
            Moon.bounds.forEachIndexed { i, (qq, b) ->
                val on = qq == q; val col = C.quarters[i]
                val a0 = Math.toDegrees(ang(b.first).toDouble()).toFloat() + 2; val a1 = Math.toDegrees(ang(b.second).toDouble()).toFloat() - 2
                drawArc(col.copy(alpha = if (on) 1f else .45f), a0, a1 - a0, false, Offset(c.x - R, c.y - R), Size(2 * R, 2 * R), style = Stroke(if (on) 7.dp.toPx() else 3.dp.toPx(), cap = StrokeCap.Round))
                val m = ang((b.first + b.second) / 2); val lr = R + 26.dp.toPx()
                val label = tm.measure(Moon.ring(qq), androidx.compose.ui.text.TextStyle(fontFamily = Inter, fontSize = 12.sp, color = col))
                drawText(label, topLeft = Offset(c.x + cos(m) * lr - label.size.width / 2, c.y + sin(m) * lr - label.size.height / 2))
            }
            val a = ang(age); val p = Offset(c.x + cos(a) * R, c.y + sin(a) * R)
            drawCircle(C.bg, 8.dp.toPx(), p); drawCircle(C.goldL, 8.dp.toPx(), p, style = Stroke(2.5f.dp.toPx()))
            moon(c, R * .58f, Moon.phase(age))
        }
        Panel { Text(Moon.title(q), style = T.h3); Text(Moon.hint(q), style = T.sub) }
        Gap(12.dp)
        val start = Moon.cycleStart(cycle)
        val now = d.casts.filter { it.cycle == cycle }.sortedBy { it.at }
        Panel {
            Eyebrow(tr("Эта луна", "This moon"))
            Gap(4.dp)
            Line(fmtDay(Moon.epochDay(start)), tr("Новолуние. Началась ${Moon.moonName(cycle)}.", "New moon. The ${Moon.moonName(cycle)} began."))
            d.spellRecipe?.takeIf { d.s.spellCycle == cycle }?.let { Line("✦", tr("Загадано: ${it.title}", "Set: ${it.title}")) }
            now.forEach { cs -> Line(fmtDay(cs.day), when (cs.verdict) { "miss" -> tr("Не вышло: ${cs.title}", "Misfire: ${cs.title}"); else -> tr("Приготовлено: ${cs.title}", "Cast: ${cs.title}") }) }
        }
        Gap(12.dp)
        val past = (1..8).map { cycle - it }.filter { c -> d.casts.any { it.cycle == c } || c >= cycle - 3 }
        if (past.isNotEmpty()) Panel {
            Eyebrow(tr("Прошлые луны", "Past moons"))
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                past.forEach { cyc ->
                    val n = d.good.filter { it.cycle == cyc }.map { it.recipeId }.toSet().size
                    val kept = d.casts.any { it.cycle == cyc && it.spell }
                    Column(Modifier.width(84.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        MoonDot(.5, 46.dp)
                        Text(Moon.moonName(cyc), style = T.h3.copy(fontSize = 15.sp, lineHeight = 17.sp), textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
                        Text((if (kept) "✦ " else "") + count(n, "новое", "новых", "новых", "new", "new"), style = T.tiny, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun Line(left: String, text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(left, style = T.small, modifier = Modifier.widthIn(min = 70.dp).padding(end = 10.dp))
        Text(text, style = T.body)
    }
    HorizontalDivider(color = C.line, thickness = 1.dp)
}
