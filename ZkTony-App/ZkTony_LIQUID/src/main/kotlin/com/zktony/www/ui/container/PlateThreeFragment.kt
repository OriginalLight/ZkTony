package com.zktony.www.ui.container

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zktony.common.base.BaseFragment
import com.zktony.common.extension.removeZero
import com.zktony.www.R
import com.zktony.www.common.extension.positionDialog
import com.zktony.www.common.extension.sizeDialog
import com.zktony.www.databinding.FragmentPlateBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlateThreeFragment :
    BaseFragment<PlateThreeViewModel, FragmentPlateBinding>(R.layout.fragment_plate) {
    override val viewModel: PlateThreeViewModel by viewModels()

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
                        if (it != null) {
                            binding.apply {
                                dynamicPlate.setXY(it.column, it.row)
                                rowColumn.text = "${it.row} X ${it.column}"
                                positionOne.text = "( ${
                                    it.x1.toString().removeZero()
                                } , ${it.y1.toString().removeZero()} )"
                                positionTwo.text = "( ${
                                    it.x2.toString().removeZero()
                                } , ${it.y2.toString().removeZero()} )"
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
            rowColumn.setOnClickListener {
                sizeDialog(
                    textRow = viewModel.uiState.value!!.row,
                    textColumn = viewModel.uiState.value!!.column,
                    block1 = { row, column -> viewModel.setRowAndColumn(row, column) }
                )
            }
            positionOne.setOnClickListener {
                positionDialog(
                    textX = viewModel.uiState.value!!.x1,
                    textY = viewModel.uiState.value!!.y1,
                    block1 = { x, y -> viewModel.move(x, y) },
                    block2 = { x, y -> viewModel.save(x, y, 0) }
                )
            }
            positionTwo.setOnClickListener {
                positionDialog(
                    textX = viewModel.uiState.value!!.x2,
                    textY = viewModel.uiState.value!!.y2,
                    block1 = { x, y -> viewModel.move(x, y) },
                    block2 = { x, y -> viewModel.save(x, y, 1) }
                )
            }
        }
    }
}