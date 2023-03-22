package com.zktony.www.ui.container

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseViewModel
import com.zktony.common.utils.Snowflake
import com.zktony.www.data.local.room.dao.ContainerDao
import com.zktony.www.data.local.room.dao.HoleDao
import com.zktony.www.data.local.room.dao.PlateDao
import com.zktony.www.data.local.room.entity.Container
import com.zktony.www.data.local.room.entity.Hole
import com.zktony.www.data.local.room.entity.Plate
import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.SerialManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PlateViewModel constructor(
    private val containerDao: ContainerDao,
    private val plateDao: PlateDao,
    private val holeDao: HoleDao,
    private val serialManager: SerialManager,
    private val executionManager: ExecutionManager,
) : BaseViewModel() {


    private val _uiState = MutableStateFlow(PlateUiState())
    val uiState = _uiState

    init {
        viewModelScope.launch {
            launch {
                containerDao.getById(1L).collect {
                    _uiState.value = _uiState.value.copy(container = it)
                }
            }
            launch {
                plateDao.getById(1L).collect {
                    _uiState.value = _uiState.value.copy(plate = it)
                }
            }
            launch {
                holeDao.getBySubId(1L).collect {
                    _uiState.value = _uiState.value.copy(holeList = it)
                }
            }
        }
    }

    fun reSize(size: Int) {
        viewModelScope.launch {
            _uiState.value.plate?.let {
                if (it.x != size) {
                    plateDao.update(it.copy(x = size))
                    holeDao.deleteBySubId(it.id)
                    val holeList = mutableListOf<Hole>()
                    val snowflake = Snowflake(1)
                    for (i in 0 until size) {
                        holeList.add(
                            Hole(
                                id = snowflake.nextId(),
                                subId = it.id,
                                x = i,
                            )
                        )
                    }
                    holeDao.insertAll(holeList)
                }
            }
        }
    }

    fun setHolePosition(index: Int, axis: Float) {
        viewModelScope.launch {
            val hole = _uiState.value.holeList.find { it.x == index }
            hole?.let {
                holeDao.update(it.copy(xAxis = axis))
                delay(500L)
                calculateCoordinate()
            }
        }
    }

    fun moveZ(z: Float) {
        if (serialManager.lock.value || serialManager.pause.value) {
            PopTip.show("机器正在运行中")
            return
        }
        val hole = _uiState.value.holeList.find { it.x == 0 }
        executionManager.executor(
            executionManager.generator(x = hole?.xAxis ?: 0f),
            executionManager.generator(x = hole?.xAxis ?: 0f, z = z)
        )
    }

    fun moveX(x: Float) {
        if (serialManager.lock.value || serialManager.pause.value) {
            PopTip.show("机器正在运行中")
            return
        }
        executionManager.executor(executionManager.generator(x = x))
    }

    fun setBottom(z: Float) {
        viewModelScope.launch {
            containerDao.update(_uiState.value.container?.copy(bottom = z) ?: return@launch)
        }
    }

    fun setTop(z: Float) {
        viewModelScope.launch {
            containerDao.update(_uiState.value.container?.copy(top = z) ?: return@launch)
        }
    }


    private suspend fun calculateCoordinate() {
        val holeList = _uiState.value.holeList
        val min = holeList.filter { it.xAxis != 0f }.minByOrNull { it.x }
        val max = holeList.filter { it.xAxis != 0f }.maxByOrNull { it.x }
        if (min == null || max == null) {
            return
        } else {
            val minIndex = min.x
            val maxIndex = max.x
            if (maxIndex - minIndex >= 2) {
                val minAxis = min.xAxis
                val maxAxis = max.xAxis
                val distance = (maxAxis - minAxis) / (maxIndex - minIndex)
                val holes = mutableListOf<Hole>()
                for (i in minIndex + 1 until maxIndex) {
                    val hole = holeList.find { it.x == i && it.xAxis == 0f }
                    hole?.let {
                        holes.add(it.copy(xAxis = minAxis + (i - minIndex) * distance))
                    }
                }
                holeDao.updateAll(holes)
            }
        }
    }
}

data class PlateUiState(
    val container: Container? = null,
    val plate: Plate? = null,
    val holeList: List<Hole> = emptyList()
)