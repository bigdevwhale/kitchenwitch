package app.kitchenwitch.ui.cook

import app.kitchenwitch.ui.components.Intro
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import app.kitchenwitch.CastResult
import app.kitchenwitch.container
import app.kitchenwitch.data.Dish
import app.kitchenwitch.domain.Intents
import app.kitchenwitch.domain.Jars
import app.kitchenwitch.domain.Moon
import app.kitchenwitch.domain.Reading
import app.kitchenwitch.domain.minutes
import app.kitchenwitch.domain.tr
import app.kitchenwitch.ui.Data
import app.kitchenwitch.ui.Nav
import app.kitchenwitch.ui.PlateImage
import app.kitchenwitch.ui.Photos
import app.kitchenwitch.ui.art.Sigil
import app.kitchenwitch.ui.art.clock
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
import app.kitchenwitch.ui.theme.C
import app.kitchenwitch.ui.theme.T
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

private enum class Stage { INTENT, STEPS, VERDICT, DONE }

/** Cooking a dish: an intention, the steps one by one with candle timers, how it went, and the sigil drawn at the end. */
@Composable
fun CookScreen(dish: Dish, d: Data, nav: Nav) {
    val ctx = LocalContext.current; val c = ctx.container; val scope = rememberCoroutineScope()
    var stage by remember { mutableStateOf(if (nav.memory) Stage.VERDICT else Stage.INTENT).also { nav.memory = false } }
    var intent by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(0) }
    var verdict by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var guests by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<CastResult?>(null) }
    val view = LocalView.current
    DisposableEffect(Unit) { view.keepScreenOn = true; onDispose { view.keepScreenOn = false } }
    val steps = dish.steps.ifEmpty { listOf(tr("По памяти.", "From memory.")) }
    val left = d.leftovers.map { it.name }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 18.dp).padding(top = 10.dp, bottom = 14.dp)) {
        if (stage != Stage.DONE) Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(if (stage == Stage.STEPS) tr("${dish.title} · шаг ${step + 1} из ${steps.size}", "${dish.title} · step ${step + 1} of ${steps.size}") else tr("Перед заклинанием", "Before the spell"), Modifier.weight(1f))
            Box(Modifier.size(36.dp).clip(CircleShape).background(C.card).clickable { nav.cook = null }, contentAlignment = Alignment.Center) { Text("×", style = T.h3.copy(color = C.muted)) }
        }
        when (stage) {
            Stage.INTENT -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Gap(8.dp)
                Intro("cook", d.s)
                Text(dish.title, style = T.title)
                Text(listOfNotNull(dish.from.ifBlank { null }, if (dish.minutes > 0) minutes(dish.minutes) else null).joinToString(" · "), style = T.sub)
                Box(Modifier.fillMaxWidth().padding(vertical = 22.dp), contentAlignment = Alignment.Center) { Sigil(dish.reagents, 130.dp) }
                Text(tr("С каким намерением?", "With what intention?"), style = T.h3)
                Text(tr("Можно и без него. Намерение запишется в книгу.", "You can go without. The dish gets a mark in the book."), style = T.sub)
                Chips { Intents.all.forEach { i -> Chip(Intents.name(i), if (intent == i) ChipKind.SEL else ChipKind.PLAIN) { intent = if (intent == i) "" else i } } }
                Gap(18.dp)
                Eyebrow(tr("Что понадобится", "You will need"))
                Chips { dish.ingredients.forEach { ing ->
                    val j = Jars.of(ing); val isNew = j != null && j.id !in d.s.jars; val isLeft = left.any { c.stemMatch(ing, it) }
                    Chip((if (isNew) "✦ " else "") + ing, when { isNew -> ChipKind.NEW; isLeft -> ChipKind.LEFT; else -> ChipKind.PLAIN })
                } }
                if (dish.page.isNotEmpty()) { Gap(16.dp); Eyebrow(tr("Страница", "The page")); Gap(8.dp)
                    PlateImage(dish.page, dish.title, emptyList(), Modifier.fillMaxWidth().aspectRatio(.75f).clip(RoundedCornerShape(12.dp)), 1400) }
            }
            Stage.STEPS -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    steps.indices.forEach { i -> Box(Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(if (i <= step) C.gold else C.line)) }
                }
                Text(steps[step], style = T.step, modifier = Modifier.padding(top = 20.dp))
                val t = Reading.timer(steps[step])
                if (t > 0) Candle(t, key = step, chime = d.s.chime)
                if (dish.page.isNotEmpty()) { Gap(16.dp); PlateImage(dish.page, dish.title, emptyList(), Modifier.fillMaxWidth().aspectRatio(.75f).clip(RoundedCornerShape(12.dp)), 1400) }
            }
            Stage.VERDICT -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Gap(8.dp)
                Text(tr("Как вышло?", "How did it go?"), style = T.title)
                Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(Triple("ok", "✦", tr("Удалось", "It worked")), Triple("meh", "~", tr("Съедобно", "Edible")), Triple("miss", "💨", tr("Осечка", "Misfire"))).forEach { (k, g, l) ->
                        val on = verdict == k
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(if (on) C.gold.copy(alpha = .12f) else Color.Transparent)
                            .border(1.dp, if (on) C.gold else C.line, RoundedCornerShape(14.dp)).clickable { verdict = k }.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(g, style = T.h2); Text(l, style = T.small)
                        }
                    }
                }
                Label(tr("Пометка на полях", "A note in the margin"))
                Field(note, { note = it }, hint = tr("что запомнить в следующий раз…", "what to remember next time…"), lines = 3, style = T.hand.copy(color = C.ink))
                Label(tr("Кто был за столом", "Who was at the table"))
                Field(guests, { guests = it }, hint = tr("имена через запятую; можно оставить пустым", "names, comma-separated – or no one"))
                Gap(14.dp)
                PhotoButton(photo) { photo = it }
            }
            Stage.DONE -> Done(dish, result, nav)
        }
        when (stage) {
            Stage.INTENT -> GoldButton(tr("Зажечь плиту", "Light the stove")) { stage = Stage.STEPS }
            Stage.STEPS -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (step > 0) GhostButton(tr("Назад", "Back"), Modifier.weight(1f)) { step-- }
                GoldButton(if (step == steps.lastIndex) tr("Заклинание сотворено", "The spell is cast") else tr("Дальше", "Next"), Modifier.weight(2f)) {
                    if (step == steps.lastIndex) stage = Stage.VERDICT else step++ }
            }
            Stage.VERDICT -> GoldButton(tr("Записать в книгу теней", "Write it in the book of shadows"), enabled = verdict.isNotEmpty()) {
                scope.launch {
                    result = c.cast(dish, intent, verdict, note, photo, guests.split(',').map { it.trim() }.filter { it.isNotEmpty() })
                    stage = Stage.DONE
                }
            }
            Stage.DONE -> GoldButton(if (result?.miss == true) tr("В журнал осечек", "To the misfires") else tr("Открыть книгу теней", "Open the book of shadows")) {
                nav.bookTab = if (result?.miss == true) 2 else 0; nav.tab = 2; nav.cook = null }
        }
    }
}

/** A candle that burns down for [minutes]: light it, and a bell rings when it's out. */
@Composable
private fun Candle(minutes: Int, key: Int, chime: Boolean) {
    val ctx = LocalContext.current
    var started by remember(key) { mutableLongStateOf(0L) }
    var now by remember(key) { mutableLongStateOf(System.currentTimeMillis()) }
    val total = minutes * 60_000L
    val leftMs = if (started == 0L) total else (total - (now - started)).coerceAtLeast(0)
    val out = started != 0L && leftMs == 0L
    LaunchedEffect(started) {
        if (started == 0L) return@LaunchedEffect
        while (System.currentTimeMillis() - started < total) { now = System.currentTimeMillis(); delay(500) }
        now = System.currentTimeMillis()
        if (chime) runCatching { RingtoneManager.getRingtone(ctx, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))?.play() }
    }
    val t = clock()
    Column(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.size(60.dp, 170.dp)) {
            val frac = leftMs.toFloat() / total
            val w = 30.dp.toPx(); val full = 130.dp.toPx(); val h = (full * (.12f + .88f * frac))
            val x = (size.width - w) / 2; val top = size.height - h
            if (started != 0L && !out) drawCircle(Brush.radialGradient(listOf(C.amber.copy(alpha = .35f), Color.Transparent), Offset(size.width / 2, top - 14.dp.toPx()), 50.dp.toPx()), 50.dp.toPx(), Offset(size.width / 2, top - 14.dp.toPx()))
            drawRoundRect(Brush.horizontalGradient(listOf(Color(0xFFD8CCB4), Color(0xFFF4ECDC), Color(0xFFCFC2A8)), x, x + w), Offset(x, top), Size(w, h), CornerRadius(5.dp.toPx()))
            drawRect(Color(0xFF2A2020), Offset(size.width / 2 - 1.dp.toPx(), top - 9.dp.toPx()), Size(2.dp.toPx(), 10.dp.toPx()))
            if (started != 0L && !out) {
                val cx = size.width / 2; val base = top - 7.dp.toPx(); val fh = 26.dp.toPx() * (1 + .07f * sin(t * 9))
                val sway = 2.dp.toPx() * sin(t * 4)
                val p = Path().apply { moveTo(cx + sway, base - fh); cubicTo(cx + 9.dp.toPx(), base - fh * .5f, cx + 8.dp.toPx(), base, cx, base); cubicTo(cx - 8.dp.toPx(), base, cx - 9.dp.toPx(), base - fh * .5f, cx + sway, base - fh) }
                drawPath(p, Color(0xFFFFC760)); drawCircle(Color(0xFFFFF6D0), 4.dp.toPx(), Offset(cx, base - 7.dp.toPx()))
            }
        }
        val mm = leftMs / 60000; val ss = (leftMs / 1000) % 60
        Text(when { out -> tr("Время вышло – загляни в котёл", "The candle is out – check the pot"); started == 0L -> tr("Свеча-таймер на ${minutes(minutes)}", "A candle for ${minutes(minutes)}")
            else -> "%d:%02d".format(mm, ss) }, style = if (out) T.body.copy(color = C.gold) else T.sub, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
        Gap(10.dp)
        when {
            started == 0L -> GhostButton(tr("Зажечь свечу", "Light the candle")) { started = System.currentTimeMillis(); now = started }
            !out -> GhostButton(tr("Задуть", "Blow it out")) { started = 0L }
        }
    }
}

@Composable
private fun PhotoButton(photo: String, onPhoto: (String) -> Unit) {
    val ctx = LocalContext.current
    var pending by remember { mutableStateOf<java.io.File?>(null) }
    val shoot = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok -> pending?.let { f -> if (ok && f.length() > 0) onPhoto(f.name) else f.delete() } }
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val f = Photos.newFile(ctx, "plate"); runCatching { ctx.contentResolver.openInputStream(uri)?.use { i -> f.outputStream().use { i.copyTo(it) } }; onPhoto(f.name) } }
    Panel(onClick = {
        val f = Photos.newFile(ctx, "plate"); pending = f
        runCatching { shoot.launch(FileProvider.getUriForFile(ctx, ctx.packageName + ".files", f)) }
    }) {
        if (photo.isNotEmpty()) PlateImage(photo, "", emptyList(), Modifier.fillMaxWidth().aspectRatio(1.3f).clip(RoundedCornerShape(10.dp)), 900)
        else Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📷", fontSize = 28.sp)
            Text(tr("Сфотографировать тарелку", "Photograph the plate"), style = T.body)
            Text(tr("камерой телефона – снимок вклеится в книгу", "with the phone's camera – it goes into the book"), style = T.small)
        }
    }
    Text(tr("или выбрать из галереи", "or pick from the gallery"), style = T.small.copy(color = C.gold), textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clickable { pick.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) })
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.Done(dish: Dish, r: CastResult?, nav: Nav) {
    val draw = remember { Animatable(0f) }
    LaunchedEffect(Unit) { delay(300); draw.animateTo(1f, tween(1800, easing = FastOutSlowInEasing)) }
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(40.dp))
        if (r?.miss == true) {
            Text("💨", fontSize = 64.sp)
            Text(tr("Котёл бахнул", "The cauldron went bang"), style = T.title, modifier = Modifier.padding(top = 12.dp))
            Text(tr("Неудача записана. Получится со второго раза – будет знак «Второй шанс».", "The misfire is written down. Make it good and earn «Second chance»."), style = T.sub, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
            return@Column
        }
        Sigil(dish.reagents, 200.dp, progress = draw.value)
        val a = ((draw.value - .8f) / .2f).coerceIn(0f, 1f)
        Column(Modifier.fillMaxWidth().padding(top = 18.dp).let { if (a < 1f) it.then(Modifier) else it }, horizontalAlignment = Alignment.CenterHorizontally) {
            Eyebrow(if (r?.kept == true) tr("Обещание луне сдержано", "The moon's word is kept") else tr("Новое заклинание", "A new spell"), color = C.muted.copy(alpha = a))
            Text(dish.title, style = T.title.copy(color = C.ink.copy(alpha = a)), textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
            Text(Moon.name(Moon.age()).lowercase(), style = T.sub.copy(color = C.muted.copy(alpha = a)))
        }
        if (a >= 1f) {
            r?.newJars?.forEach { j -> Gap(12.dp); Panel { Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(44.dp, 56.dp)) { jar(j, null, false) }; HGap(12.dp)
                Column { Eyebrow(tr("Новая банка на полке", "A new jar on the shelf")); Text(j.name, style = T.h3); Text(j.lore, style = T.small) } } } }
            r?.marks?.forEach { m -> Gap(12.dp); Panel { Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(42.dp)) { markGlyph(m.id, C.gold) }; HGap(12.dp)
                Column { Eyebrow(tr("Ведьмин знак", "A witch's mark")); Text(m.name, style = T.h3) } } } }
        }
    }
}
