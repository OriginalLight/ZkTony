package com.zktony.www.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.zktony.www.data.model.Pore


class DynamicPlate : View {

    constructor(context: Context?) : this(context, null, 0)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    private var row = 8
    private var column = 12
    private var space = 48

    // 显示定位
    private var showLocation = false
    private var onItemClick: (Int, Int) -> Unit = { _, _ -> }
    private var data = listOf<Pore>()

    fun setRow(row: Int) {
        this.row = row
        if (row > 0) {
            space = 384 / row
        }
        invalidate()
    }

    fun getRow() = row

    fun setColumn(column: Int) {
        this.column = column
        invalidate()
    }

    fun getColumn() = column

    fun setShowLocation(showLocation: Boolean) {
        this.showLocation = showLocation
        invalidate()
    }

    fun setData(data: List<Pore>) {
        this.data = data
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
                val pore = data.find { it.row == i && it.column == j }
                if (pore != null) {
                    if (pore.checked) {
                        paint.color = Color.GREEN
                    } else {
                        paint.color = Color.WHITE
                    }
                } else {
                    paint.color = Color.WHITE
                }
                canvas.drawCircle(
                    (j * space + space / 2).toFloat(),
                    (i * space + space / 2).toFloat(),
                    space / 3f,
                    paint
                )

            }
        }

        if (showLocation) {
            paint.color = Color.BLUE
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = space / 2f
            canvas.drawText("2", (space / 2).toFloat(), (space / 1.5).toFloat(), paint)
            canvas.drawText(
                "1",
                (space / 2).toFloat(),
                (space * (row - 1) + space / 1.5).toFloat(),
                paint
            )
            canvas.drawText(
                "3",
                (space * (column - 1) + space / 2).toFloat(),
                (space * (row - 1) + space / 1.5).toFloat(),
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
                onItemClick(i, j)
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