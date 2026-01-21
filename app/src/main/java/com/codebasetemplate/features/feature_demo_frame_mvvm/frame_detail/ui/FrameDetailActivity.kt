package com.codebasetemplate.features.feature_demo_frame_mvvm.frame_detail.ui

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.codebasetemplate.core.base_ui.CoreActivity
import com.codebasetemplate.databinding.CoreActivityFrameDetailBinding
import com.core.baseui.ext.collectFlowOn
import com.core.utilities.result.processResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FrameDetailActivity: CoreActivity<CoreActivityFrameDetailBinding>() {
    private val viewModel: FrameDetailViewModel by viewModels()

    override fun bindingProvider(inflater: LayoutInflater): CoreActivityFrameDetailBinding {
        return CoreActivityFrameDetailBinding.inflate(inflater)
    }

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)

    }

    override fun handleObservable() {
        super.handleObservable()
        collectFlowOn(viewModel.frameDetail) {
            it.processResult(
                onSuccess = {
                    Glide.with(this).load(it.data.imageUrl).into(viewBinding.ivFrame)
                }
            )
        }
    }
}