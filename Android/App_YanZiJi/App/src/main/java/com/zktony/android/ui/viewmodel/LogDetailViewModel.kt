package com.zktony.android.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.TipsUtils
import com.zktony.log.LogUtils
import com.zktony.room.entities.Program
import com.zktony.room.repository.LogRepository
import com.zktony.room.repository.LogSnapshotRepository
import com.zktony.room.repository.ProgramRepository
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

    init {
        viewModelScope.launch(Dispatchers.IO) {
            LogUtils.info("LogDetailViewModel id: $id")
        }
    }
}