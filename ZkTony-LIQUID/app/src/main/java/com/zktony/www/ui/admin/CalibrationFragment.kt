package com.zktony.www.ui.admin

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.calibration.collect {
                    onCalibrationValueChange(it)
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
                setText(calibration.wasteY.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            wasteY = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            wasteTankHeight.run {
                setText(calibration.wasteZ.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            wasteZ = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            washTankPosition.run {
                setText(calibration.washingY.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            washingY = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            washTankHeight.run {
                setText(calibration.washingZ.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            washingZ = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            blockingLiquidTankPosition.run {
                setText(calibration.blockingY.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            blockingY = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            blockingLiquidTankHeight.run {
                setText(calibration.blockingZ.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            blockingZ = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            antibodyOneTankPosition.run {
                setText(calibration.antibodyOneY.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            antibodyOneY = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            antibodyOneTankHeight.run {
                setText(calibration.antibodyOneZ.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            antibodyOneZ = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            recycleAntibodyOneTankHeight.run {
                setText(calibration.recycleAntibodyOneZ.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            recycleAntibodyOneZ = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            antibodyTwoTankPosition.run {
                setText(calibration.antibodyTwoY.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            antibodyTwoY = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            antibodyTwoTankHeight.run {
                setText(calibration.antibodyTwoZ.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            antibodyTwoZ = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            yMotorDistance.run {
                setText(calibration.distanceY.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            distanceY = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            zMotorDistance.run {
                setText(calibration.distanceZ.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            distanceZ = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            pumpOneDistance.run {
                setText(calibration.volumeOne.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            volumeOne = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            pumpTwoDistance.run {
                setText(calibration.volumeTwo.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            volumeTwo = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            pumpThreeDistance.run {
                setText(calibration.volumeThree.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            volumeThree = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            pumpFourDistance.run {
                setText(calibration.volumeFour.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            volumeFour = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            pumpFiveDistance.run {
                setText(calibration.volumeFive.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            volumeFive = if (it.isNotEmpty()) it.toFloat() else 0f
                        )
                    )
                }
            }
            drainDistance.run {
                setText(calibration.extract.toString().removeZero())
                afterTextChange {
                    viewModel.calibrationValueChange(
                        viewModel.editCalibration.value.copy(
                            extract = if (it.isNotEmpty()) it.toFloat() else 0f
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
                val str = calibration.wasteY.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            wasteTankHeight.run {
                val str = calibration.wasteZ.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            washTankPosition.run {
                val str = calibration.washingY.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            washTankHeight.run {
                val str = calibration.washingZ.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            blockingLiquidTankPosition.run {
                val str = calibration.blockingY.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            blockingLiquidTankHeight.run {
                val str = calibration.blockingZ.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            antibodyOneTankPosition.run {
                val str = calibration.antibodyOneY.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            antibodyOneTankHeight.run {
                val str = calibration.antibodyOneZ.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            recycleAntibodyOneTankHeight.run {
                val str = calibration.recycleAntibodyOneZ.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            antibodyTwoTankPosition.run {
                val str = calibration.antibodyTwoY.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            antibodyTwoTankHeight.run {
                val str = calibration.antibodyTwoZ.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            yMotorDistance.run {
                val str = calibration.distanceY.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            zMotorDistance.run {
                val str = calibration.distanceZ.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            pumpOneDistance.run {
                val str = calibration.volumeOne.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            pumpTwoDistance.run {
                val str = calibration.volumeTwo.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            pumpThreeDistance.run {
                val str = calibration.volumeThree.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            pumpFourDistance.run {
                val str = calibration.volumeFour.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            pumpFiveDistance.run {
                val str = calibration.volumeFive.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
            drainDistance.run {
                val str = calibration.extract.toString().removeZero()
                if (text.toString().removeZero() != str) {
                    setText(str)
                }
            }
        }
    }
}