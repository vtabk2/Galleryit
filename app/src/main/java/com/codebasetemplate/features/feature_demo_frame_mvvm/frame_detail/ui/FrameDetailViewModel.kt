package com.codebasetemplate.features.feature_demo_frame_mvvm.frame_detail.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codebasetemplate.features.feature_demo_frame_mvvm.common.FrameConstants
import com.codebasetemplate.features.feature_demo_frame_mvvm.frame_detail.repository.FrameDetailRepository
import com.codebasetemplate.shared.frame.ui.model.FrameModel
import com.core.utilities.result.ObjectUiState
import com.core.utilities.result.mapToObjectUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FrameDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val frameDetailRepository: FrameDetailRepository,
) : ViewModel() {
    private val frameId: String? = savedStateHandle.get<String>(FrameConstants.EXTRA_FRAME_ID)
    private val _frameDetail = MutableStateFlow<ObjectUiState<FrameModel>>(ObjectUiState.StandBy)
    val frameDetail: StateFlow<ObjectUiState<FrameModel>> = _frameDetail

    init {
        viewModelScope.launch() {
            _frameDetail.value = ObjectUiState.Loading
            launch(Dispatchers.IO) {
                _frameDetail.value = frameDetailRepository.getFrameById(frameId ?: "").mapToObjectUiState()
            }
        }
    }

}