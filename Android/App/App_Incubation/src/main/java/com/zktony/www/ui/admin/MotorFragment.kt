package com.zktony.www.ui.admin

import android.os.Bundle
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.adapter.MotorAdapter
import com.zktony.www.databinding.FragmentMotorBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MotorFragment : BaseFragment<MotorViewModel, FragmentMotorBinding>(R.layout.fragment_motor) {

    override val viewModel: MotorViewModel by viewModel()

    private val adapter by lazy { MotorAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    /**
     * 初始化观察者
     */
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        adapter.submitList(it.motorList)
                        if (it.motor != null) {
                            binding.apply {
                                tvTitle.text = it.motor.name
                                speed.setEqualText(it.motor.speed.toString())
                                acceleration.setEqualText(it.motor.acceleration.toString())
                                deceleration.setEqualText(it.motor.deceleration.toString())
                                waitTime.setEqualText(it.motor.waitTime.toString())
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        adapter.onEditButtonClick = { viewModel.selectMotor(it) }

        binding.apply {
            recycleView.adapter = adapter

            update.clickNoRepeat {
                viewModel.update()
            }

            speed.afterTextChange {
                viewModel.speed(it.toIntOrNull() ?: 0)
            }

            acceleration.afterTextChange {
                viewModel.acceleration(it.toIntOrNull() ?: 0)
            }

            deceleration.afterTextChange {
                viewModel.deceleration(it.toIntOrNull() ?: 0)
            }

            waitTime.afterTextChange {
                viewModel.waitTime(it.toIntOrNull() ?: 0)
            }

            with(back) {
                clickScale()
                setOnClickListener { findNavController().navigateUp() }
            }
        }
    }
}