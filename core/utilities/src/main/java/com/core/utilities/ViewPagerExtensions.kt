package com.core.utilities

import androidx.viewpager.widget.ViewPager

/**
 * Đặt trang hiện tại của ViewPager mà không gây crash khi
 * FragmentManager đang thực hiện transaction.
 *
 * - Nếu đang ở đúng trang thì bỏ qua.
 * - Nếu ViewPager đang idle thì đổi ngay.
 * - Nếu đang scroll thì chờ đến khi dừng mới đổi.
 */
fun ViewPager.setCurrentItemFixCrash(item: Int, smoothScroll: Boolean = false) {
    if (currentItem == item) return

    // Dùng cờ nội bộ để xác định trạng thái scroll
    var isIdle: Boolean

    val listener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
            // 0 = idle, 1 = dragging, 2 = settling
            isIdle = state == ViewPager.SCROLL_STATE_IDLE
            if (isIdle) {
                removeOnPageChangeListener(this)
                post { setCurrentItem(item, smoothScroll) }
            }
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        override fun onPageSelected(position: Int) {}
    }

    // Kiểm tra nhanh nếu đang idle (state mặc định)
    try {
        // Nếu chưa cuộn → đổi ngay
        post { setCurrentItem(item, smoothScroll) }
    } catch (_: Exception) {
        // Nếu có lỗi (đang cuộn) → chờ callback
        addOnPageChangeListener(listener)
    }
}