package com.zktony.android.utils

import androidx.datastore.preferences.core.floatPreferencesKey
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.data.entities.Motor
import com.zktony.android.utils.ext.dataSaver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.ConcurrentHashMap

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 14:27
 */
class AsyncHelper {
    private val md: MotorDao by inject(MotorDao::class.java)
    private val cd: CalibrationDao by inject(CalibrationDao::class.java)
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 电机信息
    val hpm: MutableMap<Int, Motor> = ConcurrentHashMap()

    // 校准信息
    val hpc: MutableMap<Int, Double> = ConcurrentHashMap()

    init {
        scope.launch {
            launch {
                asyncTaskOne()
            }
            launch {
                asyncTaskTwo()
            }
        }
    }

    /**
     * 从数据库中获取所有的电机信息
     *
     * 如果数据库中没有数据，则插入默认数据
     *
     * 如果数据库中有数据，则将数据存入hpm中
     *
     * @return Unit
     */
    private suspend fun asyncTaskOne() {
        md.getAll().collect {
            if (it.isNotEmpty()) {
                it.forEach { it1 ->
                    hpm[it1.index] = it1
                }
            } else {
                val list = mutableListOf<Motor>()
                for (i in 0..15) {
                    list.add(Motor(text = "M$i", index = i))
                }
                md.insertAll(list)
            }
        }
    }

    /**
     * 从数据库中获取所有的校准信息
     *
     * 如果数据库中没有数据，则使用默认数据
     *
     * 如果数据库中有数据，则将数据存入hpc中
     *
     * @return Unit
     */
    private suspend fun asyncTaskTwo() {
        val flow1 = dataSaver.getDataStore().data.map { setting ->
            setting[floatPreferencesKey("jyq")] ?: 1f
        }

        val flow2 = dataSaver.getDataStore().data.map { setting ->
            setting[floatPreferencesKey("jyh")] ?: 1f
        }
        flow1.zip(flow2) { jyq, jyh ->
            jyq to jyh
        }.collect {
            hpc.clear()
            hpc[0] = 4.0 / 3200
            hpc[1] = 6.35 / 3200
            hpc[2] = 4.0 / 3200
            hpc[3] = 6.35 / 3200
            hpc[4] = 4.0 / 3200
            hpc[5] = (it.first / it.second).toDouble()
        }
    }

    companion object {
        val instance: AsyncHelper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { AsyncHelper() }
    }
}