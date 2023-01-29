package com.zktony.www.ui.container

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.data.repository.PlateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContainerViewModel @Inject constructor(
    private val plateRepository: PlateRepository,
) : BaseViewModel() {

    init {
        viewModelScope.launch {
            plateRepository.init()
        }
    }
}