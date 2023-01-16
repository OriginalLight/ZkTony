package com.zktony.serialport.util

import java.nio.charset.Charset
import java.util.*

/**
 * 数据转换工具类
 * Data conversion tool class
 */
object SerialDataUtils {
    /**
     * 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
     * Judging odd or even numbers, bit operations, the last bit is 1 is odd, 0 is even
     */
    fun isOdd(num: Int): Int {
        return num and 1
    }

    /**
     * Hex字符串转int
     * Hex string to int
     */
    fun hexToInt(inHex: String): Int {
        return inHex.toInt(16)
    }

    /**
     * Hex字符串转byte
     * Hex string to byte
     */
    fun hexToByte(inHex: String): Byte {
        return inHex.toInt(16).toByte()
    }

    /**
     * 1字节转2个Hex字符
     * 1 byte to 2 Hex characters
     */
    fun byte2Hex(inByte: Byte): String {
        return String.format("%02x", inByte).uppercase(Locale.getDefault())
    }

    /**
     * 字节数组转转hex字符串
     * Byte array to hex string
     */
    fun byteArrToHex(inBytArr: ByteArray): String {
        val strBuilder = StringBuilder()
        for (valueOf in inBytArr) {
            strBuilder.append(byte2Hex(java.lang.Byte.valueOf(valueOf)))
            strBuilder.append(" ")
        }
        return strBuilder.toString()
    }

    /**
     * 字节数组转转hex字符串，可选长度
     * Byte array to hex string, optional length
     */
    fun byteArrToHex(inBytArr: ByteArray, offset: Int, byteCount: Int): String {
        val strBuilder = StringBuilder()
        for (i in offset until byteCount) {
            strBuilder.append(byte2Hex(java.lang.Byte.valueOf(inBytArr[i])))
        }
        return strBuilder.toString()
    }

    /**
     * hex字符串转字节数组
     * Hex string to byte array
     */
    @JvmStatic
    fun hexToByteArr(hex: String): ByteArray {
        var inHex = hex
        val result: ByteArray
        var hexlen = inHex.length
        if (isOdd(hexlen) == 1) {
            hexlen++
            result = ByteArray(hexlen / 2)
            inHex = "0$inHex"
        } else {
            result = ByteArray(hexlen / 2)
        }
        var j = 0
        var i = 0
        while (i < hexlen) {
            result[j] = hexToByte(inHex.substring(i, i + 2))
            j++
            i += 2
        }
        return result
    }

    /**
     * hex字符串转字节数组
     * Hex string to byte array
     */
    fun hexToByteArr2(hex: String): ByteArray {
        var inHex = hex
        val result: ByteArray
        var hexlen = inHex.length
        if (isOdd(hexlen) == 1) {
            hexlen++
            result = ByteArray(hexlen / 2)
            inHex = "0$inHex"
        } else {
            result = ByteArray(hexlen / 2)
        }
        var j = 0
        var i = 0
        while (i < hexlen) {
            result[j] = hexToByte(inHex.substring(i, i + 2))
            j++
            i += 2
        }
        return result
    }

    /**
     * 字符串转换为16进制字符串
     * String to a hex string
     *
     * @param s
     * @return
     */
    fun stringToHexString(s: String): String {
        var str = ""
        for (element in s) {
            val ch = element.code
            val s4 = Integer.toHexString(ch)
            str += s4
        }
        return str
    }

    /**
     * 16进制字符串转换为字符串
     * Convert a hex string to a string
     *
     * @param str
     * @return
     */
    fun hexStringToString(str: String?): String? {
        var s = str
        if (s == null || s == "") {
            return null
        }
        s = s.replace(" ".toRegex(), "")
        val baKeyword = ByteArray(s.length / 2)
        for (i in baKeyword.indices) {
            try {
                baKeyword[i] = (0xff and
                        s.substring(i * 2, i * 2 + 2).toInt(16)).toByte()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        try {
            s = String(baKeyword, Charset.defaultCharset())
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
        return s
    }
}