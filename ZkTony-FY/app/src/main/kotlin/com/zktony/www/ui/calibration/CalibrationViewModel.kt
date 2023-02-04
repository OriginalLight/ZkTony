package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.data.repository.CalibrationDataRepository
import com.zktony.www.data.repository.CalibrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalibrationViewModel @Inject constructor(
    private val caliRepo: CalibrationRepository,
    private val caliDataRepo: CalibrationDataRepository
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val _calibrationList = MutableStateFlow<List<Calibration>>(emptyList())
    val calibrationList = _calibrationList.asStateFlow()

    init {
        viewModelScope.launch {
            caliRepo.getAll().collect {
                _calibrationList.value = it
            }
        }
    }

    /**
     * 添加
     * @param name String
     */
    fun add(name: String) {
        viewModelScope.launch {
            val list = caliRepo.getByName(name).first()
            if (list.isNotEmpty()) {
                PopTip.show("已存在相同名称")
                return@launch
            }
            val cali = Calibration(name = name)
            val calibration = appViewModel.settings.value.motorUnits.cali
            caliRepo.insert(
                calibration.copy(
                    id = cali.id,
                    name = cali.name,
                    status = if (calibrationList.value.isEmpty()) 1 else 0
                )
            )
        }
    }

    /**
     * 删除
     * @param cali Calibration
     */
    fun delete(cali: Calibration) {
        viewModelScope.launch {
            caliRepo.delete(cali)
            caliDataRepo.deleteByCaliId(cali.id)
        }
    }

    /**
     * 选中
     */
    fun select(cali: Calibration) {
        viewModelScope.launch {
            caliRepo.select(cali)
        }
    }
}

