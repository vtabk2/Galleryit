package com.codebasetemplate.features.main.ui.host

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.codebasetemplate.databinding.CoreFragmentMainHostBinding
import com.codebasetemplate.features.feature_demo_banner_native.ui.BannerAndNativeChildOfHostFragment
import com.codebasetemplate.features.feature_demo_frame_mvvm.frame_list.ui.FrameListFragment
import com.codebasetemplate.features.feature_setting.ui.SettingChildOfHostFragment
import com.codebasetemplate.features.feature_shop.ui.ShopChildOfHostFragment
import com.codebasetemplate.features.main.ui.MainChildOfHostFragment
import com.codebasetemplate.features.main.ui.MainShareViewModel
import com.core.baseui.ext.TransitionType
import com.core.baseui.fragment.BaseHostFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainHostFragment: BaseHostFragment<CoreFragmentMainHostBinding, MainHostEvent, MainHostViewModel>() {
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): CoreFragmentMainHostBinding {
        return CoreFragmentMainHostBinding.inflate(inflater, container, false)
    }

    override val containerId: Int get() = viewBinding.mainHostContainer.id

    override val hostViewModel: MainHostViewModel by viewModels()

    private val sharedViewModel: MainShareViewModel by activityViewModels()

    override fun showFirstScreen() {
        super.showFirstScreen()
        onNavigateTo(MainHostEvent.OpenMain)
    }

    override fun handleLastBackStack() {
        sharedViewModel.navigateActionBack()
    }

    override fun onNavigateTo(event: MainHostEvent) {
        super.onNavigateTo(event)
        when(event) {
            MainHostEvent.ActionBack -> {
                handleOnBackPressed()
            }
            MainHostEvent.OpenMain -> {
                replaceFragment(MainChildOfHostFragment())
            }
            MainHostEvent.ShareApp -> {

            }
            MainHostEvent.RateApp -> {
            }

            MainHostEvent.OpenPolicy -> {
            }

            MainHostEvent.OpenChangeLanguage -> {

            }

            MainHostEvent.OpenBannerAndNative -> {
                replaceFragment(BannerAndNativeChildOfHostFragment())
            }

            MainHostEvent.OpenSetting -> {
                replaceFragment(SettingChildOfHostFragment(), transitionType = TransitionType.MODAL)
            }

            MainHostEvent.OpenNativeInList -> {
                replaceFragment(FrameListFragment())
            }

            MainHostEvent.OpenShop -> {
                replaceFragment(ShopChildOfHostFragment())
            }
        }
    }
}