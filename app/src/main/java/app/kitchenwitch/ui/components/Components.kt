package app.kitchenwitch.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.kitchenwitch.ui.theme.C
import app.kitchenwitch.ui.theme.T

private val R18 = RoundedCornerShape(18.dp)
private val R14 = RoundedCornerShape(14.dp)

/** A card in the dark: moss-dark with a faint line; [glow] is the gold-edged one that holds the moon's spell. */
@Composable
fun Panel(modifier: Modifier = Modifier, glow: Boolean = false, onClick: (() -> Unit)? = null, pad: Dp = 16.dp, content: @Composable ColumnScope.() -> Unit) {
    val m = modifier.fillMaxWidth().clip(R18)
        .background(if (glow) Brush.linearGradient(listOf(C.glowTop, C.glowBot)) else SolidColor(C.card))
        .border(1.dp, if (glow) C.goldD else C.line, R18)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(horizontal = pad, vertical = 14.dp)
    Column(m, content = content)
}

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = C.muted) = Text(text.uppercase(), style = T.eyebrow.copy(color = color), modifier = modifier)

@Composable
fun GoldButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Box(modifier.fillMaxWidth().heightIn(min = 50.dp).alpha(if (enabled) 1f else .4f).clip(R14)
        .drawBehind { drawRect(Brush.radialGradient(listOf(C.amber.copy(alpha = .25f), Color.Transparent), center, size.width)) }
        .background(Brush.verticalGradient(listOf(Color(0xFFE8C470), Color(0xFFC79A46))))
        .clickable(enabled = enabled, onClick = onClick).padding(horizontal = 14.dp, vertical = 13.dp), contentAlignment = Alignment.Center) {
        Text(text, style = T.btn, textAlign = TextAlign.Center)
    }
}

@Composable
fun GhostButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Box(modifier.fillMaxWidth().heightIn(min = 50.dp).alpha(if (enabled) 1f else .4f).clip(R14).border(1.dp, C.goldD, R14)
        .clickable(enabled = enabled, onClick = onClick).padding(horizontal = 14.dp, vertical = 13.dp), contentAlignment = Alignment.Center) {
        Text(text, style = T.btn.copy(color = C.goldL), textAlign = TextAlign.Center)
    }
}

enum class ChipKind { PLAIN, NEW, LEFT, SEL, DASHED }

@Composable
fun Chip(text: String, kind: ChipKind = ChipKind.PLAIN, onClick: (() -> Unit)? = null) {
    val shape = RoundedCornerShape(50)
    val (bg, border, fg) = when (kind) {
        ChipKind.NEW -> Triple(Color.Transparent, C.gold, C.goldL)
        ChipKind.LEFT -> Triple(Color.Transparent, C.moss, C.mossL)
        ChipKind.SEL -> Triple(C.gold, C.gold, C.onGold)
        ChipKind.DASHED -> Triple(Color.Transparent, C.dim, C.muted)
        ChipKind.PLAIN -> Triple(Color.White.copy(alpha = .03f), C.line, C.ink)
    }
    Text(text, style = T.small.copy(color = fg), modifier = Modifier.clip(shape).background(bg).border(BorderStroke(1.dp, border), shape)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }.padding(horizontal = 11.dp, vertical = 5.dp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Chips(modifier: Modifier = Modifier, content: @Composable () -> Unit) =
    FlowRow(modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { content() }

/** A segmented switch. */
@Composable
fun Seg(options: List<String>, sel: Int, modifier: Modifier = Modifier, onSel: (Int) -> Unit) {
    Row(modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = .03f)).border(1.dp, C.line, RoundedCornerShape(12.dp)).padding(3.dp)) {
        options.forEachIndexed { i, o ->
            Text(o, style = T.small.copy(color = if (i == sel) C.goldL else C.muted), textAlign = TextAlign.Center, maxLines = 1,
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(9.dp)).background(if (i == sel) C.card2 else Color.Transparent)
                    .clickable { onSel(i) }.padding(vertical = 8.dp))
        }
    }
}

/** A sheet from the bottom in the forest colours. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WitchSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = C.bg, scrimColor = Color.Black.copy(alpha = .6f), shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        dragHandle = { Box(Modifier.padding(top = 10.dp, bottom = 6.dp).size(40.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(C.line)) }) {
        Column(Modifier.fillMaxWidth().drawBehind { drawRect(Brush.radialGradient(listOf(C.card2, Color.Transparent), Offset(size.width / 2, 0f), size.width)) }
            .padding(horizontal = 18.dp).padding(bottom = 18.dp).navigationBarsPadding(), content = content)
    }
}

/** A text field on the dark: a thin line box, the hint in muted, [hand] writes in the handwriting. */
@Composable
fun Field(value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier, hint: String = "", lines: Int = 1, number: Boolean = false, style: TextStyle = T.body) {
    val shape = RoundedCornerShape(12.dp)
    BasicTextField(value, onChange, modifier.fillMaxWidth().clip(shape).background(Color.White.copy(alpha = .03f)).border(1.dp, C.line, shape),
        textStyle = style, singleLine = lines == 1, minLines = lines, cursorBrush = SolidColor(C.gold),
        keyboardOptions = if (number) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        decorationBox = { inner -> Box(Modifier.padding(horizontal = 12.dp, vertical = 11.dp)) { if (value.isEmpty()) Text(hint, style = style.copy(color = C.dim)); inner() } })
}

@Composable
fun Label(text: String) { Spacer(Modifier.height(14.dp)); Text(text, style = T.small); Spacer(Modifier.height(6.dp)) }

/** A small drawn picture of fixed size. */
@Composable
fun Pic(size: Dp, modifier: Modifier = Modifier, draw: DrawScope.() -> Unit) = Canvas(modifier.size(size), onDraw = draw)

@Composable
fun Gap(h: Dp) = Spacer(Modifier.height(h))

@Composable
fun HGap(w: Dp) = Spacer(Modifier.width(w))
