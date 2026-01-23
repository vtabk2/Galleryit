package com.codebasetemplate

import VaultKeyProvider
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.core.ads.BaseAdmobApplication
import com.core.ads.admob.ReOpenShowCondition
import com.core.billing.ProductIdManager
import com.core.preference.PurchasePreferences
import com.core.rate.RateInApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : BaseAdmobApplication(), DefaultLifecycleObserver {

    @Inject
    lateinit var purchasePreferences: PurchasePreferences

    @Inject
    lateinit var productIdManager: ProductIdManager

    @Inject
    lateinit var reOpenShowCondition: ReOpenShowCondition

    init {
        instance = this
    }

    override fun initOtherConfig() {
        RateInApp.instance.registerActivityLifecycle(this)
        RateInApp.instance.isHideNavigationBar = true
        RateInApp.instance.isHideStatusBar = true
        RateInApp.instance.isSpaceStatusBar = true
        RateInApp.instance.isSpaceDisplayCutout = true

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d("TAG5", "App_onStop: ")
        VaultKeyProvider.clearMemoryCache()
    }

    companion object {

        lateinit var instance: App
    }
}