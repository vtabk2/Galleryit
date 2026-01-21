package com.codebasetemplate.features.feature_demo_frame_mvvm.frame_detail.data.repository

import com.codebasetemplate.features.feature_demo_frame_mvvm.frame_detail.repository.FrameDetailRepository
import com.codebasetemplate.shared.frame.data.local.dao.FrameDao
import com.codebasetemplate.shared.frame.data.mapper.FrameEntityToModelMapper
import com.codebasetemplate.shared.frame.ui.model.FrameModel
import javax.inject.Inject

class FrameDetailRepositoryImpl @Inject constructor(
    private val frameDao: FrameDao,
    private val frameEntityToModelMapper: FrameEntityToModelMapper,
) : FrameDetailRepository {


    override suspend fun getFrameById(string: String): Result<FrameModel> {
        val frame = frameDao.getFrameById(string)
        return if (frame != null) {
            Result.success(frameEntityToModelMapper.map(frame))
        } else {
            Result.failure(exception = Exception("Frame not found"))
        }
    }

}