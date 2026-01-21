package com.codebasetemplate.features.app.main.adapter

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

class PickPhotoRecyclerViewItemListener(val context: Context, val position: Int, var listener: PickPhotoGestureListener? = null) : View.OnTouchListener {
    private var isLongPress = false
    private var handleTouch = false

    private val gestureDetector = GestureDetector(context, object :
        GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            isLongPress = true
            listener?.onLongTouch(position, e)
        }
    })

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                view?.isPressed = true
                listener?.onTouchDown(event)
            }

            MotionEvent.ACTION_UP -> {
                view?.isPressed = false
                if (isLongPress) {
                    listener?.onTouchUp(position)
                    isLongPress = false
                } else {
                    listener?.onClick(position)
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                view?.isPressed = false
            }
        }
        handleTouch = gestureDetector.onTouchEvent(event)
        return handleTouch
    }

    interface PickPhotoGestureListener {
        fun onClick(position: Int)

        fun onLongTouch(position: Int, event: MotionEvent?) {}

        fun onTouchUp(position: Int) {}

        fun onTouchDown(event: MotionEvent?)
    }
}