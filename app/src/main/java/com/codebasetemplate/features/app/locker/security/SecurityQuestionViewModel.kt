package com.codebasetemplate.features.app.locker.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codebasetemplate.features.app.locker.LockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityQuestionViewModel @Inject constructor(
    private val lockRepository: LockRepository,
) : ViewModel() {

    private val _selectedQuestionIndex = MutableStateFlow(lockRepository.securityQuestionIndex)
    val selectedQuestionIndex: StateFlow<Int> = _selectedQuestionIndex

    private val _answer = MutableStateFlow("")
    val answer: StateFlow<String> = _answer

    private val _event = MutableSharedFlow<SecurityQuestionEvent>(1)
    val event: SharedFlow<SecurityQuestionEvent> = _event.asSharedFlow()

    fun setSelectedQuestion(index: Int) {
        _selectedQuestionIndex.value = index
    }

    fun setAnswer(answer: String) {
        _answer.value = answer
    }

    fun saveSecurityQuestion(questionIndex: Int, answer: String) {
        viewModelScope.launch {
            try {
                lockRepository.securityQuestionIndex = questionIndex
                lockRepository.securityAnswer = answer.trim()
                _event.emit(SecurityQuestionEvent.SaveSuccess)
            } catch (e: Exception) {
                _event.emit(SecurityQuestionEvent.SaveError)
            }
        }
    }

    sealed class SecurityQuestionEvent {
        data object SaveSuccess : SecurityQuestionEvent()
        data object SaveError : SecurityQuestionEvent()
    }
}
