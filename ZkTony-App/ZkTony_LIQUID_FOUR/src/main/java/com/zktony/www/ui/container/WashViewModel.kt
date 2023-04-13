package com.zktony.www.ui.container

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.SerialManager
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.entity.Container
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WashViewModel constructor(
    private val dao: ContainerDao,
    private val serialManager: SerialManager,
    private val executionManager: ExecutionManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<Container?>(null)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getByType(0).collect {
                if (it.isNotEmpty()) {
                    _uiState.value = it[0]
                }
            }
        }
    }

    fun move(x: Float, y: Float) {
        if (serialManager.lock.value || serialManager.pause.value) {
            PopTip.show("机器正在运行中")
            return
        }
        executionManager.executor(executionManager.generator(x = x, y = y))
    }

    fun save(x: Float, y: Float) {
        viewModelScope.launch {
            _uiState.value?.let {
                dao.update(it.copy(xAxis = x, yAxis = y))
            }
        }
    }
}