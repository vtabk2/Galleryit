package com.codebasetemplate.features.app.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectedImageViewModel @Inject constructor(
    private val loadImageRepository: LoadImageRepository
) : ViewModel() {

    fun loadAlbum() {
        Log.d("TAG5", "SelectedImageViewModel_loadAlbum: ")
        viewModelScope.launch(Dispatchers.IO) {
            loadImageRepository.loadAlbum(callbackAlbumList = {
                Log.d("TAG5", "SelectedImageViewModel_loadAlbum: callbackAlbumList = $it")
            }, callbackDetailList = {

            })
        }
    }
}