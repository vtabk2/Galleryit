package com.codebasetemplate.features.feature_demo_banner_native.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.codebasetemplate.databinding.CoreFragmentNativeAdsBinding
import com.codebasetemplate.features.main.ui.host.MainHostEvent
import com.codebasetemplate.features.main.ui.host.MainHostViewModel
import com.codebasetemplate.required.ads.AppAdPlaceName
import com.codebasetemplate.required.shortcut.AppScreenType
import com.core.ads.domain.AdLoadBannerNativeUiResource
import com.core.baseui.fragment.BaseChildOfHostFragment
import com.core.baseui.fragment.ScreenType
import com.core.baseui.toolbar.CoreToolbarView
import com.core.config.domain.data.IAdPlaceName
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BannerAndNativeChildOfHostFragment: BaseChildOfHostFragment<CoreFragmentNativeAdsBinding, MainHostEvent, MainHostViewModel>() {
    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): CoreFragmentNativeAdsBinding {
        return CoreFragmentNativeAdsBinding.inflate(inflater, container, false)
    }

    override val hostViewModel: MainHostViewModel by viewModels(ownerProducer = { requireParentFragment() })
    override val screenType: ScreenType
        get() = AppScreenType.BannerNative

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)
        viewBinding.run {
            toolbar.onToolbarListener = object : CoreToolbarView.OnToolbarListener {
                override fun onBack() {
                    hostViewModel.navigateTo(MainHostEvent.ActionBack)
                }
            }
        }
    }

    override fun providerBannerNativeAdPlaceName(): List<IAdPlaceName> {
        return listOf(
            AppAdPlaceName.ANCHORED_NATIVE_TEST,
            AppAdPlaceName.ANCHORED_BANNER_TEST
        )
    }

    override fun onBannerNativeResult(adResource: AdLoadBannerNativeUiResource) {
        super.onBannerNativeResult(adResource)
        viewBinding.layoutNative.processAdResource(adResource, AppAdPlaceName.ANCHORED_NATIVE_TEST)
        viewBinding.layoutBanner.processAdResource(adResource, AppAdPlaceName.ANCHORED_BANNER_TEST)
    }

}