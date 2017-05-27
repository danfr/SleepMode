package carnaude.sleepmode


import android.content.Context
import android.graphics.Typeface
import java.util.*

/**
 * Created by norman on 3/8/15.
 *
 *
 * Code taken from britzl on StackOverflow (slightly modified):
 * http://stackoverflow.com/questions/16901930/memory-leaks-with-custom-font-for-set-custom-font/16902532#16902532
 */
object FontCache {

    private val fontCache = HashMap<String, Typeface>()

    fun getTypeface(fontname: String, context: Context): Typeface? {
        var typeface: Typeface? = fontCache[fontname]

        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.assets, fontname)
            } catch (e: Exception) {
                return null
            }

            fontCache.put(fontname, typeface)
        }

        return typeface
    }
}