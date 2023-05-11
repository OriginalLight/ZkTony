package com.zktony.android.core.ext

import com.zktony.android.data.entity.CalibrationData
import com.zktony.core.ext.format

/**
 * @author 刘贺贺
 * @date 2023/5/11 16:26
 */

fun List<CalibrationData>.compute() : List<Pair<Int, String>> {
    val vl = List(13) { 100f }.toMutableList()
    val sl = List(13) { Pair(0, "") }.toMutableList()
    vl.forEachIndexed { index, item ->
        val dataList = this.filter { it.index == index }
        if (dataList.isNotEmpty()) {
            vl[index] = item * dataList.map { data -> data.percent }.average().toFloat()
            val str = if (dataList.size > 1) {
                "V${index + 1} = ${vl[index].format(2)} μL = 100  μL x ( ${
                    dataList.joinToString(" + ") { data ->
                        data.percent.format(
                            2
                        )
                    }
                } ) / ${dataList.size}"
            } else {
                "V${index + 1} = ${vl[index].format(2)} μL = 100  μL x ${
                    dataList.joinToString(" + ") { data ->
                        data.percent.format(
                            2
                        )
                    }
                }"
            }
            sl[index] = Pair(dataList.first().index, str)
        } else {
            vl[index] = 100f
            sl[index] = Pair(-1, "V${index + 1} = 100 μL")
        }
    }
    return sl
}