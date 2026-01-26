package com.codebasetemplate.features.app.locker.setup

import android.view.LayoutInflater
import android.view.View
import com.codebasetemplate.core.base_ui.CoreActivity
import com.codebasetemplate.databinding.ActivityPasscodeSetupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LockSetupActivity : CoreActivity<ActivityPasscodeSetupBinding>() {

    override fun getSurfaceView(): View {
        return viewBinding.toolbar
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityPasscodeSetupBinding {
        return ActivityPasscodeSetupBinding.inflate(inflater)
    }
}