package com.zktony.www.ui.admin

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.zktony.www.R
import com.zktony.www.adapter.MotorAdapter
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.afterTextChange
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.extension.setEqualText
import com.zktony.www.common.extension.spannerDialog
import com.zktony.www.databinding.FragmentMotorBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MotorFragment : BaseFragment<MotorViewModel, FragmentMotorBinding>(R.layout.fragment_motor) {

    override val viewModel: MotorViewModel by viewModels()

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
                                mode.text = if (it.motor.mode == 0) "增量模式" else "坐标模式"
                                subdivision.text = it.motor.subdivision.toString()
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
        adapter.setOnEditButtonClick { viewModel.selectMotor(it) }

        binding.apply {
            recycleView.adapter = adapter

            mode.setOnClickListener {
                spannerDialog(
                    view = it,
                    menu = listOf("增量模式", "坐标模式"),
                    block = { _, index -> viewModel.model(index) }

                )
            }

            subdivision.setOnClickListener {
                spannerDialog(
                    view = it,
                    menu = listOf("2", "4", "8", "16", "32"),
                    block = { str, _ -> viewModel.subdivision(str.toInt()) }
                )
            }

            update.setOnClickListener {
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