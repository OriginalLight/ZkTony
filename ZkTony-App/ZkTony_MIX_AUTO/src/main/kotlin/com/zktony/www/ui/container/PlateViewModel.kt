package com.zktony.www.ui.container

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseViewModel
import com.zktony.common.utils.Snowflake
import com.zktony.www.data.local.dao.ContainerDao
import com.zktony.www.data.local.dao.HoleDao
import com.zktony.www.data.local.dao.PlateDao
import com.zktony.www.data.local.entity.Container
import com.zktony.www.data.local.entity.Hole
import com.zktony.www.data.local.entity.Plate
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
                if (it.size != size) {
                    plateDao.update(it.copy(size = size))
                    holeDao.deleteBySubId(it.id)
                    val holeList = mutableListOf<Hole>()
                    val snowflake = Snowflake(1)
                    for (i in 0 until size) {
                        holeList.add(
                            Hole(
                                id = snowflake.nextId(),
                                subId = it.id,
                                y = i,
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
            val hole = _uiState.value.holeList.find { it.y == index }
            hole?.let {
                holeDao.update(it.copy(yAxis = axis))
                delay(500L)
                calculate()
            }
        }
    }

    fun moveY(y: Float) {
        if (serialManager.lock.value || serialManager.pause.value) {
            PopTip.show("机器正在运行中")
            return
        }
        executionManager.executor(executionManager.generator(y = y))
    }

    /**
     * 数组计算算法
     */
    private suspend fun calculate(index: Int  = 0) {
        val holeList = _uiState.value.holeList
        val h1 = holeList[index]
        var h2 = holeList[index + 1]
        for (i in index + 1 until holeList.size) {
            if (holeList[i].yAxis != 0f) {
                h2 = holeList[i]
                break
            }
        }
        if (h2.yAxis == 0f && h2.y == index + 1) {
            return
        } else {
            val minIndex = h1.y
            val maxIndex = h2.y
            if (maxIndex - minIndex >= 2) {
                val minAxis = h1.yAxis
                val maxAxis = h2.yAxis
                val distance = (maxAxis - minAxis) / (maxIndex - minIndex)
                val holes = mutableListOf<Hole>()
                for (i in minIndex + 1 until maxIndex) {
                    val hole = holeList.find { it.y == i && it.yAxis == 0f }
                    hole?.let {
                        holes.add(it.copy(yAxis = minAxis + (i - minIndex) * distance))
                    }
                }
                holeDao.updateAll(holes)
            }
            if (maxIndex < holeList.size - 2) {
                calculate(maxIndex)
            }
        }
    }
}

data class PlateUiState(
    val container: Container? = null,
    val plate: Plate? = null,
    val holeList: List<Hole> = emptyList()
)