package app.kitchenwitch.ui.sheets

import app.kitchenwitch.ui.components.Intro
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import app.kitchenwitch.container
import app.kitchenwitch.currentLanguage
import app.kitchenwitch.data.Dish
import app.kitchenwitch.data.LinkImport
import app.kitchenwitch.data.OutsideSource
import app.kitchenwitch.data.Town
import app.kitchenwitch.domain.Crafts
import app.kitchenwitch.domain.Intents
import app.kitchenwitch.domain.Jars
import app.kitchenwitch.domain.Lang
import app.kitchenwitch.domain.Marks
import app.kitchenwitch.domain.Moon
import app.kitchenwitch.domain.Reading
import app.kitchenwitch.domain.Wheel
import app.kitchenwitch.domain.count
import app.kitchenwitch.domain.fmtDay
import app.kitchenwitch.domain.fmtDayYear
import app.kitchenwitch.domain.minutes
import app.kitchenwitch.domain.tr
import app.kitchenwitch.newRecipe
import app.kitchenwitch.setLanguage
import app.kitchenwitch.ui.Data
import app.kitchenwitch.ui.Draft
import app.kitchenwitch.ui.Nav
import app.kitchenwitch.ui.Photos
import app.kitchenwitch.ui.PlateImage
import app.kitchenwitch.ui.Sheet
import app.kitchenwitch.ui.art.MoonDot
import app.kitchenwitch.ui.art.Sigil
import app.kitchenwitch.ui.art.die
import app.kitchenwitch.ui.art.jar
import app.kitchenwitch.ui.art.markGlyph
import app.kitchenwitch.ui.components.Chip
import app.kitchenwitch.ui.components.ChipKind
import app.kitchenwitch.ui.components.Chips
import app.kitchenwitch.ui.components.Eyebrow
import app.kitchenwitch.ui.components.Field
import app.kitchenwitch.ui.components.Gap
import app.kitchenwitch.ui.components.GhostButton
import app.kitchenwitch.ui.components.GoldButton
import app.kitchenwitch.ui.components.HGap
import app.kitchenwitch.ui.components.Label
import app.kitchenwitch.ui.components.Panel
import app.kitchenwitch.ui.components.Seg
import app.kitchenwitch.ui.components.WitchSheet
import app.kitchenwitch.ui.home.weatherLine
import app.kitchenwitch.ui.theme.C
import app.kitchenwitch.ui.theme.T
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun Sheets(d: Data, nav: Nav) {
    val s = nav.sheet ?: return
    val close = { nav.sheet = null }
    WitchSheet(close) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            when (s) {
                Sheet.Dice -> DiceSheet(d, nav)
                Sheet.Write -> WriteSheet(nav)
                Sheet.Library -> LibrarySheet(d, nav)
                is Sheet.Link -> LinkSheet(s.url, nav)
                Sheet.Page -> PageSheet(nav)
                is Sheet.Editor -> EditorSheet(s.draft, d, nav)
                is Sheet.Scroll -> ScrollSheet(s.dish, d, nav)
                is Sheet.Entry -> EntrySheet(s.recipeId, d, nav)
                is Sheet.JarInfo -> JarSheet(s.id, d, nav)
                Sheet.Marks -> MarksSheet(d)
                Sheet.Pantry -> PantrySheet(d)
                Sheet.Settings -> SettingsSheet(d)
            }
        }
    }
}

/** The recipe card that the bones, the library and the scrolls all show. */
@Composable
fun DishCard(dish: Dish, d: Data, ink: Boolean = false) {
    val c = LocalContext.current.container
    val left = d.leftovers.map { it.name }
    Panel(glow = !ink) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                if (dish.from.isNotBlank()) Eyebrow(dish.from)
                Text(dish.title, style = T.h2, modifier = Modifier.padding(top = 3.dp))
                val crafts = Crafts.of(dish.steps).mapNotNull { Crafts.byId[it]?.name }
                Text(listOfNotNull(if (dish.minutes > 0) minutes(dish.minutes) else null, crafts.take(3).joinToString(", ").ifEmpty { null }).joinToString(" · "), style = T.small)
            }
            Sigil(dish.reagents, 62.dp, color = if (ink) C.dim else C.gold, glow = !ink)
        }
        Chips { dish.ingredients.forEach { ing ->
            val j = Jars.of(ing); val isNew = j != null && j.id !in d.s.jars; val isLeft = left.any { c.stemMatch(ing, it) }
            Chip((if (isNew) "✦ " else "") + ing, when { isNew -> ChipKind.NEW; isLeft -> ChipKind.LEFT; else -> ChipKind.PLAIN })
        } }
        val news = dish.ingredients.mapNotNull { Jars.of(it) }.filter { it.id !in d.s.jars }.distinctBy { it.id }
        if (news.isNotEmpty()) Text("✦ " + tr("на полке появится: ", "a new jar for the shelf: ") + news.joinToString(", ") { it.name.lowercase() }, style = T.small.copy(color = C.goldL), modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun DiceSheet(d: Data, nav: Nav) {
    val c = LocalContext.current.container; val scope = rememberCoroutineScope()
    var time by remember { mutableIntStateOf(2) }
    var home by remember { mutableIntStateOf(0) }
    var faces by remember { mutableStateOf(listOf(3, 5, 1)) }
    var rolling by remember { mutableStateOf(false) }
    var dish by remember { mutableStateOf<Dish?>(null) }
    var empty by remember { mutableStateOf(false) }
    var left by remember { mutableIntStateOf(3) }
    LaunchedEffect(Unit) { left = c.rollsLeft() }
    Intro("dice", d.s)
    Eyebrow(tr("Гадание", "Divination"))
    Text(tr("Бросить кости", "Throw the bones"), style = T.h2, modifier = Modifier.padding(top = 4.dp))
    Text(tr("Кости выберут блюдо, которого ещё нет в книге.", "The bones pick a dish that isn't in the book yet."), style = T.sub)
    Gap(12.dp)
    Seg(listOf(tr("до 30 мин", "≤ 30 min"), tr("до часа", "≤ 1 hour"), tr("не важно", "any time")), time) { time = it }
    Gap(8.dp)
    Seg(listOf(tr("Схожу за продуктами", "I'll go shopping"), tr("Из того, что есть", "From what I have")), home) { home = it }
    if (home == 1 && d.leftovers.isEmpty()) Text(tr("Кладовая пуста – добавь остатки на кухне, и кости найдут им применение.", "The pantry is empty – add leftovers in the kitchen and the bones will use them."), style = T.small, modifier = Modifier.padding(top = 6.dp))
    Row(Modifier.fillMaxWidth().padding(vertical = 22.dp), horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally)) {
        faces.forEachIndexed { i, f -> Canvas(Modifier.size(58.dp).rotate(if (rolling) ((i * 37 + f * 13) % 30 - 15).toFloat() else 0f)) { die(f) } }
    }
    dish?.let { DishCard(it, d); Gap(4.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GhostButton(tr("Загадать на луну", "Set for the moon"), Modifier.weight(1f)) { scope.launch { c.setSpell(it); nav.sheet = null; c.news.tryEmit(tr("Заклинание загадано на эту луну", "Set as this moon's spell")) } }
            GoldButton(tr("Колдовать", "Cast"), Modifier.weight(1f)) { nav.sheet = null; nav.cook = it }
        }
        GhostButton(tr("Отложить в свитки", "Save to the scrolls"), Modifier.padding(top = 10.dp)) { scope.launch { c.toScroll(it); c.news.tryEmit(tr("Добавлено в свитки", "Copied to the scrolls")) } }
    }
    if (empty) Panel { Text(tr("Кости молчат: с такими условиями нового блюда не нашлось. Попробуй их смягчить.", "The bones are silent: nothing new fits. Loosen the conditions."), style = T.sub) }
    Gap(12.dp)
    GoldButton(if (left > 0) tr("Бросить · осталось $left", "Throw · $left left") else tr("На сегодня броски закончились", "The bones rest until tomorrow"), enabled = left > 0 && !rolling) {
        scope.launch {
            if (!c.spendRoll()) { left = 0; return@launch }
            left = c.rollsLeft(); rolling = true; dish = null; empty = false
            repeat(9) { faces = List(3) { (1..6).random() }; delay(90) }
            rolling = false
            val pool = c.pool(app.kitchenwitch.Throw(maxMinutes = listOf(30, 60, 0)[time], fromHome = home == 1))
            val pick = (pool.take(maxOf(12, pool.size / 2)).filter { it.key != dish?.key }).randomOrNull()
            if (pick == null) empty = true else dish = pick
        }
    }
    Text(tr("Три броска в день – ведьма не перебирает.", "A witch doesn't dither: three throws a day."), style = T.tiny, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
}

@Composable
private fun WriteSheet(nav: Nav) {
    Eyebrow(tr("Новое заклинание", "A new spell"))
    Text(tr("Откуда рецепт?", "Where's the recipe from?"), style = T.h2, modifier = Modifier.padding(top = 4.dp))
    listOf(
        Triple("📜", tr("Ведьмина библиотека", "The witch's library") to tr("проверенные рецепты на любой сезон и праздник", "tried recipes, by season and feast"), { nav.sheet = Sheet.Library }),
        Triple("🔗", tr("По ссылке", "From a link") to tr("с любого сайта с рецептами", "from any recipe site"), { nav.sheet = Sheet.Link() }),
        Triple("📷", tr("Со страницы", "From a page") to tr("книга, бабушкина тетрадь, открытка", "a book, grandmother's notebook, a card"), { nav.sheet = Sheet.Page }),
        Triple("✒️", tr("Своими словами", "In your own words") to tr("написать вручную", "type it in"), { nav.sheet = Sheet.Editor(Draft()) }),
        Triple("🍲", tr("Уже приготовлено", "Already cooked") to tr("записать по памяти прямо в книгу", "write it from memory, straight into the book"), { nav.sheet = Sheet.Editor(Draft(memory = true, source = "memory")) }),
    ).forEach { (icon, texts, go) ->
        Gap(10.dp)
        Panel(onClick = go) { Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 24.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center); HGap(10.dp)
            Column { Text(texts.first, style = T.h3); Text(texts.second, style = T.small) }
        } }
    }
}

@Composable
private fun LibrarySheet(d: Data, nav: Nav) {
    val ctx = LocalContext.current
    var q by remember { mutableStateOf("") }
    val month = java.time.LocalDate.now().monthValue
    val all = remember { ctx.container.library() }
    val cookedLib = d.recipes.filter { it.id in d.cookedIds }.map { it.libId }.toSet()
    val list = all.filter { q.isBlank() || it.haystack.contains(q.lowercase().replace('ё', 'е').trim()) }
        .sortedWith(compareByDescending<app.kitchenwitch.data.LibRecipe> { month in it.months }.thenBy { it.title })
    Eyebrow(tr("Ведьмина библиотека", "The witch's library"))
    Text(count(all.size, "рецепт", "рецепта", "рецептов", "recipe", "recipes"), style = T.h2, modifier = Modifier.padding(top = 4.dp))
    Gap(8.dp)
    Field(q, { q = it }, hint = tr("название, продукт, кухня…", "name, ingredient, cuisine…"))
    list.forEach { l ->
        val done = l.id in cookedLib
        Row(Modifier.fillMaxWidth().padding(top = 10.dp).clip(RoundedCornerShape(14.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(14.dp))
            .clickable { nav.sheet = Sheet.Scroll(Dish.of(l)) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(l.title, style = T.h3)
                Text(listOfNotNull(l.from, minutes(l.minutes), if (month in l.months) tr("сезон", "in season") else null, if (done) tr("уже в книге", "in the book") else null).joinToString(" · "), style = T.small)
            }
            Sigil(Dish.of(l).reagents, 36.dp, color = if (done) C.gold else C.dim, glow = false)
        }
    }
}

@Composable
private fun LinkSheet(initial: String, nav: Nav) {
    val scope = rememberCoroutineScope()
    var url by remember { mutableStateOf(LinkImport.urlIn(initial) ?: initial) }
    var state by remember { mutableStateOf("") }
    Eyebrow(tr("По ссылке", "From a link"))
    Text(tr("Вставь ссылку", "Paste a link"), style = T.h2, modifier = Modifier.padding(top = 4.dp))
    Text(tr("С большинства сайтов название, ингредиенты и шаги подтянутся сами. А можно просто нажать «Поделиться» в браузере и выбрать Кухонную ведьму.",
        "Most recipe sites publish markup, so the name, ingredients and steps read themselves. You can also share a page from the browser straight here."), style = T.sub)
    Gap(10.dp)
    Field(url, { url = it }, hint = "https://…")
    if (state.isNotEmpty()) Text(state, style = T.small.copy(color = C.goldL), modifier = Modifier.padding(top = 8.dp))
    Gap(12.dp)
    GoldButton(tr("Прочитать", "Read it"), enabled = url.startsWith("http")) {
        state = tr("Читаю страницу…", "Reading the page…")
        scope.launch {
            val f = withContext(Dispatchers.IO) { runCatching { LinkImport.read(url.trim()) } }.getOrNull()
            if (f == null) { state = tr("Страница не открылась. Проверь ссылку или запиши рецепт вручную.", "The page didn't open. Check the link or write the recipe yourself."); return@launch }
            nav.sheet = Sheet.Editor(Draft(f.title, f.origin, if (f.minutes > 0) "${f.minutes}" else "", f.ingredients.joinToString("\n"), f.steps.joinToString("\n"), "link", f.url,
                note = if (f.complete) tr("Рецепт подтянулся по ссылке. Проверь, всё ли на месте.", "Read from the link. Check that everything is there.")
                else tr("Подтянулось не всё – допиши, чего не хватает.", "The page has no full recipe markup – fill in what's missing.")))
        }
    }
}

@Composable
private fun PageSheet(nav: Nav) {
    val ctx = LocalContext.current
    var pending by remember { mutableStateOf<java.io.File?>(null) }
    val done = { f: java.io.File -> nav.sheet = Sheet.Editor(Draft(source = "page", page = f.name,
        note = tr("Снимок вклеен в свиток и будет перед глазами, пока готовишь. Перепиши хотя бы название и ингредиенты – по ним кости и полка узнают блюдо.",
            "The page is pasted into the scroll and stays in view while you cook. Copy at least the name and reagents: that's how the bones and the shelf know the dish."))) }
    val shoot = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok -> pending?.let { f -> if (ok && f.length() > 0) done(f) else f.delete() } }
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val f = Photos.newFile(ctx, "page"); runCatching { ctx.contentResolver.openInputStream(uri)?.use { i -> f.outputStream().use { i.copyTo(it) } }; done(f) } }
    Eyebrow(tr("Со страницы", "From a page"))
    Text(tr("Сфотографировать страницу", "Photograph the page"), style = T.h2, modifier = Modifier.padding(top = 4.dp))
    Text(tr("Книга, бабушкина тетрадь, открытка с рецептом. Снимок останется в свитке целиком, с почерком и пятнами.",
        "A book, grandmother's notebook, a recipe card. The photo stays in the scroll whole, handwriting, stains and all."), style = T.sub)
    Gap(14.dp)
    GoldButton(tr("Сфотографировать", "Take a photo")) {
        val f = Photos.newFile(ctx, "page"); pending = f
        runCatching { shoot.launch(FileProvider.getUriForFile(ctx, ctx.packageName + ".files", f)) }
    }
    Gap(10.dp)
    GhostButton(tr("Выбрать из галереи", "Pick from the gallery")) { pick.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
}

@Composable
private fun EditorSheet(draft: Draft, d: Data, nav: Nav) {
    val c = LocalContext.current.container; val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf(draft.title) }
    var origin by remember { mutableStateOf(draft.origin) }
    var mins by remember { mutableStateOf(draft.minutes) }
    var ings by remember { mutableStateOf(draft.ingredients) }
    var steps by remember { mutableStateOf(draft.steps) }
    var warn by remember { mutableStateOf("") }
    val ingL = Reading.lines(ings); val stepL = Reading.lines(steps)
    Intro("write", d.s)
    val dish = Dish("draft", title.ifBlank { tr("Без названия", "Untitled") }, origin, mins.toIntOrNull() ?: 0, ingL, stepL, page = draft.page)
    Eyebrow(if (draft.memory) tr("По памяти", "From memory") else if (draft.editId != 0L) tr("Правка свитка", "Editing a scroll") else tr("Новый свиток", "A new scroll"))
    Text(tr("Записать заклинание", "Write down a spell"), style = T.h2, modifier = Modifier.padding(top = 4.dp))
    if (draft.note.isNotEmpty()) Text(draft.note, style = T.small.copy(color = C.goldL), modifier = Modifier.padding(top = 10.dp).fillMaxWidth()
        .clip(RoundedCornerShape(12.dp)).background(C.gold.copy(alpha = .08f)).border(1.dp, C.goldD, RoundedCornerShape(12.dp)).padding(10.dp))
    if (draft.page.isNotEmpty()) { Gap(10.dp); PlateImage(draft.page, "", emptyList(), Modifier.fillMaxWidth().heightIn(max = 220.dp).aspectRatio(1.3f).clip(RoundedCornerShape(12.dp)), 900) }
    Label(tr("Название", "Name")); Field(title, { title = it }, hint = tr("например, мамин борщ", "e.g. mum's borscht"))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(2f)) { Label(tr("Откуда", "From")); Field(origin, { origin = it }, hint = tr("кухня, книга, человек", "a cuisine, a book, a person")) }
        Column(Modifier.weight(1f)) { Label(tr("Минут", "Minutes")); Field(mins, { v -> mins = v.filter { it.isDigit() }.take(4) }, number = true) }
    }
    Label(tr("Ингредиенты – каждый с новой строки", "Reagents – one per line"))
    Field(ings, { ings = it }, hint = tr("200 г тыквы\nщепотка мускатного ореха", "200 g pumpkin\na pinch of nutmeg"), lines = 5, style = T.body.copy(fontSize = 14.sp))
    Label(if (draft.memory) tr("Шаги – если помнишь", "Steps – if you remember") else tr("Шаги – каждый с новой строки. Если указать время («20 минут»), появится свеча-таймер", "Steps – one per line. A time in the text («20 minutes») becomes a candle"))
    Field(steps, { steps = it }, hint = tr("Обжарь лук до золота, 10 минут.", "Fry the onion until golden, 10 minutes."), lines = 5, style = T.body.copy(fontSize = 14.sp))
    Gap(14.dp)
    Panel(glow = true) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Eyebrow(tr("Получится так", "It will read"))
                Text(dish.title, style = T.h3, modifier = Modifier.padding(top = 4.dp))
                val crafts = Crafts.of(stepL).mapNotNull { Crafts.byId[it]?.name }
                Text(listOfNotNull(origin.ifBlank { null }, (mins.toIntOrNull() ?: 0).takeIf { it > 0 }?.let { minutes(it) }, crafts.joinToString(", ").ifEmpty { null }).joinToString(" · "), style = T.small)
            }
            Sigil(dish.reagents, 66.dp, color = C.dim, glow = false)
        }
        Chips { ingL.forEach { l -> val j = Jars.of(l); val isNew = j != null && j.id !in d.s.jars
            Chip((if (isNew) "✦ " else "") + l + if (j != null && !isNew) tr(" · есть на полке", " · on the shelf") else "", if (isNew) ChipKind.NEW else ChipKind.PLAIN) } }
        stepL.forEachIndexed { i, st -> val t = Reading.timer(st); if (t > 0) Text(tr("🕯 шаг ${i + 1}: свеча-таймер на ${minutes(t)}", "🕯 step ${i + 1}: a candle for ${minutes(t)}"), style = T.small, modifier = Modifier.padding(top = 6.dp)) }
    }
    if (warn.isNotEmpty()) Text(warn, style = T.small.copy(color = C.goldL), modifier = Modifier.padding(top = 10.dp))
    Gap(12.dp)
    GoldButton(if (draft.memory) tr("Дальше – как вышло?", "Next – how did it go?") else tr("Записать в свитки", "Save to the scrolls")) {
        if (title.isBlank() || ingL.isEmpty() || (stepL.isEmpty() && !draft.memory)) { warn = tr("Нужны название, ингредиенты и шаги.", "A name, reagents and steps are needed."); return@GoldButton }
        scope.launch {
            val m = mins.toIntOrNull() ?: 0
            if (draft.editId != 0L) {
                c.dao.recipe(draft.editId)?.let { c.dao.update(it.copy(title = title.trim(), origin = origin.trim(), minutes = m, ingredients = ings.trim(), steps = steps.trim())) }
                nav.sheet = null; return@launch
            }
            val id = c.dao.insert(newRecipe(title, origin, m, ings, steps, draft.source, draft.url, draft.page))
            val r = c.dao.recipe(id)!!
            nav.sheet = null
            if (draft.memory) { nav.memory = true; nav.cook = Dish.of(r) }
            else { nav.tab = 2; nav.bookTab = 1; c.news.tryEmit(tr("Свиток записан. Сигил загорится, когда приготовишь блюдо.", "Scroll saved. Its sigil lights up once you cast it.")) }
        }
    }
}

@Composable
private fun ScrollSheet(dish: Dish, d: Data, nav: Nav) {
    val c = LocalContext.current.container; val scope = rememberCoroutineScope()
    val recipe = dish.recipeId?.let { id -> d.recipes.firstOrNull { it.id == id } } ?: dish.libId?.let { lib -> d.recipes.firstOrNull { it.libId == lib } }
    val cooked = recipe != null && recipe.id in d.cookedIds
    DishCard(dish, d, ink = !cooked)
    Gap(12.dp)
    Panel {
        Eyebrow(tr("Шаги", "Steps"))
        dish.steps.forEachIndexed { i, st -> val t = Reading.timer(st)
            Text("${i + 1}. $st" + if (t > 0) "  🕯" else "", style = T.body.copy(fontSize = 14.sp), modifier = Modifier.padding(top = 8.dp)) }
    }
    if (dish.page.isNotEmpty()) { Gap(12.dp); PlateImage(dish.page, "", emptyList(), Modifier.fillMaxWidth().aspectRatio(.75f).clip(RoundedCornerShape(12.dp)), 1400) }
    Gap(4.dp)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        GhostButton(tr("Загадать на луну", "Set for the moon"), Modifier.weight(1f)) { scope.launch { c.setSpell(dish); nav.sheet = null; c.news.tryEmit(tr("Заклинание загадано на эту луну", "Set as this moon's spell")) } }
        GoldButton(tr("Колдовать", "Cast"), Modifier.weight(1f)) { nav.sheet = null; nav.cook = dish }
    }
    if (recipe == null || !recipe.scroll) GhostButton(tr("Отложить в свитки", "Save to the scrolls"), Modifier.padding(top = 10.dp)) { scope.launch { c.toScroll(dish); nav.sheet = null; c.news.tryEmit(tr("Добавлено в свитки", "Copied to the scrolls")) } }
    if (recipe != null && recipe.libId.isEmpty()) Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        Text(tr("Править", "Edit"), style = T.small.copy(color = C.gold), modifier = Modifier.clickable {
            nav.sheet = Sheet.Editor(Draft(recipe.title, recipe.origin, if (recipe.minutes > 0) "${recipe.minutes}" else "", recipe.ingredients, recipe.steps, recipe.source, recipe.url, recipe.page, editId = recipe.id)) })
        if (!cooked) Text(tr("Сжечь свиток", "Burn the scroll"), style = T.small.copy(color = C.muted), modifier = Modifier.clickable { scope.launch { c.dao.deleteRecipe(recipe.id); nav.sheet = null } })
    }
    if (recipe != null && recipe.libId.isNotEmpty() && recipe.scroll && !cooked)
        Text(tr("Убрать из свитков", "Remove from the scrolls"), style = T.small.copy(color = C.muted), textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp).clickable { scope.launch { c.dao.update(recipe.copy(scroll = false)); nav.sheet = null } })
}

/** A page of the book of shadows: the latest cast of a recipe on parchment, and every time it was cast. */
@Composable
private fun EntrySheet(recipeId: Long, d: Data, nav: Nav) {
    val c = LocalContext.current.container; val scope = rememberCoroutineScope()
    val casts = d.casts.filter { it.recipeId == recipeId }
    val recipe = d.recipes.firstOrNull { it.id == recipeId }
    val e = casts.firstOrNull { it.verdict != "miss" } ?: casts.firstOrNull() ?: return
    val reagents = e.reagents.split(',')
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(C.paper).padding(horizontal = 18.dp, vertical = 22.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.rotate(-3f).width(186.dp).background(Color(0xFFF8F4EC)).padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp)) {
                PlateImage(e.photo, e.title, reagents, Modifier.fillMaxWidth().aspectRatio(1f), 700)
                Text(fmtDayYear(e.day), style = T.hand.copy(fontSize = 19.sp, color = Color(0xFF3A3424)), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 2.dp))
            }
            Column(Modifier.weight(1f).padding(start = 10.dp, top = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Sigil(reagents, 86.dp, color = C.sepia, glow = false)
                Text(Intents.name(e.intent).ifEmpty { tr("без намерения", "no intention") }, style = T.tiny.copy(color = C.paperMute), modifier = Modifier.padding(top = 4.dp))
            }
        }
        Text(e.title, style = T.h2.copy(color = C.paperInk), modifier = Modifier.padding(top = 20.dp))
        if (e.note.isNotBlank()) Text(e.note, style = T.hand, modifier = Modifier.padding(top = 10.dp))
        recipe?.let { Text(Reading.lines(it.ingredients).joinToString(" · ") { l -> Reading.bare(l).lowercase() }, style = T.small.copy(color = C.paperMute), modifier = Modifier.padding(top = 10.dp)) }
        if (e.guests.isNotBlank()) { Text(tr("За столом:", "At the table:"), style = T.tiny.copy(color = C.paperMute), modifier = Modifier.padding(top = 14.dp))
            Text(e.guests.split(',').joinToString("   ") { it.trim() }, style = T.hand.copy(color = C.seal, fontSize = 25.sp)) }
        HorizontalDivider(Modifier.padding(top = 14.dp), color = C.paperMute.copy(alpha = .4f))
        Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            MoonDot(Moon.phase(e.age), 14.dp); HGap(6.dp)
            Text(listOfNotNull(Moon.name(e.age).lowercase(), weatherLine(e.weather, e.temp).ifEmpty { null }, Wheel.byId[e.sabbat]?.name,
                e.marks.split(',').mapNotNull { Marks.byId[it]?.name }.joinToString(", ").ifEmpty { null }?.let { "✦ $it" },
                when (e.verdict) { "ok" -> tr("удалось", "worked"); "meh" -> tr("съедобно", "edible"); else -> tr("осечка", "misfire") }).joinToString(" · "),
                style = T.tiny.copy(color = C.paperMute))
        }
    }
    if (casts.size > 1) { Gap(14.dp); Panel {
        Eyebrow(tr("История приготовлений", "Every time it was cast"))
        casts.forEach { cs -> Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            MoonDot(Moon.phase(cs.age), 16.dp); HGap(8.dp)
            Text(listOfNotNull(fmtDayYear(cs.day), Intents.name(cs.intent).ifEmpty { null }, when (cs.verdict) { "ok" -> "✦"; "meh" -> "~"; else -> "💨" }, cs.note.take(40).ifEmpty { null }).joinToString(" · "), style = T.small)
        } }
    } }
    Gap(4.dp)
    recipe?.let { r -> GoldButton(tr("Приготовить снова", "Cast it again")) { nav.sheet = null; nav.cook = Dish.of(r) } }
    Text(tr("Вырвать страницу", "Tear out this page"), style = T.small.copy(color = C.muted), textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp).clickable { scope.launch { c.dao.deleteCast(e.id); if (casts.size <= 1) nav.sheet = null } })
}

@Composable
private fun JarSheet(id: String, d: Data, nav: Nav) {
    val ctx = LocalContext.current; val c = ctx.container; val scope = rememberCoroutineScope()
    val j = Jars.byId[id] ?: return
    val has = id in d.s.jars
    val used = d.good.filter { it.reagents.split(',').contains(id) }.map { it.title }.distinct()
    val where = remember(id) { c.library().filter { l -> l.ingredients.any { Jars.of(it)?.id == id } }.take(8) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(70.dp, 90.dp)) { jar(j, null, false) }; HGap(14.dp)
        Column { Eyebrow(if (has) tr("Банка", "A jar") else tr("Ещё не на полке", "Not on the shelf yet")); Text(j.name, style = T.h2) }
    }
    Gap(10.dp)
    Panel { Eyebrow(tr("Поверье", "The old belief")); Text(j.lore, style = T.italic, modifier = Modifier.padding(top = 4.dp)) }
    Gap(10.dp)
    if (has) Panel { Eyebrow(tr("В книге", "In the book")); Text(used.joinToString(", ").ifEmpty { tr("Стоит давно – пора пустить в дело.", "It's been waiting – time to use it.") }, style = T.sub, modifier = Modifier.padding(top = 4.dp)) }
    if (where.isNotEmpty()) { Gap(10.dp); Panel { Eyebrow(tr("Встречается в", "Found in")); Chips { where.forEach { l -> Chip(l.title) { nav.sheet = Sheet.Scroll(Dish.of(l)) } } } } }
    Gap(12.dp)
    if (!has) GhostButton(tr("Она у меня уже есть", "I already have it")) { scope.launch { c.settings.update { it.copy(jars = it.jars + id) }; nav.sheet = null } }
}

@Composable
private fun MarksSheet(d: Data) {
    val earned = Marks.all.filter { it.id in d.s.marks }; val hidden = Marks.all.filter { it.id !in d.s.marks }
    Eyebrow(tr("Ведьмины знаки", "Witch's marks"))
    Text("${earned.size} / ${Marks.all.size}", style = T.h2, modifier = Modifier.padding(top = 4.dp))
    Text(tr("Знаки не видны заранее – они просто появляются.", "Marks can't be seen in advance – they simply appear."), style = T.sub)
    earned.forEach { m -> MarkRow(m.id, m.name, fmtDay(d.s.marks.getValue(m.id)), true) }
    hidden.forEach { m -> MarkRow(m.id, "???", m.hint, false) }
}

@Composable
private fun MarkRow(id: String, name: String, sub: String, on: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(42.dp)) { markGlyph(id, if (on) C.gold else C.dim) }; HGap(12.dp)
        Column { Text(name, style = T.h3.copy(color = if (on) C.ink else C.muted)); Text(sub, style = if (on) T.small else T.hand.copy(color = C.dim, fontSize = 19.sp)) }
    }
    HorizontalDivider(color = C.line)
}

@Composable
private fun PantrySheet(d: Data) {
    val c = LocalContext.current.container; val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    Eyebrow(tr("Кладовая", "The pantry"))
    Text(tr("Что залежалось?", "What's been waiting?"), style = T.h2, modifier = Modifier.padding(top = 4.dp))
    Text(tr("Полпучка кинзы, кусок тыквы, сливки на донышке. Кости подберут блюдо из этих остатков, а когда приготовишь его, они сами исчезнут из списка.",
        "Half a bunch of coriander, a wedge of pumpkin, the last of the cream. The bones will find them a spell, and once it's cast they leave this list by themselves."), style = T.sub)
    Gap(10.dp)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Field(name, { name = it }, Modifier.weight(1f), hint = tr("например, кинза", "e.g. coriander"))
        HGap(8.dp)
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(C.gold).clickable { scope.launch { c.addLeftover(name); name = "" } }, contentAlignment = Alignment.Center) {
            Text("+", style = T.h2.copy(color = C.onGold)) }
    }
    d.leftovers.forEach { l -> Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(l.name, style = T.body, modifier = Modifier.weight(1f)); Text(fmtDay(l.day), style = T.tiny)
        Text("  ×", style = T.h3.copy(color = C.muted), modifier = Modifier.clickable { scope.launch { c.dao.deleteLeftover(l.id) } }.padding(start = 8.dp)) }
        HorizontalDivider(color = C.line) }
}

@Composable
private fun SettingsSheet(d: Data) {
    val c = LocalContext.current.container; val scope = rememberCoroutineScope()
    var q by remember { mutableStateOf("") }
    var towns by remember { mutableStateOf<List<Town>>(emptyList()) }
    Eyebrow(tr("Настройки", "Settings"))
    Text(tr("За окном", "Outside the window"), style = T.h2, modifier = Modifier.padding(top = 4.dp))
    Text(if (d.s.town.isNotEmpty()) tr("Погода за окном – как сейчас в городе ${d.s.town}.", "Weather and moon as in ${d.s.town}.") else tr("Город не выбран – за окном будет просто ночной лес.", "No town chosen – the window just shows night."), style = T.sub)
    Gap(8.dp)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Field(q, { q = it }, Modifier.weight(1f), hint = tr("найти город", "find a town")); HGap(8.dp)
        GoldButton(tr("Найти", "Find"), Modifier.width(90.dp), enabled = q.length >= 2) { scope.launch { towns = withContext(Dispatchers.IO) { runCatching { OutsideSource.towns(q, Lang.ru) }.getOrDefault(emptyList()) } } }
    }
    towns.forEach { t -> Text("${t.name} · ${t.region}", style = T.body, modifier = Modifier.fillMaxWidth().clickable {
        scope.launch { c.settings.update { it.copy(town = t.name, lat = t.lat, lon = t.lon, outside = "") }; towns = emptyList(); q = ""; c.refreshOutside(true) } }.padding(vertical = 10.dp)) }
    Gap(16.dp)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(tr("Колокольчик свечи", "Candle bell"), style = T.body); Text(tr("звенит, когда выходит время на свече-таймере","rings when a candle timer burns out"), style = T.small) }
        Switch(d.s.chime, { v -> scope.launch { c.settings.update { it.copy(chime = v) } } }, colors = SwitchDefaults.colors(checkedTrackColor = C.gold, checkedThumbColor = C.onGold))
    }
    Gap(16.dp)
    GhostButton(if (d.s.seen.isEmpty()) tr("Подсказки уже включены", "Hints are on") else tr("Показать подсказки снова", "Show the hints again"), enabled = d.s.seen.isNotEmpty()) {
        scope.launch { c.settings.update { it.copy(seen = emptySet()) } } }
    Gap(16.dp)
    Text(tr("Язык", "Language"), style = T.body)
    val langs = listOf("" to tr("Как в телефоне", "As the phone"), "ru" to "Русский", "en" to "English")
    Seg(langs.map { it.second }, langs.indexOfFirst { it.first == currentLanguage() }.coerceAtLeast(0), Modifier.padding(top = 8.dp)) { setLanguage(langs[it].first) }
    Gap(20.dp)
    Text("StillCraft", style = T.h3)
    Text(tr("Кухонная ведьма – одно из приложений StillCraft. Пишите нам в Telegram.", "Kitchen Witch is one of the StillCraft apps. Talk to us on Telegram."), style = T.small)
    Gap(6.dp)
    val ctx = LocalContext.current
    listOf(
        Triple(tr("Сообщить об ошибке", "Report a bug"), tr("что-то сломалось или выглядит не так", "something broke or looks wrong"), Community.BUG),
        Triple(tr("Предложить идею", "Suggest an idea"), tr("чего не хватает на кухне", "what the kitchen is missing"), Community.IDEA),
        Triple(tr("Следить за обновлениями", "Follow updates"), tr("бот напишет, когда выйдет новая версия", "the bot writes when a new version is out"), Community.FOLLOW),
        Triple(tr("Чат StillCraft", "StillCraft chat"), tr("все приложения и те, кто ими пользуется", "all the apps and the people who use them"), Community.CHAT),
        Triple(tr("Поддержать приложение", "Support the app"), tr("на новые рецепты и праздники", "for new recipes and feasts"), Community.SUPPORT),
        Triple(tr("Исходный код", "Source code"), "github.com/bigdevwhale/kitchenwitch", Community.SOURCE),
    ).forEach { (t, sub, url) ->
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { Community.open(ctx, url) }.padding(vertical = 9.dp)) {
            Text(t, style = T.body.copy(color = C.goldL)); Text(sub, style = T.small) }
    }
    Gap(16.dp)
    Text(tr("Кухонная ведьма ${app.kitchenwitch.BuildConfig.VERSION_NAME} · без рекламы и аккаунтов. Шрифты Cormorant Garamond, Inter и Caveat (SIL OFL).",
        "Kitchen Witch ${app.kitchenwitch.BuildConfig.VERSION_NAME} · no ads, no accounts. Fonts Cormorant Garamond, Inter and Caveat (SIL OFL)."), style = T.tiny)
    Gap(8.dp)
    Text(tr("Всё хранится только на этом телефоне. В интернет уходят лишь координаты города, чтобы узнать погоду, и ссылки на рецепты, которые ты открываешь.",
        "Everything stays on this phone. Only the town's coordinates go out, for the weather, and the links you open yourself."), style = T.tiny)
}

/** The StillCraft bot: deep links file a bug or an idea for this app, or follow its releases. */
object Community {
    private const val BOT = "https://t.me/ArcaneWhisperBot?start="
    const val SLUG = "kitchenwitch"
    const val BUG = "${BOT}bug_$SLUG"
    const val IDEA = "${BOT}wish_$SLUG"
    const val FOLLOW = "${BOT}f_$SLUG"
    const val CHAT = "https://t.me/+Q4gFEDk8O842OGVi"
    const val SUPPORT = "https://bigdevwhale.github.io/stillcraft/support/"
    const val SOURCE = "https://github.com/bigdevwhale/kitchenwitch"
    fun open(ctx: android.content.Context, url: String) = runCatching {
        ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) }
}
