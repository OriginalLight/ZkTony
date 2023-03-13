package com.zktony.www.common.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.www.data.local.room.dao.LogDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * @author: 刘贺贺
 * @date: 2022-09-20 14:47
 */
@HiltWorker
class LogWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val logDao: LogDao,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            logDao.deleteByDate()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}