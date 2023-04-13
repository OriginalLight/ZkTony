package com.zktony.www.ui.container

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
import com.zktony.core.ext.removeZero
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
                    binding.apply {
                        washPlate.text = (it?.axis ?: 0f).removeZero()
                        title.text = it?.name ?: "容器管理"
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
            washPlate.clickNoRepeat {
                CustomDialog.build()
                    .setCustomView(object :
                        OnBindView<CustomDialog>(R.layout.layout_axis) {
                        override fun onBind(dialog: CustomDialog, v: View) {
                            val input = v.findViewById<EditText>(R.id.input)
                            val btnMove = v.findViewById<MaterialButton>(R.id.move)
                            val btnOk = v.findViewById<MaterialButton>(R.id.ok)
                            val btnCancel = v.findViewById<MaterialButton>(R.id.cancel)
                            input.setText(viewModel.uiState.value?.axis?.removeZero() ?: "0")

                            btnMove.clickNoRepeat {
                                val axis = input.text.toString().toFloatOrNull() ?: 0f
                                if (axis > 250f) {
                                    PopTip.show("坐标不能大于250")
                                } else {
                                    viewModel.move(axis)
                                }
                            }

                            btnOk.clickNoRepeat {
                                val axis = input.text.toString().toFloatOrNull() ?: 0f
                                if (axis > 250f) {
                                    PopTip.show("坐标不能大于250")
                                } else {
                                    viewModel.save(axis)
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