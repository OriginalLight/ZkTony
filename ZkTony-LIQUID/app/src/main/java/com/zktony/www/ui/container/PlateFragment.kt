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
import com.zktony.www.databinding.FragmentPlateBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlateFragment :
    BaseFragment<PlateViewModel, FragmentPlateBinding>(R.layout.fragment_plate) {
    override val viewModel: PlateViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initView()
        initFlowCollector()
        initEditText()
    }

    /**
     * 初始化view
     */
    private fun initView() {
        arguments?.getInt("position")?.let {
            viewModel.init(it)
        }
        binding.dynamicPlate.run {
            setShowLocation(true)
            setOnItemClick { x, y ->
                if (x == 0 && y == 0) showDialog(0)
                if (x == this.getColumn() - 1 && y == this.getRow() - 1) showDialog(
                    1
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
                                positionOne.text = "(${
                                    it.x1.toString().removeZero()
                                }, ${it.y1.toString().removeZero()})"
                                positionTwo.text = "(${
                                    it.x2.toString().removeZero()
                                }, ${it.y2.toString().removeZero()})"
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

    private fun showDialog(flag: Int) {
        CustomDialog.build()
            .setCustomView(object :
                OnBindView<CustomDialog>(R.layout.layout_position_input_dialog) {
                override fun onBind(dialog: CustomDialog, v: View) {
                    val title = v.findViewById<TextView>(R.id.title)
                    val inputX = v.findViewById<EditText>(R.id.input_x)
                    val inputY = v.findViewById<EditText>(R.id.input_y)
                    val move = v.findViewById<MaterialButton>(R.id.move)
                    val save = v.findViewById<MaterialButton>(R.id.save)
                    val cancel = v.findViewById<MaterialButton>(R.id.cancel)
                    title.text = if (flag == 0) "设置坐标 1" else "设置坐标 2"
                    move.setOnClickListener {
                        val x = inputX.text.toString().toFloatOrNull() ?: 0f
                        val y = inputY.text.toString().toFloatOrNull() ?: 0f
                        viewModel.move(x, y)
                    }
                    save.setOnClickListener {
                        val x = inputX.text.toString().toFloatOrNull() ?: 0f
                        val y = inputY.text.toString().toFloatOrNull() ?: 0f
                        viewModel.save(x, y, flag)
                        dialog.dismiss()
                    }
                    cancel.setOnClickListener { dialog.dismiss() }
                }
            })
            .setCancelable(false)
            .setMaskColor(Color.parseColor("#4D000000"))
            .show()
    }
}