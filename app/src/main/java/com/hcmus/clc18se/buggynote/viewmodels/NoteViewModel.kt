package com.hcmus.clc18se.buggynote.viewmodels

import android.app.Application
import android.view.View
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

    val unpinnedNotes = Transformations.map(_noteList) {
        it.filter { noteWithTags -> !noteWithTags.isPinned() && !noteWithTags.isArchived() }
    }
    val pinnedNotes = Transformations.map(_noteList) {
        it.filter { noteWithTags -> noteWithTags.isPinned() && !noteWithTags.isArchived() }
    }
    val archivedNotes = Transformations.map(_noteList) {
        it.filter { noteWithTags -> noteWithTags.isArchived() }
            .sortedBy { noteWithTags -> noteWithTags.note.order }
    }

    val headerLabelVisibility = Transformations.map(pinnedNotes) { pinnedNotes ->
        if (pinnedNotes.isEmpty()) View.GONE else View.VISIBLE
    }

    private var _noteListVisibility = MutableLiveData(View.GONE)
    val noteListVisibility: LiveData<Int>
        get() = _noteListVisibility

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
        _noteList.observeForever { noteList ->
            viewModelScope.launch {
                if (noteList != null) {
                    val visibility = if (
                        noteList.isNotEmpty() &&
                        noteList.any { noteWithTags -> !noteWithTags.isArchived() }
                    ) View.GONE
                    else View.VISIBLE
                    _noteListVisibility.postValue(visibility)
                }
            }
        }
    }

    fun loadNotes() {
        viewModelScope.launch {
            loadNoteFromDatabase()
        }
    }

    suspend fun loadNoteFromDatabase() {
        _noteList.value = database.getAllNoteWithTags()
    }

    suspend fun insertNewNote(note: Note): Long {

        val insertedId = database.addNewNote(note)
        loadNoteFromDatabase()

        return insertedId
    }

    fun navigateToNoteDetails(id: Long) {
        _navigateToNoteDetails.postValue(id)
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
    fun filterByTags(tags: List<Tag>) {
        viewModelScope.launch {
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

    fun reorderNotes(notes: List<NoteWithTags>) {

        viewModelScope.launch {
            Timber.d("reorderNotes")
            notes.forEachIndexed { index: Int, note: NoteWithTags -> note.note.order = index }
            val nCols = database.updateNote(*notes.map { it.note }.toTypedArray())
            Timber.d("$nCols cols affected")
        }
    }

    fun finishReordering() {
        _orderChanged.value = false
    }

    fun filterByTagsWithKeyword(tags: List<Tag>, keyword: String) {
        // TODO: refactor me
        viewModelScope.launch {
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

    suspend fun togglePin(isPinned: Boolean, vararg notes: NoteWithTags) {
        notes.forEach { note: NoteWithTags -> note.note.isPinned = isPinned }
        val nCols = database.updateNote(*notes.map { it.note }.toTypedArray())
        _reloadDataRequest.value = true
    }

    private fun updateArchiveStatus(isArchived: Boolean, vararg notes: NoteWithTags) {
        viewModelScope.launch {
            notes.forEach { note: NoteWithTags -> note.note.isArchived = isArchived }
            val nCols = database.updateNote(*notes.map { it.note }.toTypedArray())
            _reloadDataRequest.value = true
        }
    }

    fun moveToArchive(vararg notes: NoteWithTags) {
        updateArchiveStatus(true, *notes)
    }

    fun moveToNoteList(vararg notes: NoteWithTags) {
        updateArchiveStatus(false, *notes)
    }
}

@Suppress("UNCHECKED_CAST")
class NoteViewModelFactory(
    private val application: Application,
    val database: BuggyNoteDatabaseDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            return NoteViewModel(application, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}