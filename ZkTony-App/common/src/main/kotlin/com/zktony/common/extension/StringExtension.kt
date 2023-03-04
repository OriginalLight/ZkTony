package com.zktony.common.extension

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2022-09-26 9:07
 */

/**
 * 删除零
 *
 * @return [String]
 */
fun String.removeZero(): String {
    // 去除小数点后面的0
    var str = this
    if (str.indexOf(".") > 0) {
        str = str.replace("0+?$".toRegex(), "") // 去掉多余的0
        str = str.replace("[.]$".toRegex(), "") // 如最后一位是.则去掉
    }
    // 去除左边的0
    if (!str.startsWith("0.") && str.startsWith("0")) {
        str = str.replace("^0+(?!$)".toRegex(), "")
    }
    return str
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
 * 将Float转化为百分比两位小数
 * @return [String]
 */
fun Float.toPercent(): String {
    return String.format("%.2f", this * 100) + " %"
}