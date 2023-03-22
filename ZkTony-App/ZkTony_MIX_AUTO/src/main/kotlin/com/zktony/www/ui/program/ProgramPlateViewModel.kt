package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.zktony.common.base.BaseViewModel
import com.zktony.www.data.local.room.dao.HoleDao
import com.zktony.www.data.local.room.dao.PlateDao
import com.zktony.www.data.local.room.entity.Hole
import com.zktony.www.data.local.room.entity.Plate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProgramPlateViewModel constructor(
    private val dao: PlateDao,
    private val holeDao: HoleDao,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(WorkPlateUiState())
    val uiState = _uiState.asStateFlow()


    fun init(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(id = id)
            launch {
                dao.getBySubId(id).collect {
                    if (it.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(plate = it[0])
                        launch {
                            holeDao.getBySubId(it[0].id).collect { holeList ->
                                _uiState.value = _uiState.value.copy(holeList = holeList)
                            }
                        }
                    }
                }
            }
        }
    }

    fun selectHole(index : Int) {
        viewModelScope.launch {
            _uiState.value.holeList.find { it.x == index }?.let {
                holeDao.update(it.copy(enable = !it.enable))
            }

        }
    }

    fun selectAll() {
        viewModelScope.launch {
            holeDao.updateAll(_uiState.value.holeList.map { it.copy(enable = true) })
        }
    }

    fun updateVolume(v1: Float, v2: Float) {
        viewModelScope.launch {
            holeDao.updateAll(_uiState.value.holeList.map { it.copy(
                v1 = v1,
                v2 = v2,
            ) })
        }
    }
}

data class WorkPlateUiState(
    val id: Long = 0L,
    val plate: Plate? = null,
    val holeList: List<Hole> = emptyList(),
)