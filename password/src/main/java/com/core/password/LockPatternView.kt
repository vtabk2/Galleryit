package com.core.password

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Debug
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min

class LockPatternView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var mDrawingProfilingStarted = false

    private val mPaint = Paint()
    private val mPathPaint = Paint()

    private var mOnPatternListener: OnPatternListener? = null
    private val mPattern = java.util.ArrayList<Cell>(9)

    private val mPatternDrawLookup = Array<BooleanArray?>(3) { BooleanArray(3) }

    private var mInProgressX = -1f
    private var mInProgressY = -1f

    private var mInputEnabled = true
    var isInStealthMode: Boolean = false
    var isTactileFeedbackEnabled: Boolean = true
    private var mPatternInProgress = false

    private val mDiameterFactor = 0.5f
    private val mHitFactor = 0.6f

    private var mSquareWidth = 0f
    private var mSquareHeight = 0f

    private val mBitmapBtnDefault: Bitmap
    private val mBitmapBtnTouched: Bitmap
    private val mBitmapCircleDefault: Bitmap
    private var mBitmapCircleColored: Bitmap? = null

    private var mBitmapArrowColoredUp: Bitmap? = null

    private val mCurrentPath = Path()
    private val mInvalidate = Rect()

    private val mBitmapWidth: Int
    private val mBitmapHeight: Int

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val mCircleMap: HashMap<DrawingColor?, Bitmap?>

    private val mArrowMap: HashMap<DrawingColor?, Bitmap?>

    class Cell private constructor(row: Int, column: Int) {
        val row: Int
        val column: Int

        init {
            checkRange(row, column)
            this.row = row
            this.column = column
        }

        override fun toString(): String {
            return "(row=" + row + ",clmn=" + column + ")"
        }

        companion object {
            private val sCells = Array<Array<Cell?>?>(3) { arrayOfNulls<Cell>(3) }

            init {
                for (i in 0..2) {
                    for (j in 0..2) {
                        sCells[i]!![j] = Cell(i, j)
                    }
                }
            }

            @Synchronized
            fun of(row: Int, column: Int): Cell? {
                checkRange(row, column)
                return sCells[row]!![column]
            }

            private fun checkRange(row: Int, column: Int) {
                require(!(row < 0 || row > 2)) { "row must be in range 0-2" }
                require(!(column < 0 || column > 2)) { "column must be in range 0-2" }
            }
        }
    }

    interface OnPatternListener {
        fun onPatternStart()

        fun onPatternCleared()

        fun onPatternCellAdded(pattern: MutableList<Cell>?)

        fun onPatternDetected(pattern: MutableList<Cell>?)
    }

    init {
        isClickable = true

        mPathPaint.isAntiAlias = true
        mPathPaint.isDither = true
        mPathPaint.setColor(Color.WHITE)
        mPathPaint.style = Paint.Style.STROKE
        mPathPaint.strokeJoin = Paint.Join.ROUND
        mPathPaint.strokeCap = Paint.Cap.ROUND

        mBitmapBtnDefault = getBitmapFor(R.drawable.btn_code_lock_default)
        mBitmapBtnTouched = getBitmapFor(R.drawable.btn_code_lock_touched)
        mBitmapCircleDefault = getBitmapFor(R.drawable.btn_code_lock_default)

        mCircleMap = HashMap<DrawingColor?, Bitmap?>()
        mCircleMap.put(
            DrawingColor.GREEN,
            getBitmapFor(R.drawable.btn_code_lock_touched)
        )
        mCircleMap.put(
            DrawingColor.YELLOW,
            getBitmapFor(R.drawable.btn_code_lock_touched)
        )
        mCircleMap.put(
            DrawingColor.ORANGE,
            getBitmapFor(R.drawable.btn_code_lock_touched)
        )
        mCircleMap.put(
            DrawingColor.RED,
            getBitmapFor(R.drawable.btn_code_lock_touched)
        )

        mArrowMap = HashMap<DrawingColor?, Bitmap?>()
        mArrowMap.put(DrawingColor.GREEN, getBitmapFor(R.drawable.indicator_code_lock_drag_direction_orange_up))
        mArrowMap.put(DrawingColor.RED, getBitmapFor(R.drawable.indicator_code_lock_drag_direction_orange_up))
        mArrowMap.put(DrawingColor.YELLOW, getBitmapFor(R.drawable.indicator_code_lock_drag_direction_orange_up))
        mArrowMap.put(DrawingColor.ORANGE, getBitmapFor(R.drawable.indicator_code_lock_drag_direction_orange_up))
        setDrawingColor(DrawingColor.GREEN)

        mBitmapWidth = mBitmapBtnDefault.getWidth()
        mBitmapHeight = mBitmapBtnDefault.getHeight()
    }

    enum class DrawingColor {
        GREEN, YELLOW, ORANGE, RED
    }

    fun setDrawingColor(color: DrawingColor?) {
        mBitmapCircleColored = mCircleMap.get(color)
        mBitmapArrowColoredUp = mArrowMap.get(color)
        invalidate()
    }

    private fun getBitmapFor(resId: Int): Bitmap {
        return BitmapFactory.decodeResource(getContext().getResources(), resId)
    }

    fun setOnPatternListener(onPatternListener: OnPatternListener?) {
        mOnPatternListener = onPatternListener
    }

    var pattern: ArrayList<Cell>
        get() = mPattern
        set(pattern) {
            mPattern.clear()
            mPattern.addAll(pattern)
            clearPatternDrawLookup()
            for (cell in pattern) {
                mPatternDrawLookup[cell.row]!![cell.column] = true
            }
        }

    fun clearPattern() {
        resetPattern()
    }

    private fun resetPattern() {
        mPattern.clear()
        clearPatternDrawLookup()
        invalidate()
    }

    private fun clearPatternDrawLookup() {
        for (i in 0..2) {
            for (j in 0..2) {
                mPatternDrawLookup[i]!![j] = false
            }
        }
    }

    fun disableInput() {
        mInputEnabled = false
    }

    fun enableInput() {
        mInputEnabled = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val width = w - getPaddingLeft() - getPaddingRight()
        mSquareWidth = width / 3.0f

        val height = h - paddingTop - paddingBottom
        mSquareHeight = height / 3.0f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val squareSide = min(width, height)
        setMeasuredDimension(squareSide, squareSide)
    }

    private fun detectAndAddHit(x: Float, y: Float): Cell? {
        val cell = checkForNewHit(x, y)
        if (cell != null) {
            // check for gaps in existing pattern

            var fillInGapCell: Cell? = null
            val pattern = mPattern
            if (!pattern.isEmpty()) {
                val lastCell = pattern.get(pattern.size - 1)
                val dRow = cell.row - lastCell.row
                val dColumn = cell.column - lastCell.column

                var fillInRow = lastCell.row
                var fillInColumn = lastCell.column

                if (abs(dRow) == 2 && abs(dColumn) != 1) {
                    fillInRow = lastCell.row + (if (dRow > 0) 1 else -1)
                }

                if (abs(dColumn) == 2 && abs(dRow) != 1) {
                    fillInColumn = lastCell.column + (if (dColumn > 0) 1 else -1)
                }

                fillInGapCell = Cell.Companion.of(fillInRow, fillInColumn)
            }

            if (fillInGapCell != null
                && !mPatternDrawLookup[fillInGapCell.row]!![fillInGapCell.column]
            ) {
                addCellToPattern(fillInGapCell)
            }
            addCellToPattern(cell)
            if (this.isTactileFeedbackEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(VIBE_PATTERN, -1))
                } else {
                    vibrator?.vibrate(VIBE_PATTERN, -1)
                }
            }
            return cell
        }
        return null
    }

    private fun addCellToPattern(newCell: Cell) {
        mPatternDrawLookup[newCell.row]!![newCell.column] = true
        mPattern.add(newCell)
        if (mOnPatternListener != null) {
            mOnPatternListener!!.onPatternCellAdded(mPattern)
        }
    }

    private fun checkForNewHit(x: Float, y: Float): Cell? {
        val rowHit = getRowHit(y)
        if (rowHit < 0) {
            return null
        }
        val columnHit = getColumnHit(x)
        if (columnHit < 0) {
            return null
        }

        if (mPatternDrawLookup[rowHit]!![columnHit]) {
            return null
        }
        return Cell.Companion.of(rowHit, columnHit)
    }

    private fun getRowHit(y: Float): Int {
        val squareHeight = mSquareHeight
        val hitSize = squareHeight * mHitFactor

        val offset = getPaddingTop() + (squareHeight - hitSize) / 2f
        for (i in 0..2) {
            val hitTop = offset + squareHeight * i
            if (y >= hitTop && y <= hitTop + hitSize) {
                return i
            }
        }
        return -1
    }

    private fun getColumnHit(x: Float): Int {
        val squareWidth = mSquareWidth
        val hitSize = squareWidth * mHitFactor

        val offset = getPaddingLeft() + (squareWidth - hitSize) / 2f
        for (i in 0..2) {
            val hitLeft = offset + squareWidth * i
            if (x >= hitLeft && x <= hitLeft + hitSize) {
                return i
            }
        }
        return -1
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        if (!mInputEnabled || !isEnabled()) {
            return false
        }

        val x = motionEvent.getX()
        val y = motionEvent.getY()
        var hitCell: Cell?
        Log.d("hnv12345", "onTouchEvent: " + motionEvent.getAction())
        when (motionEvent.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                resetPattern()
                hitCell = detectAndAddHit(x, y)
                if (hitCell != null && mOnPatternListener != null) {
                    mPatternInProgress = true
                    mOnPatternListener!!.onPatternStart()
                } else if (mOnPatternListener != null) {
                    mPatternInProgress = false
                    mOnPatternListener!!.onPatternCleared()
                }
                if (hitCell != null) {
                    val startX = getCenterXForColumn(hitCell.column)
                    val startY = getCenterYForRow(hitCell.row)

                    val widthOffset = mSquareWidth / 2f
                    val heightOffset = mSquareHeight / 2f

                    invalidate(
                        (startX - widthOffset).toInt(),
                        (startY - heightOffset).toInt(),
                        (startX + widthOffset).toInt(),
                        (startY + heightOffset).toInt()
                    )
                }
                mInProgressX = x
                mInProgressY = y
                if (PROFILE_DRAWING) {
                    if (!mDrawingProfilingStarted) {
                        Debug.startMethodTracing("LockPatternDrawing")
                        mDrawingProfilingStarted = true
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                // report pattern detected
                if (!mPattern.isEmpty() && mOnPatternListener != null) {
                    mPatternInProgress = false
                    mOnPatternListener!!.onPatternDetected(mPattern)
                    invalidate()
                }
                if (PROFILE_DRAWING) {
                    if (mDrawingProfilingStarted) {
                        Debug.stopMethodTracing()
                        mDrawingProfilingStarted = false
                    }
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val patternSizePreHitDetect = mPattern.size
                hitCell = detectAndAddHit(x, y)
                val patternSize = mPattern.size
                if (hitCell != null && (mOnPatternListener != null)
                    && (patternSize == 1)
                ) {
                    mPatternInProgress = true
                    mOnPatternListener!!.onPatternStart()
                }
                // note current x and y for rubber banding of in progress
                // patterns
                val dx = abs(x - mInProgressX)
                val dy = abs(y - mInProgressY)
                if (dx + dy > mSquareWidth * 0.01f) {
                    var oldX = mInProgressX
                    var oldY = mInProgressY

                    mInProgressX = x
                    mInProgressY = y

                    if (mPatternInProgress && patternSize > 0) {
                        val pattern = mPattern
                        val radius = mSquareWidth * mDiameterFactor * 0.5f

                        val lastCell = pattern.get(patternSize - 1)

                        var startX = getCenterXForColumn(lastCell.column)
                        var startY = getCenterYForRow(lastCell.row)

                        var left: Float
                        var top: Float
                        var right: Float
                        var bottom: Float

                        val invalidateRect = mInvalidate

                        if (startX < x) {
                            left = startX
                            right = x
                        } else {
                            left = x
                            right = startX
                        }

                        if (startY < y) {
                            top = startY
                            bottom = y
                        } else {
                            top = y
                            bottom = startY
                        }

                        // Invalidate between the pattern's last cell and the
                        // current location
                        invalidateRect.set(
                            (left - radius).toInt(),
                            (top - radius).toInt(), (right + radius).toInt(),
                            (bottom + radius).toInt()
                        )

                        if (startX < oldX) {
                            left = startX
                            right = oldX
                        } else {
                            left = oldX
                            right = startX
                        }

                        if (startY < oldY) {
                            top = startY
                            bottom = oldY
                        } else {
                            top = oldY
                            bottom = startY
                        }

                        // Invalidate between the pattern's last cell and the
                        // previous location
                        invalidateRect.union(
                            (left - radius).toInt(),
                            (top - radius).toInt(), (right + radius).toInt(),
                            (bottom + radius).toInt()
                        )

                        // Invalidate between the pattern's new cell and the
                        // pattern's previous cell
                        if (hitCell != null) {
                            startX = getCenterXForColumn(hitCell.column)
                            startY = getCenterYForRow(hitCell.row)

                            if (patternSize >= 2) {
                                // (re-using hitcell for old cell)
                                hitCell = pattern.get(
                                    (patternSize - 1
                                            - (patternSize - patternSizePreHitDetect))
                                )
                                oldX = getCenterXForColumn(hitCell.column)
                                oldY = getCenterYForRow(hitCell.row)

                                if (startX < oldX) {
                                    left = startX
                                    right = oldX
                                } else {
                                    left = oldX
                                    right = startX
                                }

                                if (startY < oldY) {
                                    top = startY
                                    bottom = oldY
                                } else {
                                    top = oldY
                                    bottom = startY
                                }
                            } else {
                                right = startX
                                left = right
                                bottom = startY
                                top = bottom
                            }

                            val widthOffset = mSquareWidth / 2f
                            val heightOffset = mSquareHeight / 2f

                            invalidateRect.set(
                                (left - widthOffset).toInt(),
                                (top - heightOffset).toInt(),
                                (right + widthOffset).toInt(),
                                (bottom + heightOffset).toInt()
                            )
                        }

                        invalidate(invalidateRect)
                    } else {
                        invalidate()
                    }
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                resetPattern()
                if (mOnPatternListener != null) {
                    mPatternInProgress = false
                    mOnPatternListener!!.onPatternCleared()
                }
                if (PROFILE_DRAWING) {
                    if (mDrawingProfilingStarted) {
                        Debug.stopMethodTracing()
                        mDrawingProfilingStarted = false
                    }
                }
                return true
            }
        }
        return false
    }

    private fun getCenterXForColumn(column: Int): Float {
        return getPaddingLeft() + column * mSquareWidth + mSquareWidth / 2f
    }

    private fun getCenterYForRow(row: Int): Float {
        return getPaddingTop() + row * mSquareHeight + mSquareHeight / 2f
    }

    override fun onDraw(canvas: Canvas) {
        val pattern = mPattern
        val count = pattern.size
        val drawLookup = mPatternDrawLookup

        val squareWidth = mSquareWidth
        val squareHeight = mSquareHeight

        val radius = (squareWidth * mDiameterFactor * 0.5f) * 2 / 3
        mPathPaint.strokeWidth = radius / 6

        val currentPath = mCurrentPath
        currentPath.rewind()

        val drawPath = (!this.isInStealthMode)
        if (drawPath) {
            var anyCircles = false
            for (i in 0..<count) {
                val cell = pattern.get(i)

                if (!drawLookup[cell.row]!![cell.column]) {
                    break
                }
                anyCircles = true

                val centerX = getCenterXForColumn(cell.column)
                val centerY = getCenterYForRow(cell.row)
                if (i == 0) {
                    currentPath.moveTo(centerX, centerY)
                } else {
                    currentPath.lineTo(centerX, centerY)
                }
            }

            // add last in progress section
            if (mPatternInProgress && anyCircles) {
                currentPath.lineTo(mInProgressX, mInProgressY)
            }
            canvas.drawPath(currentPath, mPathPaint)
        }

        // draw the circles
        val paddingTop = getPaddingTop()
        val paddingLeft = getPaddingLeft()

        for (i in 0..2) {
            val topY = paddingTop + i * squareHeight
            // float centerY = mPaddingTop + i * mSquareHeight + (mSquareHeight
            // / 2);
            for (j in 0..2) {
                val leftX = paddingLeft + j * squareWidth
                drawCircle(canvas, leftX.toInt(), topY.toInt(), drawLookup[i]!![j])
            }
        }

        val oldFlag = (mPaint.flags and Paint.FILTER_BITMAP_FLAG) != 0
        mPaint.isFilterBitmap = true // draw with higher quality since we
        // render with transforms
        if (drawPath) {
            for (i in 0..<count - 1) {
                val cell = pattern.get(i)
                val next = pattern.get(i + 1)

                if (!drawLookup[next.row]!![next.column]) {
                    break
                }

                val leftX = paddingLeft + cell.column * squareWidth
                val topY = paddingTop + cell.row * squareHeight

                drawArrow(canvas, leftX, topY, cell, next)
            }
        }
        mPaint.isFilterBitmap = oldFlag // restore default flag
    }

    private fun drawArrow(
        canvas: Canvas, leftX: Float, topY: Float, start: Cell,
        end: Cell
    ) {
        val endRow = end.row
        val startRow = start.row
        val endColumn = end.column
        val startColumn = start.column

        // offsets for centering the bitmap in the cell
        val offsetX = (mSquareWidth.toInt() - mBitmapWidth) / 2
        val offsetY = (mSquareHeight.toInt() - mBitmapHeight) / 2

        val matrix = Matrix()
        val cellWidth = mBitmapCircleDefault.getWidth()
        val cellHeight = mBitmapCircleDefault.getHeight()

        // the up arrow bitmap is at 12:00, so find the rotation from x axis and
        // add 90 degrees.
        val theta = atan2(
            (endRow - startRow).toDouble(),
            (endColumn - startColumn).toDouble()
        ).toFloat()
        val angle = Math.toDegrees(theta.toDouble()).toFloat() + 90.0f

        // compose matrix
        matrix.setTranslate(leftX + offsetX, topY + offsetY) // transform to
        // cell position
        matrix.preRotate(angle, cellWidth / 2.0f, cellHeight / 2.0f) // rotate
        // about
        // cell
        // center
        matrix.preTranslate(
            (cellWidth - mBitmapArrowColoredUp!!.getWidth()) / 2.0f, 0.0f
        ) // translate
        // to
        // 12:00
        // pos
        canvas.drawBitmap(mBitmapArrowColoredUp!!, matrix, mPaint)
    }

    private fun drawCircle(
        canvas: Canvas, leftX: Int, topY: Int,
        partOfPattern: Boolean
    ) {
        val outerCircle: Bitmap?
        val innerCircle: Bitmap

        if (!partOfPattern || this.isInStealthMode) {
            // unselected circle
            outerCircle = mBitmapCircleDefault
            innerCircle = mBitmapBtnDefault
        } else if (mPatternInProgress) {
            // user is in middle of drawing a pattern
            outerCircle = mBitmapCircleColored
            innerCircle = mBitmapBtnTouched
        } else {
            // the pattern is correct
            outerCircle = mBitmapCircleColored
            innerCircle = mBitmapBtnTouched
        }

        val width = mBitmapWidth
        val height = mBitmapHeight

        val squareWidth = mSquareWidth
        val squareHeight = mSquareHeight

        val offsetX = ((squareWidth - width) / 2f).toInt()
        val offsetY = ((squareHeight - height) / 2f).toInt()

        canvas.drawBitmap(outerCircle!!, (leftX + offsetX).toFloat(), (topY + offsetY).toFloat(), mPaint)
        canvas.drawBitmap(innerCircle, (leftX + offsetX).toFloat(), (topY + offsetY).toFloat(), mPaint)
    }

    companion object {
        private val VIBE_PATTERN = longArrayOf(0, 2)
        private const val PROFILE_DRAWING = false
    }
}
