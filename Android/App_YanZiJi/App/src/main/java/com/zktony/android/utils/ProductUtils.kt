package com.zktony.android.utils

object ProductUtils {

    val ProductNumberList = listOf("T4000", "T4100", "T4200", "T2000", "T2100", "T2200")
    var ProductNumber: String = "T4000"

    // 根据产品型号获取模块数量
    fun getModuleCount(): Int {
        return when (ProductNumber) {
            "T2000" -> 2
            "T2100" -> 2
            "T2200" -> 2
            "T4000" -> 4
            "T4100" -> 4
            "T4200" -> 4
            else -> 4
        }
    }
}