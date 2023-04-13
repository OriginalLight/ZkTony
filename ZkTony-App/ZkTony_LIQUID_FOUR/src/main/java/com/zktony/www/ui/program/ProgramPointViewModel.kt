package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.www.common.ext.volumeDialog
import com.zktony.www.room.dao.HoleDao
import com.zktony.www.room.dao.PlateDao
import com.zktony.www.room.dao.PointDao
import com.zktony.www.room.entity.Hole
import com.zktony.www.room.entity.Plate
import com.zktony.www.room.entity.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ProgramPointViewModel constructor(
    private val dao: PointDao
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ProgramHoleUiState())
    val uiState = _uiState.asStateFlow()

    fun init(id: Long, index: Int) {
        viewModelScope.launch {
            dao.getBySudIdByIndex(id, index).collect {
                _uiState.value = _uiState.value.copy(list = it)
            }
        }
    }

    fun selectAll() {
        viewModelScope.launch {
            dao.updateAll(_uiState.value.list.map {
                it.copy(enable = true)
            })
        }
    }

//    fun select(x: Int, y: Int) {
//        viewModelScope.launch {
//            if (_uiState.value.custom) {
//                val hole = _uiState.value.list.find { it.x == x && it.y == y }!!
//                dao.update(hole.copy(enable = !hole.enable))
//            } else {
//                val hole = _uiState.value.list.find { it.x == x && it.y == y }!!
//                if (hole.enable) {
//                    dao.update(hole.copy(enable = false))
//                } else {
//                    volumeDialog(
//                        v1 = hole.v1,
//                        v2 = hole.v2,
//                        v3 = hole.v3,
//                        v4 = hole.v4,
//                    ) { v1, v2, v3, v4 ->
//                        viewModelScope.launch {
//                            holeDao.update(
//                                hole.copy(
//                                    enable = true,
//                                    v1 = v1,
//                                    v2 = v2,
//                                    v3 = v3,
//                                    v4 = v4
//                                )
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    fun setVolume(v1: Float, v2: Float, v3: Float, v4: Float) {
//        viewModelScope.launch {
//            _uiState.value.plate?.let {
//                holeDao.updateAll(_uiState.value.holes.map { hole ->
//                    hole.copy(v1 = v1, v2 = v2, v3 = v3, v4 = v4)
//                })
//            }
//        }
//    }
//
//    fun setCustom() {
//        viewModelScope.launch {
//            _uiState.value.plate?.let {
//                dao.update(it.copy(custom = if (it.custom == 0) 1 else 0))
//            }
//        }
//    }
}

data class ProgramHoleUiState(
    val list: List<Point> = emptyList(),
    val custom: Boolean = false,
)