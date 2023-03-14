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
import com.zktony.common.base.BaseFragment
import com.zktony.common.ext.afterTextChange
import com.zktony.common.ext.clickScale
import com.zktony.common.ext.removeZero
import com.zktony.common.ext.setEqualText
import com.zktony.www.R
import com.zktony.www.adapter.CalibrationDataAdapter
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
        initView()
    }

    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    adapter.submitList(it.caliData)
                    binding.apply {
                        select.text = listOf("泵一", "泵二", "泵三", "泵四")[it.pumpId]
                        if (it.expect > 0f) {
                            expect.setEqualText(it.expect.toString().removeZero())
                        }
                        if (it.actual > 0f) {
                            actual.setEqualText(it.actual.toString().removeZero())
                        }
                        addLiquid.isEnabled = it.expect > 0f && !it.lock && !it.work
                        save.isEnabled = it.expect > 0f && it.actual > 0f
                    }
                }
            }
        }
    }

    private fun initView() {
        arguments?.let {
            val id = it.getLong("id")
            if (id > 0) {
                viewModel.init(id)
            }
        }
        adapter.setOnDeleteButtonClick { viewModel.delete(it) }

        binding.apply {
            recycleView.adapter = adapter

            addLiquid.setOnClickListener {
                viewModel.addLiquid()
            }

            save.setOnClickListener {
                viewModel.save()
            }

            expect.afterTextChange {
                viewModel.expect(it.toFloatOrNull() ?: 0f)
            }

            actual.afterTextChange {
                viewModel.actual(it.toFloatOrNull() ?: 0f)
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

            with(back) {
                clickScale()
                setOnClickListener { findNavController().navigateUp() }
            }
        }
    }

}