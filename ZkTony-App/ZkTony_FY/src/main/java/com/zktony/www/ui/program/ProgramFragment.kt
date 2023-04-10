package com.zktony.www.ui.program

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.zktony.core.base.BaseFragment
import com.zktony.core.dialog.deleteDialog
import com.zktony.core.dialog.inputDialog
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
import com.zktony.www.R
import com.zktony.www.common.adapter.ProgramAdapter
import com.zktony.www.databinding.FragmentProgramBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProgramFragment :
    BaseFragment<ProgramViewModel, FragmentProgramBinding>(R.layout.fragment_program) {

    override val viewModel: ProgramViewModel by viewModel()

    private val adapter by lazy { ProgramAdapter() }

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
                viewModel.programList.collect {
                    adapter.submitList(it)
                    binding.apply {
                        recycleView.isVisible = it.isNotEmpty()
                        empty.isVisible = it.isEmpty()
                    }
                }
            }
        }
    }

    private fun initView() {
        adapter.onEditButtonClick = {
            findNavController().navigate(
                R.id.action_navigation_program_to_navigation_action,
                Bundle().apply { putString("id", it.id) }
            )
        }

        adapter.onDeleteButtonClick = {
            deleteDialog(name = it.name, block = {
                viewModel.delete(it)
            })
        }

        binding.apply {
            recycleView.adapter = adapter

            with(btnAdd) {
                clickScale()
                clickNoRepeat { inputDialog { viewModel.insert(it) } }
            }
        }
    }

}