package com.zktony.www.ui.calibration

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kongzue.dialogx.dialogs.InputDialog
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.util.InputInfo
import com.zktony.www.R
import com.zktony.www.adapter.CalibrationAdapter
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.extension.showShortToast
import com.zktony.www.databinding.FragmentCalibrationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CalibrationFragment :
    BaseFragment<CalibrationViewModel, FragmentCalibrationBinding>(R.layout.fragment_calibration) {

    override val viewModel: CalibrationViewModel by viewModels()

    private val adapter by lazy { CalibrationAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initRecyclerView()
        initButton()
    }

    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    adapter.submitList(it)
                }
            }
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.adapter = adapter
        adapter.setOnCheckedClick { it -> viewModel.enable(it) }
        adapter.setOnDeleteButtonClick { it ->
            MessageDialog.show(
                "提示",
                "确定删除程序 ${it.name} 吗？",
                "确定",
                "取消"
            ).setOkButton { _, _ ->
                viewModel.delete(it)
                false
            }
        }
        adapter.setOnEditButtonClick { _ -> }
    }

    private fun initButton() {
        binding.add.run {
            clickScale()
            setOnClickListener {
                InputDialog("程序添加", "请输入程序名", "确定", "取消")
                    .setCancelable(false)
                    .setInputInfo(InputInfo().setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL))
                    .setOkButton { _, _, inputStr ->
                        if (inputStr.trim().isEmpty()) {
                            "程序名称不能为空".showShortToast()
                            return@setOkButton false
                        }
                        viewModel.insert(inputStr.trim())
                        false
                    }
                    .show()
            }
        }
    }
}