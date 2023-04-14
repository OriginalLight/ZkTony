package com.zktony.www.ui.calibration

import android.os.Bundle
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.zktony.core.base.BaseFragment
import com.zktony.core.dialog.inputDialog
import com.zktony.core.dialog.messageDialog
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
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
                    title = "删除校准程序",
                    message = "删除后将无法恢复，是否删除${it.name}？",
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
                        title = "添加校准程序",
                        hint = "请输入校准程序名称",
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