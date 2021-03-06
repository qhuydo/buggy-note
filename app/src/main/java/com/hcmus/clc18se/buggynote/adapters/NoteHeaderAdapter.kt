package com.hcmus.clc18se.buggynote.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hcmus.clc18se.buggynote.R
import com.hcmus.clc18se.buggynote.databinding.ItemNoteHeaderBinding

// stupid me doing some stupid things
class NoteHeaderAdapter(
        private val title: String,
        private val iconRes: Int?,
        private val visibility: LiveData<Int>?
) : RecyclerView.Adapter<NoteHeaderAdapter.ViewHolder>() {

    private var heightWhenVisible: Int? = null

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)

        val layoutParams = holder.itemView.layoutParams
        when (layoutParams) {
            is StaggeredGridLayoutManager.LayoutParams -> {
                layoutParams.isFullSpan = true
            }
            is GridLayoutManager -> layoutParams.spanCount =
                    holder.itemView.resources.getInteger(R.integer.note_item_span_count_list)
        }

        if (visibility?.value == View.GONE) {
            if (heightWhenVisible == null) {
                heightWhenVisible = layoutParams.height
            }
            layoutParams.height = 0
        } else {
            heightWhenVisible?.let {
                layoutParams.height = it
            }
        }
    }

    override fun getItemCount() = 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(title, iconRes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                ItemNoteHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemViewType(position: Int) = R.layout.item_note_header

    class ViewHolder(val binding: ItemNoteHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String, @DrawableRes iconRes: Int?) {
            val drawable = iconRes?.let {
                ResourcesCompat.getDrawable(
                        binding.root.resources,
                        iconRes,
                        binding.root.context.theme
                ).also { it?.setBounds(0, 0, 0, 0) }
            }

            binding.header.apply {
                text = title
                setCompoundDrawablesRelative(
                        drawable,
                        null,
                        null,
                        null
                )
            }

        }
    }
}