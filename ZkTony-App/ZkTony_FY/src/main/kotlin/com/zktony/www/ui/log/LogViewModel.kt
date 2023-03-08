package com.zktony.www.ui.log

import androidx.lifecycle.viewModelScope
import com.zktony.common.base.BaseViewModel
import com.zktony.www.data.local.room.entity.Log
import com.zktony.www.data.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val logRepository: LogRepository
) : BaseViewModel() {

    private val _logList = MutableStateFlow<List<Log>>(emptyList())
    val logList = _logList.asStateFlow()
    init {
        viewModelScope.launch {
            logRepository.getAll().collect {
                _logList.value = it
            }
        }
    }

    fun delete(log: Log) {
        viewModelScope.launch {
            logRepository.delete(log)
        }
    }

}
