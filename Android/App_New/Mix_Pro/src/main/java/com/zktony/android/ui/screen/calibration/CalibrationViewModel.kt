package com.zktony.android.ui.screen.calibration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.entity.Calibration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/9 13:19
 */
class CalibrationViewModel constructor(
    private val dao: CalibrationDao,
) : ViewModel() {

    fun entities() = dao.getAll()

    fun insert(name: String) {
        viewModelScope.launch {
            dao.insert(Calibration(name = name))
        }
    }

    fun delete(entity: Calibration) {
        viewModelScope.launch {
            dao.delete(entity)
        }
    }

    fun update(entity: Calibration) {
        viewModelScope.launch {
            dao.update(entity)
        }
    }

    fun active(id: Long) {
        viewModelScope.launch {
            dao.active(id)
        }
    }

    fun addLiquid(i: Int, fl: Float) {

    }
}