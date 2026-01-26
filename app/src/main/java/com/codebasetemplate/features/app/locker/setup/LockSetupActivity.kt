package com.codebasetemplate.features.app.locker.setup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.codebasetemplate.R
import com.codebasetemplate.core.base_ui.CoreActivity
import com.codebasetemplate.databinding.ActivityPasscodeSetupBinding
import com.codebasetemplate.features.app.locker.security.SecurityQuestionActivity
import com.core.baseui.ext.collectFlowOn
import com.core.baseui.toolbar.CoreToolbarView
import com.core.password.LockPatternView
import com.core.password.PasscodeInputView
import com.core.password.PasscodeType
import com.core.utilities.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LockSetupActivity : CoreActivity<ActivityPasscodeSetupBinding>() {

    private val passcodeSetupViewModel: PasscodeSetupViewModel by viewModels()

    private var reason: Int = CREATE_NEW

    override fun getSurfaceView(): View {
        return viewBinding.toolbar
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityPasscodeSetupBinding {
        return ActivityPasscodeSetupBinding.inflate(inflater)
    }

    private val setSecurityQuestionActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        /*Chỉ vào khi thiết lập passcode lần đầu*/
        when (result.resultCode) {
            RESULT_OK -> {
                toast(R.string.msg_passcode_setup_completed_successfully)
                setResult(RESULT_OK)
                finish()
            }

            else -> {
                passcodeSetupViewModel.clearDataLock()
                toast(R.string.msg_passcode_not_saved)
                passcodeSetupViewModel.resetPasscodeState()
            }
        }
    }

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)

        reason = intent.getIntExtra(EXTRA_REASON, CREATE_NEW)
    }

    override fun handleObservable() {
        super.handleObservable()

        viewBinding.toolbar.onToolbarListener = object : CoreToolbarView.OnToolbarListener {
            override fun onBack() {
                setupAfterOnBackPressed()
            }
        }

        viewBinding.switchTypeText.setOnClickListener {
            passcodeSetupViewModel.switchType()
        }

        viewBinding.pinInputView.onDialButtonClickListener = object : PasscodeInputView.OnDialButtonClickListener {
            override fun onDialButtonClick(text: String) {
                passcodeSetupViewModel.onPinDigitEntered(text)
            }

            override fun onDialButtonDeleteClick() {
                passcodeSetupViewModel.onPinDigitDeleted()
            }
        }

        viewBinding.patternInputView.setOnPatternListener(object : LockPatternView.OnPatternListener {
            override fun onPatternStart() {
                passcodeSetupViewModel.confirmIncorrect.value = false
            }

            override fun onPatternCleared() {
            }

            override fun onPatternCellAdded(pattern: MutableList<LockPatternView.Cell>?) {
            }

            override fun onPatternDetected(pattern: MutableList<LockPatternView.Cell>?) {

                // Convert LockPatternView.Cell to List<Int> (1-9)
                val patternIndices = pattern?.map { cell ->
                    // Convert cell position to 1-based index (1-9)
                    // 3x3 grid: row=0,1,2; column=0,1,2
                    // Index = row * 3 + column + 1 (to make it 1-based)
                    cell.row * 3 + cell.column + 1
                } ?: emptyList()

                passcodeSetupViewModel.onPatternEntered(patternIndices)
            }
        })

        // Observe passcode type changes
        collectFlowOn(passcodeSetupViewModel.currentPasscodeType) { passcodeType ->
            updatePasscodeTypeView(passcodeType)
            updateSwitchTypeText(passcodeType)
            updateInstructionText()
        }

        // Observe confirm state changes
        collectFlowOn(passcodeSetupViewModel.isConfirmState) { _ ->
            updateInstructionText()
            updatePasscodeBoxes()
        }

        // Observe first passcode changes
        collectFlowOn(passcodeSetupViewModel.firstPasscode) {
            updatePasscodeBoxes()
        }

        // Observe second passcode changes
        collectFlowOn(passcodeSetupViewModel.secondPasscode) {
            updatePasscodeBoxes()
        }

        // Observe second passcode changes
        collectFlowOn(passcodeSetupViewModel.confirmIncorrect) {
            viewBinding.tvPasscodeIncorrect.isInvisible = !it
        }

        // Observe events
        lifecycleScope.launch {
            passcodeSetupViewModel.event.collect { event ->
                when (event) {
                    is PasscodeSetupViewModel.PasscodeSetupEvent.PasscodeSetSuccess -> {
                        // Handle success based on reason
                        handlePasscodeSetSuccess()
                    }

                    is PasscodeSetupViewModel.PasscodeSetupEvent.PasscodeSetError -> {
                        finish()
                    }

                    is PasscodeSetupViewModel.PasscodeSetupEvent.MinDotPatternError -> {
                        toast(R.string.msg_pattern_must_have_at_least_4_dots)
                        viewBinding.patternInputView.clearPattern()
                    }

                }
            }
        }
    }

    private fun updatePasscodeTypeView(passcodeType: PasscodeType) {
        when (passcodeType) {
            PasscodeType.PIN -> {
                viewBinding.pinInputView.visibility = View.VISIBLE
                viewBinding.patternInputView.visibility = View.GONE
            }

            PasscodeType.PATTERN -> {
                viewBinding.pinInputView.visibility = View.GONE
                viewBinding.patternInputView.visibility = View.VISIBLE
            }

            else -> {
                viewBinding.pinInputView.visibility = View.VISIBLE
                viewBinding.patternInputView.visibility = View.GONE
            }
        }
    }

    private fun updateSwitchTypeText(passcodeType: PasscodeType) {
        viewBinding.switchTypeText.text = if (passcodeType == PasscodeType.PIN) {
            getString(R.string.text_switch_to_pattern)
        } else {
            getString(R.string.text_switch_to_pin)
        }
    }

    private fun updateInstructionText() {
        val isConfirm = passcodeSetupViewModel.isConfirmState.value
        val passcodeType = passcodeSetupViewModel.currentPasscodeType.value
        val titleText = when (reason) {
            CREATE_NEW -> {
                when {
                    passcodeType == PasscodeType.PIN && !isConfirm -> getString(R.string.title_lock_create_pin_enter)
                    passcodeType == PasscodeType.PIN && isConfirm -> getString(R.string.title_lock_create_pin_confirm)
                    passcodeType == PasscodeType.PATTERN && !isConfirm -> getString(R.string.title_lock_create_pattern_enter)
                    passcodeType == PasscodeType.PATTERN && isConfirm -> getString(R.string.title_lock_create_pattern_confirm)
                    else -> getString(R.string.title_lock_create_pin_enter)
                }
            }

            CHANGE_PASSWORD -> {
                when {
                    passcodeType == PasscodeType.PIN && !isConfirm -> getString(R.string.title_lock_change_pin_enter)
                    passcodeType == PasscodeType.PIN && isConfirm -> getString(R.string.title_lock_change_pin_confirm)
                    passcodeType == PasscodeType.PATTERN && !isConfirm -> getString(R.string.title_lock_change_pattern_enter)
                    passcodeType == PasscodeType.PATTERN && isConfirm -> getString(R.string.title_lock_change_pattern_confirm)
                    else -> getString(R.string.title_lock_change_pin_enter)
                }
            }

            RESET_PASSWORD -> {
                when {
                    passcodeType == PasscodeType.PIN && !isConfirm -> getString(R.string.title_lock_reset_pin_enter)
                    passcodeType == PasscodeType.PIN && isConfirm -> getString(R.string.title_lock_reset_pin_confirm)
                    passcodeType == PasscodeType.PATTERN && !isConfirm -> getString(R.string.title_lock_reset_pattern_enter)
                    passcodeType == PasscodeType.PATTERN && isConfirm -> getString(R.string.title_lock_reset_pattern_confirm)
                    else -> getString(R.string.title_lock_reset_pin_enter)
                }
            }

            else -> {
                ""
            }
        }
        viewBinding.titleText.text = titleText
        viewBinding.switchTypeText.isInvisible = isConfirm
    }

    private fun updatePasscodeBoxes() {
        val currentPasscode = if (passcodeSetupViewModel.isConfirmState.value) {
            passcodeSetupViewModel.secondPasscode.value
        } else {
            passcodeSetupViewModel.firstPasscode.value
        }

        if (viewBinding.pinInputView.isVisible) {
            viewBinding.pinInputView.setPasscodeLength(currentPasscode.length)
        } else if (viewBinding.patternInputView.isVisible) {
            viewBinding.patternInputView.clearPattern()
        }
    }

    private fun handlePasscodeSetSuccess() {
        Log.d("TAG5", "LockSetupActivity_handlePasscodeSetSuccess: reason = $reason")
        when (reason) {
            CREATE_NEW -> {
                val intent = Intent(this, SecurityQuestionActivity::class.java)
                intent.putExtra(SecurityQuestionActivity.EXTRA_FROM_PASSCODE_SETUP, true)
                setSecurityQuestionActivity.launch(intent)
            }

            CHANGE_PASSWORD -> {
                toast(R.string.msg_passcode_updated_successfully)
                setResult(RESULT_OK)
                finish()
            }

            RESET_PASSWORD -> {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_PIN_TYPE = "EXTRA_PIN_TYPE"

        const val EXTRA_REASON = "extra_reason"
        const val CREATE_NEW = 0
        const val CHANGE_PASSWORD = 1
        const val RESET_PASSWORD = 2
    }
}