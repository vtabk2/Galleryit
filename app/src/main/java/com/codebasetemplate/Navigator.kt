package com.codebasetemplate

import android.content.Context
import android.content.Intent
import com.codebasetemplate.features.app.main.MainActivity
import com.codebasetemplate.features.feature_demo_frame_mvvm.common.FrameConstants
import com.codebasetemplate.features.feature_demo_frame_mvvm.frame_detail.ui.FrameDetailActivity

object Navigator {
    fun startMainActivity(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun startFrameDetail(context: Context, frameId: String) {
        val intent = Intent(context, FrameDetailActivity::class.java).apply {
            putExtra(FrameConstants.EXTRA_FRAME_ID, frameId)
        }
        context.startActivity(intent)
    }

}