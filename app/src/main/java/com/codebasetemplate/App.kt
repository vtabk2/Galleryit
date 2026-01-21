package com.codebasetemplate

import com.core.ads.BaseAdmobApplication
import com.core.ads.admob.ReOpenShowCondition
import com.core.billing.ProductIdManager
import com.core.preference.PurchasePreferences
import com.core.rate.RateInApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : BaseAdmobApplication() {

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
        createOtherShortCut()
        registerKeyVipList()

        RateInApp.instance.registerActivityLifecycle(this)
        RateInApp.instance.isHideNavigationBar = true
        RateInApp.instance.isHideStatusBar = true
        RateInApp.instance.isSpaceStatusBar = true
        RateInApp.instance.isSpaceDisplayCutout = true
    }



    private fun createOtherShortCut() {
        //TODO tạo thêm shortcut
    }

    /**
     * Đăng ký các key để xác định userVip của ứng dụng (đây là các key lưu trạng thái mua các gói vip trong ứng dụng)
     */
    private fun registerKeyVipList() {
        /*purchasePreferences.registerKeyVipList(
            keyVipList = mutableListOf(
                KEY_IS_PRO_LIFE_TIME,
                KEY_IS_PRO_BY_YEAR,
                KEY_IS_PRO_BY_MONTH,
                KEY_IS_PRO_BY_WEEK,
            )
        )*/
    }

    companion object {

        lateinit var instance: App
    }
}