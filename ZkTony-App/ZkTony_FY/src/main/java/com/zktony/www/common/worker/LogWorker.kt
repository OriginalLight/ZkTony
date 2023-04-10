package com.zktony.www.common.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.www.room.dao.LogDao
import org.koin.core.component.KoinComponent

/**
 * @author: 刘贺贺
 * @date: 2022-09-20 14:47
 */
class LogWorker constructor(
    private val dao: LogDao,
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {
    override suspend fun doWork(): Result {
        return try {
            dao.deleteByDate()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}