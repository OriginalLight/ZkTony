package com.zktony.www.ui.admin

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.www.R
import com.zktony.www.adapter.MotorAdapter
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.afterTextChange
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.room.entity.Motor
import com.zktony.www.databinding.FragmentMotorSettingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MotorSettingFragment :
    BaseFragment<MotorSettingViewModel, FragmentMotorSettingBinding>(R.layout.fragment_motor_setting) {

    override val viewModel: MotorSettingViewModel by viewModels()

    private val motorAdapter by lazy { MotorAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initObserver()
        initRecyclerView()
        initButton()
        initEditText()
    }

    /**
     * 初始化观察者
     */
    private fun initObserver() {
        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {
                    is MotorSettingState.OnDataBaseChange -> onDataBaseChange(it.motorList)
                    is MotorSettingState.OnUpdateMessage -> onUpdateMessage(it.message)
                    is MotorSettingState.OnMotorValueChange -> onMotorValueChange(it.motor)
                }
            }
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        binding.rcMotor.adapter = motorAdapter
        motorAdapter.setOnEditButtonClick { motor ->
            viewModel.editMotor(motor)
        }
        viewModel.initMotors()
    }

    /**
     * 初始化按钮
     */
    private fun initButton() {
        binding.btnBack.run {
            this.clickScale()
            this.setOnClickListener {
                findNavController().navigateUp()
            }
        }
        binding.btnMode.setOnClickListener {
            val mode = arrayOf("增量模式", "坐标模式")
            BottomMenu.show(mode)
                .setOnMenuItemClickListener { _, text, index ->
                    binding.btnMode.text = text
                    viewModel.motorValueChange(
                        viewModel.uiState.value.motor.copy(
                            mode = index
                        )
                    )
                    false
                }
        }
        binding.btnSubdivision.setOnClickListener {
            val subdivision = arrayOf("2", "4", "8", "16", "32")
            BottomMenu.show(subdivision)
                .setOnMenuItemClickListener { _, text, _ ->
                    binding.btnSubdivision.text = text
                    viewModel.motorValueChange(
                        viewModel.uiState.value.motor.copy(
                            subdivision = text.toString().toInt()
                        )
                    )
                    false
                }
        }
        binding.btnUpdate.setOnClickListener {
            viewModel.updateMotor()
        }
    }

    /**
     * 初始化EditText
     */
    private fun initEditText() {
        binding.speed.afterTextChange {
            viewModel.motorValueChange(
                viewModel.uiState.value.motor.copy(
                    speed = if (it.isNotEmpty()) {
                        it.toInt()
                    } else {
                        0
                    }
                )
            )
        }
        binding.acceleration.afterTextChange {
            viewModel.motorValueChange(
                viewModel.uiState.value.motor.copy(
                    acceleration = if (it.isNotEmpty()) {
                        it.toInt()
                    } else {
                        0
                    }
                )
            )
        }
        binding.deceleration.afterTextChange {
            viewModel.motorValueChange(
                viewModel.uiState.value.motor.copy(
                    deceleration = if (it.isNotEmpty()) {
                        it.toInt()
                    } else {
                        0
                    }
                )
            )
        }
        binding.waitTime.afterTextChange {
            viewModel.motorValueChange(
                viewModel.uiState.value.motor.copy(
                    waitTime = if (it.isNotEmpty()) {
                        it.toInt()
                    } else {
                        0
                    }
                )
            )
        }
    }

    /**
     * 更新电机列表
     * @param motorList [List]<[Motor]>
     */
    private fun onDataBaseChange(motorList: List<Motor>) {
        motorAdapter.submitList(motorList)
    }

    /**
     * 更新电机值
     * @param motor [Motor] 电机
     */
    private fun onMotorValueChange(motor: Motor) {
        binding.run {
            tvTitle.text = motor.name
            btnMode.text = if (motor.mode == 0) "增量模式" else "坐标模式"
            btnSubdivision.text = motor.subdivision.toString()
            speed.setText(motor.speed.toString())
            acceleration.setText(motor.acceleration.toString())
            deceleration.setText(motor.deceleration.toString())
            waitTime.setText(motor.waitTime.toString())
        }
    }

    /**
     * 更新电机回调信息
     * @param message [String]
     */
    private fun onUpdateMessage(message: String) {
        PopTip.show(message)
    }
}