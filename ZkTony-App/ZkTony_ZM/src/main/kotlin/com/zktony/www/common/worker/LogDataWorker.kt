package com.zktony.www.common.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.www.data.local.dao.LogDataDao
import com.zktony.www.data.remote.model.LogDetailDTO
import com.zktony.www.data.remote.service.LogService
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent


class LogDataWorker constructor(
    private val dao: LogDataDao,
    private val service: LogService,
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    override suspend fun doWork(): Result {
        try {
            dao.withoutUpload().first().let { logs ->
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
                service.uploadLogDetail(list)
                    .catch {
                        Log.d("LogDataWorker", "上传日志数据失败")
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