package com.zktony.core.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.zktony.core.ext.removeZero

class GradientPlate : View {

    constructor(context: Context?) : this(context, null, 0)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    var size = 10
        set(value) {
            field = value
            invalidate()
        }
    var color = Color.GREEN
        set(value) {
            field = value
            invalidate()
        }
    var data = listOf<Pair<Int, Boolean>>()
        set(value) {
            field = value
            invalidate()
        }
    var yAxis = listOf<Pair<Int, Float>>()

    var onItemClick: (Int) -> Unit = { _ -> }

    private var space: Float = 0f

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        space = width.toFloat() / size

        // 画内边框
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        paint.strokeWidth = 4f
        paint.isAntiAlias = true
        canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // 画size个矩形
        for (i in 0 until size) {
            val left = i * space + if (i == 0) 8 else 4
            val right = (i + 1) * space - if (i == size - 1) 8 else 4
            val top = 8f
            val bottom = height.toFloat()  - 8f
            paint.style = Paint.Style.STROKE
            paint.color = Color.BLACK
            paint.strokeWidth = 2f
            paint.isAntiAlias = true
            canvas?.drawRect(left, top, right, bottom, paint)
        }

        for (i in 0 until size) {
            val fill = data.find { it.first == i }?.second ?: false
            if (fill) {
                val left = i * space + if (i == 0) 8 else 4
                val right = (i + 1) * space - if (i == size - 1) 8 else 4
                val top = 8f
                val bottom = height.toFloat()  - 8f
                paint.style = Paint.Style.FILL
                paint.color = Color.GREEN
                paint.isAntiAlias = true
                canvas?.drawRect(left, top, right, bottom, paint)
            }
        }

        // 每个矩形中竖着写字A B C D E F G H I J
        for (i in 0 until size) {
            val left = i * space + if (i == 0) 8 else 4
            val right = (i + 1) * space - if (i == size - 1) 8 else 4
            paint.style = Paint.Style.FILL
            paint.color = Color.BLACK
            paint.strokeWidth = 2f
            paint.isAntiAlias = true
            paint.textSize = space * 0.6f
            val text = ('A' + i).toString()
            val textWidth = paint.measureText(text)
            canvas?.drawText(text, left + (right - left - textWidth) / 2, height.toFloat() - 16, paint)
        }

        for (i in 0 until size) {
            val axis = yAxis.find { it.first == i }
            if (axis != null) {
                val left = i * space + if (i == 0) 8 else 4
                val right = (i + 1) * space - if (i == size - 1) 8 else 4
                val top = 8f
                val bottom = height.toFloat()  - 8f
                paint.style = Paint.Style.FILL
                paint.color = Color.BLACK
                paint.strokeWidth = 1f
                paint.isAntiAlias = true
                paint.textSize = space * 0.2f
                val text = String.format("%.2f", axis.second).removeZero()
                val x = left + (right - left - paint.measureText(text)) / 2
                val y = top + (bottom - top - paint.textSize) / 2 + paint.textSize
                canvas?.drawText(text, x, y, paint)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {}
            MotionEvent.ACTION_UP -> {
                val xAxis = event.x.toInt()
                val i = (xAxis / space).toInt()
                onItemClick(i)
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