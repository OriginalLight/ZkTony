package com.zktony.www.ui.program

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.databinding.FragmentProgramEditBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProgramEditFragment :
    BaseFragment<ProgramEditViewModel, FragmentProgramEditBinding>(R.layout.fragment_program_edit) {
    override val viewModel: ProgramEditViewModel by viewModel()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    binding.apply {
                        oneCheck.isChecked = it.pointList.any { point -> point.index == 0 }
                        twoCheck.isChecked = it.pointList.any { point -> point.index == 1 }
                        threeCheck.isChecked = it.pointList.any { point -> point.index == 2 }
                        fourCheck.isChecked = it.pointList.any { point -> point.index == 3 }
                        oneCheck.isVisible = it.pointList.any { point -> point.index == 0 }
                        twoCheck.isVisible = it.pointList.any { point -> point.index == 1 }
                        threeCheck.isVisible = it.pointList.any { point -> point.index == 2 }
                        fourCheck.isVisible = it.pointList.any { point -> point.index == 3 }

                        with(dynamicPlateOne) {
                            isVisible = it.pointList.any { point -> point.index == 0 }

                            val list = it.pointList.filter { point -> point.index == 0 }
                            if (list.isNotEmpty()) {
                                data = list.map { point -> Triple(point.x, point.y, point.enable) }
                                holeOne.text = list.filter { point -> point.enable }.size.toString()
                                column = list.maxOf { point -> point.y } + 1
                                row = list.maxOf { point -> point.x } + 1
                            } else {
                                holeOne.text = ""
                            }
                        }
                        with(selectOne) {
                            isVisible = !it.pointList.any { point -> point.index == 0 }
                            text = if (it.containerList.isNotEmpty()) {
                                it.containerList[0].name
                            } else {
                                "/"
                            }
                        }

                        with(dynamicPlateTwo) {
                            isVisible = it.pointList.any { point -> point.index == 1 }

                            val list = it.pointList.filter { point -> point.index == 1 }
                            if (list.isNotEmpty()) {
                                data = list.map { point -> Triple(point.x, point.y, point.enable) }
                                holeTwo.text = list.filter { point -> point.enable }.size.toString()
                                column = list.maxOf { point -> point.y } + 1
                                row = list.maxOf { point -> point.x } + 1
                            } else {
                                holeTwo.text = ""
                            }
                        }

                        with(selectTwo) {
                            isVisible = !it.pointList.any { point -> point.index == 1 }
                            text = if (it.containerList.isNotEmpty()) {
                                it.containerList[0].name
                            } else {
                                "/"
                            }
                        }

                        with(dynamicPlateThree) {
                            isVisible = it.pointList.any { point -> point.index == 2 }

                            val list = it.pointList.filter { point -> point.index == 2 }
                            if (list.isNotEmpty()) {
                                data = list.map { point -> Triple(point.x, point.y, point.enable) }
                                holeThree.text =
                                    list.filter { point -> point.enable }.size.toString()
                                column = list.maxOf { point -> point.y } + 1
                                row = list.maxOf { point -> point.x } + 1
                            } else {
                                holeThree.text = ""
                            }
                        }

                        with(selectThree) {
                            isVisible = !it.pointList.any { point -> point.index == 2 }
                            text = if (it.containerList.isNotEmpty()) {
                                it.containerList[0].name
                            } else {
                                "/"
                            }
                        }

                        with(dynamicPlateFour) {
                            isVisible = it.pointList.any { point -> point.index == 3 }

                            val list = it.pointList.filter { point -> point.index == 3 }
                            if (list.isNotEmpty()) {
                                data = list.map { point -> Triple(point.x, point.y, point.enable) }
                                holeFour.text =
                                    list.filter { point -> point.enable }.size.toString()
                                column = list.maxOf { point -> point.y } + 1
                                row = list.maxOf { point -> point.x } + 1
                            } else {
                                holeFour.text = ""
                            }
                        }

                        with(selectFour) {
                            isVisible = !it.pointList.any { point -> point.index == 3 }
                            text = if (it.containerList.isNotEmpty()) {
                                it.containerList[0].name
                            } else {
                                "/"
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
            dynamicPlateOne.onItemClick = { _, _ ->
                findNavController().navigate(
                    R.id.action_navigation_program_edit_to_navigation_program_point,
                    Bundle().apply {
                        putLong("id", viewModel.uiState.value.id)
                        putInt("index", 0)
                    }
                )
            }
            dynamicPlateTwo.onItemClick = { _, _ ->
                findNavController().navigate(
                    R.id.action_navigation_program_edit_to_navigation_program_point,
                    Bundle().apply {
                        putLong("id", viewModel.uiState.value.id)
                        putInt("index", 1)
                    }
                )
            }
            dynamicPlateThree.onItemClick = { _, _ ->
                findNavController().navigate(
                    R.id.action_navigation_program_edit_to_navigation_program_point,
                    Bundle().apply {
                        putLong("id", viewModel.uiState.value.id)
                        putInt("index", 2)
                    }
                )
            }
            dynamicPlateFour.onItemClick = { _, _ ->
                findNavController().navigate(
                    R.id.action_navigation_program_edit_to_navigation_program_point,
                    Bundle().apply {
                        putLong("id", viewModel.uiState.value.id)
                        putInt("index", 3)
                    }
                )
            }
            selectOne.clickNoRepeat {
                val menu = viewModel.uiState.value.containerList.map { container ->
                    container.name
                }
                if (menu.isNotEmpty()) {
                    spannerDialog(
                        view = it,
                        menu = menu,
                        block = { _, index ->
                            viewModel.selectPoint(0, index)
                        }
                    )
                }
            }
            selectTwo.clickNoRepeat {
                val menu = viewModel.uiState.value.containerList.map { container ->
                    container.name
                }
                if (menu.isNotEmpty()) {
                    spannerDialog(
                        view = it,
                        menu = menu,
                        block = { _, index ->
                            viewModel.selectPoint(1, index)
                        }
                    )
                }
            }
            selectThree.clickNoRepeat {
                val menu = viewModel.uiState.value.containerList.map { container ->
                    container.name
                }
                if (menu.isNotEmpty()) {
                    spannerDialog(
                        view = it,
                        menu = menu,
                        block = { _, index ->
                            viewModel.selectPoint(2, index)
                        }
                    )
                }
            }
            selectFour.clickNoRepeat {
                val menu = viewModel.uiState.value.containerList.map { container ->
                    container.name
                }
                if (menu.isNotEmpty()) {
                    spannerDialog(
                        view = it,
                        menu = menu,
                        block = { _, index ->
                            viewModel.selectPoint(3, index)
                        }
                    )
                }
            }
            oneCheck.clickNoRepeat {
                viewModel.deletePoint(0)
            }
            twoCheck.clickNoRepeat {
                viewModel.deletePoint(1)
            }
            threeCheck.clickNoRepeat {
                viewModel.deletePoint(2)
            }
            fourCheck.clickNoRepeat {
                viewModel.deletePoint(3)
            }
            with(back) {
                clickScale()
                clickNoRepeat {
                    findNavController().navigateUp()
                }
            }
        }
    }
}