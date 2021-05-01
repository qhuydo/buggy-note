package com.hcmus.clc18se.buggynote.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hcmus.clc18se.buggynote.BuggyNoteActivity
import com.hcmus.clc18se.buggynote.R
import com.hcmus.clc18se.buggynote.adapters.TagAdapterCallbacks
import com.hcmus.clc18se.buggynote.adapters.TagAdapter
import com.hcmus.clc18se.buggynote.data.Tag
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabase
import com.hcmus.clc18se.buggynote.databinding.FragmentTagsBinding
import com.hcmus.clc18se.buggynote.databinding.ItemTagBinding
import com.hcmus.clc18se.buggynote.utils.OnBackPressed
import com.hcmus.clc18se.buggynote.viewmodels.NoteViewModel
import com.hcmus.clc18se.buggynote.viewmodels.NoteViewModelFactory
import com.hcmus.clc18se.buggynote.viewmodels.TagViewModel
import com.hcmus.clc18se.buggynote.viewmodels.TagViewModelFactory
import timber.log.Timber

class TagsFragment : BaseFragment(), OnBackPressed {

    private lateinit var binding: FragmentTagsBinding

    private val adapter by lazy { TagAdapter(onItemEditorFocusListener) }

    private val tagViewModel: TagViewModel by activityViewModels {
        TagViewModelFactory(BuggyNoteDatabase.getInstance(requireContext()).buggyNoteDatabaseDao)
    }

    private val parentContext: Context by lazy { requireContext() }

    private val noteViewModel: NoteViewModel by activityViewModels {
        NoteViewModelFactory(
                requireActivity().application,
                BuggyNoteDatabase.getInstance(requireContext()).buggyNoteDatabaseDao
        )
    }

    private fun updateATag(tag: Tag, itemTagBinding: ItemTagBinding) {
        val newTag = itemTagBinding.tagContent.text.toString().trim()

        if (tag.name == newTag) {
            // Do nothing when the tag is unchanged
            return
        }

        val updatedTag = Tag(id = tag.id, name = newTag)
        val succeed = tagViewModel.updateTag(updatedTag)
        if (!succeed) {
            Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
            itemTagBinding.tagContent.setText(tag.name)
        } else {
            noteViewModel.requestReloadingData()
        }
    }

    private fun performRemovingTag(tag: Tag) {
        // Hide the keyboard when remove a tag
        hideTheKeyboard()
        tagViewModel.deleteTag(tag)
        noteViewModel.requestReloadingData()
    }

    private fun removeATag(tag: Tag) {
        if (!tagViewModel.isTagExistedInTheNoteList(tag)) {
            performRemovingTag(tag)
            return
        }

        MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.remove_tag_dialog_title)
                .setMessage(R.string.remove_tag_dialog_content)
                .setPositiveButton(R.string.remove) { _, _ -> performRemovingTag(tag) }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .show()
    }

    private val onItemEditorFocusListener = TagAdapterCallbacks { binding, hasFocus, tag ->
        Timber.d("On focus listener is called")
        var removeIcon = R.drawable.ic_outline_label_24
        var checkIcon = R.drawable.ic_baseline_mode_edit_24

        if (hasFocus) {
            removeIcon = R.drawable.ic_baseline_delete_24
            checkIcon = R.drawable.ic_baseline_done_24

            binding.apply {
                checkButton.setOnClickListener { updateATag(tag, this) }
                removeButton.setOnClickListener { removeATag(tag) }
            }
        } else {
            binding.apply {
                checkButton.setOnClickListener { tagContent.requestFocus() }
                removeButton.setOnClickListener { tagContent.requestFocus() }
            }
        }

        // set drawable based on the current focus state
        binding.checkButton.apply {
            setImageDrawable(ResourcesCompat.getDrawable(resources, checkIcon, parentContext.theme))
            invalidate()
        }
        binding.removeButton.apply {
            setImageDrawable(ResourcesCompat.getDrawable(resources, removeIcon, parentContext.theme))
            invalidate()
        }
        binding.root.requestLayout()

    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentTagsBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = this@TagsFragment
            tagViewModel = this@TagsFragment.tagViewModel
            tagList.adapter = adapter
        }

        tagViewModel.tags.observe(viewLifecycleOwner, {
            adapter.notifyDataSetChanged()
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addTagLayout.setOnFocusChangeListener { _, hasFocus ->
            binding.addTagIcon.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
        }

        binding.addTagDone.setOnClickListener {

            val tagContent = binding.addTagContent.text.toString().trim()
            if (tagContent.isBlank()) {
                binding.addTagLayout.editText?.text?.clear()
                return@setOnClickListener
            }

            val succeed = tagViewModel.addNewTag(tagContent)

            binding.addTagLayout.apply {
                error = if (succeed) null else getString(R.string.exist_tag)
                isErrorEnabled = !succeed
                editText?.text?.clear()
            }

        }
    }

    override fun onBackPress(): Boolean {

        if (binding.tagList.hasFocus()) {
            binding.tagList.clearFocus()
            return true
        }
        return false
    }

    override fun onPause() {
        hideTheKeyboard()
        super.onPause()
    }

    private fun hideTheKeyboard() {
        val imm: InputMethodManager? = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun getToolbarView(): Toolbar = binding.appBar.toolbar
}