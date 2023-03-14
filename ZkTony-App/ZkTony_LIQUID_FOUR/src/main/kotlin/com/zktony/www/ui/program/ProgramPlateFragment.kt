package com.zktony.www.ui.program

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseFragment
import com.zktony.common.ext.clickScale
import com.zktony.www.R
import com.zktony.www.databinding.FragmentProgramPlateBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProgramPlateFragment :
    BaseFragment<WorkPlateViewModel, FragmentProgramPlateBinding>(R.layout.fragment_program_plate) {
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
                        oneCheck.isChecked = it.plateList.find { plate -> plate.index == 0 } != null
                        twoCheck.isChecked = it.plateList.find { plate -> plate.index == 1 } != null
                        threeCheck.isChecked = it.plateList.find { plate -> plate.index == 2 } != null
                        fourCheck.isChecked = it.plateList.find { plate -> plate.index == 3 } != null
                        with(dynamicPlateOne) {
                            val plate = it.plateList.find { plate -> plate.index == 0 }
                            val holes = it.holeList.filter { hole -> hole.subId == plate?.id }
                            if (plate != null) {
                                setXY(plate.x, plate.y)
                                setData(holes.map { h -> Triple(h.x, h.y, h.enable)})
                                holeOne.text = holes.filter { hole -> hole.enable }.size.toString()
                            } else {
                                val p0 = it.plates.find { p -> p.index == 0 }
                                if (p0 != null) {
                                    setXY(p0.x, p0.y)
                                }
                                setData(emptyList())
                                holeOne.text = ""
                            }
                        }
                        with(dynamicPlateTwo) {
                            val plate = it.plateList.find { plate -> plate.index == 1 }
                            val holes = it.holeList.filter { hole -> hole.subId == plate?.id }
                            if (plate != null) {
                                setXY(plate.x, plate.y)
                                setData(holes.map { h -> Triple(h.x, h.y, h.enable)})
                                holeTwo.text = holes.filter { hole -> hole.enable }.size.toString()
                            } else {
                                val p1 = it.plates.find { p -> p.index == 1 }
                                if (p1 != null) {
                                    setXY(p1.x, p1.y)
                                }
                                setData(emptyList())
                                holeTwo.text = ""
                            }
                        }
                        with(dynamicPlateThree) {
                            val plate = it.plateList.find { plate -> plate.index == 2 }
                            val holes = it.holeList.filter { hole -> hole.subId == plate?.id }
                            if (plate != null) {
                                setXY(plate.x, plate.y)
                                setData(holes.map { h -> Triple(h.x, h.y, h.enable)})
                                holeThree.text = holes.filter { hole -> hole.enable }.size.toString()
                            } else {
                                val p2 = it.plates.find { p -> p.index == 2 }
                                if (p2 != null) {
                                    setXY(p2.x, p2.y)
                                }
                                setData(emptyList())
                                holeThree.text = ""
                            }
                        }
                        with(dynamicPlateFour) {
                            val plate = it.plateList.find { plate -> plate.index == 3 }
                            val holes = it.holeList.filter { hole -> hole.subId == plate?.id }
                            if (plate != null) {
                                setXY(plate.x, plate.y)
                                setData(holes.map { h -> Triple(h.x, h.y, h.enable)})
                                holeFour.text = holes.filter { hole -> hole.enable }.size.toString()
                            } else {
                                val p3 = it.plates.find { p -> p.index == 3 }
                                if (p3 != null) {
                                    setXY(p3.x, p3.y)
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
            val id = it.getLong("id")
            if (id != 0L) {
                viewModel.init(id)
            }
        }
        binding.apply {
            dynamicPlateOne.setOnClickListener {
                val plate = viewModel.uiState.value.plateList.find { plate -> plate.index == 0 }
                if (plate != null) {
                    findNavController().navigate(
                        R.id.action_navigation_program_plate_to_navigation_program_hole,
                        Bundle().apply { putLong("id", plate.id) }
                    )
                } else {
                    PopTip.show("请先选中")
                }
            }
            dynamicPlateTwo.setOnClickListener {
                val plate = viewModel.uiState.value.plateList.find { plate -> plate.index == 1 }
                if (plate != null) {
                    findNavController().navigate(
                        R.id.action_navigation_program_plate_to_navigation_program_hole,
                        Bundle().apply { putLong("id", plate.id) }
                    )
                } else {
                    PopTip.show("请先选中")
                }
            }
            dynamicPlateThree.setOnClickListener {
                val plate = viewModel.uiState.value.plateList.find { plate -> plate.index == 2 }
                if (plate != null) {
                    findNavController().navigate(
                        R.id.action_navigation_program_plate_to_navigation_program_hole,
                        Bundle().apply { putLong("id", plate.id) }
                    )
                } else {
                    PopTip.show("请先选中")
                }
            }
            dynamicPlateFour.setOnClickListener {
                val plate = viewModel.uiState.value.plateList.find { plate -> plate.index == 3 }
                if (plate != null) {
                    findNavController().navigate(
                        R.id.action_navigation_program_plate_to_navigation_program_hole,
                        Bundle().apply { putLong("id", plate.id) }
                    )
                } else {
                    PopTip.show("请先选中")
                }
            }
            with(back) {
                clickScale()
                setOnClickListener {
                    findNavController().navigateUp()
                }
                oneCheck.setOnClickListener {
                    viewModel.selectPlate(0)
                }
                twoCheck.setOnClickListener {
                    viewModel.selectPlate(1)
                }
                threeCheck.setOnClickListener {
                    viewModel.selectPlate(2)
                }
                fourCheck.setOnClickListener {
                    viewModel.selectPlate(3)
                }
            }
        }
    }
}