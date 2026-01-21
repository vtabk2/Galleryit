package com.codebasetemplate.shared.frame.data.mapper

import com.codebasetemplate.mapper.Mapper
import com.codebasetemplate.shared.frame.data.local.entity.FrameEntity
import com.codebasetemplate.shared.frame.data.remote.dto.FrameItemDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FrameDtoToEntityMapper @Inject constructor(): Mapper<FrameItemDto, FrameEntity> {
    override fun map(model: FrameItemDto): FrameEntity {
        return FrameEntity(
            id = model.id ?: "" ,
            name = model.name ?: "",
            imageUrl = model.imageUrl ?: "",
            imageThumbnailUrl = model.imageThumbnailUrl ?: "",
            rawImageThumbnailUrl = model.raw_image_thumbnail_url ?: "",
            dateModifier = System.currentTimeMillis()
        )
    }
}