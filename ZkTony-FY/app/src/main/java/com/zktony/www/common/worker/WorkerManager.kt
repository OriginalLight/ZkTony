package com.zktony.www.common.worker

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.zktony.www.common.app.CommonApplicationProxy
import com.zktony.www.common.utils.Constants
import java.util.concurrent.TimeUnit

/**
 * @author: 刘贺贺
 * @date: 2022-09-27 15:00
 */
class WorkerManager {

    private val logRequest by lazy {
        PeriodicWorkRequestBuilder<LogWorker>(60, TimeUnit.MINUTES).setConstraints(
                Constraints.Builder().build()
            ).build()
    }

    fun createWorker() {
        WorkManager.getInstance(CommonApplicationProxy.application).enqueueUniquePeriodicWork(
            Constants.BACKGROUND_WORKER_NAME_LOG,
            ExistingPeriodicWorkPolicy.REPLACE,
            logRequest
        )
    }

    companion object {
        @JvmStatic
        val instance: WorkerManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            WorkerManager()
        }
    }
}