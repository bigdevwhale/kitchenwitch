package app.kitchenwitch.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.exifinterface.media.ExifInterface
import app.kitchenwitch.container
import app.kitchenwitch.domain.Jars
import app.kitchenwitch.ui.art.plate
import app.kitchenwitch.ui.art.plateColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Photos of plates and pages: files in files/plates, read small and turned upright. */
object Photos {
    private val cache = object : android.util.LruCache<String, Bitmap>(24 * 1024 * 1024) { override fun sizeOf(k: String, v: Bitmap) = v.byteCount }

    fun newFile(ctx: Context, prefix: String): File = File(ctx.container.plates, "$prefix-${System.currentTimeMillis()}.jpg")

    fun load(ctx: Context, name: String, max: Int): Bitmap? {
        if (name.isEmpty()) return null
        val key = "$name@$max"; cache.get(key)?.let { return it }
        val f = File(ctx.container.plates, name); if (!f.exists() || f.length() == 0L) return null
        val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }; BitmapFactory.decodeFile(f.path, o)
        var s = 1; while (o.outWidth / (s * 2) >= max && o.outHeight / (s * 2) >= max) s *= 2
        val b = BitmapFactory.decodeFile(f.path, BitmapFactory.Options().apply { inSampleSize = s }) ?: return null
        val rot = runCatching { when (ExifInterface(f.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f; ExifInterface.ORIENTATION_ROTATE_180 -> 180f; ExifInterface.ORIENTATION_ROTATE_270 -> 270f; else -> 0f } }.getOrDefault(0f)
        val out = if (rot == 0f) b else Bitmap.createBitmap(b, 0, 0, b.width, b.height, Matrix().apply { postRotate(rot) }, true)
        cache.put(key, out); return out
    }
}

/** The plate of a cast: its photo when there is one, else a drawn plate in the dish's colours. */
@Composable
fun PlateImage(photo: String, title: String, reagents: List<String>, modifier: Modifier, max: Int = 400) {
    val ctx = LocalContext.current
    var bmp by remember(photo) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(photo) { bmp = withContext(Dispatchers.IO) { Photos.load(ctx, photo, max) } }
    val b = bmp
    if (b != null) Image(b.asImageBitmap(), null, modifier, contentScale = ContentScale.Crop)
    else { val cols = remember(title) { plateColors(title, reagents.mapNotNull { Jars.byId[it] }) }; Canvas(modifier) { plate(title, cols) } }
}
