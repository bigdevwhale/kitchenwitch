package app.kitchenwitch.ui.home

import app.kitchenwitch.ui.components.Intro
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.kitchenwitch.data.Dish
import app.kitchenwitch.data.Outside
import app.kitchenwitch.domain.Moon
import app.kitchenwitch.domain.count
import app.kitchenwitch.domain.fmtDay
import app.kitchenwitch.domain.minutes
import app.kitchenwitch.domain.tr
import app.kitchenwitch.domain.weekday
import app.kitchenwitch.ui.Data
import app.kitchenwitch.ui.Nav
import app.kitchenwitch.ui.Sheet
import app.kitchenwitch.ui.art.MoonDot
import app.kitchenwitch.ui.art.Sigil
import app.kitchenwitch.ui.art.WindowScene
import app.kitchenwitch.ui.art.daylight
import app.kitchenwitch.ui.components.Chip
import app.kitchenwitch.ui.components.ChipKind
import app.kitchenwitch.ui.components.Chips
import app.kitchenwitch.ui.components.Eyebrow
import app.kitchenwitch.ui.components.Gap
import app.kitchenwitch.ui.components.GhostButton
import app.kitchenwitch.ui.components.GoldButton
import app.kitchenwitch.ui.components.HGap
import app.kitchenwitch.ui.components.Panel
import app.kitchenwitch.ui.theme.C
import app.kitchenwitch.ui.theme.T

fun weatherWord(w: String) = when (w) {
    "CLEAR" -> tr("ясно", "clear"); "CLOUDY" -> tr("облачно", "cloudy"); "OVERCAST" -> tr("пасмурно", "overcast"); "RAIN" -> tr("дождь", "rain")
    "SNOW" -> tr("снег", "snow"); "FOG" -> tr("туман", "fog"); "STORM" -> tr("гроза", "thunderstorm"); else -> ""
}
fun weatherLine(w: String, temp: Double): String = listOfNotNull(if (temp.isNaN()) null else "${if (temp > 0) "+" else ""}${Math.round(temp)}°", weatherWord(w).ifEmpty { null }).joinToString(", ")

@Composable
fun HomeScreen(d: Data, nav: Nav) {
    val now = System.currentTimeMillis(); val age = Moon.age(now); val q = Moon.quarter(age)
    val out = Outside.of(d.s.outside)
    val today = Moon.today()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(top = 18.dp, bottom = 24.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Eyebrow("${fmtDay(today)} · ${weekday(today)}")
                Gap(6.dp)
                Text(Moon.name(age), style = T.title)
                Text(buildAnnotatedString {
                    append(tr("${Moon.dayOf(age)}-й день луны · ", "Day ${Moon.dayOf(age)} of the moon · "))
                    withStyle(SpanStyle(color = C.gold)) { append(Moon.verb(q)) }
                    out?.let { val w = weatherLine(it.weather.name, it.temp); if (w.isNotEmpty()) append(" · $w") }
                }, style = T.sub)
            }
            Canvas(Modifier.size(40.dp).clip(CircleShape).clickable { nav.open(Sheet.Settings) }.padding(10.dp)) {
                val k = size.width / 20f
                drawCircle(C.muted, 6.5f * k, center, style = Stroke(1.5f * k)); drawCircle(C.muted, 2.2f * k, center, style = Stroke(1.5f * k))
                for (i in 0 until 8) { val a = i * Math.PI / 4; drawLine(C.muted, Offset(center.x + (Math.cos(a) * 6.5 * k).toFloat(), center.y + (Math.sin(a) * 6.5 * k).toFloat()),
                    Offset(center.x + (Math.cos(a) * 9.3 * k).toFloat(), center.y + (Math.sin(a) * 9.3 * k).toFloat()), 2f * k) }
            }
        }
        Intro("kitchen", d.s, Modifier.padding(top = 12.dp))
        Box(Modifier.padding(horizontal = 0.dp)) {
            WindowScene(Moon.phase(age), out?.weather?.name.orEmpty(), daylight(now, out?.sunrise ?: 0, out?.sunset ?: 0), Modifier.padding(top = 10.dp))
        }
        SpellCard(d, nav)
        Gap(12.dp)
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MoonDot(Moon.phase(age), 40.dp)
                HGap(12.dp)
                Column { Text(Moon.title(q), style = T.h3); Text(Moon.hint(q), style = T.sub) }
            }
            Chips {
                d.leftovers.forEach { Chip(it.name, ChipKind.LEFT) { nav.open(Sheet.Pantry) } }
                Chip(tr("+ в кладовую", "+ to the pantry"), ChipKind.DASHED) { nav.open(Sheet.Pantry) }
            }
        }
        Gap(12.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat(Modifier.weight(1f), d.cookedIds.size, countWord(d.cookedIds.size, "заклинание", "заклинания", "заклинаний", "spell", "spells")) { nav.tab = 2 }
            Stat(Modifier.weight(1f), d.s.jars.size, countWord(d.s.jars.size, "банка", "банки", "банок", "jar", "jars")) { nav.tab = 3 }
            Stat(Modifier.weight(1f), d.s.marks.size, countWord(d.s.marks.size, "знак", "знака", "знаков", "mark", "marks")) { nav.open(Sheet.Marks) }
        }
        Gap(12.dp)
        GhostButton(tr("Записать рецепт", "Write down a recipe")) { nav.open(Sheet.Write) }
    }
}

private fun countWord(n: Int, r1: String, r2: String, r5: String, e1: String, en: String) = count(n, r1, r2, r5, e1, en).substringAfter(' ')

@Composable
private fun Stat(modifier: Modifier, n: Int, label: String, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$n", style = T.num); Text(label, style = T.tiny, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SpellCard(d: Data, nav: Nav) {
    val cycle = Moon.cycle()
    val spell = d.spellRecipe?.takeIf { d.s.spellCycle == cycle }
    val kept = d.casts.any { it.cycle == cycle && it.spell }
    Gap(14.dp)
    Panel(glow = true) {
        Eyebrow(tr("Заклинание этой луны", "This moon's spell"))
        when {
            kept -> {
                val cast = d.casts.first { it.cycle == cycle && it.spell }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text(cast.title, style = T.h2, modifier = Modifier.padding(top = 4.dp)); Text(tr("Обещание сдержано ✦ до новолуния можно готовить что угодно.", "Word kept ✦ until the new moon, try anything"), style = T.sub) }
                    Sigil(cast.reagents.split(','), 64.dp)
                }
                Gap(12.dp)
                GoldButton(tr("Бросить кости", "Throw the bones")) { nav.open(Sheet.Dice) }
            }
            spell != null -> {
                val dish = Dish.of(spell)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(spell.title, style = T.h2, modifier = Modifier.padding(top = 4.dp))
                        Text(listOfNotNull(spell.origin.ifBlank { null }, if (spell.minutes > 0) minutes(spell.minutes) else null).joinToString(" · "), style = T.sub)
                    }
                    Sigil(dish.reagents, 64.dp)
                }
                Gap(12.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GhostButton(tr("Свиток", "Scroll"), Modifier.weight(1f)) { nav.open(Sheet.Scroll(dish)) }
                    GoldButton(tr("Колдовать", "Cast"), Modifier.weight(1f)) { nav.cook = dish }
                }
            }
            else -> {
                Text(tr("Ещё не загадано", "Not set yet"), style = T.h3, modifier = Modifier.padding(top = 4.dp))
                Text(tr("Загадай блюдо, которого ещё нет в книге.", "One new dish a moon keeps the wheel turning."), style = T.sub)
                Gap(12.dp)
                GoldButton(tr("Бросить кости", "Throw the bones")) { nav.open(Sheet.Dice) }
            }
        }
    }
}
