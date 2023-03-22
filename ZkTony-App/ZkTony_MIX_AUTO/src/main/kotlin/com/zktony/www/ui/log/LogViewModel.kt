package com.zktony.www.ui.log

import androidx.lifecycle.viewModelScope
import com.zktony.common.base.BaseViewModel
import com.zktony.www.data.local.room.dao.LogDao
import com.zktony.www.data.local.room.entity.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LogViewModel constructor(
    private val logDao: LogDao
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<List<Log>>(emptyList())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            logDao.getAll().collect {
                _uiState.value = it
            }
        }
    }

    fun delete(log: Log) {
        viewModelScope.launch {
            logDao.delete(log)
        }
    }

}
