package com.hcmus.clc18se.buggynote.data

import androidx.recyclerview.widget.DiffUtil
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