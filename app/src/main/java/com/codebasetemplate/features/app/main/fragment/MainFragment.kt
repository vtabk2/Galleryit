package com.codebasetemplate.features.app.main.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.codebasetemplate.R
import com.codebasetemplate.core.base_ui.CoreFragment
import com.codebasetemplate.databinding.FragmentMainBinding
import com.codebasetemplate.features.app.main.adapter.AlbumDetailAdapter
import com.codebasetemplate.features.app.main.fragment.preview.PreviewFragment
import com.codebasetemplate.required.shortcut.AppScreenType
import com.codebasetemplate.utils.glide.thumb.CacheThumbnail
import com.codebasetemplate.utils.glide.thumb.MediaType
import com.codebasetemplate.utils.load.LoadImageDataUtils
import com.core.baseui.fragment.ScreenType
import com.core.baseui.fragment.collectFlowOn
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : CoreFragment<FragmentMainBinding>() {

    private var albumDetailAdapter: AlbumDetailAdapter? = null

    private var albumList = mutableListOf<LoadImageDataUtils.Album>()
    private var urlList = mutableListOf<CacheThumbnail>()
    private var pathList = mutableListOf<String>()

    private val shareMainViewModel: ShareMainViewModel by activityViewModels()

    override fun bindingProvider(inflater: LayoutInflater, container: ViewGroup?): FragmentMainBinding {
        return FragmentMainBinding.inflate(inflater)
    }

    override val screenType: ScreenType = AppScreenType.MainFragment

    private var type: Int = 0

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)

        context?.let { ct ->
            type = arguments?.getInt(TYPE) ?: type

            setupAlbum(ct)
            setupAlbumDetail(ct)
        }
    }

    private fun setupAlbum(ct: Context) {
        collectFlowOn(shareMainViewModel.albumListFlow) { list ->
            albumList.clear()
            albumList.addAll(list)
        }
    }

    private fun setupAlbumDetail(ct: Context) {
        albumDetailAdapter = AlbumDetailAdapter(
            context = ct,
            urlList = urlList,
            pathList = pathList,
            onSelectedImageListener = object : AlbumDetailAdapter.OnSelectedImageListener {
                override fun onSelectedImage(imageView: ImageView, path: String) {
                    activity?.let { ac ->
                        ac.supportFragmentManager
                            .beginTransaction()
                            .setReorderingAllowed(true)
                            .addSharedElement(imageView, path)
                            .replace(R.id.flPreview, PreviewFragment.newInstance(path))
                            .addToBackStack(null)
                            .commit()
                    }
                }

                override fun onClickFailed() {

                }

                override fun onItemLongTouch(view: View, path: String) {

                }

                override fun onItemCancelTouch(view: View) {

                }

                override fun onTouchDown(event: MotionEvent?) {

                }
            })

        viewBinding.rvAlbumDetail.adapter = albumDetailAdapter
        viewBinding.rvAlbumDetail.layoutManager = GridLayoutManager(ct, 4)
        viewBinding.rvAlbumDetail.setHasFixedSize(true)

        collectFlowOn(shareMainViewModel.albumDetailFlow) { albumDetail ->
            val list = when (type) {
                TYPE_ALL -> {
                    albumDetail?.detailList
                }

                TYPE_PHOTOS -> {
                    albumDetail?.detailList?.filter { it.mediaType == MediaType.IMAGE }
                }

                TYPE_VIDEOS -> {
                    albumDetail?.detailList?.filter { it.mediaType == MediaType.VIDEO }
                }

                else -> {
                    mutableListOf()
                }
            } ?: mutableListOf()

            urlList.clear()
            urlList.addAll(list)
            albumDetailAdapter?.notifyDataSetChanged()
        }
    }

    companion object {
        private const val TYPE = "TYPE"

        const val TYPE_ALL = 0
        const val TYPE_PHOTOS = 1
        const val TYPE_VIDEOS = 2

        fun newInstance(type: Int) = MainFragment().apply {
            arguments = Bundle().apply {
                putInt(TYPE, type)
            }
        }
    }
}