package com.zktony.www.common.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.www.common.network.result.NetworkResult
import com.zktony.www.common.utils.Logger
import com.zktony.www.data.repository.LogDataRepository
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
class LogDataWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    @Inject
    lateinit var logDataRepository: LogDataRepository

    @Inject
    lateinit var logRepository: LogRepository

    override suspend fun doWork(): Result {
        try {
            logDataRepository.withoutUpload().first().let { logs ->
                if (logs.isEmpty()) {
                    Logger.d("LogDataWorker", "上传日志数据为空")
                    return Result.success()
                }
                logRepository.uploadLogData(logs).collect { res ->
                    when (res) {
                        is NetworkResult.Success -> {
                            logDataRepository.updateBatch(logs.map { it.copy(upload = 1) })
                        }
                        is NetworkResult.Error -> {
                            Logger.d("LogDataWorker", "上传日志数据失败")
                        }
                        else -> {}
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}