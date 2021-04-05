package com.hcmus.clc18se.buggynote.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.hcmus.clc18se.buggynote.BuggyNoteActivity
import com.hcmus.clc18se.buggynote.R
import com.hcmus.clc18se.buggynote.adapters.NoteAdapter
import com.hcmus.clc18se.buggynote.adapters.OnClickListener
import com.hcmus.clc18se.buggynote.data.Note
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabase
import com.hcmus.clc18se.buggynote.databinding.FragmentNotesBinding
import com.hcmus.clc18se.buggynote.utils.SpaceItemDecoration
import com.hcmus.clc18se.buggynote.viewmodels.NoteViewModel
import com.hcmus.clc18se.buggynote.viewmodels.NoteViewModelFactory
import kotlinx.coroutines.launch

class NotesFragment : Fragment() {

    private lateinit var binding: FragmentNotesBinding

    private val viewModel: NoteViewModel by activityViewModels {
        NoteViewModelFactory(
                requireActivity().application,
                BuggyNoteDatabase.getInstance(requireActivity()).buggyNoteDatabaseDao
        )
    }

    private val adapter by lazy {
        NoteAdapter(onNoteItemClickListener)
    }

    private val onNoteItemClickListener = OnClickListener { noteWithTags ->
        viewModel.navigateToNoteDetails(noteWithTags.note.id)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        binding.fab.setOnClickListener {

            lifecycleScope.launch {
                val id = viewModel.insertNewNote(Note(title = "", noteContent = ""))
                viewModel.navigateToNoteDetails(id)
            }

        }

        binding.apply {
            lifecycleOwner = this@NotesFragment
            noteViewModel = viewModel
            noteList.adapter = adapter
            noteList.addItemDecoration(
                    SpaceItemDecoration(
                            resources.getDimension(R.dimen.item_note_margin).toInt()
                    )
            )
        }


        viewModel.noteList.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
        }

        viewModel.navigateToNoteDetails.observe(viewLifecycleOwner) {
            if (it != null) {

                findNavController().navigate(
                        NotesFragmentDirections.actionNavNotesToNoteDetailsFragment(it)
                )
                viewModel.doneNavigatingToNoteDetails()
            }
        }

        viewModel.reloadDataRequest.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.loadNotes()
                viewModel.doneRequestingLoadData()
                binding.noteList.invalidate()
                binding.noteList.requestLayout()
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
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