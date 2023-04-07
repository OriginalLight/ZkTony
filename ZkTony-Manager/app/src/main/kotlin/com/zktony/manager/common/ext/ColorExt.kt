package com.zktony.manager.common.ext

// 生成随机颜色
fun randomColor(): Int {
    val random = java.util.Random()
    return 0xff000000.toInt() or random.nextInt(0x00ffffff)
}

fun randomColor(alpha: Int): Int {
    val random = java.util.Random()
    return alpha shl 24 or random.nextInt(0x00ffffff)
}
