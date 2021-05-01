package com.hcmus.clc18se.buggynote.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.navigation.navGraphViewModels
import com.hcmus.clc18se.buggynote.R
import com.hcmus.clc18se.buggynote.adapters.TagSelectionAdapterCallbacks
import com.hcmus.clc18se.buggynote.adapters.TagSelectionAdapter
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabase
import com.hcmus.clc18se.buggynote.databinding.FragmentSelectTagBinding
import com.hcmus.clc18se.buggynote.viewmodels.NoteDetailsViewModel
import com.hcmus.clc18se.buggynote.viewmodels.NoteDetailsViewModelFactory
import com.hcmus.clc18se.buggynote.viewmodels.TagSelectionViewModel
import com.hcmus.clc18se.buggynote.viewmodels.TagSelectionViewModelFactory

class TagSelectionFragment : BaseFragment() {

    private lateinit var binding: FragmentSelectTagBinding

    private val onCheckedChangedListener = TagSelectionAdapterCallbacks { _, isChecked, tag ->
        if (tag.selectState == isChecked) {
            return@TagSelectionAdapterCallbacks
        }
        tag.selectState = isChecked

        if (isChecked) {
            tagSelectionViewModel.addSelectedTags(tag)
        } else {
            tagSelectionViewModel.removeSelectedTags(tag)
        }
    }

    private val db by lazy { BuggyNoteDatabase.getInstance(requireActivity()).buggyNoteDatabaseDao }

    private val arguments by lazy { TagSelectionFragmentArgs.fromBundle(requireArguments()) }

    private val tagSelectionViewModel: TagSelectionViewModel by viewModels {
        TagSelectionViewModelFactory(
                arguments.noteId,
                db
        )
    }

    private val noteDetailViewModel: NoteDetailsViewModel by navGraphViewModels(R.id.navigation_note_details) {
        NoteDetailsViewModelFactory(arguments.noteId, db)
    }

    private lateinit var adapter: TagSelectionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentSelectTagBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = this@TagSelectionFragment
            viewModel = tagSelectionViewModel
        }

        tagSelectionViewModel.changesOccurred.observe(viewLifecycleOwner) {
            if (it) {
                noteDetailViewModel.requestReloadingData()
            }
        }

        adapter = TagSelectionAdapter(onCheckedChangedListener)
        binding.tagList.adapter = adapter

        return binding.root
    }


    override fun getToolbarView(): Toolbar = binding.appBar.toolbar
}