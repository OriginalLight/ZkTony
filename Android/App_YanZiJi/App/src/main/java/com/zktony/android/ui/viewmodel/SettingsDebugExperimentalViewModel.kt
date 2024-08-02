package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.ExperimentalControl
import com.zktony.android.data.PipelineControl
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.StorageUtils
import com.zktony.android.utils.TipsUtils
import com.zktony.android.utils.extra.dateFormat
import com.zktony.log.LogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SettingsDebugExperimentalViewModel @Inject constructor() : ViewModel() {

    // 数据采集任务列表
    private val collectingJobList: MutableList<Job?> = MutableList(4) { null }

    suspend fun pipelineClean(channel: Int, control: PipelineControl): Boolean {
        if (!SerialPortUtils.pipelineClean(channel, control)
        ) {
            TipsUtils.showTips(Tips.error("管路清洗失败 通道：${channel + 1}"))
            return false
        } else {
            TipsUtils.showTips(Tips.info("管路清洗成功 通道：${channel + 1}"))
            return true
        }
    }

    suspend fun startExperiment(channel: Int, experimental: ExperimentalControl): Boolean {
        if (!SerialPortUtils.setExperimentalArguments(channel, experimental)) {
            TipsUtils.showTips(Tips.error("实验参数设置失败 通道：${channel + 1}"))
            return false
        } else {
            TipsUtils.showTips(Tips.info("实验参数设置成功 通道：${channel + 1}"))
        }

        if (!SerialPortUtils.setExperimentalState(channel, 1)) {
            TipsUtils.showTips(Tips.error("实验开始失败 通道：${channel + 1}"))
            return false
        } else {
            TipsUtils.showTips(Tips.info("实验开始成功 通道：${channel + 1}"))
            startCollecting(channel, experimental)
            return true
        }
    }

    suspend fun stopExperiment(channel: Int): Boolean {
        if (!SerialPortUtils.setExperimentalState(channel, 3)) {
            TipsUtils.showTips(Tips.error("实验停止失败 通道：${channel + 1}"))
            return false
        } else {
            TipsUtils.showTips(Tips.info("实验停止成功 通道：${channel + 1}"))
            stopCollecting(channel)
            return true
        }
    }

    private fun startCollecting(channel: Int, control: ExperimentalControl) {
        collectingJobList[channel]?.cancel()
        collectingJobList[channel] = viewModelScope.launch {
            val dir = StorageUtils.getCacheDir() + "/${StorageUtils.EXPERIMENTAL_LOG_DIR}"
            if (!File(dir).exists()) {
                File(dir).mkdirs()
            }
            val files =
                File("$dir/channel$channel ${Date(System.currentTimeMillis()).dateFormat("yyyyMMddHHmmss")}.csv")
            if (!files.exists()) {
                files.createNewFile()
            }
            files.appendText("电压(V): ${control.voltage},电流(A): ${control.current},功率(W): ${control.power},温度(℃): ${control.temperature},时间(s): ${control.time},流量(mL/min): ${control.flowSpeed}\n")
            while (true) {
                val state = AppStateUtils.channelStateList.value[channel]
                if (state.step == 7) {
                    files.appendText("${state.voltage},${state.current},${state.power},${state.temperature},${state.time}\n")
                }
                delay(1000L)
            }
        }
    }

    private fun stopCollecting(channel: Int) {
        collectingJobList[channel]?.cancel()
    }

    suspend fun exportCollecting() {
        try {
            val usbList = StorageUtils.getUsbStorageDir()
            if (usbList.isEmpty()) {
                TipsUtils.showTips(Tips.error("未检测到U盘"))
                return
            }
            val dstDir =
                usbList.first() + "/${StorageUtils.ROOT_DIR}/${StorageUtils.LOG_DIR}/${StorageUtils.EXPERIMENTAL_LOG_DIR}"
            if (!File(dstDir).exists()) {
                File(dstDir).mkdirs()
            }

            val srcDir = StorageUtils.getCacheDir() + "/${StorageUtils.EXPERIMENTAL_LOG_DIR}"
            if (!File(srcDir).exists()) {
                TipsUtils.showTips(Tips.error("未检测到实验数据"))
                return
            }

            val srcFiles = File(srcDir).listFiles()
            if (srcFiles.isNullOrEmpty()) {
                TipsUtils.showTips(Tips.error("未检测到实验数据"))
                return
            }

            srcFiles.forEach { file ->
                withContext(Dispatchers.IO) {
                    file.copyTo(File(dstDir + "/" + file.name), true)
                    file.delete()
                }
                delay(100L)
            }

            TipsUtils.showTips(Tips.info("导出成功"))
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
            TipsUtils.showTips(Tips.error("导出失败"))
        }
    }
}