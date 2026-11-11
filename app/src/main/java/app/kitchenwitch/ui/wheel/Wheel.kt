package app.kitchenwitch.ui.wheel

import app.kitchenwitch.ui.components.Intro
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kitchenwitch.data.Dish
import app.kitchenwitch.data.Library
import app.kitchenwitch.domain.Wheel
import app.kitchenwitch.domain.plural
import app.kitchenwitch.domain.tr
import app.kitchenwitch.ui.Data
import app.kitchenwitch.ui.Nav
import app.kitchenwitch.ui.Sheet
import app.kitchenwitch.ui.components.Chip
import app.kitchenwitch.ui.components.ChipKind
import app.kitchenwitch.ui.components.Chips
import app.kitchenwitch.ui.components.Eyebrow
import app.kitchenwitch.ui.components.Gap
import app.kitchenwitch.ui.theme.C
import app.kitchenwitch.ui.theme.Inter
import app.kitchenwitch.ui.theme.T
import java.time.LocalDate
import java.time.format.TextStyle as JTextStyle
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WheelScreen(d: Data, nav: Nav) {
    val ctx = LocalContext.current
    val today = LocalDate.now()
    val list = Wheel.upcoming(today)
    val kept = d.good.map { it.sabbat }.filter { it.isNotEmpty() }.toSet()
    val libCooked = d.recipes.filter { it.id in d.cookedIds }.map { it.libId }.toSet()
    val next = list.first()
    val days = next.second.toEpochDay() - today.toEpochDay()
    val tm = rememberTextMeasurer()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(top = 18.dp, bottom = 24.dp)) {
        Eyebrow(tr("Колесо года", "Wheel of the year"))
        Gap(6.dp)
        Text(if (days == 0L) tr("Сегодня ${next.first.name}", "${next.first.name} is today")
            else tr("${next.first.name} через $days ${plural(days.toInt(), "день", "дня", "дней")}", "${next.first.name} in $days ${if (days == 1L) "day" else "days"}"), style = T.title)
        Text(tr("Праздник отмечен, если в его дни приготовлено хотя бы одно блюдо.", "A feast is kept when any dish is cast on its days."), style = T.sub)
        Intro("wheel", d.s, Modifier.padding(top = 12.dp))
        Canvas(Modifier.fillMaxWidth().aspectRatio(1f).padding(12.dp)) {
            val c = center; val R = size.minDimension * .36f
            val yule = Wheel.byId.getValue("yule").last(today).dayOfYear
            fun ang(doy: Int) = (((doy - yule + 365) % 365) / 365.0 * 2 * Math.PI - Math.PI / 2).toFloat()
            drawCircle(C.line, R, c, style = Stroke(1.5f.dp.toPx()))
            drawCircle(C.line, R * .89f, c, style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f))))
            Wheel.all.forEach { s ->
                val a = ang(s.day.atYear(today.year).dayOfYear); val p = Offset(c.x + cos(a) * R, c.y + sin(a) * R)
                drawLine(C.line, c, p, 1.dp.toPx())
                val done = s.id in kept
                drawCircle(if (done) C.gold else C.bg, 9.dp.toPx(), p); drawCircle(C.gold, 9.dp.toPx(), p, style = Stroke(1.5f.dp.toPx()))
                val l = tm.measure(s.name, TextStyle(fontFamily = Inter, fontSize = 12.sp, color = C.ink))
                val lr = R + 34.dp.toPx()
                drawText(l, topLeft = Offset(c.x + cos(a) * lr - l.size.width / 2, c.y + sin(a) * lr - l.size.height / 2))
            }
            val at = ang(today.dayOfYear)
            drawLine(C.goldL, c, Offset(c.x + cos(at) * R * .82f, c.y + sin(at) * R * .82f), 2.5f.dp.toPx(), StrokeCap.Round)
            drawCircle(C.goldL, 6.dp.toPx(), c)
        }
        list.forEachIndexed { i, (s, date) ->
            val isKept = s.id in kept
            val mod = if (i == 0) Modifier.clip(RoundedCornerShape(14.dp)).background(C.gold.copy(alpha = .07f)).border(1.dp, C.goldD, RoundedCornerShape(14.dp)).padding(10.dp)
            else Modifier.padding(vertical = 12.dp)
            Row(Modifier.fillMaxWidth().padding(top = if (i == 0) 0.dp else 2.dp).then(mod)) {
                Column(Modifier.width(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${date.dayOfMonth}", style = T.num.copy(fontSize = 24.sp))
                    Text(date.month.getDisplayName(JTextStyle.SHORT, if (tr("ru", "en") == "ru") Locale.forLanguageTag("ru") else Locale.UK), style = T.tiny)
                }
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(s.name + if (isKept) "  ✓" else "", style = T.h3)
                    Text(s.about, style = T.small)
                    Chips {
                        s.menu.mapNotNull { Library.byId(ctx, it) }.forEach { l ->
                            val done = l.id in libCooked
                            Chip((if (done) "✓ " else "") + l.title, if (done) ChipKind.NEW else ChipKind.PLAIN) { nav.open(Sheet.Scroll(Dish.of(l))) }
                        }
                    }
                }
            }
        }
    }
}
