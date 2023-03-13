package com.zktony.www.ui.program

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
import com.zktony.common.utils.Constants.MAX_TIME
import com.zktony.common.utils.Constants.MAX_VOLTAGE_RS
import com.zktony.www.R
import com.zktony.www.databinding.FragmentRsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RsFragment : BaseFragment<RsViewModel, FragmentRsBinding>(R.layout.fragment_rs) {

    override val viewModel: RsViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initView()
        initFlowCollector()
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
            name.afterTextChange {
                viewModel.setName(it)
            }
            voltage.afterTextChange {
                viewModel.setVoltage(voltage = it.toFloatOrNull() ?: 0f, block = {
                    binding.voltage.setText(MAX_VOLTAGE_RS.toString().removeZero())
                })
            }
            time.afterTextChange {
                viewModel.setTime(time = it.toFloatOrNull() ?: 0f, block = {
                    binding.time.setText(MAX_TIME.toString().removeZero())
                })
            }
            cancel.clickNoRepeat { findNavController().navigateUp() }
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
        }
    }

    /**
     * 初始化观察者
     */
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    if (it.name.isNotEmpty() && it.voltage > 0f && it.time > 0f) {
                        binding.save.visibility = View.VISIBLE
                    } else {
                        binding.save.visibility = View.GONE
                    }
                    binding.apply {
                        name.setEqualText(it.name)
                        if (it.voltage > 0f) {
                            voltage.setEqualText(it.voltage.toString().removeZero())
                        }
                        if (it.time > 0f) {
                            time.setEqualText(it.time.toString().removeZero())
                        }
                    }
                }
            }
        }
    }
}
