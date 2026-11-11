package app.kitchenwitch.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * A written recipe: a scroll until it is first cooked, a spell after. [ingredients] and [steps] are one per line.
 * [source]: lib (copied from the library, [libId]), link ([url]), page (a photo of a page, [page] is the file),
 * own (written by hand), memory (written after cooking).
 */
@Entity
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val origin: String = "",
    val minutes: Int = 0,
    val ingredients: String = "",
    val steps: String = "",
    val source: String = "own",
    val libId: String = "",
    val url: String = "",
    val page: String = "",
    val created: Long = 0,
    val scroll: Boolean = true,      // still on the scroll list (not hidden by the witch)
)

/**
 * One time a recipe was cooked. Snapshots of the title and reagents keep the book right if the recipe changes.
 * [verdict]: ok · meh · miss. [age] is the moon's age then, [cycle] its number.
 */
@Entity
data class Cast(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val title: String,
    val reagents: String,            // comma-separated, what the sigil is drawn from
    val at: Long,
    val day: Long,
    val age: Double,
    val cycle: Long,
    val intent: String = "",
    val verdict: String = "ok",
    val note: String = "",
    val photo: String = "",          // file name in files/plates
    val guests: String = "",         // names, comma-separated
    val weather: String = "",        // Weather name, "" unknown
    val temp: Double = Double.NaN,
    val sabbat: String = "",
    val marks: String = "",          // mark ids earned by this cast
    val jars: String = "",           // jar ids this cast put on the shelf
    val spell: Boolean = false,      // it was the spell set for that moon
)

/** Something left over in the pantry, waiting to be used up. */
@Entity
data class Leftover(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val day: Long)

@Dao
interface WitchDao {
    @Query("SELECT * FROM Recipe ORDER BY created DESC") fun recipes(): Flow<List<Recipe>>
    @Query("SELECT * FROM Recipe ORDER BY created DESC") suspend fun recipeList(): List<Recipe>
    @Query("SELECT * FROM Recipe WHERE id = :id") suspend fun recipe(id: Long): Recipe?
    @Query("SELECT * FROM Recipe WHERE libId = :lib LIMIT 1") suspend fun byLib(lib: String): Recipe?
    @Query("SELECT * FROM `Cast` ORDER BY at DESC") fun casts(): Flow<List<Cast>>
    @Query("SELECT * FROM `Cast` ORDER BY at DESC") suspend fun castList(): List<Cast>
    @Query("SELECT * FROM Leftover ORDER BY day DESC") fun leftovers(): Flow<List<Leftover>>
    @Query("SELECT * FROM Leftover ORDER BY day DESC") suspend fun leftoverList(): List<Leftover>
    @Insert suspend fun insert(r: Recipe): Long
    @Update suspend fun update(r: Recipe)
    @Insert suspend fun insert(c: Cast): Long
    @Update suspend fun update(c: Cast)
    @Insert suspend fun insert(l: Leftover): Long
    @Query("DELETE FROM Leftover WHERE id = :id") suspend fun deleteLeftover(id: Long)
    @Query("DELETE FROM Recipe WHERE id = :id") suspend fun deleteRecipe(id: Long)
    @Query("DELETE FROM `Cast` WHERE id = :id") suspend fun deleteCast(id: Long)
    @Query("DELETE FROM Recipe") suspend fun clearRecipes()
    @Query("DELETE FROM `Cast`") suspend fun clearCasts()
    @Query("DELETE FROM Leftover") suspend fun clearLeftovers()
}

@Database(entities = [Recipe::class, Cast::class, Leftover::class], version = 1, exportSchema = true)
abstract class WitchDb : RoomDatabase() {
    abstract fun dao(): WitchDao

    companion object {
        fun create(ctx: Context) = Room.databaseBuilder(ctx, WitchDb::class.java, "kitchenwitch.db").build()
    }
}
