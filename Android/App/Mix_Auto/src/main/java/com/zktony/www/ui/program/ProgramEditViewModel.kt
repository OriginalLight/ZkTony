package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.core.utils.Snowflake
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.dao.PointDao
import com.zktony.www.room.entity.Container
import com.zktony.www.room.entity.Point
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProgramEditViewModel constructor(
    private val CD: ContainerDao,
    private val PD: PointDao,
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
                    _uiState.value = _uiState.value.copy(pointList = it)
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

    fun enablePoint(index: Int) {
        viewModelScope.launch {
            _uiState.value.pointList.find { it.index == index }?.let {
                PD.update(it.copy(enable = !it.enable))
            }
        }
    }

    fun enableAll() {
        viewModelScope.launch {
            PD.updateAll(_uiState.value.pointList.map { it.copy(enable = true) })
        }
    }

    fun updateVolume(v1: Int, v2: Int, v3: Int, v4: Int) {
        viewModelScope.launch {
            PD.updateAll(_uiState.value.pointList.map {
                it.copy(
                    v1 = v1,
                    v2 = v2,
                    v3 = v3,
                    v4 = v4
                )
            })
        }
    }

    fun selectContainer(index: Int) {
        viewModelScope.launch {
            val c1 = _uiState.value.containerList.getOrNull(index) ?: return@launch
            val c2 = _uiState.value.container ?: return@launch
            if (c1.id == c2.id) return@launch

            _uiState.value = _uiState.value.copy(container = c1)

            delay(200L)
            val pid = _uiState.value.id
            PD.deleteBySubId(pid)
        }
    }

    private fun buildPoint(id: Long) {
        viewModelScope.launch {
            val pid = _uiState.value.id
            val list = PD.getBySubId(id).firstOrNull()
            list?.let { it1 ->
                val snowflake = Snowflake(1)
                val list1 = mutableListOf<Point>()
                it1.forEach { point ->
                    list1.add(point.copy(id = snowflake.nextId(), subId = pid, thirdId = id))
                }
                PD.insertAll(list1)
            }
        }
    }
}

data class ProgramEditUiState(
    val containerList: List<Container> = emptyList(),
    val container: Container? = null,
    val pointList: List<Point> = emptyList(),
    val id: Long = 0L,
)