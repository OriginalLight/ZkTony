package com.zktony.serialport.command

/**
 * @author 刘贺贺
 * @date 2023/9/7 15:53
 */
interface BaseProtocol {
    @Throws(Exception::class)
    fun serialization(): ByteArray

    @Throws(Exception::class)
    fun deserialization(byteArray: ByteArray)
}