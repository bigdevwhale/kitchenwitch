package app.kitchenwitch.ui.shelf

import app.kitchenwitch.ui.components.Intro
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kitchenwitch.domain.Jar
import app.kitchenwitch.domain.Jars
import app.kitchenwitch.domain.tr
import app.kitchenwitch.ui.Data
import app.kitchenwitch.ui.Nav
import app.kitchenwitch.ui.Sheet
import app.kitchenwitch.ui.art.jar
import app.kitchenwitch.ui.components.Eyebrow
import app.kitchenwitch.ui.components.Gap
import app.kitchenwitch.ui.theme.C
import app.kitchenwitch.ui.theme.T

@Composable
fun ShelfScreen(d: Data, nav: Nav) {
    val have = Jars.all.filter { it.id in d.s.jars }
    val empty = Jars.all.filter { it.id !in d.s.jars }.sortedBy { if (it.far) 1 else 0 }
    val cells: List<Pair<Jar, Boolean>> = have.map { it to false } + empty.map { it to true }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp).padding(top = 18.dp, bottom = 24.dp)) {
        Eyebrow(tr("Полка", "The shelf"), Modifier.padding(horizontal = 4.dp))
        Gap(6.dp)
        Text(tr("${have.size} из ${Jars.all.size} банок", "${have.size} of ${Jars.all.size} jars"), style = T.title, modifier = Modifier.padding(horizontal = 4.dp))
        Text(tr("С каждой новой специей на полке появляется новая банка. Пустые места ждут своего часа.", "Every new spice puts a new jar on the shelf. The empty places wait their turn."),
            style = T.sub, modifier = Modifier.padding(horizontal = 4.dp))
        Gap(14.dp)
        Intro("shelf", d.s, Modifier.padding(horizontal = 4.dp))
        cells.chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { (j, e) ->
                    Column(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable { nav.open(Sheet.JarInfo(j.id)) }.padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center) {
                            Canvas(Modifier.size(58.dp, 74.dp)) { jar(j, null, e) }
                            if (e) Text("?", style = T.h2.copy(color = C.dim), modifier = Modifier.padding(top = 18.dp))
                            else Text(j.name.take(1), style = T.h3.copy(fontSize = 15.sp, color = Color(0xFF3A2A30)), modifier = Modifier.padding(top = 25.dp))
                        }
                        Text(if (e) "" else j.name, style = T.tiny.copy(color = C.ink.copy(alpha = .8f)), maxLines = 1, overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center, modifier = Modifier.height(18.dp).padding(horizontal = 2.dp))
                    }
                }
                repeat(4 - row.size) { Box(Modifier.weight(1f)) }
            }
            Box(Modifier.fillMaxWidth().padding(bottom = 8.dp).height(10.dp).clip(RoundedCornerShape(2.dp)).background(Brush.verticalGradient(listOf(Color(0xFF4A3420), Color(0xFF24180E)))))
        }
    }
}
