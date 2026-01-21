package com.codebasetemplate.features.core

import android.os.Bundle
import android.view.LayoutInflater
import com.codebasetemplate.databinding.ActivityMainBinding
import com.codebasetemplate.features.app.base.BaseSelectedImageActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseSelectedImageActivity<ActivityMainBinding>() {

    override fun bindingProvider(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)
    }
}