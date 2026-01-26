package com.codebasetemplate.features.app.locker.security

import android.content.Context
import com.codebasetemplate.R

fun getSecurityQuestions(context: Context): Array<String> {
    return context.resources.getStringArray(R.array.security_questions)
}
