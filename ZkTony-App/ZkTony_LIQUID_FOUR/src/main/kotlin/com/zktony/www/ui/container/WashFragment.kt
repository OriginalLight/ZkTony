package com.zktony.www.ui.container

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zktony.common.base.BaseFragment
import com.zktony.common.ext.clickNoRepeat
import com.zktony.common.ext.removeZero
import com.zktony.www.R
import com.zktony.www.common.ext.positionDialog
import com.zktony.www.databinding.FragmentWashBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class WashFragment : BaseFragment<WashViewModel, FragmentWashBinding>(R.layout.fragment_wash) {

    override val viewModel: WashViewModel by viewModel()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initView()
        initFlowCollector()
    }

    private fun initView() {
        binding.position.clickNoRepeat {
            positionDialog(
                textX = viewModel.uiState.value!!.wasteX,
                textY = viewModel.uiState.value!!.wasteY,
                block1 = { x1, y1 -> viewModel.move(x1, y1) },
                block2 = { x2, y2 -> viewModel.save(x2, y2) }
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    if (it != null) {
                        binding.position.text =
                            "( ${it.wasteX.toString().removeZero()} , ${
                                it.wasteY.toString().removeZero()
                            } )"
                    }
                }
            }
        }
    }

}