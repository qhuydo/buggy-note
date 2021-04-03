package com.hcmus.clc18se.buggynote.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hcmus.clc18se.buggynote.data.Tag
import com.hcmus.clc18se.buggynote.databinding.ItemTagBinding

class TagAdapter(val onItemEditorFocusListener: ItemEditorFocusListener) : ListAdapter<Tag, TagAdapter.ViewHolder>(Tag.DiffCallBack) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tag = getItem(position)
        holder.bind(tag)
        holder.setOnFocusListenerForEditor(onItemEditorFocusListener, tag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder(val binding: ItemTagBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tag: Tag) {
            binding.tag = tag
        }

        fun setOnFocusListenerForEditor(onFocusEditorFocusListener: ItemEditorFocusListener, tag: Tag) {
            binding.tagContent.setOnFocusChangeListener { v, hasFocus ->
                onFocusEditorFocusListener.onFocus(binding, hasFocus, tag)
            }
        }

        companion object {

            fun from(parent: ViewGroup): ViewHolder {
                val binding = ItemTagBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                )
                return ViewHolder(binding)
            }
        }
    }
}

class ItemEditorFocusListener(private val focusListener: (binding: ItemTagBinding, hasFocus: Boolean, tag: Tag) -> Unit) {
    fun onFocus(binding: ItemTagBinding, hasFocus: Boolean, tag: Tag) = focusListener(binding, hasFocus, tag)
}