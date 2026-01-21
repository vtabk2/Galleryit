package com.codebasetemplate.shared.frame.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "frame_table")
data class FrameEntity (
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "image_thumbnail_url")
    val imageThumbnailUrl: String,

    @ColumnInfo(name = "raw_image_thumbnail_url")
    val rawImageThumbnailUrl: String,

    @ColumnInfo(name = "imageUrl")
    val imageUrl: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "date_modifier")
    val dateModifier: Long,
)
