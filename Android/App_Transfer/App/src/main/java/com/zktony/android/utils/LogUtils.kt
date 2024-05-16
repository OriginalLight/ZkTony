package com.zktony.android.utils

import android.util.Log
import com.zktony.android.BuildConfig

//import com.zktony.android.BuildConfig

/**
 * @author 刘贺贺
 * @date 2023/9/15 14:14
 */
object LogUtils {
    const val TAG = "App Log"
    const val V = 0
    const val D = 1
    const val I = 2
    const val W = 3
    const val E = 4

    val show = BuildConfig.DEBUG

    fun logV(tag: String = TAG, message: String) =
        log(V, tag, message)

    fun logD(tag: String = TAG, message: String) =
        log(D, tag, message)

    fun logI(tag: String = TAG, message: String) =
        log(I, tag, message)

    fun logW(tag: String = TAG, message: String) =
        log(W, tag, message)

    fun logE(tag: String = TAG, message: String) =
        log(E, tag, message)

    fun log(level: Int, tag: String, message: String) {
//        if (!show) return
        when (level) {
            V -> Log.v(tag, message)
            D -> Log.d(tag, message)
            I -> Log.i(tag, message)
            W -> Log.w(tag, message)
            E -> Log.e(tag, message)
        }
    }
}