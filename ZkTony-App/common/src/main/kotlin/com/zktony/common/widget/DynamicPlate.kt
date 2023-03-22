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

    private var x = 12
    private var y = 8
    private var color = Color.GREEN
    private var showLocation = false
    private var onItemClick: (Int, Int) -> Unit = { _, _ -> }
    private var data = listOf<Triple<Int, Int, Boolean>>()
    private var spacex: Float = 0f
    private var spacey: Float = 0f


    fun setXY(x: Int, y: Int) {
        this.x = y
        this.y = x
        invalidate()
    }

    fun setColor(color: Int) {
        this.color = color
        invalidate()
    }

    fun setShowLocation(showLocation: Boolean) {
        this.showLocation = showLocation
        invalidate()
    }

    fun setOnItemClick(onItemClick: (Int, Int) -> Unit) {
        this.onItemClick = onItemClick
    }

    fun setData(data: List<Triple<Int, Int, Boolean>>) {
        this.data = data
        invalidate()
    }


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
            MotionEvent.ACTION_DOWN -> {
                val xAxis = event.x.toInt()
                val yAxis = event.y.toInt()
                val i = (xAxis / spacex).toInt()
                val j = (yAxis / spacey).toInt()
                onItemClick((y - j - 1), i)
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}