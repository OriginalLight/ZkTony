package com.zktony.www.common.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.www.data.local.dao.ProgramDao
import com.zktony.www.data.remote.model.ProgramDTO
import com.zktony.www.data.remote.service.ProgramService
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent


class ProgramWorker constructor(
    private val dao: ProgramDao,
    private val service: ProgramService,
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    override suspend fun doWork(): Result {
        try {
            dao.withoutUpload().first().let { programs ->
                if (programs.isEmpty()) {
                    Log.d("ProgramWorker", "上传程序为空")
                    return Result.success()
                }
                val list = mutableListOf<ProgramDTO>()
                programs.forEach {
                    val content = StringBuilder()
                    content.append("程序名称：${it.name}，泵速：${it.motor}，电压：${it.voltage}，时长：${it.time}")
                    content.append("蛋白名称：${it.proteinName}，最小蛋白：${it.proteinMinSize}，最大蛋白：${it.proteinMaxSize}")
                    content.append("胶种类：${if (it.glueType == 0) "常规胶" else "梯度胶"}，胶厚度：${it.glueConcentration}，最小浓度：${it.glueMinConcentration}%， 最大浓度：${it.glueMaxConcentration}%")
                    content.append("模式：${if (it.model == 0) "转膜" else "染色"}，缓冲液：${it.bufferType}")
                    list.add(
                        ProgramDTO(
                            id = it.id,
                            name = it.name,
                            content = content.toString(),
                            create_time = it.createTime,
                        )
                    )
                }
                service.uploadProgram(list)
                    .catch {
                        Log.d("ProgramWorker", "上传程序失败")
                    }
                    .collect {
                        dao.updateAll(programs.map { it.copy(upload = 1) })
                    }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}