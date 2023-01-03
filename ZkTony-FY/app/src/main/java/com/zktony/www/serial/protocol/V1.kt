package com.zktony.www.serial.protocol

import com.zktony.www.common.extension.toHex

/**
 * @author: 刘贺贺
 * @date: 2022-10-17 13:09
 */
data class V1(
    val head: String = "EE",
    val addr: String = "01",
    val fn: String = "06",
    val pa: String = "0A",
    val data: String = "",
    val end: String = "FFFCFFFF"
) {
    /**
     * 获取十六进制字符串
     * @return 16进制字符串
     */
    fun toHex(): String {
        return head.trim() + addr.trim() + fn.trim() + pa.trim() + data.trim() + end.trim()
    }

    companion object {
        /**
         * 设置温度 下次开机不生效
         * @param addr 地址
         * @param temp 温度
         * @return 指令
         */
        @JvmStatic
        fun setTemp(addr: String, temp: String): String {
            return "TC1:TCADJUSTTEMP=$temp@$addr\r"
        }

        /**
         * 保存温度 下次开机生效
         * @param addr 地址
         * @param temp 温度
         * @return 指令
         */
        @JvmStatic
        fun saveTemp(addr: String, temp: String): String {
            return "TC1:TCADJUSTTEMP!$temp@$addr\r"
        }

        /**
         * 查询温度
         * @param addr 地址
         * @return [String] 指令
         */
        @JvmStatic
        fun queryTemp(addr: String): String {
            return "TC1:TCACTUALTEMP?@$addr\r"
        }

        /**
         * 暂停摇床
         * @return [String] 指令
         */
        @JvmStatic
        fun pauseShakeBed(): String {
            return V1(pa = "0B", data = "0100").toHex()
        }

        /**
         * 恢复摇床
         * @return [String] 指令
         */
        @JvmStatic
        fun resumeShakeBed(): String {
            return V1(pa = "0B", data = "0101").toHex()
        }

        /**
         * 单点走位
         * @param data [String] 字符串
         * @return [String] 指令
         */
        @JvmStatic
        fun singlePoint(data: String): String {
            return V1(fn = "05", pa = "01", data = "0101" + data.toHex()).toHex()
        }

        /**
         * 多点走位
         * @param data [String] 字符串
         * @return [String] 指令
         */
        @JvmStatic
        fun multiPoint(data: String): String {
            return V1(fn = "05", pa = "04", data = "0101" + data.toHex()).toHex()
        }

        /**
         * 查询抽屉状态
         * @return [String] 指令
         */
        @JvmStatic
        fun queryDrawer(): String {
            return V1(pa = "0C").toHex()
        }
    }
}