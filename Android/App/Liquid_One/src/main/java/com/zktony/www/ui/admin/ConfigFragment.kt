package com.zktony.www.ui.admin

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
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.databinding.FragmentConfigBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConfigFragment :
    BaseFragment<ConfigViewModel, FragmentConfigBinding>(R.layout.fragment_config) {

    override val viewModel: ConfigViewModel by viewModel()


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
                        maxXTrip.setEqualText(it.maxXTrip.removeZero())
                        maxYTrip.setEqualText(it.maxYTrip.removeZero())
                        wash.text =
                            "( ${it.washXAxis.removeZero()} , ${it.washYAxis.removeZero()} )"
                    }
                }
            }
        }
    }

    private fun initView() {
        binding.apply {
            maxXTrip.afterTextChange {
                viewModel.save("MAX_X_TRIP", it.toFloatOrNull() ?: 160f)
            }
            maxYTrip.afterTextChange {
                viewModel.save("MAX_Y_TRIP", it.toFloatOrNull() ?: 200f)
            }
            wash.clickNoRepeat {
                showAxisDialog()
            }
            with(back) {
                clickScale()
                clickNoRepeat {
                    findNavController().navigateUp()
                }
            }
            with(washForm) {
                clickScale()
                clickNoRepeat {
                    showAxisDialog()
                }
            }
        }
    }

    private fun showAxisDialog() {
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

                    val maxXTrip = viewModel.uiState.value.maxXTrip
                    val maxYTrip = viewModel.uiState.value.maxYTrip
                    val washXAxis = viewModel.uiState.value.washXAxis
                    val washYAxis = viewModel.uiState.value.washYAxis

                    inputX.setText(washXAxis.removeZero())
                    inputY.setText(washYAxis.removeZero())

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
                            viewModel.save("WASH_X_AXIS", x)
                            viewModel.save("WASH_Y_AXIS", y)
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