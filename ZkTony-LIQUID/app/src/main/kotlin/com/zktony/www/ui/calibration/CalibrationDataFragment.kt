package com.zktony.www.ui.calibration

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.kongzue.dialogx.dialogs.PopMenu
import com.kongzue.dialogx.interfaces.OnIconChangeCallBack
import com.kongzue.dialogx.util.TextInfo
import com.zktony.www.R
import com.zktony.www.adapter.CalibrationDataAdapter
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.afterTextChange
import com.zktony.www.common.extension.clickScale
import com.zktony.www.databinding.FragmentCalibrationDataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CalibrationDataFragment :
    BaseFragment<CalibrationDataViewModel, FragmentCalibrationDataBinding>(R.layout.fragment_calibration_data) {
    override val viewModel: CalibrationDataViewModel by viewModels()

    private val adapter by lazy { CalibrationDataAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initRecycleView()
        initView()
        initEditText()
        initButton()
    }

    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    adapter.submitList(it.caliData)
                    binding.run {
                        select.text = listOf("泵一", "泵二", "泵三", "泵四")[it.pumpId]
                        addLiquid.isEnabled = it.expect > 0f && !it.lock && !it.work
                        save.isEnabled = it.expect > 0f && it.actual > 0f
                    }
                }
            }
        }
    }

    private fun initRecycleView() {
        binding.recycleView.adapter = adapter
        adapter.setOnDeleteButtonClick { data -> viewModel.delete(data) }
    }

    private fun initView() {
        arguments?.let {
            CalibrationDataFragmentArgs.fromBundle(it).id.run {
                if (this != "None") {
                    viewModel.init(this)
                }
            }
        }
    }

    private fun initButton() {
        binding.run {
            back.run {
                clickScale()
                setOnClickListener { findNavController().navigateUp() }
            }

            select.setOnClickListener {
                val menuList = listOf("泵一", "泵二", "泵三", "泵四")
                PopMenu.show(menuList).setMenuTextInfo(TextInfo().apply {
                    gravity = Gravity.CENTER
                    fontSize = 16
                }).setOnIconChangeCallBack(object : OnIconChangeCallBack<PopMenu>(true) {
                    override fun getIcon(dialog: PopMenu?, index: Int, menuText: String?): Int {
                        return R.mipmap.ic_pump
                    }
                }).setOnMenuItemClickListener { _, _, index ->
                    viewModel.selectPump(index)
                    false
                }.width = 300
            }

            addLiquid.setOnClickListener {
                viewModel.addLiquid()
            }

            save.setOnClickListener {
                viewModel.save()
            }

        }
    }

    private fun initEditText() {
        binding.run {
            expect.afterTextChange {
                viewModel.expect(it.toFloatOrNull() ?: 0f)
            }
            actual.afterTextChange {
                viewModel.actual(it.toFloatOrNull() ?: 0f)
            }
        }
    }

}