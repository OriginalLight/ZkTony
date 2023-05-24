package com.zktony.core.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class DynamicPlate : View {

    constructor(context: Context?) : this(context, null, 0)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    var column = 12
        set(value) {
            field = value
            invalidate()
        }
    var row = 8
        set(value) {
            field = value
            invalidate()
        }
    var color = Color.GREEN
        set(value) {
            field = value
            invalidate()
        }

    var axis = false
        set(value) {
            field = value
            invalidate()
        }

    var onItemClick: (Int, Int) -> Unit = { _, _ -> }

    var data = listOf<Triple<Int, Int, Boolean>>()
        set(value) {
            field = value
            invalidate()
        }
    private var spacex: Float = 0f
    private var spacey: Float = 0f


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        spacex = width.toFloat() / column
        spacey = height.toFloat() / row

        // 画内边框
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        paint.strokeWidth = 4f
        paint.isAntiAlias = true
        canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // 画横线和竖线
        paint.style = Paint.Style.STROKE
        paint.color = Color.LTGRAY
        paint.strokeWidth = 0.5f
        paint.isAntiAlias = true
        for (i in 1 until column) {
            canvas?.drawLine(i * spacex, 0f, i * spacex, height.toFloat(), paint)
        }
        for (i in 1 until row) {
            canvas?.drawLine(0f, i * spacey, width.toFloat(), i * spacey, paint)
        }


        // 画空心圆
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        paint.strokeWidth = 2f
        paint.isAntiAlias = true

        for (i in 0 until column) {
            for (j in 0 until row) {
                canvas?.drawCircle(
                    (i + 0.5f) * spacex,
                    (j + 0.5f) * spacey,
                    minOf(spacex, spacey) / 3f,
                    paint
                )
            }
        }

        paint.style = Paint.Style.FILL
        paint.color = color
        paint.strokeWidth = 1f
        paint.isAntiAlias = true

        for (i in 0 until column) {
            for (j in 0 until row) {
                val point = data.find { it.first == j && it.second == i && it.third }
                if (point != null) {
                    canvas?.drawCircle(
                        (i + 0.5f) * spacex,
                        (j + 0.5f) * spacey,
                        minOf(spacex, spacey) / 3f,
                        paint
                    )
                }
            }
        }

        if (axis) {
            // 写字
            paint.style = Paint.Style.FILL
            paint.color = Color.BLACK
            paint.strokeWidth = 1f
            paint.isAntiAlias = true
            paint.textSize = minOf(spacex, spacey) / 3f
            val textLeft = "A1"
            val x = spacex / 2 - paint.measureText(textLeft) / 2
            val y = spacey / 2 + (paint.fontMetrics.bottom - paint.fontMetrics.top) / 2 - paint.fontMetrics.bottom
            canvas?.drawText(textLeft, x, y, paint)
            if (row * column >= 2) {
                val textRight = "${'A' + row - 1}$column"
                val x1 = spacex * (column - 0.5f) - paint.measureText(textRight) / 2
                val y1 = spacey * (row - 0.5f) + (paint.fontMetrics.bottom - paint.fontMetrics.top) / 2 - paint.fontMetrics.bottom
                canvas?.drawText(textRight, x1, y1, paint)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {}
            MotionEvent.ACTION_UP -> {
                val xAxis = event.x.toInt()
                val yAxis = event.y.toInt()
                val i = (xAxis / spacex).toInt()
                val j = (yAxis / spacey).toInt()
                onItemClick(j, i)
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