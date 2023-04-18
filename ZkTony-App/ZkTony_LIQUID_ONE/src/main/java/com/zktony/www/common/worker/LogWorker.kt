package com.zktony.www.common.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.www.room.dao.LogDao
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-20 14:47
 */
class LogWorker constructor(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    private val dao: LogDao by inject()
    override suspend fun doWork(): Result {
        return try {
            dao.deleteByDate()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}