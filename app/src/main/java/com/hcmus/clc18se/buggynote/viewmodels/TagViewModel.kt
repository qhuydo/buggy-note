package com.hcmus.clc18se.buggynote.viewmodels

import androidx.lifecycle.*
import com.hcmus.clc18se.buggynote.data.Tag
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabaseDao
import kotlinx.coroutines.*

class TagViewModel(val database: BuggyNoteDatabaseDao) : ViewModel() {

    private var _tags = MutableLiveData<List<Tag>>()
    val tags: LiveData<List<Tag>>
        get() = _tags

    init {
        loadTags()
    }

    fun loadTags() {
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
        database.insert(tag)
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