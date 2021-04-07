package com.hcmus.clc18se.buggynote.fragments

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hcmus.clc18se.buggynote.BuggyNoteActivity
import com.hcmus.clc18se.buggynote.R
import com.hcmus.clc18se.buggynote.adapters.setNoteContentFormat
import com.hcmus.clc18se.buggynote.adapters.setNoteTitleFormat
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

        initObservers()

        return binding.root
    }

    private fun initObservers() {
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

        viewModel.deleteRequest.observe(viewLifecycleOwner) {
            if (it == true) {
                noteViewModel.requestReloadingData()
                requireActivity().onBackPressed()
            }
        }
    }

    override fun onPause() {
        super.onPause()

        val imm: InputMethodManager? = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
        saveNote()
    }

    private fun saveNote() {
        // TODO: why am I not able to use data binding?
        val title =
                binding.layout.findViewById<EditText>(R.id.text_view_title).text.toString()
        val content =
                binding.layout.findViewById<EditText>(R.id.note_content).text.toString()

        val noteWithTags = viewModel.getNoteWithTags().value
        noteWithTags?.let {
            lifecycleScope.launch {
                if (title != noteWithTags.getTitle()
                        || content != noteWithTags.getNoteContent()
                ) {
                    noteWithTags.note.title = title
                    noteWithTags.note.noteContent = content

                    Timber.d("Set new note content")

                    db.updateNote(noteWithTags.note)
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

        bottomBar.setOnMenuItemClickListener(bottomBarOnItemClickListener)
        parentActivity.setSupportActionBar(toolbar)
        parentActivity.setupActionBarWithNavController(
                findNavController(),
                parentActivity.appBarConfiguration
        )
    }

    private val bottomBarOnItemClickListener = Toolbar.OnMenuItemClickListener { it ->
        when (it.itemId) {
            R.id.action_add_tag -> {
                viewModel.navigateToTagSelection()
                true
            }
            R.id.action_remove_note -> {
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Warning")
                        .setMessage("Do you really want to remove this note?")
                        .setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> }
                        .setPositiveButton(resources.getString(R.string.remove)) { _, _ -> viewModel.deleteMe() }
                        .show()
                true
            }

            R.id.action_set_bold,
            R.id.action_set_italic,
            R.id.action_set_font_type,
            R.id.action_alignment -> {
                actionFormat(it.itemId)
                true
            }

            else -> false
        }
    }

    private fun actionFormat(itemId: Int) {
        val targetId = getActionFormatTarget()
        val noteWithTags = viewModel.getNoteWithTags().value

        noteWithTags?.let { note ->
            val formatter = if (targetId == R.id.text_view_title) note.getTitleFormat() else note.getContentFormat()
            when (itemId) {
                R.id.action_set_bold -> formatter.toggleBold()
                R.id.action_set_italic -> formatter.toggleItalic()
                R.id.action_alignment -> formatter.toggleAlignment()
                R.id.action_set_font_type -> formatter.toggleFontType()
            }


            if (targetId == R.id.text_view_title) {
                viewModel.setNoteTitleFormat(formatter)
                val title = binding.layout.findViewById<EditText>(R.id.text_view_title)
                title.setNoteTitleFormat(note)
            } else {
                val content = binding.layout.findViewById<EditText>(R.id.note_content)
                viewModel.setNoteContentFormat(formatter)
                content.setNoteContentFormat(note)
            }

        }
    }

    private fun getActionFormatTarget(): Int {
        val title = binding.layout.findViewById<EditText>(R.id.text_view_title)

        if (title.isFocused) {
            return R.id.text_view_title
        }
        return R.id.note_content
    }
}