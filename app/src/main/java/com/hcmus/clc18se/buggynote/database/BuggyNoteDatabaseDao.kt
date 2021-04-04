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

    @Insert
    suspend fun addNoteWithTagCrossRef(vararg noteCrossRef: NoteCrossRef)

    @Query("select * from notecrossref where note_id = :noteId")
    suspend fun getNoteCrossRef(noteId: Long): List<NoteCrossRef>

    @Query("select * from tag order by name asc")
    suspend fun getAllTags(): List<Tag>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag)

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag): Int

    @Query("select count(*) from tag where name = :content")
    suspend fun containsTag(content: String): Boolean
}