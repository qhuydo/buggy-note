package com.hcmus.clc18se.buggynote.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hcmus.clc18se.buggynote.data.Note
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
    suspend fun addNewNote(vararg note: Note)


    @Query("select * from tag order by name asc")
    suspend fun getAllTags(): List<Tag>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: Tag)

    @Query("select count(*) from tag where name = :content")
    suspend fun containsTag(content: String): Boolean
}