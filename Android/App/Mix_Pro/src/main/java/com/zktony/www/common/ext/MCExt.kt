package com.zktony.www.common.ext

import com.zktony.core.ext.intToHex4
import com.zktony.www.helper.MCHelper
import org.koin.java.KoinJavaComponent.get

private val MC = get<MCHelper>(MCHelper::class.java)

// x轴坐标
private var x: Int = 0

/**
 * 脉冲
 *
 * @param index Int
 * @param dv Int
 * @return Int
 */
fun pulse(index: Int, dv: Int): Int {
    return when(index) {
        0 -> {
            val d = dv - x
            x = dv
            d
        }
        else -> {
            (dv / MC.hpc[index - 1]!! * 3200).toInt()
        }
    }
}

/**
 * pulse with config
 *
 * @param index Int
 * @param dv Int
 * @return String
 */
fun pwc(index: Int, dv: Int): String {
    return pulse(index, dv).intToHex4() + MC.hpm[index]!!.hex()
}

fun initMCHelper() {
    MC.init()
}