package com.zktony.www.manager

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.zktony.common.ext.Ext
import com.zktony.www.common.worker.LogWorker
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author: 刘贺贺
 * @date: 2022-09-27 15:00
 */
class WorkerManager {

    fun createWorker() {
        WorkManager.getInstance(Ext.ctx).enqueueUniquePeriodicWork(
            UUID.randomUUID().toString(),
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<LogWorker>(1, TimeUnit.HOURS).setConstraints(
                Constraints.Builder().build()
            ).build()
        )
    }
}