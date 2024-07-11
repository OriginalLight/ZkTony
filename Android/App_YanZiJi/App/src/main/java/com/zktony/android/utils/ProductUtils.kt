package com.zktony.android.utils

object ProductUtils {

    const val MAX_CHANNEL_COUNT = 4
    private var ProductNumber: String = "M-Blot T4000"
    private var SerialNumber: String = "Unknown"

    val ProductNumberList = listOf(
        "M-Blot T4000",
        "M-Blot T4100",
        "M-Blot T4200",
        "M-Blot T2000",
        "M-Blot T2100",
        "M-Blot T2200"
    )

    // 设置产品型号
    fun setProductNumber(productNumber: String) {
        ProductNumber = productNumber
    }

    // 设置产品序列号
    fun setSerialNumber(sn: String) {
        SerialNumber = sn
    }

    // 获取产品型号
    fun getProductNumber(): String {
        return ProductNumber
    }

    // 获取产品序列号
    fun getSerialNumber(): String {
        return SerialNumber
    }

    // 根据产品型号获取模块数量
    fun getChannelCount(): Int {
        return when (ProductNumber) {
            "M-Blot T2000" -> 2
            "M-Blot T2100" -> 2
            "M-Blot T2200" -> 2
            "M-Blot T4000" -> 4
            "M-Blot T4100" -> 4
            "M-Blot T4200" -> 4
            else -> 4
        }
    }
}