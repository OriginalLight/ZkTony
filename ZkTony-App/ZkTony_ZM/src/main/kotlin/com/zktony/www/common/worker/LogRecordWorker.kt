package com.zktony.www.common.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.common.ext.simpleDateFormat
import com.zktony.common.utils.logi
import com.zktony.proto.Log
import com.zktony.www.data.local.dao.LogRecordDao
import com.zktony.www.data.remote.grpc.LogGrpc
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent


class LogRecordWorker constructor(
    private val dao: LogRecordDao,
    private val grpc: LogGrpc,
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    override suspend fun doWork(): Result {
        try {
            dao.withoutUpload().first().let { logs ->
                if (logs.isEmpty()) {
                    "上传日志数据为空".logi("LogRecordWorker")
                    return Result.success()
                }
                val list = mutableListOf<Log>()
                logs.forEach {
                    list.add(
                        Log.newBuilder()
                            .setId(it.id)
                            .setSubId(it.programId)
                            .setLogType("Running")
                            .setContent("模式：${if (it.model == 0) "转膜" else "染色"}，泵速：${it.motor}，电压：${it.voltage}, 时长：${it.time}")
                            .setCreateTime(it.createTime.simpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                            .build()
                        )
                }
                grpc.addLogs(list)
                    .catch {
                        "上传日志数据失败".logi("LogRecordWorker")
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