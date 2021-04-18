package com.hcmus.clc18se.buggynote.data

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.hcmus.clc18se.buggynote.utils.TextFormatter
import com.hcmus.clc18se.buggynote.utils.TextFormatter.Companion.DEFAULT_FORMAT_STRING

data class NoteWithTags(
        @Embedded
        val note: Note,

        @Relation(
                parentColumn = "note_id",
                entityColumn = "tag_id",
                associateBy = Junction(NoteCrossRef::class)
        )
        val tags: List<Tag>
) {

    fun getTitle(): String {
        return note.title
    }

    fun getNoteContent(): String {
        return note.noteContent
    }

    fun getId(): Long {
        return note.id
    }

    fun getTitleFormat(): TextFormatter {
        if (note.titleFormat.isEmpty()) {
            note.titleFormat = DEFAULT_FORMAT_STRING
        }
        return TextFormatter.parseFormat(note.titleFormat)
    }

    fun getContentFormat(): TextFormatter {
        if (note.contentFormat.isEmpty()) {
            note.titleFormat = DEFAULT_FORMAT_STRING
        }
        return TextFormatter.parseFormat(note.contentFormat)
    }

    fun isPinned() = note.isPinned

    fun isArchived() = note.isArchived

    companion object {
        val DiffCallBack = object : DiffUtil.ItemCallback<NoteWithTags>() {
            override fun areContentsTheSame(oldItem: NoteWithTags, newItem: NoteWithTags): Boolean {
                return oldItem.note.id == newItem.note.id
            }

            override fun areItemsTheSame(oldItem: NoteWithTags, newItem: NoteWithTags): Boolean {
                return oldItem == newItem
            }
        }
    }
}