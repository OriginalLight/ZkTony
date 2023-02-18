package com.zktony.www.common.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.www.common.utils.Logger
import com.zktony.www.data.remote.model.LogDTO
import com.zktony.www.data.remote.result.NetworkResult
import com.zktony.www.data.repository.LogRecordRepository
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
    lateinit var logRecordRepository: LogRecordRepository

    @Inject
    lateinit var logRepository: LogRepository

    override suspend fun doWork(): Result {
        try {
            logRecordRepository.withoutUpload().first().let { logs ->
                if (logs.isEmpty()) {
                    Logger.d("LogRecordWorker", "上传日志为空")
                    return Result.success()
                }
                val list = mutableListOf<LogDTO>()
                logs.forEach {
                    list.add(
                        LogDTO(
                            id = it.id,
                            sub_id = it.programId,
                            log_type = "Running",
                            content = "模式：${if (it.model == 0) "转膜" else "染色"}，泵速：${it.motor}，电压：${it.voltage}, 时长：${it.time}",
                            create_time = it.createTime,
                        )
                    )
                }
                logRepository.uploadLogRecords(list).collect { res ->
                    when (res) {
                        is NetworkResult.Success -> {
                            logRecordRepository.updateBatch(logs.map { it.copy(upload = 1) })
                        }
                        is NetworkResult.Error -> {
                            Logger.d("LogRecordWorker", "上传日志失败")
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