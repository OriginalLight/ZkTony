package com.zktony.www.manager

import androidx.work.*
import com.zktony.common.ext.Ext
import com.zktony.www.common.worker.LogDataWorker
import com.zktony.www.common.worker.LogRecordWorker
import com.zktony.www.common.worker.LogWorker
import com.zktony.www.common.worker.ProgramWorker
import java.util.*
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
        PeriodicWorkRequestBuilder<LogRecordWorker>(60, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).build()
    }

    private val logDataRequest by lazy {
        PeriodicWorkRequestBuilder<LogDataWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).build()
    }

    private val logRequest by lazy {
        PeriodicWorkRequestBuilder<LogWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .build()
            ).build()
    }

    fun createWorker() {
        WorkManager.getInstance(Ext.ctx).enqueueUniquePeriodicWork(
            UUID.randomUUID().toString(),
            ExistingPeriodicWorkPolicy.UPDATE,
            programRequest
        )
        WorkManager.getInstance(Ext.ctx).enqueueUniquePeriodicWork(
            UUID.randomUUID().toString(),
            ExistingPeriodicWorkPolicy.UPDATE,
            logRecordRequest
        )
        WorkManager.getInstance(Ext.ctx).enqueueUniquePeriodicWork(
            UUID.randomUUID().toString(),
            ExistingPeriodicWorkPolicy.UPDATE,
            logDataRequest
        )
        WorkManager.getInstance(Ext.ctx).enqueueUniquePeriodicWork(
            UUID.randomUUID().toString(),
            ExistingPeriodicWorkPolicy.UPDATE,
            logRequest
        )
    }
}