package com.zktony.android.utils

import com.zktony.android.data.enums.Product

object ProductUtils {

    const val MAX_CHANNEL_COUNT = 4
    private var product: Product = Product.M_BLOT_T4000
    private var serialNumber: String = "Unknown"

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
        product = Product.fromNumber(productNumber)
    }

    // 设置产品序列号
    fun setSerialNumber(sn: String) {
        serialNumber = sn
    }

    // 获取产品型号
    fun getProductNumber(): String {
        return product.number
    }

    // 获取产品名
    fun getProductResId(): Int {
        return product.resId
    }

    // 获取产品序列号
    fun getSerialNumber(): String {
        return serialNumber
    }

    // 根据产品型号获取模块数量
    fun getChannelCount(): Int {
        return product.count
    }
}