package com.hcmus.clc18se.buggynote.utils

import android.graphics.Typeface
import android.view.Gravity
import timber.log.Timber

data class TextFormatter(var gravity: Int = Gravity.START,
                         var typefaceStyle: Int = Typeface.NORMAL,
                         var typefaceType: Int = TYPEFACE_REGULAR) {
    fun toggleBold() {
        typefaceStyle = typefaceStyle xor Typeface.BOLD
    }

    fun toggleItalic() {
        typefaceStyle = typefaceStyle xor Typeface.ITALIC
    }

    fun toggleAlignment() {
        gravity = when {
            (gravity and Gravity.START) == Gravity.START -> {
                Gravity.CENTER
            }
            (gravity and Gravity.CENTER) == Gravity.CENTER -> {
                Gravity.END
            }
            else -> {
                Gravity.START
            }
        }
    }

    override fun toString(): String {
        return "${gravity}|${typefaceStyle}|${typefaceType}"
    }

    fun toggleFontType() {
        typefaceType = when (typefaceType) {
            TYPEFACE_SERIF -> TYPEFACE_MONOSPACE
            TYPEFACE_MONOSPACE -> TYPEFACE_REGULAR
            else -> TYPEFACE_SERIF
        }
    }

    /**
     *
     */
    companion object {
        private const val DELIM = "|"

        /**
         * format "gravity|typeface|isAllCaps"
         */
        fun parseFormat(format: String): TextFormatter {
            return try {
                val ints = format.split(DELIM).map { it.toInt() }
                TextFormatter(gravity = ints[0], typefaceStyle = ints[1], typefaceType = ints[2])
            } catch (ex: Exception) {
                Timber.d("$ex")
                TextFormatter()
            }
        }

        // Gravity.START|Typeface.NORMAL|TypeFace.Regular
        const val DEFAULT_FORMAT_STRING = "8388611|0|0"

        const val TYPEFACE_REGULAR = 0
        const val TYPEFACE_SANS_SERIF = 1
        const val TYPEFACE_SERIF = 2
        const val TYPEFACE_MONOSPACE = 4
    }

}