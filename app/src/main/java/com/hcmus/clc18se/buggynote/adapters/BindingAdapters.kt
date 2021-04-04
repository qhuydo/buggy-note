package com.hcmus.clc18se.buggynote.adapters

import android.widget.CheckBox
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hcmus.clc18se.buggynote.R
import com.hcmus.clc18se.buggynote.data.NoteWithTags
import com.hcmus.clc18se.buggynote.data.Tag
import com.hcmus.clc18se.buggynote.utils.convertLongToDateString

@BindingAdapter("loadNotes")
fun RecyclerView.loadNotes(notes: List<NoteWithTags>?) {
    notes?.let {
        if (this.adapter is NoteAdapter) {
            (adapter as NoteAdapter).submitList(notes)
        }
    }
}

@BindingAdapter("loadTags")
fun RecyclerView.loadTags(tags: List<Tag>?) {
    tags?.let {
        if (this.adapter is TagAdapter) {
            (adapter as TagAdapter).submitList(tags)
            this.adapter?.notifyDataSetChanged()
        }
    }
}

@BindingAdapter("timeStampFromLong")
fun setTimeStampFromLong(textView: TextView, value: Long) {
    val text = convertLongToDateString(value)
    textView.text = textView.context.resources.getString(R.string.last_edit, text)
}