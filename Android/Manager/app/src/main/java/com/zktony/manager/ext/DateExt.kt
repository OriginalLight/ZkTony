package com.zktony.www.common.extension

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2022-09-26 8:57
 */


/**
 * 当天某小时初始时间,value表示某小时
 */

fun Date.getDayStart(): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.time
}

/**
 * 当天某小时结束时间,value表示某小时
 */

fun Date.getDayEnd(): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.MILLISECOND, 999)
    return cal.time
}

@SuppressLint("SimpleDateFormat")
fun Date.simpleDateFormat(format: String): String {
    val sdf = SimpleDateFormat(format)
    return sdf.format(this)
}

fun currentTime(): String =
    Date(System.currentTimeMillis()).simpleDateFormat("yyyy-MM-dd HH:mm:ss")