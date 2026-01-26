package com.codebasetemplate.features.app.locker.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import com.codebasetemplate.core.base_ui.CoreActivity
import com.codebasetemplate.databinding.ActivitySecurityQuestionBinding
import com.core.baseui.ext.collectFlowOn
import com.core.baseui.toolbar.CoreToolbarView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SecurityQuestionActivity : CoreActivity<ActivitySecurityQuestionBinding>() {

    private val securityQuestionViewModel: SecurityQuestionViewModel by viewModels()

    override fun getSurfaceView(): View {
        return viewBinding.toolbar
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivitySecurityQuestionBinding {
        return ActivitySecurityQuestionBinding.inflate(inflater)
    }

    private val securityQuestions by lazy { getSecurityQuestions(this) }

    private var selectedQuestionIndex = 0
    private var isFromPasscodeSetup = false
    private var isFromSettings = false

    private var questionPopup : SecurityQuestionPopup? = null

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)

        isFromPasscodeSetup = intent.getBooleanExtra(EXTRA_FROM_PASSCODE_SETUP, false)
        isFromSettings = intent.getBooleanExtra(EXTRA_FROM_SETTINGS, false)

        securityQuestionViewModel.setSelectedQuestion(selectedQuestionIndex)
    }

    override fun handleObservable() {
        super.handleObservable()

        viewBinding.toolbar.onToolbarListener = object : CoreToolbarView.OnToolbarListener {
            override fun onBack() {
                setupAfterOnBackPressed()
            }
        }

        viewBinding.questionContent.setOnClickListener {
            if (questionPopup == null) {
                questionPopup = SecurityQuestionPopup(this)
            }
            questionPopup?.show(viewBinding.questionContent) { position ->
                selectedQuestionIndex = position
                securityQuestionViewModel.setSelectedQuestion(position)
                questionPopup?.dismiss()
            }
        }

        collectFlowOn(securityQuestionViewModel.selectedQuestionIndex) {
            viewBinding.questionContent.text = securityQuestions[it]
        }
    }

    override fun setupAfterOnBackPressed() {
        if (isFromPasscodeSetup || isFromSettings) {
            setResult(RESULT_CANCELED)
            finish()
        } else {
            super.setupAfterOnBackPressed()
        }
    }

    companion object {
        const val EXTRA_FROM_PASSCODE_SETUP = "extra_from_passcode_setup"
        const val EXTRA_FROM_SETTINGS = "extra_from_settings"
    }
}