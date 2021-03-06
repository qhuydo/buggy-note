package com.hcmus.clc18se.buggynote.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hcmus.clc18se.buggynote.data.Note
import com.hcmus.clc18se.buggynote.data.NoteCrossRef
import com.hcmus.clc18se.buggynote.data.NoteWithTags
import com.hcmus.clc18se.buggynote.data.Tag

private const val DEFAULT_SORT_ORDER = "is_pinned desc,`order` asc, note_id desc"
private const val DEFAULT_CONDITION = "1"

@Dao
interface BuggyNoteDatabaseDao {

    @Transaction
    @Query("select * from note where $DEFAULT_CONDITION order by $DEFAULT_SORT_ORDER")
    suspend fun getAllNoteWithTags(): List<NoteWithTags>

    @Transaction
    @Query("select * from note where note_id = :id")
    fun getNoteFromId(id: Long): LiveData<NoteWithTags>

    @Transaction
    @Delete
    suspend fun removeNote(vararg note: Note): Int

    @Update
    suspend fun updateNote(vararg note: Note): Int

    @Insert
    suspend fun addNewNote(note: Note): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addNoteCrossRef(vararg noteCrossRef: NoteCrossRef)

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

    @Transaction
    @Query(
        "select * from note where note_id in (" +
                "select note_id from notecrossref where tag_id in (:tagIds)" +
                ") and $DEFAULT_CONDITION order by $DEFAULT_SORT_ORDER"
    )
    suspend fun filterNoteByTagList(tagIds: List<Long>): List<NoteWithTags>

    @Transaction
    @Query(
        "select * from note where " +
                "(note_content like '%' || :keyword || '%' or title like '%' || :keyword || '%') " +
                "and (note_id in (select note_id from notecrossref where tag_id in (:tagIds))) " +
                "and $DEFAULT_CONDITION order by $DEFAULT_SORT_ORDER"
    )
    suspend fun filterNoteByKeyWordAndTags(keyword: String, tagIds: List<Long>): List<NoteWithTags>

    @Transaction
    @Query(
        "select * from note where " +
                "(note_content like '%' || :keyword || '%' or title like '%' || :keyword || '%') " +
                "and $DEFAULT_CONDITION order by $DEFAULT_SORT_ORDER"
    )
    suspend fun filterNoteByKeyWord(keyword: String): List<NoteWithTags>

    @Query("select count(*) from notecrossref where tag_id = :tagId")
    suspend fun isTagExistedInTheNoteList(tagId: Long): Boolean
}