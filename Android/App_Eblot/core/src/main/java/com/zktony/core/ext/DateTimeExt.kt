package com.zktony.core.ext

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2022-09-26 8:57
 */

/**
 * 将秒数转化为分秒格式
 */
fun Int.getTimeFormat(): String {
    val mm = this / 60
    val ss = this % 3600 % 60
    return String.format("%02d:%02d", mm, ss)
}

/**
 * 将秒数转化为分秒格式
 */
fun Long.getTimeFormat(): String {
    val temp = this.toInt()
    val hh = temp / 3600
    val mm = temp % 3600 / 60
    val ss = temp % 3600 % 60
    return String.format("%02d:%02d:%02d", hh, mm, ss)
}


@SuppressLint("SimpleDateFormat")
fun String.simpleDateFormat(format: String): Date? {
    val sdf = SimpleDateFormat(format)
    return sdf.parse(this)
}

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