package com.zktony.core.ext


/**
 * 格式化
 *
 * @receiver Float
 * @return String
 */
fun Float.format(digits: Int = 0): String {
    val format = "%.${digits}f"
    return String.format(format, this)
}

/**
 * 格式化
 *
 * @receiver Double
 * @return String
 */
fun Double.format(digits: Int = 0): String {
    val format = "%.${digits}f"
    return String.format(format, this)
}