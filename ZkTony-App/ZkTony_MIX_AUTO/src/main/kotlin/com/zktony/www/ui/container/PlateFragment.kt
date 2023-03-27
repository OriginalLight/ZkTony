package com.zktony.www.ui.container

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zktony.common.base.BaseFragment
import com.zktony.common.dialog.inputDecimalDialog
import com.zktony.common.dialog.inputNumberDialog
import com.zktony.common.ext.clickNoRepeat
import com.zktony.common.ext.removeZero
import com.zktony.www.R
import com.zktony.www.databinding.FragmentPlateBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlateFragment :
    BaseFragment<PlateViewModel, FragmentPlateBinding>(R.layout.fragment_plate) {

    override val viewModel: PlateViewModel by viewModel()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    /**
     * 初始化flow collector
     */
    @SuppressLint("SetTextI18n")
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    binding.apply {
                        size.text = "${it.plate?.size ?: 10}"
                        gradientPlate.setSize(it.plate?.size ?: 10)
                        gradientPlate.setData(it.holeList.map { hole -> hole.y to (hole.yAxis > 0f) })
                    }
                }
            }
        }
    }

    /**
     * 初始化view
     */
    private fun initView() {
        binding.apply {
            size.clickNoRepeat {
                inputNumberDialog(
                    "请输入加液板尺寸",
                    viewModel.uiState.value.plate?.size ?: 10
                ) {
                    viewModel.reSize(it)
                }
            }
            gradientPlate.setOnItemClick { index ->
                inputDecimalDialog(
                    message = "请输入 ${'A' + index} 横坐标",
                    value = viewModel.uiState.value.holeList.find { it.y == index }?.yAxis ?: 0f,
                    move = { viewModel.moveY(it) },
                    block = { viewModel.setHolePosition(index, it) }
                )
            }
        }
    }
}