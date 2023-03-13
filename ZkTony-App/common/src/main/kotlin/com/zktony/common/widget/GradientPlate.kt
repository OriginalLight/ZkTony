package com.zktony.common.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class GradientPlate : View {

    constructor(context: Context?) : this(context, null, 0)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    private var size = 10
    private var color = Color.GREEN
    private var onItemClick: (Int) -> Unit = { _ -> }
    private var data = listOf<Pair<Int, Boolean>>()
    private var space: Float = 0f


    fun setSize(size: Int) {
        this.size = size
        invalidate()
    }

    fun setColor(color: Int) {
        this.color = color
        invalidate()
    }


    fun setOnItemClick(onItemClick: (Int) -> Unit) {
        this.onItemClick = onItemClick
    }

    fun setData(data: List<Pair<Int, Boolean>>) {
        this.data = data
        invalidate()
    }


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


    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                val xAxis = event.x.toInt()
                val i = (xAxis / space).toInt()
                onItemClick(i)
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