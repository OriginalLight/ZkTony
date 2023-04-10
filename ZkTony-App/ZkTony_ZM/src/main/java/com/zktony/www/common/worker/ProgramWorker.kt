package com.zktony.www.common.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.core.ext.logi
import com.zktony.core.ext.simpleDateFormat
import com.zktony.proto.Program
import com.zktony.protobuf.grpc.ProgramGrpc
import com.zktony.www.room.dao.ProgramDao
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent


class ProgramWorker constructor(
    private val dao: ProgramDao,
    private val grpc: ProgramGrpc,
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    override suspend fun doWork(): Result {
        try {
            dao.withoutUpload().first().let { programs ->
                if (programs.isEmpty()) {
                    "上传程序数据为空".logi("ProgramWorker")
                    return Result.success()
                }
                val list = mutableListOf<Program>()
                programs.forEach {
                    val content = StringBuilder()
                    content.append("程序名称：${it.name}，泵速：${it.motor}，电压：${it.voltage}，时长：${it.time}")
                    content.append("蛋白名称：${it.proteinName}，最小蛋白：${it.proteinMinSize}，最大蛋白：${it.proteinMaxSize}")
                    content.append("胶种类：${if (it.glueType == 0) "常规胶" else "梯度胶"}，胶厚度：${it.glueConcentration}，最小浓度：${it.glueMinConcentration}%， 最大浓度：${it.glueMaxConcentration}%")
                    content.append("模式：${if (it.model == 0) "转膜" else "染色"}，缓冲液：${it.bufferType}")
                    list.add(
                        Program.newBuilder()
                            .setId(it.id)
                            .setName(it.name)
                            .setContent(content.toString())
                            .setCreateTime(it.createTime.simpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                            .build()
                    )
                }
                grpc.addPrograms(list)
                    .catch {
                        "上传程序数据失败".logi("ProgramWorker")
                    }
                    .collect {
                        if (it.success) {
                            dao.updateAll(programs.map { p -> p.copy(upload = 1) })
                        } else {
                            "上传程序数据失败".logi("ProgramWorker")
                        }
                    }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}