package com.zktony.www.ui.container

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.zktony.core.base.BaseFragment
import com.zktony.core.dialog.inputDecimalDialog
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
import com.zktony.core.ext.removeZero
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


    @SuppressLint("SetTextI18n")
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    binding.apply {
                        washPlate.text = (it?.axis ?: 0f).removeZero()
                        title.text = it?.name ?: "容器管理"
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
            washPlate.clickNoRepeat {
                inputDecimalDialog(
                    message = "请输入横坐标",
                    value = viewModel.uiState.value?.axis ?: 0f,
                    move = { viewModel.move(it) },
                    block = { viewModel.save(it) }
                )
            }

            with(back) {
                clickScale()
                clickNoRepeat { findNavController().navigateUp() }
            }
        }
    }

}