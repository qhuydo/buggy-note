package com.hcmus.clc18se.buggynote.fragments

import android.content.Context
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

    private var menu: Menu? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        binding = FragmentNoteDetailsBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = this@NoteDetailsFragment
            noteDetailsViewModel = viewModel
            chipOnClickListener = tagOnClickListener
        }

        initObservers()

        return binding.root
    }

    private fun initObservers() {
        viewModel.getNoteWithTags().observe(viewLifecycleOwner) {
            updateMenu()
        }
        viewModel.reloadDataRequest.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.reloadNote()
                noteViewModel.requestReloadingData()
                viewModel.doneRequestingLoadData()
            }
        }

        viewModel.navigateToTagSelection.observe(viewLifecycleOwner) {
            if (it != null) {
                findNavController().navigate(
                        NoteDetailsFragmentDirections.actionNavNoteDetailsToTagSelectionFragment(
                                arguments.noteId
                        )
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

        val imm: InputMethodManager? =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
        saveNote()
    }

    private fun saveNote(require: Boolean = false) {
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
                        || require
                ) {
                    noteWithTags.note.title = title
                    noteWithTags.note.noteContent = content
                    noteWithTags.note.lastModify = System.currentTimeMillis()

                    Timber.d("Set new note content")

                    db.updateNote(noteWithTags.note)
                    noteViewModel.requestReloadingData()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.appBar.toolbar.elevation = 0f
        setUpNavigation()
    }

    private fun setUpNavigation() {
        setHasOptionsMenu(true)

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

    private val bottomBarOnItemClickListener = Toolbar.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.action_add_tag -> {
                viewModel.navigateToTagSelection()
                true
            }
            R.id.action_remove_note -> {
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.remove_from_device))
                        .setMessage(getString(R.string.remove_confirmation)).setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> }
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
            val formatter =
                    if (targetId == R.id.text_view_title) note.getTitleFormat() else note.getContentFormat()
            when (itemId) {
                R.id.action_set_bold -> formatter.toggleBold()
                R.id.action_set_italic -> formatter.toggleItalic()
                R.id.action_alignment -> formatter.toggleAlignment()
                R.id.action_set_font_type -> formatter.toggleFontType()
            }


            if (targetId == R.id.text_view_title) {
                val title = binding.layout.findViewById<EditText>(R.id.text_view_title)
                title.setNoteTitleFormat(note)
                noteWithTags.note.titleFormat = formatter.toString()
            } else {
                val content = binding.layout.findViewById<EditText>(R.id.note_content)
                content.setNoteContentFormat(note)
                noteWithTags.note.contentFormat = formatter.toString()
            }
            saveNote(true)
        }
    }

    private fun getActionFormatTarget(): Int {
        val title = binding.layout.findViewById<EditText>(R.id.text_view_title)

        if (title.isFocused) {
            return R.id.text_view_title
        }
        return R.id.note_content
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.note_detail, menu)
        this.menu = menu
    }

    private fun updateMenu() {
        menu?.let { onPrepareOptionsMenu(it) }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val pinnedItem = menu.findItem(R.id.action_pin)
        Timber.d("OnPrepare")
        val note = viewModel.getNoteWithTags().value
        val pinIcon = when (note?.isPinned()) {
            true -> {
                R.drawable.ic_baseline_push_pin_24
            }
            else -> R.drawable.ic_outline_push_pin_24
        }
        pinnedItem.setIcon(pinIcon)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_pin -> {
                val note = viewModel.getNoteWithTags().value
                val pinIcon = when (note?.isPinned()) {
                    true -> {
                        R.drawable.ic_baseline_push_pin_24
                    }
                    else -> R.drawable.ic_outline_push_pin_24
                }
                item.setIcon(pinIcon)

                viewModel.togglePin()
                saveNote(true)
                true
            }
            else -> false
        }
    }
}