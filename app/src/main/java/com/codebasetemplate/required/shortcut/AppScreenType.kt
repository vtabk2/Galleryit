package com.codebasetemplate.required.shortcut

import com.core.baseui.fragment.ScreenType

sealed class AppScreenType(override val screenName: String): ScreenType {
    object Uninstall: AppScreenType("Uninstall")

    object ReasonUninstall: AppScreenType("ReasonUninstall")

    object OnBoarding: AppScreenType("OnBoarding")

    object Main: AppScreenType("Main")
    object Setting: AppScreenType("Setting")
    object NativeInList: AppScreenType("NativeInList")

    object BannerNative: AppScreenType("BannerAndNative")

    object Screen1: AppScreenType("Screen1")
    object Screen2: AppScreenType("Screen2")

    object Screen3: AppScreenType("Screen3")

    object None: AppScreenType("")
    object Shop: AppScreenType("Shop")
}