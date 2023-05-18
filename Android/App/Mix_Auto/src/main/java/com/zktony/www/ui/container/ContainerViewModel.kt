package com.zktony.www.ui.container

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.www.data.dao.ContainerDao
import com.zktony.www.data.dao.PointDao
import com.zktony.www.data.entities.Container
import com.zktony.www.data.entities.Point
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
            if (name.isEmpty()) {
                PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.not_empty))
                return@launch
            }
            val container = _uiState.value.list.find { it.name == name }
            if (container != null) {
                PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.already_exists))
            } else {
                val con = Container(name = name)
                CD.insert(con)
                val list = mutableListOf<Point>()
                for (i in 0 until con.size) {
                    list.add(Point(subId = con.id, index = i))
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