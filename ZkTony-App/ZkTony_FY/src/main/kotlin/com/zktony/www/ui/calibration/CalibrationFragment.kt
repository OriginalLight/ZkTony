package com.zktony.www.ui.calibration

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.zktony.common.base.BaseFragment
import com.zktony.common.dialog.deleteDialog
import com.zktony.common.dialog.inputDialog
import com.zktony.common.ext.clickNoRepeat
import com.zktony.common.ext.clickScale
import com.zktony.www.R
import com.zktony.www.common.adapter.CalibrationAdapter
import com.zktony.www.databinding.FragmentCalibrationBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CalibrationFragment :
    BaseFragment<CalibrationViewModel, FragmentCalibrationBinding>(R.layout.fragment_calibration) {

    override val viewModel: CalibrationViewModel by viewModel()

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
            onCheckedClick = { viewModel.enable(it) }
            onDeleteButtonClick = {
                deleteDialog(name = it.name, block = { viewModel.delete(it) })
            }
            onEditButtonClick = {
                findNavController().navigate(
                    R.id.action_navigation_calibration_to_navigation_calibration_data,
                    Bundle().apply { putString("id", it.id) }
                )
            }
        }
        binding.apply {
            recyclerView.adapter = adapter

            with(add) {
                clickScale()
                clickNoRepeat {
                    inputDialog { viewModel.insert(it) }
                }
            }
        }
    }
}