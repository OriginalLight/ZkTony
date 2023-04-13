package com.zktony.www.ui.container

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.utils.Snowflake
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.dao.PointDao
import com.zktony.www.room.entity.Container
import com.zktony.www.room.entity.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContainerViewModel constructor(
    private val dao: ContainerDao,
    private val pointDao: PointDao
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ContainerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAll().collect {
                _uiState.value = _uiState.value.copy(list = it)
            }
        }
    }

    fun delete(container: Container) {
        viewModelScope.launch {
            dao.delete(container)
            pointDao.deleteBySubId(container.id)
        }
    }

    fun insert(name: String, function: (Long) -> Unit) {
        viewModelScope.launch {
            val container = _uiState.value.list.find { it.name == name }
            val snowflake = Snowflake(1)
            if (container != null) {
                PopTip.show("容器名已存在")
            } else {
                val con = Container(
                    id = snowflake.nextId(),
                    name = name,
                    type = 1
                )
                dao.insert(con)
                val pointList = mutableListOf<Point>()
                for (i in 0 until con.x) {
                    for (j in 0 until con.y) {
                        pointList.add(Point(id = snowflake.nextId(), subId = con.id, x = i, y = j))
                    }
                }
                pointDao.insertAll(pointList)
                function(con.id)
            }
        }
    }
}

data class ContainerUiState(
    val list: List<Container> = emptyList(),
)