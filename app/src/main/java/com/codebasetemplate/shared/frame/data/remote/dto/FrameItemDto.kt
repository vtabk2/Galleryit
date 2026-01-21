package com.codebasetemplate.shared.frame.data.remote.dto


import com.google.gson.annotations.SerializedName

data class FrameItemDto(
    @SerializedName("applications")
    var applications: List<String>?,
    @SerializedName("id")
    var id: String?,
    @SerializedName("image_thumbnail_url")
    var imageThumbnailUrl: String?,
    @SerializedName("raw_image_thumbnail_url")
    var raw_image_thumbnail_url: String?,
    @SerializedName("image_url")
    var imageUrl: String?,
    @SerializedName("name")
    var name: String?
)