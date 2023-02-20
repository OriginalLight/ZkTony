package com.zktony.www.common.extension

/**
 * 提取下位机返回字符串中的温度
 * @return [String]
 */
fun String.extractTemp(): String {
    val regEx = "[^0-9.]"
    val p = regEx.toRegex()
    return p.replace(this.substring(4, this.length - 3), "")
}


