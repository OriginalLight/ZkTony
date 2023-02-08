package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.common.room.entity.CalibrationData
import com.zktony.www.control.motion.MotionManager
import com.zktony.www.data.repository.CalibrationDataRepository
import com.zktony.www.data.repository.CalibrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalibrationDataViewModel @Inject constructor(
    private val calibrationRepository: CalibrationRepository,
    private val calibrationDataRepository: CalibrationDataRepository
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val manager = MotionManager.instance

    private val _cali = MutableStateFlow(Calibration())
    private val _caliDataList = MutableStateFlow(emptyList<CalibrationData>())
    private val _motorId = MutableStateFlow(3)
    val cali = _cali.asStateFlow()
    val caliDataList = _caliDataList.asStateFlow()
    val motorId = _motorId.asStateFlow()

    /**
     * 初始化校准数据
     */
    fun initCali(id: String) {
        viewModelScope.launch {
            launch {
                calibrationRepository.getById(id).collect {
                    _cali.value = it
                }
            }
            launch {
                calibrationDataRepository.getByCaliId(id).collect {
                    _caliDataList.value = it
                }
            }
        }
    }

    /**
     * 选择电机
     */
    fun selectMotor(id: Int) {
        _motorId.value = id
    }

    /**
     * 删除校准数据
     */
    fun delete(caliData: CalibrationData) {
        viewModelScope.launch {
            calibrationDataRepository.delete(caliData)
            delay(1000L)
            updateCali()
        }
    }

    /**
     * 添加校准数据
     */
    fun add(caliData: CalibrationData) {
        viewModelScope.launch {
            val appCali = appViewModel.settings.value.motorUnits.cali
            val before = when (motorId.value) {
                3 -> appCali.p1
                4 -> appCali.p2
                5 -> appCali.p3
                6 -> appCali.p4
                7 -> appCali.p5
                else -> 0f
            }
            val after = caliData.actualVolume / caliData.volume * before
            calibrationDataRepository.insert(
                caliData.copy(
                    calibrationId = cali.value.id,
                    motorId = motorId.value,
                    before = before,
                    after = after
                )
            )
            delay(1000L)
            updateCali()
        }
    }

    /**
     * 加液
     */
    fun addLiquid(liquid: Float) {
        val con = appViewModel.settings.value.container
        manager.executor(
            manager.generator(y = con.washY),
            manager.generator(
                y = con.washY,
                z = con.washZ,
                v1 = if (motorId.value == 3) liquid else 0f,
                v2 = if (motorId.value == 4) liquid else 0f,
                v3 = if (motorId.value == 5) liquid else 0f,
                v4 = if (motorId.value == 6) liquid else 0f,
                v5 = if (motorId.value == 7) liquid else 0f,
                v6 = if (motorId.value == 8) liquid else 0f
            ),
            manager.generator(
                y = con.washY,
                v1 = if (motorId.value == 3) 15000f else 0f,
                v2 = if (motorId.value == 4) 15000f else 0f,
                v3 = if (motorId.value == 5) 15000f else 0f,
                v4 = if (motorId.value == 6) 15000f else 0f,
                v5 = if (motorId.value == 7) 15000f else 0f,
                v6 = if (motorId.value == 8) 15000f else 0f
            ),
            manager.generator()
        )

    }

    /**
     * 更新校准数据
     */
    private fun updateCali() {
        viewModelScope.launch {
            // 列表中motorId = 3的数据的after的平均值 列表为空时为0
            val p1 =
                caliDataList.value.filter { it.motorId == 3 }.map { it.after }.average().toFloat()
            val p2 =
                caliDataList.value.filter { it.motorId == 4 }.map { it.after }.average().toFloat()
            val p3 =
                caliDataList.value.filter { it.motorId == 5 }.map { it.after }.average().toFloat()
            val p4 =
                caliDataList.value.filter { it.motorId == 6 }.map { it.after }.average().toFloat()
            val p5 =
                caliDataList.value.filter { it.motorId == 7 }.map { it.after }.average().toFloat()
            calibrationRepository.update(
                cali.value.copy(
                    v1 = if (p1.isNaN()) 180f else p1,
                    v2 = if (p2.isNaN()) 180f else p2,
                    v3 = if (p3.isNaN()) 180f else p3,
                    v4 = if (p4.isNaN()) 180f else p4,
                    v5 = if (p5.isNaN()) 180f else p5
                )
            )
        }
    }

}