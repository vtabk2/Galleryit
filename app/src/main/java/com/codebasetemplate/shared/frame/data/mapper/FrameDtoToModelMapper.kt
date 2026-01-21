package com.codebasetemplate.shared.frame.data.mapper

import com.codebasetemplate.mapper.Mapper
import com.codebasetemplate.shared.frame.data.remote.dto.FrameItemDto
import com.codebasetemplate.shared.frame.ui.model.FrameModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FrameDtoToModelMapper @Inject constructor(): Mapper<FrameItemDto, FrameModel> {
    override fun map(model: FrameItemDto): FrameModel {
        return FrameModel(
            id = model.id ?: "" ,
            name = model.name,
            imageUrl = model.imageUrl,
            imageThumbnailUrl = model.imageThumbnailUrl,
            rawImageThumbnailUrl = model.raw_image_thumbnail_url
        )
    }
}