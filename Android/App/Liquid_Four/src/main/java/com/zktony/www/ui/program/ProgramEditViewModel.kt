package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.nextId
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.dao.PointDao
import com.zktony.www.room.entity.Container
import com.zktony.www.room.entity.Point
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
                }
            }
            launch {
                PD.getBySubId(id).collect {
                    _uiState.value = _uiState.value.copy(pointList = it)
                }
            }
        }
    }

    fun selectPoint(index: Int, cIndex: Int) {
        viewModelScope.launch {
            val c1 = _uiState.value.containerList[cIndex]
            val l1 = PD.getBySubId(c1.id).firstOrNull()
            l1?.let {
                val list = mutableListOf<Point>()
                it.forEach { p ->
                    list.add(
                        p.copy(
                            id = nextId(),
                            subId = _uiState.value.id,
                            thirdId = c1.id,
                            index = index,
                        )
                    )
                }
                PD.insertAll(list)
            }
        }
    }

    fun deletePoint(index: Int) {
        viewModelScope.launch {
            val list = _uiState.value.pointList.filter { it.index == index }
            PD.deleteAll(list)
        }
    }
}

data class ProgramEditUiState(
    val id: Long = 0L,
    val containerList: List<Container> = emptyList(),
    val pointList: List<Point> = emptyList(),
)