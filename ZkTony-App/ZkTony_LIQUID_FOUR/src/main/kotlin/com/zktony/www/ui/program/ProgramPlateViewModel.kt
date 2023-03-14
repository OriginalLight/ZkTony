package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.zktony.common.base.BaseViewModel
import com.zktony.common.utils.Snowflake
import com.zktony.www.data.local.room.dao.HoleDao
import com.zktony.www.data.local.room.dao.PlateDao
import com.zktony.www.data.local.room.entity.Hole
import com.zktony.www.data.local.room.entity.Plate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkPlateViewModel @Inject constructor(
    private val dao: PlateDao,
    private val holeDao: HoleDao,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(WorkPlateUiState())
    val uiState = _uiState.asStateFlow()


    fun init(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(id = id)
            launch {
                dao.getByIdList(listOf(1L, 2L, 3L, 4L)).collect {
                    _uiState.value = _uiState.value.copy(plates = it)
                }
            }
            launch {
                dao.getBySubId(id).collect {
                    _uiState.value = _uiState.value.copy(plateList = it)
                    if (it.isNotEmpty()) {
                        launch {
                            holeDao.getBySudIdList(it.map { plate -> plate.id }).collect { holeList ->
                                _uiState.value = _uiState.value.copy(holeList = holeList)
                            }
                        }
                    }
                }
            }
        }
    }

    fun selectPlate(index: Int) {
        viewModelScope.launch {
            val p1 = _uiState.value.plateList.find { plate -> plate.index == index }
            if (p1 != null) {
                dao.delete(p1)
                holeDao.deleteBySubId(p1.id)
            } else {
                val p0 = dao.getById((index + 1).toLong()).firstOrNull() ?: return@launch
                val h0 = holeDao.getBySubId(p0.id).firstOrNull() ?: return@launch
                val p2 = Plate(subId = _uiState.value.id, index = p0.index, x = p0.x, y = p0.y)
                dao.insert(p2)
                val snowflake = Snowflake(2)
                val h1 = mutableListOf<Hole>()
                h0.forEach {
                    h1.add(it.copy(id = snowflake.nextId(), subId = p2.id))
                }
                holeDao.insertAll(h1)
            }
        }
    }
}

data class WorkPlateUiState(
    val id: Long = 0L,
    val plates: List<Plate> = emptyList(),
    val holeList: List<Hole> = emptyList(),
    val plateList: List<Plate> = emptyList(),
)