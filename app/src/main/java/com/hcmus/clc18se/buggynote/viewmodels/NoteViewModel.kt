package com.hcmus.clc18se.buggynote.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.hcmus.clc18se.buggynote.data.Note
import com.hcmus.clc18se.buggynote.data.NoteCrossRef
import com.hcmus.clc18se.buggynote.data.NoteWithTags
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabaseDao
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class NoteViewModel(
        application: Application,
        private val database: BuggyNoteDatabaseDao
) : AndroidViewModel(application) {

    private var _noteList = MutableLiveData<List<NoteWithTags>>()
    val noteList: LiveData<List<NoteWithTags>>
        get() = _noteList

    private var _navigateToNoteDetails = MutableLiveData<Long?>()
    val navigateToNoteDetails: LiveData<Long?>
        get() = _navigateToNoteDetails

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            loadNoteFromDatabase()
        }
    }

    private suspend fun loadNoteFromDatabase() {
        _noteList.value = database.getAllNoteWithTags()
    }

    suspend fun insertNewNote(note: Note): Long {

        val insertedId = database.addNewNote(note)
        loadNoteFromDatabase()

        return insertedId
    }

    fun navigateToNoteDetails(id: Long) {
        _navigateToNoteDetails.value = id
    }

    fun doneNavigatingToNoteDetails() {
        _navigateToNoteDetails.value = null
    }

}

@Suppress("UNCHECKED_CAST")
class NoteViewModelFactory(
        val application: Application,
        val database: BuggyNoteDatabaseDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            return NoteViewModel(application, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

