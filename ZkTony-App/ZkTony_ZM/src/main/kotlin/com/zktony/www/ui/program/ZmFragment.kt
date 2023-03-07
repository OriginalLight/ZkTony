package com.zktony.www.ui.program

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseFragment
import com.zktony.common.ext.*
import com.zktony.common.utils.Constants.MAX_MOTOR
import com.zktony.common.utils.Constants.MAX_TIME
import com.zktony.common.utils.Constants.MAX_VOLTAGE_ZM
import com.zktony.www.R
import com.zktony.www.databinding.FragmentZmBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ZmFragment : BaseFragment<ZmViewModel, FragmentZmBinding>(R.layout.fragment_zm) {

    override val viewModel: ZmViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initProgram()
        initButton()
        initEditText()
        initRadioGroup()
    }

    /**
     * 初始化Flow观察者
     */
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    if (uiState.name.isNotEmpty()
                        && uiState.danbaiName.isNotEmpty()
                        && uiState.danbaiMin > 0f
                        && uiState.danbaiMax > 0f
                        && ((uiState.jiaoKind == 0 && uiState.jiaoNormalSize > 0f) || (uiState.jiaoKind == 1 && uiState.jiaoMax > 0f && uiState.jiaoMin > 0f))
                        && uiState.jiaoHoudu.isNotEmpty()
                        && uiState.waterKind.isNotEmpty()
                        && uiState.voltage > 0f
                        && uiState.motor > 0f
                        && uiState.time > 0f
                    ) {
                        binding.save.visibility = View.VISIBLE
                    } else {
                        binding.save.visibility = View.GONE
                    }
                }
            }
        }
    }

    /**
     * 初始化程序拿到传过来的id
     * 如果不是‘None’就是修改不然是添加
     */
    private fun initProgram() {
        arguments?.let {
            RsFragmentArgs.fromBundle(it).id.run {
                if (this != "None") {
                    viewModel.loadProgram(this) { uiState ->
                        binding.run {
                            name.setText(uiState.name)
                            danbaiName.setText(uiState.danbaiName)
                            danbaiMin.setText(uiState.danbaiMin.toString().removeZero())
                            danbaiMax.setText(uiState.danbaiMax.toString().removeZero())
                            if (uiState.jiaoKind == 0) {
                                radioJiaoNormal.isChecked = true
                                jiaoNormal.visibility = View.VISIBLE
                                jiaoMaxMin.visibility = View.GONE
                                jiaoNormalSize.setText(
                                    uiState.jiaoNormalSize.toString().removeZero()
                                )
                            }
                            if (uiState.jiaoKind == 1) {
                                radioJiaoNormal.isChecked = false
                                jiaoNormal.visibility = View.GONE
                                jiaoMaxMin.visibility = View.VISIBLE
                                jiaoMax.setText(uiState.jiaoMax.toString().removeZero())
                                jiaoMin.setText(uiState.jiaoMin.toString().removeZero())
                            }
                            if (uiState.jiaoHoudu == "0.75") {
                                radioJiao075.isChecked = true
                            }
                            if (uiState.jiaoHoudu == "1.0") {
                                radioJiao10.isChecked = true
                            }
                            if (uiState.jiaoHoudu == "1.5") {
                                radioJiao15.isChecked = true
                            }
                            if (uiState.waterKind == "厂家") {
                                factory.isChecked = true
                                otherWaterInfo.visibility = View.GONE
                            } else {
                                other.isChecked = true
                                otherWaterInfo.visibility = View.VISIBLE
                                otherWater.setText(uiState.waterKind)
                            }
                            voltage.setText(uiState.voltage.toString().removeZero())
                            motor.setText(uiState.motor.toString().removeZero())
                            time.setText(uiState.time.toString().removeZero())
                            if (uiState.name.isNotEmpty()
                                && uiState.danbaiName.isNotEmpty()
                                && uiState.danbaiMin > 0f
                                && uiState.danbaiMax > 0f
                                && ((uiState.jiaoKind == 0 && uiState.jiaoNormalSize > 0f) || (uiState.jiaoKind == 1 && uiState.jiaoMax > 0f && uiState.jiaoMin > 0f))
                                && uiState.jiaoHoudu.isNotEmpty()
                                && uiState.waterKind.isNotEmpty()
                                && uiState.voltage > 0f
                                && uiState.motor > 0f
                                && uiState.time > 0f
                            ) {
                                save.visibility = View.VISIBLE
                            } else {
                                save.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 初始化按钮
     */
    private fun initButton() {
        binding.back.run {
            clickScale()
            clickNoRepeat {
                findNavController().navigateUp()
            }
        }
        binding.cancel.clickNoRepeat {
            findNavController().navigateUp()
        }
        binding.save.clickNoRepeat {
            viewModel.save {
                PopTip.show("保存成功")
                findNavController().navigateUp()
            }
        }
    }

    /**
     * 初始化EditText
     */
    @SuppressLint("SetTextI18n")
    private fun initEditText() {
        binding.run {
            name.afterTextChange { viewModel.setName(it) }
            danbaiName.afterTextChange { viewModel.setDanbaiName(it) }
            danbaiMin.afterTextChange { viewModel.setDanbaiMin(it.toFloatOrNull() ?: 0f) }
            danbaiMax.afterTextChange { viewModel.setDanbaiMax(it.toFloatOrNull() ?: 0f) }
            jiaoNormalSize.afterTextChange { viewModel.setJiaoNormalSize(it.toFloatOrNull() ?: 0f) }
            jiaoMax.afterTextChange { viewModel.setJiaoMax(it.toFloatOrNull() ?: 0f) }
            jiaoMin.afterTextChange { viewModel.setJiaoMin(it.toFloatOrNull() ?: 0f) }
            otherWater.afterTextChange { viewModel.setWaterKind(it) }
            voltage.afterTextChange {
                viewModel.setVoltage(voltage = it.toFloatOrNull() ?: 0f, block = {
                    binding.voltage.setText(MAX_VOLTAGE_ZM.toString().removeZero())
                })
            }
            time.afterTextChange {
                viewModel.setTime(time = it.toFloatOrNull() ?: 0f, block = {
                    binding.time.setText(MAX_TIME.toString().removeZero())
                })
            }
            motor.afterTextChange {
                viewModel.setMotor(motor = it.toIntOrNull() ?: 0, block = {
                    binding.motor.setText(MAX_MOTOR.toString().removeZero())
                })
            }
        }
    }

    /**
     * 初始化RadioGroup
     */
    private fun initRadioGroup() {
        binding.run {
            radioGroupJiaoKind.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.radio_jiao_normal -> {
                        jiaoNormal.visibility = View.VISIBLE
                        jiaoMaxMin.visibility = View.GONE
                        viewModel.setJiaoKind(0)
                    }
                    R.id.radio_jiao_special -> {
                        jiaoNormal.visibility = View.GONE
                        jiaoMaxMin.visibility = View.VISIBLE
                        viewModel.setJiaoKind(1)
                    }
                }
            }
            radioGroupJiaoHoudu.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.radio_jiao_075 -> viewModel.setJiaoHoudu("0.75")
                    R.id.radio_jiao_10 -> viewModel.setJiaoHoudu("1.0")
                    R.id.radio_jiao_15 -> viewModel.setJiaoHoudu("1.5")
                }
            }
            radioGroupWaterKind.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.factory -> {
                        otherWaterInfo.visibility = View.GONE
                        viewModel.setWaterKind("厂家")
                    }
                    R.id.other -> {
                        otherWaterInfo.visibility = View.VISIBLE
                        viewModel.setWaterKind(otherWater.text.toString())
                    }
                }
            }
        }
    }
}