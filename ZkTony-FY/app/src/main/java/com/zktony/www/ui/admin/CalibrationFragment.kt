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
            viewModel.calibration.collect {
                onCalibrationValueChange(it)
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
                viewModel.updateCalibration()
            }
            btnTestWasteTankMove.setOnClickListener {
                viewModel.toWasteTank()
            }
            btnTestWasteTankDown.run {
                setOnClickListener {
                    PopTip.show("请确认针头位置，避免撞针，长按开始下降！！！")
                }
                setOnLongClickListener {
                    viewModel.wasteTankNeedleDown()
                    true
                }
            }
            btnTestWasteTankUp.setOnClickListener {
                viewModel.wasteTankNeedleUp()
            }
            btnTestWashTankMove.setOnClickListener {
                viewModel.toWashTank()
            }
            btnTestWashTankDown.run {
                setOnClickListener {
                    PopTip.show("请确认针头位置，避免撞针，长按开始下降！！！")
                }
                setOnLongClickListener {
                    viewModel.washTankNeedleDown()
                    true
                }
            }
            btnTestWashTankUp.setOnClickListener {
                viewModel.washTankNeedleUp()
            }
            btnTestBlockingLiquidTankMove.setOnClickListener {
                viewModel.toBlockingLiquidTank()
            }
            btnTestBlockingLiquidTankDown.run {
                setOnClickListener {
                    PopTip.show("请确认针头位置，避免撞针，长按开始下降！！！")
                }
                setOnLongClickListener {
                    viewModel.blockingLiquidTankNeedleDown()
                    true
                }
            }
            btnTestBlockingLiquidTankUp.setOnClickListener {
                viewModel.blockingLiquidTankNeedleUp()
            }
            btnTestAntibodyOneTankMove.setOnClickListener {
                viewModel.toAntibodyOneTank()
            }
            btnTestAntibodyOneTankDown.run {
                setOnClickListener {
                    PopTip.show("请确认针头位置，避免撞针，长按开始下降！！！")
                }
                setOnLongClickListener {
                    viewModel.antibodyOneTankNeedleDown()
                    true
                }
            }
            btnTestAntibodyOneTankUp.setOnClickListener {
                viewModel.antibodyOneTankNeedleUp()
            }
            btnTestAntibodyTwoTankMove.setOnClickListener {
                viewModel.toAntibodyTwoTank()
            }
            btnTestAntibodyTwoTankDown.run {
                setOnClickListener {
                    PopTip.show("请确认针头位置，避免撞针，长按开始下降！！！")
                }
                setOnLongClickListener {
                    viewModel.antibodyTwoTankNeedleDown()
                    true
                }
            }
            btnTestAntibodyTwoTankUp.setOnClickListener {
                viewModel.antibodyTwoTankNeedleUp()
            }
            btnTestZeroMove.setOnClickListener {
                viewModel.toZeroPosition()
            }
            btnTestAspirate.setOnClickListener {
                viewModel.aspirate()
            }
            btnTestDrainage.setOnClickListener {
                viewModel.drainage()
            }
        }
    }

    /**
     * 初始化编辑框
     */
    private fun initEditText() {
        val calibration = viewModel.calibration.value
        binding.run {
            wasteTankPosition.run {
                setText(calibration.wasteTankPosition.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            wasteTankPosition = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            wasteTankHeight.run {
                setText(calibration.wasteTankHeight.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            wasteTankHeight = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            washTankPosition.run {
                setText(calibration.washTankPosition.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            washTankPosition = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            washTankHeight.run {
                setText(calibration.washTankHeight.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            washTankHeight = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            blockingLiquidTankPosition.run {
                setText(calibration.blockingLiquidTankPosition.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            blockingLiquidTankPosition = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            blockingLiquidTankHeight.run {
                setText(calibration.blockingLiquidTankHeight.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            blockingLiquidTankHeight = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            antibodyOneTankPosition.run {
                setText(calibration.antibodyOneTankPosition.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            antibodyOneTankPosition = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            antibodyOneTankHeight.run {
                setText(calibration.antibodyOneTankHeight.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            antibodyOneTankHeight = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            recycleAntibodyOneTankHeight.run {
                setText(calibration.recycleAntibodyOneTankHeight.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            recycleAntibodyOneTankHeight = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            antibodyTwoTankPosition.run {
                setText(calibration.antibodyTwoTankPosition.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            antibodyTwoTankPosition = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            antibodyTwoTankHeight.run {
                setText(calibration.antibodyTwoTankHeight.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            antibodyTwoTankHeight = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            yMotorDistance.run {
                setText(calibration.yMotorDistance.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            yMotorDistance = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            zMotorDistance.run {
                setText(calibration.zMotorDistance.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            zMotorDistance = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            pumpOneDistance.run {
                setText(calibration.pumpOneDistance.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            pumpOneDistance = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            pumpTwoDistance.run {
                setText(calibration.pumpTwoDistance.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            pumpTwoDistance = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            pumpThreeDistance.run {
                setText(calibration.pumpThreeDistance.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            pumpThreeDistance = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            pumpFourDistance.run {
                setText(calibration.pumpFourDistance.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            pumpFourDistance = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            pumpFiveDistance.run {
                setText(calibration.pumpFiveDistance.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            pumpFiveDistance = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            drainDistance.run {
                setText(calibration.drainDistance.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            drainDistance = if (it.isNotEmpty()) it.toFloat() else 0f
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
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            wasteTankHeight.run {
                val str = calibration.wasteTankHeight.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            washTankPosition.run {
                val str = calibration.washTankPosition.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            washTankHeight.run {
                val str = calibration.washTankHeight.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            blockingLiquidTankPosition.run {
                val str = calibration.blockingLiquidTankPosition.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            blockingLiquidTankHeight.run {
                val str = calibration.blockingLiquidTankHeight.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            antibodyOneTankPosition.run {
                val str = calibration.antibodyOneTankPosition.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            antibodyOneTankHeight.run {
                val str = calibration.antibodyOneTankHeight.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            recycleAntibodyOneTankHeight.run {
                val str = calibration.recycleAntibodyOneTankHeight.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            antibodyTwoTankPosition.run {
                val str = calibration.antibodyTwoTankPosition.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            antibodyTwoTankHeight.run {
                val str = calibration.antibodyTwoTankHeight.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            yMotorDistance.run {
                val str = calibration.yMotorDistance.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            zMotorDistance.run {
                val str = calibration.zMotorDistance.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            pumpOneDistance.run {
                val str = calibration.pumpOneDistance.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            pumpTwoDistance.run {
                val str = calibration.pumpTwoDistance.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            pumpThreeDistance.run {
                val str = calibration.pumpThreeDistance.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            pumpFourDistance.run {
                val str = calibration.pumpFourDistance.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            pumpFiveDistance.run {
                val str = calibration.pumpFiveDistance.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            drainDistance.run {
                val str = calibration.drainDistance.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
        }
    }

}