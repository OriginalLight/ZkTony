package com.zktony.www.ui.container

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.TextView
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
                        size.text = "${it.container?.size ?: 10}"
                        title.text = it.container?.name ?: getString(R.string.title_container)
                        gradientPlate.size = it.container?.size ?: 10
                        gradientPlate.axis =
                            it.list.map { point -> Triple(point.index, point.axis, point.waste) }
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
            with(back) {
                clickScale()
                clickNoRepeat { findNavController().navigateUp() }
            }
            size.clickNoRepeat {
                inputDialog(
                    title = getString(com.zktony.core.R.string.edit),
                    value = size.text,
                    inputType = InputType.TYPE_CLASS_NUMBER,
                    block = { viewModel.reSize(it.toIntOrNull() ?: 10) }
                )
            }
            gradientPlate.onItemClick = { index ->
                CustomDialog.build()
                    .setCustomView(object :
                        OnBindView<CustomDialog>(R.layout.layout_axis_double) {
                        @SuppressLint("SetTextI18n")
                        override fun onBind(dialog: CustomDialog, v: View) {
                            val tvTitle = v.findViewById<TextView>(R.id.title)
                            val inputAxis = v.findViewById<EditText>(R.id.input_axis)
                            val inputWaste = v.findViewById<EditText>(R.id.input_waste)
                            val btnMoveAxis = v.findViewById<MaterialButton>(R.id.move_axis)
                            val btnMoveWaste = v.findViewById<MaterialButton>(R.id.move_waste)
                            val btnOk = v.findViewById<MaterialButton>(R.id.ok)
                            val btnCancel = v.findViewById<MaterialButton>(R.id.cancel)
                            tvTitle.text = "${'A' + index} ${getString(R.string.coordinate)}"
                            val point = viewModel.uiState.value.list.find { it.index == index }
                            inputAxis.setText(point?.axis?.removeZero() ?: "0")
                            inputWaste.setText(point?.waste?.removeZero() ?: "0")

                            btnMoveAxis.clickNoRepeat {
                                val axis = inputAxis.text.toString().toFloatOrNull() ?: 0f
                                if (axis > 250f) {
                                    PopTip.show("${com.zktony.core.R.string.over_the_trip} 250")
                                } else {
                                    viewModel.move(axis)
                                }
                            }

                            btnMoveWaste.clickNoRepeat {
                                val waste = inputWaste.text.toString().toFloatOrNull() ?: 0f
                                if (waste > 250f) {
                                    PopTip.show("${com.zktony.core.R.string.over_the_trip} 250")
                                } else {
                                    viewModel.move(waste)
                                }
                            }

                            btnOk.clickNoRepeat {
                                val axis = inputAxis.text.toString().toFloatOrNull() ?: 0f
                                val waste = inputWaste.text.toString().toFloatOrNull() ?: 0f
                                if (axis > 250f || waste > 250f) {
                                    PopTip.show("${com.zktony.core.R.string.over_the_trip} 250")
                                } else {
                                    viewModel.setPointPosition(index, axis, waste)
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