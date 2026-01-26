package com.codebasetemplate.features.app.locker

import android.content.Context
import com.codebasetemplate.utils.extensions.config
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val config = context.config

    var passcode: String?
        get() = config.passcode
        set(value) {
            passcode = hashStringSHA256(value ?: "")
        }

    var passcodeType: String
        get() = config.passcodeType
        set(value) {
            config.passcodeType = value
        }

    fun verifyPasscode(input: String): Boolean {
        val stored = config.passcode
        return stored == hashStringSHA256(input)
    }

    private fun hashStringSHA256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}