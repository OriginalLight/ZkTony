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
    private val CD: ContainerDao,
    private val PD: PointDao
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ContainerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            CD.getAll().collect {
                _uiState.value = _uiState.value.copy(list = it)
            }
        }
    }

    fun delete(container: Container) {
        viewModelScope.launch {
            CD.delete(container)
            PD.deleteBySubId(container.id)
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
                CD.insert(con)
                val list = mutableListOf<Point>()
                for (i in 0 until con.size) {
                    list.add(
                        Point(
                            id = snowflake.nextId(),
                            subId = con.id,
                            index = i,
                        )
                    )
                }
                PD.insertAll(list)
                function(con.id)
            }
        }
    }
}

data class ContainerUiState(
    val list: List<Container> = emptyList(),
)