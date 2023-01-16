@file:Suppress("UNUSED")

package com.zktony.www.common.extension

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner

private const val MIN_CLICK_DELAY_TIME = 800
var lastClickTime: Long = 0

/**
 * 防止重复点击
 */
fun isFastClick(): Boolean {
    var flag = true
    val curClickTime = System.currentTimeMillis()
    if (curClickTime - lastClickTime >= MIN_CLICK_DELAY_TIME) {
        flag = false
    }
    lastClickTime = curClickTime
    return flag
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

inline fun Spinner.onItemSelected(crossinline block: (Int) -> Unit) {
    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            block.invoke(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }
}