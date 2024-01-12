package com.zktony.android.utils.service

import com.zktony.android.data.dao.MotorDao
import com.zktony.android.utils.AppStateUtils.hpm
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
            dao.getAll().collect { list ->
                list.forEach { hpm[it.index] = it }
            }
        }
    }
}