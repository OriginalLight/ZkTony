package com.zktony.www.manager

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.zktony.core.ext.Ext
import com.zktony.core.ext.logi
import com.zktony.www.common.worker.LogWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * @author: 刘贺贺
 * @date: 2022-09-27 15:00
 */
class WorkerManager {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
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