package com.zktony.android.utils.service

import com.zktony.android.data.dao.MotorDao
import com.zktony.android.utils.extra.appState
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author 刘贺贺
 * @date 2023/9/4 13:10
 */
class MotorService @Inject constructor(
    private val dao: MotorDao
) : AbstractService() {

    override fun create() {
        job = scope.launch {
            dao.getAll().collect { motors ->
                motors.forEach { motor -> appState.hpm[motor.index] = motor }
            }
        }
    }

}