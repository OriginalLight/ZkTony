package com.zktony.www.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.afterTextChange
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
import com.zktony.core.ext.format
import com.zktony.core.ext.setEqualText
import com.zktony.core.utils.Constants.MAX_MOTOR
import com.zktony.core.utils.Constants.MAX_TIME
import com.zktony.core.utils.Constants.MAX_VOLTAGE_ZM
import com.zktony.www.R
import com.zktony.www.databinding.FragmentTransferBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TransferFragment :
    BaseFragment<TransferViewModel, FragmentTransferBinding>(R.layout.fragment_transfer) {

    override val viewModel: TransferViewModel by viewModel()

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
                        && it.proteinName.isNotEmpty()
                        && it.proteinMin > 0f
                        && it.proteinMax > 0f
                        && ((it.glueType == 0 && it.glueNormalSize > 0f) || (it.glueType == 1 && it.glueMax > 0f && it.glueMin > 0f))
                        && it.glueThickness.isNotEmpty()
                        && it.bufferType.isNotEmpty()
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
                        proteinName.setEqualText(it.proteinName)
                        if (it.proteinMin > 0f) {
                            proteinMin.setEqualText(it.proteinMin.format())
                        }
                        if (it.proteinMax > 0f) {
                            proteinMax.setEqualText(it.proteinMax.format())
                        }
                        if (it.glueType == 0) {
                            radioGlueNormal.isChecked = true
                            radioGlueGradient.isChecked = false
                            glueNormal.visibility = View.VISIBLE
                            glueMaxMin.visibility = View.GONE
                            if (it.glueNormalSize > 0f) {
                                glueNormalSize.setEqualText(
                                    it.glueNormalSize.format()
                                )
                            }
                        }
                        if (it.glueType == 1) {
                            radioGlueNormal.isChecked = false
                            radioGlueGradient.isChecked = true
                            glueNormal.visibility = View.GONE
                            glueMaxMin.visibility = View.VISIBLE
                            if (it.glueMax > 0f) {
                                glueMax.setEqualText(it.glueMax.format())
                            }
                            if (it.glueMin > 0f) {
                                glueMin.setEqualText(it.glueMin.format())
                            }
                        }
                        if (it.glueThickness == "0.75") {
                            radioGlue075.isChecked = true
                        }
                        if (it.glueThickness == "1.0") {
                            radioGlue10.isChecked = true
                        }
                        if (it.glueThickness == "1.5") {
                            radioGlue15.isChecked = true
                        }
                        if (it.bufferType == getString(R.string.factory)) {
                            factory.isChecked = true
                            otherInfo.visibility = View.GONE
                            otherBufferInfoDiv.visibility = View.GONE
                        } else {
                            other.isChecked = true
                            otherInfo.visibility = View.VISIBLE
                            otherBufferInfoDiv.visibility = View.VISIBLE
                            otherBufferInfo.setEqualText(it.bufferType)
                        }
                        if (it.voltage > 0f) {
                            voltage.setEqualText(it.voltage.format())
                        }
                        if (it.motor > 0f) {
                            motor.setEqualText(it.motor.toString())
                        }
                        if (it.time > 0f) {
                            time.setEqualText(it.time.format())
                        }
                        if (it.name.isNotEmpty()
                            && it.proteinName.isNotEmpty()
                            && it.proteinMin > 0f
                            && it.proteinMax > 0f
                            && ((it.glueType == 0 && it.glueNormalSize > 0f) || (it.glueType == 1 && it.glueMax > 0f && it.glueMin > 0f))
                            && it.glueThickness.isNotEmpty()
                            && it.bufferType.isNotEmpty()
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
                    PopTip.show(getString(com.zktony.core.R.string.save_success))
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
            proteinName.afterTextChange { viewModel.setProteinMin(it) }
            proteinMin.afterTextChange { viewModel.setProteinMin(it.toFloatOrNull() ?: 0f) }
            proteinMax.afterTextChange { viewModel.setProteinMax(it.toFloatOrNull() ?: 0f) }
            glueNormalSize.afterTextChange { viewModel.setGlueNormalSize(it.toFloatOrNull() ?: 0f) }
            glueMax.afterTextChange { viewModel.setGlueMax(it.toFloatOrNull() ?: 0f) }
            glueMin.afterTextChange { viewModel.setGlueMin(it.toFloatOrNull() ?: 0f) }
            otherBufferInfo.afterTextChange { viewModel.setBufferInfo(it) }
            voltage.afterTextChange {
                viewModel.setVoltage(voltage = it.toFloatOrNull() ?: 0f, block = {
                    binding.voltage.setText(MAX_VOLTAGE_ZM.format())
                })
            }
            time.afterTextChange {
                viewModel.setTime(time = it.toFloatOrNull() ?: 0f, block = {
                    binding.time.setText(MAX_TIME.format())
                })
            }
            motor.afterTextChange {
                viewModel.setMotor(motor = it.toIntOrNull() ?: 0, block = {
                    binding.motor.setText(MAX_MOTOR.toString())
                })
            }

            radioGroupGlueType.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.radio_glue_normal -> {
                        glueNormal.visibility = View.VISIBLE
                        glueMaxMin.visibility = View.GONE
                        viewModel.setGlueType(0)
                    }

                    R.id.radio_glue_gradient -> {
                        glueNormal.visibility = View.GONE
                        glueMaxMin.visibility = View.VISIBLE
                        viewModel.setGlueType(1)
                    }
                }
            }
            radioGroupGlueThickness.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.radio_glue_075 -> viewModel.setGlueThickness("0.75")
                    R.id.radio_glue_10 -> viewModel.setGlueThickness("1.0")
                    R.id.radio_glue_15 -> viewModel.setGlueThickness("1.5")
                }
            }
            radioGroupBufferType.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.factory -> {
                        otherInfo.visibility = View.GONE
                        viewModel.setBufferInfo(getString(R.string.factory))
                    }

                    R.id.other -> {
                        otherInfo.visibility = View.VISIBLE
                        viewModel.setBufferInfo(otherBufferInfo.text.toString())
                    }
                }
            }
        }
    }
}