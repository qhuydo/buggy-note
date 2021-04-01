package com.hcmus.clc18se.buggynote.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hcmus.clc18se.buggynote.data.NoteWithTags
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabaseDao

class NoteDetailsViewModel(
    private val noteId: Long = 0L,
    dataSource: BuggyNoteDatabaseDao
) : ViewModel() {

    val database = dataSource

    private val noteWithTags: LiveData<NoteWithTags> = database.getNoteFromId(noteId)
    fun getNoteWithTags() = noteWithTags

    private var _navigateToNoteList = MutableLiveData<Boolean?>()
    val navigateToNoteList: LiveData<Boolean?>
        get() = _navigateToNoteList

    fun navigateToNoteList() {
        _navigateToNoteList.value = true
    }

    fun doneNavigatingToNoteList() {
        _navigateToNoteList.value = null
    }
}


@Suppress("UNCHECKED_CAST")
class NoteDetailsViewModelFactory(
    val id: Long,
    val database: BuggyNoteDatabaseDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteDetailsViewModel::class.java)) {
            return NoteDetailsViewModel(id, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
