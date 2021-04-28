package com.hcmus.clc18se.buggynote.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hcmus.clc18se.buggynote.data.Tag
import com.hcmus.clc18se.buggynote.databinding.ItemTagSelectionBinding

class TagSelectionAdapter(
        private val onCheckedChangeListener: OnCheckedChangedListener,
) : ListAdapter<Tag, TagSelectionAdapter.ViewHolder>(Tag.DiffCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tag = getItem(position)
        holder.bind(tag)
        holder.binding.tagCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            onCheckedChangeListener.onCheckedChanged(buttonView, isChecked, tag)
        }
        holder.itemView.setOnClickListener {
            holder.binding.tagCheckBox.performClick()
        }
    }

    class ViewHolder(val binding: ItemTagSelectionBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tag: Tag) {
            binding.tag = tag
            binding.tagContent.isSelected = true
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val binding = ItemTagSelectionBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                )
                return ViewHolder(binding)
            }
        }
    }
}

class OnCheckedChangedListener(private val onCheckedChangedListener: (itemView: CompoundButton, isChecked: Boolean, tag: Tag) -> Unit) {
    fun onCheckedChanged(itemView: CompoundButton, isChecked: Boolean, tag: Tag) = onCheckedChangedListener(itemView, isChecked, tag)
}