package com.zktony.android.utils

import com.zktony.android.data.Arguments
import com.zktony.android.data.ChannelState
import com.zktony.android.data.ExperimentalState
import com.zktony.android.data.LedState
import com.zktony.android.data.ZktyError
import com.zktony.android.data.equateTo
import com.zktony.android.data.isRunning
import com.zktony.android.ui.components.Tips
import com.zktony.log.LogUtils
import com.zktony.room.defaultProgram
import com.zktony.room.entities.ErrorLog
import com.zktony.room.entities.Log
import com.zktony.room.entities.LogSnapshot
import com.zktony.room.entities.Program
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.LinkedTransferQueue

/**
 * @author 刘贺贺
 * @date 2023/8/30 13:40
 */
object AppStateUtils {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 轮询
    val isPolling = Mutex(false)

    // mutableStateFlow
    private val _argumentsList =
        MutableStateFlow(List(ProductUtils.MAX_CHANNEL_COUNT) { Arguments() })
    private val _channelLogList =
        MutableStateFlow(List<Log?>(ProductUtils.MAX_CHANNEL_COUNT) { null })
    private val _channelStateList =
        MutableStateFlow(List(ProductUtils.MAX_CHANNEL_COUNT) { ChannelState() })
    private val _channelProgramList =
        MutableStateFlow(List(ProductUtils.MAX_CHANNEL_COUNT) { defaultProgram() })
    private val _experimentalStateList =
        MutableStateFlow(List(ProductUtils.MAX_CHANNEL_COUNT) { ExperimentalState.NONE })


    // stateFlow
    val argumentsList = _argumentsList.asStateFlow()
    val channelLogList = _channelLogList.asStateFlow()
    val channelStateList = _channelStateList.asStateFlow()
    val channelProgramList = _channelProgramList.asStateFlow()
    val experimentalStateList = _experimentalStateList.asStateFlow()

    // Queue for database
    val channelLogQueue = LinkedTransferQueue<Log>()
    val channelLogSnapshotQueue = LinkedTransferQueue<LogSnapshot>()
    val channelErrorLogQueue = LinkedTransferQueue<ErrorLog>()

    init {
        // 通道状态轮询
        scope.launch {
            var start: Long
            while (true) {
                // 获取通道状态
                start = System.currentTimeMillis()
                repeat(ProductUtils.getChannelCount()) {
                    if (!isPolling.isLocked) {
                        SerialPortUtils.queryChannelState(it)
                    }
                }
                // 间隔时间
                delay(1000L - (System.currentTimeMillis() - start))
            }
        }
    }

    // mutableStateFlow set function
    fun setArguments(channel: Int, arg: Arguments) {
        _argumentsList.value = _argumentsList.value.mapIndexed { index, arguments ->
            if (index == channel) {
                arg
            } else {
                arguments
            }
        }
    }

    fun getArgumentList(): List<Arguments> {
        return _argumentsList.value
    }

    fun setChannelLog(channel: Int, log: Log?) {
        _channelLogList.value = _channelLogList.value.mapIndexed { index, logState ->
            if (index == channel) {
                log
            } else {
                logState
            }
        }
    }

    fun getChannelLog(channel: Int): Log? {
        return _channelLogList.value[channel]
    }

    fun setChannelState(channel: Int, state: ChannelState) {
        _channelStateList.value = _channelStateList.value.mapIndexed { index, channelState ->
            if (index == channel) {
                state
            } else {
                channelState
            }
        }
    }

    fun setChannelProgram(channel: Int, program: Program) {
        _channelProgramList.value = _channelProgramList.value.mapIndexed { index, programState ->
            if (index == channel) {
                program
            } else {
                programState
            }
        }
    }

    fun getChannelProgram(channel: Int): Program {
        return _channelProgramList.value[channel]
    }

    fun setExperimentalStateHook(channel: Int, state: ChannelState) {
        // 错误
        if (state.errorInfo > 0L) {
            collectErrorLog(channel, state)
            if (ZktyError.hasSeverity(state.errorInfo, 1)) {
                transformExperimentalState(channel, ExperimentalState.ERROR)
                return
            }
        }

        // 未插入状态
        if (state.opt1 == 0 && state.opt2 == 0) {
            transformExperimentalState(channel, ExperimentalState.NONE)
            return
        }

        // 通道状态
        when (state.runState) {
            0 -> {
                transformExperimentalState(channel, ExperimentalState.READY)
            }

            1 -> {
                when (state.step) {
                    5 -> {
                        // 开始
                        transformExperimentalState(channel, ExperimentalState.STARTING)
                    }

                    6 -> {
                        // 充液
                        transformExperimentalState(channel, ExperimentalState.FILL)
                    }

                    7 -> {
                        // 计时
                        transformExperimentalState(channel, ExperimentalState.TIMING)
                        collectLogSnapshot(channel, state)
                    }

                    8 -> {
                        // 排液
                        transformExperimentalState(channel, ExperimentalState.DRAIN)
                    }

                    64 -> {
                        // 结束
                        transformExperimentalState(channel, ExperimentalState.READY)
                    }

                    else -> {
                        transformExperimentalState(channel, ExperimentalState.NONE)
                        LogUtils.warn("通道${channel + 1} 未知状态${state.step}")
                    }
                }
            }

            2 -> {
                transformExperimentalState(channel, ExperimentalState.PAUSE)
            }

            3 -> {
                transformExperimentalState(channel, ExperimentalState.READY)
            }

            else -> {}
        }
    }

    fun setLedStateHook() {
        val channelStateList = _channelStateList.value
        val experimentalStateList = _experimentalStateList.value
        // 错误红色
        if (experimentalStateList.any { it == ExperimentalState.ERROR }) {
            // 有错误
            LedUtils.transform(LedState.RED)
            return
        }
        // 警告黄色
        if (channelStateList.any { it.errorInfo > 0L }) {
            // 有警告
            LedUtils.transform(LedState.YELLOW_FLASH)
            return
        }
        // 所有通道都是就绪状态
        if (experimentalStateList.all { it == ExperimentalState.READY }) {
            // 就绪
            LedUtils.transform(LedState.GREEN)
            return
        }
        // 运行或者未插入
        if (experimentalStateList.any { it.isRunning() || it == ExperimentalState.NONE }) {
            // 运行
            LedUtils.transform(LedState.YELLOW)
            return
        }
    }

    fun transformExperimentalState(channel: Int, newState: ExperimentalState) {
        try {
            val oldState = _experimentalStateList.value[channel]
            if (oldState == newState) {
                return
            }
            _experimentalStateList.value =
                _experimentalStateList.value.mapIndexed { index, experimentalState ->
                    if (index == channel) {
                        newState
                    } else {
                        experimentalState
                    }
                }
            // do something when state changed from oldState to newState
            if (!oldState.equateTo(newState)) {
                if (newState == ExperimentalState.READY) {
                    // 更新结束时间
                    _channelLogList.value[channel]?.let {
                        val newLog = it.copy(endTime = System.currentTimeMillis())
                        // add log to queue to database
                        channelLogQueue.add(newLog)
                        // update log
                        setChannelLog(channel, newLog)
                    }
                }

                if (newState == ExperimentalState.ERROR) {
                    // 更新结束时间
                    _channelLogList.value[channel]?.let {
                        val newLog = it.copy(endTime = System.currentTimeMillis(), status = 2)
                        // add log to queue to database
                        channelLogQueue.add(newLog)
                        // update log
                        setChannelLog(channel, newLog)
                    }
                    // 声音 TODO
                }
            }
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
        }
    }

    private fun collectErrorLog(channel: Int, state: ChannelState) {
        try {
            val newError = state.errorInfo
            val oldError = _channelStateList.value[channel].errorInfo
            if (oldError != newError) {
                val newErrorEnum = ZktyError.fromCode(newError)
                val oldErrorEnum = ZktyError.fromCode(oldError)
                // remove new error from old error
                newErrorEnum.forEach {
                    if (!oldErrorEnum.contains(it)) {
                        // add error to queue to database
                        channelErrorLogQueue.add(ErrorLog(code = it.code, channel = channel))
                        TipsUtils.showTips(
                            if (it.severity == 1) {
                                Tips.error("通道${channel + 1} ${it.message}")
                            } else {
                                Tips.warning("通道${channel + 1} ${it.message}")
                            }
                        )
                        LogUtils.error("通道${channel + 1} ${it.message} ${it.code} ${it.severity}")
                    }
                }
            }
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
        }
    }

    private fun collectLogSnapshot(channel: Int, state: ChannelState) {
        try {
            val channelLog = _channelLogList.value[channel]
            channelLog?.let {
                val snapshot = state.toLogSnapshot(it.id)
                channelLogSnapshotQueue.add(snapshot)
            }
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
        }
    }
}