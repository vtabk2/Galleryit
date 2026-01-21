package com.codebasetemplate.features.feature_demo_frame_mvvm.frame_detail.repository

import com.codebasetemplate.shared.frame.ui.model.FrameModel

interface FrameDetailRepository {
    suspend fun getFrameById(string: String): Result<FrameModel>
}