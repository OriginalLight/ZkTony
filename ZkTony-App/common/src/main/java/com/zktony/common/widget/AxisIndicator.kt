package com.zktony.common.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class AxisIndicator : View {

    constructor(context: Context?) : this(context, null, 0)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    var textLeft = "TEXT"
        set(value) {
            field = value
            invalidate()
        }
    var textRight = "TEXT"
        set(value) {
            field = value
            invalidate()
        }
    var onItemClick: (Int) -> Unit = {}

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // 左右两边画一个竖杠
        // 中间画一个横杠
        // 中间画一个文字

        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        paint.strokeWidth = 4f
        paint.isAntiAlias = true

        canvas?.drawLine(0f, 0f, 0f, height.toFloat(), paint)
        canvas?.drawLine(width.toFloat(), 0f, width.toFloat(), height.toFloat(), paint)
        paint.strokeWidth = 2f
        canvas?.drawLine(width / 2f, 0f, width / 2f, height.toFloat(), paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.BLUE
        canvas?.drawCircle(height / 2f, height / 2f, height / 3f, paint)

        paint.color = Color.GREEN
        canvas?.drawCircle((width + height) / 2f, height / 2f, height / 3f, paint)

        paint.textSize = 18f
        paint.color = Color.BLACK
        val x = height + ((width / 2) - height - paint.measureText(textLeft)) / 2
        val x1 = width / 2 + height + ((width / 2) - height - paint.measureText(textRight)) / 2
        val y =
            height / 2 + (paint.fontMetrics.bottom - paint.fontMetrics.top) / 2 - paint.fontMetrics.bottom
        canvas?.drawText(textLeft, x, y, paint)
        canvas?.drawText(textRight, x1, y, paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                scaleX = 0.9f
                scaleY = 0.9f
            }
            MotionEvent.ACTION_UP -> {
                scaleX = 1f
                scaleY = 1f
                val x = event.x
                if (x < width / 2) {
                    onItemClick(0)
                } else {
                    onItemClick(1)
                }
            }
        }
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}