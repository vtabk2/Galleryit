package com.core.utilities

import android.content.res.Resources
import android.graphics.Color

inline val Int.dpInt: Int get() = (Resources.getSystem().displayMetrics.density * this).toInt()
inline val Int.dp: Float get() = Resources.getSystem().displayMetrics.density * this
inline val Float.dp: Float get() = Resources.getSystem().displayMetrics.density * this
inline val Int.zeroFix: String get() = if (this <= 9) "0${this}" else toString()

inline val Float.dpToPx: Float get() = this.dp
inline val Int.dpToPx: Float get() = this.dp
inline val Int.dpToPxInt: Int get() = this.dp.toInt()

fun Int.toHexColor(): String {
    val stringBuilder = StringBuilder()
    stringBuilder.append("#")
    val alpha = Color.alpha(this).toString(16)
    val redHex = Color.red(this).toString(16)
    val greenHex = Color.green(this).toString(16)
    val blueHex = Color.blue(this).toString(16)
    if (alpha.length <= 1) stringBuilder.append("0$alpha") else stringBuilder.append(alpha)
    if (redHex.length <= 1) stringBuilder.append("0$redHex") else stringBuilder.append(redHex)
    if (greenHex.length <= 1) stringBuilder.append("0$greenHex") else stringBuilder.append(greenHex)
    if (blueHex.length <= 1) stringBuilder.append("0$blueHex") else stringBuilder.append(blueHex)
    return stringBuilder.toString()
}

fun colorToHex(color: Int): String {
    return String.format("#%06X", 0xFFFFFF and color)
}
