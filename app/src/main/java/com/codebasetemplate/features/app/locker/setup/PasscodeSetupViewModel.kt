package com.codebasetemplate.features.app.locker.setup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codebasetemplate.features.app.locker.LockRepository
import com.core.password.PasscodeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasscodeSetupViewModel @Inject constructor(
    private val lockRepository: LockRepository
) : ViewModel() {

    private val _event = MutableSharedFlow<PasscodeSetupEvent>()
    val event: SharedFlow<PasscodeSetupEvent> = _event.asSharedFlow()

    private val _currentPasscodeType = MutableStateFlow(PasscodeType.PIN)
    val currentPasscodeType: StateFlow<PasscodeType> = _currentPasscodeType

    private val _isConfirmState = MutableStateFlow(false)
    val isConfirmState: StateFlow<Boolean> = _isConfirmState

    private val _firstPasscode = MutableStateFlow("")
    val firstPasscode: StateFlow<String> = _firstPasscode

    private val _secondPasscode = MutableStateFlow("")
    val secondPasscode: StateFlow<String> = _secondPasscode

    var confirmIncorrect = MutableStateFlow(false)

    fun switchType() {
        val newType = if (_currentPasscodeType.value == PasscodeType.PIN) {
            PasscodeType.PATTERN
        } else {
            PasscodeType.PIN
        }
        setPasscodeType(newType)
    }

    fun setPasscodeType(passcodeType: PasscodeType) {
        _currentPasscodeType.value = passcodeType
        resetPasscodeState()
    }

    // PIN-specific methods
    fun onPinDigitEntered(digit: String) {
        if (_isConfirmState.value) {
            confirmIncorrect.value = false
            addDigitToSecondPasscode(digit)
            if (_secondPasscode.value.length == 4) {
                confirmSecondPasscode()
            }
        } else {
            addDigitToFirstPasscode(digit)
            if (_firstPasscode.value.length == 4) {
                viewModelScope.launch(Dispatchers.IO) {
                    delay(100)
                    _isConfirmState.value = true
                    _secondPasscode.value = ""
                }
            }
        }
    }

    fun onPinDigitDeleted() {
        if (_isConfirmState.value) {
            removeDigitFromSecondPasscode()
        } else {
            removeDigitFromFirstPasscode()
        }
    }

    // Pattern-specific methods
    fun onPatternEntered(pattern: List<Int>) {
        val patternString = pattern.joinToString(",")
        if (_isConfirmState.value) {
            _secondPasscode.value = patternString
            confirmSecondPasscode()  // Pattern confirm immediately
        } else {
            if (pattern.size < 4) {
                viewModelScope.launch {
                    _event.emit(PasscodeSetupEvent.MinDotPatternError)
                }
            } else {
                _firstPasscode.value = patternString
                _isConfirmState.value = true
                _secondPasscode.value = ""
            }
        }
    }

    // Helper methods for PIN
    private fun addDigitToFirstPasscode(digit: String) {
        _firstPasscode.value += digit
    }

    private fun addDigitToSecondPasscode(digit: String) {
        _secondPasscode.value += digit
    }

    private fun removeDigitFromFirstPasscode() {
        if (_firstPasscode.value.isNotEmpty()) {
            _firstPasscode.value = _firstPasscode.value.dropLast(1)
        }
    }

    private fun removeDigitFromSecondPasscode() {
        if (_secondPasscode.value.isNotEmpty()) {
            _secondPasscode.value = _secondPasscode.value.dropLast(1)
        }
    }

    // Validation methods

    fun confirmSecondPasscode() {
        if (_firstPasscode.value == _secondPasscode.value) {
            // Passcodes match, save to settings
            savePasscodeToSettings()
        } else {
            confirmIncorrect.value = true
            viewModelScope.launch(Dispatchers.IO) {
                delay(100)
                _secondPasscode.value = ""
            }
        }
    }

    // Validation method

    fun resetPasscodeState() {
        confirmIncorrect.value = false
        _isConfirmState.value = false
        _firstPasscode.value = ""
        _secondPasscode.value = ""
    }

    private fun savePasscodeToSettings() {
        viewModelScope.launch {
            Log.d("TAG5", "PasscodeSetupViewModel_savePasscodeToSettings: ")
            try {
                // Save passcode settings using Settings directly (no encryption)
                lockRepository.passcodeType = _currentPasscodeType.value.value
                lockRepository.passcode = _firstPasscode.value
                _event.emit(PasscodeSetupEvent.PasscodeSetSuccess)

            } catch (e: Exception) {
                _event.emit(PasscodeSetupEvent.PasscodeSetError(e.message ?: "Unknown error"))
            }
        }
    }

    fun clearDataLock() {
        lockRepository.passcodeType = PasscodeType.NONE.value
        lockRepository.passcode = null
    }

    sealed class PasscodeSetupEvent {
        object PasscodeSetSuccess : PasscodeSetupEvent()
        data class PasscodeSetError(val message: String) : PasscodeSetupEvent()
        object MinDotPatternError : PasscodeSetupEvent()
    }
}