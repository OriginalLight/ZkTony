package com.zktony.www.ui.work

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.extension.removeZero
import com.zktony.www.common.extension.volumeDialog
import com.zktony.www.databinding.FragmentWorkHoleBinding
import com.zktony.www.ui.calibration.CalibrationDataFragmentArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WorkHoleFragment :
    BaseFragment<WorkHoleViewModel, FragmentWorkHoleBinding>(R.layout.fragment_work_hole) {
    override val viewModel: WorkHoleViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    @SuppressLint("SetTextI18n")
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    if (it.plate != null) {
                        binding.apply {
                            title.text = when (it.plate.sort) {
                                0 -> "一号孔板"
                                1 -> "二号孔板"
                                2 -> "三号孔板"
                                3 -> "四号孔板"
                                else -> "孔板"
                            }
                            dynamicPlate.run {
                                setRowAndColumn(it.plate.row, it.plate.column)
                                setData(it.holes)
                            }
                            selectAll.isEnabled = it.plate.count != it.plate.row * it.plate.column
                            volume.text = "[ ${
                                it.plate.v1.toString().removeZero()
                            } μL, ${
                                it.plate.v2.toString().removeZero()
                            } μL, ${
                                it.plate.v3.toString().removeZero()
                            } μL, ${
                                it.plate.v4.toString().removeZero()
                            } μL ]"
                        }
                    }
                }
            }
        }
    }

    private fun initView() {
        arguments?.let {
            CalibrationDataFragmentArgs.fromBundle(it).id.run {
                if (this != "None") {
                    viewModel.init(this)
                }
            }
        }
        binding.apply {
            dynamicPlate.setOnItemClick { x, y -> viewModel.select(x, y) }
            volume.setOnClickListener {
                val plate = viewModel.uiState.value.plate
                volumeDialog(
                    v1 = plate?.v1 ?: 0.0f,
                    v2 = plate?.v2 ?: 0.0f,
                    v3 = plate?.v3 ?: 0.0f,
                    v4 = plate?.v4 ?: 0.0f
                ) { v1, v2, v3, v4 ->
                    viewModel.setVolume(v1, v2, v3, v4)
                }
            }

            with(back) {
                clickScale()
                setOnClickListener {
                    findNavController().navigateUp()
                }
            }
            with(selectAll) {
                clickScale()
                setOnClickListener {
                    viewModel.selectAll()
                }
            }
        }
    }
}