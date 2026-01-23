package com.codebasetemplate.utils.load

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import com.codebasetemplate.R
import com.codebasetemplate.utils.MediaHelper
import com.codebasetemplate.utils.extensions.config
import com.codebasetemplate.utils.extensions.hasPermission
import com.codebasetemplate.utils.glide.thumb.CacheThumbnail
import com.codebasetemplate.utils.glide.thumb.MediaType
import java.io.File

object LoadImageDataUtils {

    private const val CAMERA_FOLDER = "Camera"
    private const val GOOGLE_PHOTOS = "Google Photos"
    const val GOOGLE_PHOTOS_PACKAGE_NAME = "com.google.android.apps.photos"

    var CAMERA_FOLDER_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + File.separator + CAMERA_FOLDER


    private val imageUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    private val videoUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    }

    fun getAlbumList(context: Context, callback: (AlbumDetail) -> Unit): MutableList<Album> {
        val albumMap = hashMapOf<String, Album>()
        val recentAlbum = Album(albumName = context.getString(R.string.text_recent))

        scanImageAlbums(context, albumMap, recentAlbum)
        scanVideoAlbums(context, albumMap, recentAlbum)

        val albumList = albumMap.values.toMutableList()
        albumList.sortWith { o1, o2 ->
            o1.albumName.compareTo(o2.albumName, true)
        }

        if (context.hasPermission(hasFull = true)) {
            albumList.add(
                0, Album(
                    albumName = GOOGLE_PHOTOS,
                    albumIcon = R.drawable.ic_google_photos,
                    isGooglePhotos = true
                )
            )
        }

        albumList.add(0, recentAlbum)

        if (albumList.isNotEmpty()) {
            if (TextUtils.isEmpty(context.config.albumId)) {
                context.config.albumId = albumList[0].albumId
            }

            val index = albumList.indexOfFirst { it.albumId == context.config.albumId }.coerceAtLeast(0)

            albumList[index].isSelected = true

            callback(
                getDetailList(
                    context,
                    albumList[index].albumId,
                    albumList[index].albumName,
                    false
                )
            )
        }

        return albumList
    }

    fun getDetailList(
        context: Context,
        albumId: String,
        albumName: String,
        reload: Boolean
    ): AlbumDetail {

        val list = mutableListOf<CacheThumbnail>()
        list.addAll(loadImages(context, albumId))
        list.addAll(loadVideos(context, albumId))

        list.sortByDescending { File(it.path).lastModified() }

        return AlbumDetail(albumName, reload, list)
    }

    private fun loadImages(context: Context, albumId: String): List<CacheThumbnail> {
        val list = mutableListOf<CacheThumbnail>()

        val projection = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID
        )

        val selection = buildString {
            append("${MediaStore.MediaColumns.MIME_TYPE} LIKE ?")
            append(" AND ${MediaStore.Images.Media.SIZE} > 0")
            if (albumId.isNotEmpty()) {
                append(" AND ${MediaStore.Images.Media.BUCKET_ID} = ?")
            }
        }

        val args = mutableListOf("image/%").apply {
            if (albumId.isNotEmpty()) add(albumId)
        }

        context.contentResolver.query(
            imageUri,
            projection,
            selection,
            args.toTypedArray(),
            MediaStore.Images.Media.DATE_MODIFIED + " DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val path = cursor.getString(0)
                if (!MediaHelper.isSupportImage(path)) continue

                val id = cursor.getLong(1)
                list.add(
                    CacheThumbnail(
                        path = path,
                        uri = ContentUris.withAppendedId(imageUri, id),
                        mediaId = id,
                        mediaType = MediaType.IMAGE
                    )
                )
            }
        }
        return list
    }

    private fun loadVideos(context: Context, albumId: String): List<CacheThumbnail> {
        val list = mutableListOf<CacheThumbnail>()

        val projection = arrayOf(
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.BUCKET_ID
        )

        val selection = buildString {
            append("${MediaStore.MediaColumns.MIME_TYPE} LIKE ?")
            append(" AND ${MediaStore.Video.Media.SIZE} > 0")
            if (albumId.isNotEmpty()) {
                append(" AND ${MediaStore.Video.Media.BUCKET_ID} = ?")
            }
        }

        val args = mutableListOf("video/%").apply {
            if (albumId.isNotEmpty()) add(albumId)
        }

        context.contentResolver.query(
            videoUri,
            projection,
            selection,
            args.toTypedArray(),
            MediaStore.Video.Media.DATE_MODIFIED + " DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val path = cursor.getString(0)
                if (!MediaHelper.isSupportVideo(path)) continue

                val id = cursor.getLong(1)
                val duration = cursor.getLong(2)

                list.add(
                    CacheThumbnail(
                        path = path,
                        uri = ContentUris.withAppendedId(videoUri, id),
                        mediaId = id,
                        mediaType = MediaType.VIDEO,
                        duration = duration
                    )
                )
            }
        }
        return list
    }

    private fun scanImageAlbums(
        context: Context,
        albumMap: HashMap<String, Album>,
        recent: Album
    ) {
        val projection = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        context.contentResolver.query(
            imageUri,
            projection,
            "${MediaStore.MediaColumns.MIME_TYPE} LIKE ? AND ${MediaStore.Images.Media.SIZE} > 0",
            arrayOf("image/%"),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val path = cursor.getString(0) ?: continue
                if (!MediaHelper.isSupportImage(path)) continue

                val bucketId = cursor.getString(1) ?: continue
                val bucketName = cursor.getString(2) ?: "No Name"

                val album = albumMap.getOrPut(bucketId) {
                    Album(albumId = bucketId, albumName = bucketName, imgUrl = path)
                }

                album.imageCount++
                recent.imageCount++
                if (recent.imgUrl.isEmpty()) recent.imgUrl = path
            }
        }
    }

    private fun scanVideoAlbums(
        context: Context,
        albumMap: HashMap<String, Album>,
        recent: Album
    ) {
        val projection = arrayOf(
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )

        context.contentResolver.query(
            videoUri,
            projection,
            "${MediaStore.MediaColumns.MIME_TYPE} LIKE ? AND ${MediaStore.Video.Media.SIZE} > 0",
            arrayOf("video/%"),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val path = cursor.getString(0) ?: continue
                if (!MediaHelper.isSupportVideo(path)) continue

                val bucketId = cursor.getString(1) ?: continue
                val bucketName = cursor.getString(2) ?: "No Name"

                val album = albumMap.getOrPut(bucketId) {
                    Album(albumId = bucketId, albumName = bucketName, imgUrl = path)
                }

                album.videoCount++
                recent.videoCount++
                if (recent.imgUrl.isEmpty()) recent.imgUrl = path
            }
        }
    }

    data class AlbumDetail(
        val albumName: String,
        val reload: Boolean,
        val detailList: MutableList<CacheThumbnail>
    )

    data class Album(
        var albumId: String = "",
        var albumName: String = "",
        var imgUrl: String = "",
        var albumIcon: Int = 0,
        var isSelected: Boolean = false,
        var isCamera: Boolean = false,
        var isGooglePhotos: Boolean = false,
        var imageCount: Int = 0,
        var videoCount: Int = 0
    ) {
        val totalCount: Int
            get() = imageCount + videoCount
    }
}
