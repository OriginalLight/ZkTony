package com.zktony.www.ui.container

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.viewModels
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlateFragment :
    BaseFragment<PlateViewModel, FragmentPlateBinding>(R.layout.fragment_plate) {
    override val viewModel: PlateViewModel by viewModels()

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
                        size.text = "${it.plate?.x ?: 10}"
                        moveZ.text = "底部高度: ${it.container?.bottom?.removeZero() ?: 0}"
                        addZ.text = "头部高度: ${it.container?.top?.removeZero() ?: 0}"
                        gradientPlate.setSize(it.plate?.x ?: 10)
                        gradientPlate.setData(it.holeList.map { hole -> hole.x to (hole.xAxis > 0f) })
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
                    viewModel.uiState.value.plate?.x ?: 10
                ) {
                    viewModel.reSize(it)
                }
            }
            moveZ.clickNoRepeat {
                inputDecimalDialog(
                    message = "请输入底部高度",
                    value = viewModel.uiState.value.container?.bottom ?: 0f,
                    move = { viewModel.moveZ(it) },
                    block = { viewModel.setBottom(it) }
                )
            }
            addZ.clickNoRepeat {
                inputDecimalDialog(
                    message = "请输入头部高度",
                    value = viewModel.uiState.value.container?.top ?: 0f,
                    move = { viewModel.moveZ(it) },
                    block = { viewModel.setTop(it) }
                )
            }
            gradientPlate.setOnItemClick { index ->
                inputDecimalDialog(
                    message = "请输入 ${'A' + index} 横坐标",
                    value = viewModel.uiState.value.holeList.find { it.x == index }?.xAxis ?: 0f,
                    move = { viewModel.moveX(it) },
                    block = { viewModel.setHolePosition(index, it)  }
                )
            }
        }
    }
}