package com.hcmus.clc18se.buggynote.utils

import java.text.SimpleDateFormat
import java.util.*

fun convertLongToDateString(systemTime: Long): String {
    return SimpleDateFormat("MMM-dd-yyyy HH:mm", Locale.getDefault())
            .format(systemTime).toString()
}