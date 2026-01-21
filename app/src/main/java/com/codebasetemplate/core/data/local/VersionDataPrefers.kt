package com.codebasetemplate.core.data.local

import android.content.Context
import com.core.preference.SharedPrefs
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VersionDataPrefers @Inject constructor(@ApplicationContext context: Context) {
    private val prefers = SharedPrefs(context, name = "version_data", gson = Gson())

    var versionFrameUpdated: Boolean by prefers.preference(false)

}