package com.zktony.android.utils

object ProductUtils {

    val ProductNumberList = listOf("M-Blot T4000", "M-Blot T4100", "M-Blot T4200", "M-Blot T2000", "M-Blot T2100", "M-Blot T2200")
    var ProductNumber: String = "M-Blot T4000"

    // 根据产品型号获取模块数量
    fun getModuleCount(): Int {
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