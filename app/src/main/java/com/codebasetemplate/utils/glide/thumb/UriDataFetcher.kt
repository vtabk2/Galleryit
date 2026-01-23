package com.codebasetemplate.utils.glide.thumb

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class UriDataFetcher(private val context: Context, private val model: CacheThumbnail) : DataFetcher<InputStream> {

    private var stream: ByteArrayInputStream? = null

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        callback.onDataReady(fallback(model))
    }

    override fun cleanup() {
        try {
            stream?.close()
        } catch (ignore: IOException) {
            ignore.printStackTrace()
        }
    }

    override fun cancel() {
        // cannot cancel
    }

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }

    private fun fallback(model: CacheThumbnail): InputStream? {
        val bitmapOptions: BitmapFactory.Options = BitmapFactory.Options()
        bitmapOptions.inJustDecodeBounds = true
        val bos = ByteArrayOutputStream()

        val inputStream = try {
            model.uri?.let {
                val contentResolver = context.contentResolver
                BitmapFactory.decodeFile(model.path, bitmapOptions)
                val ratio = if (bitmapOptions.outWidth > bitmapOptions.outHeight) {
                    bitmapOptions.outWidth / bitmapOptions.outHeight.toFloat()
                } else {
                    bitmapOptions.outHeight / bitmapOptions.outWidth.toFloat()
                }
                val thumbnail: Bitmap? = if (ratio > 3f) {
                    bitmapOptions.inJustDecodeBounds = false
                    BitmapFactory.decodeFile(model.path, bitmapOptions)
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentResolver.loadThumbnail(it, Size(512, 512), null)
                    } else {
                        model.mediaId?.let { mediaId ->
                            val rotate = getExifOrientation(model.path)

                            val cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(contentResolver, mediaId, MediaStore.Images.Thumbnails.MINI_KIND, null)
                            val bitmap: Bitmap? = if (cursor.moveToFirst()) {
                                val columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                                val path = cursor.getString(columnIndexData)
                                cursor.close()
                                BitmapFactory.decodeFile(path) ?: MediaStore.Images.Thumbnails.getThumbnail(contentResolver, mediaId, MediaStore.Images.Thumbnails.MINI_KIND, null)
                            } else {
                                MediaStore.Images.Thumbnails.getThumbnail(contentResolver, mediaId, MediaStore.Images.Thumbnails.MINI_KIND, null)
                            }
                            if (bitmap == null) {
                                null
                            } else {
                                if (rotate != 0) {
                                    val matrix = Matrix().apply { postRotate(rotate.toFloat()) }
                                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                                } else {
                                    bitmap
                                }
                            }
                        }
                    }
                }
                if (thumbnail == null) {
                    null
                } else {
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 95 /*ignored for PNG*/, bos)
                    val bitmapByteArray: ByteArray = bos.toByteArray()
                    thumbnail.recycle()
                    stream = ByteArrayInputStream(bitmapByteArray)
                    stream
                }
            } ?: run {
                null
            }
        } catch (ex: Exception) {
            null
        }
        if (inputStream == null) {
            bitmapOptions.inJustDecodeBounds = false
            val thumbnail = BitmapFactory.decodeFile(model.path, bitmapOptions)
            try {
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 95 /*ignored for PNG*/, bos)
                val bitmapByteArray: ByteArray = bos.toByteArray()
                thumbnail.recycle()
                stream = ByteArrayInputStream(bitmapByteArray)
            } catch (e: Exception) {
                e.printStackTrace()
                model.isFailed = true
            }
        }
        return stream
    }

    private fun getExifOrientation(filepath: String): Int {
        var degree = 0
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(filepath)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        if (exif != null) {
            val orientation: Int = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
            if (orientation != -1) {
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                }
            }
        }
        return degree
    }
}