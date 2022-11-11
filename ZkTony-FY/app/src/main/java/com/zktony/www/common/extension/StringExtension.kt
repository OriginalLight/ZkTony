package com.zktony.www.common.extension

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
 * 字符转日期
 * @param format [String] 日期格式
 * @return [Date]
 */
@SuppressLint("SimpleDateFormat")
fun String.simpleDateFormat(format: String): Date? {
    val sdf = SimpleDateFormat(format)
    return sdf.parse(this)
}

/**
 * 提取下位机返回字符串中的温度
 * @return [String]
 */
fun String.extractTemp(): String {
    val regEx = "[^0-9.]"
    val p = regEx.toRegex()
    return p.replace(this.substring(4, this.length - 3), "")
}