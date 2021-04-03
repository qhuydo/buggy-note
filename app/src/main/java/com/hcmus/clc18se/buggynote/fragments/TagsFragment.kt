package com.hcmus.clc18se.buggynote.fragments

import android.content.Context
import android.graphics.drawable.Icon
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.hcmus.clc18se.buggynote.BuggyNoteActivity
import com.hcmus.clc18se.buggynote.R
import com.hcmus.clc18se.buggynote.adapters.ItemEditorFocusListener
import com.hcmus.clc18se.buggynote.adapters.TagAdapter
import com.hcmus.clc18se.buggynote.adapters.loadTags
import com.hcmus.clc18se.buggynote.database.BuggyNoteDatabase
import com.hcmus.clc18se.buggynote.databinding.FragmentTagsBinding
import com.hcmus.clc18se.buggynote.viewmodels.TagViewModel
import com.hcmus.clc18se.buggynote.viewmodels.TagViewModelFactory

class TagsFragment : Fragment() {

    private lateinit var binding: FragmentTagsBinding

    private val adapter by lazy {
        TagAdapter(onItemEditorFocusListener)
    }

    private val viewModel: TagViewModel by viewModels {
        TagViewModelFactory(
                BuggyNoteDatabase.getInstance(requireContext()).buggyNoteDatabaseDao
        )
    }

    private val parentContext: Context by lazy {
        requireContext()
    }

    private val onItemEditorFocusListener = ItemEditorFocusListener { binding, hasFocus, tag ->

        var removeIcon = R.drawable.ic_outline_label_24
        var checkIcon = R.drawable.ic_baseline_mode_edit_24

        if (hasFocus) {
            removeIcon = R.drawable.ic_outline_delete_24
            checkIcon = R.drawable.ic_baseline_done_24

            // TODO: action for remove & done button
        }

        binding.checkButton.setImageDrawable(ResourcesCompat.getDrawable(resources, checkIcon, parentContext.theme))
        binding.removeButton.setImageDrawable(ResourcesCompat.getDrawable(resources, removeIcon, parentContext.theme))

        binding.checkButton.invalidate()
        binding.removeButton.invalidate()
        binding.root.requestLayout()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = 300L
        }
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = 300L
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentTagsBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = this@TagsFragment
            tagList.adapter = adapter
            tagViewModel = viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpNavigation()

        viewModel.tags.observe(viewLifecycleOwner, {
            adapter.notifyDataSetChanged()

        })

        binding.addTagLayout.setOnFocusChangeListener { v, hasFocus ->
            binding.addTagIcon.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
        }

        binding.addTagDone.setOnClickListener { v ->

            val tagContent = binding.addTagContent.text.toString().trim()
            val succeed = viewModel.addNewTag(tagContent)

            binding.addTagLayout.apply {
                error = if (succeed) null else getString(R.string.exist_tag)
                isErrorEnabled = !succeed
                editText?.text?.clear()
            }

        }
    }

    private fun setUpNavigation() {
        val toolbar = binding.appBar.toolbar
        val parentActivity: BuggyNoteActivity = requireActivity() as BuggyNoteActivity

        parentActivity.setSupportActionBar(toolbar)
        val navHostFragment = NavHostFragment.findNavController(this)
        parentActivity.setupActionBarWithNavController(findNavController(), parentActivity.appBarConfiguration)
        NavigationUI.setupWithNavController(toolbar, navHostFragment, parentActivity.appBarConfiguration)

    }
}