package com.zktony.serialport.command

/**
 * @author 刘贺贺
 * @date 2023/9/7 15:53
 */
interface BaseProtocol<T> {
    @Throws(Exception::class)
    fun toByteArray(): ByteArray

    @Throws(Exception::class)
    fun toProtocol(byteArray: ByteArray)

    @Throws(Exception::class)
    fun callbackHandler(byteArray: ByteArray, block: (Int, T) -> Unit)
}