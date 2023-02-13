package com.zktony.www.common.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.www.common.repository.LogDataRepository
import com.zktony.www.common.repository.LogRecordRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-20 14:47
 */
@HiltWorker
class LogWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    @Inject
    lateinit var logRecordRepository: LogRecordRepository

    @Inject
    lateinit var logDataRepository: LogDataRepository

    override suspend fun doWork(): Result {
        return try {
            logRecordRepository.deleteByDate()
            logDataRepository.deleteByDate()
            logDataRepository.deleteDataLessThanTen()
            logRecordRepository.deleteInvalidedLog()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}