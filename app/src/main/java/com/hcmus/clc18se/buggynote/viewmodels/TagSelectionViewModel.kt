package com.hcmus.clc18se.buggynote.viewmodels

import androidx.lifecycle.*
import com.hcmus.clc18se.buggynote.data.NoteCrossRef
import com.hcmus.clc18se.buggynote.data.Tag
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabaseDao
import kotlinx.coroutines.launch
import timber.log.Timber

class TagSelectionViewModel(
    private val noteId: Long = 0L,
    dataSource: BuggyNoteDatabaseDao
) : ViewModel() {

    private val database = dataSource

    private var _tags = MutableLiveData<List<Tag>>()
    val tags: LiveData<List<Tag>>
        get() = _tags

    init {
        loadTags()
    }

    private fun loadTags() {
        viewModelScope.launch {
            loadTagFromDatabase()
        }
        //loadSelectedTags(noteId)
    }

    fun addSelectedTags(tag: Tag) {
        viewModelScope.launch {
            Timber.d("Add new column in notecrossref - (note_id, tag_id)=($noteId, ${tag.id})")
            database.addNoteCrossRef(NoteCrossRef(noteId, tag.id))
        }
    }

    fun removeSelectedTags(tag: Tag) {
        viewModelScope.launch {
            Timber.d("Remove column in notecrossref - (note_id, tag_id)=($noteId, ${tag.id})")
            database.deleteNoteCrossRef(NoteCrossRef(noteId, tag.id))
        }
    }

    private suspend fun loadTagFromDatabase() {
        _tags.value = database.getAllTagWithSelectedState(noteId)
    }

}

@Suppress("UNCHECKED_CAST")
class TagSelectionViewModelFactory(
    val id: Long,
    val database: BuggyNoteDatabaseDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TagSelectionViewModel::class.java)) {
            return TagSelectionViewModel(id, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
