package com.codebasetemplate.utils.glide.thumb

import android.content.Context
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import java.io.InputStream

class UriModelLoader(private val context: Context) : ModelLoader<CacheThumbnail, InputStream> {
    override fun buildLoadData(image: CacheThumbnail, width: Int, height: Int, options: Options): ModelLoader.LoadData<InputStream> {
        return ModelLoader.LoadData(ObjectKey(image), UriDataFetcher(context, image))
    }

    override fun handles(image: CacheThumbnail): Boolean {
        return true
    }

    class Factory(private val context: Context) : ModelLoaderFactory<CacheThumbnail, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<CacheThumbnail, InputStream> {
            return UriModelLoader(context)
        }

        override fun teardown() {
            // Do nothing.
        }
    }
}