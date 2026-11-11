package app.kitchenwitch.ui.book

import app.kitchenwitch.ui.components.Intro
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kitchenwitch.data.Dish
import app.kitchenwitch.domain.Intents
import app.kitchenwitch.domain.Moon
import app.kitchenwitch.domain.Reading
import app.kitchenwitch.domain.Wheel
import app.kitchenwitch.domain.count
import app.kitchenwitch.domain.fmtDayYear
import app.kitchenwitch.domain.minutes
import app.kitchenwitch.domain.tr
import app.kitchenwitch.ui.Data
import app.kitchenwitch.ui.Nav
import app.kitchenwitch.ui.PlateImage
import app.kitchenwitch.ui.Sheet
import app.kitchenwitch.ui.art.MoonDot
import app.kitchenwitch.ui.art.Sigil
import app.kitchenwitch.ui.components.Eyebrow
import app.kitchenwitch.ui.components.Gap
import app.kitchenwitch.ui.components.HGap
import app.kitchenwitch.ui.components.Seg
import app.kitchenwitch.ui.theme.C
import app.kitchenwitch.ui.theme.T

private val R16 = RoundedCornerShape(16.dp)

@Composable
private fun Row16(onClick: () -> Unit, content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 10.dp).clip(R16).background(C.card).border(1.dp, C.line, R16).clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically, content = content)
}

@Composable
fun BookScreen(d: Data, nav: Nav) {
    val spells = d.good.groupBy { it.recipeId }.map { (_, cs) -> cs.maxBy { it.at } to cs.size }.sortedByDescending { it.first.at }
    val scrolls = d.recipes.filter { it.scroll && it.id !in d.cookedIds }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 18.dp, bottom = 24.dp)) {
        item {
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Eyebrow(tr("Книга теней", "Book of shadows"))
                    Gap(6.dp)
                    Text(count(spells.size, "заклинание", "заклинания", "заклинаний", "spell", "spells"), style = T.title)
                }
                Box(Modifier.size(46.dp).clip(CircleShape).background(C.gold).clickable { nav.open(Sheet.Write) }, contentAlignment = Alignment.Center) {
                    Text("+", style = T.h2.copy(color = C.onGold, fontSize = 30.sp))
                }
            }
            Gap(10.dp)
            Intro("book", d.s)
            Seg(listOf(tr("Заклинания", "Spells"), tr("Свитки · ${scrolls.size}", "Scrolls · ${scrolls.size}"), tr("Осечки · ${d.misses.size}", "Misfires · ${d.misses.size}")), nav.bookTab) { nav.bookTab = it }
        }
        when (nav.bookTab) {
            0 -> {
                if (spells.isEmpty()) item { Empty(tr("Книга пока пуста. Первое приготовленное блюдо появится здесь с фотографией и сигилом.", "The book is empty. The first dish you cast lands here with its photo and sigil.")) }
                items(spells, key = { it.first.id }) { (c, n) ->
                    Row16({ nav.open(Sheet.Entry(c.recipeId)) }) {
                        PlateImage(c.photo, c.title, c.reagents.split(','), Modifier.size(62.dp).clip(RoundedCornerShape(10.dp)), 200)
                        HGap(12.dp)
                        Column(Modifier.weight(1f)) {
                            Text(c.title, style = T.h3, maxLines = 2)
                            Text(listOfNotNull(fmtDayYear(c.day), Intents.name(c.intent).ifEmpty { null }, Wheel.byId[c.sabbat]?.name, if (n > 1) tr("×$n", "×$n") else null).joinToString(" · "), style = T.small)
                        }
                        MoonDot(Moon.phase(c.age), 20.dp, Modifier.padding(horizontal = 8.dp))
                        Sigil(c.reagents.split(','), 40.dp, glow = false)
                    }
                }
            }
            1 -> {
                item { Text(tr("Рецепты, которые записаны, но ещё не приготовлены. Сигил загорится золотом, когда приготовишь блюдо.", "Written down, not yet cast. The sigil turns gold once you cook it."), style = T.sub, modifier = Modifier.padding(top = 14.dp)) }
                if (scrolls.isEmpty()) item { Empty(tr("Свитков нет. Нажми «+», чтобы записать рецепт – из библиотеки, по ссылке, со страницы или своими словами.", "No scrolls. Tap «+» to write one down – from the library, a link, a page or in your own words.")) }
                items(scrolls, key = { it.id }) { r ->
                    val dish = Dish.of(r)
                    Row16({ nav.open(Sheet.Scroll(dish)) }) {
                        Column(Modifier.weight(1f)) {
                            Text(r.title, style = T.h3, maxLines = 2)
                            Text(listOfNotNull(r.origin.ifBlank { null }, if (r.minutes > 0) minutes(r.minutes) else null,
                                count(Reading.lines(r.ingredients).size, "ингредиент", "ингредиента", "ингредиентов", "reagent", "reagents")).joinToString(" · "), style = T.small)
                        }
                        Sigil(dish.reagents, 40.dp, color = C.dim, glow = false)
                    }
                }
            }
            else -> {
                item { Text(tr("Котёл иногда взрывается. Если со второго раза выйдет, получишь ведьмин знак.", "Cauldrons sometimes go bang. A misfire made good earns a witch's mark."), style = T.sub, modifier = Modifier.padding(top = 14.dp)) }
                if (d.misses.isEmpty()) item { Empty(tr("Осечек нет. Пока.", "No misfires. Yet.")) }
                items(d.misses, key = { it.id }) { c ->
                    Row16({ nav.open(Sheet.Entry(c.recipeId)) }) {
                        Box(Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(C.card2), contentAlignment = Alignment.Center) { Text("💨", fontSize = 24.sp) }
                        HGap(12.dp)
                        Column(Modifier.weight(1f)) {
                            Text(c.title, style = T.h3)
                            if (c.note.isNotBlank()) Text(c.note, style = T.hand.copy(color = C.muted, fontSize = 20.sp))
                            Text(fmtDayYear(c.day), style = T.tiny)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Empty(text: String) = Text(text, style = T.sub, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 20.dp))
