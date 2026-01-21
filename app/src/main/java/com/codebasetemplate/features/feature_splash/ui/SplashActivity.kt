package com.codebasetemplate.features.feature_splash.ui

import android.view.LayoutInflater
import androidx.activity.viewModels
import com.codebasetemplate.databinding.CoreActivitySplashBinding
import com.core.baseui.ext.bindLiveData
import com.core.utilities.invisible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity: BaseSplashActivity<CoreActivitySplashBinding>() {
    private val viewModel by viewModels<SplashLoadDataViewModel>()
    override fun bindingProvider(inflater: LayoutInflater): CoreActivitySplashBinding {
        return CoreActivitySplashBinding.inflate(inflater)
    }

    override fun initData() {
        viewModel.initData()

        bindLiveData(viewModel.initData) { isReady ->
            if(isReady) {
                if(!viewModel.isInitData) {
                    onDataReady()
                }
                viewModel.isInitData = true
            }
        }
    }

    override fun hideLoading() {
        viewBinding.progressView.invisible()
    }
}