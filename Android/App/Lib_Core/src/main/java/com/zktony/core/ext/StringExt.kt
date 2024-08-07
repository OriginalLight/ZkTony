package com.zktony.core.ext

/**
 * @author: 刘贺贺
 * @date: 2022-09-26 9:07
 */

/**
 * 删除零
 *
 * @receiver String
 * @return String
 */
fun String.format(): String {
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
 * 删除零
 *
 * @receiver Float
 * @return String
 */
fun Float.format(): String {
    if (this == 0f) return "0"
    // 去除小数点后面的0
    return this.toString().format()
}

/**
 * 删除零
 *
 * @receiver Double
 * @return String
 */
fun Double.format(): String {
    if (this == 0.0) return "0"
    // 去除小数点后面的0
    return this.toString().format()
}

