package app.kitchenwitch.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kitchenwitch.container
import app.kitchenwitch.data.Settings
import app.kitchenwitch.domain.tr
import app.kitchenwitch.ui.theme.C
import app.kitchenwitch.ui.theme.T
import kotlinx.coroutines.launch

/** What each screen says the first time it is opened. */
object Intros {
    class Hint(val icon: String, val title: String, val lines: List<String>)

    fun of(id: String): Hint = when (id) {
        "kitchen" -> Hint("🕯", tr("Твоя кухня", "Your kitchen"), listOf(
            tr("За окном настоящая луна и погода твоего города.", "Outside the window are the real moon and your town's weather."),
            tr("Золотая карточка – заклинание этой луны: одно новое блюдо, загаданное в новолуние.", "The gold card is this moon's spell: one new dish, set at the new moon."),
            tr("В кладовую записывай то, что залежалось, – кости найдут ему применение.", "Put what's been waiting into the pantry – the bones will find a use for it.")))
        "moon" -> Hint("🌒", tr("Луна", "The moon"), listOf(
            tr("Луна делит месяц на четыре настроения: начинать, пробовать, шабаш, чистка.", "The moon splits the month into four moods: begin, try, sabbat, clear."),
            tr("В новолуние загадывают новое блюдо, в полнолуние зовут гостей, на убывающей луне доедают остатки.", "New moon: set a new dish. Full moon: call guests. Waning moon: use up leftovers."),
            tr("Ниже – что приготовлено в каждую луну.", "Below you see what was cast in each moon.")))
        "book" -> Hint("📖", tr("Книга теней", "The book of shadows"), listOf(
            tr("Заклинания – всё, что уже приготовлено хоть раз: с фото, пометкой и сигилом.", "Spells: everything cooked at least once, with photo, note and sigil."),
            tr("Свитки – записанные рецепты, которые ещё ждут. Их сигил остаётся бледным, пока блюдо не приготовлено.", "Scrolls: written recipes still waiting. Their sigil stays pale until the dish is cast."),
            tr("Неудачи тоже записываются, а за удачную вторую попытку дают знак.", "Misfires are written down too: make one good and you earn a mark."),
            tr("«+» – записать рецепт: из библиотеки, по ссылке, со страницы или своими словами.", "«+» writes down a recipe: from the library, a link, a page or your own words.")))
        "shelf" -> Hint("🫙", tr("Полка", "The shelf"), listOf(
            tr("Когда специя впервые попадает в блюдо, на полке появляется её банка.", "Every spice that first appears in a spell lands here as a jar."),
            tr("Нажми на банку – прочтёшь старое поверье и увидишь, где она встречается.", "Tap a jar to read its old belief and see where it's used."),
            tr("Пустые места подсказывают, что ещё можно попробовать.", "The empty places hint at what else to try.")))
        "wheel" -> Hint("☸", tr("Колесо года", "The wheel of the year"), listOf(
            tr("Восемь ведьминых праздников: равноденствия, солнцевороты и четыре праздника между ними.", "Eight witch's feasts: equinoxes, solstices and four feasts between them."),
            tr("У каждого своё меню. Праздник отмечен, если в его дни приготовлено хотя бы одно блюдо.", "Each has its menu. A feast is kept when any dish is cast on its days.")))
        "cook" -> Hint("🔥", tr("Колдовство", "Casting"), listOf(
            tr("Шаги идут по одному. Если в шаге указано время, появится свеча-таймер: зажги её, а когда время выйдет, зазвенит колокольчик.", "Steps come one at a time. If a step has a time, a candle appears – light it and it burns out on time."),
            tr("Экран не гаснет, пока готовишь.", "The screen stays on while you cook."),
            tr("В конце отметь, как вышло, оставь пометку и сфотографируй тарелку – тогда загорится сигил.", "At the end: how it went, a note and a photo of the plate. Then the sigil lights up.")))
        "dice" -> Hint("🎲", tr("Кости", "The bones"), listOf(
            tr("Кости выбирают блюдо, которого ещё нет в книге, и сначала предлагают сезонное.", "The bones pick a dish not yet in your book – in-season first."),
            tr("«Из того, что есть» – только блюда из остатков в кладовой.", "«From what I have» only picks dishes that use your pantry leftovers."),
            tr("Три броска в день: ведьма не перебирает.", "Three throws a day: a witch doesn't dither.")))
        else -> Hint("✒️", tr("Запись рецепта", "Writing a recipe"), listOf(
            tr("Пиши каждый ингредиент и каждый шаг с новой строки.", "Write reagents and steps one per line."),
            tr("Специи сами найдут свои банки, а время в шагах превратится в свечи-таймеры.", "Spices find their jars by themselves, and times in steps become candles."),
            tr("Записанный рецепт – это свиток. Заклинанием он станет, когда ты его приготовишь.", "A written recipe is a scroll. It becomes a spell once the dish is cooked.")))
    }
}

/** A first-visit hint for a screen: read once, «Got it», and it's gone (Settings brings the hints back). */
@Composable
fun Intro(id: String, s: Settings, modifier: Modifier = Modifier) {
    val c = LocalContext.current.container; val scope = rememberCoroutineScope()
    var gone by remember(id) { mutableStateOf(false) }
    AnimatedVisibility(id !in s.seen && !gone, modifier, exit = fadeOut() + shrinkVertically()) {
        val h = Intros.of(id)
        Panel(glow = true, modifier = Modifier.padding(bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(h.icon, fontSize = 22.sp, textAlign = TextAlign.Center, modifier = Modifier.width(34.dp))
                Column(Modifier.padding(start = 8.dp)) { Eyebrow(tr("Впервые здесь", "First time here"), color = C.gold); Text(h.title, style = T.h3) }
            }
            h.lines.forEach { l -> Row(Modifier.padding(top = 8.dp)) { Text("✦", style = T.small.copy(color = C.gold), modifier = Modifier.width(18.dp)); Text(l, style = T.sub) } }
            Gap(12.dp)
            GhostButton(tr("Понятно", "Got it")) { gone = true; scope.launch { c.settings.update { it.copy(seen = it.seen + id) } } }
        }
    }
}
