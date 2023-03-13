package com.zktony.www.ui.container

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseViewModel
import com.zktony.www.control.motion.MotionManager
import com.zktony.www.control.serial.SerialManager
import com.zktony.www.data.local.room.dao.ContainerDao
import com.zktony.www.data.local.room.dao.HoleDao
import com.zktony.www.data.local.room.dao.PlateDao
import com.zktony.www.data.local.room.entity.Plate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlateViewModel @Inject constructor(
    private val containerDao: ContainerDao,
    private val plateDao: PlateDao,
    private val holeDao: HoleDao
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<Plate?>(null)
    val uiState = _uiState

    init {
        viewModelScope.launch {
            launch {

            }
        }
    }


    fun move(x: Float, y: Float) {
        val serial = SerialManager.instance
        if (serial.lock.value || serial.pause.value) {
            PopTip.show("机器正在运行中")
            return
        }
        val manager = MotionManager.instance
        manager.executor(manager.generator(x = x, y = y))
    }

    fun save(z1: Float, z2: Float) {
        viewModelScope.launch {
            _uiState.value?.let {

            }
        }
    }

    private suspend fun calculateCoordinate(plate: Plate) {
        val holeList = holeDao.getBySubId(plate.id).firstOrNull() ?: emptyList()
        val min = holeList.filter { it.xAxis != 0f }.minByOrNull { it.x }
        val max = holeList.filter { it.xAxis != 0f }.maxByOrNull { it.x }
        if (min == null || max == null) {
            return
        } else {
            val minIndex = min.x
            val maxIndex = max.x
            if (maxIndex - minIndex >= 2) {
                val minAxis = min.xAxis
                val maxAxis = max.xAxis
                val distance = (maxAxis - minAxis) / (maxIndex - minIndex)
                for (i in minIndex + 1 until maxIndex) {
                    val hole = holeList.find { it.x == i && it.xAxis == 0f }
                    hole?.let {
                        holeDao.update(
                            it.copy(
                                xAxis = minAxis + (i - minIndex) * distance
                            )
                        )
                    }
                }
            }
        }
    }
}