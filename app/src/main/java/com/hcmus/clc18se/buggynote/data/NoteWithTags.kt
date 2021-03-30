package com.hcmus.clc18se.buggynote.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class NoteWithTags(
        @Embedded
        val note: Note,

        @Relation(
                parentColumn = "note_id",
                entityColumn = "tag_id",
                associateBy = Junction(NoteCrossRef::class)
        )
        val tags: List<Tag>
)