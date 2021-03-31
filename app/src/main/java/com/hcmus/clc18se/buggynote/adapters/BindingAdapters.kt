package com.hcmus.clc18se.buggynote.adapters

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hcmus.clc18se.buggynote.data.NoteWithTags

@BindingAdapter("loadNotes")
fun RecyclerView.loadNotes(notes: List<NoteWithTags>?) {
    notes?.let {
        if (this.adapter is NoteAdapter) {
            (adapter as NoteAdapter).submitList(notes)
        }
    }
}