package app.kitchenwitch.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.kitchenwitch.R

/** «Ведьмин лес»: a forest night, candle amber, old gold, moonlit text. */
object C {
    val bg = Color(0xFF0E1A14)
    val bgHi = Color(0xFF21432F)
    val bgLo = Color(0xFF173024)
    val card = Color(0xFF132A1F)
    val card2 = Color(0xFF1C3A2B)
    val glowTop = Color(0xFF1F3F2D)
    val glowBot = Color(0xFF11241A)
    val line = Color(0xFF27443A)
    val gold = Color(0xFFD9B25E)
    val goldL = Color(0xFFF2CF86)
    val goldD = Color(0xFF8A6D34)
    val amber = Color(0xFFF2B45A)
    val onGold = Color(0xFF17200F)
    val ink = Color(0xFFEBE6D3)
    val muted = Color(0xFF98AB9C)
    val dim = Color(0xFF5D7465)
    val moss = Color(0xFF3E6B4A)
    val mossL = Color(0xFF8FC29A)
    val brew = Color(0xFF7FD19A)
    val paper = Color(0xFFE9DCC0)
    val paperInk = Color(0xFF2F3A24)
    val paperMute = Color(0xFF6B604A)
    val seal = Color(0xFF7A2E22)
    val sepia = Color(0xFF7A5A2C)
    val nav = Color(0xFF0A140F)
    val wood = Color(0xFF2A1C10)
    val moonLit = Color(0xFFEFE8CF)
    val moonDark = Color(0xFF1A2B22)
    /** Ring colours of the moon's quarters: begin, try, sabbat, clear. */
    val quarters = listOf(gold, mossL, amber, Color(0xFF8AA7B0))
}

val Cormorant = FontFamily(Font(R.font.cormorant_semibold, FontWeight.SemiBold), Font(R.font.cormorant_bold, FontWeight.Bold),
    Font(R.font.cormorant_italic, FontWeight.Medium, FontStyle.Italic))
val Inter = FontFamily(Font(R.font.inter_regular, FontWeight.Normal), Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold), Font(R.font.inter_semibold, FontWeight.Bold))
val Hand = FontFamily(Font(R.font.caveat, FontWeight.Medium))

object T {
    val title = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 36.sp, color = C.ink)
    val h2 = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 29.sp, color = C.ink)
    val h3 = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 23.sp, color = C.ink)
    val italic = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic, fontSize = 20.sp, lineHeight = 24.sp, color = C.ink)
    val step = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.SemiBold, fontSize = 25.sp, lineHeight = 31.sp, color = C.ink)
    val body = TextStyle(fontFamily = Inter, fontSize = 15.sp, lineHeight = 21.sp, color = C.ink)
    val sub = TextStyle(fontFamily = Inter, fontSize = 14.sp, lineHeight = 19.sp, color = C.muted)
    val small = TextStyle(fontFamily = Inter, fontSize = 13.sp, lineHeight = 17.sp, color = C.muted)
    val tiny = TextStyle(fontFamily = Inter, fontSize = 11.sp, lineHeight = 14.sp, color = C.muted)
    val eyebrow = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 1.8.sp, color = C.muted)
    val btn = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = C.onGold)
    val num = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, color = C.goldL)
    val hand = TextStyle(fontFamily = Hand, fontWeight = FontWeight.Medium, fontSize = 23.sp, lineHeight = 25.sp, color = C.paperInk)
}

@Composable
fun WitchTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(primary = C.gold, background = C.bg, surface = C.card, onSurface = C.ink, onBackground = C.ink, onPrimary = C.onGold), content = content)
}
