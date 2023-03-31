package com.zktony.www.manager

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.zktony.common.ext.Ext
import com.zktony.www.common.worker.LogWorker
import java.util.concurrent.TimeUnit

/**
 * @author: 刘贺贺
 * @date: 2022-09-27 15:00
 */
class WorkerManager {
    fun createWorker() {
        WorkManager.getInstance(Ext.ctx).enqueue(
            OneTimeWorkRequestBuilder<LogWorker>()
                .setInitialDelay(15, TimeUnit.MINUTES)
                .build()
        )
    }
}