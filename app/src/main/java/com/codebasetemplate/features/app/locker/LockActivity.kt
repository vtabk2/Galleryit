package com.codebasetemplate.features.app.locker

import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import com.codebasetemplate.core.base_ui.CoreActivity
import com.codebasetemplate.databinding.ActivityLockBinding
import com.core.baseui.toolbar.CoreToolbarView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LockActivity : CoreActivity<ActivityLockBinding>() {

    private val lockViewModel: LockViewModel by viewModels()

    override fun getSurfaceView(): View {
        return viewBinding.toolbar
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityLockBinding {
        return ActivityLockBinding.inflate(inflater)
    }

    override fun handleObservable() {
        super.handleObservable()

        viewBinding.toolbar.onToolbarListener = object : CoreToolbarView.OnToolbarListener {
            override fun onBack() {
                setupAfterOnBackPressed()
            }
        }
    }

    companion object {
        const val EXTRA_LOCK_MODE = "extra_lock_mode"
        const val LOCK_MODE_UNLOCK_APP = 1
        const val LOCK_MODE_VERIFY_USER = 2
    }
}