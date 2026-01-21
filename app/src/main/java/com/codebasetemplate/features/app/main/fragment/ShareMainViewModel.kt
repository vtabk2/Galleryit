package com.codebasetemplate.features.app.main.fragment

import androidx.lifecycle.viewModelScope
import com.codebasetemplate.features.app.base.viewmodel.BaseSharedViewModel
import com.codebasetemplate.utils.load.LoadImageDataUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShareMainViewModel : BaseSharedViewModel() {
    private val _albumListFlow = MutableStateFlow<MutableList<LoadImageDataUtils.Album>>(mutableListOf())
    val albumListFlow: StateFlow<MutableList<LoadImageDataUtils.Album>> = _albumListFlow

    private val _albumDetailFlow = MutableStateFlow<LoadImageDataUtils.AlbumDetail?>(null)
    val albumDetailFlow: StateFlow<LoadImageDataUtils.AlbumDetail?> = _albumDetailFlow

    fun updateAlbumList(albumList: MutableList<LoadImageDataUtils.Album>) {
        viewModelScope.launch(Dispatchers.IO) {
            _albumListFlow.emit(albumList)
        }
    }

    fun updateAlbumDetail(albumDetail: LoadImageDataUtils.AlbumDetail?) {
        viewModelScope.launch(Dispatchers.IO) {
            _albumDetailFlow.emit(albumDetail)
        }
    }
}