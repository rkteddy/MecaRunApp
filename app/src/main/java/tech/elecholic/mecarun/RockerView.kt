package tech.elecholic.mecanum

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import tech.elecholic.mecarun.R


class RockerView: View {

    private var innerColor: Int
    private var outerColor: Int
    private var realWidth = 0f
    private var realHeight = 0f
    private var innerCircleX = 0f
    private var innerCircleY = 0f
    private var outerRadius = 0f
    private var innerRadius = 0f
    private var angle = 0f
    private var outerCircle: Paint
    private var innerCircle: Paint
    private lateinit var mListener: OnAngleChangedListener
    private var OUTER_WIDTH_SIZE: Int
    private var OUTER_HEIGHT_SIZE: Int
    private val INNER_COLOR_DEFAULT = Color.parseColor("#d32f2f")
    private val OUTER_COLOR_DEFAULT = Color.parseColor("#f44336")

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        // Obtain attributes from XML file
        val a = context.obtainStyledAttributes(attrs, R.styleable.RockerView)
        innerColor = a.getColor(R.styleable.RockerView_InnerColor, INNER_COLOR_DEFAULT)
        outerColor = a.getColor(R.styleable.RockerView_OuterColor, OUTER_COLOR_DEFAULT)
        a.recycle()
        // Convert units
        OUTER_WIDTH_SIZE = dip2px(context, 125.0f)
        OUTER_HEIGHT_SIZE = dip2px(context, 125.0f)
        // Prepare to draw circle
        outerCircle = Paint()
        innerCircle = Paint()
        outerCircle.color = outerColor
        outerCircle.style = Paint.Style.FILL_AND_STROKE
        innerCircle.color = innerColor
        innerCircle.style = Paint.Style.FILL_AND_STROKE
    }

    /**
     * Convert units from dip(dp) to px
     */
    private fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * Override onMeasure function to set a certain size of View
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec))
    }

    /**
     * Determine width for three different modes
     */
    private fun measureWidth(widthMeasureSpec: Int): Int {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthVal = View.MeasureSpec.getSize(widthMeasureSpec)
        return if (widthMode != View.MeasureSpec.EXACTLY) {
            if (widthMode == View.MeasureSpec.UNSPECIFIED) {
                OUTER_WIDTH_SIZE
            } else {
                Math.min(OUTER_WIDTH_SIZE, widthVal)
            }
        } else {
            widthVal + paddingLeft + paddingRight
        }
    }

    /**
     * Determine height for three different modes
     */
    private fun measureHeight(heightMeasureSpec: Int): Int {
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightVal = View.MeasureSpec.getSize(heightMeasureSpec)
        return if (heightMode != View.MeasureSpec.EXACTLY) {
            if (heightMode == View.MeasureSpec.UNSPECIFIED) {
                OUTER_HEIGHT_SIZE
            } else {
                Math.min(OUTER_HEIGHT_SIZE, heightVal)
            }
        } else {
            heightVal + paddingTop + paddingBottom
        }
    }

    /**
     * While size change
     */
    override fun onSizeChanged(width: Int, height: Int, preWidth: Int, preHeight: Int) {
        super.onSizeChanged(width, height, preWidth, preHeight)
        realWidth = width.toFloat()
        realHeight = height.toFloat()
        innerCircleX = (realWidth/2)
        innerCircleY = (realHeight/2)
    }

    /**
     * Draw two circles
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        outerRadius = Math.min(Math.min(realWidth/2 - paddingLeft, realWidth/2 - paddingRight)
                ,Math.min(realHeight/2 - paddingTop, realHeight/2 - paddingBottom)
                * 3/4)
        // Outer circle
        canvas.drawCircle(realWidth/2, realHeight/2, outerRadius, outerCircle)
        // Inner circle
        innerRadius = outerRadius/2
        canvas.drawCircle(innerCircleX, innerCircleY, innerRadius, innerCircle)
    }

    /**
     * Change inner circle position while touch event occurs
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event!!.action == MotionEvent.ACTION_DOWN) {
            changeInnerCirclePosition(event)
        }
        if (event.action == MotionEvent.ACTION_MOVE) {
            changeInnerCirclePosition(event)
        }
        if (event.action == MotionEvent.ACTION_UP) {
            innerCircleX = realWidth/2
            innerCircleY = realHeight/2
            invalidate()
        }
        return true
    }

    /**
     * Change inner circle position base on function of circle: (x - realWidth/2)^2 + (y - realHeight/2)^2 <= outerRadius^2
     */
    private fun changeInnerCirclePosition(event: MotionEvent?) {
        val absoluteX = event!!.x
        val absoluteY = event.y
        val outerX = realWidth/2
        val outerY = realHeight/2
        val relativeX = (absoluteX - outerX).toDouble()
        val relativeY = (outerY - absoluteY).toDouble()
        val relativeR = Math.sqrt(Math.pow(relativeX, 2.0) + Math.pow(relativeY, 2.0))
        val sin = relativeY / relativeR
        val cos = relativeX / relativeR
        angle = (Math.asin(sin) * 180.0 / 3.14159).toFloat() - 90
        if (cos > 0) angle = -angle
        mListener.onAngleChanged(angle)
        val isPointInOuterCircle: Boolean = (Math.pow(relativeX, 2.0) + Math.pow(relativeY, 2.0)
                <= Math.pow(outerRadius.toDouble(), 2.0))
        if (isPointInOuterCircle) {
            innerCircleX = absoluteX
            innerCircleY = absoluteY
        } else {
            innerCircleX = (outerX + outerRadius * 3/4 * cos).toFloat()
            innerCircleY = (outerY - outerRadius * 3/4 * sin).toFloat()
        }
        invalidate()
    }

    /**
     * Listener
     */
    interface OnAngleChangedListener {
        fun onAngleChanged(ang: Float)
    }

    fun setOnAngleChangedListener(listener: OnAngleChangedListener) {
        mListener = listener
    }
}