package com.zktony.www.ui.calibration

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.zktony.common.base.BaseFragment
import com.zktony.common.ext.clickScale
import com.zktony.www.R
import com.zktony.www.adapter.CalibrationAdapter
import com.zktony.www.common.extension.deleteDialog
import com.zktony.www.common.extension.inputDialog
import com.zktony.www.databinding.FragmentCalibrationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CalibrationFragment :
    BaseFragment<CalibrationViewModel, FragmentCalibrationBinding>(R.layout.fragment_calibration) {

    override val viewModel: CalibrationViewModel by viewModels()

    private val adapter by lazy { CalibrationAdapter() }

    /**
     * 初始化Flow收集器
     */
    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    /**
     * 初始化数据流收集器
     */
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    adapter.submitList(it)
                }
            }
        }
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        adapter.apply {
            setOnCheckedClick { viewModel.enable(it) }
            setOnDeleteButtonClick {
                deleteDialog(name = it.name, block = { viewModel.delete(it) })
            }
            setOnEditButtonClick {
                findNavController().navigate(
                    directions = CalibrationFragmentDirections.actionNavigationCalibrationToNavigationCalibrationData(
                        it.id
                    )
                )
            }
        }
        binding.apply {
            recyclerView.adapter = adapter

            with(add) {
                clickScale()
                setOnClickListener {
                    inputDialog { viewModel.insert(it) }
                }
            }
        }
    }
}