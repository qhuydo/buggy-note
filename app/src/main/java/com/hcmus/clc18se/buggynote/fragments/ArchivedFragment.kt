package com.hcmus.clc18se.buggynote.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.hcmus.clc18se.buggynote.BuggyNoteActivity
import com.hcmus.clc18se.buggynote.R
import com.hcmus.clc18se.buggynote.adapters.*
import com.hcmus.clc18se.buggynote.data.NoteWithTags
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabase
import com.hcmus.clc18se.buggynote.databinding.FragmentArchivedBinding
import com.hcmus.clc18se.buggynote.utils.*
import com.hcmus.clc18se.buggynote.viewmodels.NoteViewModel
import com.hcmus.clc18se.buggynote.viewmodels.NoteViewModelFactory
import com.hcmus.clc18se.buggynote.viewmodels.TagViewModel
import com.hcmus.clc18se.buggynote.viewmodels.TagViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

// TODO: refactor the fragment
class ArchivedFragment : Fragment(), OnBackPressed {

    private val preferences by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }

    private lateinit var binding: FragmentArchivedBinding

    private val db by lazy { BuggyNoteDatabase.getInstance(requireActivity()).buggyNoteDatabaseDao }

    private val noteViewModel: NoteViewModel by activityViewModels {
        NoteViewModelFactory(requireActivity().application, db)
    }

    private val tagViewModel: TagViewModel by activityViewModels { TagViewModelFactory(db) }

    private val archivedNoteAdapter by lazy { NoteAdapter(noteAdapterCallbacks, ARCHIVE_TAG) }

    private val filterTagAdapter by lazy { TagFilterAdapter(onTagCheckedChangeListener) }

    private val noteListTouchHelper by lazy {
        val callback = NoteItemTouchHelperCallBack(archivedNoteAdapter)
        ItemTouchHelper(callback)
    }

    private val noteAdapterCallbacks = object : NoteAdapterCallbacks {
        override fun onClick(note: NoteWithTags) {
            noteViewModel.navigateToNoteDetails(note.getId())
        }

        override fun onMultipleSelect(note: NoteWithTags): Boolean {
            val parentActivity = requireActivity()
            if (parentActivity is ControllableDrawerActivity) {
                parentActivity.lockTheDrawer()
            }
            invalidateCab()
            return true
        }

        override fun onPostReordered(notes: List<NoteWithTags>) {
            noteViewModel.requestReordering()
        }

        override fun onItemSwiped(note: NoteWithTags) {
            noteViewModel.moveToNoteList(note)

            Snackbar.make(binding.root, R.string.unarchived_moved_to_note_list, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo) {
                        noteViewModel.moveToArchive(note)
                    }.show()

            mainCab?.destroy()
        }
    }

    private val onTagCheckedChangeListener = ItemOnCheckedChangeListener { isChecked, tag ->
        if (tag.selectState != isChecked) {
            archivedNoteAdapter.finishSelection()
            mainCab?.destroy()

            runBlocking {
                if (noteViewModel.orderChanged.value == true &&
                        tagViewModel.tags.value?.all { !it.selectState } == true
                ) {
                    noteViewModel.reorderNotes(archivedNoteAdapter.currentList)
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
        binding = FragmentArchivedBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        binding.apply {
            lifecycleOwner = this@ArchivedFragment
            noteViewModel = this@ArchivedFragment.noteViewModel
            tagViewModel = this@ArchivedFragment.tagViewModel
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
                            resources.getDimension(R.dimen.item_note_padding_top).toInt()
                    )
            )
        }
        touchHelper.attachToRecyclerView(recyclerView)
    }

    private fun initRecyclerViews() {
        binding.apply {
            initNoteAdapter(noteList, archivedNoteAdapter, noteListTouchHelper, true)
            tagFilterList.adapter = filterTagAdapter
            tagFilterList.addItemDecoration(
                    SpaceItemDecoration(resources.getDimension(R.dimen.item_tag_margin).toInt())
            )

        }
    }

    private fun initObservers() {

        noteViewModel.noteList.observe(viewLifecycleOwner) {
            archivedNoteAdapter.notifyDataSetChanged()
        }

        noteViewModel.headerLabelVisibility.observe(viewLifecycleOwner) {
            archivedNoteAdapter.notifyDataSetChanged()
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
                Timber.d("reloadDataRequest.observe")
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

        lifecycleScope.launch {
            if (noteViewModel.orderChanged.value == true &&
                tagViewModel.tags.value?.all { !it.selectState } == true
            ) {
                noteViewModel.reorderNotes(archivedNoteAdapter.currentList)
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
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.archive, menu)
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
            initNoteAdapter(noteList, archivedNoteAdapter, noteListTouchHelper)
        }
        noteViewModel.requestReordering()
        archivedNoteAdapter.notifyDataSetChanged()
        binding.noteList.startLayoutAnimation()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpNavigation()
    }

    private fun setUpNavigation() {
        val toolbar = binding.appBar.toolbar
        val parentActivity = requireActivity() as? BuggyNoteActivity

        if (parentActivity == null) {
            Timber.e("Parent activity of fragment ${this.tag} is not BuggyNoteActivity")
        }

        parentActivity?.setSupportActionBar(toolbar)
        parentActivity?.setupActionBarWithNavController(
                findNavController(),
                parentActivity.appBarConfiguration
        )
    }

    /**
     * Update the mainCab states when the note list is in multi selection mode
     *
     * Create a new cab object when an item from the note is long clicked to enable multi selection mode.
     * Update the cab title when changing the selection
     * Destroy the cab when the note list is no longer in multi selection mode.
     */
    private fun invalidateCab() {
        if (archivedNoteAdapter.numberOfSelectedItems() == 0) {
            mainCab?.destroy()
            return
        }

        val numberOfSelectedItems = archivedNoteAdapter.numberOfSelectedItems()

        archivedNoteAdapter.enableSelection()

        if (mainCab.isActive()) {
            mainCab?.apply {
                title(literal = "$numberOfSelectedItems")
            }
        } else {
            createCab()
        }
    }

    private fun createCab() {
        val colorSurface = getColorAttribute(requireContext(), R.attr.colorSurface)
        val colorOnSurface = getColorAttribute(requireContext(), R.attr.colorOnSurface)

        mainCab = createCab(R.id.cab_stub) {
            title(literal = "1")
            menu(R.menu.archive_context)
            popupTheme(R.style.ThemeOverlay_AppCompat_Light)
            titleColor(literal = colorOnSurface)
            subtitleColor(literal = colorOnSurface)
            backgroundColor(literal = colorSurface)

            slideDown()

            onCreate { _, menu -> onCabCreated(menu) }
            onSelection { onCabItemSelected(it) }
            onDestroy {
                archivedNoteAdapter.finishSelection()
                val parentActivity = requireActivity()
                if (parentActivity is ControllableDrawerActivity) {
                    parentActivity.unlockTheDrawer()
                }
                mainCab = null
                true
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
            R.id.action_unarchived -> {
                if (archivedNoteAdapter.numberOfSelectedItems() == 0) {
                    return true
                }
                noteViewModel.moveToNoteList(*archivedNoteAdapter.getSelectedItems().toTypedArray())
                mainCab?.destroy()
                return true
            }
            R.id.action_remove_note -> {
                if (archivedNoteAdapter.getSelectedItems().isEmpty()
                ) {
                    return true
                }

                MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.remove_from_device))
                        .setMessage(getString(R.string.remove_confirmation))
                        .setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> }
                        .setPositiveButton(resources.getString(R.string.remove)) { _, _ ->

                            noteViewModel.removeNote(
                                    *archivedNoteAdapter.getSelectedItems().toTypedArray(),
                            )
                            mainCab?.destroy()
                        }
                        .show()
                true
            }
            R.id.action_select_all -> {
                var numberOfSelectedItems = archivedNoteAdapter.numberOfSelectedItems()
                if (numberOfSelectedItems == noteViewModel.noteList.value?.size) {
                    archivedNoteAdapter.unSelectAll()
                } else {
                    archivedNoteAdapter.selectAll()
                }

                numberOfSelectedItems = archivedNoteAdapter.numberOfSelectedItems()

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