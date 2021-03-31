package com.hcmus.clc18se.buggynote.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.hcmus.clc18se.buggynote.data.Note
import com.hcmus.clc18se.buggynote.data.NoteWithTags

@Dao
interface BuggyNoteDatabaseDao {

    @Transaction
    @Query("select * from note")
    suspend fun getAllNoteWithTags(): List<NoteWithTags>

    @Insert
    suspend fun addNewNote(vararg note: Note)

}