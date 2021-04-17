package com.hcmus.clc18se.buggynote.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hcmus.clc18se.buggynote.R

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


fun Context.getSpanCountForNoteList(preferences: SharedPreferences): Int {
    val list = "1"

    return when (preferences.getString(getString(R.string.note_list_view_type_key), "0")) {
        list -> resources.getInteger(R.integer.note_item_span_count_list)
        else -> resources.getInteger(R.integer.note_item_span_count_grid)
    }
}

fun RecyclerView.setUpLayoutManagerForNoteList(preferences: SharedPreferences) {
    val list = "1"
    layoutManager = when (preferences.getString(context.getString(R.string.note_list_view_type_key), "0")) {
        list -> {
            GridLayoutManager(context,
                context.getSpanCountForNoteList(preferences))
        }
        else -> {
            StaggeredGridLayoutManager(
                context.getSpanCountForNoteList(preferences), StaggeredGridLayoutManager.VERTICAL

            )
        }
    }
}