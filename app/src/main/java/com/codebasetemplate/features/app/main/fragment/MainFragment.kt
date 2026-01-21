package com.codebasetemplate.features.app.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.codebasetemplate.R
import com.codebasetemplate.core.base_ui.CoreFragment
import com.codebasetemplate.databinding.FragmentMainBinding
import com.codebasetemplate.required.shortcut.AppScreenType
import com.core.baseui.fragment.ScreenType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : CoreFragment<FragmentMainBinding>() {

    override fun bindingProvider(inflater: LayoutInflater, container: ViewGroup?): FragmentMainBinding {
        return FragmentMainBinding.inflate(inflater)
    }

    override val screenType: ScreenType = AppScreenType.MainFragment

    private var type: Int = 0

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)

        type = arguments?.getInt(TYPE) ?: type

        when (type) {
            0 -> {
                viewBinding.tvPage.setText(R.string.tab_all)
            }

            1 -> {
                viewBinding.tvPage.setText(R.string.tab_photos)
            }

            else -> {
                viewBinding.tvPage.setText(R.string.tab_videos)
            }
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