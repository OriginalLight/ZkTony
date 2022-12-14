package com.zktony.www.ui.admin

import androidx.lifecycle.viewModelScope
import com.zktony.serialport.util.Serial.*
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.common.room.entity.Motor
import com.zktony.www.data.repository.CalibrationRepository
import com.zktony.www.data.repository.MotorRepository
import com.zktony.www.serial.SerialManager
import com.zktony.www.serial.protocol.V1
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalibrationViewModel @Inject constructor(
    private val caliRepo: CalibrationRepository,
    private val motorRepo: MotorRepository
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val _calibration = MutableStateFlow(Calibration())
    val calibration = _calibration.asStateFlow()

    init {
        viewModelScope.launch {
            caliRepo.getCalibration().collect {
                if (it.isNotEmpty()) {
                    _calibration.value = it.first()
                }
            }
        }
    }

    /**
     * 更新校准值
     * @param calibration [Calibration]
     */
    fun updateCalibration(calibration: Calibration) {
        viewModelScope.launch {
            caliRepo.update(calibration)
        }
    }

    /**
     * 电机修改参数
     * @param motor [Motor]
     */
    fun updateMotorValue(motor: Motor) {
        viewModelScope.launch {
            motorRepo.update(motor)
        }
    }

    /**
     * 测试 移动到废液槽
     */
    fun toWasteTank() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settings.value.motorUnits
            val calibration = calibration.value
            SerialManager.instance.sendHex(
                TTYS0, V1.multiPoint(motionMotor.toMotionHex(calibration.wasteY, 0f))
            )
            SerialManager.instance.sendHex(TTYS1, V1.multiPoint("0,0,0,"))
            SerialManager.instance.sendHex(TTYS2, V1.multiPoint("0,0,0,"))
        }
    }

    /**
     * 测试 废液槽针头下降
     */
    fun wasteTankNeedleDown() {
        val motionMotor = appViewModel.settings.value.motorUnits
        val calibration = calibration.value
        SerialManager.instance.sendHex(
            TTYS0, V1.multiPoint(
                motionMotor.toMotionHex(calibration.wasteY, 0f) + motionMotor.toMotionHex(
                    calibration.wasteY, calibration.wasteZ
                )
            )
        )
        SerialManager.instance.sendHex(TTYS1, V1.multiPoint("0,0,0,0,0,0,"))
        SerialManager.instance.sendHex(TTYS2, V1.multiPoint("0,0,0,0,0,0,"))
    }


    /**
     * 测试 移动到洗液槽
     */
    fun toWashTank() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settings.value.motorUnits
            val calibration = calibration.value
            SerialManager.instance.sendHex(
                TTYS0, V1.multiPoint(motionMotor.toMotionHex(calibration.washingY, 0f))
            )
            SerialManager.instance.sendHex(TTYS1, V1.multiPoint("0,0,0,"))
            SerialManager.instance.sendHex(TTYS2, V1.multiPoint("0,0,0,"))
        }
    }

    /**
     * 测试 洗液槽针头下降
     */
    fun washTankNeedleDown() {
        val motionMotor = appViewModel.settings.value.motorUnits
        val calibration = calibration.value
        SerialManager.instance.sendHex(
            TTYS0, V1.multiPoint(
                motionMotor.toMotionHex(calibration.washingY, 0f) + motionMotor.toMotionHex(
                    calibration.washingY, calibration.washingZ
                )
            )
        )
        SerialManager.instance.sendHex(TTYS1, V1.multiPoint("0,0,0,0,0,0,"))
        SerialManager.instance.sendHex(TTYS2, V1.multiPoint("0,0,0,0,0,0,"))

    }

    /**
     * 测试 移动到阻断液槽
     */
    fun toBlockingLiquidTank() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settings.value.motorUnits
            val calibration = calibration.value
            SerialManager.instance.sendHex(
                TTYS0, V1.multiPoint(
                    motionMotor.toMotionHex(calibration.blockingY, 0f)
                )
            )
            SerialManager.instance.sendHex(TTYS1, V1.multiPoint("0,0,0,"))
            SerialManager.instance.sendHex(TTYS2, V1.multiPoint("0,0,0,"))
        }
    }

    /**
     * 测试 阻断液槽针头下降
     */
    fun blockingLiquidTankNeedleDown() {
        val motionMotor = appViewModel.settings.value.motorUnits
        val calibration = calibration.value
        SerialManager.instance.sendHex(
            TTYS0, V1.multiPoint(
                motionMotor.toMotionHex(calibration.blockingY, 0f) + motionMotor.toMotionHex(
                    calibration.blockingY, calibration.blockingZ
                )
            )
        )
        SerialManager.instance.sendHex(TTYS1, V1.multiPoint("0,0,0,0,0,0,"))
        SerialManager.instance.sendHex(TTYS2, V1.multiPoint("0,0,0,0,0,0,"))
    }

    /**
     * 测试 移动到抗体一槽
     */
    fun toAntibodyOneTank() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settings.value.motorUnits
            val calibration = calibration.value
            SerialManager.instance.sendHex(
                TTYS0, V1.multiPoint(
                    motionMotor.toMotionHex(calibration.antibodyOneY, 0f)
                )
            )
            SerialManager.instance.sendHex(TTYS1, V1.multiPoint("0,0,0,"))
            SerialManager.instance.sendHex(TTYS2, V1.multiPoint("0,0,0,"))
        }
    }

    /**
     * 测试 抗体一槽针头下降
     */
    fun antibodyOneTankNeedleDown() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settings.value.motorUnits
            val calibration = calibration.value
            SerialManager.instance.sendHex(
                TTYS0, V1.multiPoint(
                    motionMotor.toMotionHex(calibration.antibodyOneY, 0f) + motionMotor.toMotionHex(
                        calibration.antibodyOneY, calibration.antibodyOneZ
                    )
                )
            )
            SerialManager.instance.sendHex(TTYS1, V1.multiPoint("0,0,0,0,0,0,"))
            SerialManager.instance.sendHex(TTYS2, V1.multiPoint("0,0,0,0,0,0,"))
            delay(3000L)
            SerialManager.instance.sendHex(
                TTYS0, V1.multiPoint(
                    motionMotor.toMotionHex(
                        calibration.antibodyOneY, calibration.recycleAntibodyOneZ
                    )
                )
            )
            SerialManager.instance.sendHex(TTYS1, V1.multiPoint("0,0,0,"))
            SerialManager.instance.sendHex(TTYS2, V1.multiPoint("0,0,0,"))
        }
    }

    /**
     * 测试 移动到抗体二槽
     */
    fun toAntibodyTwoTank() {
        val motionMotor = appViewModel.settings.value.motorUnits
        val calibration = calibration.value
        SerialManager.instance.sendHex(
            TTYS0, V1.multiPoint(
                motionMotor.toMotionHex(calibration.antibodyTwoY, 0f)
            )
        )
        SerialManager.instance.sendHex(TTYS1, V1.multiPoint("0,0,0,"))
        SerialManager.instance.sendHex(TTYS2, V1.multiPoint("0,0,0,"))

    }

    /**
     * 测试 抗体二槽针头下降
     */
    fun antibodyTwoTankNeedleDown() {
        val motionMotor = appViewModel.settings.value.motorUnits
        val calibration = calibration.value
        SerialManager.instance.sendHex(
            TTYS0, V1.multiPoint(
                motionMotor.toMotionHex(calibration.antibodyTwoY, 0f) + motionMotor.toMotionHex(
                    calibration.antibodyTwoY, calibration.antibodyTwoZ
                )
            )
        )
        SerialManager.instance.sendHex(TTYS1, V1.multiPoint("0,0,0,0,0,0,"))
        SerialManager.instance.sendHex(TTYS2, V1.multiPoint("0,0,0,0,0,0,"))
    }

    /**
     * 测试 回到原点
     */
    fun toZeroPosition() {
        val motionMotor = appViewModel.settings.value.motorUnits
        SerialManager.instance.sendHex(
            TTYS0, V1.multiPoint(
                motionMotor.toMotionHex(0f, 0f)
            )
        )
        SerialManager.instance.sendHex(TTYS1, V1.multiPoint("0,0,0,"))
        SerialManager.instance.sendHex(TTYS2, V1.multiPoint("0,0,0,"))

    }

    /**
     * 测试 吸液
     */
    fun aspirate() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settings.value.motorUnits
            SerialManager.instance.sendHex(
                TTYS0, V1.multiPoint(
                    motionMotor.toMotionHex(0f, 0f)
                )
            )
            SerialManager.instance.sendHex(TTYS1, V1.multiPoint("3200,3200,3200,"))
            SerialManager.instance.sendHex(TTYS2, V1.multiPoint("3200,3200,0,"))
        }
    }

    /**
     * 测试 排液
     */
    fun drainage() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settings.value.motorUnits
            SerialManager.instance.sendHex(
                TTYS0, V1.multiPoint(
                    motionMotor.toMotionHex(0f, 0f)
                )
            )
            SerialManager.instance.sendHex(
                TTYS1, V1.multiPoint("-32000,-32000,-32000,")
            )
            SerialManager.instance.sendHex(TTYS2, V1.multiPoint("-32000,-32000,0,"))
        }
    }
}

