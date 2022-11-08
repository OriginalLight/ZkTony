package com.zktony.www.common.worker

import androidx.work.*
import com.zktony.www.common.app.CommonApplicationProxy
import com.zktony.www.common.utils.Constants
import java.util.concurrent.TimeUnit

/**
 * @author: 刘贺贺
 * @date: 2022-09-27 15:00
 */
class WorkerManager {

    private val programRequest by lazy {
        PeriodicWorkRequestBuilder<ProgramWorker>(60, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).build()
    }

    private val logRecordRequest by lazy {
        PeriodicWorkRequestBuilder<LogWorker>(60, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).build()
    }

    fun createWorker() {
        WorkManager.getInstance(CommonApplicationProxy.application).enqueueUniquePeriodicWork(
            Constants.BACKGROUND_WORKER_NAME_PROGRAM,
            ExistingPeriodicWorkPolicy.REPLACE,
            programRequest
        )
        WorkManager.getInstance(CommonApplicationProxy.application).enqueueUniquePeriodicWork(
            Constants.BACKGROUND_WORKER_NAME_LOG_RECORD,
            ExistingPeriodicWorkPolicy.REPLACE,
            logRecordRequest
        )
    }

    companion object {
        @JvmStatic
        val instance: WorkerManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            WorkerManager()
        }
    }
}