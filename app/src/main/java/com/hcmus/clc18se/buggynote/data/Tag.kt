package com.hcmus.clc18se.buggynote.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tag")
data class Tag(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name="tag_id", index = true)
        val id: Long,

        var name: String?
        // private val position: Int
)