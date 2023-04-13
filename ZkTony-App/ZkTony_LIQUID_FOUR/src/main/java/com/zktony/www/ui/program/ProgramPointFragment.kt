package com.zktony.www.ui.program

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
import com.zktony.core.ext.removeZero
import com.zktony.www.R
import com.zktony.www.common.ext.volumeDialog
import com.zktony.www.databinding.FragmentProgramPointBinding
import com.zktony.www.room.entity.Hole
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProgramPointFragment :
    BaseFragment<ProgramPointViewModel, FragmentProgramPointBinding>(R.layout.fragment_program_point) {
    override val viewModel: ProgramPointViewModel by viewModel()

    override fun onViewCreated(savedInstanceState: Bundle?) {
//        initFlowCollector()
//        initView()
    }

//    @SuppressLint("SetTextI18n")
//    private fun initFlowCollector() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.uiState.collect {
//                    if (it.plate != null) {
//                        binding.apply {
//                            title.text = when (it.plate.index) {
//                                0 -> "一号孔板"
//                                1 -> "二号孔板"
//                                2 -> "三号孔板"
//                                3 -> "四号孔板"
//                                else -> "孔板"
//                            }
//                            dynamicPlate.run {
//                                x = it.plate.y
//                                y = it.plate.x
//                                data = it.holes.map { h -> Triple(h.x, h.y, h.enable) }
//                            }
//                            val holeList = it.holes.filter { hole -> hole.enable }
//                            selectAll.isEnabled = holeList.size != it.plate.x * it.plate.y
//                            custom.text = "自定义：${if (it.plate.custom == 0) '关' else '开'}"
//                            val h0 = if (it.holes.isNotEmpty()) it.holes[0] else Hole()
//                            volume.text = if (it.plate.custom == 0) "[ ${
//                                h0.v1.toString().removeZero()
//                            } μL, ${
//                                h0.v2.toString().removeZero()
//                            } μL, ${
//                                h0.v3.toString().removeZero()
//                            } μL, ${
//                                h0.v4.toString().removeZero()
//                            } μL ]" else "自定义,请单独设置每孔加液量！"
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun initView() {
//        arguments?.let {
//            val id = it.getLong("id")
//            val index = it.getInt("index")
//            if (id != 0L) {
//                viewModel.init(id, index)
//            }
//        }
//        binding.apply {
//            dynamicPlate.onItemClick = { x, y -> viewModel.select(x, y) }
//            volume.clickNoRepeat {
//                val holes = viewModel.uiState.value.holes[0]
//                val plate = viewModel.uiState.value.plate
//                if (plate?.custom == 1) return@clickNoRepeat
//                volumeDialog(
//                    v1 = holes.v1,
//                    v2 = holes.v2,
//                    v3 = holes.v3,
//                    v4 = holes.v4
//                ) { v1, v2, v3, v4 ->
//                    viewModel.setVolume(v1, v2, v3, v4)
//                }
//            }
//
//            with(back) {
//                clickScale()
//                clickNoRepeat {
//                    findNavController().navigateUp()
//                }
//            }
//            with(selectAll) {
//                clickScale()
//                clickNoRepeat {
//                    viewModel.selectAll()
//                }
//            }
//            with(custom) {
//                clickScale()
//                clickNoRepeat {
//                    viewModel.setCustom()
//                }
//            }
//        }
//    }
}