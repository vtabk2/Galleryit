package com.codebasetemplate.features.feature_uninstall.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.codebasetemplate.Navigator
import com.codebasetemplate.databinding.CoreFragmentHostBinding
import com.codebasetemplate.features.feature_uninstall.ui.navigate.UninstallNavigateEvent
import com.core.baseui.ext.TransitionType
import com.core.baseui.fragment.BaseHostFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UninstallHostFragment :
    BaseHostFragment<CoreFragmentHostBinding, UninstallNavigateEvent, UninstallShareViewModel>() {
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): CoreFragmentHostBinding {
        return CoreFragmentHostBinding.inflate(inflater, container, false)
    }

    override val containerId: Int
        get() = viewBinding.hostContainer.id
    override val hostViewModel: UninstallShareViewModel by viewModels()

    override fun showFirstScreen() {
        super.showFirstScreen()
        onNavigateTo(UninstallNavigateEvent.OpenUninstallScreen)
    }

    override fun onNavigateTo(event: UninstallNavigateEvent) {
        super.onNavigateTo(event)
        when(event) {
            UninstallNavigateEvent.OpenUninstallScreen -> {
                replaceFragment(UninstallChildOfHostFragment(), transitionType = TransitionType.SIBLING)
            }

            UninstallNavigateEvent.OpenUninstallFeedbackScreen -> {
                replaceFragment(UninstallFeedbackChildOfHostFragment(), transitionType = TransitionType.SIBLING)
            }

            UninstallNavigateEvent.BackEvent -> {
                handleOnBackPressed()
            }
        }
    }

    override fun handleLastBackStack() {
        Navigator.startMainActivity(requireContext())
    }
}