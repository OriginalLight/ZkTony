package com.zktony.www.ui.container

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zktony.common.base.BaseFragment
import com.zktony.common.ext.clickNoRepeat
import com.zktony.common.ext.removeZero
import com.zktony.www.R
import com.zktony.www.common.ext.positionDialog
import com.zktony.www.common.ext.sizeDialog
import com.zktony.www.databinding.FragmentPlateBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlateThreeFragment :
    BaseFragment<PlateThreeViewModel, FragmentPlateBinding>(R.layout.fragment_plate) {
    override val viewModel: PlateThreeViewModel by viewModel()

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
                                dynamicPlate.setXY(plate.x, plate.y)
                                rowColumn.text = "${plate.x} X ${plate.y}"
                                if (x0y0 != null) {
                                    positionOne.text = "( ${
                                        x0y0.xAxis.toString().removeZero()
                                    } , ${x0y0.yAxis.toString().removeZero()} )"
                                }
                                if (x1y1 != null) {
                                    positionTwo.text = "( ${
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
            dynamicPlate.setShowLocation(true)
            rowColumn.clickNoRepeat {
                val plate = viewModel.uiState.value.plate!!
                sizeDialog(
                    textRow = plate.x,
                    textColumn = plate.y,
                    block1 = { row, column -> viewModel.setXY(row, column) }
                )
            }
            positionOne.clickNoRepeat {
                val hole = viewModel.uiState.value.holes.find { hole -> hole.x == 0 && hole.y == 0 }
                positionDialog(
                    textX = hole?.xAxis ?: 0f,
                    textY = hole?.yAxis ?: 0f,
                    block1 = { x, y -> viewModel.move(x, y) },
                    block2 = { x, y -> viewModel.save(x, y, 0) }
                )
            }
            positionTwo.clickNoRepeat {
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