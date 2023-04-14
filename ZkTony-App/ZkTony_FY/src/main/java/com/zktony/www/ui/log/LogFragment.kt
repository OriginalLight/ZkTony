package com.zktony.www.ui.log

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.*
import com.zktony.core.base.BaseFragment
import com.zktony.core.dialog.messageDialog
import com.zktony.www.R
import com.zktony.www.common.adapter.LogAdapter
import com.zktony.www.databinding.FragmentLogBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LogFragment :
    BaseFragment<LogViewModel, FragmentLogBinding>(R.layout.fragment_log) {

    override val viewModel: LogViewModel by viewModel()

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
        adapter.onDeleteButtonClick = {
            messageDialog(
                title = "删除日志",
                message = "是否删除该日志？",
                block = { viewModel.delete(it) }
            )
        }
        binding.apply {
            recycleView.adapter = adapter
        }

    }

}