package com.core.password

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.sqrt

class PatternView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val dotPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val path = Path()
    private val dots = mutableListOf<Dot>()
    private val selectedDots = mutableListOf<Dot>()
    private var isDrawing = false
    private var currentDot: Dot? = null

    private var onPatternListener: OnPatternListener? = null

    interface OnPatternListener {
        fun onPattern(pattern: List<Int>)
    }

    private data class Dot(
        val x: Float,
        val y: Float,
        val index: Int
    )

    init {
        setupDots()
    }

    private fun setupDots() {
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = minOf(width, height) / 4f

        dots.clear()
        for (i in 0..8) {
            val row = i / 3
            val col = i % 3
            val x = centerX + (col - 1) * radius
            val y = centerY + (row - 1) * radius
            dots.add(Dot(x, y, i))
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setupDots()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw dots
        dots.forEach { dot ->
            val isSelected = selectedDots.contains(dot)
            val color = if (isSelected) {
                ContextCompat.getColor(context, R.color.primary_color)
            } else {
                ContextCompat.getColor(context, R.color.text_2)
            }
            dotPaint.color = color
            canvas.drawCircle(dot.x, dot.y, 20f, dotPaint)
        }

        // Draw lines
        if (selectedDots.size > 1) {
            paint.color = ContextCompat.getColor(context, R.color.primary_color)
            path.reset()
            path.moveTo(selectedDots[0].x, selectedDots[0].y)
            for (i in 1 until selectedDots.size) {
                path.lineTo(selectedDots[i].x, selectedDots[i].y)
            }
            canvas.drawPath(path, paint)
        }

        // Draw current line
        currentDot?.let { dot ->
            if (selectedDots.isNotEmpty()) {
                paint.color = ContextCompat.getColor(context, R.color.primary_color)
                canvas.drawLine(
                    selectedDots.last().x,
                    selectedDots.last().y,
                    dot.x,
                    dot.y,
                    paint
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDrawing = true
                val dot = getDotAt(event.x, event.y)
                if (dot != null && !selectedDots.contains(dot)) {
                    selectedDots.add(dot)
                    currentDot = null
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDrawing) {
                    val dot = getDotAt(event.x, event.y)
                    if (dot != null && !selectedDots.contains(dot)) {
                        selectedDots.add(dot)
                        currentDot = null
                    } else {
                        currentDot = Dot(event.x, event.y, -1)
                    }
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                isDrawing = false
                currentDot = null
                if (selectedDots.size >= 3) {
                    val pattern = selectedDots.map { it.index }
                    onPatternListener?.onPattern(pattern)
                }
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getDotAt(x: Float, y: Float): Dot? {
        val touchRadius = 50f
        return dots.find { dot ->
            val distance = sqrt((x - dot.x) * (x - dot.x) + (y - dot.y) * (y - dot.y))
            distance <= touchRadius
        }
    }

    fun setOnPatternListener(listener: OnPatternListener?) {
        onPatternListener = listener
    }

    fun clearPattern() {
        selectedDots.clear()
        currentDot = null
        invalidate()
    }
}
