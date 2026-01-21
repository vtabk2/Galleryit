package com.codebasetemplate.features.main.ui

import androidx.lifecycle.SavedStateHandle
import com.codebasetemplate.features.main.ui.event.MainFeatureEvent
import com.core.baseui.BaseSharedViewModel
import com.core.config.domain.RemoteConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainShareViewModel @Inject constructor(
    private val remoteConfigRepository: RemoteConfigRepository,
    private val handle: SavedStateHandle,
) : BaseSharedViewModel<MainFeatureEvent>(remoteConfigRepository, handle) {

    override fun navigateActionBack() {
        navigateTo(MainFeatureEvent.ActionBack)
    }
}