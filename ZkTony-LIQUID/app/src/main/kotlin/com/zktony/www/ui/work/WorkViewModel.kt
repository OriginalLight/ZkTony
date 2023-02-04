package com.zktony.www.ui.work

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.room.entity.Work
import com.zktony.www.data.repository.WorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkViewModel @Inject constructor(
    private val workRepository: WorkRepository
) : BaseViewModel() {

    private val _workList = MutableStateFlow<List<Work>>(emptyList())
    val workList = _workList.asStateFlow()

    init {
        viewModelScope.launch {
            workRepository.getAllWork().distinctUntilChanged().collect {
                _workList.value = it
            }
        }
    }

    fun delete(work: Work) {
        viewModelScope.launch {
            workRepository.deleteWork(work)
        }
    }

    fun insert(name: String) {
        viewModelScope.launch {
            val work = workList.value.find { it.name == name }
            if (work != null) {
                PopTip.show("已存在相同名称的程序")
            } else {
                workRepository.insertWork(Work(name = name))
            }
        }
    }
}