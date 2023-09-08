package com.zktony.www.ui.container

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.R
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.core.ext.read
import com.zktony.www.core.ext.calculateCoordinate
import com.zktony.www.core.ext.decideLock
import com.zktony.www.core.ext.execute
import com.zktony.www.data.dao.ContainerDao
import com.zktony.www.data.dao.PointDao
import com.zktony.www.data.entities.Container
import com.zktony.www.data.entities.Point
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ContainerEditViewModel constructor(
    private val CD: ContainerDao,
    private val PD: PointDao,
    private val DS: DataStore<Preferences>,
) : BaseViewModel() {


    private val _uiState = MutableStateFlow(ContainerEditUiState())
    val uiState = _uiState

    fun init(id: Long) {
        viewModelScope.launch {
            launch {
                CD.getById(id).collect {
                    _uiState.value = _uiState.value.copy(container = it)
                }
            }
            launch {
                PD.getBySubId(id).collect {
                    _uiState.value = _uiState.value.copy(list = it)
                }
            }
            launch {
                DS.read("MAX_X_TRIP", 240f).collect {
                    _uiState.value = _uiState.value.copy(maxXTrip = it)
                }
            }
            launch {
                DS.read("MAX_Y_TRIP", 320f).collect {
                    _uiState.value = _uiState.value.copy(maxYTrip = it)
                }
            }
        }
    }

    fun setXY(x: Int, y: Int) {
        viewModelScope.launch {
            _uiState.value.container?.let {
                CD.update(it.copy(x = x, y = y))
                PD.deleteBySubId(it.id)
                val list = mutableListOf<Point>()
                for (i in 0 until x) {
                    for (j in 0 until y) {
                        list.add(Point(subId = it.id, x = i, y = j))
                    }
                }
                PD.insertAll(list)
            }
        }
    }


    fun move(xAxis: Float, yAxis: Float) {
        viewModelScope.launch {
            decideLock {
                yes { PopTip.show(Ext.ctx.getString(R.string.running)) }
                no {
                    execute {
                        step {
                            x = xAxis
                            y = yAxis
                        }
                    }
                }
            }
        }
    }

    fun save(x: Float, y: Float, flag: Int) {
        viewModelScope.launch {
            if (flag == 0) {
                val x0y0 = _uiState.value.list.find { it.x == 0 && it.y == 0 }!!
                PD.update(x0y0.copy(xAxis = x, yAxis = y))
            } else {
                val x1y1 =
                    _uiState.value.list.find { it.x == _uiState.value.container!!.x - 1 && it.y == _uiState.value.container!!.y - 1 }!!
                PD.update(x1y1.copy(xAxis = x, yAxis = y))
            }
            delay(500L)
            PD.updateAll(_uiState.value.list.calculateCoordinate(_uiState.value.container!!))
        }
    }
}

data class ContainerEditUiState(
    val container: Container? = null,
    val list: List<Point> = emptyList(),
    val maxXTrip: Float = 240f,
    val maxYTrip: Float = 320f,
)