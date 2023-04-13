package com.zktony.www.ui.container

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.utils.Snowflake
import com.zktony.www.common.ext.calculateCoordinate
import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.SerialManager
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.dao.PointDao
import com.zktony.www.room.entity.Container
import com.zktony.www.room.entity.Point
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ContainerEditViewModel constructor(
    private val dao: ContainerDao,
    private val pointDao: PointDao,
    private val serialManager: SerialManager,
    private val executionManager: ExecutionManager,
) : BaseViewModel() {


    private val _uiState = MutableStateFlow(ContainerEditUiState())
    val uiState = _uiState

    fun init(id: Long) {
        viewModelScope.launch {
            launch {
                dao.getById(id).collect {
                    _uiState.value = _uiState.value.copy(container = it)
                }
            }
            launch {
                pointDao.getBySubId(id).collect {
                    _uiState.value = _uiState.value.copy(list = it)
                }
            }
        }
    }

    fun setXY(x: Int, y: Int) {
        viewModelScope.launch {
            _uiState.value.container?.let {
                dao.update(it.copy(x = x, y = y))
                pointDao.deleteBySubId(it.id)
                val snowflake = Snowflake(2)
                val list = mutableListOf<Point>()
                for (i in 0 until x) {
                    for (j in 0 until y) {
                        list.add(Point(id = snowflake.nextId(), subId = it.id, x = i, y = j))
                    }
                }
                pointDao.insertAll(list)
            }
        }
    }


    fun move(x: Float, y: Float) {
        if (serialManager.lock.value || serialManager.pause.value) {
            PopTip.show("机器正在运行中")
            return
        }
        executionManager.executor(executionManager.generator(x = x, y = y))
    }

    fun save(x: Float, y: Float, flag: Int) {
        viewModelScope.launch {
            if (flag == 0) {
                val x0y0 = _uiState.value.list.find { it.x == 0 && it.y == 0 }!!
                pointDao.update(x0y0.copy(xAxis = x, yAxis = y))
            } else {
                val x1y1 =
                    _uiState.value.list.find { it.x == _uiState.value.container!!.x - 1 && it.y == _uiState.value.container!!.y - 1 }!!
                pointDao.update(x1y1.copy(xAxis = x, yAxis = y))
            }
            delay(500L)
            pointDao.updateAll(_uiState.value.list.calculateCoordinate(_uiState.value.container!!))
        }
    }
}

data class ContainerEditUiState(
    val container: Container? = null,
    val list: List<Point> = emptyList(),
)