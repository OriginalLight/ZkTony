package com.zktony.android.ui.screen.motor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.data.entity.Motor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 */
class MotorViewModel constructor(
    private val dao: MotorDao
) : ViewModel() {

    fun entities() = dao.getAll()

    fun update(motor: Motor) {
        viewModelScope.launch {
            dao.update(motor)
        }
    }
}