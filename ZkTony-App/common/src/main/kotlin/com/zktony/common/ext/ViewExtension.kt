@file:Suppress("UNUSED")

package com.zktony.common.ext

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
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
@Deprecated("use View.drawToBitmap()")
fun View.toBitmap(scale: Float = 1f, config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap? {
    if (this is ImageView) {
        if (drawable is BitmapDrawable) return (drawable as BitmapDrawable).bitmap
    }
    this.clearFocus()
    val bitmap = createBitmapSafely(
        (width * scale).toInt(),
        (height * scale).toInt(),
        config,
        1
    )
    if (bitmap != null) {
        Canvas().run {
            setBitmap(bitmap)
            save()
            drawColor(Color.WHITE)
            scale(scale, scale)
            this@toBitmap.draw(this)
            restore()
            setBitmap(null)
        }
    }
    return bitmap
}

fun createBitmapSafely(width: Int, height: Int, config: Bitmap.Config, retryCount: Int): Bitmap? {
    try {
        return Bitmap.createBitmap(width, height, config)
    } catch (e: OutOfMemoryError) {
        e.printStackTrace()
        if (retryCount > 0) {
            System.gc()
            return createBitmapSafely(width, height, config, retryCount - 1)
        }
        return null
    }
}


/**
 * 防止重复点击事件 默认0.5秒内不可重复点击
 * @param interval 时间间隔 默认0.5秒
 * @param action 执行方法
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