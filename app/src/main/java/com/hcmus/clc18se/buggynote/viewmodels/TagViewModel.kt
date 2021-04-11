package com.hcmus.clc18se.buggynote.viewmodels

import androidx.lifecycle.*
import com.hcmus.clc18se.buggynote.data.Tag
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabaseDao
import kotlinx.coroutines.*
import timber.log.Timber

class TagViewModel(val database: BuggyNoteDatabaseDao) : ViewModel() {

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
    }

    private suspend fun loadTagFromDatabase() {
        _tags.value = database.getAllTags()
    }

    private suspend fun insertTag(tagContent: String): Boolean {
        if (database.containsTag(tagContent)) {
            return false
        }
        val tag = Tag(name = tagContent)
        database.insertTag(tag)
        return true
    }

    fun addNewTag(tagContent: String): Boolean {
        var succeed: Boolean
        return runBlocking {
            async {
                succeed = insertTag(tagContent)
                if (succeed) {
                    loadTagFromDatabase()
                }
                return@async succeed
            }.await()
        }
    }

    private suspend fun updateTagFromDatabase(tag: Tag): Boolean {
        if (database.containsTag(tag.name)) {
            return false
        }
        if (tag.name.trim().isEmpty()) {
            return false
        }

        database.updateTag(tag)
        return true
    }

    fun updateTag(tag: Tag): Boolean {
        var succeed: Boolean

        return runBlocking {
            async {
                succeed = updateTagFromDatabase(tag)
                if (succeed) {
                    loadTagFromDatabase()
                }
                return@async succeed

            }.await()
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            val affectedColumn = database.deleteTag(tag)
            Timber.i("tag table - $affectedColumn column(s) affected")
            loadTagFromDatabase()
        }
    }
}

@Suppress("UNCHECKED_CAST")
class TagViewModelFactory(
        val database: BuggyNoteDatabaseDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TagViewModel::class.java)) {
            return TagViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}