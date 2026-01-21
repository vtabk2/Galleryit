package com.codebasetemplate.features.feature_demo_frame_mvvm.frame_list.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codebasetemplate.core.base_ui.model.AdItemUI
import com.codebasetemplate.features.feature_demo_frame_mvvm.frame_list.repository.FrameListRepository
import com.core.baseui.BaseItemUI
import com.core.utilities.result.PageUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FrameListViewModel @Inject constructor(
    private val frameListRepository: FrameListRepository
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 20
        private const val POSITION_AD = 4
    }

    // UI State
    private val _items = MutableStateFlow<PageUiState<List<BaseItemUI>>>(PageUiState.StandBy)
    val items: StateFlow<PageUiState<List<BaseItemUI>>> = _items.asStateFlow()

    private var _isLoading: Boolean = false

    private var isCanLoadMore = true
    private var isFirstLoad = true

    init {
        observeFrameItems()
    }

    private fun observeFrameItems() {
        // Always observe DB updates
        viewModelScope.launch {
            _isLoading = true
            _items.emit(PageUiState.FirstLoading)

            launch(Dispatchers.IO) {
                frameListRepository.getFrameItems()
                    .map { result ->
                        result.map { frameModels ->
                            frameModels
                                .toMutableList<BaseItemUI>().apply {
                                insertAdIfNeeded(this)
                            }
                        }
                    }.collect { result ->
                        val currentData = result.getOrNull().takeIf { it?.isNotEmpty() == true }
                        if (currentData != null) {
                            _items.emit(
                                PageUiState.Content(
                                    data = currentData,
                                    canLoadMore = isCanLoadMore
                                )
                            )
                        } else if (isFirstLoad) { // Only show first loading if it's indeed the first load and DB is empty
                            _items.emit(PageUiState.FirstLoading)
                        }
                        _isLoading = false

                        // On first load, trigger API fetch if DB empty
                        if (isFirstLoad) {
                            loadMoreFrameItems()
                            isFirstLoad = false
                        }
                    }
            }
        }
    }

    fun loadMoreFrameItems() {
        if (_isLoading || !isCanLoadMore) return

        _isLoading = true
        viewModelScope.launch {
            _items.emit(_items.value.getLoading())
            launch(Dispatchers.IO) {
                delay(3000)
                val currentItemCount = (_items.value as? PageUiState.Content)?.data?.size ?: 0
                val nextPageToLoad = (currentItemCount / PAGE_SIZE) + 1
                try {
                    frameListRepository.loadFrameItems(nextPageToLoad, PAGE_SIZE)
                        .collect { result ->
                            result.onSuccess { newItems ->
                                if (newItems.isEmpty()) {
                                    isCanLoadMore = false
                                }
                            }.onFailure {
                                val content = items.value as? PageUiState.Content
                                if (content != null && content.data.isNotEmpty()) {
                                    _items.emit(
                                        PageUiState.Content(
                                            data = content.data,
                                            loadMoreError = it,
                                            canLoadMore = isCanLoadMore
                                        )
                                    )
                                } else {
                                    _items.emit(
                                        PageUiState.FirstLoadError(it)
                                    )
                                }
                            }

                        }
                } finally {
                    _isLoading = false
                }
            }
        }
    }

    fun retry() {
        viewModelScope.launch {
            if (!_isLoading && isCanLoadMore) {
                val content = items.value as? PageUiState.Content
                if (content != null && content.data.isNotEmpty()) {
                    _items.emit(
                        PageUiState.Content(
                            data = content.data,
                            isLoadingMore = true,
                            canLoadMore = isCanLoadMore
                        )
                    )
                } else {
                    _items.emit(
                        PageUiState.FirstLoading
                    )
                }
                loadMoreFrameItems()
            }
        }
    }

    private fun insertAdIfNeeded(items: MutableList<BaseItemUI>) {
        if (items.size > POSITION_AD && items.none { it is AdItemUI }) {
            items.add(POSITION_AD, AdItemUI())
        }
    }
}