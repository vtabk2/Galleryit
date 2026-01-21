package com.codebasetemplate.required.firebase

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OnBoardingConfigModel (
    @Json(name = "version")
    var version: Int ?
)