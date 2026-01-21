package com.codebasetemplate.features.app.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SelectedImageViewModel : ViewModel() {

    fun loadAlbum() {
        Log.d("TAG5", "SelectedImageViewModel_loadAlbum: ")
        viewModelScope.launch(Dispatchers.IO) {

        }
    }
}