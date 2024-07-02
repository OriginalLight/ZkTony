package com.zktony.android.utils

import com.zktony.android.data.Arguments
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author 刘贺贺
 * @date 2023/8/30 13:40
 */
object AppStateUtils {
    // mutableStateFlow
    private val _argumentsList = MutableStateFlow(List(4) { Arguments() })

    // stateFlow
    val argumentsList = _argumentsList.asStateFlow()

    fun setArgumentsList(argumentsList: List<Arguments>) {
        _argumentsList.value = argumentsList
    }
}