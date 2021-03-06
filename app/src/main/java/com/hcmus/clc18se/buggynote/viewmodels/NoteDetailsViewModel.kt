package com.hcmus.clc18se.buggynote.viewmodels

import androidx.lifecycle.*
import com.hcmus.clc18se.buggynote.data.NoteWithTags
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabaseDao
import com.hcmus.clc18se.buggynote.utils.TextFormatter
import kotlinx.coroutines.launch
import timber.log.Timber

class NoteDetailsViewModel(
        private val noteId: Long = 0L,
        dataSource: BuggyNoteDatabaseDao
) : ViewModel() {

    private val database = dataSource

    private var noteWithTags: LiveData<NoteWithTags> = database.getNoteFromId(noteId)
    fun getNoteWithTags() = noteWithTags

    private var _reloadDataRequest = MutableLiveData(false)
    val reloadDataRequest: LiveData<Boolean>
        get() = _reloadDataRequest

    private var _navigateToTagSelection = MutableLiveData<Long?>(null)
    val navigateToTagSelection: LiveData<Long?>
        get() = _navigateToTagSelection

    private var _deleteRequest = MutableLiveData(false)
    val deleteRequest: LiveData<Boolean>
        get() = _deleteRequest

    init {
        viewModelScope.launch {
            Timber.d("nNoteCrossRef column ${database.getNoteCrossRef(noteId).size}")
        }
    }

    fun reloadNote() {
        Timber.d("ping")
        noteWithTags = database.getNoteFromId(noteId)
    }

    fun requestReloadingData() {
        _reloadDataRequest.value = true
    }

    // TODO: get it a better name
    fun doneRequestingLoadData() {
        _reloadDataRequest.value = false
    }

    fun navigateToTagSelection() {
        _navigateToTagSelection.postValue(noteId)
    }

    fun doneNavigatingToTagSelection() {
        _navigateToTagSelection.value = null
    }

    fun deleteMe() {
        viewModelScope.launch {
            noteWithTags.value?.let {
                val nCol = database.removeNote(it.note)
                _deleteRequest.value = true
                Timber.d("Remove note - $nCol affected")
            }
        }
    }

    fun togglePin() {
        noteWithTags.value?.let {
            it.note.isPinned = it.note.isPinned.not()
        }
    }

    fun setNoteTitleFormat(formatter: TextFormatter) {
        viewModelScope.launch {

            Timber.d("ping")

            noteWithTags.value?.let {
                it.note.titleFormat = formatter.toString()
                // database.updateNote(it.note)
            }
        }
    }

    fun setNoteContentFormat(formatter: TextFormatter) {
        viewModelScope.launch {
            noteWithTags.value?.let {

                Timber.d("ping")

                it.note.contentFormat = formatter.toString()
                // database.updateNote(it.note)
            }
        }
    }

    fun doneHandlingDeleteRequest() {
        _deleteRequest.value = false
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

