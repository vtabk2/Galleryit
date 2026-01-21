package com.codebasetemplate.required.ads

import android.util.Log
import com.core.config.domain.data.IAdPlaceName

private const val TAG = "AppAdPlaceName"
sealed class AppAdPlaceName(override val name: String): IAdPlaceName {

    /**Tạo mới cần add thêm vào list APP_AD_PLACE_LIST bên dưới*/

    object ANCHORED_NATIVE_TEST : AppAdPlaceName("anchored_native_test")
    object ANCHORED_BOTTOM_HOME : AppAdPlaceName("anchored_bottom_home")
    object ANCHORED_NATIVE_IN_LIST_TEST : AppAdPlaceName("anchored_native_in_list_test")
    object ANCHORED_BANNER_TEST : AppAdPlaceName("anchored_banner_test")
    object ANCHORED_EXIT : AppAdPlaceName("anchored_exit")
    object FULLSCREEN_TEST : AppAdPlaceName("fullscreen_test")
    object FULLSCREEN_TEST_LAZY_LOAD : AppAdPlaceName("fullscreen_test_lazy_load")
    object REWARD_TEST : AppAdPlaceName("reward_test")

    companion object {
        /**Cần add thêm vào đây nếu tạo thêm AdPlaceName*/
        val APP_AD_PLACE_LIST: List<AppAdPlaceName> by lazy {
            AppAdPlaceName::class.sealedSubclasses.mapNotNull { it.objectInstance }
        }
        // Hàm lấy ad theo key string
        fun fromKey(key: String): AppAdPlaceName? {
            Log.d(TAG, "fromKey: ${APP_AD_PLACE_LIST}")

            return APP_AD_PLACE_LIST.find {
                it.name == key
            }
        }
    }
}