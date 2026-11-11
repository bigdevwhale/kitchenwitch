package app.kitchenwitch

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import app.kitchenwitch.data.DemoSeed
import app.kitchenwitch.domain.Lang
import app.kitchenwitch.ui.Onboarding
import app.kitchenwitch.ui.Root
import app.kitchenwitch.ui.Start
import app.kitchenwitch.ui.theme.WitchTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** The main screens over the demo kitchen, drawn to PNGs (run with `-Prender`), in both languages. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w393dp-h852dp-xxhdpi")
abstract class ScreensBase {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    abstract val lang: String
    private val out by lazy { File(System.getProperty("render.out") ?: "build/art", "screens-$lang").apply { mkdirs() } }
    private val app get() = ApplicationProvider.getApplicationContext<WitchApp>()

    private fun shot(name: String, content: @Composable () -> Unit) {
        Lang.ru = lang == "ru"
        rule.mainClock.autoAdvance = false
        rule.setContent { WitchTheme { content() } }
        repeat(40) { rule.mainClock.advanceTimeBy(100); Thread.sleep(15) }
        rule.waitForIdle()
        rule.runOnUiThread {
            val view = rule.activity.window.decorView
            val image = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(image))
            File(out, "$name.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    private fun screen(name: String, tab: Int, cook: String? = null) {
        Lang.ru = lang == "ru"
        val s = runBlocking { DemoSeed.seedIfEmpty(app.container); app.container.settings.now() }
        Start.tab = tab; Start.cook = cook
        shot(name) { Root(s, null) }
    }

    @Test fun welcome() = shot("0-welcome") { Onboarding() }
    @Test fun kitchen() = screen("1-kitchen", 0)
    @Test fun moon() = screen("2-moon", 1)
    @Test fun book() = screen("3-book", 2)
    @Test fun shelf() = screen("4-shelf", 3)
    @Test fun wheel() = screen("5-wheel", 4)
    @Test fun cook() = screen("6-cook", 0, "musakhan")
}

class RenderScreensRu : ScreensBase() { override val lang = "ru" }
class RenderScreensEn : ScreensBase() { override val lang = "en" }
