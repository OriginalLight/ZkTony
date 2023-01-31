package com.zktony.www.ui.container

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.room.entity.Plate
import com.zktony.www.common.utils.Logger
import com.zktony.www.data.repository.PlateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlateTwoViewModel @Inject constructor(
    private val plateRepository: PlateRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<Plate?>(null)
    val uiState = _uiState

    init {
        viewModelScope.launch {
            plateRepository.getPlateBySort(1).distinctUntilChanged().collect {
                _uiState.value = it
                Logger.d("PlateViewModel", "init: $it")
            }
        }
    }

    fun setRowAndColumn(row: Int, column: Int) {
        viewModelScope.launch {
            _uiState.value?.let { plateRepository.updatePlate(it.copy(row = row, column = column)) }
        }

    }

    fun move(x: Float, y: Float) {

    }

    fun save(x: Float, y: Float, flag: Int) {
        viewModelScope.launch {
            _uiState.value?.let {
                when (flag) {
                    0 -> {
                        plateRepository.updatePlate(it.copy(x1 = x, y1 = y))
                    }
                    1 -> {
                        plateRepository.updatePlate(it.copy(x2 = x, y2 = y))
                    }
                }
            }
        }
    }
}