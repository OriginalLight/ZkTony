package com.zktony.www.ui.program

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.zktony.common.base.BaseFragment
import com.zktony.common.ext.clickNoRepeat
import com.zktony.common.ext.clickScale
import com.zktony.common.ext.removeZero
import com.zktony.www.R
import com.zktony.www.common.ext.volumeDialog
import com.zktony.www.databinding.FragmentProgramPlateBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProgramPlateFragment :
    BaseFragment<ProgramPlateViewModel, FragmentProgramPlateBinding>(R.layout.fragment_program_plate) {

    override val viewModel: ProgramPlateViewModel by viewModel()

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
                        if (it.plate != null) {
                            gradientPlate.setSize(it.plate.size)
                            gradientPlate.setData(it.holeList.map { hole -> hole.y to hole.enable })
                            selectAll.isEnabled = it.holeList.any { hole -> !hole.enable }
                            if (it.holeList.isNotEmpty()) {
                                volume.text =
                                    "[ 促凝剂: ${it.holeList[0].v1.removeZero()} μL, 胶体: ${it.holeList[0].v2.removeZero()} μL ]"
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
            with(back) {
                clickScale()
                clickNoRepeat {
                    findNavController().navigateUp()
                }
            }
            selectAll.clickNoRepeat {
                viewModel.selectAll()
            }
            gradientPlate.setOnItemClick {
                viewModel.selectHole(it)
            }
            volume.clickNoRepeat {
                val hole = viewModel.uiState.value.holeList[0]
                volumeDialog(
                    hole.v1,
                    hole.v2,
                ) { v1, v2 ->
                    viewModel.updateVolume(v1, v2)
                }
            }
        }
    }
}