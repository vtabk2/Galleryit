package com.core.utilities

import androidx.viewpager2.widget.ViewPager2

/**
 * Đặt trang hiện tại của ViewPager2 mà không gây lỗi
 * IllegalStateException: FragmentManager is already executing transactions.
 *
 * Cơ chế:
 * - Nếu đang ở đúng trang thì bỏ qua.
 * - Nếu ViewPager đang idle → đổi trang ngay (qua post).
 * - Nếu ViewPager đang scroll → chờ đến khi idle mới đổi.
 */
fun ViewPager2.setCurrentItemFixCrash(item: Int, smoothScroll: Boolean = false) {
    if (currentItem == item) return

    if (scrollState == ViewPager2.SCROLL_STATE_IDLE) {
        // Đang rảnh → đổi ngay (qua post để đảm bảo an toàn thread)
        post { setCurrentItem(item, smoothScroll) }
    } else {
        // Nếu đang scroll → chờ đến khi dừng
        registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    unregisterOnPageChangeCallback(this)
                    post { setCurrentItem(item, smoothScroll) }
                }
            }
        })
    }
}