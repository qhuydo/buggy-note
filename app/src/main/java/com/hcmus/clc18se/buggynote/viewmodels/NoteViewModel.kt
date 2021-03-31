package com.hcmus.clc18se.buggynote.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.hcmus.clc18se.buggynote.data.Note
import com.hcmus.clc18se.buggynote.data.NoteWithTags
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabaseDao
import kotlinx.coroutines.launch

class NoteViewModel(
        application: Application,
        private val database: BuggyNoteDatabaseDao
) : AndroidViewModel(application) {

    private var _noteList = MutableLiveData<List<NoteWithTags>>()
    val noteList: LiveData<List<NoteWithTags>>
        get() = _noteList

    private var _idx = MutableLiveData(-1)
    val idx: LiveData<Int>
        get() = _idx

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            loadNoteFromDatabase()
        }
    }

    private suspend fun loadNoteFromDatabase() {
        _noteList.value = database.getAllNoteWithTags()
    }

    fun insertNewNote(note: Note) {
        viewModelScope.launch {
            database.addNewNote(note)
            loadNoteFromDatabase()
        }
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

