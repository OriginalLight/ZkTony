package com.zktony.www.ui.calibration

import android.os.Bundle
import android.view.Gravity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.kongzue.dialogx.dialogs.PopMenu
import com.kongzue.dialogx.util.TextInfo
import com.zktony.common.base.BaseFragment
import com.zktony.common.ext.*
import com.zktony.www.R
import com.zktony.www.common.adapter.CalibrationDataAdapter
import com.zktony.www.databinding.FragmentCalibrationDataBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CalibrationDataFragment :
    BaseFragment<CalibrationDataViewModel, FragmentCalibrationDataBinding>(R.layout.fragment_calibration_data) {

    override val viewModel: CalibrationDataViewModel by viewModel()

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
                        select.text = listOf("泵一", "泵二", "泵三")[it.pumpId]
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
            if (id != 0L) {
                viewModel.init(id)
            }
        }
        adapter.setOnDeleteButtonClick { viewModel.delete(it) }

        binding.apply {
            recycleView.adapter = adapter

            addLiquid.clickNoRepeat {
                viewModel.addLiquid()
            }

            save.clickNoRepeat {
                viewModel.save()
            }

            expect.afterTextChange {
                viewModel.expect(it.toFloatOrNull() ?: 0f)
            }

            actual.afterTextChange {
                viewModel.actual(it.toFloatOrNull() ?: 0f)
            }

            select.clickNoRepeat {
                val menuList = listOf("泵一", "泵二", "泵三")
                PopMenu.show(it, menuList).setMenuTextInfo(TextInfo().apply {
                    gravity = Gravity.CENTER
                    fontSize = 16
                }).setOnMenuItemClickListener { _, _, index ->
                    viewModel.selectPump(index)
                    false
                }.setOverlayBaseView(false).setRadius(0f).alignGravity =
                    Gravity.TOP or Gravity.CENTER_HORIZONTAL
            }

            with(back) {
                clickScale()
                setOnClickListener { findNavController().navigateUp() }
            }
        }
    }

}