package com.codebasetemplate.features.app.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.codebasetemplate.R
import com.codebasetemplate.databinding.ItemAlbumDetailBinding
import com.codebasetemplate.utils.MediaHelper
import com.codebasetemplate.utils.glide.thumb.CacheThumbnail
import com.core.utilities.visibleIf
import java.io.File

class AlbumDetailAdapter(
    private val context: Context,
    private val urlList: MutableList<CacheThumbnail>,
    private val pathList: MutableList<String>,
    private val onSelectedImageListener: OnSelectedImageListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AlbumDetailHolder(ItemAlbumDetailBinding.inflate(layoutInflater, parent, false))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val cacheThumbnail = urlList[position]

        when {
            holder is AlbumDetailHolder -> {
                val listItemSelected = pathList.filter { it == cacheThumbnail.path }
                Glide.with(context)
                    .load(if (cacheThumbnail.uri != null) cacheThumbnail else cacheThumbnail.path)
                    .error(R.drawable.effect_0_thumb)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .override(512, 512)
                    .into(holder.imageDetail)
                val isSelected = listItemSelected.isNotEmpty()
                holder.slRoot.isSelected = isSelected
                holder.imageSelected.visibleIf(isSelected)

                ViewCompat.setTransitionName(holder.imageDetail, cacheThumbnail.path)

                holder.imageRipple.setOnTouchListener(PickPhotoRecyclerViewItemListener(context, position, object : PickPhotoRecyclerViewItemListener.PickPhotoGestureListener {
                    override fun onClick(position: Int) {
                        if (urlList.size > position) {
                            if (TextUtils.isEmpty(cacheThumbnail.path)
                                || File(cacheThumbnail.path).length().toDouble() == 0.0
                                || !MediaHelper.isSupportImage(cacheThumbnail.path)
                                || cacheThumbnail.isFailed
                            ) {
                                onSelectedImageListener.onClickFailed()
                            } else {
                                onSelectedImageListener.onSelectedImage(holder.imageDetail, cacheThumbnail.path)
                            }
                        }
                    }

                    override fun onLongTouch(position: Int, event: MotionEvent?) {
                        if (urlList.size > position) {
                            if (TextUtils.isEmpty(cacheThumbnail.path)
                                || File(cacheThumbnail.path).length().toDouble() == 0.0
                                || !MediaHelper.isSupportImage(cacheThumbnail.path)
                                || cacheThumbnail.isFailed
                            ) {
                                onSelectedImageListener.onClickFailed()
                            } else {
                                onSelectedImageListener.onItemLongTouch(holder.imageDetail, cacheThumbnail.path)
                            }
                        }
                    }

                    override fun onTouchUp(position: Int) {
                        onSelectedImageListener.onItemCancelTouch(holder.imageDetail)
                    }

                    override fun onTouchDown(event: MotionEvent?) {
                        onSelectedImageListener.onTouchDown(event)
                    }
                }))
            }
        }
    }

    fun notifyItemChangedWithPath(path: String) {
        run loop@{
            urlList.forEachIndexed { index, cacheThumbnail ->
                if (TextUtils.equals(path, cacheThumbnail.path)) {
                    notifyItemChanged(index)
                    return@loop
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return urlList.size
    }

    inner class AlbumDetailHolder(itemAlbumDetailBinding: ItemAlbumDetailBinding) : RecyclerView.ViewHolder(itemAlbumDetailBinding.root) {
        val slRoot = itemAlbumDetailBinding.slRoot
        val imageDetail = itemAlbumDetailBinding.imageDetail
        val imageRipple = itemAlbumDetailBinding.imageRipple
        val imageSelected = itemAlbumDetailBinding.imageSelected
    }

    interface OnSelectedImageListener {
        fun onSelectedImage(imageView: ImageView, path: String)
        fun onClickFailed()
        fun onItemLongTouch(view: View, path: String) {}
        fun onItemCancelTouch(view: View) {}
        fun onTouchDown(event: MotionEvent?) {}
    }
}