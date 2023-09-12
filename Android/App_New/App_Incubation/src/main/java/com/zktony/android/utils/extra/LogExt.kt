package com.zktony.android.utils.extra

import android.util.Log
import com.zktony.android.BuildConfig

const val TAG = "App Log"

var show = BuildConfig.DEBUG

object LEVEL {
    const val V = 0
    const val D = 1
    const val I = 2
    const val W = 3
    const val E = 4
}

fun String.logV(tag: String = TAG) =
    log(LEVEL.V, tag, this)

fun String.logD(tag: String = TAG) =
    log(LEVEL.D, tag, this)

fun String.logI(tag: String = TAG) =
    log(LEVEL.I, tag, this)

fun String.logW(tag: String = TAG) =
    log(LEVEL.W, tag, this)

fun String.logE(tag: String = TAG) =
    log(LEVEL.E, tag, this)

private fun log(level: Int, tag: String, message: String) {
    if (!show) return
    when (level) {
        LEVEL.V -> Log.v(tag, message)
        LEVEL.D -> Log.d(tag, message)
        LEVEL.I -> Log.i(tag, message)
        LEVEL.W -> Log.w(tag, message)
        LEVEL.E -> Log.e(tag, message)
    }
}