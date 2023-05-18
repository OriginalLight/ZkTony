package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.nextId
import com.zktony.www.data.dao.ContainerDao
import com.zktony.www.data.dao.PointDao
import com.zktony.www.data.entities.Container
import com.zktony.www.data.entities.Point
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProgramEditViewModel constructor(
    private val CD: ContainerDao,
    private val PD: PointDao
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ProgramEditUiState())
    val uiState = _uiState.asStateFlow()


    fun init(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(id = id)
            launch {
                CD.getAll().collect {
                    _uiState.value = _uiState.value.copy(containerList = it)
                    if (it.isNotEmpty() && _uiState.value.container == null) {
                        _uiState.value = _uiState.value.copy(container = it[0])
                    }
                }
            }
            launch {
                delay(200L)
                PD.getBySubId(id).collect {
                    _uiState.value = _uiState.value.copy(list = it)
                    if (it.isEmpty()) {
                        val c1 = _uiState.value.container
                        if (c1 != null) {
                            delay(200L)
                            buildPoint(c1.id)
                        }
                    } else {
                        val c1 = _uiState.value.containerList.find { c -> c.id == it[0].thirdId }
                        if (c1 != null) {
                            _uiState.value = _uiState.value.copy(container = c1)
                        }
                    }
                }
            }
        }
    }

    private fun buildPoint(id: Long) {
        viewModelScope.launch {
            val pid = _uiState.value.id
            val list = PD.getBySubId(id).firstOrNull()
            list?.let { it1 ->
                val list1 = mutableListOf<Point>()
                it1.forEach { point ->
                    list1.add(point.copy(id = nextId(), subId = pid, thirdId = id))
                }
                PD.insertAll(list1)
            }
        }
    }

    fun enablePoint(x: Int, y: Int) {
        viewModelScope.launch {
            _uiState.value.list.find { it.x == x && it.y == y }?.let {
                PD.update(it.copy(enable = !it.enable))
            }
        }
    }

    fun enableAll() {
        viewModelScope.launch {
            PD.updateAll(_uiState.value.list.map { it.copy(enable = true) })
        }
    }

    fun updateVolume(v1: Int) {
        viewModelScope.launch {
            PD.updateAll(_uiState.value.list.map { it.copy(v1 = v1) })
        }
    }

    fun selectContainer(index: Int) {
        viewModelScope.launch {
            val c1 = _uiState.value.containerList.getOrNull(index) ?: return@launch
            _uiState.value = _uiState.value.copy(container = c1)
            delay(200L)
            val pid = _uiState.value.id
            PD.deleteBySubId(pid)
        }
    }
}

data class ProgramEditUiState(
    val id: Long = 0L,
    val container: Container? = null,
    val containerList: List<Container> = emptyList(),
    val list: List<Point> = emptyList(),
)