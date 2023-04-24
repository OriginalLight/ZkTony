package com.zktony.www.ui.program

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.navigation.findNavController
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
                        select.text = it.container?.name ?: "/"
                        selectAll.isVisible = it.list.any { point -> !point.enable }
                        if (it.list.isNotEmpty()) {
                            val vol = it.list[0].v1
                            if (vol != 0) {
                                volume.setEqualText(vol.toString())
                            } else {
                                volume.setEqualText("")
                            }
                            with(dynamicPlate) {
                                x = it.list.maxOf { point -> point.y } + 1
                                y = it.list.maxOf { point -> point.x } + 1
                                data =
                                    it.list.map { point -> Triple(point.x, point.y, point.enable) }
                            }
                        } else {
                            volume.setEqualText("")
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
            with(back) {
                clickScale()
                clickNoRepeat {
                    findNavController().navigateUp()
                }
            }
            select.clickNoRepeat {
                val menu = viewModel.uiState.value.containerList.map { n -> n.name }
                if (menu.isEmpty()) {
                    return@clickNoRepeat
                }
                spannerDialog(
                    view = it,
                    menu = menu,
                    block = { _, index -> viewModel.selectContainer(index) }
                )
            }
            with(selectAll) {
                clickScale()
                clickNoRepeat {
                    viewModel.enableAll()
                }
            }
            dynamicPlate.onItemClick = { x, y ->
                viewModel.enablePoint(x, y)
            }
            volume.afterTextChange {
                viewModel.updateVolume(it.toIntOrNull() ?: 0)
            }
        }
    }
}