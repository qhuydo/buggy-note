package com.hcmus.clc18se.buggynote.data

import androidx.recyclerview.widget.DiffUtil
import androidx.room.*

@Entity(tableName = "tag")
data class Tag(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "tag_id", index = true)
        var id: Long = 0,

        var name: String = "",

        @Ignore
        var selectState: Boolean = false
) {
    companion object {
        val DiffCallBack = object : DiffUtil.ItemCallback<Tag>() {
            override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean {
                return oldItem == newItem
            }
        }
    }
}