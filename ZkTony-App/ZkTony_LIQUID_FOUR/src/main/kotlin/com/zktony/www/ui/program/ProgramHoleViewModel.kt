package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.zktony.common.base.BaseViewModel
import com.zktony.www.common.extension.volumeDialog
import com.zktony.www.data.local.room.dao.HoleDao
import com.zktony.www.data.local.room.dao.PlateDao
import com.zktony.www.data.local.room.entity.Hole
import com.zktony.www.data.local.room.entity.Plate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgramHoleViewModel @Inject constructor(
    private val dao: PlateDao,
    private val holeDao: HoleDao,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ProgramHoleUiState())
    val uiState = _uiState.asStateFlow()

    fun init(id: Long) {
        viewModelScope.launch {
            launch {
                dao.getById(id).distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(plate = it)
                }
            }
            launch {
                holeDao.getBySubId(id).distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(holes = it)
                }
            }
        }
    }

    fun selectAll() {
        viewModelScope.launch {
            holeDao.updateAll(_uiState.value.holes.map {
                it.copy(enable = true)
            })
        }
    }

    fun select(x: Int, y: Int) {
        viewModelScope.launch {
            if (_uiState.value.plate?.custom == 0) {
                val hole = _uiState.value.holes.find { it.x == x && it.y == y }!!
                holeDao.update(hole.copy(enable = !hole.enable))
            } else {
                val hole = _uiState.value.holes.find { it.x == x && it.y == y }
                if (hole != null) {
                    if (hole.enable) {
                        holeDao.update(hole.copy(enable = false))
                    } else {
                        volumeDialog(
                            v1 = hole.v1,
                            v2 = hole.v2,
                            v3 = hole.v3,
                            v4 = hole.v4,
                        ) { v1, v2, v3, v4 ->
                            launch {
                                holeDao.update(
                                    hole.copy(
                                        enable = true,
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
    }

    fun setVolume(v1: Float, v2: Float, v3: Float, v4: Float) {
        viewModelScope.launch {
            _uiState.value.plate?.let {
                holeDao.updateAll(_uiState.value.holes.map { hole ->
                    hole.copy(v1 = v1, v2 = v2, v3 = v3, v4 = v4)
                })
            }
        }
    }

    fun setCustom() {
        viewModelScope.launch {
            _uiState.value.plate?.let {
                dao.update(it.copy(custom = if (it.custom == 0) 1 else 0))
            }
        }
    }
}

data class ProgramHoleUiState(
    val plate: Plate? = null,
    val holes: List<Hole> = emptyList(),
)