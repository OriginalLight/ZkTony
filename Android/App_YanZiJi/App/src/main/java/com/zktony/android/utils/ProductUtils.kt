package com.zktony.android.utils

import com.zktony.android.data.Product

object ProductUtils {

    const val MAX_CHANNEL_COUNT = 4
    private var product: Product = Product.M_BLOT_T4000

    // 设置产品型号
    fun with(pn: String) {
        product = Product.fromName(pn)
    }

    // 获取产品名
    fun getProductResId(): Int {
        return product.resId
    }

    // 根据产品型号获取模块数量
    fun getChannelCount(): Int {
        return product.count
    }
}