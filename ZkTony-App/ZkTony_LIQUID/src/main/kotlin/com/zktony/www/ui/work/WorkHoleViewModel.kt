package com.zktony.www.ui.work

import androidx.lifecycle.viewModelScope
import com.zktony.common.base.BaseViewModel
import com.zktony.www.common.extension.volumeDialog
import com.zktony.www.data.local.room.entity.Hole
import com.zktony.www.data.local.room.entity.WorkPlate
import com.zktony.www.data.repository.WorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
            if (_uiState.value.plate?.custom == 0) {
                val hole = _uiState.value.holes.find { it.x == x && it.y == y }
                hole?.let {
                    updateHole(it.copy(checked = !it.checked))
                }
            } else {
                val hole = _uiState.value.holes.find { it.x == x && it.y == y }
                if (hole != null) {
                    if (hole.checked) {
                        updateHole(hole.copy(checked = false))
                    } else {
                        volumeDialog(
                            v1 = hole.v1,
                            v2 = hole.v2,
                            v3 = hole.v3,
                            v4 = hole.v4,
                        ) { v1, v2, v3, v4 ->
                            updateHole(
                                hole.copy(
                                    checked = true,
                                    v1 = v1,
                                    v2 = v2,
                                    v3 = v3,
                                    v4 = v4
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateHole(hole: Hole) {
        viewModelScope.launch {
            workRepository.updateHole(hole)
            delay(500L)
            _uiState.value.plate?.let {
                workRepository.updatePlate(it.copy(count = _uiState.value.holes.count { hole -> hole.checked }))
            }
        }
    }

    fun setVolume(v1: Float, v2: Float, v3: Float, v4: Float) {
        viewModelScope.launch {
            _uiState.value.plate?.let { it ->
                workRepository.updatePlate(it.copy(v1 = v1, v2 = v2, v3 = v3, v4 = v4))
                workRepository.updateHoleBatch(_uiState.value.holes.map { hole ->
                    hole.copy(v1 = v1, v2 = v2, v3 = v3, v4 = v4)
                })
            }
        }
    }

    fun setCustom() {
        viewModelScope.launch {
            _uiState.value.plate?.let {
                workRepository.updatePlate(it.copy(custom = if (it.custom == 0) 1 else 0))
            }
        }
    }
}

data class WorkHoleUiState(
    val plate: WorkPlate? = null,
    val holes: List<Hole> = emptyList()
)