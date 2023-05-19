package com.zktony.android.logic.ext

import com.zktony.android.logic.data.entities.CalibrationData
import com.zktony.android.logic.data.entities.CalibrationEntity
import com.zktony.android.logic.data.entities.ContainerEntity
import com.zktony.android.logic.data.entities.ProgramEntity

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

fun ProgramEntity.volume(): List<Float> {
    return if (this.data.isEmpty()) {
        listOf(0f, 0f, 0f, 0f)
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