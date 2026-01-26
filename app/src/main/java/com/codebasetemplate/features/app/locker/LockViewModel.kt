package com.codebasetemplate.features.app.locker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.password.PasscodeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LockViewModel @Inject constructor(private val lockRepository: LockRepository) : ViewModel() {

    private val _event = MutableSharedFlow<LockEvent>()
    val event: SharedFlow<LockEvent> = _event.asSharedFlow()
    val currentPasscodeType = MutableStateFlow(PasscodeType.fromValue(lockRepository.passcodeType))
    val currentPasscode = MutableStateFlow("")

    // PIN-specific methods
    fun onPinDigitEntered(digit: String) {
        if (currentPasscode.value.length < 4) {
            currentPasscode.value += digit

            // Auto-verify when 4 digits entered
            if (currentPasscode.value.length == 4) {
                viewModelScope.launch(Dispatchers.IO) {
                    delay(100)
                    verifyPasscode()
                }
            }
        }
    }

    fun onPinDigitDeleted() {
        if (currentPasscode.value.isNotEmpty()) {
            currentPasscode.value = currentPasscode.value.dropLast(1)
        }
    }

    // Pattern-specific methods
    fun onPatternEntered(pattern: List<Int>) {
        val patternString = pattern.joinToString(",")
        currentPasscode.value = patternString
        // Verify pattern immediately
        verifyPasscode()
    }

    private fun verifyPasscode() {
        val inputPasscode = currentPasscode.value
        val isValid = lockRepository.verifyPasscode(inputPasscode)


        if (isValid) {
            // Success - unlock
            viewModelScope.launch {
                _event.emit(LockEvent.UnlockSuccess)
            }
        } else {
            // Failed - show error and clear
            currentPasscode.value = ""
            viewModelScope.launch {
                _event.emit(LockEvent.UnlockFailed)
            }
        }
    }

    fun unlockWithFingerprintSuccess() {
        viewModelScope.launch {
            _event.emit(LockEvent.UnlockSuccess)
        }
    }

    fun onForgotPassword() {
        viewModelScope.launch {
            currentPasscode.value = ""
            _event.emit(LockEvent.ForgotPassword)
        }
    }

    sealed class LockEvent {
        object UnlockSuccess : LockEvent()
        object UnlockFailed : LockEvent()
        object ForgotPassword : LockEvent()
    }
}