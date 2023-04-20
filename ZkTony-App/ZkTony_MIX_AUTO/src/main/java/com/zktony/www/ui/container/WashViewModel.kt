package com.zktony.www.ui.container

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.SerialManager
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.entity.Container
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WashViewModel constructor(
    private val CD: ContainerDao,
    private val SM: SerialManager,
    private val EM: ExecutionManager,
) : BaseViewModel() {


    private val _uiState = MutableStateFlow<Container?>(null)
    val uiState = _uiState.asStateFlow()

    fun init(id: Long) {
        viewModelScope.launch {
            CD.getById(id).collect {
                _uiState.value = it
            }
        }
    }

    fun move(x: Float) {
        if (SM.lock.value || SM.pause.value) {
            PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.running))
            return
        }
        EM.actuator(EM.builder(y = x))
    }

    fun save(x: Float) {
        viewModelScope.launch {
            _uiState.value?.let {
                CD.update(it.copy(axis = x))
            }
        }
    }
}