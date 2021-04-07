package com.hcmus.clc18se.buggynote.data

import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hcmus.clc18se.buggynote.utils.TextFormatter.Companion.DEFAULT_FORMAT_STRING

@Entity(tableName = "note")
data class Note(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "note_id", index = true)
        var id: Long = 0L,

        var title: String = "",

        @ColumnInfo(name = "note_content")
        var noteContent: String = "",

        @ColumnInfo(name = "last_modify")
        var lastModify: Long = System.currentTimeMillis(),

        @ColumnInfo(name = "title_format", defaultValue = DEFAULT_FORMAT_STRING)
        var titleFormat: String = DEFAULT_FORMAT_STRING,

        @ColumnInfo(name = "content_format", defaultValue = DEFAULT_FORMAT_STRING)
        var contentFormat: String = DEFAULT_FORMAT_STRING
)