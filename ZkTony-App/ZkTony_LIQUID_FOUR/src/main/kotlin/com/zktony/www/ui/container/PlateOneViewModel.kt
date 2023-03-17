package com.zktony.www.ui.container

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseViewModel
import com.zktony.common.utils.Snowflake
import com.zktony.www.common.extension.calculateCoordinate
import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.SerialManager
import com.zktony.www.data.local.room.dao.HoleDao
import com.zktony.www.data.local.room.dao.PlateDao
import com.zktony.www.data.local.room.entity.Hole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlateOneViewModel @Inject constructor(
    private val dao: PlateDao,
    private val holeDao: HoleDao,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(PlateUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                dao.getById(1L).distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(plate = it)
                }
            }
            launch {
                holeDao.getBySubId(1L).distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(holes = it)
                }
            }
        }
    }

    fun setXY(x: Int, y: Int) {
        viewModelScope.launch {
            _uiState.value.plate?.let {
                dao.update(it.copy(x = x, y = y))
                if (_uiState.value.holes.size != x * y) {
                    holeDao.deleteBySubId(it.id)
                    val snowflake = Snowflake(2)
                    val holes = mutableListOf<Hole>()
                    for (i in 0 until  x) {
                        for (j in 0 until y) {
                            holes.add(Hole(id = snowflake.nextId(), subId = it.id, x = i, y = j))
                        }
                    }
                    holeDao.insertAll(holes)
                }
            }
        }
    }

    fun move(x: Float, y: Float) {
        val serial = SerialManager.instance
        if (serial.lock.value || serial.pause.value) {
            PopTip.show("机器正在运行中")
            return
        }
        val manager = ExecutionManager.instance
        manager.executor(manager.generator(x = x, y = y))
    }

    fun save(x: Float, y: Float, flag: Int) {
        viewModelScope.launch {
            if (flag == 0) {
                val x0y0 = _uiState.value.holes.find { it.x == 0 && it.y == 0 }!!
                holeDao.update(x0y0.copy(xAxis = x, yAxis = y))
            } else {
                val x1y1 = _uiState.value.holes.find { it.x == _uiState.value.plate!!.x - 1 && it.y == _uiState.value.plate!!.y - 1 }!!
                holeDao.update(x1y1.copy(xAxis = x, yAxis = y))
            }
            delay(500L)
            holeDao.updateAll(_uiState.value.holes.calculateCoordinate(_uiState.value.plate!!))
        }
    }

}