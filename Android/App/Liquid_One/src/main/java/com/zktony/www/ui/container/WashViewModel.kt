package com.zktony.www.ui.container

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.www.common.ext.execute
import com.zktony.www.manager.SerialManager
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.entity.Container
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WashViewModel constructor(
    private val CD: ContainerDao,
    private val SM: SerialManager,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<Container?>(null)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            CD.getByType(0).collect {
                if (it.isNotEmpty()) {
                    _uiState.value = it[0]
                }
            }
        }
    }

    fun move(xAxis: Float, yAxis: Float) {
        if (SM.lock.value || SM.pause.value) {
            PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.running))
            return
        }
        execute {
            step {
                x = xAxis
                y = yAxis
            }
        }
    }

    fun save(x: Float, y: Float) {
        viewModelScope.launch {
            _uiState.value?.let {
                CD.update(it.copy(xAxis = x, yAxis = y))
            }
        }
    }
}