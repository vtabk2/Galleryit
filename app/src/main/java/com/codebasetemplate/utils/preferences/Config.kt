package com.codebasetemplate.utils.preferences

import android.content.Context
import androidx.core.content.edit
import com.codebasetemplate.BuildConfig
import com.core.password.PasscodeType

class Config(val context: Context) {
    private val prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

    var albumId: String
        get() = prefs.getString(KEY_ALBUM_ID_LAST, "") ?: ""
        set(value) = prefs.edit { putString(KEY_ALBUM_ID_LAST, value) }

    var isShowNoteLongPress: Boolean
        get() = prefs.getBoolean(KEY_IS_SHOW_NOTE_LONG_PRESS, true)
        set(value) = prefs.edit { putBoolean(KEY_IS_SHOW_NOTE_LONG_PRESS, value) }

    var isCheckVersion: Boolean
        get() = prefs.getBoolean(KEY_IS_CHECK_VERSION, false)
        set(value) = prefs.edit { putBoolean(KEY_IS_CHECK_VERSION, value) }

    var passcode: String
        get() = prefs.getString(KEY_PASSCODE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_PASSCODE, value) }

    var passcodeType: String
        get() = prefs.getString(KEY_PASSCODE_TYPE, PasscodeType.NONE.value) ?: PasscodeType.NONE.value
        set(value) = prefs.edit { putString(KEY_PASSCODE_TYPE, value) }

    var securityQuestionIndex: Int
        get() = prefs.getInt(KEY_SECURITY_QUESTION_INDEX, -1)
        set(value) = prefs.edit { putInt(KEY_SECURITY_QUESTION_INDEX, value) }

    var securityAnswer: String
        get() = prefs.getString(KEY_SECURITY_ANSWER, "") ?: ""
        set(value) = prefs.edit { putString(KEY_SECURITY_ANSWER, value) }

    companion object {

        fun newInstance(context: Context) = Config(context)

        private const val KEY_ALBUM_ID_LAST = "KEY_ALBUM_ID_LAST"

        private const val KEY_IS_SHOW_NOTE_LONG_PRESS = "KEY_IS_SHOW_NOTE_LONG_PRESS"

        private const val KEY_IS_CHECK_VERSION = "KEY_IS_CHECK_VERSION"

        private const val KEY_PASSCODE = "KEY_PASSCODE"

        private const val KEY_PASSCODE_TYPE = "KEY_PASSCODE_TYPE"
        private const val KEY_SECURITY_QUESTION_INDEX = "KEY_SECURITY_QUESTION_INDEX"
        private const val KEY_SECURITY_ANSWER = "KEY_SECURITY_ANSWER"
    }
}