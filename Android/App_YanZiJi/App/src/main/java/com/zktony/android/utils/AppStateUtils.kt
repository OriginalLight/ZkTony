package com.zktony.android.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author 刘贺贺
 * @date 2023/8/30 13:40
 */
object AppStateUtils {
    private val _argumentsList = MutableStateFlow(listOf(""))
    val argumentsList = _argumentsList.asStateFlow()
}