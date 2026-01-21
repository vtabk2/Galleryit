package com.codebasetemplate.features.feature_onboarding.ui.model

sealed class OnBoardingItem(var isShowAds: Boolean, var isPageEnd: Boolean) {
    class Item(
        isPageEnd: Boolean,
        var position: Int,
        isShowAds: Boolean
    ) :
        OnBoardingItem(isShowAds, isPageEnd)

    object FullNativeItem : OnBoardingItem(false, false)
}