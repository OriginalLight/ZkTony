package com.zktony.common.ext

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