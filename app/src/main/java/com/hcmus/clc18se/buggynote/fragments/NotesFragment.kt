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
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
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

    private val tagViewModel: TagViewModel by activityViewModels { TagViewModelFactory(db) }

    private val pinnedNoteAdapter by lazy { NoteAdapter(onNoteItemClickListener, PIN_TAG) }

    private val unPinnedNoteAdapter by lazy { NoteAdapter(onNoteItemClickListener, UNPIN_TAG) }

    private val filterTagAdapter by lazy { TagFilterAdapter(onTagCheckedChangeListener) }

    private val noteListTouchHelper by lazy {
        val callback = NoteItemTouchHelperCallBack(pinnedNoteAdapter, unPinnedNoteAdapter)
        ItemTouchHelper(callback)
    }

    // Adapter of the note list, contains 4 adapters in the following order
    //  0. DummyHeaderAdapter - header of the pinned note list
    //  1. NoteAdapter - presents list of pinned notes
    //  2. DummyHeaderAdapter - header of the unpinned note list
    //  3. NoteAdapter - presents list of unpinned notes
    private val concatAdapter by lazy {
        val config = ConcatAdapter.Config.Builder()
            .setIsolateViewTypes(false)
            .build()

        ConcatAdapter(
            config,
            DummyHeaderAdapter(
                getString(R.string.pinned),
                R.drawable.ic_outline_push_pin_24,
                noteViewModel.headerLabelVisibility
            ),
            pinnedNoteAdapter,
            DummyHeaderAdapter(
                getString(R.string.others),
                null,
                noteViewModel.headerLabelVisibility
            ),
            unPinnedNoteAdapter,
        )
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
            pinnedNoteAdapter.finishSelection()
            unPinnedNoteAdapter.finishSelection()

            mainCab?.destroy()

            runBlocking {
                if (noteViewModel.orderChanged.value == true &&
                    tagViewModel.tags.value?.all { !it.selectState } == true
                ) {
                    noteViewModel.reorderNotes(pinnedNoteAdapter.currentList)
                    noteViewModel.reorderNotes(unPinnedNoteAdapter.currentList)
                }
            }

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
            lifecycleOwner = this@NotesFragment
            noteViewModel = this@NotesFragment.noteViewModel
            tagViewModel = this@NotesFragment.tagViewModel
        }

        initRecyclerViews()
        initObservers()

        return binding.root
    }

    private fun initNoteAdapter(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        touchHelper: ItemTouchHelper,
        addItemDecoration: Boolean = false
    ) {
        recyclerView.setUpLayoutManagerForNoteList(preferences)
        recyclerView.adapter = adapter
        if (addItemDecoration) {
            recyclerView.addItemDecoration(
                SpaceItemDecoration(
                    resources.getDimension(R.dimen.item_note_margin).toInt()
                )
            )
        }
        touchHelper.attachToRecyclerView(recyclerView)
    }

    private fun initRecyclerViews() {
        binding.apply {
            initNoteAdapter(noteList, concatAdapter, noteListTouchHelper, true)

            tagFilterList.adapter = filterTagAdapter
            tagFilterList.addItemDecoration(
                SpaceItemDecoration(resources.getDimension(R.dimen.item_tag_margin).toInt())
            )
        }

    }

    private fun initObservers() {

        noteViewModel.noteList.observe(viewLifecycleOwner) {
            concatAdapter.notifyDataSetChanged()
        }

        noteViewModel.headerLabelVisibility.observe(viewLifecycleOwner) {
            concatAdapter.notifyDataSetChanged()
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
                binding.apply {
                    noteList.invalidate()
                    noteList.requestLayout()
                }
            }
        }

    }

    override fun onPause() {
        super.onPause()

        CoroutineScope(Dispatchers.Main).launch {
            if (noteViewModel.orderChanged.value == true &&
                tagViewModel.tags.value?.all { !it.selectState } == true
            ) {
                noteViewModel.reorderNotes(pinnedNoteAdapter.currentList)
                noteViewModel.reorderNotes(unPinnedNoteAdapter.currentList)
                noteViewModel.loadNoteFromDatabase()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding.noteList.setUpLayoutManagerForNoteList(preferences)
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

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                Timber.d("searchItem.onMenuItemActionCollapse called")
                tagViewModel.tags.value?.let { noteViewModel.filterByTagsFromDatabase(it) }
                noteListTouchHelper.attachToRecyclerView(binding.noteList)
                return true
            }

            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                Timber.d("searchItem.onMenuItemActionExpand called")
                noteListTouchHelper.attachToRecyclerView(null)
                return true
            }
        })

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

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.note_list_item_view_type -> {
                onItemTypeOptionClicked()

                when (preferences.getString(getString(R.string.note_list_view_type_key), "0")) {
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

        binding.apply {
            Timber.d("refreshNoteList")
            noteList.adapter = null
            initNoteAdapter(noteList, concatAdapter, noteListTouchHelper)
        }
        noteViewModel.requestReordering()
        concatAdapter.notifyDataSetChanged()
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
        if (pinnedNoteAdapter.numberOfSelectedItems() == 0
            && unPinnedNoteAdapter.numberOfSelectedItems() == 0
        ) {
            mainCab?.destroy()
            return
        }

        val numberOfSelectedItems = pinnedNoteAdapter.numberOfSelectedItems() +
                unPinnedNoteAdapter.numberOfSelectedItems()

        pinnedNoteAdapter.enableSelection()
        unPinnedNoteAdapter.enableSelection()

        if (mainCab.isActive()) {
            mainCab?.apply {
                title(literal = "$numberOfSelectedItems")
            }
        } else {
            val colorSurface = getColorAttribute(requireContext(), R.attr.colorSurface)
            val colorOnSurface = getColorAttribute(requireContext(), R.attr.colorOnSurface)

            mainCab = createCab(R.id.cab_stub) {
                title(literal = "$numberOfSelectedItems")
                menu(R.menu.main_context)
                popupTheme(R.style.ThemeOverlay_AppCompat_Light)
                titleColor(literal = colorOnSurface)
                subtitleColor(literal = colorOnSurface)
                backgroundColor(literal = colorSurface)

                slideDown()

                onCreate { _, menu -> onCabCreated(menu) }
                onSelection { onCabItemSelected(it) }
                onDestroy {
                    pinnedNoteAdapter.finishSelection()
                    unPinnedNoteAdapter.finishSelection()
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
                if (pinnedNoteAdapter.getSelectedItems().isEmpty()
                    && unPinnedNoteAdapter.getSelectedItems().isEmpty()
                ) {
                    return true
                }

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.remove_from_device))
                    .setMessage(getString(R.string.remove_confirmation))
                    .setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> }
                    .setPositiveButton(resources.getString(R.string.remove)) { _, _ ->

                        noteViewModel.removeNote(
                            *pinnedNoteAdapter.getSelectedItems().toTypedArray()
                        )
                        noteViewModel.removeNote(
                            *unPinnedNoteAdapter.getSelectedItems().toTypedArray()
                        )
                        mainCab?.destroy()
                    }
                    .show()
                true
            }
            R.id.action_select_all -> {
                var numberOfSelectedItems = pinnedNoteAdapter.numberOfSelectedItems() +
                        unPinnedNoteAdapter.numberOfSelectedItems()

                if (numberOfSelectedItems == noteViewModel.noteList.value?.size) {
                    pinnedNoteAdapter.unSelectAll()
                    unPinnedNoteAdapter.unSelectAll()
                } else {
                    pinnedNoteAdapter.selectAll()
                    unPinnedNoteAdapter.selectAll()
                }

                numberOfSelectedItems = pinnedNoteAdapter.numberOfSelectedItems() +
                        unPinnedNoteAdapter.numberOfSelectedItems()

                mainCab?.apply {
                    title(literal = "$numberOfSelectedItems")
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