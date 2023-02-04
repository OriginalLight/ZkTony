package com.zktony.www.ui.calibration

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.kongzue.dialogx.dialogs.InputDialog
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.util.InputInfo
import com.zktony.www.R
import com.zktony.www.adapter.CalibrationAdapter
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.extension.showShortToast
import com.zktony.www.databinding.FragmentCalibrationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CalibrationFragment :
    BaseFragment<CalibrationViewModel, FragmentCalibrationBinding>(R.layout.fragment_calibration) {

    override val viewModel: CalibrationViewModel by viewModels()

    private val adapter by lazy { CalibrationAdapter() }

    @Inject
    lateinit var appViewModel: AppViewModel

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initButton()
        initRecyclerView()
    }

    /**
     * 初始化Flow收集器
     */
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.calibrationList.collect {
                    adapter.submitList(it)
                    if (it.isEmpty()) {
                        binding.empty.visibility = View.VISIBLE
                        binding.recycleView.visibility = View.GONE
                    } else {
                        binding.empty.visibility = View.GONE
                        binding.recycleView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }


    /**
     * 初始化按钮
     */
    private fun initButton() {
        binding.add.run {
            clickScale()
            setOnClickListener {
                InputDialog("程序添加", "请输入程序名", "确定", "取消").setCancelable(false)
                    .setInputInfo(InputInfo().setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL))
                    .setOkButton { _, _, inputStr ->
                        if (inputStr.trim().isEmpty()) {
                            "程序名称不能为空".showShortToast()
                            return@setOkButton false
                        }
                        viewModel.add(inputStr.trim())
                        false
                    }.show()
            }
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        binding.recycleView.adapter = adapter
        adapter.setOnEditButtonClick {
            val direction =
                CalibrationFragmentDirections.actionNavigationCalibrationToNavigationCalibrationData(
                    it.id
                )
            findNavController().navigate(direction)
        }
        adapter.setOnDeleteButtonClick {
            MessageDialog.show(
                "提示", "确定删除配置 ${it.name} 吗？", "确定", "取消"
            ).setOkButton { _, _ ->
                viewModel.delete(it)
                false
            }
        }
        adapter.setOnSelectClick {
            MessageDialog.show(
                "提示", "确定使用配置 ${it.name} 吗？", "确定", "取消"
            ).setOkButton { _, _ ->
                viewModel.select(it)
                false
            }
        }
    }
}