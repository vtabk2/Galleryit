package com.codebasetemplate.features.main.ui.event

import com.core.baseui.navigator.NavigatorEvent

sealed class MainFeatureEvent: NavigatorEvent {
    object ActionBack: MainFeatureEvent()

    object OpenMain: MainFeatureEvent()
}