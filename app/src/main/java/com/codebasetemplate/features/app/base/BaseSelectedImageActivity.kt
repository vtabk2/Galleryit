package com.codebasetemplate.features.app.base

import android.os.Bundle
import androidx.activity.viewModels
import androidx.viewbinding.ViewBinding

abstract class BaseSelectedImageActivity<VB : ViewBinding> : PermissionActivity<VB>() {

    val selectedImageViewModel: SelectedImageViewModel by viewModels()

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)

        checkPermission(hasFull = false, callback = { granted ->
            if (granted) {
                goToOtherHasWriteStoragePermission()
            }
        })
    }

    override fun goToOtherHasWriteStoragePermission() {
        selectedImageViewModel.loadAlbum()
    }
}