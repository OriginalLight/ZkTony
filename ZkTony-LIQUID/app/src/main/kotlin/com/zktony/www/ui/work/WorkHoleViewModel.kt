package com.zktony.www.ui.work

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.room.entity.Hole
import com.zktony.www.common.room.entity.WorkPlate
import com.zktony.www.data.repository.WorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkHoleViewModel @Inject constructor(
    private val workRepository: WorkRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(WorkHoleUiState())
    val uiState = _uiState.asStateFlow()

    fun init(id: String) {
        viewModelScope.launch {
            launch {
                workRepository.getWorkPlateById(id).distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(plate = it)
                }
            }
            launch {
                workRepository.getHoleByPlateId(id).distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(holes = it)
                }
            }
        }
    }

    fun selectAll() {
        viewModelScope.launch {
            _uiState.value.plate?.let {
                workRepository.updatePlate(it.copy(count = _uiState.value.holes.size))
            }
            workRepository.updateHoleBatch(_uiState.value.holes.map {
                it.copy(checked = true)
            })
        }
    }

    fun select(x: Int, y: Int) {
        viewModelScope.launch {
            _uiState.value.plate?.let {
                workRepository.updatePlate(it.copy(count = it.count - 1))
            }
            val hole = _uiState.value.holes.find { it.x == x && it.y == y }
            hole?.let {
                workRepository.updateHole(it.copy(checked = !it.checked))
            }
        }
    }
}

data class WorkHoleUiState(
    val plate: WorkPlate? = null,
    val holes: List<Hole> = emptyList()
)