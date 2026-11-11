package app.kitchenwitch.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

/** Everything that isn't a recipe or a cast: the shelf, the moon's spell, the bones, the town for the window. */
data class Settings(
    val onboarded: Boolean = false,
    val jars: Set<String> = emptySet(),          // jar ids on the shelf
    val spell: Long = 0,                         // recipe set for this moon
    val spellCycle: Long = -1,                   // the moon it was set for
    val rollDay: Long = 0, val rolls: Int = 0,   // bones thrown today
    val marks: Map<String, Long> = emptyMap(),   // witch's marks → epoch day earned
    val town: String = "", val lat: Double = Double.NaN, val lon: Double = Double.NaN,
    val outside: String = "",                    // last weather, as Outside.json()
    val chime: Boolean = true,                   // a bell when a candle burns down
    val guessed: Boolean = false,                // the town was guessed from the time zone once
    val seen: Set<String> = emptySet(),          // screens whose first-visit hint was read
) {
    fun json(): JSONObject = JSONObject().apply {
        put("onboarded", onboarded); put("jars", JSONArray(jars.toList())); put("spell", spell); put("spellCycle", spellCycle)
        put("rollDay", rollDay); put("rolls", rolls); put("marks", JSONObject().apply { marks.forEach { (k, v) -> put(k, v) } })
        put("town", town); if (!lat.isNaN()) { put("lat", lat); put("lon", lon) }; put("outside", outside); put("chime", chime); put("guessed", guessed); put("seen", JSONArray(seen.toList()))
    }

    companion object {
        fun of(j: JSONObject): Settings {
            fun set(k: String) = j.optJSONArray(k)?.let { a -> (0 until a.length()).map { a.getString(it) }.toSet() } ?: emptySet()
            return Settings(j.optBoolean("onboarded"), set("jars"), j.optLong("spell"), j.optLong("spellCycle", -1), j.optLong("rollDay"), j.optInt("rolls"),
                j.optJSONObject("marks")?.let { o -> o.keys().asSequence().associateWith { o.getLong(it) } } ?: emptyMap(),
                j.optString("town"), j.optDouble("lat", Double.NaN), j.optDouble("lon", Double.NaN), j.optString("outside"),
                j.optBoolean("chime", true), j.optBoolean("guessed"), set("seen"))
        }
    }
}

private val Context.store by preferencesDataStore("kitchenwitch")
private val KEY = stringPreferencesKey("settings")

class SettingsStore(private val ctx: Context) {
    val settings: Flow<Settings> = ctx.store.data.map { p -> p[KEY]?.let { runCatching { Settings.of(JSONObject(it)) }.getOrNull() } ?: Settings() }
    suspend fun now(): Settings = settings.first()
    suspend fun update(f: (Settings) -> Settings) { ctx.store.edit { p ->
        val cur = p[KEY]?.let { runCatching { Settings.of(JSONObject(it)) }.getOrNull() } ?: Settings()
        p[KEY] = f(cur).json().toString() } }
}
