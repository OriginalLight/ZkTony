package com.zktony.www.ui.work

import androidx.lifecycle.viewModelScope
import com.zktony.common.base.BaseViewModel
import com.zktony.www.data.local.room.entity.Hole
import com.zktony.www.data.local.room.entity.Plate
import com.zktony.www.data.local.room.entity.Work
import com.zktony.www.data.local.room.entity.WorkPlate
import com.zktony.www.data.repository.PlateRepository
import com.zktony.www.data.repository.WorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkPlateViewModel @Inject constructor(
    private val workRepository: WorkRepository,
    private val plateRepository: PlateRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(WorkPlateUiState())
    val uiState = _uiState.asStateFlow()


    fun init(id: String) {
        viewModelScope.launch {
            launch {
                plateRepository.getAllPlate().distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(plates = it)
                }
            }
            launch {
                workRepository.getWorkById(id).distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(work = it)
                }
            }
            launch {
                workRepository.getWorkPlateByWorkId(id).distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(workPlates = it)
                }
            }
            launch {
                workRepository.getHoleByWorkId(id).distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(holes = it)
                }
            }
        }
    }

    fun checkPlate(sort: Int) {
        viewModelScope.launch {
            _uiState.value.work?.let {
                val plate = _uiState.value.workPlates.find { plate -> plate.sort == sort }
                if (plate != null) {
                    workRepository.removePlate(plate)
                } else {
                    val p = plateRepository.getPlateBySort(sort).first()
                    workRepository.addPlate(p, it.id)
                }
            }
        }
    }
}

data class WorkPlateUiState(
    val work: Work? = null,
    val plates: List<Plate> = emptyList(),
    val workPlates: List<WorkPlate> = emptyList(),
    val holes: List<Hole> = emptyList()
)