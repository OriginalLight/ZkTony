package com.zktony.www.ui.program

import android.os.Bundle
import android.view.View
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
import com.zktony.www.common.extension.isFastClick
import com.zktony.www.common.extension.removeZero
import com.zktony.www.databinding.FragmentRsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RsFragment : BaseFragment<RsViewModel, FragmentRsBinding>(R.layout.fragment_rs) {

    override val viewModel: RsViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initProgram()
        initFlowCollector()
        initButton()
        initEditText()
    }

    /**
     * 初始化程序拿到传过来的id
     * 如果不是‘None’就是修改不然是添加
     */
    private fun initProgram() {
        arguments?.let {
            RsFragmentArgs.fromBundle(it).id.run {
                if (this != "None") {
                    viewModel.loadProgram(this) { program ->
                        binding.name.setText(program.name)
                        binding.voltage.setText(program.voltage.toString().removeZero())
                        binding.time.setText(program.time.toString().removeZero())
                    }
                }
            }
        }
    }

    /**
     * 初始化观察者
     */
    private fun initFlowCollector() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        if (it.name.isNotEmpty() && it.voltage > 0f && it.time > 0f) {
                            binding.save.visibility = View.VISIBLE
                        } else {
                            binding.save.visibility = View.GONE
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
            setOnClickListener {
                if (isFastClick()) return@setOnClickListener
                findNavController().navigateUp()
            }
        }
        binding.cancel.setOnClickListener {
            if (isFastClick()) return@setOnClickListener
            findNavController().navigateUp()
        }
        binding.save.setOnClickListener {
            if (isFastClick()) return@setOnClickListener
            viewModel.save {
                PopTip.show("保存成功")
                findNavController().navigateUp()
            }
        }
    }

    /**
     * 初始化输入框
     */
    private fun initEditText() {
        binding.run {
            name.afterTextChange {
                viewModel.setName(it)
            }
            voltage.afterTextChange {
                val voltage = it.toFloatOrNull() ?: 0f
                // voltage最大值为65 最小值为0
                if (voltage > 65f) {
                    viewModel.setVoltage(65f)
                } else if (voltage < 0f) {
                    viewModel.setVoltage(0f)
                } else {
                    viewModel.setVoltage(voltage)
                }
            }
            time.afterTextChange {
                val time = it.toFloatOrNull() ?: 0f
                // time最大值为99 最小值为0
                if (time > 99f) {
                    viewModel.setTime(99f)
                } else if (time < 0f) {
                    viewModel.setTime(0f)
                } else {
                    viewModel.setTime(time)
                }
            }
        }
    }
}
