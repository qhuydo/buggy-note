package com.hcmus.clc18se.buggynote.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomappbar.BottomAppBar
import com.hcmus.clc18se.buggynote.BuggyNoteActivity
import com.hcmus.clc18se.buggynote.R
import com.hcmus.clc18se.buggynote.data.Note
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabase
import com.hcmus.clc18se.buggynote.databinding.FragmentNoteDetailsBinding
import com.hcmus.clc18se.buggynote.viewmodels.NoteDetailsViewModel
import com.hcmus.clc18se.buggynote.viewmodels.NoteDetailsViewModelFactory
import com.hcmus.clc18se.buggynote.viewmodels.NoteViewModel
import com.hcmus.clc18se.buggynote.viewmodels.NoteViewModelFactory
import kotlinx.coroutines.launch
import timber.log.Timber


class NoteDetailsFragment : Fragment() {

    private lateinit var binding: FragmentNoteDetailsBinding

    private val db by lazy {
        BuggyNoteDatabase.getInstance(requireActivity()).buggyNoteDatabaseDao
    }

    private val arguments by lazy {
        NoteDetailsFragmentArgs.fromBundle(requireArguments())
    }

    private val viewModel: NoteDetailsViewModel by navGraphViewModels(R.id.navigation_note_details) {
        NoteDetailsViewModelFactory(
                arguments.noteId,
                db
        )
    }

    private val noteViewModel: NoteViewModel by activityViewModels {
        NoteViewModelFactory(
                requireActivity().application,
                db
        )
    }

    private val tagOnClickListener = View.OnClickListener {
        viewModel.navigateToTagSelection()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        binding = FragmentNoteDetailsBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = this@NoteDetailsFragment
            chipOnClickListener = tagOnClickListener
            noteDetailsViewModel = viewModel
        }

        viewModel.reloadDataRequest.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.reloadNote()
                viewModel.doneRequestingLoadData()
                noteViewModel.requestReloadingData()
            }
        }

        viewModel.navigateToTagSelection.observe(viewLifecycleOwner) {
            if (it != null) {
                findNavController().navigate(
                        NoteDetailsFragmentDirections.actionNavNoteDetailsToTagSelectionFragment(arguments.noteId)
                )
                viewModel.doneNavigatingToTagSelection()
            }
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()

        val imm: InputMethodManager? = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)

        // TODO: why am I not able to use data binding?
        val title =
                binding.layout.findViewById<EditText>(R.id.text_view_title).text.toString()
        val content =
                binding.layout.findViewById<EditText>(R.id.note_content).text.toString()

        val note = viewModel.getNoteWithTags().value
        note?.let {
            lifecycleScope.launch {
                if (title != note.getTitle() || content != note.getNoteContent()) {
                    val newNote = Note(
                            id = note.getId(),
                            title = title,
                            noteContent = content,
                            lastModify = System.currentTimeMillis()
                    )

                    Timber.d("Set new note content")

                    db.updateNote(newNote)

                    viewModel.reloadNote()
                    noteViewModel.requestReloadingData()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpNavigation()
        binding.appBar.toolbar.elevation = 0f
    }

    private fun setUpNavigation() {
        val toolbar = binding.appBar.toolbar
        val parentActivity: BuggyNoteActivity = requireActivity() as BuggyNoteActivity

        val bottomBar: BottomAppBar = binding.coordinatorLayout.findViewById(R.id.bottom_bar)

        bottomBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_add_tag -> {
                    viewModel.navigateToTagSelection()
                    true
                }
                else -> true
            }
        }

        parentActivity.setSupportActionBar(toolbar)
        parentActivity.setupActionBarWithNavController(
                findNavController(),
                parentActivity.appBarConfiguration
        )
    }
}