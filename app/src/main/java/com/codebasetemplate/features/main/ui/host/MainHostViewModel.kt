package com.codebasetemplate.features.main.ui.host

import androidx.lifecycle.SavedStateHandle
import com.core.baseui.BaseSharedViewModel
import com.core.config.domain.RemoteConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainHostViewModel  @Inject constructor(
    private val remoteConfigRepository: RemoteConfigRepository,
    private val handle: SavedStateHandle
) : BaseSharedViewModel<MainHostEvent>(remoteConfigRepository, handle) {

    override fun navigateActionBack() {
        navigateTo(MainHostEvent.ActionBack)
    }
}