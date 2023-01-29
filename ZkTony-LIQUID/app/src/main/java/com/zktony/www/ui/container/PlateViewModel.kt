package com.zktony.www.ui.container

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.room.entity.Plate
import com.zktony.www.common.room.entity.Pore
import com.zktony.www.common.utils.Logger
import com.zktony.www.data.repository.PlateRepository
import com.zktony.www.data.repository.PoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlateViewModel @Inject constructor(
    private val plateRepository: PlateRepository,
    private val poreRepository: PoreRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<Plate?>(null)
    val uiState = _uiState

    fun init(position: Int) {
        viewModelScope.launch {
            plateRepository.getPlateBySort(position).collect {
                _uiState.value = it
                Logger.d("PlateViewModel", "init: $it")
            }
        }
    }

    fun setRow(row: Int) {
        viewModelScope.launch {
            _uiState.value?.let {
                plateRepository.update(it.copy(row = row))
                calculateHoleCoordinate()
            }
        }

    }

    fun setColumn(column: Int) {
        viewModelScope.launch {
            _uiState.value?.let {
                plateRepository.update(it.copy(column = column))
                calculateHoleCoordinate()
            }
        }
    }

    fun move(x: Float, y: Float) {

    }

    fun save(x: Float, y: Float, flag: Int) {
        viewModelScope.launch {
            _uiState.value?.let {
                when (flag) {
                    0 -> {
                        plateRepository.update(it.copy(x1 = x, y1 = y))
                    }
                    1 -> {
                        plateRepository.update(it.copy(x2 = x, y2 = y))
                    }
                }
            }
            calculateHoleCoordinate()
        }
    }

    /**
     * 计算孔位坐标
     */
    private fun calculateHoleCoordinate() {
        viewModelScope.launch {
            delay(1000L)
            val x = _uiState.value?.x2!! - _uiState.value?.x1!!
            val y = _uiState.value?.y2!! - _uiState.value?.y1!!
            val xSpace = x / (_uiState.value?.column!! - 1)
            val ySpace = y / (_uiState.value?.row!! - 1)
            val poreList = mutableListOf<Pore>()
            for (i in 0 until _uiState.value?.row!!) {
                for (j in 0 until _uiState.value?.column!!) {
                    poreList.add(
                        Pore(
                            plateId = _uiState.value?.id!!,
                            x = j,
                            y = i,
                            xAxis = _uiState.value?.x1!! + xSpace * j,
                            yAxis = _uiState.value?.y1!! + ySpace * i
                        )
                    )
                }
            }
            poreRepository.deleteByPlateId(_uiState.value?.id!!)
            poreRepository.insertBatch(poreList)
        }
    }
}