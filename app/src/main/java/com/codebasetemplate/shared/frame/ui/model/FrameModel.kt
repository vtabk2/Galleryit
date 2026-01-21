package com.codebasetemplate.shared.frame.ui.model

import com.core.baseui.BaseItemUI

data class FrameModel(
    var id: String,
    var imageThumbnailUrl: String?,
    var rawImageThumbnailUrl: String?,
    var imageUrl: String?,
    var name: String?
): BaseItemUI() {
    override val identify: String
        get() = id
}