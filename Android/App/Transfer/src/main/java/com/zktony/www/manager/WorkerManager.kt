package com.zktony.www.manager

import androidx.work.*
import com.zktony.core.ext.Ext
import com.zktony.core.ext.logi
import com.zktony.www.common.worker.*
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

/**
 * @author: 刘贺贺
 * @date: 2022-09-27 15:00
 */
class WorkerManager {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            WorkManager.getInstance(Ext.ctx).enqueueUniquePeriodicWork(
                "worker_program",
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<ProgramWorker>(1, TimeUnit.HOURS)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    ).build()
            )
            WorkManager.getInstance(Ext.ctx).enqueueUniquePeriodicWork(
                "worker_log_record",
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<LogRecordWorker>(1, TimeUnit.HOURS)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    ).build()
            )
            WorkManager.getInstance(Ext.ctx).enqueueUniquePeriodicWork(
                "worker_log_data",
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<LogDataWorker>(15, TimeUnit.MINUTES)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    ).build()
            )
            WorkManager.getInstance(Ext.ctx).enqueue(
                OneTimeWorkRequestBuilder<LogWorker>()
                    .setInitialDelay(15, TimeUnit.MINUTES)
                    .build()
            )
        }
    }

    fun initializer() {
        "定时任务管理器初始化完成！！！".logi()
    }
}