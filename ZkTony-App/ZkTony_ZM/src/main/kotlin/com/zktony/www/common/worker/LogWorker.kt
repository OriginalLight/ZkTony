package com.zktony.www.common.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.www.data.local.room.dao.LogRecordDao
import org.koin.core.component.KoinComponent


class LogWorker constructor(
    private val logRecordDao: LogRecordDao,
    private val logDataDao: LogRecordDao,
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    override suspend fun doWork(): Result {
        return try {
            logRecordDao.deleteByDate()
            logDataDao.deleteByDate()
            logDataDao.deleteInvaliedLog()
            logRecordDao.deleteInvaliedLog()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}