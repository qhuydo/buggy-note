package com.hcmus.clc18se.buggynote.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.hcmus.clc18se.buggynote.data.Note
import com.hcmus.clc18se.buggynote.data.NoteWithTags

@Dao
interface BuggyNoteDatabaseDao {

    @Transaction
    @Query("select * from note order by last_modify desc ")
    suspend fun getAllNoteWithTags(): List<NoteWithTags>

    @Transaction
    @Query("select * from note where note_id = :id")
    fun getNoteFromId(id: Long): LiveData<NoteWithTags>

    @Insert
    suspend fun addNewNote(vararg note: Note)

}