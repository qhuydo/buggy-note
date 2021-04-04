package com.hcmus.clc18se.buggynote.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.hcmus.clc18se.buggynote.BuggyNoteActivity
import com.hcmus.clc18se.buggynote.R
import com.hcmus.clc18se.buggynote.data.Note
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabase
import com.hcmus.clc18se.buggynote.databinding.FragmentAddNoteBinding
import com.hcmus.clc18se.buggynote.viewmodels.NoteViewModel
import com.hcmus.clc18se.buggynote.viewmodels.NoteViewModelFactory

class AddNoteFragment : Fragment() {

    private lateinit var binding: FragmentAddNoteBinding

    private val viewModel: NoteViewModel by activityViewModels() {
        NoteViewModelFactory(
                requireActivity().application,
                BuggyNoteDatabase.getInstance(requireActivity()).buggyNoteDatabaseDao
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        binding.lifecycleOwner = this
        binding.noteViewModel = viewModel

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add_note, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                val title =
                        binding.layout.findViewById<EditText>(R.id.text_view_title).text.toString()
                val content =
                        binding.layout.findViewById<EditText>(R.id.note_content).text.toString()

                viewModel.insertNewNote(Note(title = title, noteContent = content))
                findNavController().popBackStack()
                true
            }
            else -> false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpNavigation()
    }

    private fun setUpNavigation() {
        val toolbar = binding.appBar.toolbar
        val parentActivity: BuggyNoteActivity = requireActivity() as BuggyNoteActivity

        parentActivity.apply {

            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_close_24)

            setupActionBarWithNavController(findNavController(), parentActivity.appBarConfiguration)
        }
    }

    override fun onPause() {
        super.onPause()
        val imm: InputMethodManager? = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }


}