package com.hcmus.clc18se.buggynote.fragments

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.appbar.AppBarLayout
import com.hcmus.clc18se.buggynote.BuggyNoteActivity
import com.hcmus.clc18se.buggynote.R
import com.hcmus.clc18se.buggynote.adapters.ItemOnCheckedChangeListener
import com.hcmus.clc18se.buggynote.adapters.NoteAdapter
import com.hcmus.clc18se.buggynote.adapters.OnClickListener
import com.hcmus.clc18se.buggynote.adapters.TagFilterAdapter
import com.hcmus.clc18se.buggynote.data.Note
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabase
import com.hcmus.clc18se.buggynote.databinding.FragmentNotesBinding
import com.hcmus.clc18se.buggynote.utils.SpaceItemDecoration
import com.hcmus.clc18se.buggynote.viewmodels.NoteViewModel
import com.hcmus.clc18se.buggynote.viewmodels.NoteViewModelFactory
import com.hcmus.clc18se.buggynote.viewmodels.TagViewModel
import com.hcmus.clc18se.buggynote.viewmodels.TagViewModelFactory
import kotlinx.coroutines.launch
import timber.log.Timber

class NotesFragment : Fragment() {

    private lateinit var binding: FragmentNotesBinding

    private val db by lazy {
        BuggyNoteDatabase.getInstance(requireActivity()).buggyNoteDatabaseDao
    }

    private val noteViewModel: NoteViewModel by activityViewModels {
        NoteViewModelFactory(requireActivity().application, db)
    }

    private val tagViewModel: TagViewModel by activityViewModels {
        TagViewModelFactory(db)
    }

    private val adapter by lazy {
        NoteAdapter(onNoteItemClickListener)
    }

    private val filterTagAdapter by lazy {
        TagFilterAdapter(onTagCheckedChangeListener)
    }

    private val onNoteItemClickListener = OnClickListener { noteWithTags ->
        noteViewModel.navigateToNoteDetails(noteWithTags.note.id)
    }

    private val onTagCheckedChangeListener = ItemOnCheckedChangeListener { isChecked, tag ->
        if (tag.selectState != isChecked) {
            tag.selectState = isChecked

            tagViewModel.tags.value?.let { noteViewModel.filterByTagsFromDatabase(it) }
        }
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
                val id = noteViewModel.insertNewNote(Note(title = "", noteContent = ""))
                noteViewModel.navigateToNoteDetails(id)
            }

        }

        binding.apply {
            lifecycleOwner = this@NotesFragment

            noteViewModel = this@NotesFragment.noteViewModel
            tagViewModel = this@NotesFragment.tagViewModel

            noteList.adapter = adapter
            noteList.addItemDecoration(
                    SpaceItemDecoration(resources.getDimension(R.dimen.item_note_margin).toInt())
            )

            tagFilterList.adapter = filterTagAdapter
            tagFilterList.addItemDecoration(
                    SpaceItemDecoration(resources.getDimension(R.dimen.item_tag_margin).toInt())
            )
        }

        initObservers()

        return binding.root
    }

    private fun initObservers() {

        noteViewModel.noteList.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
        }

        tagViewModel.tags.observe(viewLifecycleOwner) {
            filterTagAdapter.notifyDataSetChanged()
        }

        noteViewModel.navigateToNoteDetails.observe(viewLifecycleOwner) {
            if (it != null) {

                findNavController().navigate(
                        NotesFragmentDirections.actionNavNotesToNoteDetailsFragment(it)
                )
                noteViewModel.doneNavigatingToNoteDetails()
            }
        }

        noteViewModel.reloadDataRequest.observe(viewLifecycleOwner) {
            if (it) {
                noteViewModel.loadNotes()
                noteViewModel.doneRequestingLoadData()
                binding.noteList.invalidate()
                binding.noteList.requestLayout()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

//        val searchViewParams = searchView.layoutParams as AppBarLayout.LayoutParams
//        searchViewParams.setMargins(0, 0, 0, 0)
//        searchView.layoutParams = searchViewParams

        // TODO: fix this inefficient
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                Timber.d("newText: $newText")
                if (newText != null) {
                    tagViewModel.tags.value?.let { noteViewModel.filterByTagsWithKeyword(it, newText) }
                } else {
                    tagViewModel.tags.value?.let { noteViewModel.filterByTagsFromDatabase(it) }
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    tagViewModel.tags.value?.let { noteViewModel.filterByTagsWithKeyword(it, query) }
                    return true
                }
                return false
            }
        })

        searchView.setOnCloseListener {
            Timber.d("searchView.setOnCloseListener called")
            tagViewModel.tags.value?.let { noteViewModel.filterByTagsFromDatabase(it) }
            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {


            }

        }
        return super.onOptionsItemSelected(item)
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

    private fun addLabelFilterButtonInsideSearchView(item: MenuItem) {
        val searchView = item.actionView as SearchView
        // Timber.d("${(searchView.getChildAt(0) as LinearLayout).childCount}")
        // TODO: think of a better solution
        if ((searchView.getChildAt(0) as LinearLayout).childCount == 3) {
            val itemImageView = ImageView(requireContext())
            itemImageView.setImageResource(R.drawable.ic_outline_new_label_24)

            // searchView.addView(itemImageView)
            (searchView.getChildAt(0) as LinearLayout).addView(itemImageView)
            (searchView.getChildAt(0) as LinearLayout).gravity = Gravity.CENTER_VERTICAL or Gravity.END

            val searchParams = searchView.layoutParams as? AppBarLayout.LayoutParams
            searchParams?.let {
                searchView.setPadding(0, 0, 0, 0)
                searchView.layoutParams = it
            }

            val params = itemImageView.layoutParams as LinearLayout.LayoutParams
            params.gravity = Gravity.CENTER
            itemImageView.layoutParams = params

            searchView.invalidate()
            searchView.requestLayout()

        }
    }
}