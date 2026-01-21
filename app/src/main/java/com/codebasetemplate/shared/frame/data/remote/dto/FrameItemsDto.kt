package com.codebasetemplate.shared.frame.data.remote.dto


import com.google.gson.annotations.SerializedName

data class FrameItemsDto(
    @SerializedName("count")
    var count: Int?,
    @SerializedName("next")
    var next: String?,
    @SerializedName("previous")
    var previous: Any?,
    @SerializedName("results")
    var results: List<FrameItemDto>?
)