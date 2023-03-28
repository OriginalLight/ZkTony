package com.zktony.common.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class DynamicPlate : View {

    constructor(context: Context?) : this(context, null, 0)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    var x = 12
        set(value) {
            field = value
            invalidate()
        }
    var y = 8
        set(value) {
            field = value
            invalidate()
        }
    var color = Color.GREEN
        set(value) {
            field = value
            invalidate()
        }
    var showLocation = false
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
        spacex = width.toFloat() / x
        spacey = height.toFloat() / y

        // 画内边框
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        paint.strokeWidth = 2f
        paint.isAntiAlias = true
        canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)


        // 画空心圆
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        paint.strokeWidth = 2f
        paint.isAntiAlias = true

        for (i in 0 until x) {
            for (j in 0 until y) {
                canvas?.drawCircle(
                    (i + 0.5f) * spacex,
                    (y - j - 0.5f) * spacey,
                    minOf(spacex, spacey) / 3f,
                    paint
                )
            }
        }

        // 画实心圆 从左下角绘制
        paint.style = Paint.Style.FILL
        paint.color = color
        paint.strokeWidth = 1f
        paint.isAntiAlias = true

        for (i in 0 until x) {
            for (j in 0 until y) {
                val hole = data.find { it.first == j && it.second == i && it.third }
                if (hole != null) {
                    canvas?.drawCircle(
                        (i + 0.5f) * spacex,
                        (y - j - 0.5f) * spacey,
                        minOf(spacex, spacey) / 3f,
                        paint
                    )
                }
            }
        }

        if (showLocation) {
            paint.color = Color.BLUE
            canvas?.drawCircle(
                0.5f * spacex,
                (y - 0.5f) * spacey,
                minOf(spacex, spacey) / 3f,
                paint
            )
            paint.color = Color.GREEN
            canvas?.drawCircle(
                (x - 0.5f) * spacex,
                0.5f * spacey,
                minOf(spacex, spacey) / 3f,
                paint
            )
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
                onItemClick((y - j - 1), i)
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