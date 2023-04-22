package com.zktony.www.ui.calibration

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
import com.zktony.core.ext.inputDialog
import com.zktony.core.ext.messageDialog
import com.zktony.www.R
import com.zktony.www.common.adapter.CalibrationAdapter
import com.zktony.www.databinding.FragmentCalibrationBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CalibrationFragment :
    BaseFragment<CalibrationViewModel, FragmentCalibrationBinding>(R.layout.fragment_calibration) {

    override val viewModel: CalibrationViewModel by viewModel()

    private val adapter by lazy { CalibrationAdapter() }

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
                messageDialog(
                    title = getString(com.zktony.core.R.string.delete),
                    message = "${getString(com.zktony.core.R.string.whether_delete)} ${it.name}？",
                    block = { viewModel.delete(it) }
                )
            }
            onEditButtonClick = {
                findNavController().navigate(
                    R.id.action_navigation_calibration_to_navigation_calibration_data,
                    Bundle().apply { putLong("id", it.id) }
                )
            }
        }
        binding.apply {
            recyclerView.adapter = adapter
            with(add) {
                clickScale()
                clickNoRepeat {
                    inputDialog(
                        title = getString(com.zktony.core.R.string.add),
                        hint = getString(com.zktony.core.R.string.input_name),
                        block = {
                            viewModel.insert(it) { id ->
                                findNavController().navigate(
                                    R.id.action_navigation_calibration_to_navigation_calibration_data,
                                    Bundle().apply { putLong("id", id) })
                            }
                        }
                    )
                }
            }
        }
    }
}