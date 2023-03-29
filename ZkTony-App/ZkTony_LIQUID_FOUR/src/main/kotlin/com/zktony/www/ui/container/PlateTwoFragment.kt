package com.zktony.www.ui.container

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zktony.common.base.BaseFragment
import com.zktony.common.dialog.inputNumberDialog
import com.zktony.common.ext.clickNoRepeat
import com.zktony.common.ext.clickScale
import com.zktony.common.ext.removeZero
import com.zktony.www.R
import com.zktony.www.common.ext.positionDialog
import com.zktony.www.databinding.FragmentPlateBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlateTwoFragment :
    BaseFragment<PlateTwoViewModel, FragmentPlateBinding>(R.layout.fragment_plate) {
    override val viewModel: PlateTwoViewModel by viewModel()

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
                launch {
                    viewModel.uiState.collect {
                        val plate = it.plate
                        if (plate != null) {
                            val x0y0 = it.holes.find { hole -> hole.x == 0 && hole.y == 0 }
                            val x1y1 =
                                it.holes.find { hole -> hole.x == plate.x - 1 && hole.y == plate.y - 1 }
                            binding.apply {
                                with(dynamicPlate) {
                                    x = plate.y
                                    y = plate.x
                                }
                                sizeIndicatorWidth.text = plate.y.toString()
                                sizeIndicatorHeight.text = plate.x.toString()
                                sizeIndicatorHeight.type = 1
                                if (x0y0 != null) {
                                    position.textLeft = "( ${
                                        x0y0.xAxis.toString().removeZero()
                                    } , ${x0y0.yAxis.toString().removeZero()} )"
                                }
                                if (x1y1 != null) {
                                    position.textRight = "( ${
                                        x1y1.xAxis.toString().removeZero()
                                    } , ${x1y1.yAxis.toString().removeZero()} )"
                                }
                            }
                        }
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
            dynamicPlate.showLocation = true
            with(sizeIndicatorWidth) {
                clickScale()
                clickNoRepeat {
                    inputNumberDialog(
                        "请输入加液板尺寸",
                        sizeIndicatorWidth.text.toIntOrNull() ?: 12
                    ) {
                        viewModel.setXY(
                            x = sizeIndicatorHeight.text.toIntOrNull() ?: 8,
                            y = it
                        )
                    }
                }
            }
            with(sizeIndicatorHeight) {
                clickScale()
                clickNoRepeat {
                    inputNumberDialog(
                        "请输入加液板尺寸",
                        sizeIndicatorHeight.text.toIntOrNull() ?: 8
                    ) {
                        viewModel.setXY(
                            x = it,
                            y = sizeIndicatorWidth.text.toIntOrNull() ?: 12
                        )
                    }
                }
            }
            position.onItemClick = { index ->
                if (index == 0) {
                    val hole = viewModel.uiState.value.holes.find { hole -> hole.x == 0 && hole.y == 0 }
                    positionDialog(
                        textX = hole?.xAxis ?: 0f,
                        textY = hole?.yAxis ?: 0f,
                        block1 = { x, y -> viewModel.move(x, y) },
                        block2 = { x, y -> viewModel.save(x, y, 0) }
                    )
                } else {
                    val plate = viewModel.uiState.value.plate!!
                    val hole =
                        viewModel.uiState.value.holes.find { hole -> hole.x == plate.x - 1 && hole.y == plate.y - 1 }
                    positionDialog(
                        textX = hole?.xAxis ?: 0f,
                        textY = hole?.yAxis ?: 0f,
                        block1 = { x, y -> viewModel.move(x, y) },
                        block2 = { x, y -> viewModel.save(x, y, 1) }
                    )
                }
            }
        }
    }
}