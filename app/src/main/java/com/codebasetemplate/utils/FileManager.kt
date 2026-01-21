package com.codebasetemplate.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object FileManager {
    private const val CROP = "crop"
    private const val GOOGLE_PHOTO = "google_photo"
    private const val DOWNLOAD = "download"

    fun getPathFromName(context: Context, name: String, isCrop: Boolean = true): String {
        val folder = context.getExternalFilesDir(if (isCrop) CROP else GOOGLE_PHOTO)
        folder?.let {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
        return File(folder, name).absolutePath
    }

    fun deleteCrop(context: Context) {
        try {
            val folder = context.getExternalFilesDir(CROP)
            folder?.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteGooglePhoto(context: Context) {
        try {
            val folder = context.getExternalFilesDir(GOOGLE_PHOTO)
            folder?.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkUriValidate(context: Context, uri: Uri, callback: (path: String) -> Unit, callbackFailed: () -> Unit) {
        try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.let {
                it.moveToFirst()
                val columnIndexData = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val path = it.getString(columnIndexData) ?: ""
                if (TextUtils.isEmpty(path)) {
                    checkPathValidate(context, uri, callback, callbackFailed)
                } else {
                    if (File(path).length().toDouble() == 0.0 || !MediaHelper.isSupportImage(path)) {
                        callbackFailed.invoke()
                    } else {
                        callback(path)
                    }
                }
                it.close()
            } ?: run {
                checkPathValidate(context, uri, callback, callbackFailed)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callbackFailed.invoke()
        }
    }

    private fun checkPathValidate(context: Context, uri: Uri, callback: (path: String) -> Unit, callbackFailed: () -> Unit) {
        val tempNewSaved = getImagePathFromInputStreamUri(context, uri)
        tempNewSaved?.let { pathNew ->
            if (TextUtils.isEmpty(pathNew)) {
                callbackFailed.invoke()
            } else {
                if (File(pathNew).length().toDouble() == 0.0 || !MediaHelper.isSupportImage(pathNew)) {
                    callbackFailed.invoke()
                } else {
                    callback(pathNew)
                }
            }
        }
    }

    private fun getImagePathFromInputStreamUri(context: Context, uri: Uri): String? {
        var inputStream: InputStream? = null
        var filePath: String? = null
        if (uri.authority != null) {
            try {
                inputStream = context.contentResolver.openInputStream(uri)
                val photoFile = createTemporalFileFrom(context, inputStream)
                filePath = photoFile?.path
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return filePath
    }

    @Throws(IOException::class)
    private fun createTemporalFileFrom(context: Context, inputStream: InputStream?): File? {
        var targetFile: File? = null
        if (inputStream != null) {
            var read: Int
            val buffer = ByteArray(8 * 1024)
            targetFile = createTemporalFile(context)
            val outputStream: OutputStream = FileOutputStream(targetFile)
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
            outputStream.flush()
            try {
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return targetFile
    }

    private fun createTemporalFile(context: Context): File {
        return File(getPathFromName(context, "google_photo_" + System.currentTimeMillis() + ".jpg", isCrop = false))
    }
}