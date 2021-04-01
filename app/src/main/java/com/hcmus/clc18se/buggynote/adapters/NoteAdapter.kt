package com.hcmus.clc18se.buggynote.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hcmus.clc18se.buggynote.data.NoteWithTags
import com.hcmus.clc18se.buggynote.databinding.ItemNoteInListBinding
import com.hcmus.clc18se.buggynote.utils.convertLongToDateString

class NoteAdapter(private val onClickListener: OnClickListener = OnClickListener {}) :
    ListAdapter<NoteWithTags, NoteAdapter.ViewHolder>(NoteWithTags.DiffCallBack) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val noteWithTags = getItem(position)
        holder.bind(noteWithTags)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(noteWithTags)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(noteWithTags: NoteWithTags) {
            when (binding) {
                is ItemNoteInListBinding -> {
                    binding.apply {
                        title.text = noteWithTags.note.title
                        noteContent.text = noteWithTags.note.noteContent
                        timeStamp.text = convertLongToDateString(noteWithTags.note.lastModify)
                    }
                }
                // TODO: binding grid item
            }
        }

        companion object {
            fun from(
                parent: ViewGroup
            ): ViewHolder {
                return ViewHolder(ItemNoteInListBinding.inflate(LayoutInflater.from(parent.context)))
            }
        }

    }
}

class OnClickListener(private val clickListener: (note: NoteWithTags) -> Unit) {
    fun onClick(note: NoteWithTags) = clickListener(note)
}