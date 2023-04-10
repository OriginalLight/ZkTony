package com.zktony.common.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View


class WashPlate : View {

    constructor(context: Context?) : this(context, null, 0)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    var text = "TEXT"
        set(value) {
            field = value
            invalidate()
        }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // 绘制核心方法
        val paint = Paint()
        paint.color = Color.BLACK
        paint.strokeWidth = 4f


        // 绘制外框
        paint.style = Paint.Style.STROKE
        canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        paint.textSize = 36f
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        val x = (width - paint.measureText(text)) / 2
        val y = height / 2  + (paint.fontMetrics.bottom - paint.fontMetrics.top) / 2 - paint.fontMetrics.bottom
        canvas?.drawText(text, x, y, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

}