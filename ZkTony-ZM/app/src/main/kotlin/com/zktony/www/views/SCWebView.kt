package com.zktony.www.views

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView
import androidx.annotation.RequiresApi
import com.kongzue.dialogx.interfaces.ScrollController

/**
 * @author: 刘贺贺
 * @date: 2022-10-09 11:05
 */
class SCWebView : WebView, ScrollController {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    @Deprecated("", ReplaceWith("lockScroll"))
    override fun isLockScroll(): Boolean {
        return lockScroll
    }

    var lockScroll = false
    override fun lockScroll(lockScroll: Boolean) {
        this.lockScroll = lockScroll
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (lockScroll) false else super.onTouchEvent(event)
    }

    override fun getScrollDistance(): Int {
        return scrollY
    }

    override fun isCanScroll(): Boolean {
        return true
    }
}