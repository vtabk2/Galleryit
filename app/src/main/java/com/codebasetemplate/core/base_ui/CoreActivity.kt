package com.codebasetemplate.core.base_ui

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.core.baseui.BaseActivity
import com.core.baseui.RequireTurnOnNetworkBottomSheetFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield


abstract class CoreActivity<VB : ViewBinding> : BaseActivity<VB>() {

    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)

        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setupAfterOnBackPressed()
            }
        }
        onBackPressedCallback?.let {
            onBackPressedDispatcher.addCallback(this, it)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        newBase?.let {
            val config = it.resources.configuration
            if (config.fontScale != 1.0f) {
                config.fontScale = 1.0f
                val newContext = it.createConfigurationContext(config)
                super.attachBaseContext(newContext)
                return
            }
        }
        super.attachBaseContext(newBase)
    }

    /**
     * Hàm này cho phép Activity con override để custom logic.
     */
    open fun setupAfterOnBackPressed() {
        callSystemBack()
        showAdsAfterBack()
    }

    /**
     * Hàm này dùng để hiển thị quảng cáo khi ấn nút back
     */
    open fun showAdsAfterBack() {}

    /**
     * Hàm gọi Back hệ thống an toàn (tránh loop).
     */
    protected fun callSystemBack() {
        onBackPressedCallback?.isEnabled = false
        lifecycleScope.launch(Dispatchers.Main) {
            // nhường 1 frame cho FragmentManager hoàn tất
            yield()
            onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * Cho phép bật/tắt OnBackPressedCallback ở Activity con.
     */
    fun setBackPressEnabled(enabled: Boolean) {
        onBackPressedCallback?.isEnabled = enabled
    }

    /**
     * Handle back in fragment
     */
    fun addOnBackPressedCallback(owner: LifecycleOwner, action: () -> Unit) {
        onBackPressedDispatcher.addCallback(owner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = action()
        })
    }

    fun showRequireTurnOnNetworkBottomSheetFragment(onRetry: () -> Unit, onCancel: () -> Unit) {
        RequireTurnOnNetworkBottomSheetFragment().apply {
            this.onRetry = onRetry
            this.onCancel = onCancel
        }.show(
            supportFragmentManager,
            RequireTurnOnNetworkBottomSheetFragment::class.java.simpleName
        )
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return try {
            super.dispatchKeyEvent(event)
        } catch (e: SecurityException) {
            if (e.message?.contains("CLOSE_SYSTEM_DIALOGS") == true) {
                analyticsManager.logEvent("CLOSE_SYSTEM_DIALOGS_${this.javaClass.simpleName}")
                true
            } else {
                throw e
            }
        }
    }
}