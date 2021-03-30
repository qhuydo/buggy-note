package com.hcmus.clc18se.buggynote.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "note")
data class Note(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "note_id")
        val id: Long,

        var title: String?,

        @ColumnInfo(name = "note_content")
        var noteContent: String?,

        @ColumnInfo(name = "last_modify")
        var lastModify: Long?
)