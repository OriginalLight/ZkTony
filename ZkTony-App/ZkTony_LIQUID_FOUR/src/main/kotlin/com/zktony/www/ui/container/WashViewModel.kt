package com.zktony.www.ui.container

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseViewModel
import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.SerialManager
import com.zktony.www.data.local.room.dao.ContainerDao
import com.zktony.www.data.local.room.entity.Container
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WashViewModel @Inject constructor(
    private val dao: ContainerDao
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<Container?>(null)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAll().distinctUntilChanged().collect {
                if (it.isNotEmpty()) {
                    _uiState.value = it[0]
                }
            }
        }
    }

    fun move(x: Float, y: Float) {
        val serial = SerialManager.instance
        if (serial.lock.value || serial.pause.value) {
            PopTip.show("机器正在运行中")
            return
        }
        val m = ExecutionManager.instance
        m.executor(m.generator(x = x, y = y))
    }

    fun save(x: Float, y: Float) {
        viewModelScope.launch {
            _uiState.value?.let {
                dao.update(it.copy(wasteX = x, wasteY = y))
            }
        }
    }
}