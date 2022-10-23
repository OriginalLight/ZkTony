package com.zktony.serialport.util

import android.util.Log

class Logger {
    fun v(tag: String, msg: String) {
        if (SHOW_LOG) {
            Log.v(COMMON_TAG, "[$tag] $msg")
        }
    }

    fun d(tag: String, msg: String) {
        if (SHOW_LOG) {
            Log.d(COMMON_TAG, "[$tag] $msg")
        }
    }

    fun i(tag: String, msg: String) {
        if (SHOW_LOG) {
            Log.i(COMMON_TAG, "[$tag] $msg")
        }
    }

    fun w(tag: String, msg: String) {
        Log.w(COMMON_TAG, "[$tag] $msg")
    }

    fun e(tag: String, msg: String) {
        Log.e(COMMON_TAG, "[$tag] $msg")
    }

    fun v(tag: String, msg: String, tr: Throwable?) {
        if (SHOW_LOG) {
            Log.v(COMMON_TAG, "[$tag] $msg", tr)
        }
    }

    fun d(tag: String, msg: String, tr: Throwable?) {
        if (SHOW_LOG) {
            Log.d(COMMON_TAG, "[$tag] $msg", tr)
        }
    }

    fun i(tag: String, msg: String, tr: Throwable?) {
        if (SHOW_LOG) {
            Log.i(COMMON_TAG, "[$tag] $msg", tr)
        }
    }

    fun w(tag: String, msg: String, tr: Throwable?) {
        Log.w(COMMON_TAG, "[$tag] $msg", tr)
    }

    fun e(tag: String, msg: String, tr: Throwable?) {
        Log.e(COMMON_TAG, "[$tag] $msg", tr)
    }

    companion object {
        @JvmStatic
        val instance: Logger by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            Logger()
        }
        private const val COMMON_TAG = "serial"

        @JvmField
        var SHOW_LOG = false

    }
}