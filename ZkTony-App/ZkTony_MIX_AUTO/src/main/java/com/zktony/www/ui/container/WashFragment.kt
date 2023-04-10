package com.zktony.www.ui.container

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zktony.common.base.BaseFragment
import com.zktony.common.dialog.inputDecimalDialog
import com.zktony.common.ext.clickNoRepeat
import com.zktony.common.ext.removeZero
import com.zktony.www.R
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
        binding.washPlate.clickNoRepeat {
            inputDecimalDialog(
                message = "请输入横坐标",
                value = viewModel.uiState.value?.wasteY ?: 0f,
                move = { viewModel.move(it) },
                block = { viewModel.save(it) }
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    binding.apply {
                        washPlate.text = (it?.wasteY ?: 0f).removeZero()
                    }
                }
            }
        }
    }

}