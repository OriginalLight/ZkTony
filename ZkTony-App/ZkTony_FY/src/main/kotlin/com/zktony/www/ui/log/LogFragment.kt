package com.zktony.www.ui.log

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zktony.common.base.BaseFragment
import com.zktony.www.R
import com.zktony.www.common.adapter.LogAdapter
import com.zktony.www.databinding.FragmentLogBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LogFragment :
    BaseFragment<LogViewModel, FragmentLogBinding>(R.layout.fragment_log) {

    override val viewModel: LogViewModel by viewModels()

    private val adapter by lazy { LogAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    /**
     * 初始化观察者
     */
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.logList.collect {
                        adapter.submitList(it)
                        binding.apply {
                            recycleView.isVisible = it.isNotEmpty()
                            empty.isVisible = it.isEmpty()
                        }
                    }
                }
            }
        }
    }

    private fun initView() {
        adapter.setOnDeleteButtonClick {
            viewModel.delete(it)
        }
        binding.apply {
            recycleView.adapter = adapter
        }

    }

}