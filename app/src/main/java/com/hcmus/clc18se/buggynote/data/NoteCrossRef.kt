package com.hcmus.clc18se.buggynote.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(primaryKeys = ["note_id", "tag_id"],
        foreignKeys = [
                ForeignKey(entity = Note::class,
                        parentColumns = ["note_id"],
                        childColumns = ["note_id"],
                        onDelete = ForeignKey.CASCADE),
                ForeignKey(entity = Tag::class,
                        parentColumns = ["tag_id"],
                        childColumns = ["tag_id"],
                        onDelete = ForeignKey.CASCADE
                )
        ])
data class NoteCrossRef(
        @ColumnInfo(name = "note_id")
        val noteId: Long,

        @ColumnInfo(name = "tag_id", index = true)
        val tagId: Long
)
