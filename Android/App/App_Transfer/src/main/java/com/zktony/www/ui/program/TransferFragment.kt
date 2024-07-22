package com.zktony.www.ui.program

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
        }
    }
}