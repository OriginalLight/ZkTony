package com.zktony.www.ui.log

import androidx.lifecycle.viewModelScope
import com.zktony.common.base.BaseViewModel
import com.zktony.www.data.local.dao.LogDao
import com.zktony.www.data.local.entity.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LogViewModel constructor(
    private val dao: LogDao
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<List<Log>>(emptyList())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAll().collect {
                _uiState.value = it
            }
        }
    }

    fun delete(log: Log) {
        viewModelScope.launch {
            dao.delete(log)
        }
    }

}
