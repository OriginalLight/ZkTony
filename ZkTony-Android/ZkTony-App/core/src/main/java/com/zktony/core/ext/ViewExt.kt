@file:Suppress("UNUSED")

package com.zktony.core.ext

import android.annotation.SuppressLint
import android.graphics.*
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout

/**
 * 设置view显示
 */
fun View.visible() {
    visibility = View.VISIBLE
}


/**
 * 设置view占位隐藏
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * 根据条件设置view显示隐藏 为true 显示，为false 隐藏
 */
fun View.visibleOrGone(flag: Boolean) {
    visibility = if (flag) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

/**
 * 根据条件设置view显示隐藏 为true 显示，为false 隐藏
 */
fun View.visibleOrInvisible(flag: Boolean) {
    visibility = if (flag) {
        View.VISIBLE
    } else {
        View.INVISIBLE
    }
}

/**
 * 设置view隐藏
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * 将view转为bitmap
 */
/**
 * 获取View的截图, 支持获取整个RecyclerView列表的长截图
 * 注意：调用该方法时，请确保View已经测量完毕，如果宽高为0，则将抛出异常
 */
fun View.toBitmap(): Bitmap {
    if (measuredWidth == 0 || measuredHeight == 0) {
        "⚠️警告！View.toBitmap()：调用该方法时，请确保View已经测量完毕，当前View宽或高为0，直接Return!".logw()
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    }
    return when (this) {
        is RecyclerView -> {
            this.scrollToPosition(0)
            this.measure(
                View.MeasureSpec.makeMeasureSpec(measuredWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )

            val bmp = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)

            //draw default bg, otherwise will be black
            if (background != null) {
                background.setBounds(0, 0, measuredWidth, measuredHeight)
                background.draw(canvas)
            } else {
                canvas.drawColor(Color.WHITE)
            }
            this.draw(canvas)
            //恢复高度
            this.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST)
            )
            bmp //return
        }

        is ScrollView, is HorizontalScrollView, is NestedScrollView -> {
            //draw first child
            val child = (this as ViewGroup).getChildAt(0)
            val screenshot =
                Bitmap.createBitmap(
                    child.measuredWidth,
                    child.measuredHeight,
                    Bitmap.Config.ARGB_8888
                )
            val canvas = Canvas(screenshot)
            if (child.background != null) {
                child.background.setBounds(0, 0, child.measuredWidth, child.measuredHeight)
                child.background.draw(canvas)
            } else {
                canvas.drawColor(Color.WHITE)
            }
            child.draw(canvas)// 将 view 画到画布上
            screenshot //return
        }

        else -> {
            val screenshot =
                Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(screenshot)
            if (background != null) {
                background.setBounds(0, 0, width, measuredHeight)
                background.draw(canvas)
            } else {
                canvas.drawColor(Color.WHITE)
            }
            draw(canvas)// 将 view 画到画布上
            screenshot //return
        }
    }
}


/**
 * 防止重复点击事件 默认0.5秒内不可重复点击
 */
var lastClickTime = 0L
fun View.clickNoRepeat(interval: Long = 500, action: (view: View) -> Unit) {
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (lastClickTime != 0L && (currentTime - lastClickTime < interval)) {
            return@setOnClickListener
        }
        lastClickTime = currentTime
        action(it)
    }
}

@SuppressLint("ClickableViewAccessibility")
fun View.clickScale() {
    this.setOnTouchListener { v, event ->
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                v?.scaleX = 0.8f
                v?.scaleY = 0.8f
            }

            MotionEvent.ACTION_UP -> {
                v?.scaleX = 1f
                v?.scaleY = 1f
            }
        }
        false
    }
}

inline fun EditText.afterTextChange(crossinline block: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            block.invoke(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    })
}

fun EditText.setEqualText(text: String) {
    val str = this.text.toString()
    if (str == text || this.isFocused) {
        return
    } else {
        this.setText(text)
        this.setSelection(text.length)
    }
}

@SuppressLint("SetTextI18n")
fun EditText.addSuffix(suffix: String) {
    this.setOnFocusChangeListener { v, hasFocus ->
        v as EditText
        if (v.text.isNotEmpty() && v.text.toString().endsWith(suffix).not()) {
            v.setText(v.text.toString().removeZero() + suffix)
        }
        if (hasFocus) {
            v.setText(v.text.toString().replace(suffix, ""))
        }
    }
}

/**
 * 禁用选项
 */
fun TabLayout.disable(disable: Boolean = false) {
    val tabLayout = this.getChildAt(0) as? ViewGroup
    tabLayout?.getChildAt(0)?.isEnabled = disable
    tabLayout?.getChildAt(1)?.isEnabled = disable
}

@SuppressLint("ClickableViewAccessibility")
inline fun View.addTouchEvent(crossinline down: (View) -> Unit, crossinline up: (View) -> Unit) {
    this.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                down.invoke(v)
            }

            MotionEvent.ACTION_UP -> {
                up.invoke(v)
            }
        }
        true
    }
}