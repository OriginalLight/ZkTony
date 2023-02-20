package com.zktony.www.common.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.www.data.remote.model.LogDetailDTO
import com.zktony.common.http.result.NetworkResult
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
                    Log.d("LogDataWorker", "上传日志数据为空")
                    return Result.success()
                }
                val list = mutableListOf<LogDetailDTO>()
                logs.forEach {
                    list.add(
                        LogDetailDTO(
                            id = it.id,
                            log_id = it.logId,
                            content = "泵速：${it.motor}，电压：${it.voltage},电流：${it.current} 时间：${it.time}",
                            create_time = it.createTime,
                        )
                    )
                }
                logRepository.uploadLogData(list).collect { res ->
                    when (res) {
                        is NetworkResult.Success -> {
                            logDataRepository.updateBatch(logs.map { it.copy(upload = 1) })
                        }
                        is NetworkResult.Error -> {
                            Log.d("LogDataWorker", "上传日志数据失败")
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