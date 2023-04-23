package com.zktony.www.ui.container

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.databinding.FragmentContainerEditBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContainerEditFragment :
    BaseFragment<ContainerEditViewModel, FragmentContainerEditBinding>(R.layout.fragment_container_edit) {

    override val viewModel: ContainerEditViewModel by viewModel()

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
                        val c1 = it.container
                        if (c1 != null) {
                            val pX0y0 = it.list.find { p -> p.x == 0 && p.y == 0 }
                            val pXmyn = it.list.find { p -> p.x == c1.x - 1 && p.y == c1.y - 1 }

                            size.text = "${c1.x} X ${c1.y}"

                            pX0y0?.let { p ->
                                x0y0.text = "( ${p.xAxis.removeZero()} , ${p.yAxis.removeZero()} )"
                            }

                            pXmyn?.let { p ->
                                xmyn.text = "( ${p.xAxis.removeZero()} , ${p.yAxis.removeZero()} )"
                            }

                            with(dynamicPlate) {
                                x = c1.y
                                y = c1.x
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
        arguments?.let {
            val id = it.getLong("id")
            if (id != 0L) {
                viewModel.init(id)
            }
        }
        binding.apply {
            dynamicPlate.showLocation = true
            with(back) {
                clickScale()
                clickNoRepeat { findNavController().navigateUp() }
            }
            with(size) {
                setUnderLine()
                clickNoRepeat {
                    CustomDialog.build()
                        .setCustomView(object :
                            OnBindView<CustomDialog>(R.layout.layout_size) {
                            @SuppressLint("SetTextI18n")
                            override fun onBind(dialog: CustomDialog, v: View) {
                                val inputX = v.findViewById<EditText>(R.id.input_x)
                                val inputY = v.findViewById<EditText>(R.id.input_y)
                                val btnOk = v.findViewById<MaterialButton>(R.id.ok)
                                val btnCancel = v.findViewById<MaterialButton>(R.id.cancel)

                                val con = viewModel.uiState.value.container
                                inputX.setText(con?.x?.toString() ?: "0")
                                inputY.setText(con?.y?.toString() ?: "0")

                                btnOk.clickNoRepeat {
                                    val x = inputX.text.toString().toIntOrNull() ?: 0
                                    val y = inputY.text.toString().toIntOrNull() ?: 0
                                    viewModel.setXY(x, y)
                                    dialog.dismiss()
                                }
                                btnCancel.clickNoRepeat { dialog.dismiss() }
                            }
                        })
                        .setCancelable(false)
                        .setMaskColor(Color.parseColor("#4D000000"))
                        .show()
                }
            }
            with(x0y0) {
                setUnderLine()
                clickNoRepeat {
                    CustomDialog.build()
                        .setCustomView(object :
                            OnBindView<CustomDialog>(R.layout.layout_axis) {
                            @SuppressLint("SetTextI18n")
                            override fun onBind(dialog: CustomDialog, v: View) {
                                val inputX = v.findViewById<EditText>(R.id.input_x)
                                val inputY = v.findViewById<EditText>(R.id.input_y)
                                val move = v.findViewById<MaterialButton>(R.id.move)
                                val btnOk = v.findViewById<MaterialButton>(R.id.ok)
                                val btnCancel = v.findViewById<MaterialButton>(R.id.cancel)

                                val list = viewModel.uiState.value.list
                                val pX0y0 = list.find { p -> p.x == 0 && p.y == 0 }
                                inputX.setText(pX0y0?.xAxis?.removeZero() ?: "0")
                                inputY.setText(pX0y0?.yAxis?.removeZero() ?: "0")

                                move.clickNoRepeat {
                                    val x = inputX.text.toString().toFloatOrNull() ?: 0f
                                    val y = inputY.text.toString().toFloatOrNull() ?: 0f
                                    if (x > 240f || y > 320f) {
                                        PopTip.show("${com.zktony.core.R.string.over_the_trip} 240,320")
                                    } else {
                                        viewModel.move(x, y)
                                    }
                                }

                                btnOk.clickNoRepeat {
                                    val x = inputX.text.toString().toFloatOrNull() ?: 0f
                                    val y = inputY.text.toString().toFloatOrNull() ?: 0f
                                    if (x > 240f || y > 320f) {
                                        PopTip.show("${com.zktony.core.R.string.over_the_trip} 240,320")
                                    } else {
                                        viewModel.save(x, y, 0)
                                        dialog.dismiss()
                                    }
                                }
                                btnCancel.clickNoRepeat { dialog.dismiss() }
                            }
                        })
                        .setCancelable(false)
                        .setMaskColor(Color.parseColor("#4D000000"))
                        .show()
                }
            }

            with(xmyn) {
                setUnderLine()
                clickNoRepeat {
                    CustomDialog.build()
                        .setCustomView(object :
                            OnBindView<CustomDialog>(R.layout.layout_axis) {
                            @SuppressLint("SetTextI18n")
                            override fun onBind(dialog: CustomDialog, v: View) {
                                val inputX = v.findViewById<EditText>(R.id.input_x)
                                val inputY = v.findViewById<EditText>(R.id.input_y)
                                val move = v.findViewById<MaterialButton>(R.id.move)
                                val btnOk = v.findViewById<MaterialButton>(R.id.ok)
                                val btnCancel = v.findViewById<MaterialButton>(R.id.cancel)

                                val c1 = viewModel.uiState.value.container
                                val list = viewModel.uiState.value.list
                                c1?.let {
                                    val pXmyn =
                                        list.find { p -> p.x == c1.x - 1 && p.y == c1.y - 1 }
                                    inputX.setText(pXmyn?.xAxis?.removeZero() ?: "0")
                                    inputY.setText(pXmyn?.yAxis?.removeZero() ?: "0")
                                }

                                move.clickNoRepeat {
                                    val x = inputX.text.toString().toFloatOrNull() ?: 0f
                                    val y = inputY.text.toString().toFloatOrNull() ?: 0f
                                    if (x > 240f || y > 320f) {
                                        PopTip.show("${com.zktony.core.R.string.over_the_trip} 240,320")
                                    } else {
                                        viewModel.move(x, y)
                                    }
                                }

                                btnOk.clickNoRepeat {
                                    val x = inputX.text.toString().toFloatOrNull() ?: 0f
                                    val y = inputY.text.toString().toFloatOrNull() ?: 0f
                                    if (x > 240f || y > 320f) {
                                        PopTip.show("${com.zktony.core.R.string.over_the_trip} 240,320")
                                    } else {
                                        viewModel.save(x, y, 1)
                                        dialog.dismiss()
                                    }
                                }
                                btnCancel.clickNoRepeat { dialog.dismiss() }
                            }
                        })
                        .setCancelable(false)
                        .setMaskColor(Color.parseColor("#4D000000"))
                        .show()
                }
            }
        }
    }
}