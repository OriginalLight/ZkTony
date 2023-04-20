package com.zktony.www.ui.program

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.*
import androidx.navigation.findNavController
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.databinding.FragmentProgramPointBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProgramPointFragment :
    BaseFragment<ProgramPointViewModel, FragmentProgramPointBinding>(R.layout.fragment_program_point) {
    override val viewModel: ProgramPointViewModel by viewModel()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    @SuppressLint("SetTextI18n")
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    binding.apply {
                        custom.text = if (it.custom) getString(R.string.custom_on) else getString(
                            R.string.custom_off
                        )
                        if (it.list.isEmpty()) {
                            selectAll.isEnabled = false
                            volume.text = "/"
                            title.text = "/"
                        } else {
                            selectAll.isEnabled = it.list.any { point -> !point.enable }
                            volume.text = if (!it.custom) "[ ${
                                it.list[0].v1.toString().removeZero()
                            } μL, ${
                                it.list[0].v2.toString().removeZero()
                            } μL, ${
                                it.list[0].v3.toString().removeZero()
                            } μL, ${
                                it.list[0].v4.toString().removeZero()
                            } μL ]" else "/"
                            title.text = when (it.list[0].index) {
                                0 -> getString(R.string.plate_one)
                                1 -> getString(R.string.plate_two)
                                2 -> getString(R.string.plate_three)
                                3 -> getString(R.string.plate_four)
                                else -> getString(R.string.plate_one)
                            }
                            with(dynamicPlate) {
                                x = it.list.maxOf { point -> point.y } + 1
                                y = it.list.maxOf { point -> point.x } + 1
                                data =
                                    it.list.map { point -> Triple(point.x, point.y, point.enable) }
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
            val index = it.getInt("index")
            if (id != 0L) {
                viewModel.init(id, index)
            }
        }
        binding.apply {
            dynamicPlate.onItemClick = { x, y -> viewModel.select(x, y) }
            volume.clickNoRepeat { viewModel.setVolume() }

            with(back) {
                clickScale()
                clickNoRepeat { findNavController().navigateUp() }
            }
            with(selectAll) {
                clickScale()
                clickNoRepeat { viewModel.selectAll() }
            }
            with(custom) {
                clickScale()
                clickNoRepeat { viewModel.custom() }
            }
        }
    }
}