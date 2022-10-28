package com.zktony.www.serialport.protocol

import com.zktony.www.common.extension.toHex

/**
 * @author: 刘贺贺
 * @date: 2022-10-17 13:09
 */
data class Command(
    var header: String = "EE",
    var address: String = "01",
    var function: String = "06",
    var parameter: String = "0A",
    var data: String = "",
    var end: String = "FFFCFFFF"
) {
    constructor(hex: String) : this() {
        header = hex.substring(0, 2)
        address = hex.substring(2, 4)
        function = hex.substring(4, 6)
        parameter = hex.substring(6, 8)
        data = hex.substring(8, hex.length - 8)
        end = hex.substring(hex.length - 8, hex.length)
    }

    /**
     * 获取十六进制字符串
     * @return 16进制字符串
     */
    fun toHex(): String {
        return header.trim() + address.trim() + function.trim() + parameter.trim() + data.trim() + end.trim()
    }

    companion object {
        /**
         * 设置温度 下次开机不生效
         * @param address 地址
         * @param temperature 温度
         * @return 指令
         */
        @JvmStatic
        fun setTemperature(address: String, temperature: String): String {
            return "TC1:TCADJUSTTEMP=$temperature@$address\r"
        }

        /**
         * 保存温度 下次开机生效
         * @param address 地址
         * @param temperature 温度
         * @return 指令
         */
        @JvmStatic
        fun saveTemperature(address: String, temperature: String): String {
            return "TC1:TCADJUSTTEMP!$temperature@$address\r"
        }

        /**
         * 查询温度
         * @param address 地址
         * @return [String] 指令
         */
        @JvmStatic
        fun queryTemperature(address: String): String {
            return "TC1:TCACTUALTEMP?@$address\r"
        }

        /**
         * 暂停摇床
         * @return [String] 指令
         */
        @JvmStatic
        fun pauseShakeBed(): String {
            return Command(parameter = "0B", data = "0100").toHex()
        }

        /**
         * 恢复摇床
         * @return [String] 指令
         */
        @JvmStatic
        fun resumeShakeBed(): String {
            return Command(parameter = "0B", data = "0101").toHex()
        }

        /**
         * 单点走位
         * @param data [String] 字符串
         * @return [String] 指令
         */
        @JvmStatic
        fun singlePoint(data: String): String {
            return Command(function = "05", parameter = "01", data = "0101" + data.toHex()).toHex()
        }

        /**
         * 多点走位
         * @param data [String] 字符串
         * @return [String] 指令
         */
        @JvmStatic
        fun multiPoint(data: String): String {
            return Command(function = "05", parameter = "04", data = "0101" + data.toHex()).toHex()
        }
    }
}