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
                        maxYTrip.setEqualText(it.maxYTrip.removeZero())
                        wash.text = it.washTank.removeZero()
                    }
                }
            }
        }
    }

    private fun initView() {
        binding.apply {
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
        }
    }

    private fun showAxisDialog() {
        CustomDialog.build()
            .setCustomView(object :
                OnBindView<CustomDialog>(R.layout.layout_axis) {
                override fun onBind(dialog: CustomDialog, v: View) {
                    val input = v.findViewById<EditText>(R.id.input)
                    val btnMove = v.findViewById<MaterialButton>(R.id.move)
                    val btnOk = v.findViewById<MaterialButton>(R.id.ok)
                    val btnCancel = v.findViewById<MaterialButton>(R.id.cancel)

                    input.setText(binding.wash.text)
                    val maxYTrip = viewModel.uiState.value.maxYTrip

                    btnMove.clickNoRepeat {
                        val axis = input.text.toString().toFloatOrNull() ?: 0f
                        if (axis > maxYTrip) {
                            PopTip.show("${com.zktony.core.R.string.over_the_trip} $maxYTrip")
                        } else {
                            viewModel.move(axis)
                        }
                    }

                    btnOk.clickNoRepeat {
                        val axis = input.text.toString().toFloatOrNull() ?: 0f
                        if (axis > maxYTrip) {
                            PopTip.show("${com.zktony.core.R.string.over_the_trip} $maxYTrip")
                        } else {
                            viewModel.save("WASH_TANK", axis)
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