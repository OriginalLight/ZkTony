package com.zktony.android.utils.extra.internal

/**
 * @author 刘贺贺
 * @date 2023/9/11 15:34
 */
object ControlType {
    const val RESET: Byte = 0x00
    const val START: Byte = 0x01
    const val STOP: Byte = 0x02
    const val QUERY: Byte = 0x03
    const val GPIO: Byte = 0x04
    const val VALVE: Byte = 0x05
}