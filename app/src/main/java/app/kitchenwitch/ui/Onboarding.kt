package app.kitchenwitch.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kitchenwitch.container
import app.kitchenwitch.domain.Jars
import app.kitchenwitch.domain.Moon
import app.kitchenwitch.domain.tr
import app.kitchenwitch.ui.art.Fireflies
import app.kitchenwitch.ui.art.WindowScene
import app.kitchenwitch.ui.art.daylight
import app.kitchenwitch.ui.art.jar
import app.kitchenwitch.ui.components.Eyebrow
import app.kitchenwitch.ui.components.Gap
import app.kitchenwitch.ui.components.GoldButton
import app.kitchenwitch.ui.theme.C
import app.kitchenwitch.ui.theme.T
import kotlinx.coroutines.launch

/** Two pages: who you are here, and what already stands on your shelf. */
@Composable
fun Onboarding() {
    val c = LocalContext.current.container; val scope = rememberCoroutineScope()
    var page by remember { mutableIntStateOf(0) }
    val basics = listOf("salt", "pepper", "bay", "garlic", "cinnamon", "paprika", "dill", "honey", "ginger", "turmeric", "oregano", "basil",
        "rosemary", "thyme", "nutmeg", "clove", "vanilla", "mustard", "vinegar", "soy", "cocoa", "chili", "mint", "caraway")
    var chosen by remember { mutableStateOf(setOf("salt", "pepper", "bay", "garlic")) }
    Box(Modifier.fillMaxSize().background(C.bg).background(Brush.radialGradient(listOf(C.bgHi, Color.Transparent), Offset(540f, 0f), 1100f))) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp).padding(top = 20.dp, bottom = 16.dp)) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                if (page == 0) {
                    WindowScene(Moon.phase(Moon.age()), "", daylight(System.currentTimeMillis(), 0, 0))
                    Gap(18.dp)
                    Text(tr("Ты уже ведьма", "You're a witch already"), style = T.title)
                    Gap(10.dp)
                    Text(tr("Колдовство здесь – это готовка. Учителей и заданий нет: ритм задают луна и времена года.",
                        "Here, witchcraft is cooking. No teachers, no homework: the moon and the year keep the rhythm."), style = T.body)
                    Gap(14.dp)
                    listOf(
                        "🌑" to tr("В новолуние загадай заклинание – блюдо, которого ещё нет в книге.", "At the new moon, set a spell – a dish not yet in your book."),
                        "📜" to tr("Каждое приготовленное блюдо попадает в книгу теней – с фото, пометкой и сигилом из его ингредиентов.", "Every dish you cast goes into the book of shadows, with a photo, a note and a sigil drawn from its reagents."),
                        "🫙" to tr("Новая специя – новая банка на полке. Пустые места манят.", "A new spice is a new jar on the shelf. The empty places beckon."),
                        "🎲" to tr("Не знаешь, что приготовить, – брось кости. Три раза в день, не больше.", "Not sure what to cook? Throw the bones. Three times a day, no more."),
                    ).forEach { (i, t) -> Row(Modifier.padding(vertical = 6.dp)) { Text(i, fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp)); Text(t, style = T.sub) } }
                } else {
                    Eyebrow(tr("Полка", "The shelf"))
                    Gap(6.dp)
                    Text(tr("Что уже стоит у тебя на полке?", "What's on your shelf already?"), style = T.title)
                    Text(tr("Отметь банки, которые есть дома. Остальные появятся сами, когда понадобятся в рецептах.", "Mark the jars you have at home. The rest will appear when your spells call for them."), style = T.sub)
                    Gap(12.dp)
                    basics.chunked(4).forEach { row ->
                        Row(Modifier.fillMaxWidth()) {
                            row.forEach { id -> val j = Jars.byId.getValue(id); val on = id in chosen
                                Column(Modifier.weight(1f).padding(3.dp).clip(RoundedCornerShape(12.dp)).background(if (on) C.gold.copy(alpha = .1f) else Color.Transparent)
                                    .border(1.dp, if (on) C.gold else C.line, RoundedCornerShape(12.dp)).clickable { chosen = if (on) chosen - id else chosen + id }.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally) {
                                    Canvas(Modifier.size(40.dp, 52.dp)) { jar(j, null, !on) }
                                    Text(j.name, style = T.tiny.copy(color = if (on) C.ink else C.muted), maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
            Gap(12.dp)
            GoldButton(if (page == 0) tr("Войти на кухню", "Step into the kitchen") else tr("Готово", "Done")) {
                if (page == 0) page = 1 else scope.launch { c.settings.update { it.copy(onboarded = true, jars = chosen) }; c.refreshOutside() }
            }
        }
        Fireflies(Modifier.fillMaxSize())
    }
}
