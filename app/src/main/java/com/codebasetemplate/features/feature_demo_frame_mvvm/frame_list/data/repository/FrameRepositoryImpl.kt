package com.codebasetemplate.features.feature_demo_frame_mvvm.frame_list.data.repository

import com.codebasetemplate.core.data.local.VersionDataPrefers
import com.codebasetemplate.core.data.remote.api.ApiService
import com.codebasetemplate.features.feature_demo_frame_mvvm.frame_detail.repository.FrameDetailRepository
import com.codebasetemplate.features.feature_demo_frame_mvvm.frame_list.repository.FrameListRepository
import com.codebasetemplate.shared.frame.data.local.dao.FrameDao
import com.codebasetemplate.shared.frame.data.mapper.FrameDtoToEntityMapper
import com.codebasetemplate.shared.frame.data.mapper.FrameDtoToModelMapper
import com.codebasetemplate.shared.frame.data.mapper.FrameEntityToModelMapper
import com.codebasetemplate.shared.frame.ui.model.FrameModel
import com.core.utilities.util.Timber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.math.max

class FrameRepositoryImpl @Inject constructor(
    private val frameDao: FrameDao,
    private val apiService: ApiService,
    private val versionDataPrefers: VersionDataPrefers,
    private val frameEntityToModelMapper: FrameEntityToModelMapper,
    private val frameDtoToModelMapper: FrameDtoToModelMapper,
    private val frameDtoToEntityMapper: FrameDtoToEntityMapper,
) : FrameListRepository, FrameDetailRepository {

    override suspend fun getFrameItems(): Flow<Result<List<FrameModel>>> = flow {
        frameDao.getFrameItems().collect {
            emit(Result.success(it.map { frame -> frameEntityToModelMapper.map(frame) }))
        }
    }

    override suspend fun loadFrameItems(
        page: Int,
        pageSize: Int
    ): Flow<Result<List<FrameModel>>> = flow {
        runCatching {
            var _page = page
            if (isCacheExpired()) {
                _page = 0
            }
            val offset = max(0, pageSize * _page - 1)
            Timber.Forest.d("_page: ${_page} offset $offset")
            apiService.getFrameItems(
                categoryId = "",//All
                limit = pageSize,
                offset = offset
            )
        }.onSuccess { response ->
            Timber.Forest.d("response: ${response.results?.size}")
            if (isCacheExpired()) {
                frameDao.deleteAll()
                setCacheUpdated()
            }
            val results = response.results
            val items = response.results?.map {
                frameDtoToModelMapper.map(it)
            }
            frameDao.insertAll(results?.map {
                frameDtoToEntityMapper.map(it)
            } ?: emptyList())
            emit(Result.success(items ?: emptyList()))
        }.onFailure {
            Timber.Forest.d("onFailure: ${it.message}")
            emit(Result.failure(exception = it))
        }
    }

    override suspend fun getFrameById(string: String): Result<FrameModel> {
        val frame = frameDao.getFrameById(string)
        return if(frame != null){
            Result.success(frameEntityToModelMapper.map(frame))
        }else{
            Result.failure(exception = Exception("Frame not found"))
        }
    }

    /**Check cache expired or not, version data from server update */
    private fun isCacheExpired(): Boolean {
        return !versionDataPrefers.versionFrameUpdated
    }

    /**Set cache updated */
    private fun setCacheUpdated() {
        versionDataPrefers.versionFrameUpdated = true
    }

}