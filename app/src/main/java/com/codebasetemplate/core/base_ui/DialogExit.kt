package com.codebasetemplate.core.base_ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.codebasetemplate.databinding.DialogExitBinding
import com.codebasetemplate.required.ads.AppAdPlaceName
import com.core.ads.domain.AdLoadBannerNativeUiResource
import com.core.baseui.BaseBottomSheetDialogFragment
import com.core.baseui.ext.launchWhenResumed
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DialogExit : BaseBottomSheetDialogFragment<DialogExitBinding>() {

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogExitBinding {
        return DialogExitBinding.inflate(inflater, container, false)
    }

    var onExit: (() -> Unit)? = null


    override fun initView() {
        viewBinding.run {
            buttonNo.setOnClickListener {
                dismiss()
            }

            buttonYes.setOnClickListener {
                onExit?.invoke()
            }
        }
    }

    fun setBannerNativeAd(adResource: AdLoadBannerNativeUiResource) {
        launchWhenResumed {
            viewBinding.layoutBannerNative.processAdResource(adResource, AppAdPlaceName.ANCHORED_EXIT)
        }
    }
}