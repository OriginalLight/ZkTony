package com.zktony.www.ui.log

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.www.room.dao.LogDao
import com.zktony.www.room.entity.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LogViewModel constructor(
    private val LD: LogDao
) : BaseViewModel() {

    private val _logList = MutableStateFlow<List<Log>>(emptyList())
    val logList = _logList.asStateFlow()

    init {
        viewModelScope.launch {
            LD.getAll().collect {
                _logList.value = it
            }
        }
    }

    fun delete(log: Log) {
        viewModelScope.launch {
            LD.delete(log)
        }
    }

}
