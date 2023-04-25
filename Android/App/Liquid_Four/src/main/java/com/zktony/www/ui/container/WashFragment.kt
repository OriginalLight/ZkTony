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
import com.zktony.www.databinding.FragmentWashBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class WashFragment : BaseFragment<WashViewModel, FragmentWashBinding>(R.layout.fragment_wash) {

    override val viewModel: WashViewModel by viewModel()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initView()
        initFlowCollector()
    }

    @SuppressLint("SetTextI18n")
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    if (it != null) {
                        binding.washPlate.text =
                            "( ${it.xAxis.toString().removeZero()} , ${
                                it.yAxis.toString().removeZero()
                            } )"
                    }
                }
            }
        }
    }

    private fun initView() {
        binding.apply {
            washPlate.clickNoRepeat {
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

                            val con = viewModel.uiState.value
                            inputX.setText(con?.xAxis?.removeZero() ?: "0")
                            inputY.setText(con?.yAxis?.removeZero() ?: "0")

                            move.clickNoRepeat {
                                val x = inputX.text.toString().toFloatOrNull() ?: 0f
                                val y = inputY.text.toString().toFloatOrNull() ?: 0f
                                if (x > 240f || y > 320f) {
                                    PopTip.show("${getString(com.zktony.core.R.string.over_the_trip)} 240,320")
                                } else {
                                    viewModel.move(x, y)
                                }
                            }

                            btnOk.clickNoRepeat {
                                val x = inputX.text.toString().toFloatOrNull() ?: 0f
                                val y = inputY.text.toString().toFloatOrNull() ?: 0f
                                if (x > 240f || y > 320f) {
                                    PopTip.show("${getString(com.zktony.core.R.string.over_the_trip)} 240,320")
                                } else {
                                    viewModel.save(x, y)
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
            with(back) {
                clickScale()
                clickNoRepeat { findNavController().navigateUp() }
            }

        }

    }

}