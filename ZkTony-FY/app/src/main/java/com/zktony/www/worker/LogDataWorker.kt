package com.zktony.www.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.www.common.Logger
import com.zktony.www.common.http.adapter.isSuccess
import com.zktony.www.data.repository.LogDataRepository
import com.zktony.www.data.services.LogService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-20 14:47
 */
@HiltWorker
class LogDataWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    @Inject
    lateinit var logDataRepository: LogDataRepository

    @Inject
    lateinit var service: LogService

    override suspend fun doWork(): Result {
        try {
            logDataRepository.withoutUpload().first().let {
                if (it.isEmpty()) {
                    Logger.d("LogDataWorker", "上传日志数据为空")
                    return Result.success()
                }
                val res = service.uploadLogData(it)
                if (res.isSuccess) {
                    it.forEach { logData ->
                        logData.upload = 1
                    }
                    logDataRepository.updateBatch(it)
                } else {
                    Logger.e("LogDataWorker", "上传日志数据失败")
                    return Result.failure()
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}