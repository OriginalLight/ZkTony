package com.zktony.www.ui.work

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.zktony.common.base.BaseFragment
import com.zktony.common.ext.clickScale
import com.zktony.www.R
import com.zktony.www.databinding.FragmentWorkPlateBinding
import com.zktony.www.ui.calibration.CalibrationDataFragmentArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WorkPlateFragment :
    BaseFragment<WorkPlateViewModel, FragmentWorkPlateBinding>(R.layout.fragment_work_plate) {
    override val viewModel: WorkPlateViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    binding.apply {
                        oneCheck.isChecked = it.workPlates.find { plate -> plate.sort == 0 } != null
                        twoCheck.isChecked = it.workPlates.find { plate -> plate.sort == 1 } != null
                        threeCheck.isChecked = it.workPlates.find { plate -> plate.sort == 2 } != null
                        fourCheck.isChecked = it.workPlates.find { plate -> plate.sort == 3 } != null
                        with(dynamicPlateOne) {
                            val workPlate = it.workPlates.find { workPlate -> workPlate.sort == 0 }
                            val holes = it.holes.filter { hole -> hole.plateId == workPlate?.id }
                            if (workPlate != null) {
                                setXY(workPlate.column, workPlate.row)
                                setData(holes)
                                holeOne.text = holes.filter { hole -> hole.checked }.size.toString()
                            } else {
                                val plate = it.plates.find { plate -> plate.sort == 0 }
                                if (plate != null) {
                                    setXY(plate.column, plate.row)
                                }
                                setData(emptyList())
                                holeOne.text = ""
                            }
                        }
                        with(dynamicPlateTwo) {
                            val workPlate = it.workPlates.find { workPlate -> workPlate.sort == 1 }
                            val holes = it.holes.filter { hole -> hole.plateId == workPlate?.id }
                            if (workPlate != null) {
                                setXY(workPlate.column, workPlate.row)
                                setData(holes)
                                holeTwo.text = holes.filter { hole -> hole.checked }.size.toString()
                            } else {
                                val plate = it.plates.find { plate -> plate.sort == 1 }
                                if (plate != null) {
                                    setXY(plate.column, plate.row)
                                }
                                setData(emptyList())
                                holeTwo.text = ""
                            }
                        }
                        with(dynamicPlateThree) {
                            val workPlate = it.workPlates.find { workPlate -> workPlate.sort == 2 }
                            val holes = it.holes.filter { hole -> hole.plateId == workPlate?.id }
                            if (workPlate != null) {
                                setXY(workPlate.column, workPlate.row)
                                setData(holes)
                                holeThree.text = holes.filter { hole -> hole.checked }.size.toString()
                            } else {
                                val plate = it.plates.find { plate -> plate.sort == 2 }
                                if (plate != null) {
                                    setXY(plate.column, plate.row)
                                }
                                setData(emptyList())
                                holeThree.text = ""
                            }
                        }
                        with(dynamicPlateFour) {
                            val workPlate = it.workPlates.find { workPlate -> workPlate.sort == 3 }
                            val holes = it.holes.filter { hole -> hole.plateId == workPlate?.id }
                            if (workPlate != null) {
                                setXY(workPlate.column, workPlate.row)
                                setData(holes)
                                holeFour.text = holes.filter { hole -> hole.checked }.size.toString()
                            } else {
                                val plate = it.plates.find { plate -> plate.sort == 3 }
                                if (plate != null) {
                                    setXY(plate.column, plate.row)
                                }
                                setData(emptyList())
                                holeFour.text = ""
                            }
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
            dynamicPlateOne.setOnClickListener {
                val plate = viewModel.uiState.value.workPlates.find { plate -> plate.sort == 0 }
                plate?.let {
                    findNavController().navigate(
                        directions = WorkPlateFragmentDirections.actionNavigationWorkPlateToNavigationWorkHole(
                            it.id
                        )
                    )
                }
            }
            dynamicPlateTwo.setOnClickListener {
                val plate = viewModel.uiState.value.workPlates.find { plate -> plate.sort == 1 }
                plate?.let {
                    findNavController().navigate(
                        directions = WorkPlateFragmentDirections.actionNavigationWorkPlateToNavigationWorkHole(
                            it.id
                        )
                    )
                }
            }
            dynamicPlateThree.setOnClickListener {
                val plate = viewModel.uiState.value.workPlates.find { plate -> plate.sort == 2 }
                plate?.let {
                    findNavController().navigate(
                        directions = WorkPlateFragmentDirections.actionNavigationWorkPlateToNavigationWorkHole(
                            it.id
                        )
                    )
                }
            }
            dynamicPlateFour.setOnClickListener {
                val plate = viewModel.uiState.value.workPlates.find { plate -> plate.sort == 3 }
                plate?.let {
                    findNavController().navigate(
                        directions = WorkPlateFragmentDirections.actionNavigationWorkPlateToNavigationWorkHole(
                            it.id
                        )
                    )
                }
            }
            with(back) {
                clickScale()
                setOnClickListener {
                    findNavController().navigateUp()
                }
                oneCheck.setOnClickListener {
                    viewModel.checkPlate(0)
                }
                twoCheck.setOnClickListener {
                    viewModel.checkPlate(1)
                }
                threeCheck.setOnClickListener {
                    viewModel.checkPlate(2)
                }
                fourCheck.setOnClickListener {
                    viewModel.checkPlate(3)
                }
            }
        }
    }
}