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
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.extension.removeZero
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.databinding.FragmentCalibrationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CalibrationFragment :
    BaseFragment<CalibrationViewModel, FragmentCalibrationBinding>(R.layout.fragment_calibration) {

    override val viewModel: CalibrationViewModel by viewModels()

    @Inject
    lateinit var appViewModel: AppViewModel

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initObserver()
        initButton()
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
                val cali = Calibration()
                viewModel.updateCalibration(
                    cali.copy(
                        wasteY = binding.wasteTankPosition.text.toString().toFloat(),
                        wasteZ = binding.wasteTankHeight.text.toString().toFloat(),
                        washingY = binding.washTankPosition.text.toString().toFloat(),
                        washingZ = binding.washTankHeight.text.toString().toFloat(),
                        blockingY = binding.blockingLiquidTankPosition.text.toString().toFloat(),
                        blockingZ = binding.blockingLiquidTankHeight.text.toString().toFloat(),
                        antibodyOneY = binding.antibodyOneTankPosition.text.toString().toFloat(),
                        antibodyOneZ = binding.antibodyOneTankHeight.text.toString().toFloat(),
                        recycleAntibodyOneZ = binding.recycleAntibodyOneTankHeight.text.toString().toFloat(),
                        antibodyTwoY = binding.antibodyTwoTankPosition.text.toString().toFloat(),
                        antibodyTwoZ = binding.antibodyTwoTankHeight.text.toString().toFloat(),
                    )
                )
                viewModel.updateMotorValue(
                    appViewModel.settings.value.motorUnits.y.copy(
                        unit = binding.yMotorDistance.text.toString().toFloat()
                    )
                )
                viewModel.updateMotorValue(
                    appViewModel.settings.value.motorUnits.z.copy(
                        unit = binding.zMotorDistance.text.toString().toFloat()
                    )
                )
                viewModel.updateMotorValue(
                    appViewModel.settings.value.motorUnits.p1.copy(
                        unit = binding.pumpOneDistance.text.toString().toFloat()
                    )
                )
                viewModel.updateMotorValue(
                    appViewModel.settings.value.motorUnits.p2.copy(
                        unit = binding.pumpTwoDistance.text.toString().toFloat()
                    )
                )
                viewModel.updateMotorValue(
                    appViewModel.settings.value.motorUnits.p3.copy(
                        unit = binding.pumpThreeDistance.text.toString().toFloat()
                    )
                )
                viewModel.updateMotorValue(
                    appViewModel.settings.value.motorUnits.p4.copy(
                        unit = binding.pumpFourDistance.text.toString().toFloat()
                    )
                )
                viewModel.updateMotorValue(
                    appViewModel.settings.value.motorUnits.p5.copy(
                        unit = binding.pumpFiveDistance.text.toString().toFloat()
                    )
                )
                PopTip.show("更新成功")
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
     * 校准数据变化
     * @param calibration [Calibration] 校准数据
     */
    private fun onCalibrationValueChange(calibration: Calibration) {
        val motorUnits = appViewModel.settings.value.motorUnits
        binding.run {
            wasteTankPosition.setText(calibration.wasteY.toString().removeZero())
            wasteTankHeight.setText(calibration.wasteZ.toString().removeZero())
            washTankPosition.setText(calibration.washingY.toString().removeZero())
            washTankHeight.setText(calibration.washingZ.toString().removeZero())
            blockingLiquidTankPosition.setText(calibration.blockingY.toString().removeZero())
            blockingLiquidTankHeight.setText(calibration.blockingZ.toString().removeZero())
            antibodyOneTankPosition.setText(calibration.antibodyOneY.toString().removeZero())
            antibodyOneTankHeight.setText(calibration.antibodyOneZ.toString().removeZero())
            recycleAntibodyOneTankHeight.setText(calibration.recycleAntibodyOneZ.toString().removeZero())
            antibodyTwoTankPosition.setText(calibration.antibodyTwoZ.toString().removeZero())
            antibodyTwoTankHeight.setText(calibration.antibodyTwoZ.toString().removeZero())
            yMotorDistance.setText(motorUnits.y.unit.toString().removeZero())
            zMotorDistance.setText(motorUnits.z.unit.toString().removeZero())
            pumpOneDistance.setText(motorUnits.p1.unit.toString().removeZero())
            pumpTwoDistance.setText(motorUnits.p2.unit.toString().removeZero())
            pumpThreeDistance.setText(motorUnits.p3.unit.toString().removeZero())
            pumpFourDistance.setText(motorUnits.p4.unit.toString().removeZero())
            pumpFiveDistance.setText(motorUnits.p5.unit.toString().removeZero())
            drainDistance.setText(calibration.extract.toString().removeZero())
        }
    }
}