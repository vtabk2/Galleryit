package com.core.password.extensions

import android.view.Gravity
import android.view.View.LAYOUT_DIRECTION_RTL
import android.view.View.TEXT_DIRECTION_LOCALE
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import kotlin.math.max
import kotlin.math.min


fun EditText.actionNext(actionNext: () -> Unit) {
    this.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            actionNext.invoke()
            return@OnEditorActionListener true
        }
        return@OnEditorActionListener false
    })
}

fun EditText.actionDone(actionDone: () -> Unit) {
    this.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            actionDone.invoke()
            return@OnEditorActionListener true
        }
        return@OnEditorActionListener false
    })
}

fun EditText.prependText(content: String) {
    if (content.isNotEmpty()) {
        val newText = "${content}${this.text}"
        this.setText(newText)
    }
}

fun EditText.disableEdit() {
    isFocusable = false
    isFocusableInTouchMode = false
    isClickable = false
    isLongClickable = false
    isCursorVisible = false
}

fun EditText.enableEdit() {
    isFocusable = true
    isFocusableInTouchMode = true
    isClickable = true
    isLongClickable = true
    isCursorVisible = true

}

fun EditText.applyDirection() {
    this.apply {
        val isRtl = layoutDirection == LAYOUT_DIRECTION_RTL
        textDirection = TEXT_DIRECTION_LOCALE

        if (isRtl) {
            textDirection = TEXT_DIRECTION_LOCALE
            val vertical = gravity and Gravity.VERTICAL_GRAVITY_MASK
            gravity = Gravity.END or vertical
        } else {
            textDirection = TEXT_DIRECTION_LOCALE
            val vertical = gravity and Gravity.VERTICAL_GRAVITY_MASK
            gravity = Gravity.START or vertical
        }
    }
}

fun EditText.setSafeSelection(index: Int?) {
    val editable = this.text ?: return
    val length = editable.length

    val safeIndex = when {
        index == null -> length
        index < 0 -> 0
        index > length -> length
        else -> index
    }

    // Avoid unnecessary call
    if (selectionStart == safeIndex && selectionEnd == safeIndex) return

    try {
        setSelection(safeIndex)
    } catch (e: Exception) {
        // Absolute safety net (should never happen)
        setSelection(length)
    }
}

fun EditText.setSafeSelection(start: Int?, end: Int?) {
    val editable = this.text ?: return
    val length = editable.length

    val safeStart = when {
        start == null -> length
        start < 0 -> 0
        start > length -> length
        else -> start
    }

    val safeEnd = when {
        end == null -> safeStart
        end < 0 -> 0
        end > length -> length
        else -> end
    }

    val finalStart = min(safeStart, safeEnd)
    val finalEnd = max(safeStart, safeEnd)

    try {
        setSelection(finalStart, finalEnd)
    } catch (e: Exception) {
        setSelection(length)
    }
}
