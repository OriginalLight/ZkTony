package com.zktony.www.ui.container

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.positionDialog
import com.zktony.www.common.extension.removeZero
import com.zktony.www.databinding.FragmentWashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WashFragment : BaseFragment<WashViewModel, FragmentWashBinding>(R.layout.fragment_wash) {

    override val viewModel: WashViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initView()
        initFlowCollector()
    }

    private fun initView() {
        binding.position.setOnClickListener {
            positionDialog(
                textX = viewModel.uiState.value!!.x1,
                textY = viewModel.uiState.value!!.y1,
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
                            "( ${it.x1.toString().removeZero()} , ${
                                it.y1.toString().removeZero()
                            } )"
                    }
                }
            }
        }
    }

}