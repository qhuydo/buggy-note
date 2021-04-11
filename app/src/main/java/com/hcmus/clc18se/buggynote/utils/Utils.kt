package com.hcmus.clc18se.buggynote.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hcmus.clc18se.buggynote.R
import java.text.SimpleDateFormat
import java.util.*

fun convertLongToDateString(systemTime: Long): String {
    return SimpleDateFormat("EEEE MMM-dd-yyyy HH:mm", Locale.getDefault())
            .format(systemTime).toString()
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
    when (preferences.getString(context.getString(R.string.note_list_view_type_key), "0")) {
        list -> {
            layoutManager = GridLayoutManager(context,
                    context.getSpanCountForNoteList(preferences))
        }
        else -> {
            layoutManager = StaggeredGridLayoutManager(
                    context.getSpanCountForNoteList(preferences), StaggeredGridLayoutManager.VERTICAL

            )
        }
    }
}