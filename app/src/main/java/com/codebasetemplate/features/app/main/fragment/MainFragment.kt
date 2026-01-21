package com.codebasetemplate.features.app.main.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.codebasetemplate.core.base_ui.CoreFragment
import com.codebasetemplate.databinding.FragmentMainBinding
import com.codebasetemplate.features.app.main.adapter.AlbumDetailAdapter
import com.codebasetemplate.required.shortcut.AppScreenType
import com.codebasetemplate.utils.glide.thumb.CacheThumbnail
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
            Log.d("TAG5", "MainFragment_initViews: albumList = $list")
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
                override fun onSelectedImage(path: String) {

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
            Log.d("TAG5", "MainFragment_initViews: albumDetail = $albumDetail")
            urlList.clear()
            urlList.addAll(albumDetail?.detailList ?: mutableListOf())
            albumDetailAdapter?.notifyDataSetChanged()
        }
    }

    companion object {
        private const val TYPE = "TYPE"

        fun newInstance(type: Int) = MainFragment().apply {
            arguments = Bundle().apply {
                putInt(TYPE, type)
            }
        }
    }
}