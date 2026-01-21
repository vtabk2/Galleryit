package com.codebasetemplate.utils.glide.thumb

import android.net.Uri
import kotlin.math.abs

data class CacheThumbnail(
    var path: String = "",
    var uri: Uri? = null,
    var mediaId: Long? = null,
    var mediaType: MediaType = MediaType.IMAGE,
    var duration: Long = 0L, // chỉ dùng cho video
    var isFailed: Boolean = false
) {
    override fun hashCode(): Int {
        return abs(path.toByteArray().size + path.hashCode())
    }

    override fun equals(other: Any?): Boolean {
        val compare = other as? CacheThumbnail ?: return false
        return when {
            this.javaClass != compare.javaClass -> {
                false
            }

            path.isEmpty() -> {
                false
            }

            else -> {
                this.path == compare.path && path.toByteArray().size == compare.path.toByteArray().size
            }
        }
    }
}