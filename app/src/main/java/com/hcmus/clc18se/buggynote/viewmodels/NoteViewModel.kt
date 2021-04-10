package com.hcmus.clc18se.buggynote.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.hcmus.clc18se.buggynote.data.Note
import com.hcmus.clc18se.buggynote.data.NoteWithTags
import com.hcmus.clc18se.buggynote.data.Tag
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabaseDao
import kotlinx.coroutines.launch
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

    private var _reloadDataRequest = MutableLiveData(false)
    val reloadDataRequest: LiveData<Boolean>
        get() = _reloadDataRequest

    private var _orderChanged = MutableLiveData(false)
    val orderChanged: LiveData<Boolean>
        get() = _orderChanged

    init {
        loadNotes()
    }

    fun loadNotes() {
        Timber.d("ping")
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

    fun requestReloadingData() {
        _reloadDataRequest.value = true
    }

    // TODO: get it a better name
    fun doneRequestingLoadData() {
        _reloadDataRequest.value = false
    }

    fun removeNote(vararg notes: NoteWithTags) {
        viewModelScope.launch {
            val nCol = database.removeNote(*notes.map { it.note }.toTypedArray())
            Timber.d("Remove note - $nCol affected")
            loadNoteFromDatabase()
        }
    }

    /**
     * Filter notes by a list of tags
     * the filtering will not occurred if every element of the list has selectState == true
     */
    fun filterByTagsFromDatabase(tags: List<Tag>) {
        // TODO: refactor me
        viewModelScope.launch {
            Timber.d("Ping")
            if (tags.any { it.selectState }) {
                val start = System.currentTimeMillis()

                val tagIds = tags.filter { it.selectState }.map { it.id }

                _noteList.value = database.filterNoteByTagList(tagIds)

                val end = System.currentTimeMillis()
                Timber.d("filter time: ${end - start}")
            } else {
                Timber.d("Reloading note from database")
                loadNotes()
            }
        }
    }

    suspend fun reorderNotes(notes: List<NoteWithTags>) {
        // TODO: refactor me
        // TODO: improve performance

        _orderChanged.value = true
        Timber.d("reorderNotes")
        notes.forEachIndexed { index: Int, note: NoteWithTags -> note.note.order = index }
        val nCols = database.updateNote(*notes.map { it.note }.toTypedArray())
        Timber.d("$nCols cols affected")
        loadNoteFromDatabase()
        _orderChanged.value = false
    }

    fun filterByTagsWithKeyword(tags: List<Tag>, keyword: String) {
        // TODO: refactor me
        viewModelScope.launch {
            Timber.d("Ping")
            val start = System.currentTimeMillis()

            if (tags.any { it.selectState }) {

                val tagIds = tags.filter { it.selectState }.map { it.id }
                _noteList.value = database.filterNoteByKeyWordAndTags(keyword, tagIds)

            } else {
                _noteList.value = database.filterNoteByKeyWord(keyword)
            }
            val end = System.currentTimeMillis()
            Timber.d("filter time: ${end - start}")
        }

    }

    fun requestReordering() {
        _orderChanged.value = true
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