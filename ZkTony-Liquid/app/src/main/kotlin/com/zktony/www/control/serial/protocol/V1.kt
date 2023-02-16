package com.zktony.www.control.serial.protocol

import com.zktony.www.common.extension.toHex

/**
 * @author: 刘贺贺
 * @date: 2022-10-17 13:09
 */
data class V1(
    val header: String = "EE",
    val address: String = "01",
    val function: String = "06",
    val parameter: String = "0A",
    val data: String = "",
    val end: String = "FFFCFFFF"
) {
    /**
     * 获取十六进制字符串
     * @return 16进制字符串
     */
    fun toHex(): String {
        return header.trim() + address.trim() + function.trim() + parameter.trim() + data.trim() + end.trim()
    }

    companion object {

        /**
         * 单点走位
         * @param data [String] 字符串
         * @return [String] 指令
         */
        @JvmStatic
        fun singlePoint(data: String): String {
            return V1(function = "05", parameter = "01", data = "0101" + data.toHex()).toHex()
        }

        /**
         * 多点走位
         * @param data [String] 字符串
         * @return [String] 指令
         */
        @JvmStatic
        fun complex(data: String): String {
            return V1(function = "05", parameter = "04", data = "0101" + data.toHex()).toHex()
        }

    }
}