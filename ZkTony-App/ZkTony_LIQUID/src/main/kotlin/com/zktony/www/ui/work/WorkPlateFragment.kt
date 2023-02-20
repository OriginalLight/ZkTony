package com.zktony.www.ui.work

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.zktony.common.base.BaseFragment
import com.zktony.common.extension.clickScale
import com.zktony.www.R
import com.zktony.www.adapter.WorkPlateAdapter
import com.zktony.www.databinding.FragmentWorkPlateBinding
import com.zktony.www.ui.calibration.CalibrationDataFragmentArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WorkPlateFragment :
    BaseFragment<WorkPlateViewModel, FragmentWorkPlateBinding>(R.layout.fragment_work_plate) {
    override val viewModel: WorkPlateViewModel by viewModels()

    private val adapter by lazy { WorkPlateAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    binding.apply {
                        help.isVisible = it.plates.isEmpty()
                        recyclerView.isVisible = it.plates.isNotEmpty()
                        adapter.submitList(it.plates)
                        btn1.isChecked = it.plates.find { plate -> plate.sort == 0 } != null
                        btn2.isChecked = it.plates.find { plate -> plate.sort == 1 } != null
                        btn3.isChecked = it.plates.find { plate -> plate.sort == 2 } != null
                        btn4.isChecked = it.plates.find { plate -> plate.sort == 3 } != null
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
        adapter.setOnEditButtonClick { plate ->
            findNavController().navigate(
                directions = WorkPlateFragmentDirections.actionNavigationWorkPlateToNavigationWorkHole(
                    plate.id
                )
            )
        }
        binding.apply {
            recyclerView.adapter = adapter
            btn1.setOnClickListener {
                viewModel.checkPlate(0)
            }
            btn2.setOnClickListener {
                viewModel.checkPlate(1)
            }
            btn3.setOnClickListener {
                viewModel.checkPlate(2)
            }
            btn4.setOnClickListener {
                viewModel.checkPlate(3)
            }
            with(back) {
                clickScale()
                setOnClickListener {
                    findNavController().navigateUp()
                }
            }
        }
    }
}