package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.zktony.serialport.util.Serial
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.common.room.entity.CalibrationData
import com.zktony.www.data.repository.CalibrationDataRepository
import com.zktony.www.data.repository.CalibrationRepository
import com.zktony.www.serial.SerialManager
import com.zktony.www.serial.protocol.V1
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalibrationDataViewModel @Inject constructor(
    private val caliRepo: CalibrationRepository, private val caliDataRepo: CalibrationDataRepository
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

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
            caliDataRepo.delete(caliData)
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
            caliDataRepo.insert(
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
        val settings = appViewModel.settings.value
        val stepOne = settings.motorUnits.toPumpHex(
            one = if (motorId.value == 3) liquid else 0f,
            two = if (motorId.value == 4) liquid else 0f,
            three = if (motorId.value == 5) liquid else 0f,
            four = if (motorId.value == 6) liquid else 0f,
            five = if (motorId.value == 7) liquid else 0f
        )
        val stepTwo = settings.motorUnits.toPumpHex(
            one = if (motorId.value == 3) settings.motorUnits.cali.p1 * settings.container.extract / 1000 else 0f,
            two = if (motorId.value == 4) settings.motorUnits.cali.p2 * settings.container.extract / 1000 else 0f,
            three = if (motorId.value == 5) settings.motorUnits.cali.p3 * settings.container.extract / 1000 else 0f,
            four = if (motorId.value == 6) settings.motorUnits.cali.p4 * settings.container.extract / 1000 else 0f,
            five = if (motorId.value == 7) settings.motorUnits.cali.p5 * settings.container.extract / 1000 else 0f
        )
        val move = settings.motorUnits.toMotionHex(
            settings.container.washY, 0f
        ) + settings.motorUnits.toMotionHex(
            settings.container.washY, settings.container.washZ
        ) + settings.motorUnits.toMotionHex(
            settings.container.washY, 0f
        ) + settings.motorUnits.toMotionHex(0f, 0f)

        SerialManager.instance.lock(true)
        SerialManager.instance.sendHex(
            serial = Serial.TTYS0, hex = V1.multiPoint(
                if (motorId.value == 7) "0,0,0,0,0,0,0,0,0,0,0,0," else move,
            )
        )
        SerialManager.instance.sendHex(
            serial = Serial.TTYS1,
            hex = V1.multiPoint("0,0,0," + stepOne.first + "0,0,0," + stepTwo.first)
        )
        SerialManager.instance.sendHex(
            serial = Serial.TTYS2,
            hex = V1.multiPoint("0,0,0," + stepOne.second + "0,0,0," + stepTwo.second)
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
            caliRepo.update(
                cali.value.copy(
                    p1 = if (p1.isNaN()) 180f else p1,
                    p2 = if (p2.isNaN()) 180f else p2,
                    p3 = if (p3.isNaN()) 180f else p3,
                    p4 = if (p4.isNaN()) 180f else p4,
                    p5 = if (p5.isNaN()) 49f else p5
                )
            )
        }
    }

}