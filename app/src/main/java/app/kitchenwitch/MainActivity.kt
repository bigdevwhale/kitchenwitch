package app.kitchenwitch

import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kitchenwitch.data.Settings
import app.kitchenwitch.domain.Lang
import app.kitchenwitch.ui.Onboarding
import app.kitchenwitch.ui.Root
import app.kitchenwitch.ui.Start
import app.kitchenwitch.ui.theme.WitchTheme
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    /** A link shared from the browser, to read as a recipe. */
    private var shared by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEMO && BuildConfig.DEMO_LANG.isNotEmpty() && AppCompatDelegate.getApplicationLocales().toLanguageTags() != BuildConfig.DEMO_LANG) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(BuildConfig.DEMO_LANG)); return }
        Lang.ru = resources.configuration.locales[0].language == "ru"
        if (BuildConfig.DEMO) {
            container.scope.launch { app.kitchenwitch.data.DemoSeed.seedIfEmpty(container) }
            Start.tab = intent.getIntExtra("tab", 0); Start.sheet = intent.getStringExtra("sheet"); Start.cook = intent.getStringExtra("cook")
        }
        shared = sharedText(intent)
        val t = android.graphics.Color.TRANSPARENT
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(t), navigationBarStyle = SystemBarStyle.dark(t))
        setContent {
            val settings: Settings? by container.settings.settings.collectAsStateWithLifecycle(initialValue = null)
            WitchTheme {
                settings?.let { s -> if (s.onboarded) Root(s, shared) else Onboarding() }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        sharedText(intent)?.let { shared = it }
    }

    private fun sharedText(i: Intent?): String? = if (i?.action == Intent.ACTION_SEND) i.getStringExtra(Intent.EXTRA_TEXT) else null
}

/** Switches the app language: "" follows the phone, "ru" or "en" pins it. The activity restarts in the new language. */
fun setLanguage(tag: String) = AppCompatDelegate.setApplicationLocales(if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag))
fun currentLanguage(): String = AppCompatDelegate.getApplicationLocales().toLanguageTags().substringBefore('-')
