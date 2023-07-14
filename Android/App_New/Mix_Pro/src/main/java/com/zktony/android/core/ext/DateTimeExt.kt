package com.zktony.android.core.ext

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
fun Int.timeFormat(): String {
    val mm = this % 3600 / 60
    val ss = this % 3600 % 60
    return String.format("%02d:%02d", mm, ss)
}

/**
 * 将秒数转化为分秒格式
 */
fun Long.timeFormat(): String {
    val temp = this.toInt()
    val hh = temp / 3600
    val mm = temp % 3600 / 60
    val ss = temp % 3600 % 60
    return String.format("%02d:%02d:%02d", hh, mm, ss)
}

@SuppressLint("SimpleDateFormat")
fun String.dateFormat(format: String): Date? {
    val sdf = SimpleDateFormat(format)
    return sdf.parse(this)
}

/**
 * 当天某小时初始时间,value表示某小时
 */

@SuppressLint("SimpleDateFormat")
fun Date.dateFormat(format: String): String {
    val sdf = SimpleDateFormat(format)
    return sdf.format(this)
}