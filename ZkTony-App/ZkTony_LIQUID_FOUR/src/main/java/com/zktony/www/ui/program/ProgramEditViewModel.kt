package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.core.utils.Snowflake
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.dao.PointDao
import com.zktony.www.room.entity.Container
import com.zktony.www.room.entity.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ProgramEditViewModel constructor(
    private val containerDao: ContainerDao,
    private val pointDao: PointDao
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ProgramEditUiState())
    val uiState = _uiState.asStateFlow()


    fun init(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(id = id)
            launch {
                containerDao.getByType(1).collect {
                    _uiState.value = _uiState.value.copy(containerList = it)
                }
            }
            launch {
                pointDao.getBySubId(id).collect {
                    _uiState.value = _uiState.value.copy(pointList = it)
                }
            }
        }
    }

    fun selectPoint(index: Int, cIndex: Int) {
        viewModelScope.launch {
            val c1 = _uiState.value.containerList[cIndex]
            val l1 = pointDao.getBySubId(c1.id).firstOrNull()
            l1?.let {
                val snowflake = Snowflake(1)
                val list = mutableListOf<Point>()
                it.forEach { p ->
                    list.add(
                        p.copy(
                            id = snowflake.nextId(),
                            subId = _uiState.value.id,
                            thirdId = c1.id,
                            index = index,
                        )
                    )
                }
                pointDao.insertAll(list)
            }
        }
    }

    fun deletePoint(index: Int) {
        viewModelScope.launch {
            val list = _uiState.value.pointList.filter { it.index == index }
            pointDao.deleteAll(list)
        }
    }
}

data class ProgramEditUiState(
    val id: Long = 0L,
    val containerList: List<Container> = emptyList(),
    val pointList: List<Point> = emptyList(),
)