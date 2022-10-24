package com.zktony.www.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.www.common.Logger
import com.zktony.www.common.http.adapter.isSuccess
import com.zktony.www.data.repository.LogRecordRespository
import com.zktony.www.data.repository.LogRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-20 14:47
 */
@HiltWorker
class LogRecordWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    @Inject
    lateinit var logRecordRepository: LogRecordRespository

    @Inject
    lateinit var logRepository: LogRepository

    override suspend fun doWork(): Result {
        try {
            logRecordRepository.withoutUpload().first().let {
                if (it.isEmpty()) {
                    Logger.d("LogRecordWorker", "上传日志为空")
                    return Result.success()
                }
                val res = logRepository.uploadLogRecords(it)
                if (res.isSuccess) {
                    it.forEach { logRecord ->
                        logRecord.upload = 1
                    }
                    logRecordRepository.updateBatch(it)
                } else {
                    Logger.e("LogRecordWorker", "上传日志失败")
                    return Result.failure()
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}