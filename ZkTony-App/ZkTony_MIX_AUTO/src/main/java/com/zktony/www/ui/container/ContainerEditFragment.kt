package com.zktony.www.ui.container

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.core.base.BaseFragment
import com.zktony.core.dialog.inputNumberDialog
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
import com.zktony.core.ext.removeZero
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
                        title.text = it.container?.name ?: "容器管理"
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
                inputNumberDialog(
                    message = "请输入加液板尺寸",
                    value = viewModel.uiState.value.container?.size ?: 10,
                    block = { viewModel.reSize(it) }
                )
            }
            gradientPlate.onItemClick = { index ->
                CustomDialog.build()
                    .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_axis_input) {
                        @SuppressLint("SetTextI18n")
                        override fun onBind(dialog: CustomDialog, v: View) {
                            val tvTitle = v.findViewById<TextView>(R.id.title)
                            val input = v.findViewById<EditText>(R.id.input_axis)
                            val waste = v.findViewById<EditText>(R.id.input_waste)
                            val moveAxis = v.findViewById<MaterialButton>(R.id.move_axis)
                            val moveWaste = v.findViewById<MaterialButton>(R.id.move_waste)
                            val btnOk = v.findViewById<MaterialButton>(R.id.save)
                            val btnCancel = v.findViewById<MaterialButton>(R.id.cancel)

                            tvTitle.text = "请输入 ${'A' + index} 横坐标"
                            val axis =
                                viewModel.uiState.value.list.find { it.index == index }?.axis ?: 0f
                            val wasteAxis =
                                viewModel.uiState.value.list.find { it.index == index }?.waste ?: 0f
                            if (axis != 0f) {
                                input.setText(axis.removeZero())
                            }
                            if (wasteAxis != 0f) {
                                waste.setText(wasteAxis.removeZero())
                            }
                            moveAxis.clickNoRepeat {
                                val distance = input.text.toString().toFloatOrNull() ?: 0f
                                if (distance > 250f) {
                                    PopTip.show("最大移动距离为250mm")
                                    return@clickNoRepeat
                                }
                                viewModel.moveY(distance)
                            }
                            moveWaste.clickNoRepeat {
                                val distance = waste.text.toString().toFloatOrNull() ?: 0f
                                if (distance > 250f) {
                                    PopTip.show("最大移动距离为250mm")
                                    return@clickNoRepeat
                                }
                                viewModel.moveY(distance)
                            }
                            btnOk.clickNoRepeat {
                                val distance = input.text.toString().toFloatOrNull() ?: 0f
                                val distance1 = waste.text.toString().toFloatOrNull() ?: 0f
                                if (distance > 250f || distance1 > 250f) {
                                    PopTip.show("最大移动距离为250mm")
                                    return@clickNoRepeat
                                }
                                viewModel.setPointPosition(index, distance, distance1)
                                dialog.dismiss()
                            }
                            btnCancel.clickNoRepeat { dialog.dismiss() }
                        }
                    }).setCancelable(false).setMaskColor(Color.parseColor("#4D000000")).show()
            }
        }
    }
}