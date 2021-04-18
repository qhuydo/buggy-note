package com.hcmus.clc18se.buggynote.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hcmus.clc18se.buggynote.data.Note
import com.hcmus.clc18se.buggynote.data.NoteCrossRef
import com.hcmus.clc18se.buggynote.data.Tag

@Database(entities = [Note::class, Tag::class, NoteCrossRef::class], version = 7)
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
                            .addMigrations(MIGRATE_2_3)
                            .addMigrations(MIGRATE_3_4)
                            .addMigrations(MIGRATE_4_5)
                            .addMigrations(MIGRATE_5_6)
                            .addMigrations(MIGRATE_6_7)
                            .fallbackToDestructiveMigration()
                            .build()
                    INSTANCE = instance

                }
                return instance
            }
        }

        private val MIGRATE_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_note_note_id` ON `note` (`note_id`)")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `NoteCrossRef_new` (`note_id` INTEGER NOT NULL, 
                        `tag_id` INTEGER NOT NULL, 
                        PRIMARY KEY(`note_id`, `tag_id`), 
                        FOREIGN KEY(`note_id`) REFERENCES `note`(`note_id`) ON UPDATE NO ACTION ON DELETE CASCADE , 
                        FOREIGN KEY(`tag_id`) REFERENCES `tag`(`tag_id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                     )
                """.trimIndent())

                database.execSQL("INSERT OR IGNORE INTO NoteCrossRef_new SELECT * FROM NoteCrossRef")
                database.execSQL("DROP TABLE NoteCrossRef")

                database.execSQL("ALTER TABLE NoteCrossRef_new RENAME TO NoteCrossRef")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_NoteCrossRef_tag_id` ON `NoteCrossRef` (`tag_id`)")
            }
        }

        private val MIGRATE_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // `title_format` TEXT NOT NULL DEFAULT '8388611|0|0', `content_format` TEXT NOT NULL DEFAULT '8388611|0|0'
                database.execSQL("ALTER TABLE note ADD COLUMN `title_format` TEXT NOT NULL DEFAULT '8388611|0|0'")
                database.execSQL("ALTER TABLE note ADD COLUMN `content_format` TEXT NOT NULL DEFAULT '8388611|0|0'")
            }
        }

        private val MIGRATE_4_5 = object : Migration(4, 5)  {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS `index_note_note_id`")
                database.execSQL("ALTER TABLE note ADD COLUMN  `order` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_note_note_id` ON `note` (`note_id`)")
            }
        }

        private val MIGRATE_5_6 = object: Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE note ADD COLUMN `is_pinned` INTEGER NOT NULL DEFAULT 0")
            }
        }
        private val MIGRATE_6_7 = object: Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE note ADD COLUMN `is_archived` INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

}