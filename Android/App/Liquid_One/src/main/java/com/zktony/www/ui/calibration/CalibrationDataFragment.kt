package com.zktony.www.ui.calibration

import android.os.Bundle
import androidx.lifecycle.*
import androidx.navigation.findNavController
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
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
                        if (it.expect > 0f) {
                            expect.setEqualText(it.expect.toString().removeZero())
                        }
                        if (it.actual > 0f) {
                            actual.setEqualText(it.actual.toString().removeZero())
                        }
                        addLiquid.isEnabled = it.expect > 0f && !it.lock
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
        adapter.onDeleteButtonClick = { viewModel.delete(it) }

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

            with(back) {
                clickScale()
                clickNoRepeat { findNavController().navigateUp() }
            }
        }
    }

}