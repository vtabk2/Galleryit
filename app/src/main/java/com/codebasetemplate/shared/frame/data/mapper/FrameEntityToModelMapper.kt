package com.codebasetemplate.shared.frame.data.mapper

import com.codebasetemplate.mapper.Mapper
import com.codebasetemplate.shared.frame.data.local.entity.FrameEntity
import com.codebasetemplate.shared.frame.ui.model.FrameModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FrameEntityToModelMapper @Inject constructor(): Mapper<FrameEntity, FrameModel> {
    override fun map(model: FrameEntity): FrameModel {
        return FrameModel(
            id = model.id,
            name = model.name,
            imageUrl = model.imageUrl,
            imageThumbnailUrl = model.imageThumbnailUrl,
            rawImageThumbnailUrl = model.rawImageThumbnailUrl
        )
    }
}