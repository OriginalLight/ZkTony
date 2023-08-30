package com.zktony.android.utils.extra.internal

import com.zktony.android.data.AppDatabase
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


    fun observerOne() {
        scope.launch {
            database.CalibrationDao().getAll().collect {
                if (it.isNotEmpty()) {
                    val active = it.find { c -> c.active }
                    active?.let {
                        callback(active.vps())
                    }
                }
            }
        }
    }

    abstract fun callback(list: List<Double>)
}