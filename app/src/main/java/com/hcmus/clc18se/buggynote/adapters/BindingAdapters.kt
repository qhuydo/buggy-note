package com.hcmus.clc18se.buggynote.adapters

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
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
fun loadTags(recyclerView: RecyclerView, tags: List<Tag>?) {
    tags?.let {
        val adapter = recyclerView.adapter as TagAdapter
        adapter.submitList(tags)
    }
}

@BindingAdapter("loadSelectableTags")
fun loadSelectableTags(recyclerView: RecyclerView, tags: List<Tag>?) {
    tags.let {
        val adapter = recyclerView.adapter as TagSelectionAdapter
        adapter.submitList(tags)
    }
}

@BindingAdapter("loadFilterTags")
fun loadFilterTags(recyclerView: RecyclerView, tags: List<Tag>?) {
    tags.let {
        val adapter = recyclerView.adapter as TagFilterAdapter
        adapter.submitList(tags)
    }
}


@BindingAdapter("timeStampFromLong")
fun setTimeStampFromLong(textView: TextView, value: Long) {
    val text = convertLongToDateString(value)
    textView.text = textView.context.resources.getString(R.string.last_edit, text)
}


@BindingAdapter(value = ["loadTagList", "chipLimit", "setOnClickToChips"], requireAll = false)
fun ChipGroup.setTags(tags: List<Tag>?, limit: Int?, onClickListener: View.OnClickListener?) {
    // TODO: refactor me
    tags?.let { it ->
        this.removeAllViewsInLayout()
        this.invalidate()
        this.requestLayout()

        val maximumChip = limit.takeIf { limit != null && limit > 0 } ?: Int.MAX_VALUE
        // maximumChip = if (it.size < maximumChip) it.size else maximumChip

        for (i in (it.indices)) {
            if (i == maximumChip) {
                break
            }

            val chip = Chip(this.context)
            val chipDrawable = ChipDrawable.createFromAttributes(this.context, null, 0, R.style.Theme_BuggyNote_Tag)
            chip.setChipDrawable(chipDrawable)
            chip.setTextAppearanceResource(R.style.TextAppearance_AppCompat_Caption)

            chip.text = if (i == maximumChip - 1 && (it.size - maximumChip) > 0) {
                " ${it.size - maximumChip + 1}+ "
            } else {
                it[i].name
            }

            this.addView(chip)

            if (onClickListener != null) {
                chip.setOnClickListener(onClickListener)
            }
        }
    }
}