package com.zktony.www.manager

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.zktony.common.ext.Ext
import com.zktony.common.utils.Constants
import com.zktony.www.common.worker.LogWorker
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
        WorkManager.getInstance(Ext.ctx).enqueueUniquePeriodicWork(
            Constants.BACKGROUND_WORKER_NAME_LOG,
            ExistingPeriodicWorkPolicy.UPDATE,
            logRequest
        )
    }
}