package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.common.room.entity.CalibrationData
import com.zktony.www.data.repository.CalibrationDataRepository
import com.zktony.www.data.repository.CalibrationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class CalibrationDataViewModel @Inject constructor(
    private val caliRepo: CalibrationRepository,
    private val caliDataRepo: CalibrationDataRepository
) : BaseViewModel() {

    private val _cali = MutableStateFlow(Calibration())
    private val _caliDataList = MutableStateFlow(emptyList<CalibrationData>())
    val cali = _cali.asStateFlow()
    val caliDataList = _caliDataList.asStateFlow()

    /**
     * 初始化校准数据
     */
    fun initCali(id: String) {
        viewModelScope.launch {
            launch {
                caliRepo.getById(id).collect {
                    _cali.value = it
                }
            }
            launch {
                caliDataRepo.getByCaliId(id).collect {
                    _caliDataList.value = it
                }
            }
        }
    }
}