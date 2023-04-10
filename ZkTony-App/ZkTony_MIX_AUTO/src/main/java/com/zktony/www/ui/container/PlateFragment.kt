package com.zktony.www.ui.container

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zktony.core.base.BaseFragment
import com.zktony.core.dialog.inputDecimalDialog
import com.zktony.core.dialog.inputNumberDialog
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.removeZero
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
                        space.text = "${it.container?.space?.removeZero() ?: 2f}"
                        gradientPlate.size = it.plate?.size ?: 10
                        gradientPlate.yAxis = it.holeList.map { hole -> hole.y to hole.yAxis }
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
                    viewModel.uiState.value.plate?.size ?: 10,
                    block = { viewModel.reSize(it) }
                )
            }
            space.clickNoRepeat {
                inputNumberDialog(
                    "请输入加液孔到排液孔间距",
                    viewModel.uiState.value.container?.space ?: 2f
                ) {
                    viewModel.setSpace(it)
                }
            }
            gradientPlate.onItemClick = { index ->
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