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

}