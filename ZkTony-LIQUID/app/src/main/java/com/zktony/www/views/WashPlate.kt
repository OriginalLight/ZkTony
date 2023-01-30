package com.zktony.www.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.zktony.www.common.room.entity.Pore


class WashPlate : View {

    constructor(context: Context?) : this(context, null, 0)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    private var text = "( 0, 0 )"
    private var onItemClick: () -> Unit = { }

    fun setOnItemClick(onItemClick: () -> Unit) {
        this.onItemClick = onItemClick
    }

    fun setText(text: String) {
        this.text = text
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // 绘制核心方法
        val paint = Paint()
        paint.color = Color.LTGRAY
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 2f

        val rect = Rect(0, 0, 576, 228)
        canvas?.drawRect(rect, paint)

        paint.color = Color.WHITE
        val rect1 = Rect(20, 20, 556, 208)
        canvas?.drawRect(rect1, paint)

        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 30f
        canvas?.drawText(text, 288f, 114f, paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x.toInt()
                val y = event.y.toInt()
                if (x in 234..334 && y in 64..124) {
                    onItemClick()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 设置宽高
        setMeasuredDimension(576, 228)
    }

}