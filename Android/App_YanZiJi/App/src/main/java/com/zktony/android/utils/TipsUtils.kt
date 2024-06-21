package com.zktony.android.utils

import com.zktony.android.ui.components.tips.Tips
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object TipsUtils {
    private val _tips = MutableStateFlow<Tips?>(null)
    val tips = _tips.asStateFlow()

    fun showTips(tips: Tips) {
        _tips.value = tips
    }

    fun hideTips() {
        _tips.value = null
    }
}