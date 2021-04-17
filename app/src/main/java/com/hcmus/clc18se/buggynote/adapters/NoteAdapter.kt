package com.hcmus.clc18se.buggynote.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Checkable
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.*
import com.hcmus.clc18se.buggynote.R
import com.hcmus.clc18se.buggynote.data.NoteWithTags
import com.hcmus.clc18se.buggynote.databinding.ItemNoteBinding
import timber.log.Timber
import java.util.*

// TODO: remove these dirty flags
// dirty flags
// position of the NoteAdapter containing a list of pinned notes in a ConcatAdapter
const val PINNED_POSITION = 1

// position of the NoteAdapter containing a list of unpinned notes in a ConcatAdapter
const val UNPINNED_POSITION = 3

const val PIN_TAG = "PIN"
const val UNPIN_TAG = "UNPIN"

class NoteAdapter(
    private val onClickHandler: OnClickHandler,
    val tag: String
) : ListAdapter<NoteWithTags, NoteAdapter.ViewHolder>(NoteWithTags.DiffCallBack) {

    private var multiSelect = false
    private var selectedItems = mutableSetOf<Long>()

    internal fun getSelectedItems(): List<NoteWithTags> {
        return currentList.filter { it.getId() in selectedItems }
    }

    fun numberOfSelectedItems(): Int {
        return selectedItems.size
    }

    // helper function that adds/removes an item to the list depending on the app's state
    private fun selectItem(holder: ViewHolder, item: NoteWithTags) {
        if (selectedItems.contains(item.getId())) {
            selectedItems.remove(item.getId())
            if (holder.itemView is Checkable) {
                holder.itemView.isChecked = false
            }
        } else {
            selectedItems.add(item.getId())
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

    fun selectAll() {
        multiSelect = true
        selectedItems.addAll(currentList.map { it.getId() })
        notifyDataSetChanged()
    }

    fun unSelectAll() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun enableSelection() {
        multiSelect = true
    }

    /**
     * Rearrange item in the note list when ItemTouchHelper wants to move the dragged item
     * from its old position to the new position.
     */
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Timber.d("$fromPosition to $toPosition")
        if (fromPosition == RecyclerView.NO_POSITION
            || toPosition == RecyclerView.NO_POSITION
            || toPosition >= currentList.size
        ) {
            return false
        }
        val items = currentList.toMutableList()
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
        submitList(items)
        onClickHandler.onPostReordered(items)
        return true
    }

    override fun getItemViewType(position: Int) = R.layout.item_note

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val noteWithTags = getItem(position)
        holder.tag = tag
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
            holder.itemView.isChecked = selectedItems.contains(noteWithTags.getId())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, tag)
    }

    class ViewHolder(
        private val binding: ViewDataBinding,
        var tag: String
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(noteWithTags: NoteWithTags) {
            when (binding) {
                is ItemNoteBinding -> {
                    binding.apply {
                        note = noteWithTags
                    }
                }
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup, tag: String): ViewHolder {
                return ViewHolder(
                    ItemNoteBinding.inflate(LayoutInflater.from(parent.context)),
                    tag
                )
            }
        }
    }
}

// TODO: change my name
interface OnClickHandler {
    fun onClick(note: NoteWithTags)
    fun onMultipleSelect(note: NoteWithTags): Boolean
    fun onPostReordered(notes: List<NoteWithTags>)
}

class NoteItemTouchHelperCallBack(private vararg val adapters: NoteAdapter) :
    ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN
                or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
        0
    ) {
    override fun isLongPressDragEnabled(): Boolean {
        // The drag action occurs only when at most one item in the note list has been selected.
        return adapters.sumBy { it.numberOfSelectedItems() } <= 1
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        (viewHolder as? NoteAdapter.ViewHolder)?.let { vh ->
            Timber.d(vh.tag)
            return adapters.firstOrNull { it.tag == vh.tag }?.onItemMove(
                viewHolder.bindingAdapterPosition, target.bindingAdapterPosition
            ) ?: false
        } ?: return false
    }
}