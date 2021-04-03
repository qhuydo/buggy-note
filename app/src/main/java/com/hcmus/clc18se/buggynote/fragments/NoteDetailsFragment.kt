package com.hcmus.clc18se.buggynote.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.hcmus.clc18se.buggynote.BuggyNoteActivity
import com.hcmus.clc18se.buggynote.R
import com.hcmus.clc18se.buggynote.data.Note
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabase
import com.hcmus.clc18se.buggynote.databinding.FragmentNoteDetailsBinding
import com.hcmus.clc18se.buggynote.viewmodels.NoteDetailsViewModel
import com.hcmus.clc18se.buggynote.viewmodels.NoteDetailsViewModelFactory
import com.hcmus.clc18se.buggynote.viewmodels.NoteViewModel
import com.hcmus.clc18se.buggynote.viewmodels.NoteViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


class NoteDetailsFragment : Fragment() {

    private lateinit var binding: FragmentNoteDetailsBinding

    private val db by lazy {
        BuggyNoteDatabase.getInstance(requireActivity()).buggyNoteDatabaseDao
    }

    private val arguments by lazy {
        NoteDetailsFragmentArgs.fromBundle(requireArguments())
    }

    private val viewModel: NoteDetailsViewModel by viewModels {
        NoteDetailsViewModelFactory(
                arguments.noteId,
                db
        )
    }
    private lateinit var noteViewModel: NoteViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        // what the hell am i doing
        val noteViewModel: NoteViewModel by activityViewModels {
            NoteViewModelFactory(
                    requireActivity().application,
                    db
            )
        }
        this.noteViewModel = noteViewModel

        binding = FragmentNoteDetailsBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = this@NoteDetailsFragment
            noteDetailsViewModel = viewModel
        }

        binding.appBar.toolbar.elevation = 0f
        return binding.root
    }

    override fun onPause() {
        super.onPause()

        val imm: InputMethodManager? = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)

        // TODO: why am I not able to use data binding?
        val title =
                binding.constraintLayout.findViewById<EditText>(R.id.text_view_title).text.toString()
        val content =
                binding.constraintLayout.findViewById<EditText>(R.id.note_content).text.toString()

        val note = viewModel.getNoteWithTags().value!!
        CoroutineScope(Dispatchers.Default).launch {
            if (title != note.getTitle() || content != note.getNoteContent()) {
                val newNote = Note(
                        id = note.getId(),
                        title = title,
                        noteContent = content,
                        lastModify = System.currentTimeMillis()
                )

                Timber.d("Set new note content")

                db.updateNote(newNote)

                withContext(Dispatchers.Main) {
                    viewModel.reloadNote()
                    // TODO: replace this line with code with a more efficient way
                    noteViewModel.loadNotes()
                }
            }
        }
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