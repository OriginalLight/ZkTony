package com.zktony.android.utils

import com.zktony.android.data.Arguments
import com.zktony.android.data.ChannelState
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

    // stateFlow
    val argumentsList = _argumentsList.asStateFlow()
    val channelStateList = _channelStateList.asStateFlow()

    init {
        // 通道状态轮询
        scope.launch {
            while (true) {
                if (_channelStateList.subscriptionCount.value > 0) {
                    // 获取通道状态
                    val start = System.currentTimeMillis()
                    repeat(ProductUtils.getChannelCount()) {
                        if (!isPolling.isLocked) {
                            SerialPortUtils.queryChannelState(it)
                        }
                    }
                    val end = System.currentTimeMillis()
                    // 间隔时间
                    delay(1000L - (end - start))
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
}