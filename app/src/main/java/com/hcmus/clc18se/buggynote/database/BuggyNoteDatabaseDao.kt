package com.hcmus.clc18se.buggynote.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hcmus.clc18se.buggynote.data.Note
import com.hcmus.clc18se.buggynote.data.NoteCrossRef
import com.hcmus.clc18se.buggynote.data.NoteWithTags
import com.hcmus.clc18se.buggynote.data.Tag

@Dao
interface BuggyNoteDatabaseDao {

    @Transaction
    @Query("select * from note order by note_id desc")
    suspend fun getAllNoteWithTags(): List<NoteWithTags>

    @Transaction
    @Query("select * from note where note_id = :id")
    fun getNoteFromId(id: Long): LiveData<NoteWithTags>

    @Update
    suspend fun updateNote(note: Note)

    @Insert
    suspend fun addNewNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addNoteCrossRef(vararg noteCrossRef: NoteCrossRef)

    @Transaction
    suspend fun getAllTagWithSelectedState(noteId: Long): List<Tag> {
        val tags = getAllTags()
        tags.forEach { it.selectState = containsNoteCrossRef(noteId, it.id) }
        return tags
    }

    @Query("select count(*) from notecrossref where note_id = :noteId and tag_id = :tagId")
    suspend fun containsNoteCrossRef(noteId: Long, tagId: Long): Boolean

    @Delete
    suspend fun deleteNoteCrossRef(vararg noteCrossRef: NoteCrossRef)

    @Query("select * from notecrossref where note_id = :noteId")
    suspend fun getNoteCrossRef(noteId: Long): List<NoteCrossRef>

    @Query("select * from tag order by name asc")
    suspend fun getAllTags(): List<Tag>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tag)

    @Update
    suspend fun updateTag(tag: Tag)

    @Transaction
    @Delete
    suspend fun deleteTag(tag: Tag): Int

    @Query("select count(*) from tag where name = :content")
    suspend fun containsTag(content: String): Boolean
}