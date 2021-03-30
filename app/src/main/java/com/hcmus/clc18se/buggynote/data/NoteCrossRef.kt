package com.hcmus.clc18se.buggynote.data

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["note_id", "tag_id"])
data class NoteCrossRef(
        @ColumnInfo(name = "note_id")
        val noteId: Long,
        @ColumnInfo(name = "tag_id", index = true)
        val tagId: Long
)
