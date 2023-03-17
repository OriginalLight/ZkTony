package com.zktony.www.ui.program

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.zktony.common.base.BaseFragment
import com.zktony.common.dialog.deleteDialog
import com.zktony.common.dialog.inputDialog
import com.zktony.common.ext.clickScale
import com.zktony.www.R
import com.zktony.www.common.adapter.ProgramAdapter
import com.zktony.www.databinding.FragmentProgramBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProgramFragment : BaseFragment<ProgramViewModel, FragmentProgramBinding>(R.layout.fragment_program) {
    override val viewModel: ProgramViewModel by viewModels()

    private val adapter by lazy { ProgramAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.workList.collect {
                    adapter.submitList(it)
                }
            }
        }
    }

    private fun initView() {
        adapter.apply {
            setOnDeleteButtonClick {
                deleteDialog(name = it.name, block = { viewModel.delete(it) })
            }
            setOnEditButtonClick {
                findNavController().navigate(
                    R.id.action_navigation_program_to_navigation_program_plate,
                    Bundle().apply { putLong("id", it.id) }
                )
            }
        }
        binding.apply {
            binding.apply {
                recyclerView.adapter = adapter
                with(add) {
                    clickScale()
                    setOnClickListener {
                        inputDialog { viewModel.insert(it) }
                    }
                }
            }
        }
    }
}