package com.codebasetemplate.core.data.local

import android.content.Context
import com.core.preference.SharedPrefs
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyboardVersionDataPrefers @Inject constructor(@ApplicationContext context: Context) {
    private val prefers = SharedPrefs(context, name = "keyboard_prefers", gson = Gson())
    companion object {
        const val KEYBOARD_VERSION = "keyboard_version"
    }

    fun clearAll() {
        prefers.clear()
    }

    fun isKeyboardVersionUpdated(id: String): Boolean {
        return prefers.get(KEYBOARD_VERSION + id, false)
    }

    fun setKeyboardVersionUpdated(id: String) {
        prefers.put(KEYBOARD_VERSION + id, true)
    }

    var versionFrameUpdated: Boolean by prefers.preference(false)

}