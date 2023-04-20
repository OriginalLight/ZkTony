package com.zktony.www.ui.program

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.lifecycle.*
import androidx.navigation.findNavController
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.core.base.BaseFragment
import com.zktony.core.dialog.spannerDialog
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
import com.zktony.www.R
import com.zktony.www.databinding.FragmentProgramEditBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProgramEditFragment :
    BaseFragment<ProgramEditViewModel, FragmentProgramEditBinding>(R.layout.fragment_program_edit) {

    override val viewModel: ProgramEditViewModel by viewModel()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    @SuppressLint("SetTextI18n")
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    binding.apply {
                        select.text = it.container?.name ?: "/"
                        gradientPlate.apply {
                            size = it.pointList.size
                            data = it.pointList.map { point -> point.index to point.enable }
                        }
                        selectAll.isEnabled = it.pointList.any { point -> !point.enable }
                        if (it.pointList.isNotEmpty()) {
                            volume.text =
                                "[ ${it.pointList[0].v1} μL, ${it.pointList[0].v2} μL, ${it.pointList[0].v3} μL, ${it.pointList[0].v4} μL ]"
                        } else {
                            volume.text = "/"
                        }
                    }
                }
            }
        }
    }

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
                clickNoRepeat {
                    findNavController().navigateUp()
                }
            }
            select.clickNoRepeat {
                val menu = viewModel.uiState.value.containerList.map { n -> n.name }
                if (menu.isEmpty()) {
                    return@clickNoRepeat
                }
                spannerDialog(
                    view = it,
                    menu = menu,
                    block = { _, index -> viewModel.selectContainer(index) }
                )
            }
            selectAll.clickNoRepeat {
                viewModel.enableAll()
            }
            gradientPlate.onItemClick = {
                viewModel.enablePoint(it)
            }
            volume.clickNoRepeat {
                val pointList = viewModel.uiState.value.pointList
                if (pointList.isEmpty()) {
                    return@clickNoRepeat
                }
                val point = pointList[0]
                CustomDialog.build()
                    .setCustomView(object :
                        OnBindView<CustomDialog>(R.layout.layout_volume) {
                        override fun onBind(dialog: CustomDialog, v: View) {
                            val inputV1 = v.findViewById<EditText>(R.id.input_v1)
                            val inputV2 = v.findViewById<EditText>(R.id.input_v2)
                            val inputV3 = v.findViewById<EditText>(R.id.input_v3)
                            val inputV4 = v.findViewById<EditText>(R.id.input_v4)
                            val btnOk = v.findViewById<MaterialButton>(R.id.ok)
                            val btnCancel = v.findViewById<MaterialButton>(R.id.cancel)
                            if (point.v1 != 0) inputV1.setText(point.v1.toString())
                            if (point.v2 != 0) inputV2.setText(point.v2.toString())
                            if (point.v3 != 0) inputV3.setText(point.v3.toString())
                            if (point.v4 != 0) inputV4.setText(point.v4.toString())

                            btnOk.clickNoRepeat {
                                val v1 = inputV1.text.toString().toIntOrNull() ?: 0
                                val v2 = inputV2.text.toString().toIntOrNull() ?: 0
                                val v3 = inputV3.text.toString().toIntOrNull() ?: 0
                                val v4 = inputV4.text.toString().toIntOrNull() ?: 0
                                if (v1 > 800 || v3 > 800) {
                                    PopTip.show("${getString(R.string.over_the_volume)} 800μL")
                                } else {
                                    viewModel.updateVolume(v1, v2, v3, v4)
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