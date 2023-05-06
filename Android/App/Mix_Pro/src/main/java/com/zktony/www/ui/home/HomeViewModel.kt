package com.zktony.www.ui.home

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.www.common.ext.asyncHex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel : BaseViewModel() {
    fun init() {
        viewModelScope.launch {
            delay(100L)
            asyncHex {
                x(1000f)
            }
        }
    }
}