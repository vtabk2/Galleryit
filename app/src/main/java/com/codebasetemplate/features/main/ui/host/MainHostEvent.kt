package com.codebasetemplate.features.main.ui.host

import com.core.baseui.navigator.NavigatorEvent

sealed class MainHostEvent: NavigatorEvent {

    object ActionBack: MainHostEvent()

    object OpenMain: MainHostEvent()

    object OpenBannerAndNative: MainHostEvent()

    object ShareApp: MainHostEvent()

    object RateApp: MainHostEvent()

    object OpenPolicy: MainHostEvent()

    object OpenChangeLanguage: MainHostEvent()

    object OpenSetting: MainHostEvent()

    object OpenNativeInList: MainHostEvent()

    object OpenShop: MainHostEvent()

}