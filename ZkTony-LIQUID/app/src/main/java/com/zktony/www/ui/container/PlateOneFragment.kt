package com.zktony.www.ui.container

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.afterTextChange
import com.zktony.www.common.extension.removeZero
import com.zktony.www.common.extension.showPositionDialog
import com.zktony.www.common.extension.showSizeDialog
import com.zktony.www.databinding.FragmentPlateBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlateOneFragment :
    BaseFragment<PlateOneViewModel, FragmentPlateBinding>(R.layout.fragment_plate) {
    override val viewModel: PlateOneViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initView()
        initFlowCollector()
    }

    /**
     * 初始化view
     */
    private fun initView() {
        binding.run {
            dynamicPlate.setShowLocation(true)
            rowColumn.setOnClickListener {
                showSizeDialog(
                    textRow = viewModel.uiState.value!!.row,
                    textColumn = viewModel.uiState.value!!.column,
                    block1 = { row, column -> viewModel.setRowAndColumn(row, column) }
                )
            }
            positionOne.setOnClickListener {
                showPositionDialog(
                    textX = viewModel.uiState.value!!.x1,
                    textY = viewModel.uiState.value!!.y1,
                    block1 = { x, y -> viewModel.move(x, y) },
                    block2 = { x, y -> viewModel.save(x, y, 0) }
                )
            }
            positionTwo.setOnClickListener {
                showPositionDialog(
                    textX = viewModel.uiState.value!!.x2,
                    textY = viewModel.uiState.value!!.y2,
                    block1 = { x, y -> viewModel.move(x, y) },
                    block2 = { x, y -> viewModel.save(x, y, 1) }
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        if (it != null) {
                            binding.run {
                                rowColumn.text = "${it.row} X ${it.column}"
                                positionOne.text = "( ${
                                    it.x1.toString().removeZero()
                                } , ${it.y1.toString().removeZero()} )"
                                positionTwo.text = "( ${
                                    it.x2.toString().removeZero()
                                } , ${it.y2.toString().removeZero()} )"

                                dynamicPlate.setRowAndColumn(it.row, it.column)
                            }
                        }
                    }
                }
            }
        }
    }
}