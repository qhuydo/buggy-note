package com.hcmus.clc18se.buggynote.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Checkable
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hcmus.clc18se.buggynote.data.NoteWithTags
import com.hcmus.clc18se.buggynote.databinding.ItemNoteBinding

class NoteAdapter(private val onClickHandler: OnClickHandler) :
    ListAdapter<NoteWithTags, NoteAdapter.ViewHolder>(NoteWithTags.DiffCallBack) {

    private var multiSelect = false
    private var selectedItems = mutableListOf<NoteWithTags>()

    internal fun getSelectedItems(): List<NoteWithTags> {
        return selectedItems
    }

    fun numberOfSelectedItems(): Int {
        return selectedItems.size
    }

    fun toggleSelectAll() {
        if (currentList.size == selectedItems.size) {
            selectedItems.clear()
        } else {
            selectedItems = currentList.toMutableList()
        }
        notifyDataSetChanged()
    }

    // helper function that adds/removes an item to the list depending on the app's state
    private fun selectItem(holder: ViewHolder, item: NoteWithTags) {

        if (selectedItems.contains(item)) {
            selectedItems.remove(item)
            if (holder.itemView is Checkable) {
                holder.itemView.isChecked = false
            }
        } else {
            selectedItems.add(item)
            if (holder.itemView is Checkable) {
                holder.itemView.isChecked = true
            }
        }
    }

    fun finishSelection() {
        multiSelect = false
        selectedItems.clear()
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val noteWithTags = getItem(position)
        holder.bind(noteWithTags)

        holder.itemView.setOnClickListener {
            if (multiSelect) {
                selectItem(holder, noteWithTags)
                onClickHandler.onMultipleSelect(noteWithTags)
            } else {
                onClickHandler.onClick(noteWithTags)
            }
        }

        holder.itemView.setOnLongClickListener {
            if (!multiSelect) {
                multiSelect = true
                selectItem(holder, noteWithTags)
                onClickHandler.onMultipleSelect(noteWithTags)
            }
            return@setOnLongClickListener true
        }

        if (holder.itemView is Checkable) {
            holder.itemView.isChecked = selectedItems.contains(noteWithTags)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(noteWithTags: NoteWithTags) {
            when (binding) {
                is ItemNoteBinding -> {
                    binding.apply {
                        note = noteWithTags
                    }
                }
                // TODO: binding grid item
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                return ViewHolder(ItemNoteBinding.inflate(LayoutInflater.from(parent.context)))
            }
        }

    }
}

interface OnClickHandler {
    fun onClick(note: NoteWithTags)
    fun onMultipleSelect(note: NoteWithTags): Boolean
}