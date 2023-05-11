package com.zktony.android.core.ext

import com.zktony.android.data.entity.CalibrationData
import com.zktony.core.ext.format

/**
 * @author 刘贺贺
 * @date 2023/5/11 16:26
 */

fun List<CalibrationData>.compute() : List<Triple<Int, Float, List<CalibrationData>>> {
    val vl = mutableListOf<Triple<Int, Float, List<CalibrationData>>>()
    for (i in 0..12) {
        val dataList = this.filter { it.index == i }
        if (dataList.isNotEmpty()) {
            val avg = dataList.map { data -> data.percent }.average().toFloat()
            vl.add(Triple(i, avg, dataList))
        }
    }
    return vl
}

fun List<CalibrationData>.avgRate() : List<Float> {
    val vl = mutableListOf<Float>()
    for (i in 0..12) {
        val dataList = this.filter { it.index == i }
        if (dataList.isNotEmpty()) {
            val avg = dataList.map { data -> data.percent }.average().toFloat()
            vl.add(avg)
        } else {
            vl.add(1f)
        }
    }
    return vl
}