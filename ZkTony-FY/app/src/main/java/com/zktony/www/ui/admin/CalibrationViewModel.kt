package com.zktony.www.ui.admin

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.data.repository.CalibrationRepository
import com.zktony.www.serialport.SerialPortEnum
import com.zktony.www.serialport.SerialPortManager
import com.zktony.www.serialport.protocol.Command
import com.zktony.www.serialport.protocol.CommandBlock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalibrationViewModel @Inject constructor(
    private val calibrationRepository: CalibrationRepository,
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val _state = MutableSharedFlow<CalibrationState>()
    val state: SharedFlow<CalibrationState> get() = _state

    private val intent = MutableSharedFlow<CalibrationIntent>()

    private val _uiState = MutableStateFlow(CalibrationUiState())
    val uiState: StateFlow<CalibrationUiState> get() = _uiState

    init {
        viewModelScope.launch {
            launch {
                intent.collect {
                    when (it) {
                        is CalibrationIntent.OnCalibrationValueChange -> onCalibrationValueChange(it.calibration)
                        is CalibrationIntent.OnUpdateCalibration -> onUpdateCalibration()
                        is CalibrationIntent.ToWasteTank -> toWasteTank()
                        is CalibrationIntent.WasteTankNeedleDown -> wasteTankNeedleDown()
                        is CalibrationIntent.WasteTankNeedleUp -> wasteTankNeedleUp()
                        is CalibrationIntent.ToWashTank -> toWashTank()
                        is CalibrationIntent.WashTankNeedleDown -> washTankNeedleDown()
                        is CalibrationIntent.WashTankNeedleUp -> washTankNeedleUp()
                        is CalibrationIntent.ToBlockingLiquidTank -> toBlockingLiquidTank()
                        is CalibrationIntent.BlockingLiquidTankNeedleDown -> blockingLiquidTankNeedleDown()
                        is CalibrationIntent.BlockingLiquidTankNeedleUp -> blockingLiquidTankNeedleUp()
                        is CalibrationIntent.ToAntibodyOneTank -> toAntibodyOneTank()
                        is CalibrationIntent.AntibodyOneTankNeedleDown -> antibodyOneTankNeedleDown()
                        is CalibrationIntent.AntibodyOneTankNeedleUp -> antibodyOneTankNeedleUp()
                        is CalibrationIntent.ToAntibodyTwoTank -> toAntibodyTwoTank()
                        is CalibrationIntent.AntibodyTwoTankNeedleDown -> antibodyTwoTankNeedleDown()
                        is CalibrationIntent.AntibodyTwoTankNeedleUp -> antibodyTwoTankNeedleUp()
                        is CalibrationIntent.ToZeroPosition -> toZeroPosition()
                        is CalibrationIntent.Aspirate -> aspirate()
                        is CalibrationIntent.Drainage -> drainage()
                    }
                }
            }
            launch {
                calibrationRepository.getCalibration().collect { calibrationList ->
                    if (calibrationList.isNotEmpty()) {
                        _uiState.update {
                            uiState.value.copy(
                                calibration = calibrationList[0]
                            )
                        }
                        _state.emit(CalibrationState.OnCalibrationValueChange(calibrationList[0]))
                    }
                }
            }
        }
    }

    /**
     * Intent处理器
     * @param intent [CalibrationIntent]
     */
    fun dispatch(intent: CalibrationIntent) {
        try {
            viewModelScope.launch {
                this@CalibrationViewModel.intent.emit(intent)
            }
        } catch (_: Exception) {

        }
    }

    /**
     * 校准值改变
     * @param calibration [Calibration]
     */
    private fun onCalibrationValueChange(calibration: Calibration) {
        viewModelScope.launch {
            _uiState.update {
                uiState.value.copy(
                    calibration = calibration
                )
            }
        }
    }

    /**
     * 更新校准值
     */
    private fun onUpdateCalibration() {
        viewModelScope.launch {
            calibrationRepository.update(uiState.value.calibration)
        }
    }

    /**
     * 测试 移动到废液槽
     */
    private fun toWasteTank() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settingState.value.motionMotor
            val calibration = uiState.value.calibration
            val commandBlock = listOf(
                CommandBlock.Hex(
                    SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                        motionMotor.toMultiPointHex(calibration.wasteTankPosition, 0f)
                    )
                ),
                CommandBlock.Hex(SerialPortEnum.SERIAL_TWO, Command.multiPoint("0,0,0,")),
                CommandBlock.Hex(SerialPortEnum.SERIAL_THREE, Command.multiPoint("0,0,0,")),
            )
            SerialPortManager.instance.commandQueue.enqueue(commandBlock)
        }
    }

    /**
     * 测试 废液槽针头下降
     */
    private fun wasteTankNeedleDown() {
        val motionMotor = appViewModel.settingState.value.motionMotor
        val calibration = uiState.value.calibration
        val commandBlock = listOf(
            CommandBlock.Hex(
                SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                    motionMotor.toMultiPointHex(calibration.wasteTankPosition, 0f) +
                            motionMotor.toMultiPointHex(
                                calibration.wasteTankPosition,
                                calibration.wasteTankHeight
                            )
                )
            ),
            CommandBlock.Hex(SerialPortEnum.SERIAL_TWO, Command.multiPoint("0,0,0,0,0,0,")),
            CommandBlock.Hex(SerialPortEnum.SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,")),
        )
        SerialPortManager.instance.commandQueue.enqueue(commandBlock)
    }

    /**
     * 测试 废液槽针头上升
     */
    private fun wasteTankNeedleUp() {
        val motionMotor = appViewModel.settingState.value.motionMotor
        val calibration = uiState.value.calibration
        val commandBlock = listOf(
            CommandBlock.Hex(
                SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                    motionMotor.toMultiPointHex(calibration.wasteTankPosition, 0f) +
                            motionMotor.toMultiPointHex(0f, 0f)
                )
            ),
            CommandBlock.Hex(SerialPortEnum.SERIAL_TWO, Command.multiPoint("0,0,0,0,0,0,")),
            CommandBlock.Hex(SerialPortEnum.SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,")),
        )
        SerialPortManager.instance.commandQueue.enqueue(commandBlock)
    }

    /**
     * 测试 移动到洗液槽
     */
    private fun toWashTank() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settingState.value.motionMotor
            val calibration = uiState.value.calibration
            val commandBlock = listOf(
                CommandBlock.Hex(
                    SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                        motionMotor.toMultiPointHex(calibration.washTankPosition, 0f)
                    )
                ),
                CommandBlock.Hex(SerialPortEnum.SERIAL_TWO, Command.multiPoint("0,0,0,")),
                CommandBlock.Hex(SerialPortEnum.SERIAL_THREE, Command.multiPoint("0,0,0,")),
            )
            SerialPortManager.instance.commandQueue.enqueue(commandBlock)
        }
    }

    /**
     * 测试 洗液槽针头下降
     */
    private fun washTankNeedleDown() {
        val motionMotor = appViewModel.settingState.value.motionMotor
        val calibration = uiState.value.calibration
        val commandBlock = listOf(
            CommandBlock.Hex(
                SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                    motionMotor.toMultiPointHex(calibration.washTankPosition, 0f) +
                            motionMotor.toMultiPointHex(
                                calibration.washTankPosition,
                                calibration.washTankHeight
                            )
                )
            ),
            CommandBlock.Hex(SerialPortEnum.SERIAL_TWO, Command.multiPoint("0,0,0,0,0,0,")),
            CommandBlock.Hex(SerialPortEnum.SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,")),
        )
        SerialPortManager.instance.commandQueue.enqueue(commandBlock)
    }

    /**
     * 测试 洗液槽针头上升
     */
    private fun washTankNeedleUp() {
        val motionMotor = appViewModel.settingState.value.motionMotor
        val calibration = uiState.value.calibration
        val commandBlock = listOf(
            CommandBlock.Hex(
                SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                    motionMotor.toMultiPointHex(calibration.washTankPosition, 0f) +
                            motionMotor.toMultiPointHex(0f, 0f)
                )
            ),
            CommandBlock.Hex(SerialPortEnum.SERIAL_TWO, Command.multiPoint("0,0,0,0,0,0,")),
            CommandBlock.Hex(SerialPortEnum.SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,")),
        )
        SerialPortManager.instance.commandQueue.enqueue(commandBlock)
    }

    /**
     * 测试 移动到阻断液槽
     */
    private fun toBlockingLiquidTank() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settingState.value.motionMotor
            val calibration = uiState.value.calibration
            val commandBlock = listOf(
                CommandBlock.Hex(
                    SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                        motionMotor.toMultiPointHex(calibration.blockingLiquidTankPosition, 0f)
                    )
                ),
                CommandBlock.Hex(SerialPortEnum.SERIAL_TWO, Command.multiPoint("0,0,0,")),
                CommandBlock.Hex(SerialPortEnum.SERIAL_THREE, Command.multiPoint("0,0,0,")),
            )
            SerialPortManager.instance.commandQueue.enqueue(commandBlock)
        }
    }

    /**
     * 测试 阻断液槽针头下降
     */
    private fun blockingLiquidTankNeedleDown() {
        val motionMotor = appViewModel.settingState.value.motionMotor
        val calibration = uiState.value.calibration
        val commandBlock = listOf(
            CommandBlock.Hex(
                SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                    motionMotor.toMultiPointHex(calibration.blockingLiquidTankPosition, 0f) +
                            motionMotor.toMultiPointHex(
                                calibration.blockingLiquidTankPosition,
                                calibration.blockingLiquidTankHeight
                            )
                )
            ),
            CommandBlock.Hex(SerialPortEnum.SERIAL_TWO, Command.multiPoint("0,0,0,0,0,0,")),
            CommandBlock.Hex(SerialPortEnum.SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,")),
        )
        SerialPortManager.instance.commandQueue.enqueue(commandBlock)
    }

    /**
     * 测试 阻断液槽针头上升
     */
    private fun blockingLiquidTankNeedleUp() {
        val motionMotor = appViewModel.settingState.value.motionMotor
        val calibration = uiState.value.calibration
        val commandBlock = listOf(
            CommandBlock.Hex(
                SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                    motionMotor.toMultiPointHex(calibration.blockingLiquidTankPosition, 0f) +
                            motionMotor.toMultiPointHex(0f, 0f)
                )
            ),
            CommandBlock.Hex(SerialPortEnum.SERIAL_TWO, Command.multiPoint("0,0,0,0,0,0,")),
            CommandBlock.Hex(SerialPortEnum.SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,")),
        )
        SerialPortManager.instance.commandQueue.enqueue(commandBlock)
    }

    /**
     * 测试 移动到抗体一槽
     */
    private fun toAntibodyOneTank() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settingState.value.motionMotor
            val calibration = uiState.value.calibration
            val commandBlock = listOf(
                CommandBlock.Hex(
                    SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                        motionMotor.toMultiPointHex(calibration.antibodyOneTankPosition, 0f)
                    )
                ),
                CommandBlock.Hex(SerialPortEnum.SERIAL_TWO, Command.multiPoint("0,0,0,")),
                CommandBlock.Hex(SerialPortEnum.SERIAL_THREE, Command.multiPoint("0,0,0,")),
            )
            SerialPortManager.instance.commandQueue.enqueue(commandBlock)
        }
    }

    /**
     * 测试 抗体一槽针头下降
     */
    private fun antibodyOneTankNeedleDown() {
        val motionMotor = appViewModel.settingState.value.motionMotor
        val calibration = uiState.value.calibration
        val commandBlock = listOf(
            CommandBlock.Hex(
                SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                    motionMotor.toMultiPointHex(calibration.antibodyOneTankPosition, 0f) +
                            motionMotor.toMultiPointHex(
                                calibration.antibodyOneTankPosition,
                                calibration.antibodyOneTankHeight
                            )
                )
            ),
            CommandBlock.Hex(SerialPortEnum.SERIAL_TWO, Command.multiPoint("0,0,0,0,0,0,")),
            CommandBlock.Hex(SerialPortEnum.SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,")),
            CommandBlock.Delay(3000),
            CommandBlock.Hex(
                SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                    motionMotor.toMultiPointHex(
                        calibration.antibodyOneTankPosition,
                        calibration.recycleAntibodyOneTankHeight
                    )
                )
            ),
            CommandBlock.Hex(SerialPortEnum.SERIAL_TWO, Command.multiPoint("0,0,0,")),
            CommandBlock.Hex(SerialPortEnum.SERIAL_THREE, Command.multiPoint("0,0,0,")),
        )
        SerialPortManager.instance.commandQueue.enqueue(commandBlock)
    }

    /**
     * 测试 抗体一槽针头上升
     */
    private fun antibodyOneTankNeedleUp() {
        val motionMotor = appViewModel.settingState.value.motionMotor
        val calibration = uiState.value.calibration
        val commandBlock = listOf(
            CommandBlock.Hex(
                SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                    motionMotor.toMultiPointHex(calibration.antibodyOneTankPosition, 0f) +
                            motionMotor.toMultiPointHex(0f, 0f)
                )
            ),
            CommandBlock.Hex(SerialPortEnum.SERIAL_TWO, Command.multiPoint("0,0,0,0,0,0,")),
            CommandBlock.Hex(SerialPortEnum.SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,")),
        )
        SerialPortManager.instance.commandQueue.enqueue(commandBlock)
    }

    /**
     * 测试 移动到抗体二槽
     */
    private fun toAntibodyTwoTank() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settingState.value.motionMotor
            val calibration = uiState.value.calibration
            val commandBlock = listOf(
                CommandBlock.Hex(
                    SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                        motionMotor.toMultiPointHex(calibration.antibodyTwoTankPosition, 0f)
                    )
                ),
                CommandBlock.Hex(SerialPortEnum.SERIAL_TWO, Command.multiPoint("0,0,0,")),
                CommandBlock.Hex(SerialPortEnum.SERIAL_THREE, Command.multiPoint("0,0,0,")),
            )
            SerialPortManager.instance.commandQueue.enqueue(commandBlock)
        }
    }

    /**
     * 测试 抗体二槽针头下降
     */
    private fun antibodyTwoTankNeedleDown() {
        val motionMotor = appViewModel.settingState.value.motionMotor
        val calibration = uiState.value.calibration
        val commandBlock = listOf(
            CommandBlock.Hex(
                SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                    motionMotor.toMultiPointHex(calibration.antibodyTwoTankPosition, 0f) +
                            motionMotor.toMultiPointHex(
                                calibration.antibodyTwoTankPosition,
                                calibration.antibodyTwoTankHeight
                            )
                )
            ),
            CommandBlock.Hex(SerialPortEnum.SERIAL_TWO, Command.multiPoint("0,0,0,0,0,0,")),
            CommandBlock.Hex(SerialPortEnum.SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,")),
        )
        SerialPortManager.instance.commandQueue.enqueue(commandBlock)
    }

    /**
     * 测试 抗体二槽针头上升
     */
    private fun antibodyTwoTankNeedleUp() {
        val motionMotor = appViewModel.settingState.value.motionMotor
        val calibration = uiState.value.calibration
        val commandBlock = listOf(
            CommandBlock.Hex(
                SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                    motionMotor.toMultiPointHex(calibration.antibodyTwoTankPosition, 0f) +
                            motionMotor.toMultiPointHex(0f, 0f)
                )
            ),
            CommandBlock.Hex(SerialPortEnum.SERIAL_TWO, Command.multiPoint("0,0,0,0,0,0,")),
            CommandBlock.Hex(SerialPortEnum.SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,")),
        )
        SerialPortManager.instance.commandQueue.enqueue(commandBlock)
    }

    /**
     * 测试 回到原点
     */
    private fun toZeroPosition() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settingState.value.motionMotor
            val commandBlock = listOf(
                CommandBlock.Hex(
                    SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                        motionMotor.toMultiPointHex(0f, 0f)
                    )
                ),
                CommandBlock.Hex(SerialPortEnum.SERIAL_TWO, Command.multiPoint("0,0,0,")),
                CommandBlock.Hex(SerialPortEnum.SERIAL_THREE, Command.multiPoint("0,0,0,")),
            )
            SerialPortManager.instance.commandQueue.enqueue(commandBlock)
        }
    }

    /**
     * 测试 吸液
     */
    private fun aspirate() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settingState.value.motionMotor
            val commandBlock = listOf(
                CommandBlock.Hex(
                    SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                        motionMotor.toMultiPointHex(0f, 0f)
                    )
                ),
                CommandBlock.Hex(SerialPortEnum.SERIAL_TWO, Command.multiPoint("3200,3200,3200,")),
                CommandBlock.Hex(SerialPortEnum.SERIAL_THREE, Command.multiPoint("3200,3200,0,")),
            )
            SerialPortManager.instance.commandQueue.enqueue(commandBlock)
        }
    }

    /**
     * 测试 排液
     */
    private fun drainage() {
        viewModelScope.launch {
            val motionMotor = appViewModel.settingState.value.motionMotor
            val commandBlock = listOf(
                CommandBlock.Hex(
                    SerialPortEnum.SERIAL_ONE, Command.multiPoint(
                        motionMotor.toMultiPointHex(0f, 0f)
                    )
                ),
                CommandBlock.Hex(
                    SerialPortEnum.SERIAL_TWO,
                    Command.multiPoint("-32000,-32000,-32000,")
                ),
                CommandBlock.Hex(
                    SerialPortEnum.SERIAL_THREE,
                    Command.multiPoint("-32000,-32000,0,")
                ),
            )
            SerialPortManager.instance.commandQueue.enqueue(commandBlock)
        }
    }
}

sealed class CalibrationIntent {
    data class OnCalibrationValueChange(val calibration: Calibration) : CalibrationIntent()
    object OnUpdateCalibration : CalibrationIntent()
    object ToWasteTank : CalibrationIntent()
    object WasteTankNeedleDown : CalibrationIntent()
    object WasteTankNeedleUp : CalibrationIntent()
    object ToWashTank : CalibrationIntent()
    object WashTankNeedleDown : CalibrationIntent()
    object WashTankNeedleUp : CalibrationIntent()
    object ToBlockingLiquidTank : CalibrationIntent()
    object BlockingLiquidTankNeedleDown : CalibrationIntent()
    object BlockingLiquidTankNeedleUp : CalibrationIntent()
    object ToAntibodyOneTank : CalibrationIntent()
    object AntibodyOneTankNeedleDown : CalibrationIntent()
    object AntibodyOneTankNeedleUp : CalibrationIntent()
    object ToAntibodyTwoTank : CalibrationIntent()
    object AntibodyTwoTankNeedleDown : CalibrationIntent()
    object AntibodyTwoTankNeedleUp : CalibrationIntent()
    object ToZeroPosition : CalibrationIntent()
    object Aspirate : CalibrationIntent()
    object Drainage : CalibrationIntent()
}

sealed class CalibrationState {
    data class OnCalibrationValueChange(val calibration: Calibration) : CalibrationState()
}

data class CalibrationUiState(
    var calibration: Calibration = Calibration()
)

