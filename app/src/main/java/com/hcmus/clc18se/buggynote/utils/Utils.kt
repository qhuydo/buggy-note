package com.hcmus.clc18se.buggynote.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.hcmus.clc18se.buggynote.R
import java.text.SimpleDateFormat
import java.util.*

fun convertLongToDateString(systemTime: Long): String {
    return SimpleDateFormat("EEEE MMM-dd-yyyy HH:mm", Locale.getDefault())
            .format(systemTime).toString()
}

fun Context.getSpanCountForNoteList(preferences: SharedPreferences): Int {
    val list = "1"

    val noteListDisplayType = preferences.getString(getString(R.string.note_list_view_type_key), "0")
    return when (noteListDisplayType) {
        list -> resources.getInteger(R.integer.note_item_span_count_list)
        else -> resources.getInteger(R.integer.note_item_span_count_grid)
    }
}