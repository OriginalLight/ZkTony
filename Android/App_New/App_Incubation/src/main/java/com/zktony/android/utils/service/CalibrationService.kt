package com.zktony.android.utils.service

import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.entities.Calibration
import com.zktony.android.utils.AlgorithmUtils.calculateCalibrationFactor
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
                    hpc[item.index] = calculateFunction(item)
                }
            }
        }
    }

    private fun calculateFunction(calibration: Calibration): (Double) -> Double {
        if (!calibration.enable) {
            return { x -> x * 100 }
        }

        return calculateCalibrationFactor(calibration.points)
    }
}
