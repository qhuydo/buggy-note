package com.hcmus.clc18se.buggynote.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.hcmus.clc18se.buggynote.R
import com.hcmus.clc18se.buggynote.data.NoteWithTags
import com.hcmus.clc18se.buggynote.data.Tag
import com.hcmus.clc18se.buggynote.databinding.TagChipBinding
import com.hcmus.clc18se.buggynote.utils.TextFormatter
import com.hcmus.clc18se.buggynote.utils.TextFormatter.Companion.TYPEFACE_MONOSPACE
import com.hcmus.clc18se.buggynote.utils.TextFormatter.Companion.TYPEFACE_SANS_SERIF
import com.hcmus.clc18se.buggynote.utils.TextFormatter.Companion.TYPEFACE_SERIF
import com.hcmus.clc18se.buggynote.utils.convertLongToDateString
import timber.log.Timber

@BindingAdapter("loadNotes")
fun RecyclerView.loadNotesFromNoteAdapter(notes: List<NoteWithTags>?) {
    notes?.let {
        if (this.adapter is NoteAdapter) {
            (adapter as NoteAdapter).submitList(notes)
        }
    }
}

@BindingAdapter(value = ["pinnedNotes", "unpinnedNotes"], requireAll = false)
fun RecyclerView.loadNotes(pinnedNotes: List<NoteWithTags>?, unpinnedNotes: List<NoteWithTags>?) {
    if (this.adapter !is ConcatAdapter) {
        Timber.w("Use the concat adapter to load 2 note lists")
        return
    }

    val adapters = (this.adapter as ConcatAdapter).adapters
    if (adapters.isEmpty()) {
        Timber.w("ConcatAdapter does not contain any NoteAdapter")
        return
    }

    pinnedNotes?.takeIf { it.isNotEmpty() }.let { (adapters[PINNED_POSITION] as NoteAdapter).submitList(it) }

    unpinnedNotes?.let {
        (adapters[UNPINNED_POSITION] as NoteAdapter).submitList(it)
    }

//    if (pinnedNotes != null && unpinnedNotes != null) {
//        startLayoutAnimation()
//    }
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
    textView.text = text
}

@BindingAdapter(value = ["loadTagList", "chipLimit", "setOnClickToChips", "chipSmallText"], requireAll = false)
fun ChipGroup.setTags(tags: List<Tag>?, limit: Int?, onClickListener: View.OnClickListener?) {

    tags?.let { it ->
        removeAllViewsInLayout()
        invalidate()
        requestLayout()

        val maximumChip = limit.takeIf { limit != null && limit > 0 } ?: Int.MAX_VALUE

        for (i in (it.indices)) {
            if (i == maximumChip) {
                break
            }
            val chip = TagChipBinding.inflate(LayoutInflater.from(context), this, true).root as Chip

            chip.apply {
                text = if (i == maximumChip - 1 && (it.size - maximumChip) > 0) {
                    " ${it.size - maximumChip + 1}+ "
                } else {
                    it[i].name
                }
            }

            if (onClickListener != null) {
                chip.setOnClickListener(onClickListener)
            }
        }
    }
}


@BindingAdapter("placeholderVisibility")
fun <T> ViewGroup.setViewHolderVisibility(list: List<T>?) {
    if (list != null && list.isEmpty()) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.GONE
    }
}

@BindingAdapter("placeHolderEmoticon")
fun TextView.setPlaceHolderEmoticon(nothing: Nothing?) {
    this.text = this.context.resources.getStringArray(R.array.emoticons).random()
}


@BindingAdapter("noteContentFormat")
fun TextView.setNoteContentFormat(noteWithTags: NoteWithTags?) {
    noteWithTags?.let {
        val formatter = it.getContentFormat()
        setFormat(formatter)
    }
}

@BindingAdapter("noteTitleFormat")
fun TextView.setNoteTitleFormat(noteWithTags: NoteWithTags?) {
    noteWithTags?.let {
        val formatter = it.getTitleFormat()
        setFormat(formatter)
    }
}

fun TextView.setFormat(formatter: TextFormatter) {
    // get Typeface object from TYPEFACE_* flag
    val typeface = when (formatter.typefaceType) {
        TYPEFACE_SERIF -> Typeface.SERIF
        TYPEFACE_SANS_SERIF -> Typeface.SANS_SERIF
        TYPEFACE_MONOSPACE -> Typeface.MONOSPACE
        else -> null
    }

    textAlignment = TextView.TEXT_ALIGNMENT_GRAVITY
    gravity = formatter.gravity

    setTypeface(typeface, formatter.typefaceStyle)

    invalidate()
    requestLayout()
}