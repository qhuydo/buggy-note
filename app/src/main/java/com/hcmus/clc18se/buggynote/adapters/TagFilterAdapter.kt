package com.hcmus.clc18se.buggynote.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hcmus.clc18se.buggynote.data.Tag
import com.hcmus.clc18se.buggynote.databinding.ItemTagFilterBinding

class TagFilterAdapter(private val onCheckedChangedListener: ItemOnCheckedChangeListener) : ListAdapter<Tag, TagFilterAdapter.ViewHolder>(Tag.DiffCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.binding.tagFilter.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangedListener.onCheckedChanged(isChecked, item)
        }
    }

    class ViewHolder(internal val binding: ItemTagFilterBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tag: Tag) {
            binding.tag = tag
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val binding = ItemTagFilterBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                )
                return ViewHolder(binding)
            }
        }
    }
}

class ItemOnCheckedChangeListener(private val onCheckedChangedListener: (isChecked: Boolean, tag: Tag) -> Unit) {
    fun onCheckedChanged(isCheked: Boolean, tag: Tag) = onCheckedChangedListener(isCheked, tag)
}