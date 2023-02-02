package com.zktony.www.ui.work

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.utils.Logger
import com.zktony.www.data.repository.WorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkFlowViewModel @Inject constructor(
    private val workRepository: WorkRepository
) : BaseViewModel() {


    fun init(id: String) {
        viewModelScope.launch {
            workRepository.getWorkById(id).distinctUntilChanged().collect {
                Logger.d(msg = "WorkFlowViewModel: ${it.plates}")
            }
        }
    }
}