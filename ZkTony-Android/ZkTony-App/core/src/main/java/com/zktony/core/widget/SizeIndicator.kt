package com.zktony.core.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class SizeIndicator : View {

    constructor(context: Context?) : this(context, null, 0)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    var text = "TEXT"
        set(value) {
            field = value
            invalidate()
        }
    var type = 0
        set(value) {
            field = value
            invalidate()
        }

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

        if (type == 0) {
            canvas?.drawLine(0f, 0f, 0f, height.toFloat(), paint)
            canvas?.drawLine(width.toFloat(), 0f, width.toFloat(), height.toFloat(), paint)
        } else {
            canvas?.drawLine(0f, 0f, width.toFloat(), 0f, paint)
            canvas?.drawLine(0f, height.toFloat(), width.toFloat(), height.toFloat(), paint)
        }

        paint.strokeWidth = 2f
        if (type == 0) {
            canvas?.drawLine(
                width.toFloat() / 20,
                height.toFloat() / 2,
                width.toFloat() / 10 * 4,
                height.toFloat() / 2,
                paint
            )
            canvas?.drawLine(
                width.toFloat() / 10 * 6,
                height.toFloat() / 2,
                width.toFloat() / 20 * 19,
                height.toFloat() / 2,
                paint
            )
        } else {
            canvas?.drawLine(
                width.toFloat() / 2,
                height.toFloat() / 20,
                width.toFloat() / 2,
                height.toFloat() / 10 * 4,
                paint
            )
            canvas?.drawLine(
                width.toFloat() / 2,
                height.toFloat() / 10 * 6,
                width.toFloat() / 2,
                height.toFloat() / 20 * 19,
                paint
            )
        }

        paint.textSize = 18f
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        val x = (width - paint.measureText(text)) / 2
        val y =
            height / 2 + (paint.fontMetrics.bottom - paint.fontMetrics.top) / 2 - paint.fontMetrics.bottom
        canvas?.drawText(text, x, y, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}