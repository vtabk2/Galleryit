package com.codebasetemplate.features.app.base

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codebasetemplate.utils.load.LoadImageDataUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectedImageViewModel @Inject constructor(
    private val loadImageRepository: LoadImageRepository
) : ViewModel() {

    private val _albumListFlow = MutableStateFlow<MutableList<LoadImageDataUtils.Album>>(mutableListOf())
    val albumListFlow: SharedFlow<MutableList<LoadImageDataUtils.Album>> = _albumListFlow

    private val _detailFlow = MutableStateFlow<LoadImageDataUtils.AlbumDetail?>(null)
    val detailFlow: SharedFlow<LoadImageDataUtils.AlbumDetail?> = _detailFlow

    fun loadAlbum() {
        viewModelScope.launch(Dispatchers.IO) {
            loadImageRepository.loadAlbum(callbackAlbumList = {
                viewModelScope.launch(Dispatchers.IO) {
                    _albumListFlow.emit(it)
                }
            }, callbackDetailList = {
                viewModelScope.launch(Dispatchers.IO) {
                    _detailFlow.emit(it)
                }
            })
        }
    }

    fun loadAlbumDetail(albumId: String, albumName: String, reload: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            loadImageRepository.loadAlbumDetail(albumId, albumName, reload, callbackDetailList = {
                viewModelScope.launch(Dispatchers.IO) {
                    _detailFlow.emit(it)
                }
            })
        }
    }

    fun checkUriValidate(uri: Uri, callback: (path: String) -> Unit, callbackFailed: () -> Unit) {
        loadImageRepository.checkUriValidate(uri, callback, callbackFailed)
    }
}