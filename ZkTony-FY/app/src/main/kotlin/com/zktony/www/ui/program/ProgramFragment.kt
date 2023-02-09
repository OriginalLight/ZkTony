package com.zktony.www.ui.program

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.zktony.www.R
import com.zktony.www.adapter.ProgramAdapter
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.extension.deleteDialog
import com.zktony.www.common.extension.inputDialog
import com.zktony.www.databinding.FragmentProgramBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ProgramFragment :
    BaseFragment<ProgramViewModel, FragmentProgramBinding>(R.layout.fragment_program) {

    override val viewModel: ProgramViewModel by viewModels()

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
        adapter.setOnEditButtonClick {
            findNavController().navigate(
                directions = ProgramFragmentDirections.actionNavigationProgramToNavigationAction(it.id)
            )
        }

        adapter.setOnDeleteButtonClick {
            deleteDialog(name = it.name, block = {
                viewModel.delete(it)
            })
        }

        binding.apply {
            recycleView.adapter = adapter

            with(btnAdd) {
                clickScale()
                setOnClickListener { inputDialog { viewModel.insert(it) } }
            }
        }
    }

}