package com.zktony.android.utils.service

import com.zktony.android.data.dao.CalibrationDao
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
    override fun create() {
        job = scope.launch {
            dao.getAll().collect { items ->
                items.forEach { item ->
                    if (item.enable) {
                        hpc[item.index] = calculateCalibrationFactor(item.points)
                    }
                }
            }
        }
    }
}
