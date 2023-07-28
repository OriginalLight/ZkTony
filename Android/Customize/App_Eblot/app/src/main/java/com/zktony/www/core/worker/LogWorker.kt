package com.zktony.www.core.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.www.data.dao.LogRecordDao
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class LogWorker constructor(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val logRecordDao: LogRecordDao by inject()
    private val logDataDao: LogRecordDao by inject()

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