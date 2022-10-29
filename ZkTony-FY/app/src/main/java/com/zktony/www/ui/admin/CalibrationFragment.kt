package com.zktony.www.ui.admin

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.afterTextChange
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.extension.removeZero
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.databinding.FragmentCalibrationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CalibrationFragment :
    BaseFragment<CalibrationViewModel, FragmentCalibrationBinding>(R.layout.fragment_calibration) {

    override val viewModel: CalibrationViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initObserver()
        initButton()
        initEditText()
    }

    private fun initObserver() {
        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {
                    is CalibrationState.OnCalibrationValueChange -> onCalibrationValueChange(
                        it.calibration
                    )
                }
            }
        }
    }


    /**
     * 初始化按钮
     */
    private fun initButton() {
        binding.run {
            btnBack.run {
                clickScale()
                setOnClickListener { findNavController().navigateUp() }
            }
            btnUpdate.setOnClickListener {
                viewModel.dispatch(CalibrationIntent.OnUpdateCalibration)
            }
            btnTestWasteTankMove.setOnClickListener {
                viewModel.dispatch(CalibrationIntent.ToWasteTank)
            }
            btnTestWasteTankDown.run {
                setOnClickListener {
                    PopTip.show("请确认针头位置，避免撞针，长按开始下降！！！")
                }
                setOnLongClickListener {
                    viewModel.dispatch(CalibrationIntent.WasteTankNeedleDown)
                    true
                }
            }
            btnTestWasteTankUp.setOnClickListener {
                viewModel.dispatch(CalibrationIntent.WasteTankNeedleUp)
            }
            btnTestWashTankMove.setOnClickListener {
                viewModel.dispatch(CalibrationIntent.ToWashTank)
            }
            btnTestWashTankDown.run {
                setOnClickListener {
                    PopTip.show("请确认针头位置，避免撞针，长按开始下降！！！")
                }
                setOnLongClickListener {
                    viewModel.dispatch(CalibrationIntent.WashTankNeedleDown)
                    true
                }
            }
            btnTestWashTankUp.setOnClickListener {
                viewModel.dispatch(CalibrationIntent.WashTankNeedleUp)
            }
            btnTestBlockingLiquidTankMove.setOnClickListener {
                viewModel.dispatch(CalibrationIntent.ToBlockingLiquidTank)
            }
            btnTestBlockingLiquidTankDown.run {
                setOnClickListener {
                    PopTip.show("请确认针头位置，避免撞针，长按开始下降！！！")
                }
                setOnLongClickListener {
                    viewModel.dispatch(CalibrationIntent.BlockingLiquidTankNeedleDown)
                    true
                }
            }
            btnTestBlockingLiquidTankUp.setOnClickListener {
                viewModel.dispatch(CalibrationIntent.BlockingLiquidTankNeedleUp)
            }
            btnTestAntibodyOneTankMove.setOnClickListener {
                viewModel.dispatch(CalibrationIntent.ToAntibodyOneTank)
            }
            btnTestAntibodyOneTankDown.run {
                setOnClickListener {
                    PopTip.show("请确认针头位置，避免撞针，长按开始下降！！！")
                }
                setOnLongClickListener {
                    viewModel.dispatch(CalibrationIntent.AntibodyOneTankNeedleDown)
                    true
                }
            }
            btnTestAntibodyOneTankUp.setOnClickListener {
                viewModel.dispatch(CalibrationIntent.AntibodyOneTankNeedleUp)
            }
            btnTestAntibodyTwoTankMove.setOnClickListener {
                viewModel.dispatch(CalibrationIntent.ToAntibodyTwoTank)
            }
            btnTestAntibodyTwoTankDown.run {
                setOnClickListener {
                    PopTip.show("请确认针头位置，避免撞针，长按开始下降！！！")
                }
                setOnLongClickListener {
                    viewModel.dispatch(CalibrationIntent.AntibodyTwoTankNeedleDown)
                    true
                }
            }
            btnTestAntibodyTwoTankUp.setOnClickListener {
                viewModel.dispatch(CalibrationIntent.AntibodyTwoTankNeedleUp)
            }
            btnTestZeroMove.setOnClickListener {
                viewModel.dispatch(CalibrationIntent.ToZeroPosition)
            }
            btnTestAspirate.setOnClickListener {
                viewModel.dispatch(CalibrationIntent.Aspirate)
            }
            btnTestDrainage.setOnClickListener {
                viewModel.dispatch(CalibrationIntent.Drainage)
            }
        }
    }

    /**
     * 初始化编辑框
     */
    private fun initEditText() {
        val calibration = viewModel.uiState.value.calibration
        binding.run {
            wasteTankPosition.run {
                setText(calibration.wasteTankPosition.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                wasteTankPosition = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
            wasteTankHeight.run {
                setText(calibration.wasteTankHeight.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                wasteTankHeight = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
            washTankPosition.run {
                setText(calibration.washTankPosition.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                washTankPosition = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
            washTankHeight.run {
                setText(calibration.washTankHeight.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                washTankHeight = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
            blockingLiquidTankPosition.run {
                setText(calibration.blockingLiquidTankPosition.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                blockingLiquidTankPosition = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
            blockingLiquidTankHeight.run {
                setText(calibration.blockingLiquidTankHeight.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                blockingLiquidTankHeight = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
            antibodyOneTankPosition.run {
                setText(calibration.antibodyOneTankPosition.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                antibodyOneTankPosition = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
            antibodyOneTankHeight.run {
                setText(calibration.antibodyOneTankHeight.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                antibodyOneTankHeight = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
            recycleAntibodyOneTankHeight.run {
                setText(calibration.recycleAntibodyOneTankHeight.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                recycleAntibodyOneTankHeight = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
            antibodyTwoTankPosition.run {
                setText(calibration.antibodyTwoTankPosition.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                antibodyTwoTankPosition = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
            antibodyTwoTankHeight.run {
                setText(calibration.antibodyTwoTankHeight.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                antibodyTwoTankHeight = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
            yMotorDistance.run {
                setText(calibration.yMotorDistance.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                yMotorDistance = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
            zMotorDistance.run {
                setText(calibration.zMotorDistance.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                zMotorDistance = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
            pumpOneDistance.run {
                setText(calibration.pumpOneDistance.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                pumpOneDistance = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
            pumpTwoDistance.run {
                setText(calibration.pumpTwoDistance.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                pumpTwoDistance = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
            pumpThreeDistance.run {
                setText(calibration.pumpThreeDistance.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                pumpThreeDistance = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
            pumpFourDistance.run {
                setText(calibration.pumpFourDistance.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                pumpFourDistance = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
            pumpFiveDistance.run {
                setText(calibration.pumpFiveDistance.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                pumpFiveDistance = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
            drainDistance.run {
                setText(calibration.drainDistance.toString().removeZero())
                afterTextChange {
                    viewModel.dispatch(
                        CalibrationIntent.OnCalibrationValueChange(
                            viewModel.uiState.value.calibration.copy(
                                drainDistance = if (it.isNotEmpty()) it.toFloat() else 0f
                            )
                        )
                    )
                }
            }
        }
    }

    /**
     * 校准数据变化
     * @param calibration [Calibration] 校准数据
     */
    private fun onCalibrationValueChange(calibration: Calibration) {
        binding.run {
            wasteTankPosition.run {
                val str = calibration.wasteTankPosition.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
            wasteTankHeight.run {
                val str = calibration.wasteTankHeight.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
            washTankPosition.run {
                val str = calibration.washTankPosition.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
            washTankHeight.run {
                val str = calibration.washTankHeight.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
            blockingLiquidTankPosition.run {
                val str = calibration.blockingLiquidTankPosition.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
            blockingLiquidTankHeight.run {
                val str = calibration.blockingLiquidTankHeight.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
            antibodyOneTankPosition.run {
                val str = calibration.antibodyOneTankPosition.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
            antibodyOneTankHeight.run {
                val str = calibration.antibodyOneTankHeight.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
            recycleAntibodyOneTankHeight.run {
                val str = calibration.recycleAntibodyOneTankHeight.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
            antibodyTwoTankPosition.run {
                val str = calibration.antibodyTwoTankPosition.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
            antibodyTwoTankHeight.run {
                val str = calibration.antibodyTwoTankHeight.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
            yMotorDistance.run {
                val str = calibration.yMotorDistance.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
            zMotorDistance.run {
                val str = calibration.zMotorDistance.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
            pumpOneDistance.run {
                val str = calibration.pumpOneDistance.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
            pumpTwoDistance.run {
                val str = calibration.pumpTwoDistance.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
            pumpThreeDistance.run {
                val str = calibration.pumpThreeDistance.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
            pumpFourDistance.run {
                val str = calibration.pumpFourDistance.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
            pumpFiveDistance.run {
                val str = calibration.pumpFiveDistance.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
            drainDistance.run {
                val str = calibration.drainDistance.toString().removeZero()
                if (text.toString() != str) {
                    setText(str)
                }
            }
        }
    }

}