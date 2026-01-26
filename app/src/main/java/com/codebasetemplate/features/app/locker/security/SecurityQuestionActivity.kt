package com.codebasetemplate.features.app.locker.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.codebasetemplate.core.base_ui.CoreActivity
import com.codebasetemplate.databinding.ActivitySecurityQuestionBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SecurityQuestionActivity : CoreActivity<ActivitySecurityQuestionBinding>() {

    override fun getSurfaceView(): View {
        return viewBinding.toolbar
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivitySecurityQuestionBinding {
        return ActivitySecurityQuestionBinding.inflate(inflater)
    }

    private val securityQuestions by lazy { getSecurityQuestions(this) }

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)
    }

    companion object {
        const val EXTRA_FROM_PASSCODE_SETUP = "extra_from_passcode_setup"
        const val EXTRA_FROM_SETTINGS = "extra_from_settings"
    }
}