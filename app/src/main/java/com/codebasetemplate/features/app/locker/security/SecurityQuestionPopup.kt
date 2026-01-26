package com.codebasetemplate.features.app.locker.security

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import com.core.utilities.dpInt
import com.core.utilities.dpToPx
import com.core.utilities.dpToPxInt

class SecurityQuestionPopup(
    private val context: Context,
) {
    private var popupWindow: PopupWindow? = null
    private val securityQuestion: SecurityQuestion by lazy { SecurityQuestion(context) }


    fun show(anchorView: View, onClickQuestion: ((Int) -> Unit)? = null) {
        if (popupWindow?.isShowing == true) {
            return
        }
        val width = context.resources.displayMetrics.widthPixels - 32.dpToPxInt
        val height = 280.dpInt

        // Measure the menuSort view with the fixed width
        securityQuestion.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        popupWindow = PopupWindow(securityQuestion, width, height).apply {
            // Tạo background với màu và đổ bóng
            val backgroundDrawable = createPopupBackground()
            setBackgroundDrawable(backgroundDrawable)
            isOutsideTouchable = true // Cho phép đóng popup khi click ra ngoài
            elevation = 0f.dpToPx // Tăng elevation để có đổ bóng đẹp hơn

            securityQuestion.onClickQuestion = onClickQuestion

            // Show the popup below the anchor view
            showAsDropDown(anchorView, 0, 0, Gravity.START)
        }
    }

    fun dismiss() {
        popupWindow?.dismiss()
        popupWindow = null
    }

    /**
     * Tạo background drawable với màu và đổ bóng cho popup
     */
    private fun createPopupBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f.dpToPx
            setColor(Color.WHITE) // Sử dụng màu surface thay vì màu đỏ
        }
    }
} 