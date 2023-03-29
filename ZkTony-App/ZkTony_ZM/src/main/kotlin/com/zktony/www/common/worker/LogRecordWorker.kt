package com.zktony.www.common.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.www.data.local.dao.LogRecordDao
import com.zktony.www.data.remote.model.LogDTO
import com.zktony.www.data.remote.service.LogService
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent


class LogRecordWorker constructor(
    private val dao: LogRecordDao,
    private val service: LogService,
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    override suspend fun doWork(): Result {
        try {
            dao.withoutUpload().first().let { logs ->
                if (logs.isEmpty()) {
                    Log.d("LogRecordWorker", "上传日志为空")
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
                service.uploadLog(list)
                    .catch {
                        Log.d("LogRecordWorker", "上传日志失败")
                    }
                    .collect {
                        dao.updateAll(logs.map { it.copy(upload = 1) })
                    }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}