package com.hcmus.clc18se.buggynote.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hcmus.clc18se.buggynote.data.Note
import com.hcmus.clc18se.buggynote.data.NoteCrossRef
import com.hcmus.clc18se.buggynote.data.Tag

@Database(entities = [Note::class, Tag::class, NoteCrossRef::class], version = 1, exportSchema = false)
abstract class BuggyNoteDatabase : RoomDatabase() {
    abstract val buggyNoteDatabaseDao: BuggyNoteDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: BuggyNoteDatabase? = null

        fun getInstance(context: Context): BuggyNoteDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            BuggyNoteDatabase::class.java,
                            "buggy_note_database"
                    )
                            .fallbackToDestructiveMigration()
                            .build()
                    INSTANCE = instance

                }
                return instance
            }
        }
    }
}