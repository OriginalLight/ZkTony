package com.zktony.www.ui.container

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.navigation.findNavController
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.common.adapter.PointAdapter
import com.zktony.www.databinding.FragmentContainerEditBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContainerEditFragment :
    BaseFragment<ContainerEditViewModel, FragmentContainerEditBinding>(R.layout.fragment_container_edit) {

    override val viewModel: ContainerEditViewModel by viewModel()

    private val adapter by lazy { PointAdapter() }

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
                        adapter.submitList(it.list)
                        editForm.isVisible = it.edit
                        if (it.container != null) {
                            size.text = "${it.container.x} X ${it.container.y}"
                            c2Title.text = "${'A' + it.container.x - 1}${it.container.y}"
                            if (it.list.isNotEmpty()) {
                                val x1 = it.list.find { c -> c.x == 0 && c.y == 0 }
                                val x2 =
                                    it.list.find { c -> c.x == it.container.x - 1 && c.y == it.container.y - 1 }
                                if (x1 != null) {
                                    c1.text =
                                        "( ${x1.xAxis.removeZero()} , ${x1.yAxis.removeZero()} )"
                                } else {
                                    c1.text = "( 0 , 0 )"
                                }
                                if (x2 != null) {
                                    c2.text =
                                        "( ${x2.xAxis.removeZero()} , ${x2.yAxis.removeZero()} )"
                                } else {
                                    c1.text = "( 0 , 0 )"
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
        arguments?.let {
            val id = it.getLong("id")
            if (id != 0L) {
                viewModel.init(id)
            }
        }
        binding.apply {
            recyclerView.adapter = adapter
            with(back) {
                clickScale()
                clickNoRepeat {
                    findNavController().navigateUp()
                }
            }
            with(edit) {
                clickScale()
                clickNoRepeat {
                    viewModel.edit()
                }
            }
            with(sizeForm) {
                clickScale()
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
            with(c1Form) {
                clickScale()
                clickNoRepeat {
                    setCoordinate(0)
                }
            }
            with(c2Form) {
                clickScale()
                clickNoRepeat {
                    setCoordinate(1)
                }
            }
        }
    }

    private fun setCoordinate(index: Int) {
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
                    val maxXTrip = viewModel.uiState.value.maxXTrip
                    val maxYTrip = viewModel.uiState.value.maxYTrip
                    c1?.let {
                        val xy =
                            if (index == 1) list.find { p -> p.x == c1.x - 1 && p.y == c1.y - 1 }
                            else list.find { p -> p.x == 0 && p.y == 0 }
                        inputX.setText(xy?.xAxis?.removeZero() ?: "0")
                        inputY.setText(xy?.yAxis?.removeZero() ?: "0")
                    }

                    move.clickNoRepeat {
                        val x = inputX.text.toString().toFloatOrNull() ?: 0f
                        val y = inputY.text.toString().toFloatOrNull() ?: 0f
                        if (x > maxXTrip || y > maxYTrip) {
                            PopTip.show("${getString(com.zktony.core.R.string.over_the_trip)} ${maxXTrip.removeZero()},${maxYTrip.removeZero()}")
                        } else {
                            viewModel.move(x, y)
                        }
                    }

                    btnOk.clickNoRepeat {
                        val x = inputX.text.toString().toFloatOrNull() ?: 0f
                        val y = inputY.text.toString().toFloatOrNull() ?: 0f
                        if (x > maxXTrip || y > maxYTrip) {
                            PopTip.show("${getString(com.zktony.core.R.string.over_the_trip)} ${maxXTrip.removeZero()},${maxYTrip.removeZero()}")
                        } else {
                            viewModel.save(x, y, index)
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