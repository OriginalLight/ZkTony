package com.zktony.www.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.zktony.www.common.room.entity.Hole


class DynamicPlate : View {

    constructor(context: Context?) : this(context, null, 0)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    private var row = 0
    private var column = 0
    private var space = 40
    private var color = Color.GREEN

    // 显示定位
    private var showLocation = false
    private var onItemClick: (Int, Int) -> Unit = { _, _ -> }
    private var data = listOf<Hole>()

    fun getRow() = row

    fun getColumn() = column

    fun setRowAndColumn(row: Int, column: Int) {
        this.row = row
        this.column = column
        space = minOf(480 / column, 320 / row)
        requestLayout()
        invalidate()
    }

    fun setShowLocation(showLocation: Boolean) {
        this.showLocation = showLocation
        requestLayout()
        invalidate()
    }

    fun setOnItemClick(onItemClick: (Int, Int) -> Unit) {
        this.onItemClick = onItemClick
    }

    fun setData(data: List<Hole>) {
        this.data = data
        invalidate()
    }

    fun setColor(color: Int) {
        this.color = color
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // 绘制核心方法
        val paint = Paint()
        paint.color = Color.WHITE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 2f

        canvas!!.drawColor(Color.LTGRAY)
        val width = column * space
        val height = row * space

        var vert = 0
        var hort = 0
        for (i in 0 until row) {
            canvas.drawLine(0f, vert.toFloat(), width.toFloat(), vert.toFloat(), paint)
            vert += space
        }
        for (i in 0 until column) {
            canvas.drawLine(hort.toFloat(), 0f, hort.toFloat(), height.toFloat(), paint)
            hort += space
        }
        paint.color = Color.WHITE

        for (i in 0 until row) {
            for (j in 0 until column) {
                val hole = data.find { it.x == j && it.y == i }
                if (hole != null) {
                    if (hole.checked) {
                        paint.color = color
                    } else {
                        paint.color = Color.WHITE
                    }
                } else {
                    paint.color = Color.WHITE
                }
                canvas.drawCircle(
                    (j * space + space / 2).toFloat(),
                    (row * space - i * space - space / 2).toFloat(),
                    space / 3f,
                    paint
                )

            }
        }

        if (showLocation) {
            paint.color = Color.BLUE
            canvas.drawCircle(
                (space / 2).toFloat(),
                (space * (row - 1) + space / 2).toFloat(),
                space / 3f,
                paint
            )
            paint.color = Color.GREEN
            canvas.drawCircle(
                (space * (column - 1) + space / 2).toFloat(),
                (space / 2).toFloat(),
                space / 3f,
                paint
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x.toInt()
                val y = event.y.toInt()
                val i = x / space
                val j = y / space
                onItemClick(i, (row - j - 1))
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 设置宽高
        setMeasuredDimension(column * space, row * space)
    }

}