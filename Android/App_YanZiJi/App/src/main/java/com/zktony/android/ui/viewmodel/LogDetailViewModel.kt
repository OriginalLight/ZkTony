package com.zktony.android.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.room.entities.Log
import com.zktony.room.entities.LogSnapshot
import com.zktony.room.repository.LogRepository
import com.zktony.room.repository.LogSnapshotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val logRepository: LogRepository,
    private val logSnapshotRepository: LogSnapshotRepository
) : ViewModel() {

    private val id: Long = checkNotNull(savedStateHandle["id"])
    private val _navObj = MutableStateFlow<Log?>(null)
    private val _entities = MutableStateFlow<List<LogSnapshot>>(emptyList())

    val navObj = _navObj.asStateFlow()
    val entities = _entities.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _navObj.value = logRepository.getById(id)
            logSnapshotRepository.getBySubId(id).collect {
                _entities.value = it
            }
        }
    }
}