package com.zktony.android.utils

import com.zktony.android.data.Arguments
import com.zktony.android.data.ChannelState
import com.zktony.android.data.ExperimentalState
import com.zktony.android.data.equateTo
import com.zktony.log.LogUtils
import com.zktony.room.defaultProgram
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

    // Queue
    val channelLogQueue = LinkedTransferQueue<Log>()
    val channelLogSnapshotQueue = LinkedTransferQueue<LogSnapshot>()

    init {
        // 通道状态轮询
        scope.launch {
            var start: Long
            while (true) {
                if (_channelStateList.subscriptionCount.value > 0) {
                    // 获取通道状态
                    start = System.currentTimeMillis()
                    repeat(ProductUtils.getChannelCount()) {
                        if (!isPolling.isLocked) {
                            SerialPortUtils.queryChannelState(it)
                        }
                    }
                    // 间隔时间
                    delay(1000L - (System.currentTimeMillis() - start))
                } else {
                    delay(1000L)
                }
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
        // 未插入状态
        if (state.opt1 == 0 && state.opt2 == 0) {
            transformState(channel, ExperimentalState.NONE)
            return
        }

        // 通道状态
        when (state.runState) {
            0 -> {
                transformState(channel, ExperimentalState.READY)
            }

            1 -> {
                when (state.step) {
                    5 -> {
                        // 开始
                        transformState(channel, ExperimentalState.STARTING)
                    }

                    6 -> {
                        // 充液
                        transformState(channel, ExperimentalState.FILL)
                    }

                    7 -> {
                        // 计时
                        transformState(channel, ExperimentalState.TIMING)
                        collectLogSnapshot(channel, state)
                    }

                    8 -> {
                        // 排液
                        transformState(channel, ExperimentalState.DRAIN)
                    }

                    64 -> {
                        // 结束
                        transformState(channel, ExperimentalState.READY)
                    }

                    else -> {
                        transformState(channel, ExperimentalState.NONE)
                        LogUtils.warn("通道${channel + 1} 未知状态${state.step}")
                    }
                }
            }

            2 -> {
                transformState(channel, ExperimentalState.PAUSE)
            }

            3 -> {
                transformState(channel, ExperimentalState.READY)
            }

            else -> {}
        }
    }

    fun transformState(channel: Int, newState: ExperimentalState) {
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
                        channelLogQueue.add(newLog)
                        setChannelLog(channel, newLog)
                    }
                }

                if (newState == ExperimentalState.ERROR) {
                    // 更新结束时间
                    _channelLogList.value[channel]?.let {
                        val newLog = it.copy(endTime = System.currentTimeMillis(), status = 2)
                        channelLogQueue.add(newLog)
                        setChannelLog(channel, newLog)
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