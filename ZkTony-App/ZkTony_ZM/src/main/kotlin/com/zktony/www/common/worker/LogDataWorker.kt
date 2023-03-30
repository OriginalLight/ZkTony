package com.zktony.www.common.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.common.ext.simpleDateFormat
import com.zktony.common.utils.logi
import com.zktony.proto.LogDetail
import com.zktony.www.data.local.dao.LogDataDao
import com.zktony.www.data.remote.grpc.LogDetailGrpc
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent


class LogDataWorker constructor(
    private val dao: LogDataDao,
    private val grpc: LogDetailGrpc,
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    override suspend fun doWork(): Result {
        try {
            dao.withoutUpload().first().let { logs ->
                if (logs.isEmpty()) {
                    "上传日志数据为空".logi("LogDataWorker")
                    return Result.success()
                }
                val list = mutableListOf<LogDetail>()
                logs.forEach {
                    list.add(
                        LogDetail.newBuilder()
                            .setId(it.id)
                            .setLogId(it.logId)
                            .setContent("泵速：${it.motor}，电压：${it.voltage},电流：${it.current} 时间：${it.time}")
                            .setCreateTime(it.createTime.simpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                            .build()
                    )
                }
                grpc.addLogDetails(list)
                    .catch {
                        "上传日志数据失败".logi("LogDataWorker")
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