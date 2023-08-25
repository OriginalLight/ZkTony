package com.zktony.android.utils.extra.internal

import com.zktony.android.data.AppDatabase
import com.zktony.android.data.entities.Motor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @author 刘贺贺
 * @date 2023/8/25 8:58
 */
abstract class AppStateObserver : KoinComponent {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val database: AppDatabase by inject()

    init {
        observerOne()
        observerTwo()
    }

    private fun observerOne() {
        scope.launch {
            database.MotorDao().getAll().collect {
                if (it.isNotEmpty()) {
                    callbackOne(it)
                } else {
                    val list = mutableListOf<Motor>()
                    for (i in 0..15) {
                        list.add(Motor(text = "M$i", index = i))
                    }
                    database.MotorDao().insertAll(list)
                }
            }
        }
    }

    private fun observerTwo() {
        scope.launch {
            database.CalibrationDao().getAll().collect {
                if (it.isNotEmpty()) {
                    val active = it.find { c -> c.active }
                    if (active == null) {
                        database.CalibrationDao().update(it[0].copy(active = true))
                    } else {
                        callbackTwo(active.vps())
                    }
                }
            }
        }
    }

    abstract fun callbackOne(list: List<Motor>)

    abstract fun callbackTwo(list: List<Double>)
}