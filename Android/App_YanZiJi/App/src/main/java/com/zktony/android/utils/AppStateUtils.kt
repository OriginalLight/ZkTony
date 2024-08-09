package com.zktony.android.utils

import com.zktony.android.data.Arguments
import com.zktony.android.data.ChannelState
import com.zktony.android.data.ExperimentalState
import com.zktony.android.data.LedState
import com.zktony.android.data.SoundType
import com.zktony.android.data.ZktyError
import com.zktony.android.data.equateTo
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

    // 心跳
    private val heartbeatList = MutableList(ProductUtils.MAX_CHANNEL_COUNT) { 0L }

    // 轮询
    private val pollingLock = Mutex(false)

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
                    if (!pollingLock.isLocked) {
                        refreshHeartbeat(it, SerialPortUtils.queryChannelState(it))
                    }
                }
                refreshLedState()
                // 间隔时间
                delay(1000L - (System.currentTimeMillis() - start))
            }
        }
    }

    // 获取轮询锁
    fun getPollingLock(): Mutex {
        return pollingLock
    }

    // 获取心跳
    fun getHeartbeat(channel: Int): Long {
        return heartbeatList[channel]
    }

    // state set and get
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

    fun getChannelState(channel: Int): ChannelState {
        return _channelStateList.value[channel]
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

    fun setExperimentalState(channel: Int, state: ExperimentalState) {
        _experimentalStateList.value =
            _experimentalStateList.value.mapIndexed { index, experimentalState ->
                if (index == channel) {
                    state
                } else {
                    experimentalState
                }
            }
    }

    fun getExperimentalState(channel: Int): ExperimentalState {
        return _experimentalStateList.value[channel]
    }

    // 根据通道状态获取实验状态
    fun setChannelStateQueryHook(channel: Int, state: ChannelState) {
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

    // 实验状态变更
    fun transformExperimentalState(channel: Int, newState: ExperimentalState) {
        try {
            val oldState = getExperimentalState(channel)
            if (oldState == newState) {
                return
            }
            setExperimentalState(channel, newState)
            // do something when state changed from oldState to newState
            if (!oldState.equateTo(newState)) {
                if (newState == ExperimentalState.READY) {
                    // 更新结束时间
                    getChannelLog(channel)?.let {
                        val newLog = it.copy(endTime = System.currentTimeMillis())
                        // add log to queue to database
                        channelLogQueue.add(newLog)
                        // update log
                        setChannelLog(channel, newLog)
                    }
                }

                if (newState == ExperimentalState.ERROR) {
                    // 更新结束时间
                    getChannelLog(channel)?.let {
                        val newLog = it.copy(endTime = System.currentTimeMillis(), status = 2)
                        // add log to queue to database
                        channelLogQueue.add(newLog)
                        // update log
                        setChannelLog(channel, newLog)
                    }
                    // 声音
                    SoundUtils.play(SoundType.ERROR)
                }
            }
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
        }
    }

    // 收集错误日志
    private fun collectErrorLog(channel: Int, state: ChannelState) {
        try {
            val newError = state.errorInfo
            val oldError = getChannelState(channel).errorInfo
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

    // 收集日志快照
    private fun collectLogSnapshot(channel: Int, state: ChannelState) {
        try {
            val channelLog = getChannelLog(channel)
            channelLog?.let {
                val snapshot = state.toLogSnapshot(it.id)
                channelLogSnapshotQueue.add(snapshot)
            }
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
        }
    }

    // 刷新LED状态
    private fun refreshLedState() {
        try {
            // 错误红色
            if (_experimentalStateList.value.any { it == ExperimentalState.ERROR }) {
                // 有错误
                LedUtils.transform(LedState.RED)
                return
            }
            // 警告黄色
            if (_channelStateList.value.any { it.errorInfo > 0L }) {
                // 有警告
                LedUtils.transform(LedState.YELLOW_FLASH)
                return
            }

            // 所有通道都是就绪状态
            if (_experimentalStateList.value.all { it == ExperimentalState.READY }) {
                // 就绪
                LedUtils.transform(LedState.GREEN)
            } else {
                // 运行或者未插入
                LedUtils.transform(LedState.YELLOW)
            }
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
        }
    }

    // 刷新心跳
    private fun refreshHeartbeat(channel: Int, heart: Boolean) {
        try {
            heartbeatList[channel] = if (heart) {
                0L
            } else {
                heartbeatList[channel] + 1L
            }

            if (heartbeatList[channel] == 10L + channel) {
                transformExperimentalState(channel, ExperimentalState.ERROR)
                val error = ZktyError.ERROR_18
                channelErrorLogQueue.add(ErrorLog(code = error.code, channel = channel))
                TipsUtils.showTips(Tips.error("通道${channel + 1} ${error.message}"))
                LogUtils.error("通道${channel + 1} ${error.message} ${error.code} ${error.severity}")
            }
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
        }
    }
}