package com.zktony.android.utils.ext

import java.math.BigDecimal


/**
 * 格式化
 *
 * @receiver Float
 * @return String
 */
fun Float.format(digits: Int = 0): String {
    return BigDecimal(String.format("%.${digits}f", this)).stripTrailingZeros().toPlainString()
}

/**
 * 格式化
 *
 * @receiver Double
 * @return String
 */
fun Double.format(digits: Int = 0): String {
    return BigDecimal(String.format("%.${digits}f", this)).stripTrailingZeros().toPlainString()
}