package com.zktony.www.ui.work

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseViewModel
import com.zktony.www.data.local.room.entity.Work
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkViewModel @Inject constructor(
) : BaseViewModel() {

    private val _workList = MutableStateFlow<List<Work>>(emptyList())
    val workList = _workList.asStateFlow()

    init {
        viewModelScope.launch {

        }
    }

    fun delete(work: Work) {
        viewModelScope.launch {

        }
    }

    fun insert(name: String) {
        viewModelScope.launch {
            val work = workList.value.find { it.name == name }
            if (work != null) {
                PopTip.show("已存在相同名称的程序")
            } else {
            }
        }
    }
}