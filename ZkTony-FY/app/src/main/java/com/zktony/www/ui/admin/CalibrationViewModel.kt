package com.zktony.www.ui.admin

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.data.repository.CalibrationRepository
import com.zktony.www.serialport.SerialPort.*
import com.zktony.www.serialport.SerialPortManager
import com.zktony.www.serialport.protocol.Command
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalibrationViewModel @Inject constructor(
    private val calibrationRepository: CalibrationRepository,
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val _calibration = MutableStateFlow(Calibration())
    private val _editCalibration = MutableStateFlow(Calibration())
    val calibration = _calibration.asStateFlow()
    val editCalibration = _editCalibration.asStateFlow()

    init {
        viewModelScope.launch {
            calibrationRepository.getCalibration().collect { calibrationList ->
                if (calibrationList.isNotEmpty()) {
                    _calibration.value = calibrationList.first()
                    _editCalibration.value = calibrationList.first()
                }
            }
        }
    }

    /**
     * 校准值改变
     * @param calibration [Calibration]
     */
    fun calibrationValueChange(calibration: Calibration) {
        viewModelScope.launch {
            _editCalibration.value = calibration
        }
    }

    /**
     * 更新校准值
     */
    fun updateCalibration() {
        viewModelScope.launch {
            calibrationRepository.update(editCalibration.value)
            PopTip.show("更新成功")
        }
    }

    /**
     * 测试 移动到废液槽
     */
    fun toWasteTank() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settingState.value.motionMotor
            val calibration = calibration.value
            SerialPortManager.instance.sendHex(
                SERIAL_ONE,
                Command.multiPoint(motionMotor.toMotionHex(calibration.wasteTankPosition, 0f))
            )
            SerialPortManager.instance.sendHex(SERIAL_TWO, Command.multiPoint("0,0,0,"))
            SerialPortManager.instance.sendHex(SERIAL_THREE, Command.multiPoint("0,0,0,"))
        }
    }

    /**
     * 测试 废液槽针头下降
     */
    fun wasteTankNeedleDown() {
        val motionMotor = appViewModel.settingState.value.motionMotor
        val calibration = calibration.value
        SerialPortManager.instance.sendHex(
            SERIAL_ONE,
            Command.multiPoint(
                motionMotor.toMotionHex(calibration.wasteTankPosition, 0f) +
                        motionMotor.toMotionHex(
                            calibration.wasteTankPosition,
                            calibration.wasteTankHeight
                        )
            )
        )
        SerialPortManager.instance.sendHex(SERIAL_TWO, Command.multiPoint("0,0,0,0,0,0,"))
        SerialPortManager.instance.sendHex(SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,"))
    }


    /**
     * 测试 移动到洗液槽
     */
    fun toWashTank() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settingState.value.motionMotor
            val calibration = calibration.value
            SerialPortManager.instance.sendHex(
                SERIAL_ONE,
                Command.multiPoint(motionMotor.toMotionHex(calibration.washTankPosition, 0f))
            )
            SerialPortManager.instance.sendHex(SERIAL_TWO, Command.multiPoint("0,0,0,"))
            SerialPortManager.instance.sendHex(SERIAL_THREE, Command.multiPoint("0,0,0,"))
        }
    }

    /**
     * 测试 洗液槽针头下降
     */
    fun washTankNeedleDown() {
        val motionMotor = appViewModel.settingState.value.motionMotor
        val calibration = calibration.value
        SerialPortManager.instance.sendHex(
            SERIAL_ONE,
            Command.multiPoint(
                motionMotor.toMotionHex(calibration.washTankPosition, 0f) +
                        motionMotor.toMotionHex(
                            calibration.washTankPosition,
                            calibration.washTankHeight
                        )
            )
        )
        SerialPortManager.instance.sendHex(SERIAL_TWO, Command.multiPoint("0,0,0,0,0,0,"))
        SerialPortManager.instance.sendHex(SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,"))

    }
    /**
     * 测试 移动到阻断液槽
     */
    fun toBlockingLiquidTank() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settingState.value.motionMotor
            val calibration = calibration.value
            SerialPortManager.instance.sendHex(
                SERIAL_ONE,
                Command.multiPoint(
                    motionMotor.toMotionHex(calibration.blockingLiquidTankPosition, 0f)
                )
            )
            SerialPortManager.instance.sendHex(SERIAL_TWO, Command.multiPoint("0,0,0,"))
            SerialPortManager.instance.sendHex(SERIAL_THREE, Command.multiPoint("0,0,0,"))
        }
    }

    /**
     * 测试 阻断液槽针头下降
     */
    fun blockingLiquidTankNeedleDown() {
        val motionMotor = appViewModel.settingState.value.motionMotor
        val calibration = calibration.value
        SerialPortManager.instance.sendHex(
            SERIAL_ONE,
            Command.multiPoint(
                motionMotor.toMotionHex(calibration.blockingLiquidTankPosition, 0f) +
                        motionMotor.toMotionHex(
                            calibration.blockingLiquidTankPosition,
                            calibration.blockingLiquidTankHeight
                        )
            )
        )
        SerialPortManager.instance.sendHex(SERIAL_TWO, Command.multiPoint("0,0,0,0,0,0,"))
        SerialPortManager.instance.sendHex(SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,"))
    }

    /**
     * 测试 移动到抗体一槽
     */
    fun toAntibodyOneTank() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settingState.value.motionMotor
            val calibration = calibration.value
            SerialPortManager.instance.sendHex(
                SERIAL_ONE,
                Command.multiPoint(
                    motionMotor.toMotionHex(calibration.antibodyOneTankPosition, 0f)
                )
            )
            SerialPortManager.instance.sendHex(SERIAL_TWO, Command.multiPoint("0,0,0,"))
            SerialPortManager.instance.sendHex(SERIAL_THREE, Command.multiPoint("0,0,0,"))
        }
    }

    /**
     * 测试 抗体一槽针头下降
     */
    fun antibodyOneTankNeedleDown() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settingState.value.motionMotor
            val calibration = calibration.value
            SerialPortManager.instance.sendHex(
                SERIAL_ONE,
                Command.multiPoint(
                    motionMotor.toMotionHex(calibration.antibodyOneTankPosition, 0f) +
                            motionMotor.toMotionHex(
                                calibration.antibodyOneTankPosition,
                                calibration.antibodyOneTankHeight
                            )
                )
            )
            SerialPortManager.instance.sendHex(SERIAL_TWO, Command.multiPoint("0,0,0,0,0,0,"))
            SerialPortManager.instance.sendHex(SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,"))
            delay(3000L)
            SerialPortManager.instance.sendHex(
                SERIAL_ONE,
                Command.multiPoint(
                    motionMotor.toMotionHex(
                        calibration.antibodyOneTankPosition,
                        calibration.recycleAntibodyOneTankHeight
                    )
                )
            )
            SerialPortManager.instance.sendHex(SERIAL_TWO, Command.multiPoint("0,0,0,"))
            SerialPortManager.instance.sendHex(SERIAL_THREE, Command.multiPoint("0,0,0,"))
        }
    }

    /**
     * 测试 移动到抗体二槽
     */
    fun toAntibodyTwoTank() {
        val motionMotor = appViewModel.settingState.value.motionMotor
        val calibration = calibration.value
        SerialPortManager.instance.sendHex(
            SERIAL_ONE,
            Command.multiPoint(
                motionMotor.toMotionHex(calibration.antibodyTwoTankPosition, 0f)
            )
        )
        SerialPortManager.instance.sendHex(SERIAL_TWO, Command.multiPoint("0,0,0,"))
        SerialPortManager.instance.sendHex(SERIAL_THREE, Command.multiPoint("0,0,0,"))

    }

    /**
     * 测试 抗体二槽针头下降
     */
    fun antibodyTwoTankNeedleDown() {
        val motionMotor = appViewModel.settingState.value.motionMotor
        val calibration = calibration.value
        SerialPortManager.instance.sendHex(
            SERIAL_ONE,
            Command.multiPoint(
                motionMotor.toMotionHex(calibration.antibodyTwoTankPosition, 0f) +
                        motionMotor.toMotionHex(
                            calibration.antibodyTwoTankPosition,
                            calibration.antibodyTwoTankHeight
                        )
            )
        )
        SerialPortManager.instance.sendHex(SERIAL_TWO, Command.multiPoint("0,0,0,0,0,0,"))
        SerialPortManager.instance.sendHex(SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,"))
    }

    /**
     * 测试 回到原点
     */
    fun toZeroPosition() {
        val motionMotor = appViewModel.settingState.value.motionMotor
        SerialPortManager.instance.sendHex(
            SERIAL_ONE,
            Command.multiPoint(
                motionMotor.toMotionHex(0f, 0f)
            )
        )
        SerialPortManager.instance.sendHex(SERIAL_TWO, Command.multiPoint("0,0,0,"))
        SerialPortManager.instance.sendHex(SERIAL_THREE, Command.multiPoint("0,0,0,"))

    }

    /**
     * 测试 吸液
     */
    fun aspirate() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settingState.value.motionMotor
            SerialPortManager.instance.sendHex(
                SERIAL_ONE,
                Command.multiPoint(
                    motionMotor.toMotionHex(0f, 0f)
                )
            )
            SerialPortManager.instance.sendHex(SERIAL_TWO, Command.multiPoint("3200,3200,3200,"))
            SerialPortManager.instance.sendHex(SERIAL_THREE, Command.multiPoint("3200,3200,0,"))
        }
    }

    /**
     * 测试 排液
     */
    fun drainage() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settingState.value.motionMotor
            SerialPortManager.instance.sendHex(
                SERIAL_ONE,
                Command.multiPoint(
                    motionMotor.toMotionHex(0f, 0f)
                )
            )
            SerialPortManager.instance.sendHex(
                SERIAL_TWO,
                Command.multiPoint("-32000,-32000,-32000,")
            )
            SerialPortManager.instance.sendHex(SERIAL_THREE, Command.multiPoint("-32000,-32000,0,"))
        }
    }
}

