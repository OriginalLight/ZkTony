package com.zktony.www.ui.program

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.zktony.common.base.BaseFragment
import com.zktony.common.ext.clickNoRepeat
import com.zktony.common.ext.clickScale
import com.zktony.common.ext.removeZero
import com.zktony.www.R
import com.zktony.www.common.extension.volumeDialog
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

    @SuppressLint("SetTextI18n")
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    binding.apply {
                        if (it.plate != null) {
                            gradientPlate.setSize(it.plate.x)
                            gradientPlate.setData(it.holeList.map { hole -> hole.x to hole.enable })
                            selectAll.isEnabled = it.holeList.any { hole -> !hole.enable }
                            if (it.holeList.isNotEmpty()) {
                                volume.text =
                                    "[ 促凝剂:${it.holeList[0].v1.removeZero()} μL, 胶体:${it.holeList[0].v2.removeZero()} μL, 速度:${it.holeList[0].s1.removeZero()} rpm ]"
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
                setOnClickListener {
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
                    hole.s1
                ) { v1, v2, s1 ->
                    viewModel.updateVolume(v1, v2, s1)
                }
            }
        }
    }
}