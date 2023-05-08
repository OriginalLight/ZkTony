package com.zktony.www.ui.container

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.datastore.ext.read
import com.zktony.www.common.ext.decideLock
import com.zktony.www.common.ext.execute
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.dao.PointDao
import com.zktony.www.room.entity.Container
import com.zktony.www.room.entity.Point
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
                DS.read("MAX_Y_TRIP", 200f).collect {
                    _uiState.value = _uiState.value.copy(maxYTrip = it)
                }
            }

        }
    }


    fun reSize(size: Int) {
        viewModelScope.launch {
            _uiState.value.container?.let {
                if (it.size != size) {
                    CD.update(it.copy(size = size))
                    PD.deleteBySubId(it.id)
                    val list = mutableListOf<Point>()
                    for (i in 0 until size) {
                        list.add(Point(subId = it.id, index = i))
                    }
                    PD.insertAll(list)
                }
            }
        }
    }

    fun setPointPosition(index: Int, axis: Float, waste: Float) {
        viewModelScope.launch {
            val point = _uiState.value.list.find { it.index == index }
            point?.let {
                PD.update(it.copy(axis = axis, waste = waste))
                delay(500L)
                calculateAxis()
                delay(500L)
                calculateWaste()
            }
        }
    }

    fun move(yAxis: Float) {
        viewModelScope.launch {
            decideLock {
                yes {
                    PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.running))
                }
                no {
                    execute {
                        step {
                            y = yAxis
                        }
                    }
                }
            }
        }
    }

    /**
     * 数组计算算法
     */
    private suspend fun calculateAxis(index: Int = 0) {
        val list = _uiState.value.list
        val h1 = list[index]
        var h2 = list[index + 1]
        for (i in index + 1 until list.size) {
            if (list[i].axis != 0f) {
                h2 = list[i]
                break
            }
        }
        if (h2.axis == 0f && h2.index == index + 1) {
            return
        } else {
            val minIndex = h1.index
            val maxIndex = h2.index
            if (maxIndex - minIndex >= 2) {
                val minAxis = h1.axis
                val maxAxis = h2.axis
                val distance = (maxAxis - minAxis) / (maxIndex - minIndex)
                val pointList = mutableListOf<Point>()
                for (i in minIndex + 1 until maxIndex) {
                    val point = list.find { it.index == i && it.axis == 0f }
                    point?.let {
                        pointList.add(it.copy(axis = minAxis + (i - minIndex) * distance))
                    }
                }
                PD.updateAll(pointList)
            }
            if (maxIndex < list.size - 2) {
                calculateAxis(maxIndex)
            }
        }
    }

    private suspend fun calculateWaste(index: Int = 0) {
        val list = _uiState.value.list
        val h1 = list[index]
        var h2 = list[index + 1]
        for (i in index + 1 until list.size) {
            if (list[i].waste != 0f) {
                h2 = list[i]
                break
            }
        }
        if (h2.waste == 0f && h2.index == index + 1) {
            return
        } else {
            val minIndex = h1.index
            val maxIndex = h2.index
            if (maxIndex - minIndex >= 2) {
                val minAxis = h1.waste
                val maxAxis = h2.waste
                val distance = (maxAxis - minAxis) / (maxIndex - minIndex)
                val pointList = mutableListOf<Point>()
                for (i in minIndex + 1 until maxIndex) {
                    val point = list.find { it.index == i && it.waste == 0f }
                    point?.let {
                        pointList.add(it.copy(waste = minAxis + (i - minIndex) * distance))
                    }
                }
                PD.updateAll(pointList)
            }
            if (maxIndex < list.size - 2) {
                calculateWaste(maxIndex)
            }
        }
    }
}

data class ContainerEditUiState(
    val container: Container? = null,
    val list: List<Point> = emptyList(),
    val maxYTrip: Float = 200f,
)