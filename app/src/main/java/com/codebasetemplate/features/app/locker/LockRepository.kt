package com.codebasetemplate.features.app.locker

import android.content.Context
import com.codebasetemplate.utils.extensions.config
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockRepository @Inject constructor(@ApplicationContext private val context: Context) {
    var passcodeType: String
        get() = context.config.passcodeType
        set(value) {
            context.config.passcodeType = value
        }

    fun verifyPasscode(input: String): Boolean {
        val stored = context.config.passcode
        return stored == hashStringSHA256(input)
    }

    private fun hashStringSHA256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}