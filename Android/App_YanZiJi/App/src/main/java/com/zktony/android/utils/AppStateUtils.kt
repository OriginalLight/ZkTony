package com.zktony.android.utils

import com.zktony.android.data.Arguments
import com.zktony.android.data.ChannelState
import com.zktony.android.data.ExperimentalState
import com.zktony.log.LogUtils
import com.zktony.room.defaults.defaultProgram
import com.zktony.room.entities.Program
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

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
    private val _channelStateList =
        MutableStateFlow(List(ProductUtils.MAX_CHANNEL_COUNT) { ChannelState() })
    private val _channelProgramList =
        MutableStateFlow(List(ProductUtils.MAX_CHANNEL_COUNT) { defaultProgram() })
    private val _experimentalStateList =
        MutableStateFlow(List(ProductUtils.MAX_CHANNEL_COUNT) { ExperimentalState.NONE })

    // stateFlow
    val argumentsList = _argumentsList.asStateFlow()
    val channelStateList = _channelStateList.asStateFlow()
    val channelProgramList = _channelProgramList.asStateFlow()
    val experimentalStateList = _experimentalStateList.asStateFlow()

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
    fun setArgumentsList(argumentsList: List<Arguments>) {
        _argumentsList.value = argumentsList
    }

    fun getArgumentList(): List<Arguments> {
        return _argumentsList.value
    }

    fun setChannelStateList(channelStateList: List<ChannelState>) {
        _channelStateList.value = channelStateList
    }

    fun setChannelProgramList(channelProgramList: List<Program>) {
        _channelProgramList.value = channelProgramList
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
                transformState(
                    channel, when (state.step) {
                        5 -> ExperimentalState.STARTING
                        6 -> ExperimentalState.FILL
                        7 -> ExperimentalState.TIMING
                        8 -> ExperimentalState.DRAIN
                        64 -> ExperimentalState.READY
                        else -> ExperimentalState.NONE
                    }
                )
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
            when (oldState) {
                ExperimentalState.NONE -> {}
                ExperimentalState.READY -> {}
                ExperimentalState.PAUSE -> {}
                ExperimentalState.STARTING -> {}
                ExperimentalState.TIMING -> {}
                ExperimentalState.FILL -> {}
                ExperimentalState.DRAIN -> {}
                ExperimentalState.ERROR -> {}
            }
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
        }
    }
}