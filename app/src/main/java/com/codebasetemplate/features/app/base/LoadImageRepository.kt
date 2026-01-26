package com.codebasetemplate.features.app.base

import android.content.Context
import android.net.Uri
import com.codebasetemplate.utils.FileManager
import com.codebasetemplate.utils.extensions.config
import com.codebasetemplate.utils.load.LoadImageDataUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class LoadImageRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private var isLoadingAlbum: AtomicBoolean = AtomicBoolean(false)

    fun loadAlbum(
        callbackAlbumList: (MutableList<LoadImageDataUtils.Album>) -> Unit,
        callbackDetailList: (LoadImageDataUtils.AlbumDetail) -> Unit
    ) {
        LoadImageDataUtils.getAlbumList(
            context,
            callbackAlbum = callbackAlbumList,
            callbackAlbumDetail = {
                isLoadingAlbum.set(false)
                callbackDetailList.invoke(it)
            })
    }

    fun loadAlbumDetail(albumId: String, albumName: String, reload: Boolean, callbackDetailList: (LoadImageDataUtils.AlbumDetail) -> Unit) {
        context.config.albumId = albumId
        callbackDetailList.invoke(LoadImageDataUtils.getDetailList(context, albumId, albumName, reload))
    }

    fun checkUriValidate(uri: Uri, callback: (path: String) -> Unit, callbackFailed: () -> Unit) {
        FileManager.checkUriValidate(context, uri, callback, callbackFailed)
    }
}