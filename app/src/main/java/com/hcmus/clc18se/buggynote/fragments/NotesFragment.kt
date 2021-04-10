package com.hcmus.clc18se.buggynote.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hcmus.clc18se.buggynote.BuggyNoteActivity
import com.hcmus.clc18se.buggynote.R
import com.hcmus.clc18se.buggynote.adapters.*
import com.hcmus.clc18se.buggynote.data.Note
import com.hcmus.clc18se.buggynote.data.NoteWithTags
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabase
import com.hcmus.clc18se.buggynote.databinding.FragmentNotesBinding
import com.hcmus.clc18se.buggynote.utils.*
import com.hcmus.clc18se.buggynote.viewmodels.NoteViewModel
import com.hcmus.clc18se.buggynote.viewmodels.NoteViewModelFactory
import com.hcmus.clc18se.buggynote.viewmodels.TagViewModel
import com.hcmus.clc18se.buggynote.viewmodels.TagViewModelFactory
import kotlinx.coroutines.*
import timber.log.Timber

class NotesFragment : Fragment(), OnBackPressed {

    private val preferences by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }

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

    private val onNoteItemClickListener = object : OnClickHandler {
        override fun onClick(note: NoteWithTags) {
            noteViewModel.navigateToNoteDetails(note.getId())
        }

        override fun onMultipleSelect(note: NoteWithTags): Boolean {
            invalidateCab()
            return true
        }

        override fun onPostReordered(notes: List<NoteWithTags>) {
            noteViewModel.requestReordering()
        }

    }

    private val onTagCheckedChangeListener = ItemOnCheckedChangeListener { isChecked, tag ->
        if (tag.selectState != isChecked) {
            adapter.finishSelection()
            mainCab?.destroy()
            tag.selectState = isChecked

            tagViewModel.tags.value?.let { noteViewModel.filterByTagsFromDatabase(it) }
        }
    }

    private var mainCab: AttachedCab? = null

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
            // TODO: refactor me
            lifecycleOwner = this@NotesFragment

            noteViewModel = this@NotesFragment.noteViewModel
            tagViewModel = this@NotesFragment.tagViewModel

            noteList.adapter = adapter
            noteList.addItemDecoration(
                    SpaceItemDecoration(resources.getDimension(R.dimen.item_note_margin).toInt())
            )
            val layoutManager = noteList.layoutManager as StaggeredGridLayoutManager
            layoutManager.spanCount = requireContext().getSpanCountForNoteList(preferences)

            val callback = NoteItemTouchHelperCallBack(adapter)
            val itemTouchHelper = ItemTouchHelper(callback)
            itemTouchHelper.attachToRecyclerView(binding.noteList)

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
                if (tagViewModel.tags.value != null) {
                    noteViewModel.filterByTagsFromDatabase(tagViewModel.tags.value!!)
                } else {
                    noteViewModel.loadNotes()
                }
                noteViewModel.doneRequestingLoadData()
                binding.noteList.invalidate()
                binding.noteList.requestLayout()
            }
        }

    }

    override fun onPause() {
        super.onPause()

        CoroutineScope(Dispatchers.Main).launch {
            if (noteViewModel.orderChanged.value == true) {
                noteViewModel.reorderNotes(adapter.currentList)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val noteListDisplayType =
                preferences.getString(getString(R.string.note_list_view_type_key), "0")

        val noteListDisplayItem = menu.findItem(R.id.note_list_item_view_type)
        when (noteListDisplayType) {
            "0" -> noteListDisplayItem.setIcon(R.drawable.ic_baseline_list_alt_24)
            else -> noteListDisplayItem.setIcon(R.drawable.ic_baseline_grid_view_24)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        // TODO: fix this inefficient
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                Timber.d("newText: $newText")
                if (newText != null) {
                    tagViewModel.tags.value?.let {
                        noteViewModel.filterByTagsWithKeyword(
                                it,
                                newText
                        )
                    }
                } else {
                    tagViewModel.tags.value?.let { noteViewModel.filterByTagsFromDatabase(it) }
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    tagViewModel.tags.value?.let {
                        noteViewModel.filterByTagsWithKeyword(
                                it,
                                query
                        )
                    }
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
        return when (item.itemId) {

            R.id.note_list_item_view_type -> {
                onItemTypeOptionClicked()
                val noteListDisplayType =
                        preferences.getString(getString(R.string.note_list_view_type_key), "0")

                when (noteListDisplayType) {
                    "0" -> item.setIcon(R.drawable.ic_baseline_list_alt_24)
                    else -> item.setIcon(R.drawable.ic_baseline_grid_view_24)
                }
                true
            }

            else -> false
        }
    }

    private fun onItemTypeOptionClicked() {
        val currentItemView =
                preferences.getString(getString(R.string.note_list_view_type_key), "0")
        val nextItemView = if (currentItemView == "0") "1" else "0"

        preferences.edit()
                .putString(getString(R.string.note_list_view_type_key), nextItemView)
                .apply()

        refreshNoteList()
    }

    private fun refreshNoteList() {
        binding.noteList.adapter = null
        binding.noteList.adapter = adapter

        val layoutManager = binding.noteList.layoutManager as StaggeredGridLayoutManager
        layoutManager.spanCount = requireContext().getSpanCountForNoteList(preferences)

        binding.noteList.loadNotes(noteViewModel.noteList.value)
        adapter.notifyDataSetChanged()
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

    fun invalidateCab() {
        if (adapter.numberOfSelectedItems() == 0) {
            mainCab?.destroy()
            // mainCab = null
            return
        }

        if (mainCab.isActive()) {
            mainCab?.apply {
                title(literal = "${adapter.numberOfSelectedItems()}")
            }
        } else {
            val colorSurface = getColorAttribute(requireContext(), R.attr.colorSurface)
            val colorOnSurface = getColorAttribute(requireContext(), R.attr.colorOnSurface)

            mainCab = createCab(R.id.cab_stub) {
                title(literal = "${adapter.numberOfSelectedItems()}")
                menu(R.menu.main_context)
                popupTheme(R.style.ThemeOverlay_AppCompat_Light)
                titleColor(literal = colorOnSurface)
                subtitleColor(literal = colorOnSurface)
                backgroundColor(literal = colorSurface)

                slideDown()

                onCreate { _, menu -> onCabCreated(menu) }
                onSelection { onCabItemSelected(it) }
                onDestroy {
                    adapter.finishSelection()
                    mainCab = null
                    true
                }
            }
        }
    }

    private fun onCabCreated(menu: Menu): Boolean {
        // Makes the icons in the overflow menu visible
        val colorOnSurface = getColorAttribute(requireContext(), R.attr.colorOnSurface)

        menu.tintAllIcons(colorOnSurface)
        if (menu.javaClass.simpleName == "MenuBuilder") {
            try {
                val field = menu.javaClass.getDeclaredField("mOptionalIconsVisible")
                field.isAccessible = true
                field.setBoolean(menu, true)
            } catch (ignored: Exception) {
                ignored.printStackTrace()
            }
        }
        return true // allow creation
    }

    private fun onCabItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_remove_note -> {
                if (adapter.getSelectedItems().isEmpty()) {
                    return true
                }

                MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.remove_from_device))
                        .setMessage(getString(R.string.remove_confirmation))
                        .setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> }
                        .setPositiveButton(resources.getString(R.string.remove)) { _, _ ->

                            noteViewModel.removeNote(*adapter.getSelectedItems().toTypedArray())
                            mainCab?.destroy()
                        }
                        .show()
                true
            }
            R.id.action_select_all -> {
                adapter.toggleSelectAll()
                mainCab?.apply {
                    title(literal = "${adapter.numberOfSelectedItems()}")
                }
                true
            }
            else -> false
        }
    }

    override fun onDetach() {
        mainCab?.destroy()
        super.onDetach()
    }

    override fun onBackPress(): Boolean {
        mainCab?.let {
            it.destroy()
            return true
        } ?: return false
    }
}