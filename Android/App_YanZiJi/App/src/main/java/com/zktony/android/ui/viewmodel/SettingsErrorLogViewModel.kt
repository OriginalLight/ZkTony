package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.room.repository.ErrorLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsErrorLogViewModel @Inject constructor(
    private val errorLogRepository: ErrorLogRepository
) : ViewModel() {

    val entities = Pager(PagingConfig(pageSize = 20, initialLoadSize = 40)) {
        errorLogRepository.getByPage()
    }.flow.cachedIn(viewModelScope)
}