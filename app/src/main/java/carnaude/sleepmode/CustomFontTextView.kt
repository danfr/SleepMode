package carnaude.sleepmode

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet

class CustomFontTextView : android.support.v7.widget.AppCompatTextView {

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        applyCustomFont(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {

        applyCustomFont(context, attrs)
    }

    private fun applyCustomFont(context: Context, attrs: AttributeSet) {
        val attributeArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.DigitsFontTextView)

        val fontName = attributeArray.getString(R.styleable.DigitsFontTextView_font)
        val textStyle = attrs.getAttributeIntValue(ANDROID_SCHEMA, "textStyle", Typeface.NORMAL)

        val customFont = selectTypeface(context, fontName, textStyle)
        typeface = customFont

        attributeArray.recycle()
    }

    private fun selectTypeface(context: Context, fontName: String, textStyle: Int): Typeface? {
        if (fontName == context.getString(R.string.digit_font_mono)) {
            /*
              information about the TextView textStyle:
              http://developer.android.com/reference/android/R.styleable.html#TextView_textStyle
              */
            when (textStyle) {
                Typeface.ITALIC -> return FontCache.getTypeface("digital-7-mono-italic.ttf", context)

                else -> return FontCache.getTypeface("digital-7-mono.ttf", context)
            }
        } else {
            // no matching font found
            // return null so Android just uses the standard font (Roboto)
            return null
        }
    }

    companion object {

        val ANDROID_SCHEMA = "http://schemas.android.com/apk/res/android"
    }
}
