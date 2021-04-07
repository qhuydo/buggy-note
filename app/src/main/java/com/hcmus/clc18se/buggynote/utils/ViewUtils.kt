package com.hcmus.clc18se.buggynote.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

fun getColorAttribute(context: Context, attribute: Int): Int {
    val typedValue = TypedValue()
    val theme: Resources.Theme = context.theme
    theme.resolveAttribute(attribute, typedValue, true)
    return typedValue.data
}

fun Menu.tintAllIcons(color: Int) {
    for (i in 0 until this.size()) {
        val item = this.getItem(i)
        item.tintMenuItemIcon(color)
    }
}

fun Drawable.tint(@ColorInt color: Int): Drawable {
    val wrapped = DrawableCompat.wrap(this)
    DrawableCompat.setTint(wrapped, color)
    return wrapped
}

fun MenuItem.tintMenuItemIcon(color: Int) {
    val drawable = this.icon
    if (drawable != null) {
        this.icon = drawable.tint(color)
    }
}