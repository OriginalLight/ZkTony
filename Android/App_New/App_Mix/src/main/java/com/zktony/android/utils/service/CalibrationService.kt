package com.zktony.android.utils.service

import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.entities.Calibration
import com.zktony.android.utils.AlgorithmUtils.calculateLinearRelation
import com.zktony.android.utils.AlgorithmUtils.fitQuadraticCurve
import com.zktony.android.utils.AppStateUtils.hpc
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author 刘贺贺
 * @date 2023/9/1 10:29
 */
class CalibrationService @Inject constructor(
    private val dao: CalibrationDao
) : AbstractService() {
    /**
     * 初始化曲线
     * 1. 从数据库中获取所有曲线
     * 2. 计算曲线函数
     * 3. 将曲线函数保存到appState中
     */
    override fun create() {
        job = scope.launch {
            dao.getAll().collect { items ->
                items.forEach { item ->
                    val curveFunction = calculateCurveFunction(item)
                    hpc[item.index] = curveFunction
                }
            }
        }
    }

    private fun calculateCurveFunction(calibration: Calibration): (Double) -> Double? {
        if (!calibration.enable) {
            return { x -> x * 100 }
        }

        val points = calibration.points
        val check = points.all { it.x > 0.0 && it.y > 0.0 }

        return when (points.size) {
            0 -> { x -> x * 100 }
            1 -> if (check) calculateLinearRelation(points[0]) else { x -> x * 100 }
            2 -> if (check) calculateLinearRelation(points[0], points[1]) else { x -> x * 100 }
            else -> if (check) fitQuadraticCurve(points) else { x -> x * 100 }
        }
    }
}
