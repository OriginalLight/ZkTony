package com.zktony.android.core.ext

import com.zktony.android.data.entity.CalibrationData
import com.zktony.android.data.entity.CalibrationEntity
import com.zktony.android.data.entity.ContainerEntity
import com.zktony.android.data.entity.ProgramEntity

/**
 * @author 刘贺贺
 * @date 2023/5/16 13:29
 */

fun ContainerEntity.axis(): List<Float> {
    return if (this.data.isEmpty()) {
        listOf(0f, 0f, 0f)
    } else {
        this.data[0].axis
    }
}

fun ProgramEntity.volume(): List<Int> {
    return if (this.data.isEmpty()) {
        listOf(0, 0, 0, 0)
    } else {
        this.data[0].volume
    }
}

fun CalibrationEntity.compute(): List<Triple<Int, Float, List<CalibrationData>>> {
    val vl = mutableListOf<Triple<Int, Float, List<CalibrationData>>>()
    for (i in 0..12) {
        val dataList = this.data.filter { it.index == i }
        if (dataList.isNotEmpty()) {
            val avg = dataList.map { data -> data.percent }.average().toFloat()
            vl.add(Triple(i, avg, dataList))
        }
    }
    return vl
}

fun CalibrationEntity.avgRate(): List<Float> {
    val vl = mutableListOf<Float>()
    for (i in 0..12) {
        val dataList = this.data.filter { it.index == i }
        if (dataList.isNotEmpty()) {
            val avg = dataList.map { data -> data.percent }.average().toFloat()
            vl.add(avg)
        } else {
            vl.add(1f)
        }
    }
    return vl
}