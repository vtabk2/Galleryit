package com.codebasetemplate.features.app.customview

import android.content.Context
import android.util.DisplayMetrics
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

class SpeedyLinearLayoutManager : LinearLayoutManager {
    var timeScroll = MILLISECONDS_PER_INCH_DEFAULT

    constructor(context: Context, orientation: Int, reverseLayout: Boolean, timeScroll: Float) : super(context, orientation, reverseLayout) {
        this.timeScroll = timeScroll
    }

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State, position: Int) {
        try {
            val linearSmoothScroller: LinearSmoothScroller = object : LinearSmoothScroller(recyclerView.context) {
                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                    return timeScroll / displayMetrics.densityDpi
                }
            }
            if (position < 0) return
            if (position >= recyclerView.size) return
            linearSmoothScroller.targetPosition = position
            startSmoothScroll(linearSmoothScroller)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        //default is 25f (bigger = slower)
        const val MILLISECONDS_PER_INCH_MIN = 5f
        const val MILLISECONDS_PER_INCH_DEFAULT = 25f
        const val MILLISECONDS_PER_INCH_HIGH = 75f
        const val MILLISECONDS_PER_INCH_HIGH_2 = 175f
        const val MILLISECONDS_PER_INCH_MAX = 225f
    }
}