package com.core.password

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.autofill.HintConstants
import androidx.core.content.withStyledAttributes
import androidx.core.view.updatePadding
import com.core.password.extensions.setSafeSelection

/**
 * OtpView: 1 EditText nhận input + các ô hiển thị gạch chân.
 * - Nhập tuần tự, xóa cuối->đầu, caret luôn ghim ở cuối.
 * - Paste/autofill được sanitize, cắt theo length.
 * - underline đổi màu theo state: INACTIVE/ACTIVE/ERROR/SUCCESS.
 */
class OtpView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    // region Public API --------------------------------------------------------

    interface Listener {
        fun onChanged(current: String) {}
        fun onComplete(code: String) {}
    }

    var listener: Listener? = null

    /** Code hiện tại */
    var otp: String
        get() = input.text?.toString().orEmpty()
        set(value) = setOtp(value, fromUser = false)

    /** Đặt độ dài OTP runtime (recreate boxes) */
    fun setLength(newLen: Int) {
        if (newLen <= 0 || newLen == length) return
        length = newLen
        applyInputFilters()
        buildBoxes()
        setOtp(otp.take(length), fromUser = false)
    }

    fun clear() = setOtp("", fromUser = false)
    fun isComplete(): Boolean = otp.length == length

    fun requestFocusOtp() {
        input.requestFocus()
        moveCursorToEnd()
        showKeyboard()
    }

    fun setErrorState(enabled: Boolean) {
        state = if (enabled) State.ERROR else State.NORMAL
        refreshBoxesStates()
        if (enabled) performHapticFeedback(HapticFeedbackConstants.REJECT)
    }

    fun setSuccessState(enabled: Boolean) {
        state = if (enabled) State.SUCCESS else State.NORMAL
        refreshBoxesStates()
    }

    /** Callback ngắn gọn khi đủ ký tự */
    fun setOnOtpComplete(block: (String) -> Unit) {
        onOtpComplete = block
    }

    // endregion ----------------------------------------------------------------

    // region Config attrs (kèm default) ----------------------------------------

    private var length: Int = 6
    private var boxWidthPx: Int = dp(40)
    private var boxHeightPx: Int = dp(48)
    private var boxSpacingPx: Int = dp(8)

    @ColorInt
    private var colorText: Int = Color.WHITE

    // underline
    private var underlineHeightPx: Int = dp(2)

    @ColorInt
    private var underlineColor: Int = 0x66FFFFFF.toInt()    // normal nhạt

    @ColorInt
    private var underlineActive: Int = Color.WHITE          // active trắng

    @ColorInt
    private var underlineError: Int = 0xFFE53935.toInt()    // đỏ

    @ColorInt
    private var underlineSuccess: Int = 0xFF43A047.toInt()  // xanh

    // endregion ----------------------------------------------------------------

    // region Internals ---------------------------------------------------------

    private val container = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
    }

    /** EditText nhận input (ẩn bằng alpha) */
    private val input = OtpEditText(context).apply {
        background = null
        alpha = 0f                 // ẩn hình ảnh nhưng vẫn focus được
        isFocusable = true
        isFocusableInTouchMode = true
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        imeOptions = EditorInfo.IME_ACTION_DONE
        isCursorVisible = false
        // filters sẽ gán sau khi đọc attrs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            importantForAutofill = IMPORTANT_FOR_AUTOFILL_YES
            // gợi ý các loại OTP phổ biến (SMS, Email, 2FA app)
            setAutofillHints(
                HintConstants.AUTOFILL_HINT_SMS_OTP,
                HintConstants.AUTOFILL_HINT_EMAIL_OTP,
                HintConstants.AUTOFILL_HINT_2FA_APP_OTP
            )
        }
        // khoá caret luôn ở cuối
        selectionLocker = { moveCursorToEnd() }
    }

    private val boxes = mutableListOf<OtpBoxView>()
    private var programmaticChange = false
    private var onOtpComplete: ((String) -> Unit)? = null
    private var state: State = State.NORMAL

    private enum class State { NORMAL, ERROR, SUCCESS }

    // digit filter: chỉ cho số
    private val digitFilter = InputFilter { source, start, end, _, _, _ ->
        for (i in start until end) if (!source[i].isDigit()) return@InputFilter ""
        null
    }

    // endregion ----------------------------------------------------------------

    init {
        // đọc attrs
        context.withStyledAttributes(attrs, R.styleable.OtpView) {
            length = getInt(R.styleable.OtpView_otpLength, length)
            boxWidthPx = getDimension(R.styleable.OtpView_boxWidth, boxWidthPx.toFloat()).toInt()
            boxHeightPx = getDimension(R.styleable.OtpView_boxHeight, boxHeightPx.toFloat()).toInt()
            boxSpacingPx = getDimension(R.styleable.OtpView_boxSpacing, boxSpacingPx.toFloat()).toInt()

            colorText = getColor(R.styleable.OtpView_boxTextColor, colorText)

            underlineHeightPx = getDimension(R.styleable.OtpView_underlineHeight, underlineHeightPx.toFloat()).toInt()
            underlineColor = getColor(R.styleable.OtpView_underlineColor, underlineColor)
            underlineActive = getColor(R.styleable.OtpView_underlineColorActive, underlineActive)
            underlineError = getColor(R.styleable.OtpView_underlineColorError, underlineError)
            underlineSuccess = getColor(R.styleable.OtpView_underlineColorSuccess, underlineSuccess)
        }

        // gán filter sau khi đã có length
        applyInputFilters()

        // layout tree
        addView(container, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        addView(input, LayoutParams(dp(1), dp(1), Gravity.CENTER)) // 1x1, alpha 0f

        buildBoxes()
        bindInput()

        // click bất kỳ -> focus nhập
        setOnClickListener { requestFocusOtp() }
        container.setOnClickListener { requestFocusOtp() }
    }

    // gán filters cho input
    private fun applyInputFilters() {
        input.filters = arrayOf(digitFilter, InputFilter.LengthFilter(length))
    }

    private fun buildBoxes() {
        container.removeAllViews()
        boxes.clear()

        repeat(length) { i ->
            val tv = OtpBoxView(context).apply {
                layoutParams = LinearLayout.LayoutParams(boxWidthPx, boxHeightPx).also { lp ->
                    if (i > 0) {
                        lp.marginStart = boxSpacingPx
                    }
                }
                gravity = Gravity.CENTER
                setTextColor(colorText)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
                typeface = typeface // giữ default bold nếu cần tuỳ ý
                // chừa đáy một chút để line rõ hơn
                updatePadding(bottom = dp(4))
                setState(BoxState.INACTIVE)
                setUnderline(underlineHeightPx, underlineColor)
                isFocusable = false
                isClickable = false
            }
            boxes.add(tv)
            container.addView(tv)
        }
        syncBoxes(otp)
        refreshBoxesStates()
    }

    private fun bindInput() {
        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(cs: CharSequence?, start: Int, before: Int, count: Int) {
                if (programmaticChange) return
                val raw = cs?.toString().orEmpty()
                val digits = raw.filter { it.isDigit() }.take(length)

                // sanitize nếu khác
                if (digits != raw) {
                    programmaticChange = true
                    input.setText(digits)
                    moveCursorToEnd()
                    programmaticChange = false
                }

                // reset state UI về NORMAL khi user đang gõ
                if (state == State.ERROR || state == State.SUCCESS) state = State.NORMAL

                syncBoxes(digits)
                refreshBoxesStates()
                listener?.onChanged(digits)
                if (digits.length == length) {
                    listener?.onComplete(digits)
                    onOtpComplete?.invoke(digits)
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun syncBoxes(code: String) {
        boxes.forEachIndexed { i, box ->
            box.text = code.getOrNull(i)?.toString() ?: ""
        }
    }

    private fun refreshBoxesStates() {
        val codeLen = otp.length
        boxes.forEachIndexed { i, b ->
            val isActiveSpot = (i == codeLen.coerceAtMost(length - 1) && codeLen < length)
            val targetState = when {
                state == State.ERROR -> BoxState.ERROR
                state == State.SUCCESS -> BoxState.SUCCESS
                isActiveSpot -> BoxState.ACTIVE
                else -> BoxState.INACTIVE
            }
            b.setState(targetState)
            val color = when (targetState) {
                BoxState.ERROR -> underlineError
                BoxState.SUCCESS -> underlineSuccess
                BoxState.ACTIVE -> underlineActive
                BoxState.INACTIVE -> underlineColor
            }
            b.setUnderline(underlineHeightPx, color)
        }
    }

    private fun setOtp(value: String, fromUser: Boolean) {
        val sanitized = value.filter { it.isDigit() }.take(length)
        programmaticChange = true
        input.setText(sanitized)
        moveCursorToEnd()
        programmaticChange = false

        syncBoxes(sanitized)
        refreshBoxesStates()

        if (!fromUser) listener?.onChanged(sanitized)
        if (sanitized.length == length) {
            listener?.onComplete(sanitized)
            onOtpComplete?.invoke(sanitized)
        }
    }

    private fun moveCursorToEnd() {
        input.setSafeSelection(input.text?.length ?: 0)
    }

    private fun showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
    }

    // region Saved state -------------------------------------------------------

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("super", super.onSaveInstanceState())
        bundle.putString("otp", otp)
        bundle.putInt("state", state.ordinal)
        bundle.putInt("length", length)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val bundle = state as? Bundle
        super.onRestoreInstanceState(bundle?.getParcelable("super"))
        val savedOtp = bundle?.getString("otp").orEmpty()
        this.state = State.values()[bundle?.getInt("state") ?: 0]
        val savedLen = bundle?.getInt("length") ?: length
        setLength(savedLen)
        this.otp = savedOtp
        refreshBoxesStates()
    }

    // endregion ----------------------------------------------------------------

    // region Child views & drawables ------------------------------------------

    private enum class BoxState { INACTIVE, ACTIVE, ERROR, SUCCESS }

    /** TextView ô OTP, vẽ gạch chân bằng drawable riêng */
    private class OtpBoxView(context: Context) : AppCompatTextView(context) {
        var state: BoxState = BoxState.INACTIVE
            private set
        private val underline = UnderlineDrawable()

        init {
            background = underline
        }

        fun setState(s: BoxState) {
            state = s
        }

        fun setUnderline(heightPx: Int, @ColorInt color: Int) {
            underline.heightPx = heightPx
            underline.color = color
            invalidate()
        }
    }

    /** Drawable chỉ vẽ một đường ở đáy view */
    private class UnderlineDrawable : Drawable() {
        var heightPx: Int = 2

        @ColorInt
        var color: Int = Color.WHITE
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        override fun draw(canvas: Canvas) {
            paint.color = color
            val r = RectF(bounds)
            canvas.drawRect(r.left, r.bottom - heightPx, r.right, r.bottom.toFloat(), paint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha; invalidateSelf()
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter; invalidateSelf()
        }

        @Deprecated("Deprecated in Java")
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }

    /** EditText nhận input, luôn ghim selection ở cuối */
    private class OtpEditText(ctx: Context) : AppCompatEditText(ctx) {
        var selectionLocker: (() -> Unit)? = null
        override fun onSelectionChanged(selStart: Int, selEnd: Int) {
            // ép caret về cuối
            val end = text?.length ?: 0
            selectionLocker?.invoke()
            super.onSelectionChanged(end, end)
        }
    }

    // endregion ----------------------------------------------------------------

    // region utils -------------------------------------------------------------

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    // endregion ----------------------------------------------------------------
}
