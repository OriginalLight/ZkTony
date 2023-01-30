package com.zktony.www.ui.container

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.afterTextChange
import com.zktony.www.common.extension.removeZero
import com.zktony.www.common.extension.showPositionDialog
import com.zktony.www.databinding.FragmentPlateBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlateThreeFragment :
    BaseFragment<PlateThreeViewModel, FragmentPlateBinding>(R.layout.fragment_plate) {
    override val viewModel: PlateThreeViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initView()
        initFlowCollector()
        initEditText()
        initTextView()
    }

    /**
     * 初始化view
     */
    private fun initView() {
        binding.dynamicPlate.run {
            setShowLocation(true)
            setOnItemClick { x, y ->
                if (x == 0 && y == 0) showPositionDialog(0,
                    { x1, y1 ->
                        viewModel.move(x1, y1)
                    },
                    { x2, y2, flag ->
                        viewModel.save(x2, y2, flag)
                    }
                )
                if (x == this.getColumn() - 1 && y == this.getRow() - 1) showPositionDialog(1,
                    { x1, y1 ->
                        viewModel.move(x1, y1)
                    },
                    { x2, y2, flag ->
                        viewModel.save(x2, y2, flag)
                    }
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
                                positionOne.text = "1 = ( ${
                                    it.x1.toString().removeZero()
                                }, ${it.y1.toString().removeZero()} )"
                                positionTwo.text = "2 = ( ${
                                    it.x2.toString().removeZero()
                                }, ${it.y2.toString().removeZero()} )"
                                dynamicPlate.setRowAndColumn(it.row, it.column)
                            }
                        }
                    }
                }
                launch {
                    delay(300L)
                    binding.run {
                        row.setText(viewModel.uiState.value?.row.toString())
                        column.setText(viewModel.uiState.value?.column.toString())
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initEditText() {
        binding.run {
            row.afterTextChange { viewModel.setRow(maxOf(it.toIntOrNull() ?: 2, 2)) }
            column.afterTextChange { viewModel.setColumn(maxOf(it.toIntOrNull() ?: 2, 2)) }
        }
    }

    private fun initTextView() {
        binding.run {
            positionOne.setOnClickListener {
                showPositionDialog(0,
                    { x1, y1 ->
                        viewModel.move(x1, y1)
                    },
                    { x2, y2, flag ->
                        viewModel.save(x2, y2, flag)
                    }
                )
            }
            positionTwo.setOnClickListener {
                showPositionDialog(1,
                    { x1, y1 ->
                        viewModel.move(x1, y1)
                    },
                    { x2, y2, flag ->
                        viewModel.save(x2, y2, flag)
                    }
                )
            }
        }
    }
}