package com.hcmus.clc18se.buggynote.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.hcmus.clc18se.buggynote.BuggyNoteActivity
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabase
import com.hcmus.clc18se.buggynote.databinding.FragmentNoteDetailsBinding
import com.hcmus.clc18se.buggynote.viewmodels.NoteDetailsViewModel
import com.hcmus.clc18se.buggynote.viewmodels.NoteDetailsViewModelFactory

class NoteDetailsFragment : Fragment() {

    private lateinit var binding: FragmentNoteDetailsBinding

    private val arguments by lazy {
        NoteDetailsFragmentArgs.fromBundle(requireArguments())
    }

    private val viewModel: NoteDetailsViewModel by viewModels {
        NoteDetailsViewModelFactory(
            arguments.noteId,
            BuggyNoteDatabase.getInstance(requireActivity()).buggyNoteDatabaseDao
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteDetailsBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = this@NoteDetailsFragment
            noteDetailsViewModel = viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpNavigation()
    }

    private fun setUpNavigation() {
        val toolbar = binding.appBar.toolbar
        val parentActivity: BuggyNoteActivity = requireActivity() as BuggyNoteActivity

        parentActivity.setSupportActionBar(toolbar)
        parentActivity.setupActionBarWithNavController(
            findNavController(),
            parentActivity.appBarConfiguration
        )
    }
}