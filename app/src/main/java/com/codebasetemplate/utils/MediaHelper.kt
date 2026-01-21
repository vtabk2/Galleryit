package com.codebasetemplate.utils

object MediaHelper {
    val IMAGE_SUPPORT_FORMAT = arrayOf(
        ".jpg",
        ".jpeg",
        ".png",
        ".webp",
        ".gif",
        ".heic"
    )

    val VIDEO_SUPPORT_FORMAT = arrayOf(
        ".mp4",
        ".mkv",
        ".webm",
        ".3gp",
        ".mov",
        ".avi"
    )

    fun isSupportImage(path: String): Boolean {
        val lower = path.lowercase()
        return IMAGE_SUPPORT_FORMAT.any { lower.endsWith(it) }
    }

    fun isSupportVideo(path: String): Boolean {
        val lower = path.lowercase()
        return VIDEO_SUPPORT_FORMAT.any { lower.endsWith(it) }
    }
}