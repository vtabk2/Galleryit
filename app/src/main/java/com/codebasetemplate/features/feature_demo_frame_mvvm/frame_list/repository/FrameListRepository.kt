package com.codebasetemplate.features.feature_demo_frame_mvvm.frame_list.repository

import com.codebasetemplate.shared.frame.ui.model.FrameModel
import kotlinx.coroutines.flow.Flow

interface FrameListRepository {
    suspend fun getFrameItems(): Flow<Result<List<FrameModel>>>
    suspend fun loadFrameItems(page: Int, pageSize: Int): Flow<Result<List<FrameModel>>>
}