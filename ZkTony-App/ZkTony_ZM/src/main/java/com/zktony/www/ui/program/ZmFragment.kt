package com.zktony.www.ui.program

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
import com.zktony.core.utils.Constants.MAX_MOTOR
import com.zktony.core.utils.Constants.MAX_TIME
import com.zktony.core.utils.Constants.MAX_VOLTAGE_ZM
import com.zktony.www.R
import com.zktony.www.databinding.FragmentZmBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ZmFragment : BaseFragment<ZmViewModel, FragmentZmBinding>(R.layout.fragment_zm) {

    override val viewModel: ZmViewModel by viewModel()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    /**
     * 初始化Flow观察者
     */
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    if (it.name.isNotEmpty()
                        && it.danbaiName.isNotEmpty()
                        && it.danbaiMin > 0f
                        && it.danbaiMax > 0f
                        && ((it.jiaoKind == 0 && it.jiaoNormalSize > 0f) || (it.jiaoKind == 1 && it.jiaoMax > 0f && it.jiaoMin > 0f))
                        && it.jiaoHoudu.isNotEmpty()
                        && it.waterKind.isNotEmpty()
                        && it.voltage > 0f
                        && it.motor > 0f
                        && it.time > 0f
                    ) {
                        binding.save.visibility = View.VISIBLE
                    } else {
                        binding.save.visibility = View.GONE
                    }
                    binding.apply {
                        name.setEqualText(it.name)
                        danbaiName.setEqualText(it.danbaiName)
                        if (it.danbaiMin > 0f) {
                            danbaiMin.setEqualText(it.danbaiMin.toString().removeZero())
                        }
                        if (it.danbaiMax > 0f) {
                            danbaiMax.setEqualText(it.danbaiMax.toString().removeZero())
                        }
                        if (it.jiaoKind == 0) {
                            radioJiaoNormal.isChecked = true
                            radioJiaoSpecial.isChecked = false
                            jiaoNormal.visibility = View.VISIBLE
                            jiaoMaxMin.visibility = View.GONE
                            if (it.jiaoNormalSize > 0f) {
                                jiaoNormalSize.setEqualText(
                                    it.jiaoNormalSize.toString().removeZero()
                                )
                            }
                        }
                        if (it.jiaoKind == 1) {
                            radioJiaoNormal.isChecked = false
                            radioJiaoSpecial.isChecked = true
                            jiaoNormal.visibility = View.GONE
                            jiaoMaxMin.visibility = View.VISIBLE
                            if (it.jiaoMax > 0f) {
                                jiaoMax.setEqualText(it.jiaoMax.toString().removeZero())
                            }
                            if (it.jiaoMin > 0f) {
                                jiaoMin.setEqualText(it.jiaoMin.toString().removeZero())
                            }
                        }
                        if (it.jiaoHoudu == "0.75") {
                            radioJiao075.isChecked = true
                        }
                        if (it.jiaoHoudu == "1.0") {
                            radioJiao10.isChecked = true
                        }
                        if (it.jiaoHoudu == "1.5") {
                            radioJiao15.isChecked = true
                        }
                        if (it.waterKind == "厂家") {
                            factory.isChecked = true
                            otherWaterInfo.visibility = View.GONE
                        } else {
                            other.isChecked = true
                            otherWaterInfo.visibility = View.VISIBLE
                            otherWater.setEqualText(it.waterKind)
                        }
                        if (it.voltage > 0f) {
                            voltage.setEqualText(it.voltage.toString().removeZero())
                        }
                        if (it.motor > 0f) {
                            motor.setEqualText(it.motor.toString().removeZero())
                        }
                        if (it.time > 0f) {
                            time.setEqualText(it.time.toString().removeZero())
                        }
                        if (it.name.isNotEmpty()
                            && it.danbaiName.isNotEmpty()
                            && it.danbaiMin > 0f
                            && it.danbaiMax > 0f
                            && ((it.jiaoKind == 0 && it.jiaoNormalSize > 0f) || (it.jiaoKind == 1 && it.jiaoMax > 0f && it.jiaoMin > 0f))
                            && it.jiaoHoudu.isNotEmpty()
                            && it.waterKind.isNotEmpty()
                            && it.voltage > 0f
                            && it.motor > 0f
                            && it.time > 0f
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

    /**
     * 初始化程序拿到传过来的id
     * 如果不是‘None’就是修改不然是添加
     */
    private fun initView() {
        arguments?.let {
            val id = it.getString("id") ?: ""
            if (id.isNotEmpty()) {
                viewModel.load(id)
            }
        }
        binding.apply {
            cancel.clickNoRepeat {
                findNavController().navigateUp()
            }
            save.clickNoRepeat {
                viewModel.save {
                    PopTip.show("保存成功")
                    findNavController().navigateUp()
                }
            }
            with(back) {
                clickScale()
                clickNoRepeat {
                    findNavController().navigateUp()
                }
            }
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