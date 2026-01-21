package com.codebasetemplate.features.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import com.codebasetemplate.R
import com.codebasetemplate.databinding.ActivityMainBinding
import com.codebasetemplate.features.app.base.BaseSelectedImageActivity
import com.codebasetemplate.features.app.customview.CustomTabLayoutView
import com.codebasetemplate.features.app.main.adapter.MainCategoryAdapter
import com.codebasetemplate.features.app.main.fragment.ShareMainViewModel
import com.core.baseui.ext.collectFlowOn
import com.core.baseui.ext.collectFlowOnNullable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseSelectedImageActivity<ActivityMainBinding>() {
    private var adapter: MainCategoryAdapter? = null

    private val shareMainViewModel: ShareMainViewModel by viewModels()

    override fun getSurfaceView(): View {
        return viewBinding.toolbar
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)

        val list = mutableListOf<String>().apply {
            add(getString(R.string.tab_all))
            add(getString(R.string.tab_photos))
            add(getString(R.string.tab_videos))
        }

        adapter = MainCategoryAdapter(supportFragmentManager, lifecycle)
        viewBinding.vpMain.adapter = adapter

        viewBinding.customTabLayoutView.onUpdateTitleTabLayoutListener = object : CustomTabLayoutView.OnUpdateTitleTabLayoutListener {
            override fun getTitle(position: Int): String {
                return list.getOrNull(position) ?: ""
            }
        }

        viewBinding.customTabLayoutView.setupWithViewPager(viewBinding.vpMain, onPageSelected = { position ->
            val delta = if (viewBinding.vpMain.offscreenPageLimit > 0) {
                position - viewBinding.vpMain.offscreenPageLimit
            } else {
                position
            }
            if (delta in 1..3 && position > 1) {
                viewBinding.vpMain.offscreenPageLimit = position
            }
        })

        collectFlowOn(selectedImageViewModel.albumListFlow) { albumList ->
            shareMainViewModel.updateAlbumList(albumList = albumList)
        }

        collectFlowOnNullable(selectedImageViewModel.albumDetailFlow) { albumDetail ->
            shareMainViewModel.updateAlbumDetail(albumDetail = albumDetail)
        }
    }
}